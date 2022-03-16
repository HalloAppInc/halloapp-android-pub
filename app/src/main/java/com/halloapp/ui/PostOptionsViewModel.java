package com.halloapp.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.protobuf.ByteString;
import com.halloapp.AppContext;
import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.crypto.CryptoByteUtils;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.media.MediaUtils;
import com.halloapp.media.Uploader;
import com.halloapp.proto.clients.Container;
import com.halloapp.proto.clients.PostContainer;
import com.halloapp.proto.server.ExternalSharePost;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.OgTagInfo;
import com.halloapp.proto.server.UploadMedia;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.ExternalShareResponseIq;
import com.halloapp.xmpp.HalloIq;
import com.halloapp.xmpp.MediaUploadIq;
import com.halloapp.xmpp.feed.FeedContentEncoder;
import com.halloapp.xmpp.util.Observable;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class PostOptionsViewModel extends ViewModel {

    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private final ContentDb contentDb = ContentDb.getInstance();
    private final AppContext appContext = AppContext.getInstance();

    final ComputableLiveData<Post> post;
    final MutableLiveData<Boolean> postDeleted = new MutableLiveData<>();

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        @Override
        public void onPostRetracted(@NonNull Post retractedPost) {
            final Post post = PostOptionsViewModel.this.post.getLiveData().getValue();
            if (post != null && post.senderUserId.equals(retractedPost.senderUserId) && post.id.equals(retractedPost.id)) {
                postDeleted.postValue(true);
            }
        }

    };

    private PostOptionsViewModel(@NonNull String postId, boolean isArchived) {
        post = new ComputableLiveData<Post>() {
            @Override
            protected Post compute() {
                return isArchived ? contentDb.getArchivePost(postId) : contentDb.getPost(postId);
            }
        };

        contentDb.addObserver(contentObserver);
    }

    public LiveData<Boolean> savePostToGallery() {
        MutableLiveData<Boolean> success = new MutableLiveData<>();
        Post post = this.post.getLiveData().getValue();
        if (post == null) {
            success.setValue(false);
            return success;
        }
        Context context = appContext.get();
        bgWorkers.execute(() -> {
            for (Media media : post.media) {
                if (!media.canBeSavedToGallery()) {
                    Log.e("PostOptionsViewModel.savePostToGallery attempted to save an incomplete video stream");
                    continue;
                }
                if (!MediaUtils.saveMediaToGallery(context, media)) {
                    success.postValue(false);
                    Log.e("PostOptionsViewModel/savePostToGallery failed to save media to gallery: " + media);
                    return;
                }
            }
            success.postValue(true);
        });
        return success;
    }

    public LiveData<String> shareExternally() {
        MutableLiveData<String> result = new MutableLiveData<>();
        Post post = this.post.getLiveData().getValue();
        if (post == null) {
            result.setValue(null);
            return result;
        }

        bgWorkers.execute(() -> {
            Container.Builder containerBuilder = Container.newBuilder();
            FeedContentEncoder.encodePost(containerBuilder, post);
            PostContainer postContainer = containerBuilder.getPostContainer();
            byte[] payload = postContainer.toByteArray();

            Context context = appContext.get();
            String title = context.getString(R.string.external_share_title, Me.getInstance().getName());
            String thumbnailUrl = null;
            final String description;
            Media media = null;
            if (post.media.isEmpty()) {
                description = context.getString(R.string.external_share_description_text);
            } else {
                if (post.type == Post.TYPE_VOICE_NOTE) {
                    description = context.getString(R.string.external_share_description_audio);
                    if (post.media.size() > 1) {
                        media = post.media.get(1);
                    }
                } else {
                    media = post.media.get(0);
                    description = context.getString(R.string.external_share_description_media);
                }
            }

            if (media != null) {
                try {
                    Bitmap bitmap = MediaUtils.decode(media.file, media.type, Constants.MAX_EXTERNAL_SHARE_THUMB_DIMENSION);
                    if (bitmap != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, Constants.JPEG_QUALITY, baos);
                        byte[] thumbnail = baos.toByteArray();
                        MediaUploadIq.Urls urls = Connection.getInstance().requestMediaUpload(thumbnail.length, null, UploadMedia.Type.DIRECT).await();
                        Uploader.run(new ByteArrayInputStream(thumbnail), null, Media.MEDIA_TYPE_IMAGE, urls.putUrl, percent -> true, "external-" + post.id);
                        thumbnailUrl = urls.getUrl;
                    }
                } catch (IOException e) {
                    Log.e("Failed to decode media for sharing", e);
                } catch (ObservableErrorException e) {
                    Log.e("Observable failure getting urls", e);
                } catch (InterruptedException e) {
                    Log.e("Interrupted while getting urls", e);
                }
            }

            byte[] attachmentKey = new byte[15];
            new SecureRandom().nextBytes(attachmentKey);
            try {
                byte[] fullKey = CryptoUtils.hkdf(attachmentKey, null, "HalloApp Share Post".getBytes(StandardCharsets.UTF_8), 80);
                byte[] iv = Arrays.copyOfRange(fullKey, 0, 16);
                byte[] aesKey = Arrays.copyOfRange(fullKey, 16, 48);
                byte[] hmacKey = Arrays.copyOfRange(fullKey, 48, 80);

                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(iv));
                byte[] encrypted = cipher.doFinal(payload);

                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(hmacKey, "HmacSHA256"));
                byte[] hmac = mac.doFinal(encrypted);

                byte[] encryptedPayload = CryptoByteUtils.concat(encrypted, hmac);

                final String thumbnailUrlCopy = thumbnailUrl;
                final Observable<ExternalShareResponseIq> observable = Connection.getInstance().sendRequestIq(new HalloIq() {
                    @Override
                    public Iq.Builder toProtoIq() {
                        OgTagInfo.Builder ogTagInfo = OgTagInfo.newBuilder()
                                .setTitle(title)
                                .setDescription(description);
                        if (thumbnailUrlCopy != null) {
                            ogTagInfo.setThumbnailUrl(thumbnailUrlCopy);
                        }

                        ExternalSharePost externalSharePost = ExternalSharePost.newBuilder()
                                .setAction(ExternalSharePost.Action.STORE)
                                .setBlob(ByteString.copyFrom(encryptedPayload))
                                .setExpiresInSeconds(3 * 60 * 60 * 24)
                                .setOgTagInfo(ogTagInfo)
                                .build();
                        return Iq.newBuilder()
                                .setId(RandomId.create())
                                .setType(Iq.Type.SET)
                                .setExternalSharePost(externalSharePost);
                    }
                });
                observable.onError(e -> Log.e("Failed to send for external sharing"))
                        .onResponse(response -> {
                            Log.i("Got external sharing response " + response);
                            String url = "https://share.halloapp.com/" + response.blobId + "#k" + Base64.encodeToString(attachmentKey, Base64.NO_WRAP | Base64.URL_SAFE);
                            result.postValue(url);
                        });
            } catch (GeneralSecurityException e) {
                Log.e("Failed to encrypt for external sharing", e);
            }
        });

        return result;
    }

    @Override
    protected void onCleared() {
        contentDb.removeObserver(contentObserver);
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final String postId;
        private final boolean isArchived;

        Factory(@NonNull String postId, boolean isArchived) {
            this.postId = postId;
            this.isArchived = isArchived;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(PostOptionsViewModel.class)) {
                //noinspection unchecked
                return (T) new PostOptionsViewModel(postId, isArchived);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
