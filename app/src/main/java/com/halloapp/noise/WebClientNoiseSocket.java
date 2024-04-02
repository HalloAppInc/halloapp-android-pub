package com.halloapp.noise;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Group;
import com.halloapp.content.Media;
import com.halloapp.content.MomentPost;
import com.halloapp.content.Post;
import com.halloapp.content.SeenByInfo;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.keys.PrivateEdECKey;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.proto.clients.CommentContainer;
import com.halloapp.proto.clients.Container;
import com.halloapp.proto.clients.PostContainer;
import com.halloapp.proto.server.Audience;
import com.halloapp.proto.server.MediaCounters;
import com.halloapp.proto.server.NoiseMessage;
import com.halloapp.proto.web.FeedItem;
import com.halloapp.proto.web.FeedRequest;
import com.halloapp.proto.web.FeedResponse;
import com.halloapp.proto.web.FeedType;
import com.halloapp.proto.web.FeedUpdate;
import com.halloapp.proto.web.GroupDisplayInfo;
import com.halloapp.proto.web.GroupRequest;
import com.halloapp.proto.web.GroupResponse;
import com.halloapp.proto.web.PostDisplayInfo;
import com.halloapp.proto.web.ReceiptInfo;
import com.halloapp.proto.web.ReceiptUpdate;
import com.halloapp.proto.web.UserDisplayInfo;
import com.halloapp.proto.web.WebContainer;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.feed.FeedContentEncoder;
import com.halloapp.xmpp.groups.MemberElement;
import com.southernstorm.noise.protocol.CipherState;
import com.southernstorm.noise.protocol.CipherStatePair;
import com.southernstorm.noise.protocol.HandshakeState;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    private Context context;

    public WebClientNoiseSocket(@NonNull Me me, @NonNull Connection connection, @NonNull ContentDb contentDb, @NonNull ContactsDb contactsDb, @NonNull Context context) {
        this.me = me;
        this.connection = connection;
        this.contentDb = contentDb;
        this.contactsDb = contactsDb;
        this.context = context;
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

    public void finishHandshake(byte[] msgBContent) throws NoiseException, BadPaddingException, ShortBufferException {
        byte[] msgBuf = createMsgBuffer(BUFFER_SIZE);
        handshakeState.readMessage(msgBContent, 0, msgBContent.length, msgBuf,0);

        finishHandshake();
    }

    private void finishHandshake() throws NoiseException {
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
        WebContainer.Builder webContainerResponse = WebContainer.newBuilder();
        String requestId = "";

        if (webContainer.hasFeedRequest()) {
            Log.i("WebClientNoiseSocket handling feed request from web client");
            FeedRequest feedRequest = webContainer.getFeedRequest();
            FeedResponse response = getFeedResponse(feedRequest);
            requestId = feedRequest.getId();
            webContainerResponse.setFeedResponse(response);
        } else if (webContainer.hasPrivacyListRequest()) {
            Log.i("WebClientNoiseSocket handling incoming privacy list request from web client");
            return;
        } else if (webContainer.hasGroupRequest()) {
            Log.i("WebClientNoiseSocket handling incoming group request from web client");
            GroupRequest request = webContainer.getGroupRequest();
            GroupResponse response = getGroups(request.getId());
            requestId = request.getId();
            webContainerResponse.setGroupResponse(response);
        } else if (webContainer.hasReceiptUpdate()) {
            Log.i("WebClientNoiseSocket handling incoming receipt update from web client");
            handleReceiptUpdate(webContainer.getReceiptUpdate());
            return;
        } else {
            Log.e("WebClientNoiseSocket receiving invalid webcontainer" + webContainer);
            return;
        }
        sendMessageToWebClient(webContainerResponse.build().toByteArray(), requestId);
    }

    public void sendFeedUpdate(@NonNull ContentItem feedItem, boolean isRetracted) {
        FeedUpdate.Builder feedUpdate = FeedUpdate.newBuilder();
        String contentId = "";

        if (feedItem instanceof Post) {
            Post post = (Post) feedItem;
            feedUpdate.addItems(getFeedItem(post, isRetracted))
                    .addPostDisplayInfo(getPostDisplayInfo(post, isRetracted))
                    .addGroupDisplayInfo(getGroupDisplayInfo(post))
                    .addUserDisplayInfo(getUserDisplayInfo(post));
            contentId = post.id;
        } else if (feedItem instanceof Comment) {
            Comment comment = (Comment) feedItem;
            feedUpdate.addItems(getCommentFeedItem(comment, isRetracted));
            contentId = comment.id;
        }

        WebContainer webContainer = WebContainer.newBuilder().setFeedUpdate(feedUpdate).build();
        sendMessageToWebClient(webContainer.toByteArray(), contentId);
    }

    private void sendMessageToWebClient(byte[] message, String requestId) {
        try {
            byte[] encryptedResponse = encrypt(message);
            connection.sendMessageToWebClient(encryptedResponse, me.getWebClientStaticKey(), requestId);
        } catch (NoiseException | ShortBufferException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] encrypt(@NonNull byte[] message) throws NoiseException, ShortBufferException {
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

    private GroupResponse getGroups(String id) {
        GroupResponse.Builder response = GroupResponse.newBuilder().setId(id);
        List<Group> groups = contentDb.getGroups();

        for (Group group : groups) {
            response.addGroups(getGroupDisplayInfo(group));
        }
        return response.build();
    }

    private void handleReceiptUpdate(@NonNull ReceiptUpdate receiptUpdate) {
        ReceiptInfo receipt = receiptUpdate.getReceipt();
        if (receipt.getStatus() != ReceiptInfo.Status.SEEN) {
            throw new IllegalStateException("WebClientNoiseSocket receipts currently only support seen status and doesn't support your status of " + receipt.getStatus());
        }
        UserId userId = contentDb.getPost(receiptUpdate.getContentId()).senderUserId;
        connection.sendPostSeenReceipt(userId, receiptUpdate.getContentId());
    }

    private FeedResponse getFeedResponse(FeedRequest request) {
        FeedType feedType = request.getType();

        if (feedType == FeedType.HOME) {
            FeedResponse.Builder response = getHomeFeed(request.getId(), request.getCursor(), request.getLimit());
            return response.build();
        } else if (feedType == FeedType.GROUP) {
            FeedResponse.Builder response = getGroupFeed(request.getId(), request.getContentId(), request.getCursor(), request.getLimit());
            return response.build();
        } else if (feedType == FeedType.POST_COMMENTS) {
            FeedResponse.Builder response = getPostComments(request.getId(), request.getContentId(), request.getCursor(), request.getLimit());
            return response.build();
        } else if (feedType == FeedType.MOMENTS) {
            FeedResponse.Builder response = getMomentUpdate(request.getId(),request.getCursor(),  request.getLimit());
            return response.build();
        }
        return FeedResponse.newBuilder().setError(FeedResponse.Error.UNRECOGNIZED).build();
    }

    private FeedResponse.Builder getMomentUpdate(String id, String cursor, int limit) {
        FeedResponse.Builder responseBuilder = FeedResponse.newBuilder();
        List<MomentPost> moments;
        try {
            moments = contentDb.getMoments(!cursor.equals("") ? Long.parseLong(cursor) : null);
        } catch (NumberFormatException e) {
            return responseBuilder.setError(FeedResponse.Error.INVALID_CURSOR);
        }

        long lastMomentTimestamp = 0;
        for (int i = 0; i < Math.min(limit, moments.size()); i++) {
            MomentPost moment = moments.get(i);
            responseBuilder
                    .addItems(getFeedItem(moment, false))
                    .addUserDisplayInfo(getUserDisplayInfo(moment))
                    .addPostDisplayInfo(getPostDisplayInfo(moment, false));
            lastMomentTimestamp = moment.timestamp;
        }
        responseBuilder.setNextCursor(String.valueOf(lastMomentTimestamp));
        // TODO: uncomment the line below once the web client supports feedType.MOMENTS (moments currently only show up with no type set)
//        responseBuilder.setType(FeedType.MOMENTS);
        responseBuilder.setError(FeedResponse.Error.NONE);
        return responseBuilder;
    }
    
    private FeedResponse.Builder getHomeFeed(String id, String cursor, int limit) {
        FeedResponse.Builder responseBuilder = FeedResponse.newBuilder().setId(id);
        List<Post> posts;
        try {
            posts = contentDb.getPostsForWebClient(!cursor.equals("") ? Long.parseLong(cursor) : null, limit, true, null, false, false);
        } catch (NumberFormatException e) {
            return responseBuilder.setError(FeedResponse.Error.INVALID_CURSOR);
        }

        long lastPostTimestamp = 0;
        for (int i = 0; i < Math.min(limit, posts.size()); i++) {
            Post post = posts.get(i);
            responseBuilder
                    .addItems(getFeedItem(post, false))
                    .addUserDisplayInfo(getUserDisplayInfo(post))
                    .addGroupDisplayInfo(getGroupDisplayInfo(post))
                    .addPostDisplayInfo(getPostDisplayInfo(post, false));
            lastPostTimestamp = post.timestamp;
        }
        responseBuilder.setNextCursor(String.valueOf(lastPostTimestamp));
        responseBuilder.setType(FeedType.HOME);
        responseBuilder.setError(FeedResponse.Error.NONE);
        return responseBuilder;
    }

    private FeedResponse.Builder getGroupFeed(String id, String contentId, String cursor, int limit) {
        FeedResponse.Builder responseBuilder = FeedResponse.newBuilder().setId(id);
        GroupId groupId = GroupId.fromNullable(contentId);
        List<Post> posts;

        if (contentDb.getGroup(groupId) == null) {
            return responseBuilder.setError(FeedResponse.Error.UNRECOGNIZED);
        }
        try {
            posts = contentDb.getPostsForWebClient(!cursor.equals("") ? Long.parseLong(cursor) : null, limit, true, groupId, false, false);
        } catch (NumberFormatException e) {
            return responseBuilder.setError(FeedResponse.Error.INVALID_CURSOR);
        }

        long lastPostTimestamp = 0;
        for (int i = 0; i < Math.min(limit, posts.size()); i++) {
            Post post = posts.get(i);
            responseBuilder
                    .addItems(getFeedItem(post, false))
                    .addUserDisplayInfo(getUserDisplayInfo(post))
                    .addGroupDisplayInfo(getGroupDisplayInfo(post))
                    .addPostDisplayInfo(getPostDisplayInfo(post, false));
            lastPostTimestamp = post.timestamp;
        }
        responseBuilder.setNextCursor(String.valueOf(lastPostTimestamp));
        responseBuilder.setType(FeedType.GROUP);
        responseBuilder.setError(FeedResponse.Error.NONE);
        return responseBuilder;
    }

    private FeedResponse.Builder getPostComments(String id, String contentId, String cursor, int limit) {
        FeedResponse.Builder responseBuilder = FeedResponse.newBuilder().setId(id);
        List<Post> posts;

        Post post = contentDb.getPost(contentId);
        List<Comment> comments;
        int start = 0;
        try {
            start = !cursor.equals("") ? Integer.parseInt(cursor) : 0;
            comments = contentDb.getComments(post.id, start, limit);
        } catch (NumberFormatException e) {
            return responseBuilder.setError(FeedResponse.Error.INVALID_CURSOR);
        }

        for (Comment comment : comments) {
            start += 1;
            responseBuilder.addItems(getCommentFeedItem(comment, false));
        }
        responseBuilder.setType(FeedType.POST_COMMENTS);
        responseBuilder.setError(FeedResponse.Error.NONE);
        responseBuilder.setNextCursor(String.valueOf(start));
        return responseBuilder;
    }

    private UserDisplayInfo getUserDisplayInfo(Post post) {
        UserDisplayInfo.Builder builder = UserDisplayInfo.newBuilder();
        UserId userId = post.senderUserId;

        ContactsDb.ContactAvatarInfo contactAvatarInfo = contactsDb.getContactAvatarInfo(userId);
        if (contactAvatarInfo != null) {
            builder.setAvatarId(contactAvatarInfo.avatarId);
        }

        if (userId.isMe()) {
            userId = new UserId(me.getUser());
            builder.setContactName(context.getString(R.string.me));
        } else {
            builder.setContactName(contactsDb.getContact(userId).getDisplayName());
        }

        builder.setUid(userId.rawIdLong());
        return builder.build();
    }

    private GroupDisplayInfo getGroupDisplayInfo(Post post) {
        if (post.getParentGroup() == null) {
            return GroupDisplayInfo.newBuilder().build();
        }
        GroupId groupId = post.getParentGroup();
        Group group = contentDb.getGroup(groupId);
        return getGroupDisplayInfo(group);
    }

    private GroupDisplayInfo getGroupDisplayInfo(Group group) {
        GroupDisplayInfo.Builder builder = GroupDisplayInfo.newBuilder();
        GroupId groupId = group.groupId;

        builder.setId(groupId.rawId())
                .setName(group.name)
                .setExpiryInfo(group.expiryInfo)
                .setMembershipStatus(getMembershipStatus(groupId));

        if (group.groupAvatarId != null) {
            builder.setAvatarId(group.groupAvatarId);
        }
        if (group.groupDescription != null) {
            builder.setDescription(group.groupDescription);
        }
        return builder.build();
    }

    private PostDisplayInfo getPostDisplayInfo(@NonNull Post post, boolean isRetracted) {
        PostDisplayInfo.Builder postDisplayInfo = PostDisplayInfo.newBuilder()
                .setId(post.id)
                .setSeenState(convertSeenTypes(post))
                .setTransferState(PostDisplayInfo.TransferState.valueOf(post.transferred))
                .setRetractState(isRetracted ? PostDisplayInfo.RetractState.RETRACTED : PostDisplayInfo.RetractState.UNRETRACTED)
                .setUnreadComments(post.unseenCommentCount);

        List<SeenByInfo> seenByList = contentDb.getPostSeenByInfos(post.id);
        for (SeenByInfo seenByInfo : seenByList) {
            postDisplayInfo.addUserReceipts(ReceiptInfo.newBuilder()
                        .setUid(seenByInfo.userId.rawIdLong())
                        .setStatus(ReceiptInfo.Status.SEEN)
                        .setTimestamp(TimeUnit.MILLISECONDS.toSeconds(seenByInfo.timestamp))
                        .build());
        }
        return postDisplayInfo.build();
    }

    private FeedItem getFeedItem(@NonNull Post post, boolean isRetracted) {
        FeedItem.Builder builder = FeedItem.newBuilder();
        UserId userId = post.senderUserId;
        if (post.senderUserId.isMe()) {
            userId = new UserId(me.getUser());
        }

        if (post.getParentGroup() != null) {
            builder.setGroupId(post.getParentGroup().rawId());
        }

        com.halloapp.proto.server.Post.Builder postBuilder = com.halloapp.proto.server.Post.newBuilder()
                        .setId(post.id)
                        .setAudience(Audience.newBuilder().setTypeValue(post.type))
                        .setTimestamp(TimeUnit.MILLISECONDS.toSeconds(post.timestamp))
                        .setMediaCounters(getMediaCounter(post))
                        .setTag(com.halloapp.proto.server.Post.Tag.PUBLIC_POST)
                        .setPublisherUid(userId.rawIdLong());

        // web client expects an empty postContainer payload if the post is retracted
        if (isRetracted) {
            postBuilder.setPayload(ByteString.copyFrom(Container.newBuilder().setPostContainer(PostContainer.newBuilder().build()).build().toByteArray()));
        } else {
            Container.Builder container = Container.newBuilder();
            FeedContentEncoder.encodePost(container, contentDb.getPost(post.id));
            postBuilder.setPayload(ByteString.copyFrom(container.build().toByteArray()));
        }
        builder.setPost(postBuilder);
        builder.setExpiryTimestamp(post.expirationTime);
        return builder.build();
    }

    private FeedItem getCommentFeedItem(Comment comment, boolean isRetracted) {
        UserId userId = comment.senderUserId;
        if (comment.senderUserId.isMe()) {
            userId = new UserId(me.getUser());
        }

        com.halloapp.proto.server.Comment.Builder commentBuilder = com.halloapp.proto.server.Comment.newBuilder()
                .setId(comment.id)
                .setPostId(comment.postId)
                .setTimestamp(TimeUnit.MILLISECONDS.toSeconds(comment.timestamp))
                .setMediaCounters(getMediaCounter(comment))
                .setPublisherUid(userId.rawIdLong())
                .setPublisherName(contactsDb.getContact(userId).getDisplayName())
                .setCommentType(com.halloapp.proto.server.Comment.CommentType.COMMENT);

        if (isRetracted) {
            commentBuilder.setPayload(ByteString.copyFrom(Container.newBuilder().setCommentContainer(CommentContainer.newBuilder().build()).build().toByteArray()));
        } else {
            Container.Builder container = Container.newBuilder();
            FeedContentEncoder.encodeComment(container, comment);
            commentBuilder.setPayload(ByteString.copyFrom(container.build().toByteArray()));
        }

        return FeedItem.newBuilder().setComment(commentBuilder).build();
    }

    private MediaCounters getMediaCounter(@NonNull Post post) {
        List<Media> mediaList = post.media;
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

    private MediaCounters getMediaCounter(@NonNull Comment comment) {
        List<Media> mediaList = comment.media;
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

    private GroupDisplayInfo.MembershipStatus getMembershipStatus(@NonNull GroupId groupId) {
        List<MemberInfo> memberInfos = contentDb.getGroupMembers(groupId);
        for (MemberInfo memberInfo : memberInfos) {
            if (memberInfo.userId.isMe()) {
                if (memberInfo.type == MemberElement.Type.INVALID) {
                    return GroupDisplayInfo.MembershipStatus.NOT_MEMBER;
                }
                return memberInfo.isAdmin() ? GroupDisplayInfo.MembershipStatus.ADMIN : GroupDisplayInfo.MembershipStatus.MEMBER;
            }
        }
        return GroupDisplayInfo.MembershipStatus.NOT_MEMBER;
    }

    private PostDisplayInfo.SeenState convertSeenTypes(Post post) {
        if (post.seen == Post.SEEN_YES) {
            return PostDisplayInfo.SeenState.SEEN;
        } else if (post.seen == Post.SEEN_NO || post.seen == Post.SEEN_NO_HIDDEN) {
            return PostDisplayInfo.SeenState.UNSEEN;
        }
        return PostDisplayInfo.SeenState.UNRECOGNIZED;
    }
}
