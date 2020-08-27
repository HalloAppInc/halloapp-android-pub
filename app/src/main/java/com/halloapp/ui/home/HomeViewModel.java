package com.halloapp.ui.home;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.UserId;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.content.PostsDataSource;
import com.halloapp.util.ComputableLiveData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class HomeViewModel extends AndroidViewModel {

    final LiveData<PagedList<Post>> postList;
    final ComputableLiveData<SocialHistory> socialHistory;

    private final ContentDb contentDb;
    private final ContactsDb contactsDb;
    private final AtomicBoolean pendingOutgoing = new AtomicBoolean(false);
    private final AtomicBoolean pendingIncoming = new AtomicBoolean(false);
    private final PostsDataSource.Factory dataSourceFactory;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private Parcelable savedScrollState;

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {
        @Override
        public void onPostAdded(@NonNull Post post) {
            if (post.isOutgoing()) {
                pendingOutgoing.set(true);
                mainHandler.post(() -> reloadPostsAt(Long.MAX_VALUE));
            } else {
                pendingIncoming.set(true);
                invalidatePosts();
                if (post.doesMention(UserId.ME)) {
                    invalidateSocialHistory();
                }
            }
        }

        @Override
        public void onPostRetracted(@NonNull UserId senderUserId, @NonNull String postId) {
            invalidatePosts();
            invalidateSocialHistory();
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
            invalidateSocialHistory();
        }

        @Override
        public void onCommentAdded(@NonNull Comment comment) {
            invalidatePosts();
            if (comment.isIncoming()) {
                invalidateSocialHistory();
            }
        }

        @Override
        public void onCommentRetracted(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
            invalidatePosts();
            invalidateSocialHistory();
        }

        @Override
        public void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId) {
            invalidatePosts();
            invalidateSocialHistory();
        }

        @Override
        public void onFeedHistoryAdded(@NonNull Collection<Post> historyPosts, @NonNull Collection<Comment> historyComments) {
            invalidatePosts();
            invalidateSocialHistory();
        }

        @Override
        public void onFeedCleanup() {
            invalidatePosts();
            invalidateSocialHistory();
        }

        private void invalidatePosts() {
            mainHandler.post(dataSourceFactory::invalidateLatestDataSource);
        }

        private void invalidateSocialHistory() {
            mainHandler.post(socialHistory::invalidate);
        }
    };

    public HomeViewModel(@NonNull Application application) {
        super(application);

        contentDb = ContentDb.getInstance(application);
        contentDb.addObserver(contentObserver);
        contactsDb = ContactsDb.getInstance();

        dataSourceFactory = new PostsDataSource.Factory(contentDb, null);
        postList = new LivePagedListBuilder<>(dataSourceFactory, 50).build();

        socialHistory = new ComputableLiveData<SocialHistory>() {
            @Override
            protected SocialHistory compute() {
                return loadSocialHistory();
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

    public void saveScrollState(@Nullable Parcelable savedScrollState) {
        this.savedScrollState = savedScrollState;
    }

    public @Nullable Parcelable getSavedScrollState() {
        return savedScrollState;
    }

    private void processMentionedComments(@NonNull List<Comment> mentionedComments, @NonNull List<SocialActionEvent> seenOut, @NonNull List<SocialActionEvent> unseenOut) {
        for (Comment comment : mentionedComments) {
            SocialActionEvent activity = SocialActionEvent.fromMentionedComment(comment);
            if (activity.seen) {
                seenOut.add(activity);
            } else {
                unseenOut.add(activity);
            }
        }
    }

    private void processMentionedPosts(@NonNull List<Post> mentionedPosts, @NonNull List<SocialActionEvent> seenOut, @NonNull List<SocialActionEvent> unseenOut) {
        for (Post post : mentionedPosts) {
            SocialActionEvent activity = SocialActionEvent.fromMentionedPost(post);
            if (activity.seen){
                seenOut.add(activity);
            } else {
                unseenOut.add(activity);
            }
        }
    }

    @WorkerThread
    private SocialHistory loadSocialHistory() {
        final HashSet<Comment> comments = new HashSet<>(ContentDb.getInstance(getApplication()).getIncomingCommentsHistory(250));
        final List<Post> mentionedPosts = ContentDb.getInstance(getApplication()).getMentionedPosts(UserId.ME, 50);
        final List<Comment> mentionedComments = ContentDb.getInstance(getApplication()).getMentionedComments(UserId.ME, 50);

        for (Comment mentionedComment : mentionedComments) {
            comments.remove(mentionedComment);
        }

        final Map<Pair<UserId, String>, SocialActionEvent> unseenComments = new HashMap<>();
        final List<SocialActionEvent> seenComments = new ArrayList<>();
        final List<SocialActionEvent> seenMentions = new ArrayList<>();
        final List<SocialActionEvent> unseenMentions = new ArrayList<>();
        SocialActionEvent lastActivity = null;
        for (Comment comment : comments) {
            if (comment.seen) {
                if (lastActivity == null || !lastActivity.postId.equals(comment.postId) || !lastActivity.postSenderUserId.equals(comment.postSenderUserId)) {
                    lastActivity = new SocialActionEvent(SocialActionEvent.Action.TYPE_COMMENT, comment.postSenderUserId, comment.postId);
                    lastActivity.seen = true;
                    seenComments.add(lastActivity);
                }
                lastActivity.involvedUsers.add(comment.commentSenderUserId);
                if (comment.timestamp > lastActivity.timestamp) {
                    lastActivity.timestamp = comment.timestamp;
                }
            } else {
                final Pair<UserId, String> postKey = Pair.create(comment.postSenderUserId, comment.postId);
                SocialActionEvent commentsGroup = unseenComments.get(postKey);
                if (commentsGroup == null) {
                    commentsGroup = new SocialActionEvent(SocialActionEvent.Action.TYPE_COMMENT, comment.postSenderUserId, comment.postId);
                    commentsGroup.seen = false;
                    unseenComments.put(postKey, commentsGroup);
                }
                commentsGroup.involvedUsers.add(comment.commentSenderUserId);
                if (comment.timestamp > commentsGroup.timestamp) {
                    commentsGroup.timestamp = comment.timestamp;
                }
            }
        }

        processMentionedComments(mentionedComments, seenMentions, unseenMentions);
        processMentionedPosts(mentionedPosts, seenMentions, unseenMentions);

        final ArrayList<SocialActionEvent> socialActionEvents = new ArrayList<>(unseenMentions.size() + seenMentions.size() + unseenComments.size() + seenComments.size());
        socialActionEvents.addAll(seenMentions);
        socialActionEvents.addAll(unseenMentions);
        socialActionEvents.addAll(unseenComments.values());
        socialActionEvents.addAll(seenComments);

        Collections.sort(socialActionEvents, ((o1, o2) -> {
            if (o1.seen != o2.seen) {
                return o1.seen ? 1 : -1;
            }
            return -Long.compare(o1.timestamp, o2.timestamp);
        }));

        final Map<UserId, Contact> contacts = new HashMap<>();
        for (SocialActionEvent event : socialActionEvents) {
            if (!event.postSenderUserId.isMe() && !contacts.containsKey(event.postSenderUserId)) {
                final Contact contact = contactsDb.getContact(event.postSenderUserId);
                contacts.put(event.postSenderUserId, contact);
            }
            for (UserId involvedUser : event.involvedUsers) {
                if (involvedUser.isMe() || contacts.containsKey(involvedUser)) {
                    continue;
                }
                final Contact contact = contactsDb.getContact(involvedUser);
                contacts.put(involvedUser, contact);
            }
        }
        return new SocialHistory(socialActionEvents, unseenMentions.size() + unseenComments.size(), contacts);
    }

    public static class SocialActionEvent {

        @IntDef({Action.TYPE_COMMENT, Action.TYPE_MENTION_IN_COMMENT, Action.TYPE_MENTION_IN_POST})
        public @interface Action {
            int TYPE_COMMENT = 0;
            int TYPE_MENTION_IN_POST = 1;
            int TYPE_MENTION_IN_COMMENT = 2;
        }

        public final UserId postSenderUserId;
        public final String postId;

        public boolean seen;

        public long timestamp;

        public final @Action int action;

        public final List<UserId> involvedUsers = new ArrayList<>();

        public static SocialActionEvent fromMentionedPost(@NonNull Post post) {
            SocialActionEvent activity = new SocialActionEvent(Action.TYPE_MENTION_IN_POST, post.senderUserId, post.id);
            activity.timestamp = post.timestamp;
            activity.seen = post.seen != Post.SEEN_NO;
            activity.involvedUsers.add(post.senderUserId);
            return activity;
        }

        public static SocialActionEvent fromMentionedComment(@NonNull Comment comment) {
            SocialActionEvent activity = new SocialActionEvent(Action.TYPE_MENTION_IN_COMMENT, comment.postSenderUserId, comment.postId);
            activity.timestamp = comment.timestamp;
            activity.seen = comment.seen;
            activity.involvedUsers.add(comment.commentSenderUserId);
            return activity;
        }

        SocialActionEvent(@Action int action, UserId postSenderUserId, String postId) {
            this.action = action;
            this.postId = postId;
            this.postSenderUserId = postSenderUserId;
        }

    }

    public static class SocialHistory {

        public final List<SocialActionEvent> socialActionEvent;
        public final Map<UserId, Contact> contacts;
        public final int unseenCount;

        SocialHistory(@NonNull List<SocialActionEvent> socialActionEvent, int unseenCount, @NonNull Map<UserId, Contact> contacts) {
            this.socialActionEvent = socialActionEvent;
            this.unseenCount = unseenCount;
            this.contacts = contacts;
        }
    }

}
