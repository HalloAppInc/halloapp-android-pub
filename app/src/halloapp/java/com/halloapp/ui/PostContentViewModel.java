package com.halloapp.ui;


import android.app.Application;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.FileStore;
import com.halloapp.Me;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.id.UserId;
import com.halloapp.media.ChunkedMediaParametersException;
import com.halloapp.media.Downloader;
import com.halloapp.media.ForeignRemoteAuthorityException;
import com.halloapp.media.VoiceNotePlayer;
import com.halloapp.proto.clients.PostContainerBlob;
import com.halloapp.proto.server.ExternalSharePostContainer;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.FileUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.ExternalShareRetrieveResponseIq;
import com.halloapp.xmpp.feed.FeedContentParser;
import com.halloapp.xmpp.util.Observable;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class PostContentViewModel extends AndroidViewModel {

    final ComputableLiveData<Post> post;
    final ComputableLiveData<Boolean> isRegistered;
    final MutableLiveData<Boolean> canInteract = new MutableLiveData<>();

    private final String postId;
    private final ContentDb contentDb;

    String backupName;

    private VoiceNotePlayer voiceNotePlayer;

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        @Override
        public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId) {
            if (postId.equals(PostContentViewModel.this.postId)) {
                invalidatePost();
            }
        }

        @Override
        public void onFeedCleanup() {
            invalidatePost();
        }
    };

    private PostContentViewModel(@NonNull Application application, @Nullable String postId, @Nullable String shareId, @Nullable String shareKey, boolean isArchived) {
        super(application);

        if (postId == null && (shareId == null || shareKey == null)) {
            throw new IllegalArgumentException("PostContentViewModel requires either a postId or both a shareId and a shareKey");
        }

        this.postId = postId;

        contentDb = ContentDb.getInstance();
        contentDb.addObserver(contentObserver);

        voiceNotePlayer = new VoiceNotePlayer(application);

        isRegistered = new ComputableLiveData<Boolean>() {
            @Override
            protected Boolean compute() {
                return Me.getInstance().isRegistered();
            }
        };

        post = new ComputableLiveData<Post>() {
            @Override
            protected Post compute() {
                if (postId != null) {
                    canInteract.postValue(!isArchived && Me.getInstance().isRegistered());
                    if (isArchived) {
                        return ContentDb.getInstance().getArchivePost(postId);
                    }
                    return ContentDb.getInstance().getPost(postId);
                } else {
                    final byte[] blob;
                    if (!Me.getInstance().isRegistered()) {
                        String remotePath = "https://share.halloapp.com/" + shareId + "?format=pb";
                        File localFile = FileStore.getInstance().getTmpFile(shareId);
                        try {
                            Downloader.runExternal(remotePath, localFile, null, "external-" + shareId);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            FileUtils.copyFile(new FileInputStream(localFile), baos);
                            byte[] payload = baos.toByteArray();
                            ExternalSharePostContainer externalSharePostContainer = ExternalSharePostContainer.parseFrom(payload);
                            backupName = externalSharePostContainer.getName();
                            blob = externalSharePostContainer.getBlob().toByteArray();
                        } catch (IOException e) {
                            Log.e("External post download failed", e);
                            return null;
                        } catch (GeneralSecurityException | ChunkedMediaParametersException | ForeignRemoteAuthorityException e) {
                            Log.e("Impossible exception while downloading external share post", e);
                            return null;
                        }
                    } else {
                        Observable<ExternalShareRetrieveResponseIq> observable = Connection.getInstance().getSharedPost(shareId);
                        try {
                            ExternalShareRetrieveResponseIq responseIq = observable.await();
                            blob = responseIq.blob;
                            backupName = responseIq.name;
                        } catch (ObservableErrorException e) {
                            Log.e("Failed observing shared post fetch", e);
                            return null;
                        } catch (InterruptedException e) {
                            Log.e("Interrupted while waiting for shared post fetch", e);
                            return null;
                        }
                    }

                    try {
                        byte[] attachmentKey = Base64.decode(shareKey, Base64.NO_WRAP | Base64.URL_SAFE);
                        byte[] encryptedMessage = Arrays.copyOfRange(blob, 0, blob.length - 32);
                        byte[] receivedHmac = Arrays.copyOfRange(blob, blob.length - 32, blob.length);

                        byte[] fullKey = CryptoUtils.hkdf(attachmentKey, null, "HalloApp Share Post".getBytes(StandardCharsets.UTF_8), 80);
                        byte[] iv = Arrays.copyOfRange(fullKey, 0, 16);
                        byte[] aesKey = Arrays.copyOfRange(fullKey, 16, 48);
                        byte[] hmacKey = Arrays.copyOfRange(fullKey, 48, 80);

                        Mac mac = Mac.getInstance("HmacSHA256");
                        mac.init(new SecretKeySpec(hmacKey, "HmacSHA256"));
                        byte[] computedHmac = mac.doFinal(encryptedMessage);

                        if (!Arrays.equals(computedHmac, receivedHmac)) {
                            Log.e("Hmac mismatch decrypting shared post");
                            return null;
                        }

                        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(iv));
                        byte[] decrypted = cipher.doFinal(encryptedMessage);

                        PostContainerBlob postContainerBlob = PostContainerBlob.parseFrom(decrypted);
                        String id = postContainerBlob.getPostId();
                        Post post = ContentDb.getInstance().getPost(id); // TODO(jack): specify poster id to prevent interception
                        if (post != null) {
                            Log.d("PostContentViewModel found post in db with id " + id);
                            canInteract.postValue(Me.getInstance().isRegistered());
                            return post;
                        }

                        canInteract.postValue(false);
                        UserId posterUserId = new UserId(Long.toString(postContainerBlob.getUid()));
                        FeedContentParser parser = new FeedContentParser(Me.getInstance());
                        post = parser.parsePost(id, posterUserId, postContainerBlob.getTimestamp() * 1000L, postContainerBlob.getPostContainer(), false);
                        post.transferred = Post.TRANSFERRED_YES;

                        return post;
                    } catch (GeneralSecurityException e) {
                        Log.e("Failed to decrypt shared post", e);
                    } catch (InvalidProtocolBufferException e) {
                        Log.e("Failed to parse shared post blob", e);
                    } catch (IllegalArgumentException e) {
                        Log.e("Failed to decode base64", e);
                    }
                    return null;
                }
            }
        };
    }

    @NonNull
    public VoiceNotePlayer getVoiceNotePlayer() {
        return voiceNotePlayer;
    }

    @Override
    protected void onCleared() {
        contentDb.removeObserver(contentObserver);
        voiceNotePlayer.onCleared();
    }

    private void invalidatePost() {
        post.invalidate();
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final String postId;
        private final String shareId;
        private final String shareKey;
        private final boolean isArchived;

        Factory(@NonNull Application application, @Nullable String postId, @Nullable String shareId, @Nullable String shareKey, boolean isArchived) {
            this.application = application;
            this.postId = postId;
            this.shareId = shareId;
            this.shareKey = shareKey;
            this.isArchived = isArchived;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(PostContentViewModel.class)) {
                //noinspection unchecked
                return (T) new PostContentViewModel(application, postId, shareId, shareKey, isArchived);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
