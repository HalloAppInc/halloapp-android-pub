package com.halloapp.content;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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

    void notifyPostRetracted(@NonNull Post post) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onPostRetracted(post);
            }
        }
    }
    void notifyArchivedPostRemoved(@NonNull Post post) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onArchivedPostRemoved(post);
            }
        }
    }

    void notifyIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId, @Nullable GroupId parentGroup) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onIncomingPostSeen(senderUserId, postId, parentGroup);
            }
        }
    }

    void notifyIncomingMomentScreenshotted(@NonNull UserId senderUserId, @NonNull String postId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onIncomingMomentScreenshot(senderUserId, postId);
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

    void notifyLocalPostSeen(@NonNull String postId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onLocalPostSeen(postId);
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

    void notifyCommentUpdated(@NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onCommentUpdated(postId, commentSenderUserId, commentId);
            }
        }
    }

    void notifyCommentRetracted(@NonNull Comment comment) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onCommentRetracted(comment);
            }
        }
    }

    void notifyCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId, @Nullable GroupId parentGroup) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onCommentsSeen(postSenderUserId, postId, parentGroup);
            }
        }
    }

    void notifyGroupFeedAdded(@NonNull GroupId groupId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onGroupFeedAdded(groupId);
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

    void notifyGroupBackgroundChanged(@NonNull GroupId groupId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onGroupBackgroundChanged(groupId);
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

    void notifyGroupAdminsChanged(@NonNull GroupId groupId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onGroupAdminsChanged(groupId);
            }
        }
    }

    void notifyReactionAdded(@NonNull Reaction reaction, @NonNull ContentItem contentItem) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onReactionAdded(reaction, contentItem);
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

    void notifyMessageUpdated(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onMessageUpdated(chatId, senderUserId,  messageId);
            }
        }
    }

    void notifyIncomingMessagePlayed(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onIncomingMessagePlayed(chatId, senderUserId,  messageId);
            }
        }
    }

    void notifyOutgoingMessageDelivered(@NonNull ChatId chatId, @NonNull UserId seenByUserId, @NonNull String messageId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onOutgoingMessageDelivered(chatId, seenByUserId, messageId);
            }
        }
    }

    void notifyOutgoingMessageSeen(@NonNull ChatId chatId, @NonNull UserId seenByUserId, @NonNull String messageId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onOutgoingMessageSeen(chatId, seenByUserId, messageId);
            }
        }
    }

    void notifyMessageRetracted(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onMessageRetracted(chatId, senderUserId, messageId);
            }
        }
    }

    void notifyMessageDeleted(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onMessageDeleted(chatId, senderUserId, messageId);
            }
        }
    }

    void notifyMediaPercentTransferred(@NonNull ContentItem contentItem, @NonNull Media media, int percent) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onMediaPercentTransferred(contentItem, media, percent);
            }
        }
    }

    void notifySuggestedGalleryItemsAdded(@NonNull List<Long> suggestedGalleryItems) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onSuggestedGalleryItemsAdded(suggestedGalleryItems);
            }
        }
    }

    void notifyChatSeen(@NonNull ChatId chatId, @NonNull Collection<SeenReceipt> seenReceipts) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onChatSeen(chatId, seenReceipts);
            }
        }
    }

    void notifyGroupSeen(@NonNull GroupId groupId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onGroupSeen(groupId);
            }
        }
    }

    void notifyGroupDeleted(@NonNull GroupId groupId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onGroupDeleted(groupId);
            }
        }
    }

    void notifyChatDeleted(@NonNull ChatId chatId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onChatDeleted(chatId);
            }
        }
    }

    void notifyOutgoingMomentScreenshot(@NonNull UserId seenByUserId, @NonNull String postId) {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onOutgoingMomentScreenshot(seenByUserId, postId);
            }
        }
    }

    void notifyPostsExpired() {
        synchronized (observers) {
            for (ContentDb.Observer observer : observers) {
                observer.onPostsExpired();
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
