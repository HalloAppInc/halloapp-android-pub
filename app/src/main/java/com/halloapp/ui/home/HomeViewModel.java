package com.halloapp.ui.home;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.UserId;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.content.PostsDataSource;
import com.halloapp.util.ComputableLiveData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class HomeViewModel extends AndroidViewModel {

    final LiveData<PagedList<Post>> postList;
    final ComputableLiveData<CommentsHistory> commentsHistory;

    private final ContentDb contentDb;
    private final AtomicBoolean pendingOutgoing = new AtomicBoolean(false);
    private final AtomicBoolean pendingIncoming = new AtomicBoolean(false);
    private final PostsDataSource.Factory dataSourceFactory;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {
        @Override
        public void onPostAdded(@NonNull Post post) {
            if (post.isOutgoing()) {
                pendingOutgoing.set(true);
                mainHandler.post(() -> reloadPostsAt(Long.MAX_VALUE));
            } else {
                pendingIncoming.set(true);
                invalidatePosts();
            }
        }

        @Override
        public void onPostRetracted(@NonNull UserId senderUserId, @NonNull String postId) {
            invalidatePosts();
            invalidateCommentHistory();
        }

        @Override
        public void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {
            invalidatePosts();
        }

        @Override
        public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId) {
            invalidatePosts();
        }

        @Override
        public void onCommentAdded(@NonNull Comment comment) {
            invalidatePosts();
            if (comment.isIncoming()) {
                invalidateCommentHistory();
            }
        }

        @Override
        public void onCommentRetracted(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
            invalidatePosts();
            invalidateCommentHistory();
        }

        @Override
        public void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId) {
            invalidatePosts();
            invalidateCommentHistory();
        }

        @Override
        public void onFeedHistoryAdded(@NonNull Collection<Post> historyPosts, @NonNull Collection<Comment> historyComments) {
            invalidatePosts();
            invalidateCommentHistory();
        }

        @Override
        public void onFeedCleanup() {
            invalidatePosts();
            invalidateCommentHistory();
        }

        private void invalidatePosts() {
            mainHandler.post(dataSourceFactory::invalidateLatestDataSource);
        }

        private void invalidateCommentHistory() {
            mainHandler.post(commentsHistory::invalidate);
        }
    };

    public HomeViewModel(@NonNull Application application) {
        super(application);

        contentDb = ContentDb.getInstance(application);
        contentDb.addObserver(contentObserver);

        dataSourceFactory = new PostsDataSource.Factory(contentDb, false);
        postList = new LivePagedListBuilder<>(dataSourceFactory, 50).build();

        commentsHistory = new ComputableLiveData<CommentsHistory>() {
            @Override
            protected CommentsHistory compute() {
                return loadCommentHistory();
            }
        };
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

    @WorkerThread
    private CommentsHistory loadCommentHistory() {

        final List<Comment> comments = ContentDb.getInstance(getApplication()).getIncomingCommentsHistory(250);

        final Map<Pair<UserId, String>, CommentsGroup> unseenComments = new HashMap<>();
        final List<CommentsGroup> seenComments = new ArrayList<>();
        CommentsGroup lastSeenCommentGroup = null;
        for (Comment comment : comments) {
            if (comment.seen) {
                if (lastSeenCommentGroup == null || !lastSeenCommentGroup.postId.equals(comment.postId) || !lastSeenCommentGroup.postSenderUserId.equals(comment.postSenderUserId)) {
                    if (lastSeenCommentGroup != null) {
                        seenComments.add(lastSeenCommentGroup);
                    }
                    lastSeenCommentGroup = new CommentsGroup(comment.postSenderUserId, comment.postId);
                }
                lastSeenCommentGroup.comments.add(comment);
            } else {
                final Pair<UserId, String> postKey = Pair.create(comment.postSenderUserId, comment.postId);
                CommentsGroup commentsGroup = unseenComments.get(postKey);
                if (commentsGroup == null) {
                    commentsGroup = new CommentsGroup(comment.postSenderUserId, comment.postId);
                    unseenComments.put(postKey, commentsGroup);
                }
                commentsGroup.comments.add(comment);
                if (commentsGroup.timestamp < comment.timestamp) {
                    commentsGroup.timestamp = comment.timestamp;
                }
            }
        }
        if (lastSeenCommentGroup != null) {
            seenComments.add(lastSeenCommentGroup);
        }

        final ArrayList<CommentsGroup> commentGroups = new ArrayList<>(unseenComments.values());
        Collections.sort(commentGroups, ((o1, o2) -> -Long.compare(o1.timestamp, o2.timestamp)));
        commentGroups.addAll(seenComments);

        final Map<UserId, Contact> contacts = new HashMap<>();
        for (Comment comment : comments) {
            if (!comment.commentSenderUserId.isMe() && !contacts.containsKey(comment.commentSenderUserId)) {
                final Contact contact = ContactsDb.getInstance(getApplication()).getContact(comment.commentSenderUserId);
                contacts.put(comment.commentSenderUserId, contact);
            }
            if (!comment.postSenderUserId.isMe() && !contacts.containsKey(comment.postSenderUserId)) {
                final Contact contact = ContactsDb.getInstance(getApplication()).getContact(comment.postSenderUserId);
                contacts.put(comment.postSenderUserId, contact);
            }
        }
        return new CommentsHistory(commentGroups, unseenComments.size(), contacts);
    }

    public static class CommentsGroup {

        public final UserId postSenderUserId;
        public final String postId;

        public long timestamp;

        public final List<Comment> comments = new ArrayList<>();


        CommentsGroup(@NonNull UserId postSenderUserId, @NonNull String postId) {
            this.postSenderUserId = postSenderUserId;
            this.postId = postId;
        }
    }

    public static class CommentsHistory {

        public final List<CommentsGroup> commentGroups;
        public final Map<UserId, Contact> contacts;
        public final int unseenCount;

        CommentsHistory(@NonNull List<CommentsGroup> commentGroups, int unseenCount, @NonNull Map<UserId, Contact> contacts) {
            this.commentGroups = commentGroups;
            this.unseenCount = unseenCount;
            this.contacts = contacts;
        }
    }

}
