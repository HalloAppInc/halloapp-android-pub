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
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.content.Group;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.permissions.PermissionUtils;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.contacts.EditFavoritesActivity;
import com.halloapp.ui.contacts.ViewMyContactsActivity;
import com.halloapp.util.FilterUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class SharePrivacyActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {

    public static final String RESULT_GROUP_ID = "selected_group_id";
    public static final String RESULT_PRIVACY_TYPE = "privacy_type";

    private static final String EXTRA_CURRENT_SELECTION = "current_selection";
    private static final String EXTRA_SELECTED_PRIVACY_TYPE = "selected_privacy_type";
    private static final String EXTRA_FOR_ONBOARDING = "for_onboarding";

    private static final int REQUEST_CODE_SELECT_ONLY_LIST = 1;

    public static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION_ONLY = 1;
    public static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION_VIEW_MY_CONTACTS = 2;

    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();

    private ShareItemAdapter adapter;

    private SharePrivacyViewModel viewModel;

    private MenuItem searchMenuItem;

    private boolean showShareButton;

    public static Intent openPostPrivacy(@NonNull Context context, @Nullable @PrivacyList.Type String selectedPrivacyType, @Nullable GroupId groupId) {
        Intent i = new Intent(context, SharePrivacyActivity.class);
        i.putExtra(EXTRA_CURRENT_SELECTION, groupId);
        i.putExtra(EXTRA_SELECTED_PRIVACY_TYPE, selectedPrivacyType);
        return i;
    }

    public static Intent selectFirstPostDestination(@NonNull Context context, @Nullable @PrivacyList.Type String selectedPrivacyType, @Nullable GroupId groupId) {
        Intent i = new Intent(context, SharePrivacyActivity.class);
        i.putExtra(EXTRA_CURRENT_SELECTION, groupId);
        i.putExtra(EXTRA_SELECTED_PRIVACY_TYPE, selectedPrivacyType);
        i.putExtra(EXTRA_FOR_ONBOARDING, true);
        return i;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SELECT_ONLY_LIST:
                List<UserId> onlyList = getOnlyList();
                if (onlyList != null && !onlyList.isEmpty()) {
                    onSelectFeed(PrivacyList.Type.ONLY);
                }
                break;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_share_with);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        showShareButton = getIntent().getBooleanExtra(EXTRA_FOR_ONBOARDING, false);

        RecyclerView shareListRv = findViewById(R.id.share_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        shareListRv.setLayoutManager(layoutManager);

        adapter = new ShareItemAdapter();
        adapter.setSelectedId(getIntent().getParcelableExtra(EXTRA_CURRENT_SELECTION));
        shareListRv.setAdapter(adapter);

        if (showShareButton) {
            shareListRv.setPadding(0,0,0,getResources().getDimensionPixelSize(R.dimen.share_onboarding_bottom_padding));
            View shareButton = findViewById(R.id.share_button);
            shareButton.setVisibility(View.VISIBLE);
            shareButton.setOnClickListener(v -> {
                Intent data = new Intent();
                GroupId selectedGroup = adapter.getSelectedGroup();
                data.putExtra(RESULT_GROUP_ID, selectedGroup);
                if (selectedGroup == null) {
                    data.putExtra(RESULT_PRIVACY_TYPE, adapter.getFeedPrivacy().activeList);
                }
                setResult(RESULT_OK, data);
                finish();
            });
        }

        viewModel = new ViewModelProvider(this).get(SharePrivacyViewModel.class);
        viewModel.getGroupList().observe(this, adapter::setGroupsList);
        String feedPrivacyType = getIntent().getStringExtra(EXTRA_SELECTED_PRIVACY_TYPE);
        FeedPrivacy feedPrivacy = new FeedPrivacy(feedPrivacyType, null, null);
        adapter.setFeedPrivacy(feedPrivacy);
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

    private void editOnlyList() {
        startActivityForResult(EditFavoritesActivity.openFavorites(SharePrivacyActivity.this, true), REQUEST_CODE_SELECT_ONLY_LIST);
    }

    private List<UserId> getOnlyList() {
        FeedPrivacy privacy = viewModel.getFeedPrivacy().getValue();
        if (privacy != null) {
            return privacy.onlyList;
        }
        return null;
    }

    private void saveList(@PrivacyList.Type @NonNull String listType, List<UserId> userId) {
        viewModel.savePrivacy(listType, userId).observe(this, done -> {
            if (done != null) {
                if (done) {
                    Toast.makeText(this, R.string.feed_privacy_update_success, Toast.LENGTH_LONG).show();
                    onSelectFeed(listType);
                } else {
                    SnackbarHelper.showWarning(this, R.string.feed_privacy_update_failure);
                }
            }
        });
    }

    private void viewMyContacts() {
        startActivity(ViewMyContactsActivity.viewMyContacts(this));
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_CONTACTS_PERMISSION_ONLY: {
                editOnlyList();
                break;
            }
            case REQUEST_CODE_ASK_CONTACTS_PERMISSION_VIEW_MY_CONTACTS: {

            }
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    private class GroupsFilter extends FilterUtils.ItemFilter<Group> {

        GroupsFilter(@NonNull List<Group> groups) {
            super(groups);
        }

        @Override
        protected String itemToString(Group group) {
            return group.name;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            final List<Group> filteredGroups = (List<Group>) results.values;
            adapter.setFilteredGroups(filteredGroups, constraint);
        }
    }

    private static final int TYPE_HOME = 1;
    private static final int TYPE_GROUP = 2;

    private class ShareItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

        private List<Group> filteredGroupsList;
        private CharSequence filterText;
        private List<Group> groupsList;
        private FeedPrivacy feedPrivacy;
        private GroupId selectedId;

        public void setGroupsList(@Nullable List<Group> groups) {
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

        void setFilteredGroups(@NonNull List<Group> contacts, CharSequence filterText) {
            this.filteredGroupsList = contacts;
            this.filterText = filterText;
            notifyDataSetChanged();
        }

        public FeedPrivacy getFeedPrivacy() {
            return feedPrivacy;
        }

        public GroupId getSelectedGroup() {
            return selectedId;
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
            } else {
                return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.share_list_item, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof HomeViewHolder) {
                ((HomeViewHolder)holder).bind(feedPrivacy, selectedId == null);
            } else if (holder instanceof ItemViewHolder) {
                ItemViewHolder viewHolder = (ItemViewHolder) holder;
                Group group = filteredGroupsList.get(position - 1);
                viewHolder.bindGroup(group, group.groupId.equals(selectedId));
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_HOME;
            }
            return TYPE_GROUP;
        }

        public void setSelectedGroup(@NonNull GroupId groupId) {
            this.selectedId = groupId;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return 1 + getFilteredGroupsCount();
        }

        private int getFilteredGroupsCount() {
            return filteredGroupsList == null ? 0 : filteredGroupsList.size();
        }
    }

    private void onSelectGroup(@Nullable GroupId groupId) {
        if (showShareButton) {
            adapter.setSelectedGroup(groupId);
        } else {
            Intent data = new Intent();
            data.putExtra(RESULT_GROUP_ID, groupId);

            setResult(RESULT_OK, data);
            finish();
        }
    }

    private void onSelectFeed(@NonNull @PrivacyList.Type String privacyList) {
        if (showShareButton) {
            adapter.setFeedPrivacy(new FeedPrivacy(privacyList, null, null));
        } else {
            Intent data = new Intent();
            data.putExtra(RESULT_GROUP_ID, (GroupId) null);
            data.putExtra(RESULT_PRIVACY_TYPE, privacyList);

            setResult(RESULT_OK, data);
            finish();
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private final ImageView avatarIconView;
        private final TextView nameView;
        private final RadioButton selectionView;

        private Group group;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            avatarIconView = itemView.findViewById(R.id.group_avatar);
            nameView = itemView.findViewById(R.id.group_name);
            selectionView = itemView.findViewById(R.id.selection_indicator);

            avatarIconView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), itemView.getContext().getResources().getDimension(R.dimen.share_privacy_avatar_corner_radius));
                }
            });
            avatarIconView.setClipToOutline(true);

            itemView.setOnClickListener(v -> {
                if (group != null) {
                    onSelectGroup(group.groupId);
                }
            });
        }

        public void bindGroup(@NonNull Group group, boolean selected) {
            this.group = group;
            nameView.setText(group.name);
            avatarLoader.load(avatarIconView, group.groupId);
            selectionView.setChecked(selected);
        }
    }

    private class HomeViewHolder extends RecyclerView.ViewHolder {

        private final RadioButton selMyContacts;
        private final RadioButton selOnlyContacts;

        private final View myContactsChevron;
        private final View favoritesChevron;

        public HomeViewHolder(@NonNull View itemView) {
            super(itemView);

            selMyContacts = itemView.findViewById(R.id.my_contacts_selection);
            selOnlyContacts = itemView.findViewById(R.id.only_with_selection);

            favoritesChevron = itemView.findViewById(R.id.favorites_chevron);
            myContactsChevron = itemView.findViewById(R.id.my_contacts_chevron);

            itemView.findViewById(R.id.my_contacts).setOnClickListener(v -> {
                if (selMyContacts.isChecked()) {
                    onSelectFeed(PrivacyList.Type.ALL);
                } else {
                    saveList(PrivacyList.Type.ALL, Collections.emptyList());
                }
            });
            itemView.findViewById(R.id.only_share_with).setOnClickListener(v -> {
                List<UserId> onlyList = getOnlyList();
                if (onlyList == null || onlyList.isEmpty()) {
                    if (PermissionUtils.hasOrRequestContactPermissions(SharePrivacyActivity.this, REQUEST_CODE_ASK_CONTACTS_PERMISSION_ONLY)) {
                        editOnlyList();
                    }
                } else {
                    onSelectFeed(PrivacyList.Type.ONLY);
                }
            });

            favoritesChevron.setOnClickListener(v -> {
                if (PermissionUtils.hasOrRequestContactPermissions(SharePrivacyActivity.this, REQUEST_CODE_ASK_CONTACTS_PERMISSION_ONLY)) {
                    editOnlyList();
                }
            });

            myContactsChevron.setOnClickListener(v -> {
                if (PermissionUtils.hasOrRequestContactPermissions(SharePrivacyActivity.this, REQUEST_CODE_ASK_CONTACTS_PERMISSION_VIEW_MY_CONTACTS)) {
                    viewMyContacts();
                }
            });
        }

        public void bind(FeedPrivacy feedPrivacy, boolean selected) {
            int selection = 0;
            if (feedPrivacy == null) {
            } else if (PrivacyList.Type.ALL.equals(feedPrivacy.activeList)) {
                selection = 0;
            } else if (PrivacyList.Type.EXCEPT.equals(feedPrivacy.activeList)) {
                selection = 1;
            } else if (PrivacyList.Type.ONLY.equals(feedPrivacy.activeList)) {
                selection = 2;
            }
            setSelection(selected ? selection : -1);
        }

        private void setSelection(int sel) {
            selMyContacts.setChecked(sel == 0);
            selOnlyContacts.setChecked(sel >= 2);
        }
    }
}
