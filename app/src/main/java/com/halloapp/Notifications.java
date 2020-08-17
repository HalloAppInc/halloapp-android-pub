package com.halloapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.Person;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Chat;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.media.MediaUtils;
import com.halloapp.ui.AppExpirationActivity;
import com.halloapp.ui.MainActivity;
import com.halloapp.ui.RegistrationRequestActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.chat.ChatActivity;
import com.halloapp.util.ListFormatter;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Notifications {

    private static Notifications instance;

    private static final int NOTIFICATION_REQUEST_CODE_FEED = 1;
    private static final int NOTIFICATION_REQUEST_CODE_MESSAGES = 2;

    private static final String FEED_NOTIFICATION_CHANNEL_ID = "feed_notifications";
    private static final String MESSAGE_NOTIFICATION_CHANNEL_ID = "message_notifications";
    private static final String CRITICAL_NOTIFICATION_CHANNEL_ID = "critical_notifications";

    private static final String MESSAGE_NOTIFICATION_GROUP_KEY = "message_notification";

    private static final int FEED_NOTIFICATION_ID = 0;
    private static final int MESSAGE_NOTIFICATION_ID = 1;
    private static final int EXPIRATION_NOTIFICATION_ID = 2;
    private static final int LOGIN_FAILED_NOTIFICATION_ID = 3;

    private static final int UNSEEN_POSTS_LIMIT = 256;
    private static final int UNSEEN_COMMENTS_LIMIT = 64;
    private static final int UNSEEN_MESSAGES_LIMIT = 256;

    private static final String EXTRA_FEED_NOTIFICATION_TIME_CUTOFF = "last_feed_notification_time";

    private final Context context;
    private final Preferences preferences;
    private final AvatarLoader avatarLoader;
    private final ContactsDb contactsDb;
    private final ForegroundChat foregroundChat;
    private final Executor executor = Executors.newSingleThreadExecutor();

    private long feedNotificationTimeCutoff;

    private boolean enabled = true;

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
        this.avatarLoader = AvatarLoader.getInstance(context);
        this.contactsDb = ContactsDb.getInstance();
        this.foregroundChat = ForegroundChat.getInstance();
    }

    public void init() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= 26) {
            final NotificationChannel feedNotificationsChannel = new NotificationChannel(FEED_NOTIFICATION_CHANNEL_ID, context.getString(R.string.feed_notifications_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            final NotificationChannel messageNotificationsChannel = new NotificationChannel(MESSAGE_NOTIFICATION_CHANNEL_ID, context.getString(R.string.message_notifications_channel_name), NotificationManager.IMPORTANCE_HIGH);
            messageNotificationsChannel.enableLights(true);
            messageNotificationsChannel.enableVibration(true);
            final NotificationChannel criticalNotificationsChannel = new NotificationChannel(CRITICAL_NOTIFICATION_CHANNEL_ID, context.getString(R.string.critical_notifications_channel_name), NotificationManager.IMPORTANCE_HIGH);
            criticalNotificationsChannel.enableLights(true);
            criticalNotificationsChannel.enableVibration(true);

            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.createNotificationChannel(feedNotificationsChannel);
            notificationManager.createNotificationChannel(messageNotificationsChannel);
            notificationManager.createNotificationChannel(criticalNotificationsChannel);

            notificationManager.deleteNotificationChannel("new_post_notification"); // TODO (ds): remove
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
            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(FEED_NOTIFICATION_ID);
        });
    }

    public void updateFeedNotifications() {
        if (!enabled) {
            return;
        }
        executor.execute(() -> {
            final String newPostsNotificationText = getNewPostsNotificationText();
            final String newCommentsNotificationText = getNewCommentsNotificationText();
            if (TextUtils.isEmpty(newPostsNotificationText) && TextUtils.isEmpty(newCommentsNotificationText)) {
                final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.cancel(FEED_NOTIFICATION_ID);
            } else {
                final String text;
                if (TextUtils.isEmpty(newCommentsNotificationText) && !TextUtils.isEmpty(newPostsNotificationText)) {
                    text = newPostsNotificationText;
                } else if (TextUtils.isEmpty(newPostsNotificationText) && !TextUtils.isEmpty(newCommentsNotificationText)) {
                    text = newCommentsNotificationText;
                } else {
                    text = context.getString(R.string.new_posts_and_comments_notification, newPostsNotificationText, newCommentsNotificationText);
                }
                showFeedNotification(context.getString(R.string.app_name), text);
            }
        });
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

    public void updateMessageNotifications() {
        executor.execute(() -> {

            final List<Message> messages = ContentDb.getInstance(context).getUnseenMessages(UNSEEN_MESSAGES_LIMIT);
            Log.i("Notifications.updateMessageNotifications: " + messages.size() + " messages");

            // group messages by chat IDs
            final HashMap<ChatId, List<Message>> chatsMessages = new HashMap<>();
            final List<ChatId> chatsIds = new ArrayList<>();
            for (Message message : messages) {
                if (message.isOutgoing() || message.isRetracted() || foregroundChat.isForegroundChatId(message.chatId)) {
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
            int chatIndex = 0;
            for (ChatId chatId : chatsIds) {
                final List<Message> chatMessages = Preconditions.checkNotNull(chatsMessages.get(chatId));

                final IconCompat chatIcon = IconCompat.createWithResource(context, R.drawable.avatar_person);
                final Person chatUser = new Person.Builder().setIcon(chatIcon).setName(context.getString(R.string.me)).setKey("").build();
                final NotificationCompat.MessagingStyle style = new NotificationCompat.MessagingStyle(chatUser);
                final List<String> chatNames = new ArrayList<>();
                final Set<ChatId> chats = new HashSet<>();
                for (Message message : chatMessages) {
                    Bitmap avatar = avatars.get(message.chatId);
                    if (avatar == null) {
                        avatar = MediaUtils.getCircledBitmap(avatarLoader.getAvatar(message.chatId));
                        avatars.put(message.chatId, avatar);
                    }
                    final IconCompat icon = IconCompat.createWithBitmap(avatar);
                    final Contact sender = contactsDb.getContact(message.senderUserId);
                    final String senderName = sender.getDisplayName();
                    if (senders.add(message.senderUserId)) {
                        names.add(senderName);
                    }
                    final Chat chat = Preconditions.checkNotNull(ContentDb.getInstance(context).getChat(message.chatId));
                    if (chats.add(message.chatId)) {
                        chatNames.add(chat.name);
                    }
                    String chatName = message.chatId instanceof GroupId ? chat.name : senderName;
                    Person user = new Person.Builder().setIcon(icon).setName(message.senderUserId.isMe() ? context.getString(R.string.me) : chatName).setKey(message.chatId.rawId()).build();
                    String previewText = getMessagePreviewIcon(message) + getMessagePreviewText(message);
                    String textWithAttribution = message.chatId instanceof GroupId ? context.getString(R.string.chat_message_attribution, senderName, previewText) : previewText;
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
                        .setStyle(style);
                final Intent contentIntent = new Intent(context, ChatActivity.class);
                contentIntent.putExtra(ChatActivity.EXTRA_CHAT_ID, chatId);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                final Intent parentIntent = new Intent(context, MainActivity.class);
                parentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                parentIntent.putExtra(MainActivity.EXTRA_NAV_TARGET, MainActivity.NAV_TARGET_MESSAGES);
                stackBuilder.addNextIntent(parentIntent);
                stackBuilder.addNextIntent(contentIntent);

                builder.setContentIntent(stackBuilder.getPendingIntent(chatIndex, PendingIntent.FLAG_UPDATE_CURRENT));
                chatIndex++;

                notificationManager.notify(chatId.rawId(), MESSAGE_NOTIFICATION_ID, builder.build());
            }

            final String text = context.getResources().getQuantityString(R.plurals.new_messages_notification, messages.size(), messages.size(), ListFormatter.format(context, names));
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MESSAGE_NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(text)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setColor(ContextCompat.getColor(context, R.color.color_accent))
                    .setAutoCancel(true)
                    .setGroup(MESSAGE_NOTIFICATION_GROUP_KEY)
                    .setGroupSummary(true);

            if (chatsIds.size() > 1) {
                final Intent contentIntent = new Intent(context, MainActivity.class);
                contentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                contentIntent.putExtra(MainActivity.EXTRA_NAV_TARGET, MainActivity.NAV_TARGET_MESSAGES);
                builder.setContentIntent(PendingIntent.getActivity(context, NOTIFICATION_REQUEST_CODE_MESSAGES, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT));
            } else {
                final ChatId chatId = chatsIds.get(0);
                final Intent contentIntent = new Intent(context, ChatActivity.class);
                contentIntent.putExtra(ChatActivity.EXTRA_CHAT_ID, chatId);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                final Intent parentIntent = new Intent(context, MainActivity.class);
                parentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                parentIntent.putExtra(MainActivity.EXTRA_NAV_TARGET, MainActivity.NAV_TARGET_MESSAGES);
                stackBuilder.addNextIntent(parentIntent);
                stackBuilder.addNextIntent(contentIntent);
                builder.setContentIntent(stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT));
            }
            notificationManager.notify(MESSAGE_NOTIFICATION_ID, builder.build());
        });
    }

    private String getMessagePreviewText(@NonNull Message message) {
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
            return message.text;
        }
    }

    private String getMessagePreviewIcon(@NonNull Message message) {
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
                case Media.MEDIA_TYPE_UNKNOWN:
                default: {
                    return "\uD83D\uDCC2 ";
                }
            }
        } else {
            return "\uD83D\uDCC2 ";
        }
    }

    private String getNewPostsNotificationText() {
        if (!preferences.getNotifyPosts()) {
            return null;
        }
        final List<Post> unseenPosts = ContentDb.getInstance(context).getUnseenPosts(preferences.getFeedNotificationTimeCutoff(), UNSEEN_POSTS_LIMIT);
        if (unseenPosts.isEmpty()) {
            return null;
        }
        final Set<UserId> userIds = new HashSet<>();
        for (Post post : unseenPosts) {
            Log.d("Notifications.update: " + post);
            userIds.add(post.senderUserId);
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

    private String getNewCommentsNotificationText() {
        if (!preferences.getNotifyComments()) {
            return null;
        }
        final List<Comment> unseenComments = ContentDb.getInstance(context).getUnseenCommentsOnMyPosts(preferences.getFeedNotificationTimeCutoff(), UNSEEN_COMMENTS_LIMIT);
        if (unseenComments.isEmpty()) {
            return null;
        }
        final Set<UserId> userIds = new HashSet<>();
        final Set<String> postIds = new HashSet<>();
        for (Comment comment : unseenComments) {
            Log.d("Notifications.update: " + comment);
            userIds.add(comment.commentSenderUserId);
            postIds.add(comment.postId);
            if (comment.timestamp > feedNotificationTimeCutoff) {
                feedNotificationTimeCutoff = comment.timestamp;
            }
        }
        final List<String> names = new ArrayList<>();
        for (UserId userId : userIds) {
            final Contact contact = ContactsDb.getInstance().getContact(userId);
            names.add(contact.getDisplayName());
        }
        return postIds.size() == 1 ? context.getString(R.string.new_comments_notification, ListFormatter.format(context, names)) :
                context.getString(R.string.new_comments_on_multiple_posts_notification, ListFormatter.format(context, names));
    }

    private void showFeedNotification(@NonNull String title, @NonNull String body) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, FEED_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(context, R.color.color_accent))
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        final Intent contentIntent = new Intent(context, MainActivity.class);
        contentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        contentIntent.putExtra(MainActivity.EXTRA_NAV_TARGET, MainActivity.NAV_TARGET_FEED);
        builder.setContentIntent(PendingIntent.getActivity(context, NOTIFICATION_REQUEST_CODE_FEED, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        final Intent deleteIntent = new Intent(context, DeleteNotificationReceiver.class);
        deleteIntent.putExtra(EXTRA_FEED_NOTIFICATION_TIME_CUTOFF, feedNotificationTimeCutoff) ;
        builder.setDeleteIntent(PendingIntent.getBroadcast(context, 0 , deleteIntent, PendingIntent. FLAG_CANCEL_CURRENT));
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(FEED_NOTIFICATION_ID, builder.build());
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
        builder.setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT));
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
        builder.setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(LOGIN_FAILED_NOTIFICATION_ID, builder.build());
    }

    public void clearLoginFailedNotification() {
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(LOGIN_FAILED_NOTIFICATION_ID);
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
}
