package com.halloapp.ui.home;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.core.util.Pair;
import androidx.core.util.Preconditions;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ComputableLiveData;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.UserId;
import com.halloapp.posts.Comment;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDataSource;
import com.halloapp.posts.PostsDb;

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

    private final PostsDb postsDb;
    private final AtomicBoolean pendingOutgoing = new AtomicBoolean(false);
    private final AtomicBoolean pendingIncoming = new AtomicBoolean(false);

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final PostsDb.Observer postsObserver = new PostsDb.Observer() {
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
        public void onPostDuplicate(@NonNull Post post) {
            // do not update model on duplicate post
        }

        @Override
        public void onPostDeleted(@NonNull UserId senderUserId, @NonNull String postId) {
            invalidatePosts();
            invalidateCommentHistory();
        }

        @Override
        public void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {
            invalidatePosts();
        }

        @Override
        public void onIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId) {
        }

        @Override
        public void onOutgoingPostSeen(@NonNull String ackId, @NonNull UserId seenByUserId, @NonNull String postId) {
            invalidatePosts();
        }

        @Override
        public void onCommentAdded(@NonNull Comment comment) {
            if (comment.isIncoming()) {
                invalidatePosts();
                invalidateCommentHistory();
            }
        }

        @Override
        public void onCommentDuplicate(@NonNull Comment comment) {
        }

        @Override
        public void onCommentUpdated(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
        }

        @Override
        public void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId) {
            invalidatePosts();
            invalidateCommentHistory();
        }

        @Override
        public void onHistoryAdded(@NonNull Collection<Post> historyPosts, @NonNull Collection<Comment> historyComments) {
            invalidatePosts();
            invalidateCommentHistory();
        }

        @Override
        public void onPostsCleanup() {
            invalidatePosts();
            invalidateCommentHistory();
        }

        private void invalidatePosts() {
            mainHandler.post(() -> Preconditions.checkNotNull(postList.getValue()).getDataSource().invalidate());
        }

        private void invalidateCommentHistory() {
            mainHandler.post(commentsHistory::invalidate);
        }
    };

    public HomeViewModel(@NonNull Application application) {
        super(application);

        postsDb = PostsDb.getInstance(application);
        postsDb.addObserver(postsObserver);

        final PostsDataSource.Factory dataSourceFactory = new PostsDataSource.Factory(postsDb, false);
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
        postsDb.removeObserver(postsObserver);
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

        final List<Comment> comments = PostsDb.getInstance(getApplication()).getIncomingCommentsHistory(250);

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
                if (contact == null) {
                    contacts.put(comment.commentSenderUserId, new Contact(comment.commentSenderUserId));
                } else {
                    contacts.put(comment.commentSenderUserId, contact);
                }
            }
            if (!comment.postSenderUserId.isMe() && !contacts.containsKey(comment.postSenderUserId)) {
                final Contact contact = ContactsDb.getInstance(getApplication()).getContact(comment.postSenderUserId);
                if (contact == null) {
                    contacts.put(comment.postSenderUserId, new Contact(comment.postSenderUserId));
                } else {
                    contacts.put(comment.postSenderUserId, contact);
                }
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
