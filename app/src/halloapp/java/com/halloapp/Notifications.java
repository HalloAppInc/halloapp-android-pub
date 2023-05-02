package com.halloapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.Person;
import androidx.core.app.RemoteInput;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.halloapp.calling.calling.CallNotificationBroadcastReceiver;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.CallMessage;
import com.halloapp.content.Chat;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Group;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Message;
import com.halloapp.content.MomentPost;
import com.halloapp.content.Post;
import com.halloapp.content.ScreenshotByInfo;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaUtils;
import com.halloapp.privacy.BlockListManager;
import com.halloapp.proto.clients.ContactCard;
import com.halloapp.proto.server.CallType;
import com.halloapp.ui.AppExpirationActivity;
import com.halloapp.ui.PostSeenByActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.calling.calling.CallActivity;
import com.halloapp.ui.chat.chat.ChatActivity;
import com.halloapp.ui.groups.ViewGroupFeedActivity;
import com.halloapp.ui.markdown.MarkdownUtils;
import com.halloapp.ui.mentions.MentionsLoader;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ContactCardUtils;
import com.halloapp.util.ListFormatter;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Notifications {

    private static Notifications instance;

    private static final int NOTIFICATION_REQUEST_CODE_FEED_FLAG = 1 << 31;
    private static final int NOTIFICATION_REQUEST_CODE_MESSAGES_FLAG = 1 << 30;
    private static final int NOTIFICATION_REQUEST_CODE_SCREENSHOTS_FLAG = 1 << 29;

    private static final String FEED_NOTIFICATION_CHANNEL_ID = "feed_notifications";
    private static final String MOMENTS_NOTIFICATION_CHANNEL_ID = "moments_notifications";
    private static final String MESSAGE_NOTIFICATION_CHANNEL_ID = "message_notifications";
    private static final String CRITICAL_NOTIFICATION_CHANNEL_ID = "critical_notifications";
    private static final String INVITE_NOTIFICATION_CHANNEL_ID = "invite_notifications";
    private static final String GROUPS_NOTIFICATION_CHANNEL_ID = "group_notifications";
    private static final String CALLS_NOTIFICATION_CHANNEL_ID = "call_notifications";
    private static final String ONGOING_CALL_NOTIFICATION_CHANNEL_ID = "ongoing_call_notifications";
    private static final String BROADCASTS_NOTIFICATION_CHANNEL_ID = "broadcast_notifications";
    private static final String MISSED_CALL_NOTIFICATION_CHANNEL_ID = "missed_call_notifications";

    private static final String MESSAGE_NOTIFICATION_GROUP_KEY = "message_notification";
    private static final String FEED_NOTIFICATION_GROUP_KEY = "feed_notification";
    private static final String REPLY_TEXT_KEY = "reply_text";
    private static final String CALL_MESSAGE_TEXT_KEY = "call_message_text";

    private static final String HOME_FEED_NOTIFICATION_TAG = "home_feed_notification_tag";
    private static final String MOMENTS_NOTIFICATION_TAG = "moments_notification_tag";
    private static final String UNLOCK_MOMENTS_NOTIFICATION_TAG = "unlock_moments_notification_tag";

    private static final int FEED_NOTIFICATION_ID = 0;
    private static final int MESSAGE_NOTIFICATION_ID = 1;
    private static final int EXPIRATION_NOTIFICATION_ID = 2;
    private static final int LOGIN_FAILED_NOTIFICATION_ID = 3;
    private static final int ADDED_TO_GROUP_NOTIFICATION_ID = 4;
    private static final int CALL_NOTIFICATION_ID = 5;
    public static final int ONGOING_CALL_NOTIFICATION_ID = 6;
    private static final int MISSED_CALL_NOTIFICATION_ID = 7;
    private static final int MOMENTS_NOTIFICATION_ID = 8;
    private static final int MOMENT_SCREENSHOT_NOTIFICATION = 9;
    private static final int REMOVED_FROM_GROUP_NOTIFICATION_ID = 10;
    private static final int UNFINISHED_REGISTRATION_NOTIFICATION_ID = 11;
    private static final int DAILY_MOMENT_NOTIFICATION_ID = 12;

    private static final int UNSEEN_POSTS_LIMIT = 256;
    private static final int UNSEEN_COMMENTS_LIMIT = 64;
    private static final int UNSEEN_MESSAGES_LIMIT = 256;
    private static final int UNSEEN_CALLS_LIMIT = 256;

    private static final String EXTRA_FEED_NOTIFICATION_TIME_CUTOFF = "last_feed_notification_time";
    private static final String EXTRA_MOMENT_NOTIFICATION_TIME_CUTOFF = "last_moment_notification_time";
    private static final String EXTRA_SCREENSHOT_NOTIFICATION_TIME_CUTOFF = "last_screenshot_notification_time";
    private static final String EXTRA_CHAT_ID = "chat_id";

    private final Context context;
    private final Preferences preferences;
    private final AvatarLoader avatarLoader;
    private final ContactsDb contactsDb;
    private final ForegroundChat foregroundChat;
    private final BlockListManager blockListManager;
    private final BgWorkers bgWorkers;
    private final Executor executor = Executors.newSingleThreadExecutor();

    private long feedNotificationTimeCutoff;
    private long momentNotificationTimeCutoff;
    private long screenshotNotificationTimeCutoff;

    private final Set<String> localPostIds = new HashSet<>();
    private final Set<String> localCommentIds = new HashSet<>();

    private final HashMap<ChatId, Integer> chatRequestCodeMap = new HashMap<>();

    private int chatRequestCodeOffset = 0;

    private boolean enabled = true;

    private List<UserId> blockList = null;

    public static Notifications getInstance(final @NonNull Context context) {
        if (instance == null) {
            synchronized(Notifications.class) {
                if (instance == null) {
                    instance = new Notifications(context);
                }
            }
        }
        return instance;
    }

    private Notifications(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = Preferences.getInstance();
        this.avatarLoader = AvatarLoader.getInstance();
        this.contactsDb = ContactsDb.getInstance();
        this.foregroundChat = ForegroundChat.getInstance();
        this.blockListManager = BlockListManager.getInstance();
        this.bgWorkers = BgWorkers.getInstance();
        blockListManager.addObserver(blockListObserver);
        fetchBlockList();
    }

    private final BlockListManager.Observer blockListObserver = new BlockListManager.Observer() {
        @Override
        public void onBlockListChanged() {
            fetchBlockList();
        }
    };

    private void fetchBlockList() {
        bgWorkers.execute(() -> {
            blockList = blockListManager.getBlockList();
        });
    }

    public void init() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= 26) {
            final NotificationChannel feedNotificationsChannel = new NotificationChannel(FEED_NOTIFICATION_CHANNEL_ID, context.getString(R.string.feed_notifications_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            final NotificationChannel momentsNotificationChannel = new NotificationChannel(MOMENTS_NOTIFICATION_CHANNEL_ID, context.getString(R.string.moments_notifications_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            final NotificationChannel messageNotificationsChannel = new NotificationChannel(MESSAGE_NOTIFICATION_CHANNEL_ID, context.getString(R.string.message_notifications_channel_name), NotificationManager.IMPORTANCE_HIGH);
            messageNotificationsChannel.enableLights(true);
            messageNotificationsChannel.enableVibration(true);
            final NotificationChannel criticalNotificationsChannel = new NotificationChannel(CRITICAL_NOTIFICATION_CHANNEL_ID, context.getString(R.string.critical_notifications_channel_name), NotificationManager.IMPORTANCE_HIGH);
            criticalNotificationsChannel.enableLights(true);
            criticalNotificationsChannel.enableVibration(true);
            final NotificationChannel broadcastNotificationsChannel = new NotificationChannel(BROADCASTS_NOTIFICATION_CHANNEL_ID, context.getString(R.string.broadcast_notifications_channel_name), NotificationManager.IMPORTANCE_HIGH);
            broadcastNotificationsChannel.enableLights(true);
            broadcastNotificationsChannel.enableVibration(true);
            final NotificationChannel inviteNotificationsChannel = new NotificationChannel(INVITE_NOTIFICATION_CHANNEL_ID, context.getString(R.string.invite_notifications_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            final NotificationChannel groupNotificationsChannel = new NotificationChannel(GROUPS_NOTIFICATION_CHANNEL_ID, context.getString(R.string.group_notifications_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            final NotificationChannel callNotificationsChannel = new NotificationChannel(CALLS_NOTIFICATION_CHANNEL_ID, context.getString(R.string.call_notifications_channel_name), NotificationManager.IMPORTANCE_HIGH);
            callNotificationsChannel.enableLights(true);
            callNotificationsChannel.enableVibration(true);
            callNotificationsChannel.setSound(Settings.System.DEFAULT_RINGTONE_URI, getCallNotificationAudioAttributes());

            final NotificationChannel ongoingCallNotificationsChannel = new NotificationChannel(ONGOING_CALL_NOTIFICATION_CHANNEL_ID, context.getString(R.string.ongoing_call_notifications_channel_name), NotificationManager.IMPORTANCE_LOW);
            final NotificationChannel missedCallNotificationsChannel = new NotificationChannel(MISSED_CALL_NOTIFICATION_CHANNEL_ID, context.getString(R.string.missed_call_notifications_channel_name), NotificationManager.IMPORTANCE_HIGH);
            missedCallNotificationsChannel.enableLights(true);
            missedCallNotificationsChannel.enableVibration(true);

            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.createNotificationChannel(momentsNotificationChannel);
            notificationManager.createNotificationChannel(feedNotificationsChannel);
            notificationManager.createNotificationChannel(messageNotificationsChannel);
            notificationManager.createNotificationChannel(criticalNotificationsChannel);
            notificationManager.createNotificationChannel(inviteNotificationsChannel);
            notificationManager.createNotificationChannel(groupNotificationsChannel);
            notificationManager.createNotificationChannel(callNotificationsChannel);
            notificationManager.createNotificationChannel(ongoingCallNotificationsChannel);
            notificationManager.createNotificationChannel(broadcastNotificationsChannel);
            notificationManager.createNotificationChannel(missedCallNotificationsChannel);
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void clearFeedNotifications() {
        executor.execute(() -> {
            if (feedNotificationTimeCutoff != 0) {
                preferences.setFeedNotificationTimeCutoff(feedNotificationTimeCutoff);
            }
            if (momentNotificationTimeCutoff != 0) {
                preferences.setMomentNotificationTimeCutoff(momentNotificationTimeCutoff);
            }
            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(FEED_NOTIFICATION_ID);
            notificationManager.cancel(UNLOCK_MOMENTS_NOTIFICATION_TAG, MOMENTS_NOTIFICATION_ID);
            notificationManager.cancel(MOMENTS_NOTIFICATION_TAG, MOMENTS_NOTIFICATION_ID);
        });
        localPostIds.clear();
        localCommentIds.clear();
    }

    public void updateScreenshotNotifications() {
        if (!enabled) {
            return;
        }
        executor.execute(() -> {
            List<ScreenshotByInfo> screenshotContacts = getScreenshotContacts();
            if (screenshotContacts == null || screenshotContacts.isEmpty()) {
                final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.cancel(MOMENT_SCREENSHOT_NOTIFICATION);
                return;
            }
            HashSet<UserId> users = new HashSet<>();
            final List<String> names = new ArrayList<>();
            for (ScreenshotByInfo info : screenshotContacts) {
                if (info.timestamp > screenshotNotificationTimeCutoff) {
                    screenshotNotificationTimeCutoff = info.timestamp;
                }
                if (users.contains(info.userId)) {
                    continue;
                }
                names.add(contactsDb.getContact(info.userId).getShortName());
                users.add(info.userId);

            }

            int numNames = names.size();
            String body;
            if (numNames == 1) {
                body = context.getString(R.string.new_moment_screenshot_notification, names.get(0));
            } else if (numNames == 2) {
                body = context.getString(R.string.two_new_moment_screenshot_notification, names.get(0), names.get(1));
            } else if (numNames == 3) {
                body = context.getString(R.string.three_new_moment_screenshot_notification, names.get(0), names.get(1), names.get(2));
            } else {
                body = context.getString(R.string.many_new_moment_screenshot_notification, names.get(0), names.get(1), names.get(2));
            }

            final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MOMENTS_NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setColor(ContextCompat.getColor(context, R.color.color_accent))
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(body)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_LIGHTS |
                            NotificationCompat.DEFAULT_SOUND |
                            NotificationCompat.DEFAULT_VIBRATE)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI, AudioManager.STREAM_NOTIFICATION);

            final Intent contentIntent = new Intent(context, PostSeenByActivity.class);
            contentIntent.putExtra(PostSeenByActivity.EXTRA_POST_ID, ContentDb.getInstance().getUnlockingMomentId());

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            final Intent parentIntent = new Intent(context, MainActivity.class);
            parentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            parentIntent.putExtra(MainActivity.EXTRA_NAV_TARGET, MainActivity.NAV_TARGET_FEED);
            stackBuilder.addNextIntent(parentIntent);
            stackBuilder.addNextIntent(contentIntent);

            builder.setContentIntent(stackBuilder.getPendingIntent(NOTIFICATION_REQUEST_CODE_SCREENSHOTS_FLAG, getPendingIntentFlags(true)));
            final Intent deleteIntent = new Intent(context, DeleteScreenshotNotificationReceiver.class);
            deleteIntent.putExtra(EXTRA_SCREENSHOT_NOTIFICATION_TIME_CUTOFF, screenshotNotificationTimeCutoff) ;
            builder.setDeleteIntent(PendingIntent.getBroadcast(context, 0 , deleteIntent, PendingIntent. FLAG_CANCEL_CURRENT | getPendingIntentFlags(false)));
            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(MOMENT_SCREENSHOT_NOTIFICATION, builder.build());
        });
    }

    public void updateFeedNotifications() {
        if (!enabled) {
            return;
        }
        executor.execute(() -> {
            List<Post> unseenPosts = getNewPosts();
            List<Comment> allNewComments = getNewComments();
            List<Comment> unseenComments = new ArrayList<>();
            if (allNewComments != null) {
                for (Comment comment : allNewComments) {
                    if (blockList != null && !blockList.contains(comment.senderUserId)) {
                        unseenComments.add(comment);
                    }
                }
            }
            List<MomentPost> unseenMoments = getNewMoments();
            Log.i("Notifications/updateFeedNotifications"
                    + " unseenPosts=" + (unseenPosts == null ? "none" : unseenPosts.size())
                    + " unseenComments=" + (unseenComments == null ? "none" : unseenComments.size())
                    + " unseenMoments=" + (unseenMoments == null ? "none" : unseenMoments.size()));
            List<Post> homePosts = null;
            List<Post> momentPosts = null;
            List<Post> unlockedMomentPosts = null;
            List<Comment> homeComments = null;
            HashSet<GroupId> groupIds = new HashSet<>();
            HashMap<GroupId, List<Comment>> groupCommentListMap = new HashMap<>();
            HashMap<GroupId, List<Post>> groupPostListMap = new HashMap<>();
            if (unseenComments == null && unseenPosts == null && unseenMoments == null) {
                final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.cancel(FEED_NOTIFICATION_ID);
                notificationManager.cancel(UNLOCK_MOMENTS_NOTIFICATION_TAG, MOMENTS_NOTIFICATION_ID);
                notificationManager.cancel(MOMENTS_NOTIFICATION_TAG, MOMENTS_NOTIFICATION_ID);
                return;
            }
            if (unseenPosts != null) {
                homePosts = new ArrayList<>(unseenPosts);
                ListIterator<Post> homePostsListIterator = homePosts.listIterator();
                while (homePostsListIterator.hasNext()) {
                    Post post = homePostsListIterator.next();
                    GroupId parentGroupId = post.getParentGroup();
                    if (parentGroupId != null) {
                        groupIds.add(parentGroupId);
                        homePostsListIterator.remove();
                        List<Post> groupPostList = groupPostListMap.get(parentGroupId);
                        if (groupPostList == null) {
                            groupPostList = new ArrayList<>();
                            groupPostListMap.put(parentGroupId, groupPostList);
                        }
                        groupPostList.add(post);
                    }
                }
            }
            if (unseenMoments != null) {
                momentPosts = new ArrayList<>(unseenMoments);
                ListIterator<Post> momentListIterator = momentPosts.listIterator();
                while (momentListIterator.hasNext()) {
                    Post post = momentListIterator.next();
                    if (post != null && post.isOutgoing()) {
                        momentListIterator.remove();
                        continue;
                    }
                    if (post instanceof MomentPost && ((MomentPost) post).unlockedUserId != null && ((MomentPost) post).unlockedUserId.isMe()) {
                        if (unlockedMomentPosts == null) {
                            unlockedMomentPosts = new ArrayList<>();
                        }
                        unlockedMomentPosts.add(post);
                        momentListIterator.remove();
                    }
                }
            }
            if (unseenComments != null) {
                homeComments = new ArrayList<>(unseenComments);
                ListIterator<Comment> unseenCommentIterator = homeComments.listIterator();
                while (unseenCommentIterator.hasNext()) {
                    Comment comment = unseenCommentIterator.next();
                    Post parentPost = comment.getParentPost();
                    GroupId parentGroupId = parentPost == null ? null : parentPost.getParentGroup();
                    if (parentGroupId != null) {
                        groupIds.add(parentGroupId);
                        unseenCommentIterator.remove();
                        List<Comment> groupCommentList = groupCommentListMap.get(parentGroupId);
                        if (groupCommentList == null) {
                            groupCommentList = new ArrayList<>();
                            groupCommentListMap.put(parentGroupId, groupCommentList);
                        }
                        groupCommentList.add(comment);
                    }
                }
            }
            int index = 0;
            String appName = context.getString(R.string.app_name);
            showCombinedFeedNotification(HOME_FEED_NOTIFICATION_TAG, appName, index++ | NOTIFICATION_REQUEST_CODE_FEED_FLAG, homePosts, homeComments);
            showMomentsNotification(appName, index++ | NOTIFICATION_REQUEST_CODE_FEED_FLAG, momentPosts);
            showMomentsUnlockNotification(appName, index++ | NOTIFICATION_REQUEST_CODE_FEED_FLAG, unlockedMomentPosts);

            for (GroupId groupId : groupIds) {
                List<Comment> comments = groupCommentListMap.get(groupId);
                List<Post> posts = groupPostListMap.get(groupId);

                Group group = ContentDb.getInstance().getGroup(groupId);
                if (group != null) {
                    showCombinedFeedNotification(groupId.rawId(), group.name, index++ | NOTIFICATION_REQUEST_CODE_FEED_FLAG, posts, comments);
                    index++;
                } else {
                    Log.e("Notifications/updateFeedNotifications no group found for groupId=" + groupId);
                }
            }
            String postsSummary = null;
            String commentsSummary = null;
            if (unseenPosts != null && !unseenPosts.isEmpty()) {
                postsSummary = getNewPostsNotificationText(unseenPosts);
            }
            if (unseenComments != null && !unseenComments.isEmpty()) {
                commentsSummary = getNewCommentsNotificationText(unseenComments);
            }
            if (TextUtils.isEmpty(postsSummary) && TextUtils.isEmpty(commentsSummary)) {
                final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.cancel(FEED_NOTIFICATION_ID);
            } else {
                final String summaryText;
                if (TextUtils.isEmpty(commentsSummary) && !TextUtils.isEmpty(postsSummary)) {
                    summaryText = postsSummary;
                } else if (TextUtils.isEmpty(postsSummary) && !TextUtils.isEmpty(commentsSummary)) {
                    summaryText = commentsSummary;
                } else {
                    summaryText = context.getString(R.string.new_posts_and_comments_notification, postsSummary, commentsSummary);
                }
                final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, FEED_NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setColor(ContextCompat.getColor(context, R.color.color_accent))
                        .setContentTitle(appName)
                        .setContentText(summaryText)
                        .setAutoCancel(true)
                        .setGroup(FEED_NOTIFICATION_GROUP_KEY)
                        .setGroupSummary(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setDefaults(NotificationCompat.DEFAULT_LIGHTS |
                                NotificationCompat.DEFAULT_SOUND |
                                NotificationCompat.DEFAULT_VIBRATE)
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI, AudioManager.STREAM_NOTIFICATION);
                final Intent contentIntent = new Intent(context, MainActivity.class);
                contentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                contentIntent.putExtra(MainActivity.EXTRA_NAV_TARGET, MainActivity.NAV_TARGET_FEED);
                contentIntent.putExtra(MainActivity.EXTRA_SCROLL_TO_TOP, true);
                builder.setContentIntent(PendingIntent.getActivity(context, index | NOTIFICATION_REQUEST_CODE_FEED_FLAG, contentIntent, getPendingIntentFlags(true)));
                final Intent deleteIntent = new Intent(context, DeleteNotificationReceiver.class);
                deleteIntent.putExtra(EXTRA_FEED_NOTIFICATION_TIME_CUTOFF, feedNotificationTimeCutoff);
                builder.setDeleteIntent(PendingIntent.getBroadcast(context, 0, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT | getPendingIntentFlags(false)));
                final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(FEED_NOTIFICATION_ID, builder.build());
            }
        });
    }

    private void showMomentsNotification(@NonNull String title, int requestCode, @Nullable List<Post> unseenMoments) {
        String newPostsNotificationText = null;

        if (unseenMoments != null && !unseenMoments.isEmpty()) {
            newPostsNotificationText = getNewMomentsNotificationText(unseenMoments);
        }
        if (TextUtils.isEmpty(newPostsNotificationText)) {
            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(MOMENTS_NOTIFICATION_TAG, MOMENTS_NOTIFICATION_ID);
            Log.i("Notifications/showMomentsNotification hiding moments notification group");
        } else {
            Log.i("Notifications/showMomentsNotification unseenMoments=" + unseenMoments.size());
            showNotificationForMoments(MOMENTS_NOTIFICATION_TAG, title, newPostsNotificationText, requestCode, unseenMoments);
        }
    }

    private void showMomentsUnlockNotification(@NonNull String title, int requestCode, @Nullable List<Post> unseenMoments) {
        String newPostsNotificationText = null;

        if (unseenMoments != null && !unseenMoments.isEmpty()) {
            final Set<UserId> userIds = new LinkedHashSet<>();
            for (Post post : unseenMoments) {
                Log.d("Notifications.update: " + post);
                userIds.add(post.senderUserId);
                if (post.timestamp > momentNotificationTimeCutoff) {
                    momentNotificationTimeCutoff = post.timestamp;
                }
                if (userIds.size() > 3) {
                    break;
                }
            }
            final List<String> names = new ArrayList<>();
            for (UserId userId : userIds) {
                final Contact contact = ContactsDb.getInstance().getContact(userId);
                names.add(contact.getDisplayName());
            }
            int numNames = names.size();
            if (numNames == 1) {
                newPostsNotificationText = context.getString(R.string.new_moment_unlock_notification, names.get(0));
            } else if (numNames == 2) {
                newPostsNotificationText = context.getString(R.string.two_new_moment_unlock_notification, names.get(0), names.get(1));
            } else if (numNames == 3) {
                newPostsNotificationText = context.getString(R.string.three_new_moment_unlock_notification, names.get(0), names.get(1), names.get(2));
            } else {
                newPostsNotificationText = context.getString(R.string.many_new_moment_unlock_notification, names.get(0), names.get(1), names.get(2));
            }
        }
        if (TextUtils.isEmpty(newPostsNotificationText)) {
            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(UNLOCK_MOMENTS_NOTIFICATION_TAG, FEED_NOTIFICATION_ID);
            Log.i("Notifications/showMomentsUnlockNotification hiding moments notification group");
        } else {
            Log.i("Notifications/showMomentsUnlockNotification unseenMoments=" + unseenMoments.size());
            showNotificationForMoments(UNLOCK_MOMENTS_NOTIFICATION_TAG, title, newPostsNotificationText, requestCode, unseenMoments);
        }
    }

    private void showCombinedFeedNotification(@NonNull String tag, @NonNull String title, int requestCode, @Nullable List<Post> unseenPosts, @Nullable List<Comment> unseenComments) {
        String newPostsNotificationText = null;
        String newCommentsNotificationText = null;

        if (unseenPosts != null && !unseenPosts.isEmpty()) {
            newPostsNotificationText = getNewPostsNotificationText(unseenPosts);
        }
        if (unseenComments != null && !unseenComments.isEmpty()) {
            newCommentsNotificationText = getNewCommentsNotificationText(unseenComments);
        }
        if (TextUtils.isEmpty(newPostsNotificationText) && TextUtils.isEmpty(newCommentsNotificationText)) {
            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(tag, FEED_NOTIFICATION_ID);
        } else {
            final String text;
            if (TextUtils.isEmpty(newCommentsNotificationText) && !TextUtils.isEmpty(newPostsNotificationText)) {
                text = newPostsNotificationText;
            } else if (TextUtils.isEmpty(newPostsNotificationText) && !TextUtils.isEmpty(newCommentsNotificationText)) {
                text = newCommentsNotificationText;
            } else {
                text = context.getString(R.string.new_posts_and_comments_notification, newPostsNotificationText, newCommentsNotificationText);
            }
            showFeedNotification(tag, title, Preconditions.checkNotNull(text), requestCode, unseenPosts, unseenComments);
        }
    }

    public void updateFeedNotifications(Post post) {
        if (isNewPostRetracted(post)) {
            updateFeedNotifications();
        }
    }

    public void updateFeedNotifications(Comment comment) {
        if (isNewCommentRetracted(comment)) {
            updateFeedNotifications();
        }
    }

    public void clearMessageNotifications(@NonNull ChatId chatId) {
        executor.execute(() -> {
            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (Build.VERSION.SDK_INT >= 23 && getMessageNotificationCount() <= 2) {
                notificationManager.cancel(MESSAGE_NOTIFICATION_ID);
            }
            notificationManager.cancel(chatId.rawId(), MESSAGE_NOTIFICATION_ID);
        });
    }

    @RequiresApi(23)
    private int getMessageNotificationCount() {
        final NotificationManager notificationManager = (NotificationManager) Preconditions.checkNotNull(context.getSystemService(Context.NOTIFICATION_SERVICE));
        final StatusBarNotification[] statusBarNotifications = notificationManager.getActiveNotifications();
        int count = 0;
        for (StatusBarNotification statusBarNotification : statusBarNotifications) {
            if (statusBarNotification.getGroupKey().endsWith(MESSAGE_NOTIFICATION_GROUP_KEY)) {
                count++;
            }
        }
        return count;
    }

    public void updateMissedCallNotifications() {
        executor.execute(() -> {
            final List<CallMessage> unseenCalls = ContentDb.getInstance().getUnseenCallMessages(UNSEEN_CALLS_LIMIT);
            final HashMap<ChatId, List<CallMessage>> callMessageMap = new LinkedHashMap<>();

            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (unseenCalls.isEmpty()) {
                notificationManager.cancel(MISSED_CALL_NOTIFICATION_ID);
                return;
            }
            for (CallMessage message : unseenCalls) {
                if (message.isOutgoing() || message.isRetracted() || foregroundChat.isForegroundChatId(message.chatId) || !message.isMissedCall()) {
                    continue;
                }
                List<CallMessage> callMsgs = callMessageMap.get(message.chatId);
                if (callMsgs == null) {
                    callMsgs = new ArrayList<>();
                    callMessageMap.put(message.chatId, callMsgs);
                }
                callMsgs.add(message);
            }

            for (ChatId chatId : callMessageMap.keySet()) {
                if (chatId instanceof UserId) {
                    CallType callbackType = CallType.AUDIO;
                    final Contact sender = contactsDb.getContact((UserId) chatId);
                    final String senderName = sender.getDisplayName();

                    Bitmap avatar = MediaUtils.getCircledBitmap(avatarLoader.getAvatar(context, chatId));

                    final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MISSED_CALL_NOTIFICATION_CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setColor(ContextCompat.getColor(context, R.color.color_accent))
                            .setGroupSummary(false)
                            .setLargeIcon(avatar)
                            .setContentTitle(senderName)
                            .setDefaults(NotificationCompat.DEFAULT_LIGHTS |
                                    NotificationCompat.DEFAULT_SOUND |
                                    NotificationCompat.DEFAULT_VIBRATE)
                            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI, AudioManager.STREAM_NOTIFICATION);

                    final Intent contentIntent = ChatActivity.open(context, chatId, true);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    final Intent parentIntent = new Intent(context, MainActivity.class);
                    parentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    parentIntent.putExtra(MainActivity.EXTRA_NAV_TARGET, MainActivity.NAV_TARGET_MESSAGES);
                    stackBuilder.addNextIntent(parentIntent);
                    stackBuilder.addNextIntent(contentIntent);

                    List<CallMessage> messages = callMessageMap.get(chatId);
                    if (messages == null) {
                        Log.e("Notifications/updatedMissedCallNotification missing list of missed call messages");
                        continue;
                    }
                    builder.setContentIntent(stackBuilder.getPendingIntent(0, getPendingIntentFlags(true)));
                    if (messages.size() > 1) {
                        builder.setContentText(context.getResources().getQuantityString(R.plurals.missed_calls_notification, messages.size(), messages.size()));
                    } else {
                        CallMessage missedCall = messages.get(0);
                        if (missedCall.callUsage == CallMessage.USAGE_MISSED_VIDEO_CALL) {
                            builder.setContentText(context.getResources().getText(R.string.log_missed_video_call));
                            callbackType = CallType.VIDEO;
                        } else {
                            builder.setContentText(context.getResources().getText(R.string.log_missed_voice_call));
                        }
                    }

                    Intent callBackIntent = CallActivity.getStartCallIntent(context, (UserId) chatId, callbackType);
                    PendingIntent acceptPendingIntent = PendingIntent.getActivity(context, 0, callBackIntent, getPendingIntentFlags(false));

                    NotificationCompat.Action callbackAction = new NotificationCompat.Action.Builder(R.drawable.ic_call, context.getString(R.string.call_back_notification_label), acceptPendingIntent)
                            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_CALL)
                            .build();
                    builder.addAction(callbackAction);

                    notificationManager.notify(chatId.rawId(), MISSED_CALL_NOTIFICATION_ID, builder.build());
                    Log.i("Notifications: missed call notification for " + chatId.rawId());
                }
            }
        });
    }

    public void updateMessageNotifications() {
        executor.execute(() -> {

            final List<Message> messages = ContentDb.getInstance().getUnseenMessages(UNSEEN_MESSAGES_LIMIT);
            Log.i("Notifications.updateMessageNotifications: " + messages.size() + " messages");

            // group messages by chat IDs
            final HashMap<ChatId, List<Message>> chatsMessages = new HashMap<>();
            final List<ChatId> chatsIds = new ArrayList<>();
            for (Message message : messages) {
                if (message.isOutgoing() || message.isRetracted() || foregroundChat.isForegroundChatId(message.chatId) || (message instanceof CallMessage)) {
                    Log.i("Notifications.updateMessageNotifications skipping " + message.id + " outgoing? " + message.isOutgoing() + " retracted? " + message.isRetracted());
                    continue;
                }
                List<Message> chatMessages = chatsMessages.get(message.chatId);
                if (chatMessages == null) {
                    chatMessages = new ArrayList<>();
                    chatsMessages.put(message.chatId, chatMessages);
                    chatsIds.add(message.chatId);
                }
                chatMessages.add(message);
            }

            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (chatsMessages.isEmpty()) {
                notificationManager.cancel(MESSAGE_NOTIFICATION_ID);
                return;
            }

            final List<String> names = new ArrayList<>();
            final Set<UserId> senders = new HashSet<>();
            final Map<ChatId, Bitmap> avatars = new HashMap<>();
            final Map<ChatId, Notification> groupedNotifications = new HashMap<>();
            int chatIndex = 0;
            for (ChatId chatId : chatsIds) {
                final List<Message> chatMessages = Preconditions.checkNotNull(chatsMessages.get(chatId));

                String replyLabel = context.getString(R.string.reply_notification_label);
                RemoteInput remoteInput = new RemoteInput.Builder(REPLY_TEXT_KEY)
                        .setLabel(replyLabel)
                        .build();
                Intent replyIntent = new Intent(context, MessageReplyReceiver.class);
                replyIntent.putExtra(EXTRA_CHAT_ID, chatId.rawId());
                PendingIntent replyPendingIntent = PendingIntent.getBroadcast(
                        context.getApplicationContext(),
                        getChatRequestCode(chatId),
                        replyIntent,
                        getPendingIntentFlags(true)
                );
                NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(R.drawable.ic_reply, replyLabel, replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

                String markReadLabel = context.getString(R.string.mark_read_notification_label);
                Intent markReadIntent = new Intent(context, MarkReadReceiver.class);
                markReadIntent.putExtra(EXTRA_CHAT_ID, chatId.rawId());
                PendingIntent markReadPendingIntent = PendingIntent.getBroadcast(
                        context.getApplicationContext(),
                        getChatRequestCode(chatId),
                        markReadIntent,
                        getPendingIntentFlags(true)
                );
                NotificationCompat.Action markReadAction = new NotificationCompat.Action.Builder(R.drawable.ic_messaging_seen, markReadLabel, markReadPendingIntent).build();

                final IconCompat chatIcon = IconCompat.createWithResource(context, R.drawable.avatar_person);
                final Person chatUser = new Person.Builder().setIcon(chatIcon).setName(context.getString(R.string.me)).setKey("").build();
                final NotificationCompat.MessagingStyle style = new NotificationCompat.MessagingStyle(chatUser);
                final List<String> chatNames = new ArrayList<>();
                final Set<ChatId> chats = new HashSet<>();
                for (Message message : chatMessages) {
                    Bitmap avatar = avatars.get(message.chatId);
                    if (avatar == null) {
                        avatar = MediaUtils.getCircledBitmap(avatarLoader.getAvatar(context, message.chatId));
                        avatars.put(message.chatId, avatar);
                    }
                    final IconCompat icon = IconCompat.createWithBitmap(avatar);
                    final Contact sender = contactsDb.getContact(message.senderUserId);
                    final String senderName = sender.getDisplayName();
                    if (senders.add(message.senderUserId)) {
                        names.add(senderName);
                    }
                    final Chat chat = Preconditions.checkNotNull(ContentDb.getInstance().getChat(message.chatId));
                    String chatName = message.chatId instanceof GroupId ? chat.name : senderName;
                    if (chatName != null && chats.add(message.chatId)) {
                        chatNames.add(chatName);
                    }
                    Person user = new Person.Builder().setIcon(icon).setName(message.senderUserId.isMe() ? context.getString(R.string.me) : chatName).setKey(message.chatId.rawId()).build();
                    CharSequence previewText = getMessagePreviewIcon(message) + getMessagePreviewText(message);
                    CharSequence textWithAttribution = message.chatId instanceof GroupId ? context.getString(R.string.chat_message_attribution, senderName, previewText) : previewText;
                    style.addMessage(textWithAttribution, message.timestamp, user);
                }
                final String text = context.getResources().getQuantityString(R.plurals.new_messages_notification, chatMessages.size(), chatMessages.size(), ListFormatter.format(context, chatNames));
                final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MESSAGE_NOTIFICATION_CHANNEL_ID)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(text)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setColor(ContextCompat.getColor(context, R.color.color_accent))
                        .setGroup(MESSAGE_NOTIFICATION_GROUP_KEY)
                        .setGroupSummary(false)
                        .setDefaults(NotificationCompat.DEFAULT_LIGHTS |
                                NotificationCompat.DEFAULT_SOUND |
                                NotificationCompat.DEFAULT_VIBRATE)
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI, AudioManager.STREAM_NOTIFICATION)
                        .setStyle(style)
                        .addAction(replyAction)
                        .addAction(markReadAction);
                final Intent contentIntent = ChatActivity.open(context, chatId, true);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                final Intent parentIntent = new Intent(context, MainActivity.class);
                parentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                parentIntent.putExtra(MainActivity.EXTRA_NAV_TARGET, MainActivity.NAV_TARGET_MESSAGES);
                stackBuilder.addNextIntent(parentIntent);
                stackBuilder.addNextIntent(contentIntent);

                builder.setContentIntent(stackBuilder.getPendingIntent(chatIndex++, getPendingIntentFlags(true)));

                groupedNotifications.put(chatId, builder.build());
            }

            final String text = context.getResources().getQuantityString(R.plurals.new_messages_notification, messages.size(), messages.size(), ListFormatter.format(context, names));
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MESSAGE_NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(text)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setColor(ContextCompat.getColor(context, R.color.color_accent))
                    .setAutoCancel(true)
                    .setGroup(MESSAGE_NOTIFICATION_GROUP_KEY)
                    .setGroupSummary(true)
                    .setDefaults(NotificationCompat.DEFAULT_LIGHTS |
                            NotificationCompat.DEFAULT_SOUND |
                            NotificationCompat.DEFAULT_VIBRATE)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI, AudioManager.STREAM_NOTIFICATION);

            if (chatsIds.size() > 1) {
                final Intent contentIntent = new Intent(context, MainActivity.class);
                contentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                contentIntent.putExtra(MainActivity.EXTRA_NAV_TARGET, MainActivity.NAV_TARGET_MESSAGES);
                builder.setContentIntent(PendingIntent.getActivity(context, NOTIFICATION_REQUEST_CODE_MESSAGES_FLAG, contentIntent, getPendingIntentFlags(true)));
            } else {
                final ChatId chatId = chatsIds.get(0);
                final Intent contentIntent = ChatActivity.open(context, chatId, true);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                final Intent parentIntent = new Intent(context, MainActivity.class);
                parentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                parentIntent.putExtra(MainActivity.EXTRA_NAV_TARGET, MainActivity.NAV_TARGET_MESSAGES);
                stackBuilder.addNextIntent(parentIntent);
                stackBuilder.addNextIntent(contentIntent);
                builder.setContentIntent(stackBuilder.getPendingIntent(0, getPendingIntentFlags(true)));
            }
            notificationManager.notify(MESSAGE_NOTIFICATION_ID, builder.build());

            // Individual notifications must be sent to the notification manager after the summary notification,
            // otherwise newer Samsung devices will use the summary notification's intent when the user taps on
            // an individual notification in its pop-over state (everything else works properly). I was not
            // able to find any official documentation indicating that you have to create the summary prior
            // to the individual notifications, and the grouping docs even show the summary being posted after:
            // https://developer.android.com/develop/ui/views/notifications/group#set_a_group_summary
            // However, this SO post claims the summary must be created first and doing so resolves the issue:
            // https://stackoverflow.com/a/41114135/11817085
            for (Map.Entry<ChatId, Notification> entry : groupedNotifications.entrySet()) {
                notificationManager.notify(entry.getKey().rawId(), MESSAGE_NOTIFICATION_ID, entry.getValue());
            }
        });
    }

    private int getChatRequestCode(ChatId chatId) {
        if (chatRequestCodeMap.containsKey(chatId)) {
            return chatRequestCodeMap.get(chatId);
        }
        int code = chatRequestCodeOffset++;
        chatRequestCodeMap.put(chatId, code);
        return code;
    }

    private CharSequence getMessagePreviewText(@NonNull Message message) {
        if (message.type == Message.TYPE_CONTACT) {
            ContactCard contactCard = ContactCardUtils.deserializeContactCard(message.text);
            if (contactCard != null) {
                for (com.halloapp.proto.clients.Contact contact : contactCard.getContactsList()) {
                    return contact.getName();
                }
            }
            return context.getString(R.string.attachment_contact);
        }
        if (TextUtils.isEmpty(message.text)) {
            if (message.media.size() == 1) {
                final Media media = message.media.get(0);
                switch (media.type) {
                    case Media.MEDIA_TYPE_IMAGE: {
                        return context.getString(R.string.photo);
                    }
                    case Media.MEDIA_TYPE_VIDEO: {
                        return context.getString(R.string.video);
                    }
                    case Media.MEDIA_TYPE_AUDIO: {
                        return context.getString(R.string.voice_note);
                    }
                    case Media.MEDIA_TYPE_UNKNOWN:
                    default: {
                        Log.e("unknown media type " + media.file.getAbsolutePath());
                        return "";
                    }
                }
            } else {
                return context.getString(R.string.album);
            }
        } else {
            List<Mention> mentions = MentionsLoader.loadMentionNames(Me.getInstance(), contactsDb, message.mentions);
            return MarkdownUtils.formatMarkdownWithMentions(context, message.text, mentions);
        }
    }

    private String getMessagePreviewIcon(@NonNull Message message) {
        if (message.type == Message.TYPE_CONTACT) {
            return "\uD83D\uDC64";
        }
        if (message.media.size() == 0) {
            return "";
        } if (message.media.size() == 1) {
            final Media media = message.media.get(0);
            switch (media.type) {
                case Media.MEDIA_TYPE_IMAGE: {
                    return "\uD83D\uDCF7 ";
                }
                case Media.MEDIA_TYPE_VIDEO: {
                    return "\uD83D\uDCF9 ";
                }
                case Media.MEDIA_TYPE_AUDIO: {
                    return "\uD83C\uDFA4";
                }
                case Media.MEDIA_TYPE_DOCUMENT: {
                    return "\uD83D\uDCC4";
                }
                case Media.MEDIA_TYPE_UNKNOWN:
                default: {
                    return "\uD83D\uDCC2 ";
                }
            }
        } else {
            return "\uD83D\uDCC2 ";
        }
    }

    @Nullable
    private List<ScreenshotByInfo> getScreenshotContacts() {
        if (!preferences.getNotifyMoments()) {
            return null;
        }
        String unlockingMomentId = ContentDb.getInstance().getUnlockingMomentId();
        if (unlockingMomentId == null) {
            return null;
        }
        return ContentDb.getInstance().getRecentMomentScreenshotInfo(unlockingMomentId, preferences.getScreenshotNotificationTimeCutoff());
    }

    @Nullable
    private List<MomentPost> getNewMoments() {
        if (!preferences.getNotifyMoments()) {
            return null;
        }
        final List<MomentPost> moments = ContentDb.getInstance().getMomentsAfter(preferences.getMomentNotificationTimeCutoff());

        if (moments.isEmpty()) {
            return null;
        }

        return moments;
    }

    @Nullable
    private List<Post> getNewPosts() {
        if (!preferences.getNotifyPosts()) {
            return null;
        }
        final List<Post> unseenPosts = ContentDb.getInstance().getUnseenPosts(preferences.getFeedNotificationTimeCutoff(), UNSEEN_POSTS_LIMIT);

        ListIterator<Post> iterator = unseenPosts.listIterator();
        while(iterator.hasNext()){
            Post post = iterator.next();
            if (post.isRetracted() || post.type == Post.TYPE_MOMENT) {
                iterator.remove();
            }
        }

        if (unseenPosts.isEmpty()) {
            return null;
        }

        return unseenPosts;
    }

    private boolean isNewPostRetracted(Post post){
        if (post == null) {
            return false;
        }
        return localPostIds.contains(post.id);
    }

    private boolean isNewCommentRetracted(Comment comment){
        if (comment == null) {
            return false;
        }
        return localCommentIds.contains(comment.id);
    }

    @Nullable
    private List<Comment> getNewComments() {
        if (!preferences.getNotifyComments()) {
            return null;
        }
        final List<Comment> unseenComments = ContentDb.getInstance().getNotificationComments(preferences.getFeedNotificationTimeCutoff(), UNSEEN_COMMENTS_LIMIT);

        ListIterator<Comment> iterator = unseenComments.listIterator();
        while(iterator.hasNext()){
            Comment comment = iterator.next();
            if (comment.isRetracted() || comment.getParentPost().isRetracted()) {
                iterator.remove();
            }
        }

        if (unseenComments.isEmpty()) {
            return null;
        }
        return unseenComments;
    }

    private String getNewPostsNotificationText(@NonNull List<Post> unseenPosts) {
        final Set<UserId> userIds = new HashSet<>();
        localPostIds.clear();
        for (Post post : unseenPosts) {
            Log.d("Notifications.update: " + post);
            userIds.add(post.senderUserId);
            localPostIds.add(post.id);
            if (post.timestamp > feedNotificationTimeCutoff) {
                feedNotificationTimeCutoff = post.timestamp;
            }
        }
        final List<String> names = new ArrayList<>();
        for (UserId userId : userIds) {
            final Contact contact = ContactsDb.getInstance().getContact(userId);
            names.add(contact.getDisplayName());
        }
        final String text;
        if (unseenPosts.size() == 1) {
            text = context.getString(R.string.new_post_notification, names.get(0));
        } else {
            text = context.getResources().getQuantityString(R.plurals.new_posts_notification, unseenPosts.size(), unseenPosts.size(), ListFormatter.format(context, names));
        }
        return text;
    }

    private String getNewMomentsNotificationText(@NonNull List<Post> unseenMoments) {
        final Set<UserId> userIds = new HashSet<>();
        final List<String> names = new ArrayList<>();
        for (Post post : unseenMoments) {
            Log.d("Notifications.update: " + post);
            if (userIds.add(post.senderUserId)) {
                final Contact contact = ContactsDb.getInstance().getContact(post.senderUserId);
                if (!TextUtils.isEmpty(post.psaTag)) {
                    names.add(contact.getDisplayName(false));
                } else {
                    names.add(contact.getDisplayName());
                }
            }
            if (post.timestamp > momentNotificationTimeCutoff) {
                momentNotificationTimeCutoff = post.timestamp;
            }
        }
        final String text;
        if (unseenMoments.size() == 1) {
            text = context.getString(R.string.new_moment_notification, names.get(0));
        } else {
            text = context.getResources().getQuantityString(R.plurals.new_moments_notification, unseenMoments.size(), unseenMoments.size(), ListFormatter.format(context, names));
        }
        return text;
    }

    private String getNewCommentsNotificationText(@NonNull List<Comment> unseenComments) {
        final Set<UserId> userIds = new HashSet<>();
        localCommentIds.clear();
        for (Comment comment : unseenComments) {
            Log.d("Notifications.update: " + comment);
            userIds.add(comment.senderUserId);
            localCommentIds.add(comment.id);
            if (comment.timestamp > feedNotificationTimeCutoff) {
                feedNotificationTimeCutoff = comment.timestamp;
            }
        }
        final List<String> names = new ArrayList<>();
        for (UserId userId : userIds) {
            final Contact contact = ContactsDb.getInstance().getContact(userId);
            names.add(contact.getDisplayName());
        }
        final String text;
        if (unseenComments.size() == 1) {
            text = context.getString(R.string.new_comment_notification, names.get(0));
        } else {
            text = context.getResources().getQuantityString(R.plurals.new_comments_list_notification, unseenComments.size(), unseenComments.size(), ListFormatter.format(context, names));
        }
        return text;
    }

    private void showNotificationForMoments(@NonNull String tag, @NonNull String title, @NonNull String body, int requestCode, @Nullable List<Post> unseenPosts) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MOMENTS_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(context, R.color.color_accent))
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS |
                        NotificationCompat.DEFAULT_SOUND |
                        NotificationCompat.DEFAULT_VIBRATE)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI, AudioManager.STREAM_NOTIFICATION);
        final Intent contentIntent = new Intent(context, MainActivity.class);
        contentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        contentIntent.putExtra(MainActivity.EXTRA_NAV_TARGET, MainActivity.NAV_TARGET_FEED);
        contentIntent.putExtra(MainActivity.EXTRA_SCROLL_TO_TOP, true);
        if (unseenPosts != null && unseenPosts.size() > 0) {
            contentIntent.putExtra(MainActivity.EXTRA_STACK_TOP_MOMENT_ID, unseenPosts.get(0).id);
        }
        builder.setContentIntent(PendingIntent.getActivity(context, requestCode, contentIntent, getPendingIntentFlags(true)));
        final Intent deleteIntent = new Intent(context, DeleteMomentNotificationReceiver.class);
        deleteIntent.putExtra(EXTRA_MOMENT_NOTIFICATION_TIME_CUTOFF, momentNotificationTimeCutoff) ;
        builder.setDeleteIntent(PendingIntent.getBroadcast(context, 0 , deleteIntent, PendingIntent. FLAG_CANCEL_CURRENT | getPendingIntentFlags(false)));
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(tag, MOMENTS_NOTIFICATION_ID, builder.build());
    }

    public void showDailyMomentNotification(long timestamp) {
        String title = context.getString(R.string.notification_daily_moment_title);
        String body = context.getString(R.string.notification_daily_moment_body);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MOMENTS_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(context, R.color.color_accent))
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS |
                        NotificationCompat.DEFAULT_SOUND |
                        NotificationCompat.DEFAULT_VIBRATE)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI, AudioManager.STREAM_NOTIFICATION);

        Intent contentIntent = new Intent(context, MainActivity.class);
        contentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        contentIntent.putExtra(MainActivity.EXTRA_NAV_TARGET, MainActivity.NAV_TARGET_FEED);
        contentIntent.putExtra(MainActivity.EXTRA_SCROLL_TO_TOP, true);
        contentIntent.putExtra(MainActivity.EXTRA_POST_START_MOMENT_POST, true);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, getPendingIntentFlags(true)));

        Intent deleteIntent = new Intent(context, DeleteMomentNotificationReceiver.class);
        deleteIntent.putExtra(EXTRA_MOMENT_NOTIFICATION_TIME_CUTOFF, momentNotificationTimeCutoff) ;
        builder.setDeleteIntent(PendingIntent.getBroadcast(context, 0 , deleteIntent, PendingIntent. FLAG_CANCEL_CURRENT | getPendingIntentFlags(false)));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(DAILY_MOMENT_NOTIFICATION_ID, builder.build());
    }

    private void showFeedNotification(@NonNull String tag, @NonNull String title, @NonNull String body, int requestCode, @Nullable List<Post> unseenPosts, @Nullable List<Comment> unseenComments) {
        HashSet<String> postIds = new HashSet<>();
        if (unseenPosts != null) {
            for (Post post : unseenPosts) {
                postIds.add(post.id);
            }
        }
        if (unseenComments != null) {
            for (Comment comment : unseenComments) {
                postIds.add(comment.postId);
            }
        }
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, FEED_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(context, R.color.color_accent))
                .setContentTitle(title)
                .setContentText(body)
                .setGroup(FEED_NOTIFICATION_GROUP_KEY)
                .setGroupSummary(false)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS |
                        NotificationCompat.DEFAULT_SOUND |
                        NotificationCompat.DEFAULT_VIBRATE)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI, AudioManager.STREAM_NOTIFICATION);
        final Intent contentIntent = new Intent(context, MainActivity.class);
        contentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        contentIntent.putExtra(MainActivity.EXTRA_NAV_TARGET, MainActivity.NAV_TARGET_FEED);
        if (postIds.size() == 1) {
            for (String id : postIds) {
                contentIntent.putExtra(MainActivity.EXTRA_POST_ID, id);
                if (unseenComments != null && !unseenComments.isEmpty()) {
                    contentIntent.putExtra(MainActivity.EXTRA_POST_SHOW_COMMENTS, true);
                }
                break;
            }
        } else {
            contentIntent.putExtra(MainActivity.EXTRA_SCROLL_TO_TOP, true);
        }
        builder.setContentIntent(PendingIntent.getActivity(context, requestCode, contentIntent, getPendingIntentFlags(true)));
        final Intent deleteIntent = new Intent(context, DeleteNotificationReceiver.class);
        deleteIntent.putExtra(EXTRA_FEED_NOTIFICATION_TIME_CUTOFF, feedNotificationTimeCutoff) ;
        builder.setDeleteIntent(PendingIntent.getBroadcast(context, 0 , deleteIntent, PendingIntent. FLAG_CANCEL_CURRENT | getPendingIntentFlags(false)));
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(tag, FEED_NOTIFICATION_ID, builder.build());
    }

    public void showNewGroupNotification(GroupId groupId, String inviterName, String groupName, boolean feedGroup) {
        String body = String.format(context.getResources().getString(R.string.added_to_group), inviterName, groupName);
        String title = context.getResources().getString(R.string.app_name);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, GROUPS_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setColor(ContextCompat.getColor(context, R.color.color_accent))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentText(body)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS |
                        NotificationCompat.DEFAULT_SOUND |
                        NotificationCompat.DEFAULT_VIBRATE)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI, AudioManager.STREAM_NOTIFICATION);
        final Intent groupIntent;
        if (feedGroup) {
            groupIntent = ViewGroupFeedActivity.viewFeed(context, groupId);
        }  else {
            groupIntent = ChatActivity.open(context, groupId);
        }
        builder.setContentIntent(PendingIntent.getActivity(context, 0, groupIntent, getPendingIntentFlags(true)));
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(groupId.rawId(), ADDED_TO_GROUP_NOTIFICATION_ID, builder.build());
    }

    public void clearNewGroupNotification() {
        executor.execute(() -> {
            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(ADDED_TO_GROUP_NOTIFICATION_ID);
        });
    }

    public void showRemovedFromGroupNotification(GroupId groupId, String removerName, String groupName, boolean feedGroup) {
        String body = String.format(context.getResources().getString(R.string.removed_from_group), removerName, groupName);
        String title = context.getResources().getString(R.string.app_name);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, GROUPS_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setColor(ContextCompat.getColor(context, R.color.color_accent))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentText(body);
        final Intent groupIntent;
        if (feedGroup) {
            groupIntent = ViewGroupFeedActivity.viewFeed(context, groupId);
        }  else {
            groupIntent = ChatActivity.open(context, groupId);
        }
        builder.setContentIntent(PendingIntent.getActivity(context, 0, groupIntent, getPendingIntentFlags(true)));
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(groupId.rawId(), REMOVED_FROM_GROUP_NOTIFICATION_ID, builder.build());
    }

    public void clearRemovedFromGroupNotification() {
        executor.execute(() -> {
            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(REMOVED_FROM_GROUP_NOTIFICATION_ID);
        });
    }

    @WorkerThread
    public void showIncomingCallNotification(String callId, UserId peerUid, CallType callType) {
        executor.execute(() -> {

            Intent callIntent = CallActivity.incomingCallIntent(context, callId, peerUid, callType);
            Intent declineIntent = CallNotificationBroadcastReceiver.declineCallIntent(context, callId, peerUid);
            Intent acceptIntent = CallActivity.acceptCallIntent(context, callId, peerUid, callType);

            PendingIntent callPendingIntent = PendingIntent.getActivity(context, 0, callIntent, getPendingIntentFlags(false));
            PendingIntent declinePendingIntent = PendingIntent.getBroadcast(context, 0, declineIntent, getPendingIntentFlags(false));
            PendingIntent acceptPendingIntent = PendingIntent.getActivity(context, 0, acceptIntent, getPendingIntentFlags(false));

            final Contact contact = ContactsDb.getInstance().getContact(peerUid);
            String name = contact.getDisplayName();

            Bitmap avatar = MediaUtils.getCircledBitmap(avatarLoader.getAvatar(context, peerUid));
            final IconCompat icon = IconCompat.createWithBitmap(avatar);
            final Person person = new Person.Builder()
                    .setBot(false)
                    .setIcon(icon)
                    .setName(name)
                    .setKey(peerUid.rawId())
                    .build();

            SpannableString declineText = new SpannableString(context.getString(R.string.call_decline_button));
            declineText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.call_decline)), 0, declineText.length(), 0);

            NotificationCompat.Action declineAction = new NotificationCompat.Action.Builder(
                        R.drawable.ic_call_end,
                        declineText,
                        declinePendingIntent)
                    .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_DELETE)
                    .setShowsUserInterface(false)
                    .build();

            SpannableString acceptText = new SpannableString(context.getString(R.string.call_accept_button));
            acceptText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.call_accept)), 0, acceptText.length(), 0);

            NotificationCompat.Action acceptAction = new NotificationCompat.Action.Builder(
                        R.drawable.ic_call,
                        acceptText,
                        acceptPendingIntent)
                    .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_CALL)
                    .setShowsUserInterface(true)
                    .build();

            final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CALLS_NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setLargeIcon(avatar)
                    // TODO(nikola): improve the notification based on designs
                    .setContentTitle(context.getString(callType == CallType.AUDIO? R.string.incoming_voice_call_notification_title : R.string.incoming_video_call_notification_title))
                    .setContentText(name)
                    .setOngoing(true)
                    .setColor(ContextCompat.getColor(context, R.color.color_accent))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .setDefaults(NotificationCompat.DEFAULT_LIGHTS |
                            NotificationCompat.DEFAULT_SOUND |
                            NotificationCompat.DEFAULT_VIBRATE)
                    .setSound(Settings.System.DEFAULT_RINGTONE_URI, AudioManager.STREAM_RING)
                    .addAction(declineAction)
                    .addAction(acceptAction)
                    .addPerson(person)
                    .setFullScreenIntent(callPendingIntent, true);

            // TODO(nikola): https://developer.android.com/training/notify-user/build-notification#metadata

            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            Notification notification = builder.build();
            notification.flags |= Notification.FLAG_INSISTENT;
            try {
                notificationManager.notify(CALL_NOTIFICATION_ID, notification);
                Log.i("Notifications: showIncomingCallNotification " + callId);
            } catch (SecurityException e) {
                String errorMessage = e.getMessage();
                Log.e("Failed to post IncomingCall Notification: " + errorMessage, e);
                Log.sendErrorReport("Failed to post IncomingCall Notification: " + errorMessage);
            }
        });
    }

    public void clearIncomingCallNotification() {
        executor.execute(() -> {
            Log.i("Notifications: clearIncomingCallNotification");
            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(CALL_NOTIFICATION_ID);
        });
    }

    @WorkerThread
    public Notification getOngoingCallNotification(UserId peerUid, boolean isInitiator) {
        Intent callIntent = CallActivity.getOngoingCallIntent(context, peerUid, isInitiator);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, callIntent, getPendingIntentFlags(true));

        final Contact contact = ContactsDb.getInstance().getContact(peerUid);
        String name = contact.getDisplayName();

        Notification notification = new NotificationCompat.Builder(context, ONGOING_CALL_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(name)
                // TODO(nikola): Update this for video calls
                .setContentText(context.getResources().getString(R.string.ongoing_voice_call))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setTicker(context.getResources().getString(R.string.ongoing_voice_call))
                .build();
        return notification;
    }

    public AudioAttributes getCallNotificationAudioAttributes() {
        return new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_REQUEST)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
    }

    public void showExpirationNotification(int daysLeft) {
        final String title;
        if (daysLeft > 0) {
            title = context.getResources().getQuantityString(R.plurals.notification_app_expiration_days_left_title, daysLeft, daysLeft);
        } else {
            title = context.getString(R.string.notification_app_expired_title);
        }
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CRITICAL_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(context, R.color.color_accent))
                .setContentTitle(title)
                .setContentText(context.getString(R.string.notification_app_expiration_body))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        final Intent contentIntent = new Intent(context, AppExpirationActivity.class);
        contentIntent.putExtra(AppExpirationActivity.EXTRA_DAYS_LEFT, daysLeft);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, getPendingIntentFlags(true)));
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(EXPIRATION_NOTIFICATION_ID, builder.build());
    }

    public void showLoginFailedNotification() {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CRITICAL_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(context, R.color.color_accent))
                .setContentTitle(context.getString(R.string.login_failed))
                .setContentText(context.getString(R.string.login_failed_explanation))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        final Intent contentIntent = new Intent(context, RegistrationRequestActivity.class);
        contentIntent.putExtra(RegistrationRequestActivity.EXTRA_RE_VERIFY, true);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, getPendingIntentFlags(true)));
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(LOGIN_FAILED_NOTIFICATION_ID, builder.build());
    }

    public void showInviteAcceptedNotification(@NonNull Contact contact) {
        int id = preferences.getAndIncrementNotificationId();
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, INVITE_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(context, R.color.color_accent))
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.invite_notification_text, contact.getShortName()))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS |
                        NotificationCompat.DEFAULT_SOUND |
                        NotificationCompat.DEFAULT_VIBRATE)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI, AudioManager.STREAM_NOTIFICATION);
        final Intent contentIntent = ChatActivity.open(context, Preconditions.checkNotNull(contact.userId), true);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, getPendingIntentFlags(true)));
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(id, builder.build());
    }

    public void clearLoginFailedNotification() {
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(LOGIN_FAILED_NOTIFICATION_ID);
    }

    public void showFinishRegistrationNotification() {
        Intent contentIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, contentIntent, getPendingIntentFlags(false));
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CRITICAL_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(context, R.color.color_accent))
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.finish_registration_notification_text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS |
                        NotificationCompat.DEFAULT_SOUND |
                        NotificationCompat.DEFAULT_VIBRATE)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI, AudioManager.STREAM_NOTIFICATION);
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(UNFINISHED_REGISTRATION_NOTIFICATION_ID, builder.build());
        Log.i("Unfinished Notification at time : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z", Locale.US).format(new Date()));
    }

    public void clearFinishRegistrationNotification() {
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(UNFINISHED_REGISTRATION_NOTIFICATION_ID);
    }

    static public class DeleteNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive (Context context , Intent intent) {
            Log.i("Notifications.BroadcastReceiver: cancel");
            final long feedNotificationTimeCutoff = intent.getLongExtra(EXTRA_FEED_NOTIFICATION_TIME_CUTOFF, 0);
            if (feedNotificationTimeCutoff > 0) {
                Log.i("Notifications.BroadcastReceiver: cancel, notification cutoff at " + feedNotificationTimeCutoff);
                Notifications.getInstance(context).executor.execute(() -> Notifications.getInstance(context).preferences.setFeedNotificationTimeCutoff(feedNotificationTimeCutoff));
            }
        }
    }

    static public class DeleteMomentNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive (Context context , Intent intent) {
            Log.i("Notifications.BroadcastReceiver: cancel");
            final long momentNotificationTimeCutoff = intent.getLongExtra(EXTRA_MOMENT_NOTIFICATION_TIME_CUTOFF, 0);
            if (momentNotificationTimeCutoff > 0) {
                Log.i("Notifications.BroadcastReceiver: cancel, moment notification cutoff at " + momentNotificationTimeCutoff);
                Notifications.getInstance(context).executor.execute(() -> Notifications.getInstance(context).preferences.setMomentNotificationTimeCutoff(momentNotificationTimeCutoff));
            }
        }
    }

    static public class DeleteScreenshotNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive (Context context , Intent intent) {
            Log.i("Notifications.BroadcastReceiver: cancel");
            final long screenshotNotificationCutoffTime = intent.getLongExtra(EXTRA_SCREENSHOT_NOTIFICATION_TIME_CUTOFF, 0);
            if (screenshotNotificationCutoffTime > 0) {
                Log.i("Notifications.BroadcastReceiver: cancel, moment notification cutoff at " + screenshotNotificationCutoffTime);
                Notifications.getInstance(context).executor.execute(() -> Notifications.getInstance(context).preferences.setScreenshotNotificationTimeCutoff(screenshotNotificationCutoffTime));
            }
        }
    }

    public static class MessageReplyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Notifications.MessageReplyReceiver");
            Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
            if (remoteInput != null) {
                CharSequence text = Preconditions.checkNotNull(remoteInput.getCharSequence(REPLY_TEXT_KEY));
                String rawId = intent.getStringExtra(EXTRA_CHAT_ID);
                ChatId chatId = ChatId.fromNullable(rawId);
                String id = RandomId.create();
                Log.i("Notifications.MessageReplyReceiver: sending message id " + id + " in " + chatId);

                Message message = new Message(
                        0,
                        chatId,
                        UserId.ME,
                        id,
                        System.currentTimeMillis(),
                        Message.TYPE_CHAT,
                        Message.USAGE_CHAT,
                        Message.STATE_INITIAL,
                        StringUtils.preparePostText(text.toString()),
                        null,
                        0,
                        null,
                        0,
                        null,
                        0
                );
                message.addToStorage(ContentDb.getInstance());

                final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MESSAGE_NOTIFICATION_CHANNEL_ID)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(text)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setColor(ContextCompat.getColor(context, R.color.color_accent))
                        .setAutoCancel(true)
                        .setGroup(MESSAGE_NOTIFICATION_GROUP_KEY)
                        .setGroupSummary(true);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(rawId, MESSAGE_NOTIFICATION_ID, builder.build());
            }
        }
    }

    public static class MarkReadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Notifications.MarkReadReceiver");
            ChatId chatId = Preconditions.checkNotNull(ChatId.fromNullable(intent.getStringExtra(EXTRA_CHAT_ID)));
            Log.i("Notifications.MarkReadReceiver marking chat " + chatId + " as read");
            ContentDb.getInstance().setChatSeen(chatId);
            Notifications.getInstance(context).clearMessageNotifications(chatId);
        }
    }

    public static int getMutableIntentFlag(boolean mutable) {
        if (Build.VERSION.SDK_INT >= 23) {
            return (mutable
                    ? (Build.VERSION.SDK_INT >= 31
                        ? PendingIntent.FLAG_MUTABLE
                        : 0)
                    : PendingIntent.FLAG_IMMUTABLE);
        } else {
            return 0;
        }
    }

    public static int getPendingIntentFlags(boolean mutable) {
        if (Build.VERSION.SDK_INT >= 23) {
            return PendingIntent.FLAG_UPDATE_CURRENT | getMutableIntentFlag(mutable);
        } else {
            return PendingIntent.FLAG_UPDATE_CURRENT;
        }
    }
}
