package com.halloapp.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.AppContext;
import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
            if (shouldMergeMomentMedia(post)) {
                File mergedFile = FileStore.getInstance().getMediaFile(RandomId.create() + "." + Media.getFileExt(Media.MEDIA_TYPE_IMAGE));

                try {
                    mergeMomentImages(post.media.get(0).file, post.media.get(1).file, mergedFile);
                } catch (IOException e) {
                    success.postValue(false);
                    Log.e("PostOptionsViewModel/savePostToGallery failed to merge media images", e);
                    return;
                }

                MediaUtils.saveMediaToGallery(context, mergedFile, Media.MEDIA_TYPE_IMAGE);
            } else {
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
            }

            success.postValue(true);
        });
        return success;
    }

    private boolean shouldMergeMomentMedia(@NonNull Post post) {
        return post.type == Post.TYPE_MOMENT &&
                post.media.size() > 1 &&
                post.isAllMediaTransferred() &&
                post.media.get(0).type == Media.MEDIA_TYPE_IMAGE &&
                post.media.get(1).type == Media.MEDIA_TYPE_IMAGE;
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

    @NonNull
    @WorkerThread
    public static void mergeMomentImages(@NonNull File firstFile, @NonNull File secondFile, @NonNull File targetFile) throws IOException {
        Bitmap bitmap = MediaUtils.decodeImage(firstFile, Constants.MAX_IMAGE_DIMENSION);
        if (bitmap == null) {
            throw new IOException("cannot decode the first image");
        }

        int width = bitmap.getWidth() < bitmap.getHeight() ? bitmap.getWidth() * 2 : bitmap.getWidth();
        int height = bitmap.getHeight();

        Bitmap result = Bitmap.createBitmap(width, height, MediaUtils.getBitmapConfig(bitmap.getConfig()));
        Canvas canvas = new Canvas(result);

        // draw the image on the left
        int left = Math.max(bitmap.getWidth() / 2 - result.getWidth() / 4, 0);
        int right = Math.min(bitmap.getWidth() / 2 + result.getWidth() / 4, bitmap.getWidth());
        Rect source = new Rect(left, 0, right, bitmap.getHeight());
        Rect target = new Rect(0, 0, result.getWidth() / 2, result.getHeight());
        canvas.drawBitmap(bitmap, source, target, null);
        bitmap.recycle();

        bitmap = MediaUtils.decodeImage(secondFile, Constants.MAX_IMAGE_DIMENSION);
        if (bitmap == null) {
            throw new IOException("cannot decode the second image");
        }

        // draw the image on the right
        left = Math.max(bitmap.getWidth() / 2 - result.getWidth() / 4, 0);
        right = Math.min(bitmap.getWidth() / 2 + result.getWidth() / 4, bitmap.getWidth());
        source = new Rect(left, 0, right, bitmap.getHeight());
        target = new Rect(result.getWidth() / 2, 0, result.getWidth(), result.getHeight());
        canvas.drawBitmap(bitmap, source, target, null);
        bitmap.recycle();

        try (final FileOutputStream output = new FileOutputStream(targetFile)) {
            result.compress(Bitmap.CompressFormat.JPEG, Constants.JPEG_QUALITY, output);
        }
    }
}
