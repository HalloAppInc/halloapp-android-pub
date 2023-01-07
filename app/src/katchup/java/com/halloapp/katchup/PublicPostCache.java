package com.halloapp.katchup;

import androidx.annotation.NonNull;

import com.halloapp.content.Post;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublicPostCache {
    private static PublicPostCache instance;

    public static PublicPostCache getInstance() {
        if (instance == null) {
            synchronized (PublicPostCache.class) {
                if (instance == null) {
                    instance = new PublicPostCache();
                }
            }
        }
        return instance;
    }

    private PublicPostCache() {}

    private Map<String, Post> cache = new HashMap<>();

    public void insertPosts(@NonNull List<Post> posts) {
        for (Post post : posts) {
            cache.put(post.id, post);
        }
    }

    public Post getPost(@NonNull String postId) {
        return cache.get(postId);
    }

    public void clear() {
        cache.clear();
    }
}
