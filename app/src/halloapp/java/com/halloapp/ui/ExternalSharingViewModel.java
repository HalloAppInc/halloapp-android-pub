package com.halloapp.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.protobuf.ByteString;
import com.halloapp.AppContext;
import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ExternalShareInfo;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.crypto.CryptoByteUtils;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.media.MediaUtils;
import com.halloapp.media.Uploader;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.clients.Container;
import com.halloapp.proto.clients.PostContainer;
import com.halloapp.proto.clients.PostContainerBlob;
import com.halloapp.proto.server.ExternalSharePost;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.OgTagInfo;
import com.halloapp.proto.server.UploadMedia;
import com.halloapp.ui.posts.PostScreenshotGenerator;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.DrawableUtils;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ExternalSharingViewModel extends ViewModel {

    private final Me me = Me.getInstance();
    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private final ContentDb contentDb = ContentDb.getInstance();
    private final AppContext appContext = AppContext.getInstance();
    private final ServerProps serverProps = ServerProps.getInstance();

    private final ComputableLiveData<Boolean> revocable;
    private final ComputableLiveData<String> title;
    private final ComputableLiveData<String> description;
    private final String postId;

    private ExternalSharingViewModel(@NonNull String postId) {
        this.postId = postId;
        revocable = new ComputableLiveData<Boolean>() {
            @Override
            protected Boolean compute() {
                ExternalShareInfo externalShareInfo = contentDb.getExternalShareInfo(postId);
                return externalShareInfo != null && externalShareInfo.shareId != null;
            }
        };
        title = new ComputableLiveData<String>() {
            @Override
            protected String compute() {
                return me.getName();
            }
        };
        description = new ComputableLiveData<String>() {
            @Override
            protected String compute() {
                Post post = contentDb.getPost(postId);
                if (post == null) {
                    return null;
                }
                if (post.type == Post.TYPE_VOICE_NOTE) {
                    return appContext.get().getString(R.string.external_share_description_audio);
                } else if (!TextUtils.isEmpty(post.text)) {
                    return post.text;
                } else {
                    return appContext.get().getString(R.string.external_share_description_media);
                }
            }
        };
    }

    public LiveData<Boolean> getIsRevokable() {
        return revocable.getLiveData();
    }

    public LiveData<String> getTitle() {
        return title.getLiveData();
    }

    public LiveData<String> getDescription() {
        return description.getLiveData();
    }

    public LiveData<Boolean> revokeLink() {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        bgWorkers.execute(() -> {
            ExternalShareInfo externalShareInfo = contentDb.getExternalShareInfo(postId);
            if (externalShareInfo == null || externalShareInfo.shareId == null) {
                Log.w("ExternalSharingViewModel/revokeLink failed to get external share info from db for link revoke " + postId);
                result.postValue(false);
                return;
            }
            Connection.getInstance().revokeSharedPost(externalShareInfo.shareId).onError(e -> {
                Log.w("ExternalSharingViewModel/revokeLink external share revoke failed", e);
                result.postValue(false);
            }).onResponse(res -> {
                contentDb.setExternalShareInfo(postId, null, null);
                result.postValue(true);
            });
        });
        return result;
    }

    public LiveData<Bitmap> getThumbnail() {
        MutableLiveData<Bitmap> result = new MutableLiveData<>();

        bgWorkers.execute(() -> {
            Post post = contentDb.getPost(postId);
            if (post == null) {
                Log.w("ExternalSharingViewModel/getThumbnail could not load post for media preview " + postId);
                result.postValue(null);
                return;
            }

            Media media = null;
            if (!post.media.isEmpty()) {
                if (post.type == Post.TYPE_VOICE_NOTE) {
                    if (post.media.size() > 1) {
                        media = post.media.get(1);
                    }
                } else {
                    media = post.media.get(0);
                }
            }

            if (media == null) {
                @DrawableRes int id = post.type == Post.TYPE_VOICE_NOTE ? R.drawable.audio_post_preview : R.drawable.text_post_preview;
                Bitmap bitmap = DrawableUtils.drawableToBitmap(ContextCompat.getDrawable(appContext.get(), id));
                result.postValue(bitmap);
                return;
            }

            try {
                Bitmap bitmap = MediaUtils.decode(media.file, media.type, Constants.MAX_EXTERNAL_SHARE_THUMB_DIMENSION);
                result.postValue(bitmap);
            } catch (IOException e) {
                Log.e("ExternalSharingViewModel/getThumbnail failed to decode media item for external share preview", e);
                result.postValue(null);
            }
        });

        return result;
    }

    public LiveData<Intent> shareExternallyWithPreview(@NonNull Context context, @NonNull String targetPackage, int previewIndex) {
        MutableLiveData<Intent> result = new MutableLiveData<>();
        bgWorkers.execute(() -> {
            Post post = contentDb.getPost(postId);
            Intent sendIntent;
            if (Constants.PACKAGE_INSTAGRAM.equals(targetPackage)) {
                Bitmap preview = PostScreenshotGenerator.generateScreenshotWithBackgroundSplit(context, post, previewIndex);
                if (preview != null) {
                    File postFile = FileStore.getInstance().getShareFile(postId + "-sticker.png");
                    saveImage(postFile, preview);
                    sendIntent = new Intent("com.instagram.share.ADD_TO_STORY");
                    sendIntent.putExtra("source_application", "5856403147724250");
                    Uri stickerUri = FileProvider.getUriForFile(context, "com.halloapp.fileprovider", postFile);
                    sendIntent.setType("image/png");
                    sendIntent.putExtra("interactive_asset_uri", stickerUri);
                    sendIntent.putExtra("top_background_color", "#000000");
                    sendIntent.putExtra("bottom_background_color", "#000000");
                    sendIntent.setPackage(targetPackage);
                    context.grantUriPermission(
                            "com.instagram.android", stickerUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    sendIntent = generateShareIntent(context, targetPackage);
                }
            } else if (Constants.PACKAGE_SNAPCHAT.equals(targetPackage)) {
                File postFile = FileStore.getInstance().getShareFile(postId + ".png");
                Bitmap preview = PostScreenshotGenerator.generateScreenshotWithBackgroundCombined(context, post, previewIndex);
                if (preview != null) {
                    saveImage(postFile, preview);
                    sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.setPackage(targetPackage);
                    sendIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, "com.halloapp.fileprovider", postFile));
                    sendIntent.setType("image/png");
                } else {
                    sendIntent = generateShareIntent(context, targetPackage);
                }
            } else {
                sendIntent = generateShareIntent(context, targetPackage);
            }
            result.postValue(sendIntent);
        });

        return result;
    }

    private Intent generateShareIntent(@NonNull Context context, String targetPackage) {
        String url = generateExternalShareUrl();
        String text = context.getString(R.string.external_share_copy, url);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setPackage(targetPackage);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");

        return sendIntent;
    }

    private void saveImage(File name, Bitmap bitmap) {
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(name);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            Log.e("ExternalSharingViewModel/saveImage failed to save image to " + name, e);
        }
    }

    public LiveData<String> shareExternally() {
        MutableLiveData<String> result = new MutableLiveData<>();

        bgWorkers.execute(() -> {
            result.postValue(generateExternalShareUrl());
        });

        return result;
    }

    @WorkerThread
    private String generateExternalShareUrl() {
        String domain = serverProps.getIsInternalUser() ? "share-test.halloapp.com" : "share.halloapp.com";
        Post post = contentDb.getPost(postId);
        if (post == null) {
            Log.w("ExternalSharingViewModel/shareExternally could not load post for external share encoding " + postId);
            return null;
        }

        ExternalShareInfo externalShareInfo = contentDb.getExternalShareInfo(postId);
        if (externalShareInfo != null && externalShareInfo.shareId != null && externalShareInfo.shareKey != null) {
            Log.i("ExternalSharingViewModel/shareExternally found stored share info for " + postId + " with shareId " + externalShareInfo.shareId);
            String url = "https://" + domain + "/" + externalShareInfo.shareId + "#k" + externalShareInfo.shareKey;
            return url;
        }

        Container.Builder containerBuilder = Container.newBuilder();
        FeedContentEncoder.encodePost(containerBuilder, post);
        PostContainer postContainer = containerBuilder.getPostContainer();
        PostContainerBlob postContainerBlob = PostContainerBlob.newBuilder()
                .setPostContainer(postContainer)
                .setPostId(post.id)
                .setUid(Long.parseLong(Me.getInstance().getUser()))
                .setTimestamp(post.timestamp / 1000L)
                .build();
        byte[] payload = postContainerBlob.toByteArray();

        Context context = appContext.get();
        String title = context.getString(R.string.external_share_title, me.getName());
        String thumbnailUrl = null;
        final String description;
        if (post.media.isEmpty()) {
            description = context.getString(R.string.external_share_description_text);
        } else {
            if (post.type == Post.TYPE_VOICE_NOTE) {
                description = context.getString(R.string.external_share_description_audio);
            } else {
                description = context.getString(R.string.external_share_description_media);
            }
        }

        try {
            Bitmap bitmap = generatePostThumb(post);
            if (bitmap != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, Constants.JPEG_QUALITY, baos);
                byte[] thumbnail = baos.toByteArray();
                MediaUploadIq.Urls urls = Connection.getInstance().requestMediaUpload(thumbnail.length, null, UploadMedia.Type.DIRECT).await();
                Uploader.run(new ByteArrayInputStream(thumbnail), null, Media.MEDIA_TYPE_IMAGE, urls.putUrl, percent -> true, "external-" + post.id);
                thumbnailUrl = urls.getUrl;
            }
        } catch (IOException e) {
            Log.e("ExternalSharingViewModel/shareExternally failed to decode media for sharing", e);
        } catch (ObservableErrorException e) {
            Log.e("ExternalSharingViewModel/shareExternally observable failure getting urls", e);
        } catch (InterruptedException e) {
            Log.e("ExternalSharingViewModel/shareExternally interrupted while getting urls", e);
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

                    long expiresInMs = Constants.POSTS_EXPIRATION - (System.currentTimeMillis() - post.timestamp);
                    ExternalSharePost externalSharePost = ExternalSharePost.newBuilder()
                            .setAction(ExternalSharePost.Action.STORE)
                            .setBlob(ByteString.copyFrom(encryptedPayload))
                            .setExpiresInSeconds(expiresInMs / 1000)
                            .setOgTagInfo(ogTagInfo)
                            .build();
                    return Iq.newBuilder()
                            .setId(RandomId.create())
                            .setType(Iq.Type.SET)
                            .setExternalSharePost(externalSharePost);
                }
            });
            String shareKey = Base64.encodeToString(attachmentKey, Base64.NO_WRAP | Base64.URL_SAFE);
            try {
                ExternalShareResponseIq response = observable.await();
                String url = "https://" + domain + "/" + response.blobId + "#k" + shareKey;
                contentDb.setExternalShareInfo(postId, response.blobId, shareKey);

                return url;
            } catch (ObservableErrorException | InterruptedException e) {
                Log.e("ExternalSharingViewModel/shareExternally failed to send for external sharing");
            }
        } catch (GeneralSecurityException e) {
            Log.e("ExternalSharingViewModel/shareExternally failed to encrypt for external sharing", e);
        }
        return null;
    }

    @Nullable
    private Bitmap generatePostThumb(@NonNull Post post) throws IOException {
        Media media = null;
        if (post.media.isEmpty()) {
            return null;
        } else {
            if (post.type == Post.TYPE_VOICE_NOTE) {
                if (post.media.size() > 1) {
                    media = post.media.get(1);
                    return MediaUtils.decode(media.file, media.type, Constants.MAX_EXTERNAL_SHARE_THUMB_DIMENSION);
                }
            } else if (post.type == Post.TYPE_MOMENT) {
                Media firstImg = post.media.get(0);
                Bitmap first = MediaUtils.decode(firstImg.file, firstImg.type, Constants.MAX_EXTERNAL_SHARE_THUMB_DIMENSION);
                Bitmap second = null;
                if (post.media.size() > 1) {
                    Media secondImg = post.media.get(1);
                    second = MediaUtils.decode(secondImg.file, secondImg.type, Constants.MAX_EXTERNAL_SHARE_THUMB_DIMENSION);
                }
                if (first == null) {
                    return null;
                }
                return PostScreenshotGenerator.combineMomentsBitmap(first, second, Constants.MAX_EXTERNAL_SHARE_THUMB_DIMENSION, 0);
            } else {
                media = post.media.get(0);
                return MediaUtils.decode(media.file, media.type, Constants.MAX_EXTERNAL_SHARE_THUMB_DIMENSION);
            }
        }
        return null;
    }


    public static class Factory implements ViewModelProvider.Factory {

        private final String postId;

        Factory(@NonNull String postId) {
            this.postId = postId;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ExternalSharingViewModel.class)) {
                //noinspection unchecked
                return (T) new ExternalSharingViewModel(postId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
