package com.halloapp.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.id.ChatId;
import com.halloapp.id.UserId;
import com.halloapp.util.ComputableLiveData;

import java.text.Collator;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

public class GroupsInCommonViewModel extends ViewModel {

    private final UserId userId;

    final ComputableLiveData<List<Chat>> groupsList;

    private GroupsInCommonViewModel(@NonNull UserId userId) {
        this.userId = userId;

        groupsList = new ComputableLiveData<List<Chat>>() {
            @Override
            protected List<Chat> compute() {
                final HashSet<ChatId> groups = new HashSet<>(ContentDb.getInstance().getGroupsInCommon(userId));
                final List<Chat> chats = ContentDb.getInstance().getGroups();
                ListIterator<Chat> chatListIterator = chats.listIterator();
                while (chatListIterator.hasNext()) {
                    Chat chat = chatListIterator.next();
                    if (chat == null || !groups.contains(chat.chatId)) {
                        chatListIterator.remove();
                    }
                }
                final Collator collator = Collator.getInstance(Locale.getDefault());
                Collections.sort(chats, (obj1, obj2) -> collator.compare(obj1.name, obj2.name));
                return chats;
            }
        };
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final UserId userId;

        Factory(@NonNull UserId userId) {
            this.userId = userId;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(GroupsInCommonViewModel.class)) {
                //noinspection unchecked
                return (T) new GroupsInCommonViewModel(userId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

}
