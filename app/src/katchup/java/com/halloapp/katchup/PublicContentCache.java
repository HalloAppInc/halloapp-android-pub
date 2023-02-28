package com.halloapp.katchup;

import androidx.annotation.NonNull;

import com.halloapp.ConnectionObservers;
import com.halloapp.content.Comment;
import com.halloapp.content.Post;
import com.halloapp.proto.server.MomentNotification;
import com.halloapp.xmpp.Connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublicContentCache {
    private static PublicContentCache instance;

    public static PublicContentCache getInstance() {
        if (instance == null) {
            synchronized (PublicContentCache.class) {
                if (instance == null) {
                    instance = new PublicContentCache(
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

    private PublicContentCache(@NonNull ConnectionObservers connectionObservers) {
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

    public void addPost(@NonNull Post post) {
        postCache.put(post.id, post);
    }

    public void addComment(@NonNull String postId, @NonNull Comment comment) {
        List<Comment> existingComments = commentCache.get(postId);
        if (existingComments == null) {
            existingComments = new ArrayList<>();
        }
        existingComments = new ArrayList<>(existingComments);
        existingComments.add(comment);
        commentCache.put(postId, existingComments);
    }

    public void removeComment(@NonNull String postId, @NonNull Comment comment) {
        List<Comment> existingComments = commentCache.get(postId);
        if (existingComments == null) {
            return;
        }
        existingComments = new ArrayList<>(existingComments);
        existingComments.remove(comment);
        commentCache.put(postId, existingComments);
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
