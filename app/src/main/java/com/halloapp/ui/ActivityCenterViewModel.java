package com.halloapp.ui;

import android.Manifest;
import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.halloapp.Constants;
import com.halloapp.Preferences;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.permissions.PermissionWatcher;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.invites.InvitesApi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ActivityCenterViewModel extends AndroidViewModel {

    final ComputableLiveData<SocialHistory> socialHistory;

    final ComputableLiveData<Map<UserId, Contact>> contacts;

    private final LiveData<Boolean> contactPermissionLiveData;

    private final BgWorkers bgWorkers;
    private final ContentDb contentDb;
    private final Connection connection;
    private final ContactsDb contactsDb;
    private final Preferences preferences;
    private final PermissionWatcher permissionWatcher;

    private final InvitesApi invitesApi;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private @Nullable Integer numInvites;

    private final Observer<Boolean> contactPermissionObserver;

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
        public void onLocalPostSeen(@NonNull String postId) {
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
            Log.i("ActivityCenterViewModel invalidating social history");
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
        connection = Connection.getInstance();
        contactsDb = ContactsDb.getInstance();
        contactsDb.addObserver(contactsObserver);
        preferences = Preferences.getInstance();
        permissionWatcher = PermissionWatcher.getInstance();

        contactPermissionLiveData = permissionWatcher.getPermissionLiveData(Manifest.permission.READ_CONTACTS);

        invitesApi = new InvitesApi(connection);

        socialHistory = new ComputableLiveData<SocialHistory>() {
            @Override
            protected SocialHistory compute() {
                return loadSocialHistory();
            }
        };

        contactPermissionObserver = hasPermission -> {
            // Only check true case because flipping to false would kill our process
            if (hasPermission) {
                socialHistory.invalidate();
            }
        };
        contactPermissionLiveData.observeForever(contactPermissionObserver);

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

        fetchInvites();
    }

    private void fetchInvites() {
        bgWorkers.execute(() -> {
            synchronized (this) {
                if (numInvites == null) {
                    numInvites = preferences.getInvitesRemaining();
                    socialHistory.invalidate();
                }
            }
            invitesApi.getAvailableInviteCount().onResponse(response -> {
                if (response != null) {
                    preferences.setInvitesRemaining(response);
                    synchronized (this) {
                        if (!Objects.equals(numInvites, response)) {
                            numInvites = response;
                            socialHistory.invalidate();
                        }
                    }
                }
            });
        });
    }

    public void markAllRead() {
        bgWorkers.execute(() -> {
            preferences.setWelcomeInviteNotificationSeen(true);

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
        contactPermissionLiveData.removeObserver(contactPermissionObserver);
    }

    public LiveData<SocialHistory> getSocialHistory() {
        return socialHistory.getLiveData();
    }

    private boolean shouldShowPost(@Nullable Post post, boolean hasContactsPerms) {
        if (post == null) {
            return false;
        }
        if (post.timestamp < System.currentTimeMillis() - Constants.POSTS_EXPIRATION) {
            return false;
        }
        if (post.senderUserId.isMe()) {
            return true;
        }
        return hasContactsPerms || post.getParentGroup() != null;
    }

    private void processMentionedComments(@NonNull List<Comment> mentionedComments, @NonNull List<SocialActionEvent> seenOut, @NonNull List<SocialActionEvent> unseenOut, boolean hasContactPermissions) {
        for (Comment comment : mentionedComments) {
            Post parentPost = comment.getParentPost();
            if (!shouldShowPost(parentPost, hasContactPermissions)) {
                continue;
            }
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

    private void processMentionedPosts(@NonNull List<Post> mentionedPosts, @NonNull List<SocialActionEvent> seenOut, @NonNull List<SocialActionEvent> unseenOut, boolean hasContactPermissions) {
        for (Post post : mentionedPosts) {
            if (!shouldShowPost(post, hasContactPermissions)) {
                continue;
            }
            SocialActionEvent activity = SocialActionEvent.fromMentionedPost(post);
            if (activity.seen){
                seenOut.add(activity);
            } else {
                unseenOut.add(activity);
            }
        }
    }

    public void markInvitesNotificationSeen() {
        bgWorkers.execute(() -> {
            preferences.setWelcomeInviteNotificationSeen(true);
            socialHistory.invalidate();
        });
    }

    @WorkerThread
    private SocialHistory loadSocialHistory() {
        final HashSet<Comment> comments = new HashSet<>(contentDb.getIncomingCommentsHistory(250));
        final List<Post> mentionedPosts = contentDb.getMentionedPosts(UserId.ME, 50);
        final List<Comment> mentionedComments = contentDb.getMentionedComments(UserId.ME, 50);

        for (Comment mentionedComment : mentionedComments) {
            comments.remove(mentionedComment);
        }
        boolean hasContactPerms = Boolean.TRUE.equals(contactPermissionLiveData.getValue());

        final List<SocialActionEvent> commentEvents = new ArrayList<>();
        final List<SocialActionEvent> seenMentions = new ArrayList<>();
        final List<SocialActionEvent> unseenMentions = new ArrayList<>();
        final HashMap<String, SocialActionEvent> groupedPostMap = new HashMap<>();
        final Map<UserId, Contact> contacts = new HashMap<>();

        List<Comment> orderedComments = new ArrayList<>(comments);
        Collections.sort(orderedComments, (o2, o1) -> Long.compare(o1.timestamp, o2.timestamp));
        for (Comment comment : orderedComments) {
            Post parentPost = comment.getParentPost();
            if (!shouldShowPost(parentPost, hasContactPerms)) {
                continue;
            }
            if (comment.senderUserId.isMe()) {
                continue;
            }
            Contact contact;
            if (!contacts.containsKey(comment.senderUserId)) {
                contact = contactsDb.getContact(comment.senderUserId);
                contacts.put(comment.senderUserId, contact);
            } else {
                contact = contacts.get(comment.senderUserId);
            }

            if (contact == null || TextUtils.isEmpty(contact.addressBookName)) {
                SocialActionEvent groupedEvent = groupedPostMap.get(comment.postId);
                if (groupedEvent == null) {
                    groupedEvent = new SocialActionEvent(SocialActionEvent.Action.TYPE_COMMENT, parentPost.senderUserId, comment.postId);
                    groupedEvent.contentItem = comment;
                    groupedPostMap.put(comment.postId, groupedEvent);
                    groupedEvent.seen = comment.seen;
                    groupedEvent.timestamp = comment.timestamp;
                } else {
                    if (comment.timestamp > groupedEvent.timestamp) {
                        groupedEvent.timestamp = comment.timestamp;
                        groupedEvent.contentItem = comment;
                    }
                    groupedEvent.seen &= comment.seen;
                }
                groupedEvent.involvedUsers.add(comment.senderUserId);
            } else {
                SocialActionEvent commentEvent = new SocialActionEvent(SocialActionEvent.Action.TYPE_COMMENT, parentPost.senderUserId, comment.postId);
                commentEvent.seen = comment.seen;
                commentEvent.contentItem = comment;
                commentEvent.timestamp = comment.timestamp;
                commentEvent.involvedUsers.add(comment.senderUserId);
                commentEvents.add(commentEvent);
            }
        }

        processMentionedComments(mentionedComments, seenMentions, unseenMentions, hasContactPerms);
        processMentionedPosts(mentionedPosts, seenMentions, unseenMentions, hasContactPerms);

        final ArrayList<SocialActionEvent> socialActionEvents = new ArrayList<>(unseenMentions.size() + seenMentions.size() + commentEvents.size());
        socialActionEvents.addAll(seenMentions);
        socialActionEvents.addAll(unseenMentions);
        socialActionEvents.addAll(groupedPostMap.values());
        socialActionEvents.addAll(commentEvents);

        long initialRegTimestamp = preferences.getInitialRegistrationTime();
        if (initialRegTimestamp != 0 && (numInvites != null && numInvites > 0)) {
            SocialActionEvent event = SocialActionEvent.forInvites(numInvites, initialRegTimestamp);
            event.seen = preferences.getWelcomeInviteNotificationSeen();
            socialActionEvents.add(event);
        }

        int unseenCount = 0;
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
            if (!event.seen) {
                unseenCount++;
            }
        }
        Collections.sort(socialActionEvents, ((o1, o2) -> {
            return -Long.compare(o1.timestamp, o2.timestamp);
        }));

        Log.i("ActivityCenterViewModel/loadSocialHistory got " + socialActionEvents.size() + " events, " + unseenCount + " of which unseen");
        return new SocialHistory(socialActionEvents, unseenCount, contacts);
    }

    public static class SocialActionEvent {

        @IntDef({Action.TYPE_COMMENT, Action.TYPE_MENTION_IN_COMMENT, Action.TYPE_MENTION_IN_POST, Action.TYPE_WELCOME})
        public @interface Action {
            int TYPE_COMMENT = 0;
            int TYPE_MENTION_IN_POST = 1;
            int TYPE_MENTION_IN_COMMENT = 2;
            int TYPE_WELCOME = 3;
        }

        public final UserId postSenderUserId;
        public final String postId;

        public boolean seen;

        public long timestamp;

        public final @Action int action;

        public final List<UserId> involvedUsers = new ArrayList<>();

        public int numInvites;

        public ContentItem contentItem;

        public static SocialActionEvent fromMentionedPost(@NonNull Post post) {
            SocialActionEvent activity = new SocialActionEvent(Action.TYPE_MENTION_IN_POST, post.senderUserId, post.id);
            activity.timestamp = post.timestamp;
            activity.seen = post.seen != Post.SEEN_NO;
            activity.involvedUsers.add(post.senderUserId);
            activity.contentItem = post;
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
            activity.contentItem = comment;
            return activity;
        }

        public static SocialActionEvent forInvites(int numInvites, long timestamp) {
            SocialActionEvent activity = new SocialActionEvent(Action.TYPE_WELCOME, UserId.ME, null);
            activity.timestamp = timestamp;
            activity.numInvites = numInvites;
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
