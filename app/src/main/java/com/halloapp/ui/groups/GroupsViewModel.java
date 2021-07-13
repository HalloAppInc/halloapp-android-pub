package com.halloapp.ui.groups;

import android.app.Application;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.Preferences;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.DelayedProgressLiveData;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.groups.GroupsApi;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;


public class GroupsViewModel extends AndroidViewModel {

        final ComputableLiveData<List<Chat>> groupsList;
        final MutableLiveData<Boolean> groupPostUpdated;

        private final BgWorkers bgWorkers;
        private final ContentDb contentDb;
        private final GroupsApi groupsApi;
        private final ContactsDb contactsDb;
        private final Preferences preferences;
        final GroupPostLoader groupPostLoader;

        private Parcelable savedScrollState;

        private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
            @Override
            public void onContactsChanged() {
                groupsList.invalidate();
            }
        };

        private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

            @Override
            public void onPostAdded(@NonNull Post post) {
                if (post.getParentGroup() != null) {
                    groupPostLoader.removeFromCache(post.getParentGroup());
                    invalidateGroups();
                }
            }

            @Override
            public void onIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId) {
                invalidateGroups();
            }

            @Override
            public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId) {
                invalidateGroups();
            }

            public void onGroupChatAdded(@NonNull GroupId groupId) {
                invalidateGroups();
            }

            @Override
            public void onGroupMetadataChanged(@NonNull GroupId groupId) {
                invalidateGroups();
            }

            public void onChatDeleted(@NonNull ChatId chatId) {
                if (chatId instanceof GroupId) {
                    invalidateGroups();
                }
            }

            private void invalidateGroups() {
                groupsList.invalidate();
            }
        };

        public GroupsViewModel(@NonNull Application application) {
            super(application);

            bgWorkers = BgWorkers.getInstance();
            contactsDb = ContactsDb.getInstance();
            contactsDb.addObserver(contactsObserver);
            groupsApi = GroupsApi.getInstance();

            contentDb = ContentDb.getInstance();
            contentDb.addObserver(contentObserver);

            preferences = Preferences.getInstance();

            groupPostLoader = new GroupPostLoader();
            groupsList = new ComputableLiveData<List<Chat>>() {
                @Override
                protected List<Chat> compute() {

                    final List<Chat> chats = ContentDb.getInstance().getGroups();
                    final Collator collator = Collator.getInstance(Locale.getDefault());
                    final HashMap<ChatId, String> postsNames = new HashMap<>();
                    for (Chat chat : chats) {
                        if (chat.chatId instanceof GroupId) {
                            String postName = chat.name;
                            if (postName != null) {
                                postsNames.put(chat.chatId, postName);
                            }
                        }
                    }
                    Collections.sort(chats, (obj1, obj2) -> {
                        return collator.compare(obj1.name, obj2.name);
                    });
                    return chats;
                }
            };

            groupPostUpdated = new MutableLiveData<>(false);
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
            groupPostLoader.destroy();
        }
    }
