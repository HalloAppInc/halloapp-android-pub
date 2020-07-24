package com.halloapp.content;

import androidx.annotation.NonNull;

import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class ContentDbObservers {

    private final Set<ContentDb.Observer> observers = new HashSet<>();

    void addObserver(@NonNull ContentDb.Observer observer) {
        synchronized (observers) {
            observers.add(observer);
        }
    }

    void removeObserver(@NonNull ContentDb.Observer observer) {
        synchronized (observers) {
            observers.remove(observer);
        }
    }

    void notifyPostAdded(@NonNull Post post) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onPostAdded(post);
            }
        }
    }

    void notifyPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onPostUpdated(senderUserId, postId);
            }
        }
    }

    void notifyPostRetracted(@NonNull UserId senderUserId, @NonNull String postId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onPostRetracted(senderUserId, postId);
            }
        }
    }

    void notifyIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onIncomingPostSeen(senderUserId, postId);
            }
        }
    }

    void notifyOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onOutgoingPostSeen(seenByUserId, postId);
            }
        }
    }

    void notifyCommentAdded(@NonNull Comment comment) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onCommentAdded(comment);
            }
        }
    }

    void notifyCommentUpdated(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onCommentUpdated(postSenderUserId, postId, commentSenderUserId, commentId);
            }
        }
    }

    void notifyCommentRetracted(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onCommentRetracted(postSenderUserId, postId, commentSenderUserId, commentId);
            }
        }
    }

    void notifyCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onCommentsSeen(postSenderUserId, postId);
            }
        }
    }

    void notifyGroupChatAdded(@NonNull GroupId groupId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onGroupChatAdded(groupId);
            }
        }
    }

    void notifyGroupMetadataChanged(@NonNull GroupId groupId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onGroupMetadataChanged(groupId);
            }
        }
    }

    void notifyGroupMembersChanged(@NonNull GroupId groupId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onGroupMembersChanged(groupId);
            }
        }
    }

    void notifyMessageAdded(@NonNull Message message) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onMessageAdded(message);
            }
        }
    }

    void notifyMessageUpdated(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onMessageUpdated(chatId, senderUserId,  messageId);
            }
        }
    }

    void notifyOutgoingMessageDelivered(@NonNull String chatId, @NonNull UserId seenByUserId, @NonNull String messageId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onOutgoingMessageDelivered(chatId, seenByUserId, messageId);
            }
        }
    }

    void notifyOutgoingMessageSeen(@NonNull String chatId, @NonNull UserId seenByUserId, @NonNull String messageId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onOutgoingMessageSeen(chatId, seenByUserId, messageId);
            }
        }
    }

    void notifyMessageRetracted(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onMessageRetracted(chatId, senderUserId, messageId);
            }
        }
    }

    void notifyChatSeen(@NonNull String chatId, @NonNull Collection<SeenReceipt> seenReceipts) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onChatSeen(chatId, seenReceipts);
            }
        }
    }

    void notifyChatDeleted(@NonNull String chatId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onChatDeleted(chatId);
            }
        }
    }

    void notifyFeedHistoryAdded(@NonNull Collection<Post> historyPosts, @NonNull Collection<Comment> historyComments) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onFeedHistoryAdded(historyPosts, historyComments);
            }
        }
    }

    void notifyFeedCleanup() {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onFeedCleanup();
            }
        }
    }

    void notifyDbCreated() {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onDbCreated();
            }
        }
    }
}
