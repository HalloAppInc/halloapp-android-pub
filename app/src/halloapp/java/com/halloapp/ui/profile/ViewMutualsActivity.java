package com.halloapp.ui.profile;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Group;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.groups.ViewGroupFeedActivity;
import com.halloapp.util.BgWorkers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class ViewMutualsActivity extends HalloActivity {

    private static final String EXTRA_NAME = "name";
    private static final String EXTRA_MUTUAL_FRIENDS_LIST = "mutual_friends_list";
    private static final String EXTRA_MUTUAL_GROUPS_LIST = "mutual_groups_list";

    private ViewMutualsViewModel viewModel;

    public static Intent open(@NonNull Context context, @NonNull String name, @NonNull ArrayList<String> mutualFriends, @NonNull ArrayList<String> mutualGroups) {
        Intent intent = new Intent(context, ViewMutualsActivity.class);
        intent.putExtra(EXTRA_NAME, name);
        intent.putExtra(EXTRA_MUTUAL_FRIENDS_LIST, mutualFriends);
        intent.putExtra(EXTRA_MUTUAL_GROUPS_LIST, mutualGroups);
        return intent;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Type.SECTION_HEADER, Type.FRIEND, Type.GROUP})
    public @interface Type {
        int SECTION_HEADER = 1;
        int FRIEND = 2;
        int GROUP = 3;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String name = getIntent().getStringExtra(EXTRA_NAME);
        ArrayList<String> mutualFriends = getIntent().getStringArrayListExtra(EXTRA_MUTUAL_FRIENDS_LIST);
        ArrayList<String> mutualGroups = getIntent().getStringArrayListExtra(EXTRA_MUTUAL_GROUPS_LIST);

        setContentView(R.layout.activity_view_mutuals);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(getResources().getDimension(R.dimen.action_bar_elevation));
            actionBar.setTitle(name);
        }

        RecyclerView list = findViewById(R.id.list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        MutualsAdapter adapter = new MutualsAdapter();
        list.setLayoutManager(layoutManager);
        list.setAdapter(adapter);

        viewModel = new ViewModelProvider(this, new ViewMutualsViewModel.Factory(getApplication(), ContactsDb.getInstance(), ContentDb.getInstance(), mutualFriends, mutualGroups)).get(ViewMutualsViewModel.class);

        viewModel.getItems().observe(this, adapter::setItems);
    }

    public class MutualsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<Item> items = new ArrayList<>();

        public MutualsAdapter() {}

        public void setItems(@NonNull List<Item> items) {
            this.items = items;
            notifyDataSetChanged();
        }
        
        @Override
        public int getItemViewType(int position) {
            return items.get(position).type;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case Type.SECTION_HEADER:
                    return new SectionHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.mutuals_item_header, parent, false));
                case Type.FRIEND:
                    return new FriendViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.mutuals_item_friend, parent, false));
                case Type.GROUP:
                    return new GroupViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.mutuals_item_group, parent, false));
            }
            throw new IllegalArgumentException("Invalid viewType " + viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof FriendViewHolder) {
                FriendViewHolder friendViewHolder = (FriendViewHolder) holder;
                FriendItem item = (FriendItem) items.get(position);
                friendViewHolder.bindTo(item);
            } else if (holder instanceof GroupViewHolder) {
                GroupViewHolder groupViewHolder = (GroupViewHolder) holder;
                GroupItem item = (GroupItem) items.get(position);
                groupViewHolder.bindTo(item);
            } else if (holder instanceof SectionHeaderViewHolder) {
                SectionHeaderViewHolder sectionHeaderViewHolder = (SectionHeaderViewHolder) holder;
                SectionHeaderItem item = (SectionHeaderItem) items.get(position);
                sectionHeaderViewHolder.bindTo(item);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    public static abstract class ViewHolder<T> extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bindTo(@NonNull T item) {}
    }

    public static class SectionHeaderViewHolder extends ViewHolder<SectionHeaderItem> {

        private final TextView textView;

        public SectionHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text);
        }

        @Override
        public void bindTo(@NonNull SectionHeaderItem item) {
            textView.setText(item.title);
        }
    }

    public class FriendViewHolder extends ViewHolder<FriendItem> {

        private UserId userId;
        private final ImageView avatarView;
        private final TextView nameView;
        private final TextView usernameView;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.avatar);
            nameView = itemView.findViewById(R.id.name);
            usernameView = itemView.findViewById(R.id.username);

            itemView.setOnClickListener(view -> {
                startActivity(ViewProfileActivity.viewProfile(getBaseContext(), userId));
            });
        }

        @Override
        public void bindTo(@NonNull FriendItem item) {
            this.userId = item.userId;
            this.nameView.setText(item.name);
            this.usernameView.setText(item.username);
            AvatarLoader.getInstance().load(avatarView, item.userId, item.avatarId);
        }
    }

    public class GroupViewHolder extends ViewHolder<GroupItem> {

        private GroupId groupUid;
        private final ImageView avatar;
        private final TextView name;
        private final TextView description;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.name);
            description = itemView.findViewById(R.id.username);

            itemView.setOnClickListener(view -> {
                startActivity(ViewGroupFeedActivity.viewFeed(getBaseContext(), groupUid));
            });
        }

        @Override
        public void bindTo(@NonNull GroupItem item) {
            this.groupUid = item.groupUid;
            this.name.setText(item.name);
            this.description.setText(item.description);
            AvatarLoader.getInstance().load(avatar, item.groupUid, item.avatarId);
        }
    }

    public static abstract class Item {

        private final int type;

        public Item(@Type int type) {
            this.type = type;
        }
    }
    
    public static class FriendItem extends Item {

        private final UserId userId;
        private final String name;
        private final String username;
        private final String avatarId;

        public FriendItem(@NonNull UserId userId, @NonNull String name, @Nullable String username, @Nullable String avatarId) {
            super(Type.FRIEND);
            this.userId = userId;
            this.name = name;
            this.username = username;
            this.avatarId = avatarId;
        }
    }

    public static class GroupItem extends Item {

        private final GroupId groupUid;
        private final String name;
        private final String description;
        private final String avatarId;

        public GroupItem(@NonNull GroupId groupUid, @NonNull String name, @NonNull String description, @Nullable String avatarId) {
            super(Type.GROUP);
            this.groupUid = groupUid;
            this.name = name;
            this.description = description;
            this.avatarId = avatarId;
        }
    }

    public static class SectionHeaderItem extends Item {

        private final String title;

        public SectionHeaderItem(@NonNull String title) {
            super(Type.SECTION_HEADER);
            this.title = title;
        }
    }

    public static class ViewMutualsViewModel extends AndroidViewModel {

        private final ContactsDb contactsDb;
        private final ContentDb contentDb;
        private final ArrayList<String> mutualFriends;
        private final ArrayList<String> mutualGroups;
        private final MutableLiveData<List<Item>> items = new MutableLiveData<>();

        @SuppressLint("RestrictedApi")
        public ViewMutualsViewModel(@NonNull Application application, @NonNull ContactsDb contactsDb, @NonNull ContentDb contentDb, @NonNull ArrayList<String> mutualFriends, @NonNull ArrayList<String> mutualGroups) {
            super(application);
            this.contactsDb = contactsDb;
            this.contentDb = contentDb;
            this.mutualFriends = mutualFriends;
            this.mutualGroups = mutualGroups;

            computeItems();
        }

        public MutableLiveData<List<Item>> getItems() {
            return items;
        }

        private void computeItems() {
            BgWorkers.getInstance().execute(() -> {
                List<Item> list = new ArrayList<>();
                List<FriendItem> friends = new ArrayList<>();
                List<GroupItem> groups = new ArrayList<>();

                for (String friendUid : mutualFriends) {
                    if (friendUid == null) {
                        continue;
                    }
                    UserId userId = new UserId(friendUid);
                    Contact contact = contactsDb.getContact(userId);
                    friends.add(new FriendItem(userId, contact.getDisplayName(), contact.getUsername(), contact.avatarId));
                }

                for (String groupUid : mutualGroups) {
                    if (groupUid == null) {
                        continue;
                    }
                    GroupId groupId = new GroupId(groupUid);
                    Group group = contentDb.getGroup(groupId);
                    List<MemberInfo> members = contentDb.getGroupMembers(groupId);
                    if (group != null) {
                        String description = getApplication().getResources().getString(R.string.group_members, members.size());
                        groups.add(new GroupItem(groupId, group.name, description, group.groupAvatarId));
                    }
                }

                if (!friends.isEmpty()) {
                    list.add(new SectionHeaderItem(getApplication().getResources().getString(R.string.num_friends_in_common, friends.size())));
                    list.addAll(friends);
                }
                if (!groups.isEmpty()) {
                    list.add(new SectionHeaderItem(getApplication().getResources().getString(R.string.num_groups_in_common, groups.size())));
                    list.addAll(groups);
                }

                items.postValue(list);
            });
        }

        public static class Factory implements ViewModelProvider.Factory {

            private final Application application;
            private final ContactsDb contactsDb;
            private final ContentDb contentDb;
            private final ArrayList<String> mutualFriends;
            private final ArrayList<String> mutualGroups;

            Factory(@NonNull Application application, @NonNull ContactsDb contactsDb, @NonNull ContentDb contentDb, @Nullable ArrayList<String> mutualFriends, @Nullable ArrayList<String> mutualGroups) {
                this.application = application;
                this.contactsDb = contactsDb;
                this.contentDb = contentDb;
                this.mutualFriends = mutualFriends == null ? new ArrayList<>() : mutualFriends;
                this.mutualGroups = mutualGroups == null ? new ArrayList<>() : mutualGroups;
            }

            @Override
            public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(ViewMutualsViewModel.class)) {
                    //noinspection unchecked
                    return (T) new ViewMutualsViewModel(application, contactsDb, contentDb, mutualFriends, mutualGroups);
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        }
    }
}
