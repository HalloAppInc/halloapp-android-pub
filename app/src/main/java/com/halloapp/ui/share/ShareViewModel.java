package com.halloapp.ui.share;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.privacy.FeedPrivacyManager;
import com.halloapp.util.ComputableLiveData;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ShareViewModel extends AndroidViewModel {
    final ComputableLiveData<List<ShareDestination>> destinationList;
    final MutableLiveData<List<ShareDestination>> selectionList =  new MutableLiveData<>(new ArrayList<>());
    final ComputableLiveData<FeedPrivacy> feedPrivacyLiveData;

    private final ContentDb contentDb;
    private final ContactsDb contactsDb;

    private final FeedPrivacyManager feedPrivacyManager = FeedPrivacyManager.getInstance();

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
        @Override
        public void onContactsChanged() {
            destinationList.invalidate();
        }
    };

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        @Override
        public void onPostAdded(@NonNull Post post) {
            if (post.getParentGroup() != null) {
                destinationList.invalidate();
            }
        }

        @Override
        public void onLocalPostSeen(@NonNull String postId) {
            destinationList.invalidate();
        }

        @Override
        public void onIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId) {
            destinationList.invalidate();
        }

        @Override
        public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId) {
            destinationList.invalidate();
        }

        public void onGroupChatAdded(@NonNull GroupId groupId) {
            destinationList.invalidate();
        }

        @Override
        public void onGroupMetadataChanged(@NonNull GroupId groupId) {
            destinationList.invalidate();
        }

        public void onChatDeleted(@NonNull ChatId chatId) {
            if (chatId instanceof GroupId) {
                destinationList.invalidate();
            }
        }
    };

    public ShareViewModel(@NonNull Application application) {
        super(application);

        contactsDb = ContactsDb.getInstance();
        contentDb = ContentDb.getInstance();

        destinationList = new ComputableLiveData<List<ShareDestination>>() {
            @Override
            protected List<ShareDestination> compute() {
                List<Chat> groups = contentDb.getActiveGroups();
                List<Contact> contacts = contactsDb.getUsers();
                ArrayList<ShareDestination> destinations = new ArrayList<>(groups.size() + contacts.size() + 1);

                destinations.add(ShareDestination.feed());

                for (Chat group : groups) {
                    destinations.add(ShareDestination.fromGroup(group));
                }

                for (Contact contact : sort(contacts)) {
                    destinations.add(ShareDestination.fromContact(contact));
                }

                return destinations;
            }
        };

        feedPrivacyLiveData = new ComputableLiveData<FeedPrivacy>() {
            @Override
            protected FeedPrivacy compute() {
                return feedPrivacyManager.getFeedPrivacy();
            }
        };

        feedPrivacyManager.addObserver(feedPrivacyLiveData::invalidate);
        contactsDb.addObserver(contactsObserver);
        contentDb.addObserver(contentObserver);
    }

    private List<Contact> sort(List<Contact> contacts) {
        List<Chat> chats = contentDb.getChats(false);
        Map<ChatId, Chat> chatsMap = new HashMap<>();

        for (Chat chat : chats) {
            chatsMap.put(chat.chatId, chat);
        }

        Collator collator = Collator.getInstance(Locale.getDefault());
        Collections.sort(contacts, (o1, o2) -> {
            Chat chat1 = chatsMap.get(o1.userId);
            Chat chat2 = chatsMap.get(o2.userId);

            long t1 = chat1 != null ? chat1.timestamp : o1.connectionTime;
            long t2 = chat2 != null ? chat2.timestamp : o2.connectionTime;

            if (t1 == t2) {
                boolean alpha1 = Character.isAlphabetic(o1.getDisplayName().codePointAt(0));
                boolean alpha2 = Character.isAlphabetic(o2.getDisplayName().codePointAt(0));
                if (alpha1 == alpha2) {
                    return collator.compare(o1.getDisplayName(), o2.getDisplayName());
                } else {
                    return alpha1 ? -1 : 1;
                }
            } else {
                return t1 < t2 ? 1 : -1;
            }
        });

        return contacts;
    }

    public void updateSelectionList(List<ShareDestination> selectedDestinations) {
        List<ShareDestination> selection = selectionList.getValue();

        if (selection == null) {
            return;
        }
        selection = new ArrayList<>(selectedDestinations);

        selectionList.setValue(selection);
    }

    void toggleSelection(ShareDestination destination) {
        List<ShareDestination> selection = selectionList.getValue();

        if (selection == null) {
            return;
        }
        selection = new ArrayList<>(selection);

        if (selection.contains(destination)) {
            selection.remove(destination);
        } else {
            selection.add(destination);
        }

        selectionList.setValue(selection);
    }

    boolean isSelected(ShareDestination destination) {
        List<ShareDestination> selection = selectionList.getValue();
        return selection != null && selection.contains(destination);
    }

    @Override
    protected void onCleared() {
        contactsDb.removeObserver(contactsObserver);
        contentDb.removeObserver(contentObserver);
        feedPrivacyManager.removeObserver(feedPrivacyLiveData::invalidate);
    }
}
