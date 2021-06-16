package com.halloapp.ui.chats;

import android.app.Application;
import android.os.Parcelable;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.Preferences;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Message;
import com.halloapp.content.MessageLoader;
import com.halloapp.content.Post;
import com.halloapp.content.SeenReceipt;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.Preconditions;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
        @Override
        public void onContactsChanged() {
            chatsList.invalidate();
        }
    };

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        @Override
        public void onPostAdded(@NonNull Post post) {
            if (post.getParentGroup() != null) {
                invalidateChats();
            }
        }

        @Override
        public void onIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId) {
            invalidateChats();
        }

        @Override
        public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId) {
            invalidateChats();
        }

        public void onMessageAdded(@NonNull Message message) {
            invalidateChats();
        }

        public void onMessageRetracted(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
            invalidateMessage(chatId, senderUserId, messageId);
        }

        public void onMessageDeleted(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
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

        contentDb = ContentDb.getInstance();
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

                final List<Chat> chats = ContentDb.getInstance().getChats(false);
                Map<ChatId, Chat> chatsMap = new HashMap<>();
                for (Chat chat : chats) {
                    chatsMap.put(chat.chatId, chat);
                }
                final List<Contact> contacts = ContactsDb.getInstance().getUsers();
                final Collator collator = java.text.Collator.getInstance(Locale.getDefault());
                Collections.sort(contacts, (obj1, obj2) -> collator.compare(obj1.getDisplayName(), obj2.getDisplayName()));
                final List<Chat> contactChats = new ArrayList<>();
                for (Contact contact : contacts) {
                    if (contact.userId == null) {
                        continue;
                    }
                    Chat chat = chatsMap.get(contact.userId);
                    if (chat == null && !contact.hideChat) {
                        chat = new Chat(-1, contact.userId, contact.connectionTime, contact.newConnection ? Chat.MARKED_UNSEEN : 0, -1L, -1L, contact.getDisplayName(), false, null, null, true, 0);
                        if (contact.connectionTime > 0) {
                            chats.add(chat);
                        } else {
                            contactChats.add(chat);
                        }
                    }
                }
                for (Chat chat : chats) {
                    if (TextUtils.isEmpty(chat.name) && chat.chatId instanceof UserId) {
                        Contact contact = contactsDb.getContact((UserId)chat.chatId);
                        String phone = TextUtils.isEmpty(contact.addressBookName) ? contactsDb.readPhone(Preconditions.checkNotNull(contact.userId)) : null;
                        String normalizedPhone = phone == null ? null : PhoneNumberUtils.formatNumber("+" + phone, null);
                        chat.name = TextUtils.isEmpty(contact.addressBookName) ? normalizedPhone : contact.getDisplayName();
                    }
                }
                Collections.sort(chats, (obj1, obj2) -> {
                    if (obj1.timestamp == obj2.timestamp) {
                        return collator.compare(obj1.name, obj2.name);
                    }
                    return obj1.timestamp < obj2.timestamp ? 1 : -1;
                });
                chats.addAll(contactChats);
                return chats;
            }
        };

        messageUpdated = new MutableLiveData<>(false);
    }

    public void deleteChats(@NonNull Collection<ChatId> chatIds) {
        final HashSet<ChatId> deleteIds = new HashSet<>(chatIds);
        bgWorkers.execute(()-> {
            for (ChatId chatId : deleteIds) {
                contentDb.deleteChat(chatId);
                if (chatId instanceof UserId) {
                    contactsDb.hideEmptyChat((UserId) chatId);
                }
            }
        });
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
