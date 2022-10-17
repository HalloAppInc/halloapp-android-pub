package com.halloapp.ui.share;

import android.app.Application;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Preferences;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Group;
import com.halloapp.content.Post;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.privacy.FeedPrivacyManager;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.Preconditions;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;

public class ShareViewModel extends AndroidViewModel {
    private static final int MAX_FREQUENT_CONTACTS_LENGTH = 5;
    private static final long FREQUENCY_CUTOFF_PERIOD = DateUtils.WEEK_IN_MILLIS;

    private final BgWorkers bgWorkers;
    public final ComputableLiveData<List<ShareDestination>> destinationList;
    public final ComputableLiveData<List<ChatId>> frequentDestinationIdList;
    public final MutableLiveData<List<ShareDestination>> selectionList =  new MutableLiveData<>(new ArrayList<>());
    public final ComputableLiveData<FeedPrivacy> feedPrivacyLiveData;
    public final MutableLiveData<Boolean> shouldForceCompactShare = new MutableLiveData<>(false);

    private final ContentDb contentDb;
    private final ContactsDb contactsDb;

    private final MutableLiveData<String> selectedFeedTargetLiveData = new MutableLiveData<>(PrivacyList.Type.ALL);

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
        public void onIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId, @Nullable GroupId groupId) {
            destinationList.invalidate();
        }

        @Override
        public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId) {
            destinationList.invalidate();
        }

        public void onGroupFeedAdded(@NonNull GroupId groupId) {
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

    public ShareViewModel(@NonNull Application application, boolean chatsOnly) {
        super(application);

        contactsDb = ContactsDb.getInstance();
        contentDb = ContentDb.getInstance();
        bgWorkers = BgWorkers.getInstance();
        final Preferences preferences = Preferences.getInstance();

        destinationList = new ComputableLiveData<List<ShareDestination>>() {
            @Override
            protected List<ShareDestination> compute() {
                List<Chat> chats = contentDb.getOneToOneChats();
                List<Contact> contacts = contactsDb.getUsers();
                Map<UserId, Chat> contactChatMap = new HashMap<>();

                for (Chat chat : chats) {
                    Contact contact = contactsDb.getContact((UserId) chat.chatId);
                    contactChatMap.put(contact.userId, chat);
                }

                Collator collator = Collator.getInstance(Locale.getDefault());
                Collections.sort(contacts, (o1, o2) -> {
                    Chat chat1 = contactChatMap.get(o1.userId);
                    Chat chat2 = contactChatMap.get(o2.userId);

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

                List<Group> groups = chatsOnly ? new ArrayList<>() : contentDb.getActiveGroups();
                ArrayList<ShareDestination> destinations = new ArrayList<>(groups.size() + contacts.size() + 1);

                if (!chatsOnly) {
                    if (PrivacyList.Type.ONLY.equals(selectedFeedTargetLiveData.getValue())) {
                        destinations.add(ShareDestination.myFavorites());
                    } else {
                        ShareDestination shareDestination = ShareDestination.myContacts();
                        shareDestination.size = contactsDb.getUsers().size();
                        destinations.add(shareDestination);
                    }

                    for (Group group : groups) {
                        destinations.add(ShareDestination.fromGroup(group));
                    }
                }

                for (Contact contact : contacts) {
                    destinations.add(ShareDestination.fromContact(contact));
                }

                return destinations;
            }
        };

        frequentDestinationIdList = new ComputableLiveData<List<ChatId>>() {
            @Override
            protected List<ChatId> compute() {
                final Map<ChatId, Integer> contactFrequencyMap = contentDb.computeContactFrequency(System.currentTimeMillis() - FREQUENCY_CUTOFF_PERIOD);

                final PriorityQueue<Map.Entry<ChatId, Integer>> contactFrequencyHeap = new PriorityQueue<>(MAX_FREQUENT_CONTACTS_LENGTH + 1, (entry1, entry2) -> entry1.getValue() - entry2.getValue());
                for (Map.Entry<ChatId, Integer> entry : contactFrequencyMap.entrySet()) {
                    contactFrequencyHeap.add(entry);
                    if (contactFrequencyHeap.size() > MAX_FREQUENT_CONTACTS_LENGTH) {
                        contactFrequencyHeap.poll();
                    }
                }
                final List<ChatId> destinationIdList = new ArrayList<>();
                while (!contactFrequencyHeap.isEmpty()) {
                    destinationIdList.add(Preconditions.checkNotNull(contactFrequencyHeap.poll()).getKey());
                }
                Collections.reverse(destinationIdList);
                return destinationIdList;
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

        bgWorkers.execute(() -> shouldForceCompactShare.postValue(preferences.getForceCompactShare()));
    }

    public void invalidate() {
        destinationList.invalidate();
        feedPrivacyLiveData.invalidate();
    }

    public void setSelectedFeedTarget(@PrivacyList.Type String target) {
        selectedFeedTargetLiveData.setValue(target);
        destinationList.invalidate();
    }

    public LiveData<String > getFeedTarget() {
        return selectedFeedTargetLiveData;
    }

    public void updateSelectionList(List<ShareDestination> selectedDestinations) {
        List<ShareDestination> selection = selectionList.getValue();

        if (selection == null) {
            return;
        }
        selection = new ArrayList<>(selectedDestinations);

        selectionList.setValue(selection);
    }

    public void toggleSelection(ShareDestination destination) {
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

    public void selectGroupById(@NonNull GroupId groupId) {
        bgWorkers.execute(() -> {
            final Group group = contentDb.getGroup(groupId);
            if (group != null) {
                final ShareDestination groupDest = ShareDestination.fromGroup(group);
                final List<ShareDestination> selection = selectionList.getValue();
                if (selection != null && !selection.contains(groupDest)) {
                    selection.add(groupDest);
                    selectionList.postValue(selection);
                }
            }
        });
    }

    public void selectMyContacts() {
        final List<ShareDestination> selection = selectionList.getValue();
        final ShareDestination myContactsDest = ShareDestination.myContacts();

        if (selection != null && !selection.contains(myContactsDest)) {
            selection.add(myContactsDest);
            selectionList.setValue(selection);
        }
    }

    public void toggleHomeSelection(@PrivacyList.Type String target) {
        List<ShareDestination> selection = selectionList.getValue();

        if (selection == null) {
            return;
        }
        selection = new ArrayList<>(selection);

        ShareDestination favorites = ShareDestination.myFavorites();
        ShareDestination myContacts = ShareDestination.myContacts();
        if (PrivacyList.Type.ONLY.equals(target)) {
            selection.remove(myContacts);
            if (selection.contains(favorites)) {
                selection.remove(favorites);
            } else {
                selection.add(favorites);
            }
        } else {
            selection.remove(favorites);
            if (selection.contains(myContacts)) {
                selection.remove(myContacts);
            } else {
                selection.add(myContacts);
            }
        }
        selectionList.setValue(selection);
    }

    void selectDestination(ShareDestination destination) {
        List<ShareDestination> selection = selectionList.getValue();

        if (selection == null) {
            return;
        }
        selection = new ArrayList<>(selection);

        if (!selection.contains(destination)) {
            selection.add(destination);
            selectionList.setValue(selection);
        }
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

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final boolean chatsOnly;

        public Factory(@NonNull Application application, boolean chatOnly) {
            this.application = application;
            this.chatsOnly = chatOnly;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ShareViewModel.class)) {
                //noinspection unchecked
                return (T) new ShareViewModel(application, chatsOnly);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
