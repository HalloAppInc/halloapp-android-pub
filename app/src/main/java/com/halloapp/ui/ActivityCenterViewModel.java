package com.halloapp.ui;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.UserId;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ActivityCenterViewModel extends AndroidViewModel {

    final ComputableLiveData<SocialHistory> socialHistory;

    final ComputableLiveData<Map<UserId, Contact>> contacts;

    private final BgWorkers bgWorkers;
    private final ContentDb contentDb;
    private final ContactsDb contactsDb;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {
        @Override
        public void onPostAdded(@NonNull Post post) {
            if (!post.isOutgoing()) {
                if (post.doesMention(UserId.ME)) {
                    invalidateSocialHistory();
                }
            }
        }

        @Override
        public void onPostRetracted(@NonNull Post post) {
            invalidateSocialHistory();
        }

        public void onIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId) {
            invalidateSocialHistory();
        }

        @Override
        public void onCommentAdded(@NonNull Comment comment) {
            if (comment.isIncoming()) {
                invalidateSocialHistory();
            }
        }

        @Override
        public void onCommentRetracted(@NonNull Comment comment) {
            invalidateSocialHistory();
        }

        @Override
        public void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId) {
            invalidateSocialHistory();
        }

        @Override
        public void onFeedCleanup() {
            invalidateSocialHistory();
        }

        private void invalidateSocialHistory() {
            mainHandler.post(socialHistory::invalidate);
        }
    };

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
        @Override
        public void onContactsChanged() {
            contacts.invalidate();
        }
    };

    public ActivityCenterViewModel(@NonNull Application application) {
        super(application);

        bgWorkers = BgWorkers.getInstance();
        contentDb = ContentDb.getInstance();
        contentDb.addObserver(contentObserver);
        contactsDb = ContactsDb.getInstance();
        contactsDb.addObserver(contactsObserver);

        socialHistory = new ComputableLiveData<SocialHistory>() {
            @Override
            protected SocialHistory compute() {
                return loadSocialHistory();
            }
        };

        contacts = new ComputableLiveData<Map<UserId, Contact>>() {
            @Override
            protected Map<UserId, Contact> compute() {
                SocialHistory history = socialHistory.getLiveData().getValue();
                if (history == null || history.contacts == null) {
                    return null;
                }
                Collection<UserId> userIds = history.contacts.keySet();
                final Map<UserId, Contact> contacts = new HashMap<>();
                for (UserId userId : userIds) {
                    contacts.put(userId, contactsDb.getContact(userId));
                }
                return contacts;
            }
        };
    }

    public void markAllRead() {
        bgWorkers.execute(() -> {
            final HashSet<Comment> comments = new HashSet<>(contentDb.getIncomingCommentsHistory(-1));
            final List<Post> mentionedPosts = contentDb.getMentionedPosts(UserId.ME, -1);
            final List<Comment> mentionedComments = contentDb.getMentionedComments(UserId.ME, -1);

            comments.addAll(mentionedComments);

            for (Comment comment : comments) {
                contentDb.setCommentSeen(comment.postId, comment.id, true);
            }

            for (Post post : mentionedPosts) {
                contentDb.setIncomingPostSeen(post.senderUserId, post.id);
            }
        });
    }

    @Override
    protected void onCleared() {
        contentDb.removeObserver(contentObserver);
        contactsDb.removeObserver(contactsObserver);
    }

    private void processMentionedComments(@NonNull List<Comment> mentionedComments, @NonNull List<SocialActionEvent> seenOut, @NonNull List<SocialActionEvent> unseenOut) {
        for (Comment comment : mentionedComments) {
            SocialActionEvent activity = SocialActionEvent.fromMentionedComment(comment);
            if (activity == null) {
                continue;
            }
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
        final HashSet<Comment> comments = new HashSet<>(contentDb.getIncomingCommentsHistory(250));
        final List<Post> mentionedPosts = contentDb.getMentionedPosts(UserId.ME, 50);
        final List<Comment> mentionedComments = contentDb.getMentionedComments(UserId.ME, 50);

        for (Comment mentionedComment : mentionedComments) {
            comments.remove(mentionedComment);
        }

        final Map<Pair<UserId, String>, SocialActionEvent> unseenComments = new HashMap<>();
        final List<SocialActionEvent> seenComments = new ArrayList<>();
        final List<SocialActionEvent> seenMentions = new ArrayList<>();
        final List<SocialActionEvent> unseenMentions = new ArrayList<>();
        SocialActionEvent lastActivity = null;
        for (Comment comment : comments) {
            Post parentPost = comment.getParentPost();
            if (parentPost == null) {
                continue;
            }
            if (comment.seen) {
                if (lastActivity == null || !lastActivity.postId.equals(comment.postId)) {
                    lastActivity = new SocialActionEvent(SocialActionEvent.Action.TYPE_COMMENT, parentPost.senderUserId, comment.postId);
                    lastActivity.seen = true;
                    seenComments.add(lastActivity);
                }
                lastActivity.involvedUsers.add(comment.senderUserId);
                if (comment.timestamp > lastActivity.timestamp) {
                    lastActivity.timestamp = comment.timestamp;
                }
            } else {
                final Pair<UserId, String> postKey = Pair.create(parentPost.senderUserId, comment.postId);
                SocialActionEvent commentsGroup = unseenComments.get(postKey);
                if (commentsGroup == null) {
                    commentsGroup = new SocialActionEvent(SocialActionEvent.Action.TYPE_COMMENT, parentPost.senderUserId, comment.postId);
                    commentsGroup.seen = false;
                    unseenComments.put(postKey, commentsGroup);
                }
                commentsGroup.involvedUsers.add(comment.senderUserId);
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

        @Nullable
        public static SocialActionEvent fromMentionedComment(@NonNull Comment comment) {
            Post parentPost = comment.getParentPost();
            if (parentPost == null) {
                return null;
            }
            SocialActionEvent activity = new SocialActionEvent(Action.TYPE_MENTION_IN_COMMENT, parentPost.senderUserId, comment.postId);
            activity.timestamp = comment.timestamp;
            activity.seen = comment.seen;
            activity.involvedUsers.add(comment.senderUserId);
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
