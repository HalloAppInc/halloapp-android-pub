package com.halloapp.katchup;

import androidx.annotation.NonNull;

import com.halloapp.ConnectionObservers;
import com.halloapp.content.Comment;
import com.halloapp.content.Post;
import com.halloapp.proto.server.MomentNotification;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublicPostCache {
    private static PublicPostCache instance;

    public static PublicPostCache getInstance() {
        if (instance == null) {
            synchronized (PublicPostCache.class) {
                if (instance == null) {
                    instance = new PublicPostCache(
                            ConnectionObservers.getInstance()
                    );
                }
            }
        }
        return instance;
    }

    private Connection.Observer connectionObserver = new Connection.Observer() {
        @Override
        public void onMomentNotificationReceived(MomentNotification momentNotification, @NonNull String ackId) {
            clear();
        }
    };

    private PublicPostCache(@NonNull ConnectionObservers connectionObservers) {
        connectionObservers.addObserver(connectionObserver);
    }

    private final Map<String, Post> postCache = new HashMap<>();
    private final Map<String, List<Comment>> commentCache = new HashMap<>();

    public void insertContent(@NonNull List<Post> posts, @NonNull Map<String, List<Comment>> commentMap) {
        for (Post post : posts) {
            postCache.put(post.id, post);
        }
        commentCache.putAll(commentMap);
    }

    public Post getPost(@NonNull String postId) {
        return postCache.get(postId);
    }

    public List<Comment> getComments(@NonNull String postId) {
        return commentCache.get(postId);
    }

    public void clear() {
        postCache.clear();
        commentCache.clear();
    }
}
