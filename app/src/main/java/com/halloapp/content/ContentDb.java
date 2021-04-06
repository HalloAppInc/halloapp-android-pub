package com.halloapp.content;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.AppContext;
import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.Me;
import com.halloapp.groups.GroupInfo;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.props.ServerProps;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.DecryptStats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@SuppressWarnings("WeakerAccess")
public class ContentDb {

    private static ContentDb instance;

    private final Executor databaseWriteExecutor = Executors.newSingleThreadExecutor();

    private final ContentDbObservers observers = new ContentDbObservers();
    private final ContentDbHelper databaseHelper;

    private final Me me;
    private final MentionsDb mentionsDb;
    private final MessagesDb messagesDb;
    private final PostsDb postsDb;

    private final ServerProps serverProps;

    public interface Observer {
        void onPostAdded(@NonNull Post post);
        void onPostRetracted(@NonNull Post post);
        void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId);
        void onIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId);
        void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId);
        void onCommentAdded(@NonNull Comment comment);
        void onCommentRetracted(@NonNull Comment comment);
        void onCommentUpdated(@NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId);
        void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId);
        void onMessageAdded(@NonNull Message message);
        void onMessageRetracted(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId);
        void onMessageDeleted(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId);
        void onMessageUpdated(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId);
        void onGroupChatAdded(@NonNull GroupId groupId);
        void onGroupMetadataChanged(@NonNull GroupId groupId);
        void onGroupMembersChanged(@NonNull GroupId groupId);
        void onGroupAdminsChanged(@NonNull GroupId groupId);
        void onGroupBackgroundChanged(@NonNull GroupId groupId);
        void onOutgoingMessageDelivered(@NonNull ChatId chatId, @NonNull UserId recipientUserId, @NonNull String messageId);
        void onOutgoingMessageSeen(@NonNull ChatId chatId, @NonNull UserId seenByUserId, @NonNull String messageId);
        void onChatSeen(@NonNull ChatId chatId, @NonNull Collection<SeenReceipt> seenReceipts);
        void onChatDeleted(@NonNull ChatId chatId);
        void onFeedCleanup();
        void onDbCreated();
    }

    public static class DefaultObserver implements Observer {
        public void onPostAdded(@NonNull Post post) {}
        public void onPostRetracted(@NonNull Post post) {}
        public void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {}
        public void onPostAudienceChanged(@NonNull Post post, @NonNull Collection<UserId> addedUsers) { }
        public void onIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId) {}
        public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId) {}
        public void onCommentAdded(@NonNull Comment comment) {}
        public void onCommentRetracted(@NonNull Comment comment) {}
        public void onCommentUpdated(@NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {}
        public void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId) {}
        public void onMessageAdded(@NonNull Message message) {}
        public void onMessageRetracted(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {}
        public void onMessageDeleted(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {}
        public void onMessageUpdated(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {}
        public void onGroupChatAdded(@NonNull GroupId groupId) {}
        public void onGroupMetadataChanged(@NonNull GroupId groupId) {}
        public void onGroupMembersChanged(@NonNull GroupId groupId) {}
        public void onGroupAdminsChanged(@NonNull GroupId groupId) {}
        public void onGroupBackgroundChanged(@NonNull GroupId groupId) {}
        public void onOutgoingMessageDelivered(@NonNull ChatId chatId, @NonNull UserId recipientUserId, @NonNull String messageId) {}
        public void onOutgoingMessageSeen(@NonNull ChatId chatId, @NonNull UserId seenByUserId, @NonNull String messageId) {}
        public void onChatSeen(@NonNull ChatId chatId, @NonNull Collection<SeenReceipt> seenReceipts) {}
        public void onChatDeleted(@NonNull ChatId chatId) {}

        public void onFeedCleanup() {}
        public void onDbCreated() {}
    }

    public static ContentDb getInstance() {
        if (instance == null) {
            synchronized (ContentDb.class) {
                if (instance == null) {
                    instance = new ContentDb(Me.getInstance(), FileStore.getInstance(), AppContext.getInstance(), ServerProps.getInstance());
                }
            }
        }
        return instance;
    }

    private ContentDb(
            @NonNull Me me,
            @NonNull final FileStore fileStore,
            final @NonNull AppContext appContext,
            @NonNull final ServerProps serverProps) {
        Context context = appContext.get();
        databaseHelper = new ContentDbHelper(context.getApplicationContext(), observers);
        this.me = me;
        this.serverProps = serverProps;

        mentionsDb = new MentionsDb(databaseHelper);
        messagesDb = new MessagesDb(fileStore, mentionsDb, serverProps, databaseHelper);
        postsDb = new PostsDb(mentionsDb, databaseHelper, fileStore, serverProps);
    }

    public void addObserver(@NonNull Observer observer) {
        observers.addObserver(observer);
    }

    public void removeObserver(@NonNull Observer observer) {
        observers.removeObserver(observer);
    }

    public void addPost(@NonNull Post post) {
        addPost(post, null);
    }

    public void addPost(@NonNull Post post, @Nullable Runnable completionRunnable) {
        addFeedItems(Collections.singletonList(post), new ArrayList<>(), completionRunnable);
    }

    public void addFeedItems(@NonNull List<Post> posts, @NonNull List<Comment> comments, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            for (Post post : posts) {
                boolean duplicate = false;
                final SQLiteDatabase db = databaseHelper.getWritableDatabase();
                db.beginTransaction();
                try {
                    if (post.isRetracted()) {
                        postsDb.retractPost(post);
                    } else {
                        try {
                            postsDb.addPost(post);
                        } catch (SQLiteConstraintException ex) {
                            Log.w("ContentDb.addPost: duplicate " + ex.getMessage() + " " + post);
                            duplicate = true;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                // important to notify outside of transaction
                if (!duplicate) {
                    if (post.isRetracted()) {
                        observers.notifyPostRetracted(post);
                    } else {
                        observers.notifyPostAdded(post);
                    }
                }
            }

            final HashMap<String, Post> postCache = new HashMap<>();
            final HashSet<String> checkedIds = new HashSet<>();
            for (Comment comment : comments) {
                if (checkedIds.contains(comment.postId)) {
                    comment.setParentPost(postCache.get(comment.postId));
                } else {
                    Post parentPost = postsDb.getPost(comment.postId);
                    if (parentPost != null) {
                        comment.setParentPost(parentPost);
                        postCache.put(comment.postId, parentPost);
                    }
                    checkedIds.add(comment.postId);
                }
                if (comment.isRetracted()) {
                    postsDb.retractComment(comment);
                    observers.notifyCommentRetracted(comment);
                } else {
                    try {
                        postsDb.addComment(comment);
                        observers.notifyCommentAdded(comment);
                    } catch (SQLiteConstraintException ex) {
                        Log.w("ContentDb.addComment: duplicate " + ex.getMessage() + " " + comment);
                    }
                }
            }

            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void retractPost(@NonNull Post post) {
        databaseWriteExecutor.execute(() -> {
            postsDb.retractPost(post);
            observers.notifyPostRetracted(post);
        });
    }

    public void setIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId) {
        databaseWriteExecutor.execute(() -> {
            postsDb.setIncomingPostSeen(senderUserId, postId);
            observers.notifyIncomingPostSeen(senderUserId, postId);
        });
    }

    // for debug only
    public void setIncomingPostsSeen(@Post.SeenState int seen) {
        databaseWriteExecutor.execute(() -> postsDb.setIncomingPostsSeen(seen));
    }

    public void setPostSeenReceiptSent(@NonNull UserId senderUserId, @NonNull String postId) {
        databaseWriteExecutor.execute(() -> postsDb.setPostSeenReceiptSent(senderUserId, postId));
    }

    public void setOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId, long timestamp, @NonNull Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            postsDb.setOutgoingPostSeen(seenByUserId, postId, timestamp);
            observers.notifyOutgoingPostSeen(seenByUserId, postId);
            completionRunnable.run();
        });
    }

    public void setPostTransferred(@NonNull UserId senderUserId, @NonNull String postId) {
        databaseWriteExecutor.execute(() -> {
            postsDb.setPostTransferred(senderUserId, postId);
            observers.notifyPostUpdated(senderUserId, postId);
        });
    }

    public void setMediaTransferred(@NonNull Post post, @NonNull Media media) {
        databaseWriteExecutor.execute(() -> {
            postsDb.setMediaTransferred(post, media);
            if (post.isIncoming() && post.isAllMediaTransferred()) {
                postsDb.setPostTransferred(post.senderUserId, post.id);
            }
            observers.notifyPostUpdated(post.senderUserId, post.id);
        });
    }

    public void setMediaTransferred(@NonNull Comment comment, @NonNull Media media) {
        databaseWriteExecutor.execute(() -> {
            postsDb.setMediaTransferred(comment, media);
            if (comment.isIncoming() && comment.isAllMediaTransferred()) {
                postsDb.setCommentTransferred(comment.postId, comment.senderUserId, comment.id);
            }
            // TODO(jack): Should this be notifyCommentUpdated?
            observers.notifyPostUpdated(comment.senderUserId, comment.id);
        });
    }

    @WorkerThread
    public void setPatchUrl(@NonNull Post post, long rowId, @NonNull String url) {
        databaseWriteExecutor.execute(() -> {
            Log.i("Set patch url in Post: " + post);
            postsDb.setPatchUrl(rowId, url);
        });
    }

    @WorkerThread
    public void setPatchUrl(@NonNull Comment comment, long rowId, @NonNull String url) {
        databaseWriteExecutor.execute(() -> {
            Log.i("Set patch url in Comment: " + comment);
            postsDb.setPatchUrl(rowId, url);
        });
    }

    @WorkerThread
    public void setPatchUrl(@NonNull Message message, long rowId, @NonNull String url) {
        databaseWriteExecutor.execute(() -> {
            Log.i("Set patch url in Message: " + message);
            messagesDb.setPatchUrl(rowId, url);
        });
    }

    @WorkerThread
    public String getPatchUrl(@NonNull Post post, long rowId) {
        Log.i("Get patch url in Post: " + post);
        return postsDb.getPatchUrl(rowId);
    }

    @WorkerThread
    public String getPatchUrl(@NonNull Comment comment, long rowId) {
        Log.i("Get patch url in Comment: " + comment);
        return postsDb.getPatchUrl(rowId);
    }

    @WorkerThread
    public String getPatchUrl(@NonNull Message message, long rowId) {
        Log.i("Get patch url in Message: " + message);
        return messagesDb.getPatchUrl(rowId);
    }

    @WorkerThread
    public @Media.TransferredState int getMediaTransferred(@NonNull Post post, long rowId) {
        Log.i("Get transferred state from Post: "+ post);
        return postsDb.getMediaTransferred(rowId);
    }

    @WorkerThread
    public @Media.TransferredState int getMediaTransferred(@NonNull Comment comment, long rowId) {
        Log.i("Get transferred state from Comment: "+ comment);
        return postsDb.getMediaTransferred(rowId);
    }

    @WorkerThread
    public @Media.TransferredState int getMediaTransferred(@NonNull Message message, long rowId) {
        Log.i("Get transferred state from Message: "+ message);
        return messagesDb.getMediaTransferred(rowId);
    }

    @WorkerThread
    public byte[] getMediaEncKey(@NonNull Post post, long rowId) {
        Log.i("Get Media encKey from Post: "+ post);
        return postsDb.getMediaEncKey(rowId);
    }

    @WorkerThread
    public byte[] getMediaEncKey(@NonNull Comment comment, long rowId) {
        Log.i("Get Media encKey from Comment: "+ comment);
        return postsDb.getMediaEncKey(rowId);
    }

    @WorkerThread
    public byte[] getMediaEncKey(@NonNull Message message, long rowId) {
        Log.i("Get Media encKey from Message: "+ message);
        return messagesDb.getMediaEncKey(rowId);
    }

    @WorkerThread
    public void setUploadProgress(@NonNull Post post, long rowId, long offset) {
        databaseWriteExecutor.execute(() -> {
            Log.i("Set upload progress in Post: " + post);
            postsDb.setUploadProgress(rowId, offset);
        });
    }

    @WorkerThread
    public void setUploadProgress(@NonNull Comment comment, long rowId, long offset) {
        databaseWriteExecutor.execute(() -> {
            Log.i("Set upload progress in Comment: " + comment);
            postsDb.setUploadProgress(rowId, offset);
        });
    }

    @WorkerThread
    public void setUploadProgress(@NonNull Message message, long rowId, long offset) {
        databaseWriteExecutor.execute(() -> {
            Log.i("Set upload progress in Message: " + message);
            messagesDb.setUploadProgress(rowId, offset);
        });
    }

    @WorkerThread
    public long getUploadProgress(@NonNull Post post, long rowId) {
        Log.i("Get upload progress in Post: " + post);
        return postsDb.getUploadProgress(rowId);
    }

    @WorkerThread
    public long getUploadProgress(@NonNull Comment comment, long rowId) {
        Log.i("Get upload progress in Comment: " + comment);
        return postsDb.getUploadProgress(rowId);
    }

    @WorkerThread
    public long getUploadProgress(@NonNull Message message, long rowId) {
        Log.i("Get upload progress in Message: " + message);
        return messagesDb.getUploadProgress(rowId);
    }

    @WorkerThread
    public void setRetryCount(@NonNull Post post, long rowId, int count) {
        databaseWriteExecutor.execute(() -> {
            Log.i("Set retry count in Post: " + post);
            postsDb.setRetryCount(rowId, count);
        });
    }

    @WorkerThread
    public void setRetryCount(@NonNull Comment comment, long rowId, int count) {
        databaseWriteExecutor.execute(() -> {
            Log.i("Set retry count in Comment: " + comment);
            postsDb.setRetryCount(rowId, count);
        });
    }

    @WorkerThread
    public void setRetryCount(@NonNull Message message, long rowId, int count) {
        databaseWriteExecutor.execute(() -> {
            Log.i("Set retry count in Message: " + message);
            messagesDb.setRetryCount(rowId, count);
        });
    }

    @WorkerThread
    public int getRetryCount(@NonNull Post post, long rowId) {
        Log.i("Get retry count in Post: " + post);
        return postsDb.getRetryCount(rowId);
    }

    @WorkerThread
    public int getRetryCount(@NonNull Comment comment, long rowId) {
        Log.i("Get retry count in Comment: " + comment);
        return postsDb.getRetryCount(rowId);
    }

    @WorkerThread
    public int getRetryCount(@NonNull Message message, long rowId) {
        Log.i("Get retry count in Message: " + message);
        return messagesDb.getRetryCount(rowId);
    }

    @WorkerThread
    public @NonNull List<Post> getUnseenPosts(long timestamp, int count) {
        return getPosts(timestamp, count, false, null, true);
    }

    @WorkerThread
    public @NonNull List<Post> getUnseenGroupPosts(@NonNull GroupId groupId) {
        return getPosts(null, 256, true, null, groupId, true);
    }

    @WorkerThread
    public @Nullable Post getLastUnseenGroupPost(@NonNull GroupId groupId) {
        List<Post> posts = getPosts(null, 10, true, null, groupId, true);
        if (posts.isEmpty()) {
            return null;
        }
        return posts.get(0);
    }

    @WorkerThread
    public @Nullable Post getLastGroupPost(@NonNull GroupId groupId) {
        List<Post> posts = getPosts(null, Constants.MAX_POST_MEDIA_ITEMS + 1, true, null, groupId, false);
        if (posts.isEmpty()) {
            return null;
        }
        return posts.get(0);
    }

    @WorkerThread
    @NonNull List<Post> getPosts(@Nullable Long timestamp, int count, boolean after, @Nullable UserId senderUserId) {
        return getPosts(timestamp, count, after, senderUserId,null);
    }

    @WorkerThread
    @NonNull List<Post> getPosts(@Nullable Long timestamp, int count, boolean after, @Nullable UserId senderUserId, @Nullable GroupId groupId) {
        return getPosts(timestamp, count, after, senderUserId, groupId, false);
    }

    @WorkerThread
    private @NonNull List<Post> getPosts(@Nullable Long timestamp, int count, boolean after, @Nullable UserId senderUserId, boolean unseenOnly) {
        return getPosts(timestamp, count, after, senderUserId, null, unseenOnly);
    }

    @WorkerThread
    private @NonNull List<Post> getPosts(@Nullable Long timestamp, int count, boolean after, @Nullable UserId senderUserId, @Nullable GroupId groupId, boolean unseenOnly) {
        return postsDb.getPosts(timestamp, count, after, senderUserId, groupId, unseenOnly);
    }

    @WorkerThread
    public @Nullable Post getPost(@NonNull String postId) {
        return postsDb.getPost(postId);
    }

    @WorkerThread
    public @NonNull List<UserId> getPostSeenByUsers(@NonNull String postId) {
        return postsDb.getPostSeenByUsers(postId);
    }

    @WorkerThread
    public @NonNull List<UserId> getPostAudience(@NonNull String postId) {
        return postsDb.getPostSeenByUsers(postId);
    }

    @WorkerThread
    public @NonNull List<SeenByInfo> getPostSeenByInfos(@NonNull String postId) {
        return postsDb.getPostSeenByInfos(postId);
    }

    public void addComment(@NonNull Comment comment) {
        addFeedItems(new ArrayList<>(), Collections.singletonList(comment), null);
    }

    public void retractComment(@NonNull Comment comment) {
        databaseWriteExecutor.execute(() -> {
            Comment dbComment = comment;
            if (dbComment.rowId == 0) {
                Comment temp = postsDb.getComment(comment.id);
                if (temp != null) {
                    dbComment = temp;
                }
            }
            postsDb.retractComment(dbComment);
            observers.notifyCommentRetracted(dbComment);
        });
    }

    public void setCommentTransferred(@NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
        databaseWriteExecutor.execute(() -> {
            postsDb.setCommentTransferred(postId, commentSenderUserId, commentId);
            observers.notifyCommentUpdated(postId, commentSenderUserId, commentId);
        });
    }

    // for debug only
    public void setCommentsSeen(boolean seen) {
        databaseWriteExecutor.execute(() -> {
            if (postsDb.setCommentsSeen(seen)) {
                observers.notifyCommentsSeen(UserId.ME, "");
            }
        });
    }

    public void setCommentsSeen(@NonNull String postId) {
        setCommentsSeen(postId, true);
    }

    public void setCommentsSeen(@NonNull String postId, boolean seen) {
        databaseWriteExecutor.execute(() -> {
            if (postsDb.setCommentsSeen(postId, seen)) {
                Post post = postsDb.getPost(postId);
                if (post != null) {
                    observers.notifyCommentsSeen(post.senderUserId, postId);
                }
            }
        });
    }

    public void setCommentSeen(@NonNull String postId, @NonNull String commentId, boolean seen) {
        databaseWriteExecutor.execute(() -> {
            if (postsDb.setCommentSeen(postId, commentId, seen)) {
                Post post = postsDb.getPost(postId);
                if (post != null) {
                    observers.notifyCommentsSeen(post.senderUserId, postId);
                }
            }
        });
    }

    @WorkerThread
    public long getLastSeenCommentRowId(@NonNull String postId) {
        return postsDb.getLastSeenCommentRowId(postId);
    }

    @WorkerThread
    @NonNull List<Comment> getComments(@NonNull String postId, int start, int count) {
        return postsDb.getComments(postId, start, count);
    }

    /*
     * Returns posts that the userid is mentioned in
     * */
    @WorkerThread
    public @NonNull List<Post> getMentionedPosts(@NonNull UserId userId, int limit) {
        return postsDb.getMentionedPosts(userId, limit);
    }

    /*
     * Returns comments the userid is mentioned in
     */
    @WorkerThread
    public @NonNull List<Comment> getMentionedComments(@NonNull UserId userId, int limit) {
        return postsDb.getMentionedComments(userId, limit);
    }

    /*
     * returns "important" comments only
     * */
    @WorkerThread
    public @NonNull List<Comment> getIncomingCommentsHistory(int limit) {
        return postsDb.getIncomingCommentsHistory(limit);
    }

    @WorkerThread
    public @NonNull List<Comment> getUnseenCommentsOnMyPosts(long timestamp, int count) {
        return postsDb.getUnseenCommentsOnMyPosts(timestamp, count);
    }

    public void addMessage(@NonNull Message message) {
        addMessage(message, false, null);
    }

    public void addMessage(@NonNull Message message, boolean unseen, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            final Post replyPost = message.replyPostId == null ? null : getPost(message.replyPostId);
            final Message replyMessage = message.replyMessageId == null ? null : getMessage(message.chatId, message.replyMessageSenderId, message.replyMessageId);
            if (messagesDb.addMessage(message, unseen, replyPost, replyMessage)) {
                observers.notifyMessageAdded(message);
            }
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void updateMessageDecrypt(@NonNull Message message, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            if (messagesDb.updateMessageDecrypt(message)) {
                observers.notifyMessageUpdated(message.chatId, message.senderUserId, message.id);
            }
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void addSilentMessage(@NonNull Message message, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            messagesDb.addSilentMessage(message);
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void updateSilentMessageDecrypt(@NonNull Message message, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            messagesDb.updateSilentMessageDecrypt(message);
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void addGroupChat(@NonNull GroupInfo groupInfo, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            if (messagesDb.addGroupChat(groupInfo)) {
                observers.notifyGroupChatAdded(groupInfo.groupId);
            }
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void updateGroupChat(@NonNull GroupInfo groupInfo, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            if (messagesDb.updateGroupChat(groupInfo)) {
                observers.notifyGroupMetadataChanged(groupInfo.groupId);
            }
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void setGroupName(@NonNull GroupId groupId, @NonNull String name, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            if (messagesDb.setGroupName(groupId, name)) {
                observers.notifyGroupMetadataChanged(groupId);
            }
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void setGroupTheme(@NonNull GroupId groupId, int theme, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            if (messagesDb.setGroupTheme(groupId, theme)) {
                observers.notifyGroupBackgroundChanged(groupId);
            }
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void setGroupAvatar(@NonNull GroupId groupId, @NonNull String avatarId, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            if (messagesDb.setGroupAvatar(groupId, avatarId)) {
                observers.notifyGroupMetadataChanged(groupId);
            }
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void setGroupInactive(@NonNull GroupId groupId, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            if (messagesDb.setGroupInactive(groupId)) {
                observers.notifyGroupMetadataChanged(groupId);
            }
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void addRemoveGroupMembers(@NonNull GroupId groupId, @Nullable String groupName, @Nullable String avatarId, @NonNull List<MemberInfo> added, @NonNull List<MemberInfo> removed, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            if (messagesDb.addRemoveGroupMembers(groupId, groupName, avatarId, added, removed)) {
                observers.notifyGroupMembersChanged(groupId);
            }
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void promoteDemoteGroupAdmins(@NonNull GroupId groupId, @NonNull List<MemberInfo> promoted, @NonNull List<MemberInfo> demoted, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            if (messagesDb.promoteDemoteGroupAdmins(groupId, promoted, demoted)) {
                observers.notifyGroupAdminsChanged(groupId);
            }
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void deleteMessage(long rowId) {
        databaseWriteExecutor.execute(() -> {
            Message message = messagesDb.getMessage(rowId);
            if (message != null) {
                messagesDb.deleteMessage(rowId);
                observers.notifyMessageDeleted(message.chatId, message.senderUserId, message.id);
            }
        });
    }

    public void retractMessage(long rowId, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            Message msg = messagesDb.getMessage(rowId);
            if (msg != null) {
                retractMessage(msg, completionRunnable);
            } else {
                Log.e("ContentDb/retractMessage no message found for row id");
            }
        });
    }

    public void retractMessage(@NonNull ChatId chatId, @NonNull UserId senderId, @NonNull String msgId, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            Message msg = messagesDb.getMessage(chatId, senderId, msgId);
            if (msg != null) {
                retractMessage(msg, completionRunnable);
            } else {
                Log.e("ContentDb/retractMessage no message found for row id");
            }
        });
    }

    public void retractMessage(@NonNull Message message, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            messagesDb.retractMessage(message);
            observers.notifyMessageRetracted(message.chatId, message.senderUserId, message.id);
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void setMediaTransferred(@NonNull Message message, @NonNull Media media) {
        databaseWriteExecutor.execute(() -> {
            messagesDb.setMediaTransferred(message, media);
            if (message.isIncoming() && message.isAllMediaTransferred()) {
                messagesDb.setMessageTransferred(message.chatId, message.senderUserId, message.id);
            }
            observers.notifyMessageUpdated(message.chatId, message.senderUserId, message.id);
        });
    }

    public void setMessageRerequestCount(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId, int count) {
        databaseWriteExecutor.execute(() -> {
            messagesDb.setMessageRerequestCount(chatId, senderUserId, messageId, count);
        });
    }

    public void setSilentMessageRerequestCount(@NonNull UserId senderUserId, @NonNull String messageId, int count) {
        databaseWriteExecutor.execute(() -> {
            messagesDb.setSilentMessageRerequestCount(senderUserId, messageId, count);
        });
    }

    public void setMessageTransferred(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        databaseWriteExecutor.execute(() -> {
            messagesDb.setMessageTransferred(chatId, senderUserId, messageId);
            observers.notifyMessageUpdated(chatId, senderUserId, messageId);
        });
    }

    public void setOutgoingMessageDelivered(@NonNull ChatId chatId, @NonNull UserId recipientUserId, @NonNull String messageId, long timestamp, @NonNull Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            messagesDb.setOutgoingMessageDelivered(chatId, recipientUserId, messageId, timestamp);
            observers.notifyOutgoingMessageDelivered(chatId, recipientUserId, messageId);
            completionRunnable.run();
        });
    }

    public void setOutgoingMessageSeen(@NonNull ChatId chatId, @NonNull UserId recipientUserId, @NonNull String messageId, long timestamp, @NonNull Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            messagesDb.setOutgoingMessageSeen(chatId, recipientUserId, messageId, timestamp);
            observers.notifyOutgoingMessageSeen(chatId, recipientUserId, messageId);
            completionRunnable.run();
        });
    }

    @WorkerThread
    public @NonNull List<Message> getUnseenMessages(int count) {
        return messagesDb.getUnseenMessages(count);
    }

    @WorkerThread
    public boolean hasMessage(UserId senderUserId, String id) {
        return messagesDb.hasMessage(senderUserId, id);
    }

    @WorkerThread
    public boolean hasSilentMessage(UserId senderUserId, String id) {
        return messagesDb.hasSilentMessage(senderUserId, id);
    }

    @WorkerThread
    public @Nullable Message getMessage(long rowId) {
        return messagesDb.getMessage(rowId);
    }

    @WorkerThread
    public @Nullable Message getMessage(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        return messagesDb.getMessage(chatId, senderUserId, messageId);
    }

    @WorkerThread
    public int getMessageRerequestCount(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        return messagesDb.getMessageRerequestCount(chatId, senderUserId, messageId);
    }

    @WorkerThread
    public int getSilentMessageRerequestCount(@NonNull UserId senderUserId, @NonNull String messageId) {
        return messagesDb.getSilentMessageRerequestCount(senderUserId, messageId);
    }

    @WorkerThread
    public @Nullable Message getMessageForMedia(long mediaRowId) {
        return messagesDb.getMessageForMedia(mediaRowId);
    }

    @WorkerThread
    @NonNull List<Message> getMessages(@NonNull ChatId chatId, @Nullable Long startRowId, int count, boolean after) {
        return messagesDb.getMessages(chatId, startRowId, count, after);
    }

    @WorkerThread
    public @NonNull List<Media> getChatMedia(@NonNull ChatId chatId, @Nullable Long startRowId, int count, boolean after) {
        return messagesDb.getChatMedia(chatId, startRowId, count, after);
    }

    @WorkerThread
    public long getChatMediaPosition(ChatId chatId, long rowId) {
        return messagesDb.getChatMediaPosition(chatId, rowId);
    }

    @WorkerThread
    public long getChatMediaCount(ChatId chatId) {
        return messagesDb.getChatMediaCount(chatId);
    }

    @WorkerThread
    public @Nullable ReplyPreview getReplyPreview(long messageRowId) {
        return messagesDb.getReplyPreview(messageRowId);
    }

    @WorkerThread
    public @NonNull List<Chat> getChats(boolean includeGroups) {
        return messagesDb.getChats(includeGroups);
    }

    @WorkerThread
    public @NonNull List<Chat> getGroups() {
        return messagesDb.getGroups();
    }

    @WorkerThread
    public @Nullable Chat getChat(@NonNull ChatId chatId) {
        return messagesDb.getChat(chatId);
    }

    @WorkerThread
    public @NonNull List<MemberInfo> getGroupMembers(@NonNull GroupId groupId) {
        return messagesDb.getGroupMembers(groupId);
    }

    @WorkerThread
    public int getUnseenChatsCount() {
        return messagesDb.getUnseenChatsCount();
    }

    public void deleteChat(@NonNull ChatId chatId) {
        databaseWriteExecutor.execute(() -> {
            messagesDb.deleteChat(chatId);
            observers.notifyChatDeleted(chatId);
        });
    }

    public void setChatSeen(@NonNull ChatId chatId) {
        databaseWriteExecutor.execute(() -> {
            final Collection<SeenReceipt> seenReceipts = messagesDb.setChatSeen(chatId);
            if (!seenReceipts.isEmpty()) {
                observers.notifyChatSeen(chatId, seenReceipts);
            }
        });
    }

    public void setMessageSeenReceiptSent(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        databaseWriteExecutor.execute(() -> messagesDb.setMessageSeenReceiptSent(chatId, senderUserId, messageId));
    }

    @WorkerThread
    @NonNull List<Message> getPendingMessages() {
        return messagesDb.getPendingMessages();
    }

    @WorkerThread
    @NonNull List<SeenReceipt> getPendingMessageSeenReceipts() {
        return messagesDb.getPendingMessageSeenReceipts();
    }

    @WorkerThread
    @NonNull List<Post> getPendingPosts() {
        return postsDb.getPendingPosts();
    }

    @WorkerThread
    @NonNull List<Comment> getPendingComments() {
        return postsDb.getPendingComments();
    }

    @WorkerThread
    @NonNull List<SeenReceipt> getPendingPostSeenReceipts() {
        return postsDb.getPendingPostSeenReceipts();
    }

    public void updatePostAudience(@NonNull Map<UserId, Collection<Post>> shareMap) {
        postsDb.updatePostAudience(shareMap);
    }

    @WorkerThread
    public List<DecryptStats> getSilentMessageDecryptStats(long lastRowId) {
        return messagesDb.getSilentMessageDecryptStats(lastRowId);
    }

    @WorkerThread
    public List<DecryptStats> getMessageDecryptStats(long lastRowId) {
        return messagesDb.getMessageDecryptStats(lastRowId);
    }

    @WorkerThread
    public DecryptStats getMessageDecryptStats(String messageId) {
        return messagesDb.getMessageDecryptStats(messageId);
    }

    @WorkerThread
    public void cleanup() {
        Log.i("ContentDb.cleanup");
        if (postsDb.cleanup()) {
            databaseHelper.getWritableDatabase().execSQL("VACUUM");
            Log.i("ContentDb.cleanup: vacuum");
            observers.notifyFeedCleanup();
        }
    }

    @NonNull
    public Collection<Post> getShareablePosts() {
        return postsDb.getShareablePosts();
    }

    // TODO(clarkc): remove after version 100
    public void fixGroupMembership() {
        Log.i("ContentDb.fixGroupMembership");
        messagesDb.fixGroupMembership();
        Log.i("ContentDb.groupmembership fix complete");
    }

    public void deleteDb() {
        databaseHelper.deleteDb();
    }
}
