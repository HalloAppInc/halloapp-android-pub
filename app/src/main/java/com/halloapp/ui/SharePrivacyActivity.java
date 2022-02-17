package com.halloapp.ui;

import android.Manifest;
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
import androidx.annotation.StringRes;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.content.Chat;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.contacts.ContactPermissionBottomSheetDialog;
import com.halloapp.ui.contacts.MultipleContactPickerActivity;
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

    private static final String EXTRA_CURRENT_SELECTION = "current_selection";

    private static final int REQUEST_CODE_SELECT_EXCEPT_LIST = 1;
    private static final int REQUEST_CODE_SELECT_ONLY_LIST = 2;

    public static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION_ONLY = 1;
    public static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION_EXCEPT = 2;

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
            case REQUEST_CODE_SELECT_EXCEPT_LIST:
                if (resultCode == RESULT_OK && data != null) {
                    List<UserId> exceptList = data.getParcelableArrayListExtra(MultipleContactPickerActivity.EXTRA_RESULT_SELECTED_IDS);
                    saveList(PrivacyList.Type.EXCEPT, exceptList);
                }
                break;
            case REQUEST_CODE_SELECT_ONLY_LIST:
                if (resultCode == RESULT_OK && data != null) {
                    List<UserId> onlyList = data.getParcelableArrayListExtra(MultipleContactPickerActivity.EXTRA_RESULT_SELECTED_IDS);
                    saveList(PrivacyList.Type.ONLY, onlyList);
                }
                break;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_share_with);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

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

    private void openMultipleContactPicker(int requestCode, List<UserId> currentList, @StringRes int title) {
        startActivityForResult(MultipleContactPickerActivity.newPickerIntentAllowEmpty(this, currentList, title), requestCode);
    }

    private void editExceptList() {
        openMultipleContactPicker(REQUEST_CODE_SELECT_EXCEPT_LIST, getExceptList(), R.string.contact_picker_feed_except_title);
    }

    private void editOnlyList() {
        openMultipleContactPicker(REQUEST_CODE_SELECT_ONLY_LIST, getOnlyList(), R.string.contact_picker_feed_only_title);
    }

    private List<UserId> getOnlyList() {
        FeedPrivacy privacy = viewModel.getFeedPrivacy().getValue();
        if (privacy != null) {
            return privacy.onlyList;
        }
        return null;
    }

    private List<UserId> getExceptList() {
        FeedPrivacy privacy = viewModel.getFeedPrivacy().getValue();
        if (privacy != null) {
            return privacy.exceptList;
        }
        return null;
    }

    private void saveList(@PrivacyList.Type @NonNull String listType, List<UserId> userId) {
        viewModel.savePrivacy(listType, userId).observe(this, done -> {
            if (done != null) {
                if (done) {
                    Toast.makeText(this, R.string.feed_privacy_update_success, Toast.LENGTH_LONG).show();
                    onSelectChat(null);
                } else {
                    SnackbarHelper.showWarning(this, R.string.feed_privacy_update_failure);
                }
            }
        });
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_CONTACTS_PERMISSION_EXCEPT: {
                editExceptList();
                break;
            }
            case REQUEST_CODE_ASK_CONTACTS_PERMISSION_ONLY: {
                editOnlyList();
                break;
            }
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

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
                Chat chat = filteredGroupsList.get(position - 1);
                viewHolder.bindChat(chat, chat.chatId.equals(selectedId));
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_HOME;
            }
            return TYPE_GROUP;
        }

        @Override
        public int getItemCount() {
            return 1 + getFilteredGroupsCount();
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

        public void bindChat(@NonNull Chat chat, boolean selected) {
            nameView.setText(chat.name);
            avatarLoader.load(avatarIconView, chat.chatId);
            this.chat = chat;
            selectionView.setVisibility(selected ? View.VISIBLE : View.GONE);
        }
    }

    private class HomeViewHolder extends RecyclerView.ViewHolder {

        private TextView exceptContactsSetting;
        private TextView onlyContactsSetting;

        private RadioButton selMyContacts;
        private RadioButton selExceptContacts;
        private RadioButton selOnlyContacts;

        public HomeViewHolder(@NonNull View itemView) {
            super(itemView);

            selMyContacts = itemView.findViewById(R.id.my_contacts_selection);
            selExceptContacts = itemView.findViewById(R.id.contacts_except_selection);
            selOnlyContacts = itemView.findViewById(R.id.only_with_selection);

            exceptContactsSetting = itemView.findViewById(R.id.contacts_except_setting);
            onlyContactsSetting = itemView.findViewById(R.id.only_with_setting);

            itemView.findViewById(R.id.my_contacts).setOnClickListener(v -> {
                if (selMyContacts.isChecked()) {
                    onSelectChat(null);
                } else {
                    saveList(PrivacyList.Type.ALL, Collections.emptyList());
                }
            });
            itemView.findViewById(R.id.contacts_except).setOnClickListener(v -> {
                if (EasyPermissions.hasPermissions(itemView.getContext(), Manifest.permission.READ_CONTACTS)) {
                    editExceptList();
                } else {
                    ContactPermissionBottomSheetDialog.showRequest(getSupportFragmentManager(), REQUEST_CODE_ASK_CONTACTS_PERMISSION_EXCEPT);
                }
            });
            itemView.findViewById(R.id.only_share_with).setOnClickListener(v -> {
                if (EasyPermissions.hasPermissions(itemView.getContext(), Manifest.permission.READ_CONTACTS)) {
                    editOnlyList();
                } else {
                    ContactPermissionBottomSheetDialog.showRequest(getSupportFragmentManager(), REQUEST_CODE_ASK_CONTACTS_PERMISSION_ONLY);
                }
            });
        }

        public void bind(FeedPrivacy feedPrivacy, boolean selected) {
            int selection = 0;
            if (feedPrivacy == null) {
                onlyContactsSetting.setText("");
                exceptContactsSetting.setText("");
            } else if (PrivacyList.Type.ALL.equals(feedPrivacy.activeList)) {
                selection = 0;
            } else if (PrivacyList.Type.EXCEPT.equals(feedPrivacy.activeList)) {
                selection = 1;
            } else if (PrivacyList.Type.ONLY.equals(feedPrivacy.activeList)) {
                selection = 2;
            }
            setSelection(selected ? selection : -1);
            if (feedPrivacy != null) {
                onlyContactsSetting.setText(getResources().getQuantityString(R.plurals.composer_sharing_only_summary, feedPrivacy.onlyList.size(), feedPrivacy.onlyList.size()));
                exceptContactsSetting.setText(R.string.composer_sharing_except_summary);

            }
        }

        private void setSelection(int sel) {
            selMyContacts.setChecked(sel == 0);
            selExceptContacts.setChecked(sel == 1);
            selOnlyContacts.setChecked(sel >= 2);
        }
    }
}
