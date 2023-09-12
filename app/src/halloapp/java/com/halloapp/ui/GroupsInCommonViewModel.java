package com.halloapp.ui;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Group;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
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

    final ComputableLiveData<List<Group>> groupsList;

    private GroupsInCommonViewModel(@NonNull UserId userId) {
        this.userId = userId;

        groupsList = new ComputableLiveData<List<Group>>() {
            @SuppressLint("RestrictedApi")
            @Override
            protected List<Group> compute() {
                final HashSet<GroupId> groupIds = new HashSet<>(ContentDb.getInstance().getGroupsInCommon(userId));
                final List<Group> groups = ContentDb.getInstance().getGroups();
                ListIterator<Group> groupsIterator = groups.listIterator();
                while (groupsIterator.hasNext()) {
                    Group group = groupsIterator.next();
                    if (group == null || !groupIds.contains(group.groupId)) {
                        groupsIterator.remove();
                    }
                }
                final Collator collator = Collator.getInstance(Locale.getDefault());
                Collections.sort(groups, (obj1, obj2) -> collator.compare(obj1.name, obj2.name));
                return groups;
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
