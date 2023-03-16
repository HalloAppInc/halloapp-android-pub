package com.halloapp.katchup.compose;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.ConnectionObservers;
import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.KatchupPost;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.id.UserId;
import com.halloapp.katchup.SelfiePostComposerActivity;
import com.halloapp.katchup.ShareIntentHelper;
import com.halloapp.katchup.media.MediaTranscoderTask;
import com.halloapp.katchup.media.PrepareLiveSelfieTask;
import com.halloapp.media.MediaUtils;
import com.halloapp.proto.server.MomentInfo;
import com.halloapp.ui.mediapicker.GalleryDataSource;
import com.halloapp.ui.mediapicker.GalleryItem;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.FileUtils;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class SelfieComposerViewModel extends AndroidViewModel {

    public SelfieComposerViewModel(@NonNull Application application, int contentType) {
        super(application);
        startTime = System.currentTimeMillis();
        this.contentType = contentType;
        dataSourceFactory = new GalleryDataSource.Factory(getApplication().getContentResolver(), false);
        mediaList = new LivePagedListBuilder<>(dataSourceFactory, 250).build();
        ConnectionObservers.getInstance().addObserver(connectionObserver);
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ComposeState.COMPOSING_CONTENT, ComposeState.COMPOSING_SELFIE, ComposeState.TRANSITIONING, ComposeState.READY_TO_SEND})
    public @interface ComposeState {
        int COMPOSING_CONTENT = 0;
        int COMPOSING_SELFIE = 1;
        int TRANSITIONING = 2;
        int READY_TO_SEND = 3;
    }

    private final BgWorkers bgWorkers = BgWorkers.getInstance();

    private final MutableLiveData<Integer> currentState = new MutableLiveData<>(ComposeState.COMPOSING_CONTENT);
    private final MutableLiveData<Bitmap> generatedImage = new MutableLiveData<>();
    private final GalleryDataSource.Factory dataSourceFactory;
    private final LiveData<PagedList<GalleryItem>> mediaList;

    private final Connection.Observer connectionObserver = new Connection.Observer() {
        @Override
        public void onAiImageReceived(@NonNull String id, @NonNull byte[] bytes, @NonNull String ackId) {
            if (id.equals(pendingAiImageId)) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                generatedImage.postValue(bitmap);
            }
        }
    };

    private String pendingAiImageId;

    private File selfieFile;

    public LiveData<Integer> getComposerState() {
        return currentState;
    }

    public LiveData<Bitmap> getGeneratedImage() {
        return generatedImage;
    }

    private float selfieX;
    private float selfieY;

    private int numTakes = 0;
    private int numSelfieTakes = 0;
    private long notificationTime;
    private long notificationId;
    private long startTime;
    private int contentType;

    private MediaTranscoderTask mediaTranscoderTask;

    public void setNotification(long notificationId, long notificationTime) {
        this.notificationId = notificationId;
        this.notificationTime = notificationTime;
    }

    public void onCapturedSelfie(@NonNull File selfieFile) {
        this.selfieFile = selfieFile;
        currentState.setValue(ComposeState.TRANSITIONING);
        numSelfieTakes++;
    }

    public void onTransitionComplete() {
        currentState.setValue(ComposeState.READY_TO_SEND);
    }

    public void onDiscardSelfie() {
        if (selfieFile != null) {
            final File fileToDelete = selfieFile;
            bgWorkers.execute(() -> {
                fileToDelete.delete();
            });
        }
        this.selfieFile = null;
        currentState.setValue(ComposeState.COMPOSING_SELFIE);
    }

    public void onComposedMedia(@NonNull Uri uri, @Media.MediaType int mediaType) {
        currentState.setValue(ComposeState.COMPOSING_SELFIE);
        numTakes++;
    }

    public void onComposedText(@NonNull String text, @ColorInt int color) {
        currentState.setValue(ComposeState.COMPOSING_SELFIE);
        numTakes++;
    }

    public void generateAiImage(@NonNull String text) {
        Connection.getInstance().sendAiImageRequest(text, 1).onResponse(res -> {
            if (res.success) {
                pendingAiImageId = res.id;
            }
        }).onError(err -> {
            Log.w("SelfieComposerViewModel AI image request failed", err);
        });
    }

    public boolean onBackPressed() {
        switch (currentState.getValue()) {
            case ComposeState.COMPOSING_CONTENT:
                return true;
            case ComposeState.COMPOSING_SELFIE:
                currentState.setValue(ComposeState.COMPOSING_CONTENT);
                return false;
            case ComposeState.TRANSITIONING:
            case ComposeState.READY_TO_SEND:
                currentState.setValue(ComposeState.COMPOSING_SELFIE);
                return false;
        }
        return true;
    }

    public LiveData<PagedList<GalleryItem>> getMediaList() {
        return mediaList;
    }

    public LiveData<KatchupPost> sendPost(@NonNull Media content, boolean isMirrored) {
        MutableLiveData<KatchupPost> sendResult = new MutableLiveData<>();

        final File selfiePostFile = FileStore.getInstance().getMediaFile(RandomId.create() + "." + Media.getFileExt(content.type));
        PrepareLiveSelfieTask prepareLiveSelfieTask = new PrepareLiveSelfieTask(selfieFile.getAbsolutePath(), selfiePostFile.getAbsolutePath(), isMirrored);
        mediaTranscoderTask = new MediaTranscoderTask(prepareLiveSelfieTask);

        mediaTranscoderTask.setListener(new MediaTranscoderTask.DefaultListener() {
            @Override
            public void onSuccess() {
                KatchupPost post = createPost(selfiePostFile, content);

                sendResult.postValue(post);
            }

            @Override
            public void onError(Exception e) {
                Log.e("SelfieComposerViewModel/sendPost failed to transcode", e);
                sendResult.postValue(null);
            }
        });
        mediaTranscoderTask.start();
        return sendResult;
    }

    private KatchupPost createPost(@NonNull File selfiePostFile, @NonNull Media content) {
        KatchupPost post = new KatchupPost(0, UserId.ME, RandomId.create(), System.currentTimeMillis(), Post.TRANSFERRED_NO, Post.SEEN_YES, "");
        post.selfieX = this.selfieX;
        post.selfieY = this.selfieY;
        post.numTakes = numTakes;
        post.numSelfieTakes = numSelfieTakes;
        post.notificationTimestamp = notificationTime;
        post.notificationId = notificationId;
        post.timeTaken = Math.max(System.currentTimeMillis() - startTime, 0);
        post.media.add(Media.createFromFile(Media.MEDIA_TYPE_VIDEO, selfiePostFile));
        addMedia(post, content);
        @PrivacyList.Type String audienceType;
        List<UserId> audienceList;

        List<Contact> contacts = ContactsDb.getInstance().getUsers();
        audienceList = new ArrayList<>(contacts.size());
        for (Contact contact : contacts) {
            audienceList.add(contact.userId);
        }
        audienceType = PrivacyList.Type.ALL;
        post.setAudience(audienceType, audienceList);
        post.commentKey = generateCommentKey();

        return post;
    }

    private byte[] generateCommentKey() {
        byte[] chainKey = EncryptedKeyStore.getInstance().getMyHomeChainKey(false);
        try {
            return CryptoUtils.hkdf(chainKey, null, new byte[] {0x07}, 64);
        } catch (GeneralSecurityException e) {
            Log.e("Failed to compute comment key", e);
            throw new IllegalStateException(e);
        }
    }

    public void setSelfiePosition(float x, float y) {
        Log.i("SelfieComposerViewModel/setSelfiePosition: selfie positioned at x=" + x + ", y=" + y);
        this.selfieX = x;
        this.selfieY = y;
    }

    private boolean addMedia(KatchupPost post, Media content) {
        final File postFile = FileStore.getInstance().getMediaFile(RandomId.create() + "." + Media.getFileExt(content.type));
        switch (content.type) {
            case Media.MEDIA_TYPE_IMAGE: {
                try {
                    MediaUtils.transcodeImage(content.file, postFile, null, Constants.MAX_IMAGE_DIMENSION, Constants.JPEG_QUALITY, true);
                    if (this.contentType == SelfiePostComposerActivity.Type.LIVE_CAPTURE || this.contentType == SelfiePostComposerActivity.Type.ALBUM_COMPOSE) {
                        post.contentType = MomentInfo.ContentType.IMAGE;
                    } else {
                        post.contentType = MomentInfo.ContentType.TEXT;
                    }
                } catch (IOException e) {
                    Log.e("failed to transcode image", e);
                    return false;
                }
                break;
            }
            case Media.MEDIA_TYPE_VIDEO: {
                try {
                    FileUtils.copyFile(content.file, postFile);
                    post.contentType = MomentInfo.ContentType.VIDEO;
                    break;
                } catch (IOException e) {
                    Log.e("failed to copy " + content.file.getAbsolutePath() + " to " + postFile.getAbsolutePath(), e);
                    return false;
                }
            }
            case Media.MEDIA_TYPE_AUDIO:
            case Media.MEDIA_TYPE_UNKNOWN:
            default: {
                Log.e("unknown/unsupported media type " + content.file.getAbsolutePath());
                return false;
            }
        }
        content.file = postFile;
        post.media.add(content);
        return true;
    }

    public void invalidateGallery() {
        dataSourceFactory.invalidateLatestDataSource();
    }

    public LiveData<Boolean> saveToGallery(@NonNull Context context, Post post) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        bgWorkers.execute(() -> {
            if (post == null) {
                Log.e("SelfieComposerViewModel/saveToGallery missing post");
                result.postValue(false);
                return;
            }

            try {
                ShareIntentHelper.prepareExternalShareVideo(post, false, input -> {
                    if (input == null) {
                        Log.e("SelfieComposerViewModel/saveToGallery failed to get transcoded file");
                        result.postValue(false);
                    } else {
                        MediaUtils.saveMediaToGallery(context, input, Media.MEDIA_TYPE_VIDEO);
                        result.postValue(true);
                    }
                    return null;
                });
            } catch (IOException e) {
                Log.e("SelfieComposerViewModel/saveToGallery failed", e);
                result.postValue(false);
            }
        });

        return result;
    }

    @Override
    protected void onCleared() {
        ConnectionObservers.getInstance().removeObserver(connectionObserver);
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final int contentType;

        public Factory(@NonNull Application application, int contentType) {
            this.application = application;
            this.contentType = contentType;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(SelfieComposerViewModel.class)) {
                //noinspection unchecked
                return (T) new SelfieComposerViewModel(application, contentType);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

}
