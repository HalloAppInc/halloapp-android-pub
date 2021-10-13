package com.halloapp.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Outline;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.content.Chat;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.groups.CreateGroupActivity;
import com.halloapp.ui.groups.GroupCreationPickerActivity;
import com.halloapp.ui.privacy.FeedPrivacyActivity;
import com.halloapp.util.FilterUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.util.ArrayList;
import java.util.List;

public class SharePrivacyActivity extends HalloActivity {

    public static final String RESULT_GROUP_ID = "selected_group_id";

    private static final String EXTRA_CURRENT_SELECTION = "current_selection";
    private static final int REQUEST_CREATE_GROUP = 1;

    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();

    private ShareItemAdapter adapter;

    private SharePrivacyViewModel viewModel;

    private MenuItem searchMenuItem;

    public static Intent openPostPrivacy(@NonNull Context context, @Nullable GroupId groupId) {
        Intent i = new Intent(context, SharePrivacyActivity.class);
        i.putExtra(EXTRA_CURRENT_SELECTION, groupId);
        return i;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CREATE_GROUP:
                if (resultCode == RESULT_OK) {
                    if (data == null) {
                        break;
                    }
                    GroupId groupId = data.getParcelableExtra(CreateGroupActivity.RESULT_GROUP_ID);
                    if (groupId != null) {
                        onSelectChat(groupId);
                    }
                }
                break;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_share_with);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        View fab = findViewById(R.id.create_group_fab);
        fab.setOnClickListener(v -> {
            startActivityForResult(GroupCreationPickerActivity.newIntent(v.getContext(), null, false), REQUEST_CREATE_GROUP);
        });

        RecyclerView shareListRv = findViewById(R.id.share_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        shareListRv.setLayoutManager(layoutManager);

        adapter = new ShareItemAdapter();
        adapter.setSelectedId(getIntent().getParcelableExtra(EXTRA_CURRENT_SELECTION));
        shareListRv.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(SharePrivacyViewModel.class);
        viewModel.getGroupList().observe(this, adapter::setGroupsList);
        viewModel.getFeedPrivacy().observe(this, adapter::setFeedPrivacy);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_list_menu, menu);
        searchMenuItem = menu.findItem(R.id.menu_search);
        final MenuItem closeMenuItem = menu.findItem(R.id.menu_clear);
        final SearchView searchView = (SearchView) searchMenuItem.getActionView();

        closeMenuItem.setVisible(false);
        ImageView closeBtn = searchView.findViewById(R.id.search_close_btn);
        closeBtn.setEnabled(false);
        closeBtn.setImageDrawable(null);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String text) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(final String text) {
                adapter.getFilter().filter(text);
                closeMenuItem.setVisible(!TextUtils.isEmpty(text));
                return false;
            }
        });
        closeMenuItem.setOnMenuItemClickListener(item -> {
            searchView.setQuery("", false);
            return true;
        });
        return true;
    }

    private class GroupsFilter extends FilterUtils.ItemFilter<Chat> {

        GroupsFilter(@NonNull List<Chat> chats) {
            super(chats);
        }

        @Override
        protected String itemToString(Chat chat) {
            return chat.name;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            final List<Chat> filteredContacts = (List<Chat>) results.values;
            adapter.setFilteredGroups(filteredContacts, constraint);
        }
    }

    private static final int TYPE_HOME = 1;
    private static final int TYPE_GROUP = 2;
    private static final int TYPE_CREATE_GROUP = 3;

    private class ShareItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

        private List<Chat> filteredGroupsList;
        private CharSequence filterText;
        private List<Chat> groupsList;
        private FeedPrivacy feedPrivacy;
        private GroupId selectedId;

        public void setGroupsList(@Nullable List<Chat> groups) {
            this.groupsList = groups;
            if (groups == null) {
                filteredGroupsList = null;
            } else {
                this.filteredGroupsList = new ArrayList<>(groupsList);
                getFilter().filter(filterText);
            }
            notifyDataSetChanged();
        }

        public void setFeedPrivacy(@Nullable FeedPrivacy feedPrivacy) {
            this.feedPrivacy = feedPrivacy;
            notifyDataSetChanged();
        }

        void setFilteredGroups(@NonNull List<Chat> contacts, CharSequence filterText) {
            this.filteredGroupsList = contacts;
            this.filterText = filterText;
            notifyDataSetChanged();
        }

        @Override
        public Filter getFilter() {
            return new GroupsFilter(groupsList);
        }

        public void setSelectedId(@Nullable GroupId groupId) {
            this.selectedId = groupId;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_HOME) {
                return new HomeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.share_list_header, parent, false));
            } else if (viewType == TYPE_GROUP) {
                return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.share_list_item, parent, false));
            } else {
                return new CreateGroupViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.share_list_create_group, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof HomeViewHolder) {
                ((HomeViewHolder)holder).bind(feedPrivacy, selectedId == null);
            } else if (holder instanceof ItemViewHolder) {
                ItemViewHolder viewHolder = (ItemViewHolder) holder;
                int type = getItemViewType(position);
                if (type == TYPE_CREATE_GROUP) {
                    viewHolder.bindCreateGroup();
                } else {
                    Chat chat = filteredGroupsList.get(position - 1);
                    viewHolder.bindChat(chat, chat.chatId.equals(selectedId));
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_HOME;
            } else if (position == getItemCount() - 1) {
                return TYPE_CREATE_GROUP;
            }
            return TYPE_GROUP;
        }

        @Override
        public int getItemCount() {
            return 2 + getFilteredGroupsCount();
        }

        private int getFilteredGroupsCount() {
            return filteredGroupsList == null ? 0 : filteredGroupsList.size();
        }
    }

    private void onSelectChat(@Nullable ChatId chatId) {
        Intent data = new Intent();
        data.putExtra(RESULT_GROUP_ID, chatId);

        setResult(RESULT_OK, data);
        finish();
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private ImageView avatarIconView;
        private TextView nameView;
        private View selectionView;

        private Chat chat;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            avatarIconView = itemView.findViewById(R.id.group_avatar);
            nameView = itemView.findViewById(R.id.group_name);
            selectionView = itemView.findViewById(R.id.selection_indicator);
            selectionView.setSelected(true);
            selectionView.setVisibility(View.GONE);

            avatarIconView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), itemView.getContext().getResources().getDimension(R.dimen.share_privacy_avatar_corner_radius));
                }
            });
            avatarIconView.setClipToOutline(true);

            itemView.setOnClickListener(v -> {
                if (chat != null) {
                    onSelectChat(chat.chatId);
                }
            });
        }

        public void bindCreateGroup() {

        }

        public void bindChat(@NonNull Chat chat, boolean selected) {
            nameView.setText(chat.name);
            avatarLoader.load(avatarIconView, chat.chatId);
            this.chat = chat;
            selectionView.setVisibility(selected ? View.VISIBLE : View.GONE);
        }
    }

    private class CreateGroupViewHolder extends RecyclerView.ViewHolder {
        private ImageView createIcon;

        public CreateGroupViewHolder(@NonNull View itemView) {
            super(itemView);

            createIcon = itemView.findViewById(R.id.create_group_icon);
            createIcon.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), itemView.getContext().getResources().getDimension(R.dimen.share_privacy_avatar_corner_radius));
                }
            });
            createIcon.setClipToOutline(true);

            itemView.setOnClickListener(v -> {
                startActivityForResult(GroupCreationPickerActivity.newIntent(v.getContext(), null, false), REQUEST_CREATE_GROUP);
            });
        }
    }

    private class HomeViewHolder extends RecyclerView.ViewHolder {

        private ImageView homeIconView;
        private TextView homePrivacySetting;
        private View selectionView;
        private View changePrivacySetting;

        public HomeViewHolder(@NonNull View itemView) {
            super(itemView);

            homeIconView = itemView.findViewById(R.id.home_icon);
            homePrivacySetting = itemView.findViewById(R.id.home_privacy_setting);
            selectionView = itemView.findViewById(R.id.selection_indicator);
            changePrivacySetting = itemView.findViewById(R.id.change_privacy_setting);
            selectionView.setSelected(true);
            selectionView.setVisibility(View.GONE);

            homeIconView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), itemView.getContext().getResources().getDimension(R.dimen.share_privacy_avatar_corner_radius));
                }
            });
            homeIconView.setClipToOutline(true);

            itemView.setOnClickListener(v -> {
                onSelectChat(null);
            });

            changePrivacySetting.setOnClickListener(v -> {
                startActivity(FeedPrivacyActivity.editFeedPrivacy(v.getContext()));
            });
        }

        public void bind(FeedPrivacy feedPrivacy, boolean selected) {
            selectionView.setVisibility(selected ? View.VISIBLE : View.GONE);
            if (feedPrivacy == null) {
                homePrivacySetting.setText("");
            } else if (PrivacyList.Type.ALL.equals(feedPrivacy.activeList)) {
                homePrivacySetting.setText(R.string.composer_sharing_all_summary);
            } else if (PrivacyList.Type.EXCEPT.equals(feedPrivacy.activeList)) {
                homePrivacySetting.setText(R.string.composer_sharing_except_summary);
            } else if (PrivacyList.Type.ONLY.equals(feedPrivacy.activeList)) {
                final int onlySize = feedPrivacy.onlyList.size();
                homePrivacySetting.setText(getResources().getQuantityString(R.plurals.composer_sharing_only_summary, onlySize, onlySize));
            } else {
                homePrivacySetting.setText("");
            }
        }
    }
}
