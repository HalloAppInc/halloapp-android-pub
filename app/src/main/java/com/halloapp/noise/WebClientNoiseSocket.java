package com.halloapp.noise;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.Me;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Group;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.keys.PrivateEdECKey;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.proto.clients.Container;
import com.halloapp.proto.clients.PostContainer;
import com.halloapp.proto.clients.Text;
import com.halloapp.proto.server.Audience;
import com.halloapp.proto.server.MediaCounters;
import com.halloapp.proto.server.NoiseMessage;
import com.halloapp.proto.web.FeedItem;
import com.halloapp.proto.web.FeedRequest;
import com.halloapp.proto.web.FeedResponse;
import com.halloapp.proto.web.FeedType;
import com.halloapp.proto.web.GroupDisplayInfo;
import com.halloapp.proto.web.PostDisplayInfo;
import com.halloapp.proto.web.UserDisplayInfo;
import com.halloapp.proto.web.WebContainer;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.southernstorm.noise.protocol.CipherState;
import com.southernstorm.noise.protocol.CipherStatePair;
import com.southernstorm.noise.protocol.HandshakeState;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.ShortBufferException;

public class WebClientNoiseSocket {

    private static final String IK_PROTOCOL = "Noise_IK_25519_AESGCM_SHA256";
    private static final String KK_PROTOCOL = "Noise_KK_25519_AESGCM_SHA256";

    private static final int BUFFER_SIZE = 4096;
    private static final int NOISE_EXTRA_SIZE = 128; // Noise actually only needs 96 extra bytes, but using 128 just in case

    private final Me me;
    private HandshakeState handshakeState;

    private CipherState sendCrypto;
    private CipherState recvCrypto;

    private Connection connection;
    private ContentDb contentDb;
    private ContactsDb contactsDb;

    public WebClientNoiseSocket(@NonNull Me me, @NonNull Connection connection, @NonNull ContentDb contentDb, @NonNull ContactsDb contactsDb) {
        this.me = me;
        this.connection = connection;
        this.contentDb = contentDb;
        this.contactsDb = contactsDb;
    }

    @WorkerThread
    public void initialize(@NonNull byte[] connectionInfo, boolean useKK) throws NoiseException {
        byte[] noiseKey = me.getMyWebClientEd25519NoiseKey();
        if (noiseKey == null) {
            throw new NoiseException("Missing my web client key for noise authentication");
        }
        if (me.getWebClientStaticKey() == null) {
            throw new NoiseException("Missing web client static key for noise authentication");
        }
        initialize(noiseKey, connectionInfo, useKK);
    }

    public void initialize(@NonNull byte[] noiseKey, @NonNull byte[] connectionInfo, @NonNull boolean useKK) throws NoiseException {
        PublicEdECKey webClientStaticKey = me.getWebClientStaticKey();
        if (webClientStaticKey == null) {
            Log.e("WebClientNoiseSocket web client key doesn't exist. Make sure qrCode was successfully scanned/converted to key");
        }
        try {
            if (useKK) {
                performKKHandshake(connectionInfo, noiseKey, webClientStaticKey);
            } else {
                performIKHandshake(connectionInfo, noiseKey, webClientStaticKey);
            }
        } catch (IOException | NoSuchAlgorithmException | CryptoException | ShortBufferException e) {
            throw new NoiseException(e);
        }
    }

    private void performIKHandshake(@NonNull byte[] connectionInfo, byte[] localKeypair, PublicEdECKey webClientStaticKey) throws IOException, NoSuchAlgorithmException, CryptoException, ShortBufferException {
        handshakeState = new HandshakeState(IK_PROTOCOL, HandshakeState.INITIATOR);

        PrivateEdECKey priv = new PrivateEdECKey(Arrays.copyOfRange(localKeypair, 32, 96));
        byte[] convertedKey = CryptoUtils.convertPrivateEdToX(priv).getKeyMaterial();
        handshakeState.getLocalKeyPair().setPrivateKey(convertedKey, 0);
        handshakeState.getRemotePublicKey().setPublicKey(webClientStaticKey.getKeyMaterial(), 0);

        handshakeState.start();

        byte[] msgBuf = createMsgBuffer(connectionInfo.length);
        int msgALen = handshakeState.writeMessage(msgBuf, 0, connectionInfo, 0, connectionInfo.length);
        connection.sendNoiseMessageToWebClient(msgBuf, NoiseMessage.MessageType.IK_A, webClientStaticKey, msgALen);
    }

    private void performKKHandshake(@NonNull byte[] connectionInfo, byte[] localKeypair, PublicEdECKey webClientStaticKey) throws IOException, NoSuchAlgorithmException, CryptoException, ShortBufferException {
        handshakeState = new HandshakeState(KK_PROTOCOL, HandshakeState.INITIATOR);

        PrivateEdECKey priv = new PrivateEdECKey(Arrays.copyOfRange(localKeypair, 32, 96));
        byte[] convertedKey = CryptoUtils.convertPrivateEdToX(priv).getKeyMaterial();
        handshakeState.getLocalKeyPair().setPrivateKey(convertedKey, 0);
        handshakeState.getRemotePublicKey().setPublicKey(webClientStaticKey.getKeyMaterial(), 0);

        handshakeState.start();

        byte[] msgBuf = createMsgBuffer(connectionInfo.length);
        int msgALen = handshakeState.writeMessage(msgBuf, 0, connectionInfo, 0, connectionInfo.length);
        connection.sendNoiseMessageToWebClient(msgBuf, NoiseMessage.MessageType.KK_A, webClientStaticKey, msgALen);
    }

    public void receiveKKHandshake(byte[] msgAContent, byte[] connectionInfo) throws NoiseException, BadPaddingException, ShortBufferException, NoSuchAlgorithmException, CryptoException {
        byte[] msgABuf = createMsgBuffer(msgAContent.length);
        handshakeState = new HandshakeState(KK_PROTOCOL, HandshakeState.RESPONDER);

        byte[] localKeypair = me.getMyWebClientEd25519NoiseKey();
        PublicEdECKey webClientStaticKey = me.getWebClientStaticKey();

        PrivateEdECKey priv = new PrivateEdECKey(Arrays.copyOfRange(localKeypair, 32, 96));
        byte[] convertedKey = CryptoUtils.convertPrivateEdToX(priv).getKeyMaterial();
        handshakeState.getLocalKeyPair().setPrivateKey(convertedKey, 0);
        handshakeState.getRemotePublicKey().setPublicKey(webClientStaticKey.getKeyMaterial(), 0);

        handshakeState.start();
        handshakeState.readMessage(msgAContent, 0, msgAContent.length, msgABuf, 0);

        byte[] msgBuf = createMsgBuffer(connectionInfo.length);
        int msgBLen = handshakeState.writeMessage(msgBuf, 0, connectionInfo, 0, connectionInfo.length);
        connection.sendNoiseMessageToWebClient(msgBuf, NoiseMessage.MessageType.KK_B, me.getWebClientStaticKey(), msgBLen);

        finishHandshake();
    }

    public void receiveIKHandshake(byte[] msgBContent) throws NoiseException, BadPaddingException, ShortBufferException {
        byte[] msgBuf = createMsgBuffer(BUFFER_SIZE);
        handshakeState.readMessage(msgBContent, 0, msgBContent.length, msgBuf,0);

        finishHandshake();
    }

    public void finishHandshake() throws NoiseException {
        if (HandshakeState.SPLIT != handshakeState.getAction()) {
            throw new NoiseException("Web client handshake failed");
        }

        CipherStatePair crypto = handshakeState.split();

        sendCrypto = crypto.getSender();
        recvCrypto = crypto.getReceiver();
        Log.i("NoiseSocket handshake complete");
    }

    public void handleIncomingContainer(@NonNull byte[] encryptedWebContainer) throws ShortBufferException, BadPaddingException, InvalidProtocolBufferException, NoiseException {
        byte[] decryptedWebContainer = decrypt(encryptedWebContainer);
        WebContainer webContainer = WebContainer.parseFrom(decryptedWebContainer);

        if (webContainer.hasFeedRequest()) {
            Log.i("WebClientNoiseSocket handling feed request from web client");
            FeedRequest feedRequest = webContainer.getFeedRequest();
            FeedResponse response = getFeedResponse(feedRequest);


                if (response != null) {
                    WebContainer webContainerResponse = WebContainer.newBuilder()
                            .setFeedResponse(response)
                            .build();
                    try {
                        byte[] encryptedResponse = encrypt(webContainerResponse.toByteArray());
                        connection.sendMessageToWebClient(encryptedResponse, me.getWebClientStaticKey(), feedRequest.getId());
                    } catch (NoiseException | ShortBufferException e) {
                        throw new RuntimeException(e);
                    }
                }

        } else if (webContainer.hasPrivacyListRequest()) {
            Log.i("WebClientNoiseSocket handling incoming privacy list request from web client");
        }
    }

    private byte[] encrypt(@NonNull byte[]  message) throws NoiseException, ShortBufferException {
        if (sendCrypto == null) {
            throw new NoiseException("You have to authenticate first");
        }
        byte[] encryptedBytes = new byte[message.length + sendCrypto.getKeyLength()];
        int encryptedLength = sendCrypto.encryptWithAd(null, message, 0, encryptedBytes, 0, message.length);
        return Arrays.copyOf(encryptedBytes, encryptedLength);
    }

    private byte[] decrypt(byte[] message) throws ShortBufferException, BadPaddingException, NoiseException {
        if (recvCrypto == null) {
            throw new NoiseException("You have to authenticate first");
        }
        byte[] decryptedBytes = new byte[message.length];
        int decryptedLength = recvCrypto.decryptWithAd(null, message, 0, decryptedBytes, 0, message.length);
        return Arrays.copyOf(decryptedBytes, decryptedLength);
    }

    private byte[] createMsgBuffer(int initLength) {
        return new byte[Math.max(initLength + NOISE_EXTRA_SIZE, BUFFER_SIZE)];
    }

    @Nullable
    private FeedResponse getFeedResponse(FeedRequest request) {
        FeedType feedType = request.getType();

        if (feedType == FeedType.HOME) {
            FeedResponse.Builder response = getHomeFeed(request.getId(), request.getCursor(), request.getLimit());
            return response.build();
        } else if (feedType == FeedType.GROUP) {
            // pass
        } else if (feedType == FeedType.POST_COMMENTS) {
            //pass
        }
        return null;
    }

    @Nullable
    private FeedResponse.Builder getHomeFeed(String id, String cursor, int limit) {
        FeedResponse.Builder responseBuilder = FeedResponse.newBuilder();
        List<Post> posts;

        if (cursor.equals("")) {
            posts = contentDb.getPostsForWebClient(null, limit, true, null, false, false);
            for (int i = 0; i < 4; i++) { // TODO(Justin): hardcoding this to 4 (testing value for my device), since this will break on posts that don't belong to groups
                Post post = posts.get(i);
                responseBuilder
                        .addItems(getFeedItem(post))
                        .addUserDisplayInfo(getUserDisplayInfo(post))
                        .addGroupDisplayInfo(getGroupDisplayInfo(post))
                        .addPostDisplayInfo(getPostDisplayInfo(post))
                        .setError(FeedResponse.Error.NONE)
                        .setType(FeedType.HOME)
                        .setNextCursor(""); // TODO(Justin): update cursor to non-empty string and handle non-empty strings
            }
        } else {
            Log.i("Not able to retrieve any posts or no posts exist");
        }
        responseBuilder.setId(id);
        return responseBuilder;
    }

    private UserDisplayInfo getUserDisplayInfo(Post post) {
        UserDisplayInfo.Builder builder = UserDisplayInfo.newBuilder();
        UserId userId = post.senderUserId;

        if (post.senderUserId.isMe()) {
            userId = new UserId(me.getUser());
        }

        builder.setUid(userId.rawIdLong())
                .setContactName(contactsDb.getContact(userId).getDisplayName());

        return builder.build();
    }

    private GroupDisplayInfo getGroupDisplayInfo(Post post) {
        GroupDisplayInfo.Builder builder = GroupDisplayInfo.newBuilder();

        if (post.getParentGroup() == null) {
            return builder.build();
        }

        //TODO(Justin): missing background, membership status
        GroupId groupId = post.getParentGroup();
        Group group = contentDb.getGroup(groupId);

        builder.setId(groupId.rawId())
                .setName(group.name)
                .setExpiryInfo(group.expiryInfo);

        if (group.groupAvatarId != null) {
            builder.setAvatarId(group.groupAvatarId);
        }
        if (group.groupDescription != null) {
            builder.setDescription(group.groupDescription);
        }
        return builder.build();
    }

    private PostDisplayInfo getPostDisplayInfo(Post post) {
        return PostDisplayInfo.newBuilder()
                .setId(post.id)
                .setSeenState(PostDisplayInfo.SeenState.valueOf(post.seen))
                .setTransferState(PostDisplayInfo.TransferState.valueOf(post.transferred))
                .setRetractState(post.isRetracted() ? PostDisplayInfo.RetractState.RETRACTED : PostDisplayInfo.RetractState.UNRETRACTED)
                .build();
    }

    private FeedItem getFeedItem(Post post) {
        UserId userId = post.senderUserId;
        if (post.senderUserId.isMe()) {
            userId = new UserId(me.getUser());
        }

        // uncomment later, used for getting media to add to postcontainer
//        PostContainer.Builder postContainer = PostContainer.newBuilder();
//        if (post.hasMedia()) {
//            List<Media> mediaList = post.getMedia();
//            for (Media media : mediaList) {
//                if (media.type == Media.MEDIA_TYPE_IMAGE) {
//                    postContainer.setAlbum(Album.newBuilder()
//                                    .addMedia(AlbumMedia.newBuilder()
//                                            .setImage(Image.newBuilder()
//                                                    .setImg(EncryptedResource.newBuilder()
//                                                            .setDownloadUrl(media.url)
//                                                            .setEncryptionKey(ByteString.copyFrom(media.encSha256hash))
//                                                            .setCiphertextHash(ByteString.copyFrom(media.decSha256hash))
//                                                            .build())
//                                                    .setWidth(media.width)
//                                                    .setHeight(media.height)
//                                                    .build())
//                                            .build())
//                                    .build());
//                }
//            }
//        }

        return FeedItem.newBuilder()
                .setPost(com.halloapp.proto.server.Post.newBuilder()
                        .setId(post.id)
                        .setPayload(ByteString.copyFrom(
                                Container.newBuilder()
                                        .setPostContainer(PostContainer.newBuilder().setText(Text.newBuilder().setText(post.getText()).build()).build())
                                        .build()
                                        .toByteArray()
                        ))
                        .setAudience(Audience.newBuilder().setTypeValue(post.type))
                        .setTimestamp(post.timestamp)
                        .setMediaCounters(getMediaCounter(post))
                        .setTag(com.halloapp.proto.server.Post.Tag.PUBLIC_POST)
                        .setPublisherUid(userId.rawIdLong())
                )
                .setGroupId(String.valueOf(post.getParentGroup()))
                .setExpiryTimestamp(post.expirationTime)
                .build();
    }

    private MediaCounters getMediaCounter(Post post) {
        List<Media> mediaList = post.getMedia();
        int audio = 0, image = 0, video = 0;
        for (Media media : mediaList) {
            if (media.type == Media.MEDIA_TYPE_AUDIO) {
                audio += 1;
            } else if (media.type == Media.MEDIA_TYPE_VIDEO) {
                video += 1;
            } else if (media.type == Media.MEDIA_TYPE_IMAGE) {
                image += 1;
            }
        }
        return MediaCounters.newBuilder()
                .setNumImages(image)
                .setNumAudio(audio)
                .setNumVideos(video)
                .build();
    }
}
