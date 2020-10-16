package com.halloapp.ui;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.id.UserId;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.util.ComputableLiveData;

public class PostContentViewModel extends AndroidViewModel {

    final ComputableLiveData<Post> post;

    private final String postId;
    private final ContentDb contentDb;

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

    private PostContentViewModel(@NonNull Application application, @NonNull String postId) {
        super(application);

        this.postId = postId;

        contentDb = ContentDb.getInstance();
        contentDb.addObserver(contentObserver);

        post = new ComputableLiveData<Post>() {
            @Override
            protected Post compute() {
                return ContentDb.getInstance().getPost(postId);
            }
        };
    }

    @Override
    protected void onCleared() {
        contentDb.removeObserver(contentObserver);
    }

    private void invalidatePost() {
        post.invalidate();
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final String postId;

        Factory(@NonNull Application application, @NonNull String postId) {
            this.application = application;
            this.postId = postId;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(PostContentViewModel.class)) {
                //noinspection unchecked
                return (T) new PostContentViewModel(application, postId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
