package com.halloapp.ui.chats;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.UserId;
import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Message;
import com.halloapp.content.MessageLoader;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.Preconditions;

import java.text.Collator;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ChatsViewModel extends AndroidViewModel {

    final ComputableLiveData<List<Chat>> chatsList;
    final MutableLiveData<Boolean> messageUpdated;

    private final ContentDb contentDb;
    private final ContactsDb contactsDb;
    final MessageLoader messageLoader;

    private final ContactsDb.Observer contactsObserver = new ContactsDb.Observer() {

        @Override
        public void onContactsChanged() {
            chatsList.invalidate();
        }

        @Override
        public void onContactsReset() {
        }
    };

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        public void onMessageAdded(@NonNull Message message) {
            invalidateChats();
        }

        public void onMessageRetracted(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
            invalidateMessage(chatId, senderUserId, messageId);
        }

        public void onMessageUpdated(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
            invalidateMessage(chatId, senderUserId, messageId);
        }

        public void onOutgoingMessageDelivered(@NonNull String chatId, @NonNull UserId recipientUserId, @NonNull String messageId) {
            invalidateMessage(chatId, UserId.ME, messageId);
        }

        public void onOutgoingMessageSeen(@NonNull String chatId, @NonNull UserId seenByUserId, @NonNull String messageId) {
            invalidateMessage(chatId, UserId.ME, messageId);
        }

        public void onChatSeen(@NonNull String chatId) {
            invalidateChats();
        }

        public void onChatDeleted(@NonNull String chatId) {
            invalidateChats();
        }

        private void invalidateChats() {
            chatsList.invalidate();
        }

        private void invalidateMessage(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
            messageLoader.removeFromCache(chatId, senderUserId, messageId);
            messageUpdated.postValue(true);
        }
    };

    public ChatsViewModel(@NonNull Application application) {
        super(application);

        contactsDb = ContactsDb.getInstance(application);
        contactsDb.addObserver(contactsObserver);

        contentDb = ContentDb.getInstance(application);
        contentDb.addObserver(contentObserver);

        messageLoader = new MessageLoader(Preconditions.checkNotNull(application));

        chatsList = new ComputableLiveData<List<Chat>>() {
            @Override
            protected List<Chat> compute() {

                final List<Chat> chats = ContentDb.getInstance(application).getChats();
                Map<String, Chat> chatsMap = new HashMap<>();
                for (Chat chat : chats) {
                    chatsMap.put(chat.chatId, chat);
                }
                final List<Contact> friends = ContactsDb.getInstance(application).getFriends();
                final Collator collator = java.text.Collator.getInstance(Locale.getDefault());
                Collections.sort(friends, (obj1, obj2) -> collator.compare(obj1.getDisplayName(), obj2.getDisplayName()));
                for (Contact friend : friends) {
                    if (friend.userId == null) {
                        continue;
                    }
                    Chat chat = chatsMap.get(friend.userId.rawId());
                    if (chat == null) {
                        chat = new Chat(-1, friend.userId.rawId(), 0, 0, -1L, -1L);
                        chats.add(chat);
                    }
                    chat.name = friend.getDisplayName();
                    chat.info = friend.getInternationalPhone();
                }

                return chats;
            }
        };

        messageUpdated = new MutableLiveData<>(false);
    }

    @Override
    protected void onCleared() {
        contactsDb.removeObserver(contactsObserver);
        contentDb.removeObserver(contentObserver);
        messageLoader.destroy();
    }
}