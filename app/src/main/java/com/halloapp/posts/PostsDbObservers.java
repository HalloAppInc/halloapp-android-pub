package com.halloapp.posts;

import androidx.annotation.NonNull;

import com.halloapp.contacts.UserId;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PostsDbObservers {

    private final Set<PostsDb.Observer> observers = new HashSet<>();

    void addObserver(@NonNull PostsDb.Observer observer) {
        synchronized (observers) {
            observers.add(observer);
        }
    }

    void removeObserver(@NonNull PostsDb.Observer observer) {
        synchronized (observers) {
            observers.remove(observer);
        }
    }

    void notifyPostAdded(@NonNull Post post) {
        synchronized (observers) {
            for (PostsDb.Observer observer : observers) {
                observer.onPostAdded(post);
            }
        }
    }

    void notifyPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {
        synchronized (observers) {
            for (PostsDb.Observer observer : observers) {
                observer.onPostUpdated(senderUserId, postId);
            }
        }
    }

    public void notifyPostRetracted(@NonNull UserId senderUserId, @NonNull String postId) {
        synchronized (observers) {
            for (PostsDb.Observer observer : observers) {
                observer.onPostRetracted(senderUserId, postId);
            }
        }
    }

    void notifyIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId) {
        synchronized (observers) {
            for (PostsDb.Observer observer : observers) {
                observer.onIncomingPostSeen(senderUserId, postId);
            }
        }
    }

    void notifyOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId) {
        synchronized (observers) {
            for (PostsDb.Observer observer : observers) {
                observer.onOutgoingPostSeen(seenByUserId, postId);
            }
        }
    }

    void notifyCommentAdded(@NonNull Comment comment) {
        synchronized (observers) {
            for (PostsDb.Observer observer : observers) {
                observer.onCommentAdded(comment);
            }
        }
    }

    void notifyCommentUpdated(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
        synchronized (observers) {
            for (PostsDb.Observer observer : observers) {
                observer.onCommentUpdated(postSenderUserId, postId, commentSenderUserId, commentId);
            }
        }
    }

    void notifyCommentRetracted(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
        synchronized (observers) {
            for (PostsDb.Observer observer : observers) {
                observer.onCommentRetracted(postSenderUserId, postId, commentSenderUserId, commentId);
            }
        }
    }

    void notifyCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId) {
        synchronized (observers) {
            for (PostsDb.Observer observer : observers) {
                observer.onCommentsSeen(postSenderUserId, postId);
            }
        }
    }

    void notifyHistoryAdded(@NonNull Collection<Post> historyPosts, @NonNull Collection<Comment> historyComments) {
        synchronized (observers) {
            for (PostsDb.Observer observer : observers) {
                observer.onHistoryAdded(historyPosts, historyComments);
            }
        }
    }

    void notifyPostsCleanup() {
        synchronized (observers) {
            for (PostsDb.Observer observer : observers) {
                observer.onPostsCleanup();
            }
        }
    }
}
