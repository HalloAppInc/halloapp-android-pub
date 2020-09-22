package com.halloapp.ui.chats;

import android.app.Application;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.Preferences;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Message;
import com.halloapp.content.MessageLoader;
import com.halloapp.content.SeenReceipt;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.Preconditions;

import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatsViewModel extends AndroidViewModel {

    final ComputableLiveData<Boolean> showMessagesNux;
    final ComputableLiveData<List<Chat>> chatsList;
    final MutableLiveData<Boolean> messageUpdated;

    private final BgWorkers bgWorkers;
    private final ContentDb contentDb;
    private final ContactsDb contactsDb;
    private final Preferences preferences;
    final MessageLoader messageLoader;

    private Parcelable savedScrollState;

    private final ContactsDb.Observer contactsObserver = new ContactsDb.Observer() {

        @Override
        public void onContactsChanged() {
            chatsList.invalidate();
        }

        @Override
        public void onContactsReset() {
        }

        @Override
        public void onNewFriends(@NonNull Collection<UserId> newFriends) {

        }
    };

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        public void onMessageAdded(@NonNull Message message) {
            invalidateChats();
        }

        public void onMessageRetracted(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
            invalidateMessage(chatId, senderUserId, messageId);
        }

        public void onMessageUpdated(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
            invalidateMessage(chatId, senderUserId, messageId);
        }

        public void onGroupChatAdded(@NonNull GroupId groupId) {
            invalidateChats();
        }

        public void onOutgoingMessageDelivered(@NonNull ChatId chatId, @NonNull UserId recipientUserId, @NonNull String messageId) {
            invalidateMessage(chatId, UserId.ME, messageId);
        }

        public void onOutgoingMessageSeen(@NonNull ChatId chatId, @NonNull UserId seenByUserId, @NonNull String messageId) {
            invalidateMessage(chatId, UserId.ME, messageId);
        }

        public void onChatSeen(@NonNull ChatId chatId, @NonNull Collection<SeenReceipt> seenReceipts) {
            invalidateChats();
        }

        public void onChatDeleted(@NonNull ChatId chatId) {
            invalidateChats();
        }

        private void invalidateChats() {
            chatsList.invalidate();
        }

        private void invalidateMessage(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
            messageLoader.removeFromCache(chatId, senderUserId, messageId);
            messageUpdated.postValue(true);
        }
    };

    public ChatsViewModel(@NonNull Application application) {
        super(application);

        bgWorkers = BgWorkers.getInstance();
        contactsDb = ContactsDb.getInstance();
        contactsDb.addObserver(contactsObserver);

        contentDb = ContentDb.getInstance(application);
        contentDb.addObserver(contentObserver);

        preferences = Preferences.getInstance();

        messageLoader = new MessageLoader(Preconditions.checkNotNull(application));
        showMessagesNux = new ComputableLiveData<Boolean>() {
            @Override
            protected Boolean compute() {
                return !preferences.getShowedMessagesNux();
            }
        };
        chatsList = new ComputableLiveData<List<Chat>>() {
            @Override
            protected List<Chat> compute() {

                final List<Chat> chats = ContentDb.getInstance(application).getChats();
                Map<ChatId, Chat> chatsMap = new HashMap<>();
                for (Chat chat : chats) {
                    chatsMap.put(chat.chatId, chat);
                }
                final List<Contact> friends = ContactsDb.getInstance().getFriends();
                final Collator collator = java.text.Collator.getInstance(Locale.getDefault());
                Collections.sort(friends, (obj1, obj2) -> collator.compare(obj1.getDisplayName(), obj2.getDisplayName()));
                for (Contact friend : friends) {
                    if (friend.userId == null) {
                        continue;
                    }
                    Chat chat = chatsMap.get(friend.userId);
                    if (chat == null) {
                        chat = new Chat(-1, friend.userId, 0, 0, -1L, -1L, friend.getDisplayName(), false, null, null, true);
                        chats.add(chat);
                    }
                }
                for (Chat chat : chats) {
                    if (TextUtils.isEmpty(chat.name) && chat.chatId instanceof UserId) {
                        chat.name = ContactsDb.getInstance().getContact((UserId)chat.chatId).getDisplayName();
                    }
                }

                return chats;
            }
        };

        messageUpdated = new MutableLiveData<>(false);
    }

    public void closeNux() {
        bgWorkers.execute(() -> {
            preferences.markMessagesNuxShown();
            showMessagesNux.invalidate();
        });
    }

    public void saveScrollState(@Nullable Parcelable savedScrollState) {
        this.savedScrollState = savedScrollState;
    }

    public @Nullable Parcelable getSavedScrollState() {
        return savedScrollState;
    }

    @Override
    protected void onCleared() {
        contactsDb.removeObserver(contactsObserver);
        contentDb.removeObserver(contentObserver);
        messageLoader.destroy();
    }
}
