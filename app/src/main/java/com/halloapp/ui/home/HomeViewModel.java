package com.halloapp.ui.home;

import android.Manifest;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.permissions.PermissionWatcher;
import com.halloapp.Preferences;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.content.PostsDataSource;
import com.halloapp.id.UserId;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HomeViewModel extends AndroidViewModel {

    private static final String STATE_SAVED_SCROLL_STATE = "homeviewmodel_saved_scroll_state";

    final LiveData<PagedList<Post>> postList;

    final ComputableLiveData<Boolean> showWelcomeNux;
    final ComputableLiveData<Boolean> unseenHomePosts;

    private final BgWorkers bgWorkers;
    private final ContentDb contentDb;
    private final ContactsDb contactsDb;
    private final Preferences preferences;
    private final AtomicBoolean pendingOutgoing = new AtomicBoolean(false);
    private final AtomicBoolean pendingIncoming = new AtomicBoolean(false);
    private final PostsDataSource.Factory dataSourceFactory;
    private final PermissionWatcher permissionWatcher = PermissionWatcher.getInstance();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private Parcelable savedScrollState;

    private long lastSeenTimestamp;
    private long lastSavedTimestamp;

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {
        @Override
        public void onPostAdded(@NonNull Post post) {
            if (post.isOutgoing()) {
                pendingOutgoing.set(true);
                mainHandler.post(() -> reloadPostsAt(Long.MAX_VALUE));
            } else {
                if (post.type == Post.TYPE_USER) {
                    pendingIncoming.set(true);
                }
                invalidatePosts();
                lastSeenTimestamp = Math.max(post.timestamp, lastSeenTimestamp);
                unseenHomePosts.invalidate();
            }
        }

        @Override
        public void onPostRetracted(@NonNull Post post) {
            invalidatePosts();
        }

        @Override
        public void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {
            invalidatePosts();
        }

        @Override
        public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId) {
            invalidatePosts();
        }

        public void onIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId) {
            unseenHomePosts.invalidate();
        }

        @Override
        public void onCommentAdded(@NonNull Comment comment) {
            invalidatePosts();
        }

        @Override
        public void onCommentRetracted(@NonNull Comment comment) {
            invalidatePosts();
        }

        @Override
        public void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId) {
            invalidatePosts();
        }

        @Override
        public void onFeedCleanup() {
            invalidatePosts();
        }

        private void invalidatePosts() {
            mainHandler.post(dataSourceFactory::invalidateLatestDataSource);
        }
    };

    public HomeViewModel(@NonNull Application application) {
        super(application);

        bgWorkers = BgWorkers.getInstance();
        contentDb = ContentDb.getInstance();
        contentDb.addObserver(contentObserver);
        contactsDb = ContactsDb.getInstance();
        preferences = Preferences.getInstance();

        dataSourceFactory = new PostsDataSource.Factory(contentDb, null, null);
        postList = new LivePagedListBuilder<>(dataSourceFactory, 50).build();

        showWelcomeNux = new ComputableLiveData<Boolean>() {
            @Override
            protected Boolean compute() {
                return !preferences.getShowedWelcomeNux();
            }
        };

        unseenHomePosts = new ComputableLiveData<Boolean>() {
            @Override
            protected Boolean compute() {
                long lastTime = preferences.getLastSeenPostTime();
                List<Post> unseenPosts = contentDb.getUnseenPosts(lastTime, 5);
                if (!unseenPosts.isEmpty()) {
                    lastSeenTimestamp = Math.max(lastSeenTimestamp, unseenPosts.get(0).timestamp);
                }
                return !unseenPosts.isEmpty();
            }
        };
    }

    public void loadSavedState(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null && savedScrollState == null) {
            savedScrollState = savedInstanceState.getParcelable(STATE_SAVED_SCROLL_STATE);
        }
    }

    public void saveInstanceState(@NonNull Bundle outState) {
        if (savedScrollState != null) {
            outState.putParcelable(STATE_SAVED_SCROLL_STATE, savedScrollState);
        }
    }

    public LiveData<Boolean> getUnseenHomePosts() {
        return unseenHomePosts.getLiveData();
    }

    public void closeWelcomeNux() {
        bgWorkers.execute(() -> {
            preferences.markWelcomeNuxShown();
            showWelcomeNux.invalidate();
        });
    }

    @Override
    protected void onCleared() {
        contentDb.removeObserver(contentObserver);
    }

    boolean checkPendingOutgoing() {
        return pendingOutgoing.compareAndSet(true, false);
    }

    boolean checkPendingIncoming() {
        return pendingIncoming.compareAndSet(true, false);
    }

    void reloadPostsAt(long timestamp) {
        final PagedList pagedList = postList.getValue();
        if (pagedList != null) {
            ((PostsDataSource)pagedList.getDataSource()).reloadAt(timestamp);
        }
    }

    public LiveData<Boolean> getHasContactPermission() {
        return permissionWatcher.getPermissionLiveData(Manifest.permission.READ_CONTACTS);
    }

    public void saveScrollState(@Nullable Parcelable savedScrollState) {
        this.savedScrollState = savedScrollState;
    }

    public @Nullable Parcelable getSavedScrollState() {
        return savedScrollState;
    }

    public void onScrollToTop() {
        if (lastSavedTimestamp != lastSeenTimestamp) {
            lastSavedTimestamp = lastSeenTimestamp;
            bgWorkers.execute(() -> {
                preferences.setLastSeenPostTime(lastSeenTimestamp);
                unseenHomePosts.invalidate();
            });
        }
    }

}
