package com.halloapp.ui;

import androidx.annotation.NonNull;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.content.ContentDb;
import com.halloapp.content.Message;
import com.halloapp.content.MomentManager;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;

public class MomentViewerViewModel extends ViewModel {

    final ComputableLiveData<Post> post;
    final ComputableLiveData<Post> unlockingMoment;

    private final String postId;
    private final ContentDb contentDb;
    private final MomentManager momentManager;

    private LiveData<Boolean> unlockLiveData;

    private boolean loaded;
    private boolean uncovered;

    private MomentViewerViewModel(@NonNull String postId) {
        this.postId = postId;

        contentDb = ContentDb.getInstance();
        momentManager = MomentManager.getInstance();

        post = new ComputableLiveData<Post>() {
            @Override
            protected Post compute() {
                return contentDb.getPost(postId);
            }
        };

        unlockingMoment = new ComputableLiveData<Post>() {
            @Override
            protected Post compute() {
                String postId = contentDb.getUnlockingMomentId();
                if (postId != null) {
                    return contentDb.getPost(postId);
                }
                return null;
            }
        };
        contentDb.addObserver(new ContentDb.DefaultObserver() {

            @Override
            public void onPostAdded(@NonNull Post post) {
                if (post.type == Post.TYPE_MOMENT) {
                    unlockingMoment.invalidate();
                }
            }

            @Override
            public void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {
                if (unlockingMoment.getLiveData().getValue() == null || postId.equals(unlockingMoment.getLiveData().getValue().id)) {
                    unlockingMoment.invalidate();
                }
            }
        });
    }

    @Override
    protected void onCleared() {
        if (loaded && uncovered) {
            Post moment = post.getLiveData().getValue();
            if (moment != null && moment.isIncoming()) {
                contentDb.deleteMoment(moment);
            }
        }
    }

    public void sendMessage(String text) {
        Post moment = post.getLiveData().getValue();
        if (moment == null) {
            Log.e("MomentViewerViewModel/sendMessage no such moment to reply to");
            return;
        }
        Message msg = new Message(0,
                moment.senderUserId,
                UserId.ME,
                RandomId.create(),
                System.currentTimeMillis(),
                Message.TYPE_CHAT,
                Message.USAGE_CHAT,
                Message.STATE_INITIAL,
                text,
                moment.id,
                0,
                null,
                -1 ,
                null,
                0);
        msg.addToStorage(contentDb);
    }

    public void setLoaded() {
        loaded = true;
        if (uncovered) {
            sendSeenReceipt();
        }
    }

    private void sendSeenReceipt() {
        Post moment = post.getLiveData().getValue();
        if (moment == null || moment.isOutgoing()) {
            Log.e("MomentViewerViewModel/sendSeenReceipt no post");
            return;
        }
        contentDb.setIncomingPostSeen(moment.senderUserId, moment.id);
    }

    public void setUncovered() {
        uncovered = true;
        if (loaded) {
            sendSeenReceipt();
        }
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final String postId;

        Factory(@NonNull String postId) {
            this.postId = postId;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(MomentViewerViewModel.class)) {
                //noinspection unchecked
                return (T) new MomentViewerViewModel(postId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
