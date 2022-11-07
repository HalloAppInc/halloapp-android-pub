package com.halloapp.ui;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.AppContext;
import com.halloapp.Constants;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.MomentPost;
import com.halloapp.content.Post;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.logs.Log;

import java.io.File;
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
            if ((post instanceof MomentPost) && ((MomentPost) post).canMergeMedia()) {
                File momentMediaFile;

                try {
                    momentMediaFile = ((MomentPost) post).createThumb(Constants.MAX_IMAGE_DIMENSION);
                } catch (IOException e) {
                    success.postValue(false);
                    Log.e("PostOptionsViewModel/savePostToGallery failed to merge media images", e);
                    return;
                }

                MediaUtils.saveMediaToGallery(context, momentMediaFile, Media.MEDIA_TYPE_IMAGE);
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
