package com.halloapp;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.halloapp.contacts.UserId;
import com.halloapp.media.DownloadPostTask;
import com.halloapp.media.MediaStore;
import com.halloapp.media.MediaUploadDownloadThreadPool;
import com.halloapp.media.UploadPostTask;
import com.halloapp.posts.Comment;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;
import com.halloapp.xmpp.Connection;

import java.util.Collection;

public class MainPostsObserver implements PostsDb.Observer {

    private static MainPostsObserver instance;

    private final Connection connection;
    private final MediaStore mediaStore;
    private final PostsDb postsDb;
    private final Notifications notifications;

    public static MainPostsObserver getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized(MainPostsObserver.class) {
                if (instance == null) {
                    instance = new MainPostsObserver(context);
                }
            }
        }
        return instance;
    }

    private MainPostsObserver(@NonNull Context context) {
        this.connection = Connection.getInstance();
        this.mediaStore = MediaStore.getInstance(context);
        this.postsDb = PostsDb.getInstance(context);
        this.notifications = Notifications.getInstance(context);
    }

    @Override
    public void onPostAdded(@NonNull Post post) {
        if (post.isOutgoing()) {
            if (post.media.isEmpty()) {
                connection.sendPost(post);
            } else {
                new UploadPostTask(post, mediaStore, postsDb, connection).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR);
            }
        } else { // if (post.isIncoming())
            if (!post.media.isEmpty()) {
                new DownloadPostTask(post, mediaStore, postsDb).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR);
            }
            notifications.update();
        }
    }

    @Override
    public void onPostDeleted(@NonNull UserId senderUserId, @NonNull String postId) {
    }

    @Override
    public void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {
    }

    @Override
    public void onIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId) {
        connection.sendSeenReceipt(senderUserId, postId);
    }

    @Override
    public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId) {
    }

    @Override
    public void onCommentAdded(@NonNull Comment comment) {
        if (comment.isOutgoing()) {
            connection.sendComment(comment);
        } else { // if (comment.isIncoming())
            if (comment.postSenderUserId.isMe()) {
                notifications.update();
            }
        }
    }

    @Override
    public void onCommentUpdated(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
    }

    @Override
    public void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId) {
    }

    @Override
    public void onHistoryAdded(@NonNull Collection<Post> historyPosts, @NonNull Collection<Comment> historyComments) {
        for (Post post : historyPosts) {
            if (!post.media.isEmpty()) {
                new DownloadPostTask(post, mediaStore, postsDb).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR);
            }
        }
    }

    @Override
    public void onPostsCleanup() {
    }
}
