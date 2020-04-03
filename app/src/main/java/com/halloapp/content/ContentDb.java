package com.halloapp.content;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.FileStore;
import com.halloapp.contacts.UserId;
import com.halloapp.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@SuppressWarnings("WeakerAccess")
public class ContentDb {

    private static ContentDb instance;

    private final Executor databaseWriteExecutor = Executors.newSingleThreadExecutor();

    private final ContentDbObservers observers = new ContentDbObservers();
    private final ContentDbHelper databaseHelper;

    private final MessagesDb messagesDb;
    private final PostsDb postsDb;

    public interface Observer {
        void onPostAdded(@NonNull Post post);
        void onPostRetracted(@NonNull UserId senderUserId, @NonNull String postId);
        void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId);
        void onIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId);
        void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId);
        void onCommentAdded(@NonNull Comment comment);
        void onCommentRetracted(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId);
        void onCommentUpdated(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId);
        void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId);
        void onMessageAdded(@NonNull Message message);
        void onMessageRetracted(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String messageId);
        void onMessageUpdated(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String messageId);
        void onOutgoingMessageDelivered(@NonNull String chatId, @NonNull UserId recipientUserId, @NonNull String messageId);
        void onOutgoingMessageSeen(@NonNull String chatId, @NonNull UserId seenByUserId, @NonNull String messageId);
        void onChatSeen(@NonNull String chatId);
        void onChatDeleted(@NonNull String chatId);
        void onFeedHistoryAdded(@NonNull Collection<Post> historyPosts, @NonNull Collection<Comment> historyComments);
        void onFeedCleanup();
        void onDbCreated();
    }

    public static class DefaultObserver implements Observer {
        public void onPostAdded(@NonNull Post post) {}
        public void onPostRetracted(@NonNull UserId senderUserId, @NonNull String postId) {}
        public void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {}
        public void onIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId) {}
        public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId) {}
        public void onCommentAdded(@NonNull Comment comment) {}
        public void onCommentRetracted(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {}
        public void onCommentUpdated(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {}
        public void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId) {}
        public void onMessageAdded(@NonNull Message message) {}
        public void onMessageRetracted(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String messageId) {}
        public void onMessageUpdated(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String messageId) {}
        public void onOutgoingMessageDelivered(@NonNull String chatId, @NonNull UserId recipientUserId, @NonNull String messageId) {}
        public void onOutgoingMessageSeen(@NonNull String chatId, @NonNull UserId seenByUserId, @NonNull String messageId) {}
        public void onChatSeen(@NonNull String chatId) {}
        public void onChatDeleted(@NonNull String chatId) {}
        public void onFeedHistoryAdded(@NonNull Collection<Post> historyPosts, @NonNull Collection<Comment> historyComments) {}
        public void onFeedCleanup() {}
        public void onDbCreated() {}
    }

    public static ContentDb getInstance(final @NonNull Context context) {
        if (instance == null) {
            synchronized(ContentDb.class) {
                if (instance == null) {
                    instance = new ContentDb(context);
                }
            }
        }
        return instance;
    }

    private ContentDb(final @NonNull Context context) {
        databaseHelper = new ContentDbHelper(context.getApplicationContext(), observers);
        messagesDb = new MessagesDb(databaseHelper, FileStore.getInstance(context));
        postsDb = new PostsDb(databaseHelper, FileStore.getInstance(context));
    }

    public void addObserver(@NonNull Observer observer) {
        observers.addObserver(observer);
    }

    public void removeObserver(@NonNull Observer observer) {
        observers.removeObserver(observer);
    }

    public void addPost(@NonNull Post post) {
        addFeedItems(Collections.singletonList(post), new ArrayList<>(), null);
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
                        observers.notifyPostRetracted(post.senderUserId, post.id);
                    } else {
                        observers.notifyPostAdded(post);
                    }
                }
            }

            for (Comment comment : comments) {
                if (comment.isRetracted()) {
                    postsDb.retractComment(comment);
                    observers.notifyCommentRetracted(comment.postSenderUserId, comment.postId, comment.commentSenderUserId, comment.commentId);
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
            observers.notifyPostRetracted(post.senderUserId, post.id);
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
            observers.notifyPostUpdated(post.senderUserId, post.id);
            if (post.isIncoming() && post.isAllMediaTransferred()) {
                setPostTransferred(post.senderUserId, post.id);
            }
        });
    }

    @WorkerThread
    public @NonNull List<Post> getUnseenPosts(long timestamp, int count) {
        return getPosts(timestamp, count, false, false, true);
    }

    @WorkerThread
    @NonNull List<Post> getPosts(@Nullable Long timestamp, int count, boolean after, boolean outgoingOnly) {
        return getPosts(timestamp, count, after, outgoingOnly, false);
    }

    @WorkerThread
    private @NonNull List<Post> getPosts(@Nullable Long timestamp, int count, boolean after, boolean outgoingOnly, boolean unseenOnly) {
        return postsDb.getPosts(timestamp, count, after, outgoingOnly, unseenOnly);
    }

    @WorkerThread
    public @Nullable Post getPost(@NonNull UserId userId, @NonNull String postId) {
        return postsDb.getPost(userId, postId);
    }

    @WorkerThread
    public @NonNull List<UserId> getPostSeenBy(@NonNull String postId) {
        return postsDb.getPostSeenBy(postId);
    }

    public void addComment(@NonNull Comment comment) {
        addFeedItems(new ArrayList<>(), Collections.singletonList(comment), null);
    }

    public void retractComment(@NonNull Comment comment) {
        databaseWriteExecutor.execute(() -> {
            postsDb.retractComment(comment);
            observers.notifyCommentRetracted(comment.postSenderUserId, comment.postId, comment.commentSenderUserId, comment.commentId);
        });
    }

    public void setCommentTransferred(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
        databaseWriteExecutor.execute(() -> {
            postsDb.setCommentTransferred(postSenderUserId, postId, commentSenderUserId, commentId);
            observers.notifyCommentUpdated(postSenderUserId, postId, commentSenderUserId, commentId);
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

    public void setCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId) {
        setCommentsSeen(postSenderUserId, postId, true);
    }

    public void setCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId, boolean seen) {
        databaseWriteExecutor.execute(() -> {
            if (postsDb.setCommentsSeen(postSenderUserId, postId, seen)) {
                observers.notifyCommentsSeen(postSenderUserId, postId);
            }
        });
    }

    @WorkerThread
    @NonNull List<Comment> getComments(@NonNull UserId postSenderUserId, @NonNull String postId, int start, int count) {
        return postsDb.getComments(postSenderUserId, postId, start, count);
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

    void addFeedHistory(@NonNull Collection<Post> historyPosts, @NonNull Collection<Comment> historyComments) {
        databaseWriteExecutor.execute(() -> {
            final List<Post> addedHistoryPosts = new ArrayList<>();
            final Collection<Comment> addedHistoryComments = new ArrayList<>();
            final SQLiteDatabase db = databaseHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                for (Post post : historyPosts) {
                    try {
                        postsDb.addPost(post);
                        addedHistoryPosts.add(post);
                        Log.i("ContentDb.addHistory: post added " + post);
                    } catch (SQLiteConstraintException ex) {
                        Log.i("ContentDb.addHistory: post duplicate " + post, ex);
                    }
                }
                for (Comment comment : historyComments) {
                    try {
                        postsDb.addComment(comment);
                        addedHistoryComments.add(comment);
                        Log.i("ContentDb.addHistory: comment added " + comment);
                    } catch (SQLiteConstraintException ex) {
                        Log.i("ContentDb.addHistory: comment duplicate " + comment);
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            // important to notify outside of transaction
            if (!addedHistoryPosts.isEmpty() || !addedHistoryComments.isEmpty()) {
                Collections.sort(addedHistoryPosts, (o1, o2) -> Long.compare(o2.timestamp, o1.timestamp)); // sort, so download would happen in reverse order
                observers.notifyFeedHistoryAdded(addedHistoryPosts, addedHistoryComments);
            }
        });
    }

    public void addMessage(@NonNull Message message) {
        addMessage(message, null);
    }

    public void addMessage(@NonNull Message message, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {

            boolean duplicate = false;
            final SQLiteDatabase db = databaseHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                if (message.isRetracted()) {
                    messagesDb.retractMessage(message);
                } else {
                    try {
                        messagesDb.addMessage(message);
                    } catch (SQLiteConstraintException ex) {
                        Log.w("ContentDb.addMessage: duplicate " + ex.getMessage() + " " + message);
                        duplicate = true;
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            // important to notify outside of transaction
            if (!duplicate) {
                if (message.isRetracted()) {
                    observers.notifyMessageRetracted(message.chatId, message.senderUserId, message.id);
                } else {
                    observers.notifyMessageAdded(message);
                }
            }

            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void setMediaTransferred(@NonNull Message message, @NonNull Media media) {
        databaseWriteExecutor.execute(() -> {
            messagesDb.setMediaTransferred(message, media);
            observers.notifyMessageUpdated(message.chatId, message.senderUserId, message.id);
            if (message.isIncoming() && message.isAllMediaTransferred()) {
                setMessageTransferred(message.chatId, message.senderUserId, message.id);
            }
        });
    }

    public void setMessageTransferred(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        databaseWriteExecutor.execute(() -> {
            messagesDb.setMessageTransferred(chatId, senderUserId, messageId);
            observers.notifyMessageUpdated(chatId, senderUserId, messageId);
        });
    }

    public void setOutgoingMessageDelivered(@NonNull String chatId, @NonNull UserId recipientUserId, @NonNull String messageId, long timestamp, @NonNull Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            messagesDb.setOutgoingMessageDelivered(chatId, recipientUserId, messageId, timestamp);
            observers.notifyOutgoingMessageDelivered(chatId, recipientUserId, messageId);
            completionRunnable.run();
        });
    }

    public void setOutgoingMessageSeen(@NonNull String chatId, @NonNull UserId recipientUserId, @NonNull String messageId, long timestamp, @NonNull Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            messagesDb.setOutgoingMessageSeen(chatId, recipientUserId, messageId, timestamp);
            observers.notifyOutgoingMessageSeen(chatId, recipientUserId, messageId);
            completionRunnable.run();
        });
    }

    @WorkerThread
    public @NonNull List<Message> getUnseenMessages(long timestamp, int count) {
        return messagesDb.getUnseenMessages(timestamp, count);
    }

    @WorkerThread
    @Nullable Message getMessage(long rowId) {
        return messagesDb.getMessage(rowId);
    }

    @WorkerThread
    @NonNull List<Message> getMessages(@NonNull String chatId, @Nullable Long timestamp, int count, boolean after) {
        return messagesDb.getMessages(chatId, timestamp, count, after);
    }

    @WorkerThread
    public @NonNull List<Chat> getChats() {
        return messagesDb.getChats();
    }

    @WorkerThread
    public @Nullable Chat getChat(@NonNull String chatId) {
        return messagesDb.getChat(chatId);
    }

    public void deleteChat(@NonNull String chatId) {
        databaseWriteExecutor.execute(() -> {
            messagesDb.deleteChat(chatId);
            observers.notifyChatDeleted(chatId);
        });
    }

    public void setChatSeen(@NonNull String chatId) {
        databaseWriteExecutor.execute(() -> {
            if (messagesDb.setChatSeen(chatId)) {
                observers.notifyChatSeen(chatId);
            }
        });
    }

    @WorkerThread
    @NonNull List<Message> getPendingMessages() {
        return messagesDb.getPendingMessages();
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
    @NonNull List<Receipt> getPendingPostSeenReceipts() {
        return postsDb.getPendingPostSeenReceipts();
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

    public void deleteDb() {
        databaseHelper.deleteDb();
    }
}
