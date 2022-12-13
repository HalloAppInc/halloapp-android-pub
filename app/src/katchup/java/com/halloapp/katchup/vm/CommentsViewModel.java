package com.halloapp.katchup.vm;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.util.ComputableLiveData;

public class CommentsViewModel extends ViewModel {

    private final ContentDb contentDb = ContentDb.getInstance();

    private ComputableLiveData<Post> postLiveData;

    public CommentsViewModel(String postId) {
        postLiveData = new ComputableLiveData<Post>() {
            @Override
            protected Post compute() {
                return contentDb.getPost(postId);
            }
        };
    }

    public LiveData<Post> getPost() {
        return postLiveData.getLiveData();
    }

    public static class CommentsViewModelFactory implements ViewModelProvider.Factory {

        private final String postId;

        public CommentsViewModelFactory(String postId) {
            this.postId = postId;
        }


        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CommentsViewModel.class)) {
                //noinspection unchecked
                return (T) new CommentsViewModel(postId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
