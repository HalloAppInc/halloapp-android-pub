package com.halloapp.ui.chats;

import android.app.Application;
import android.os.Parcelable;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.Me;
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
import com.halloapp.nux.ZeroZoneManager;
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
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

public class ChatsViewModel extends AndroidViewModel {

    final ComputableLiveData<List<Chat>> chatsList;
    final ComputableLiveData<List<Contact>> contactsList;
    final MutableLiveData<Boolean> messageUpdated;
    final MutableLiveData<Boolean> showInviteList;

    private final Me me;
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
            contactsList.invalidate();
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
        public void onIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId, @Nullable GroupId groupId) {
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
            invalidateChats();
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

        me = Me.getInstance();
        bgWorkers = BgWorkers.getInstance();
        contactsDb = ContactsDb.getInstance();
        contactsDb.addObserver(contactsObserver);

        contentDb = ContentDb.getInstance();
        contentDb.addObserver(contentObserver);

        preferences = Preferences.getInstance();

        contactsList = new ComputableLiveData<List<Contact>>() {

            @Override
            protected List<Contact> compute() {
                List<Contact> contacts = Contact.sort(ContactsDb.getInstance().getUniqueContactsWithPhones());

                ListIterator<Contact> iterator = contacts.listIterator();
                String myUserId = me.getUser();
                while(iterator.hasNext()){
                    UserId userId = iterator.next().userId;
                    if (userId != null) {
                        iterator.remove();
                    }
                }
                Collator collator = Collator.getInstance(Locale.getDefault());
                Collections.sort(contacts, (o1, o2) -> {
                    if (o1.userId != null || o2.userId != null) {
                        if (o1.userId == null) {
                            return -1;
                        } else if (o2.userId == null) {
                            return 1;
                        }
                    } else if (o1.numPotentialFriends > 1 && o2.numPotentialFriends > 1) {
                        if (o1.numPotentialFriends != o2.numPotentialFriends) {
                            return (int) o2.numPotentialFriends - (int) o1.numPotentialFriends;
                        }
                    } else if (o1.numPotentialFriends > 1) {
                        return -1;
                    } else if (o2.numPotentialFriends > 1) {
                        return 1;
                    }
                    boolean alpha1 = Character.isAlphabetic(o1.getDisplayName().codePointAt(0));
                    boolean alpha2 = Character.isAlphabetic(o2.getDisplayName().codePointAt(0));
                    if (alpha1 == alpha2) {
                        return collator.compare(o1.getDisplayName(), o2.getDisplayName());
                    } else {
                        return alpha1 ? -1 : 1;
                    }
                });
                return contacts;
            }
        };
        showInviteList = new MutableLiveData<>();
        messageLoader = new MessageLoader(Preconditions.checkNotNull(application));
        chatsList = new ComputableLiveData<List<Chat>>() {
            @Override
            protected List<Chat> compute() {

                final List<Chat> chats = contentDb.getChats();
                Map<ChatId, Chat> chatsMap = new HashMap<>();
                for (Chat chat : chats) {
                    chatsMap.put(chat.chatId, chat);
                }
                final List<Contact> contacts = ContactsDb.getInstance().getPlaceholderChats();
                final Collator collator = java.text.Collator.getInstance(Locale.getDefault());
                Contact.sort(contacts);
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
                        String nonContactDisplayName = phone == null ? contact.getDisplayName() : PhoneNumberUtils.formatNumber("+" + phone, null);
                        chat.name = TextUtils.isEmpty(contact.addressBookName) ? nonContactDisplayName : contact.getDisplayName();
                    }
                }
                Collections.sort(chats, (obj1, obj2) -> {
                    if (obj1.timestamp == obj2.timestamp) {
                        return collator.compare(obj1.name, obj2.name);
                    }
                    return obj1.timestamp < obj2.timestamp ? 1 : -1;
                });
                chats.addAll(contactChats);
                showInviteList.postValue(chats.isEmpty() && preferences.getZeroZoneState() >= ZeroZoneManager.ZeroZoneState.NEEDS_INITIALIZATION);
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

    public void saveScrollState(@Nullable Parcelable savedScrollState) {
        this.savedScrollState = savedScrollState;
    }

    public @Nullable Parcelable getSavedScrollState() {
        return savedScrollState;
    }

    public void markContactInvited(@NonNull Contact contact) {
        bgWorkers.execute(() -> {
            contactsDb.markInvited(contact);
            contactsList.invalidate();
        });
    }

    @Override
    protected void onCleared() {
        contactsDb.removeObserver(contactsObserver);
        contentDb.removeObserver(contentObserver);
        messageLoader.destroy();
    }
}
