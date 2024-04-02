package com.halloapp.content;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.halloapp.FileStore;
import com.halloapp.crypto.signal.SignalSessionManager;
import com.halloapp.crypto.signal.SignalSessionSetupInfo;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.media.DownloadMediaTask;
import com.halloapp.media.MediaUploadDownloadThreadPool;
import com.halloapp.media.UploadMediaTask;
import com.halloapp.props.ServerProps;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;

import java.util.List;

public class TransferPendingItemsTask extends AsyncTask<Void, Void, Void> {

    private final Connection connection;
    private final FileStore fileStore;
    private final ContentDb contentDb;
    private final SignalSessionManager signalSessionManager;

    public TransferPendingItemsTask(@NonNull Context context) {
        this.connection = Connection.getInstance();
        this.fileStore = FileStore.getInstance();
        this.contentDb = ContentDb.getInstance();
        this.signalSessionManager = SignalSessionManager.getInstance();
    }

    @Override
    protected Void doInBackground(Void... voids) {

        final Handler mainHandler = new Handler(Looper.getMainLooper());

        final List<Post> posts = contentDb.getPendingPosts();
        Log.i("TransferPendingItemsTask: " + posts.size() + " posts");
        for (Post post : posts) {
            if (post.isRetracted()) {
                connection.retractPost(post.id);
                continue;
            }
            if (post.isIncoming()) {
                if (post.hasMedia()) {
                    mainHandler.post(() -> DownloadMediaTask.download(post, fileStore, contentDb));
                }
            } else /*post.isOutgoing()*/ {
                if (!post.hasMedia()) {
                    connection.sendPost(post);
                } else {
                    mainHandler.post(() -> new UploadMediaTask(post, fileStore, contentDb, connection).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR));
                }
            }
        }

        final List<Message> messages = contentDb.getPendingMessages();
        Log.i("TransferPendingItemsTask: " + messages.size() + " messages");
        for (Message message : messages) {
            if (message.isIncoming()) {
                if (message.hasMedia()) {
                    mainHandler.post(() -> DownloadMediaTask.download(message, fileStore, contentDb));
                } else {
                    contentDb.setMessageTransferred(message.chatId, message.senderUserId, message.id);
                }
            } else /*post.isOutgoing()*/ {
                if (message.isRetracted()) {
                    if (message.chatId instanceof UserId) {
                        connection.retractMessage((UserId) message.chatId, message.id);
                    } else if (message.chatId instanceof GroupId) {
                        connection.retractGroupMessage((GroupId) message.chatId, message.id);
                    }
                } else if (!message.hasMedia()) {
                    signalSessionManager.sendMessage(message);
                } else {
                    mainHandler.post(() -> new UploadMediaTask(message, fileStore, contentDb, connection).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR));
                }
            }
        }

        final List<Comment> comments = contentDb.getPendingComments();
        Log.i("TransferPendingItemsTask: " + comments.size() + " comments");
        for (Comment comment : comments) {
            if (comment.isIncoming()) {
                if (comment.hasMedia()) {
                    mainHandler.post(() -> DownloadMediaTask.download(comment, fileStore, contentDb));
                } else {
                    contentDb.setCommentTransferred(comment.postId, comment.senderUserId, comment.id);
                }
            } else /*comment.isOutgoing()*/ {
                if (comment.isRetracted()) {
                    connection.retractComment(comment.postId, comment.id);
                } else if (!comment.hasMedia()) {
                    connection.sendComment(comment);
                } else {
                    mainHandler.post(() -> new UploadMediaTask(comment, fileStore, contentDb, connection).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR));
                }
            }
        }

        final List<Reaction> reactions = contentDb.getPendingReactions();
        Log.i("TransferPendingItemsTask: " + reactions.size() + " reactions");
        for (Reaction reaction : reactions) {
            // TODO: Differentiate reaction parent item types
            if (ServerProps.getInstance().getChatReactionsEnabled()) {
                Message message = contentDb.getMessage(reaction.contentId);
                if (message != null) {
                    if (message.chatId instanceof GroupId) {
                        connection.sendGroupChatReaction(reaction, message);
                    } else {
                        try {
                            SignalSessionSetupInfo signalSessionSetupInfo = signalSessionManager.getSessionSetupInfo((UserId) message.chatId);
                            connection.sendChatReaction(reaction, message, signalSessionSetupInfo);
                        } catch (Exception e) {
                            Log.e("Failed to encrypt chat reaction", e);
                        }
                    }
                }
            }

            if (ServerProps.getInstance().getCommentReactionsEnabled()) {
                Comment comment = contentDb.getComment(reaction.contentId);
                if (comment != null) {
                    ReactionComment reactionComment = new ReactionComment(reaction, 0, comment.postId, reaction.senderUserId, reaction.reactionId, reaction.contentId, reaction.timestamp, Comment.TRANSFERRED_NO, true, null);
                    Post parentPost = contentDb.getPost(reactionComment.postId);
                    reactionComment.setParentPost(parentPost);
                    connection.sendComment(reactionComment);
                }
            }
        }

        final List<SeenReceipt> postSeenReceipts = contentDb.getPendingPostSeenReceipts();
        Log.i("TransferPendingItemsTask: " + postSeenReceipts.size() + " post seen receipts");
        for (SeenReceipt receipt : postSeenReceipts) {
            connection.sendPostSeenReceipt(receipt.senderUserId, receipt.itemId);
        }

        final List<SeenReceipt> messageSeenReceipts = contentDb.getPendingMessageSeenReceipts();
        Log.i("TransferPendingItemsTask: " + messageSeenReceipts.size() + " message seen receipts");
        for (SeenReceipt receipt : messageSeenReceipts) {
            connection.sendMessageSeenReceipt(receipt.chatId, receipt.senderUserId, receipt.itemId);
        }

        final List<PlayedReceipt> messagePlayedReceipts = contentDb.getPendingMessagePlayedReceipts();
        Log.i("TransferPendingItemsTask: " + messagePlayedReceipts.size() + " message played receipts");
        for (PlayedReceipt receipt : messagePlayedReceipts) {
            connection.sendMessagePlayedReceipt(receipt.chatId, receipt.senderUserId, receipt.itemId);
        }
        return null;
    }
}
