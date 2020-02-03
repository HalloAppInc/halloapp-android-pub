package com.halloapp;

import android.content.Context;

import androidx.annotation.NonNull;

import com.halloapp.contacts.UserId;
import com.halloapp.media.DownloadPostTask;
import com.halloapp.media.MediaStore;
import com.halloapp.media.MediaUploadDownloadThreadPool;
import com.halloapp.media.UploadPostTask;
import com.halloapp.posts.Comment;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;

import java.util.Collection;

public class MainPostsObserver implements PostsDb.Observer {

    private static MainPostsObserver instance;

    private final Connection connection;
    private final MediaStore mediaStore;
    private final PostsDb postsDb;

    public static MainPostsObserver getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized(MainPostsObserver.class) {
                if (instance == null) {
                    instance = new MainPostsObserver(Connection.getInstance(), MediaStore.getInstance(context), PostsDb.getInstance(context));
                }
            }
        }
        return instance;
    }

    private MainPostsObserver(@NonNull Connection connection, @NonNull MediaStore mediaStore, @NonNull PostsDb postsDb) {
        this.connection = connection;
        this.mediaStore = mediaStore;
        this.postsDb = postsDb;
    }

    @Override
    public void onPostAdded(@NonNull Post post) {
        if (post.isOutgoing()) {
            if (post.media.isEmpty()) {
                connection.sendPost(post);
            } else {
                new UploadPostTask(post, connection, postsDb).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR);
            }
        } else { // if (post.isIncoming())
            connection.sendDeliveryReceipt(post);

            if (!post.media.isEmpty()) {
                new DownloadPostTask(post, mediaStore, postsDb).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR);
            }
        }
    }

    @Override
    public void onPostDuplicate(@NonNull Post post) {
        //connection.sendDeliveryReceipt(post);
    }

    @Override
    public void onPostDeleted(@NonNull Post post) {
    }

    @Override
    public void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {
    }

    @Override
    public void onCommentAdded(@NonNull Comment comment) {
        if (comment.isOutgoing()) {
            connection.sendComment(comment);
        } else { // if (post.isIncoming())
            connection.sendDeliveryReceipt(comment);
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
    }

    @Override
    public void onHistoryAdded(@NonNull Collection<Post> historyPosts, @NonNull Collection<Comment> historyComments) {
        for (Post post : historyPosts) {
            if (!post.media.isEmpty()) {
                new DownloadPostTask(post, mediaStore, postsDb).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR);
            }
        }
    }
}
