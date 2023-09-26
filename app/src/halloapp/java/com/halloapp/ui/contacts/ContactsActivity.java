package com.halloapp.ui.contacts;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkInfo;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.id.UserId;
import com.halloapp.permissions.PermissionUtils;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.SystemUiVisibility;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.groups.CreateGroupActivity;
import com.halloapp.ui.invites.InviteContactsActivity;
import com.halloapp.util.FilterUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class ContactsActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION = 1;

    private static final String EXTRA_SHOW_INVITE = "show_invite_option";
    private static final String EXTRA_SHOW_CREATE_GROUP = "show_create_group";
    private static final String EXTRA_EXCLUDE_UIDS = "excluded_uids";
    private static final String EXTRA_TITLE_RES = "title_res";
    public static final String RESULT_SELECTED_ID = "selected_id";
    public static final String RESULT_SELECTED_CONTACT = "selected_contact";

    private final ContactsAdapter adapter = new ContactsAdapter();
    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();

    private ContactsViewModel viewModel;
    private TextView emptyView;
    private ProgressBar progressBar;

    public static Intent createBlocklistContactPicker(@NonNull Context context, @Nullable List<UserId> disabledUserIds) {
        Intent intent = new Intent(context, ContactsActivity.class);
        intent.putExtra(EXTRA_SHOW_INVITE, false);
        intent.putExtra(EXTRA_SHOW_CREATE_GROUP, false);
        intent.putExtra(EXTRA_TITLE_RES, R.string.picker_title_block_user);
        if (disabledUserIds != null) {
            intent.putParcelableArrayListExtra(EXTRA_EXCLUDE_UIDS, new ArrayList<>(disabledUserIds));
        }
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_contacts);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        int titleRes = getIntent().getIntExtra(EXTRA_TITLE_RES, 0);
        if (titleRes != 0) {
            setTitle(titleRes);
        }

        EditText searchBox = findViewById(R.id.search_text);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.getFilter().filter(s.toString());
            }
        });
        searchBox.requestFocus();

        final RecyclerView listView = findViewById(android.R.id.list);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter);
        listView.addItemDecoration(new ContactsSectionItemDecoration(
                getResources().getDimension(R.dimen.contacts_list_item_header_width),
                getResources().getDimension(R.dimen.contacts_list_item_height),
                getResources().getDimension(R.dimen.contacts_list_item_header_text_size),
                getResources().getColor(R.color.contacts_list_item_header_text_color),
                adapter::getSectionName));

        emptyView = findViewById(android.R.id.empty);

        progressBar = findViewById(R.id.progress);

        viewModel = new ViewModelProvider(this).get(ContactsViewModel.class);
        viewModel.getFriendsList().getLiveData().observe(this, adapter::setContacts);

        boolean showInviteOption = getIntent().getBooleanExtra(EXTRA_SHOW_INVITE, true);
        adapter.setInviteVisible(showInviteOption);

        List<UserId> excludedUsers = getIntent().getParcelableArrayListExtra(EXTRA_EXCLUDE_UIDS);
        adapter.setExcludedUsers(excludedUsers);

        loadContacts();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contacts_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh_contacts) {
            progressBar.setVisibility(View.VISIBLE);
            final ContactsSync contactsSync = ContactsSync.getInstance();
            contactsSync.cancelContactsSync();
            contactsSync.getWorkInfoLiveData()
                    .observe(this, workInfos -> {
                        if (workInfos != null) {
                            for (WorkInfo workInfo : workInfos) {
                                if (workInfo.getId().equals(contactsSync.getLastFullSyncRequestId())) {
                                    if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                        progressBar.setVisibility(View.GONE);
                                    } else if (workInfo.getState().isFinished()) {
                                        progressBar.setVisibility(View.GONE);
                                        SnackbarHelper.showWarning(this, R.string.refresh_contacts_failed);
                                    }
                                    break;
                                }
                            }
                        }
                    });
            contactsSync.forceFullContactsSync();
            return true;
        } else if (item.getItemId() == R.id.invite_friends) {
            onInviteFriends();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> list) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {
            case REQUEST_CODE_ASK_CONTACTS_PERMISSION: {
                loadContacts();
                break;
            }
        }
    }

    private void onInviteFriends() {
        final Intent intent = new Intent(this, InviteContactsActivity.class);
        intent.putExtra(InviteContactsActivity.EXTRA_SEARCH_TEXT, adapter.filterText);
        startActivity(intent);
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            //noinspection SwitchStatementWithTooFewBranches
            switch (requestCode) {
                case REQUEST_CODE_ASK_CONTACTS_PERMISSION: {
                    new AppSettingsDialog.Builder(this)
                            .setRationale(getString(R.string.contacts_permission_rationale_denied))
                            .build().show();
                    break;
                }
            }
        }
    }

    private void loadContacts() {
        if (PermissionUtils.hasOrRequestContactPermissions(this, REQUEST_CODE_ASK_CONTACTS_PERMISSION)) {
            viewModel.getFriendsList().invalidate();
        }
    }

    private static final int ITEM_TYPE_CONTACT = 0;
    private static final int ITEM_TYPE_INVITE = 1;
    private static final int ITEM_TYPE_NEW_GROUP = 2;

    private class ContactsAdapter extends RecyclerView.Adapter<ViewHolder> implements FastScrollRecyclerView.SectionedAdapter, Filterable {

        private boolean showCreateGroup = ServerProps.getInstance().getGroupChatsEnabled();

        private List<Contact> contacts = new ArrayList<>();
        private List<Contact> filteredContacts;
        private CharSequence filterText;
        private List<String> filterTokens;

        private boolean showInviteOption;

        private Set<UserId> excludedUsers;

        void setContacts(@NonNull List<Contact> contacts) {
            if (excludedUsers != null) {
                this.contacts = new ArrayList<>(contacts.size());
                for (Contact contact : contacts) {
                    if (!excludedUsers.contains(contact.userId)) {
                        this.contacts.add(contact);
                    }
                }
            } else {
                this.contacts = contacts;
            }
            getFilter().filter(filterText);
        }

        void setExcludedUsers(@Nullable List<UserId> excludedContacts) {
            if (excludedContacts != null) {
                this.excludedUsers = new HashSet<>(excludedContacts);
            } else {
                this.excludedUsers = null;
            }
        }

        void setFilteredContacts(@NonNull List<Contact> contacts, CharSequence filterText) {
            this.filteredContacts = contacts;
            this.filterText = filterText;
            this.filterTokens = FilterUtils.getFilterTokens(filterText);
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            if (showCreateGroup) {
                if (position == 0) {
                    return ITEM_TYPE_NEW_GROUP;
                }
                return position <= getFilteredContactsCount() ? ITEM_TYPE_CONTACT : ITEM_TYPE_INVITE;
            } else {
                return position < getFilteredContactsCount() ? ITEM_TYPE_CONTACT : ITEM_TYPE_INVITE;
            }
        }

        @Override
        public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.i("ContactsAdapter.onCreateViewHolder " + viewType);
            switch (viewType) {
                case ITEM_TYPE_INVITE: {
                    return new InviteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.invite_item, parent, false));
                }
                case ITEM_TYPE_CONTACT: {
                    return new ContactViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false));
                }
                case ITEM_TYPE_NEW_GROUP: {
                    return new NewGroupViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.new_group_item, parent, false));
                }
                default: {
                    throw new IllegalArgumentException("Invalid view type " + viewType);
                }
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (showCreateGroup) {
                position--;
            }
            if (position < getFilteredContactsCount() && position >= 0) {
                holder.bindTo(filteredContacts.get(position), filterTokens);
            }
        }

        private boolean shouldShowInviteItem() {
            return showInviteOption;
        }

        protected void setInviteVisible(boolean visible) {
            showInviteOption = visible;
        }

        @Override
        public int getItemCount() {
            return getFilteredContactsCount() + (shouldShowInviteItem() ? 1 : 0) + (showCreateGroup ? 1 : 0);
        }

        @NonNull
        @Override
        public String getSectionName(int position) {
            if (showCreateGroup) {
                position--;
            }
            if (position < 0 || filteredContacts == null || position >= filteredContacts.size()) {
                return "";
            }
            final String name = filteredContacts.get(position).getDisplayName();
            if (TextUtils.isEmpty(name)) {
                return "";
            }
            final int codePoint = name.codePointAt(0);
            return Character.isAlphabetic(codePoint) ? new String(Character.toChars(codePoint)).toUpperCase(Locale.getDefault()) : "#";
        }

        @Override
        public Filter getFilter() {
            return new ContactsFilter(contacts);
        }

        private int getFilteredContactsCount() {
            return filteredContacts == null ? 0 : filteredContacts.size();
        }
    }

    private class ContactsFilter extends FilterUtils.ItemFilter<Contact> {

        public ContactsFilter(@NonNull List<Contact> contacts) {
            super(contacts);
        }

        @Override
        protected String itemToString(Contact contact) {
            return contact.getDisplayName();
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            final List<Contact> filteredContacts = (List<Contact>) results.values;
            adapter.setFilteredContacts(filteredContacts, constraint);
            if (filteredContacts.isEmpty() && !TextUtils.isEmpty(constraint)) {
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText(getString(R.string.contact_search_empty, constraint));
            } else {
                emptyView.setVisibility(View.GONE);
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void bindTo(@NonNull Contact contact, List<String> filterTokens) {
        }
    }

    class NewGroupViewHolder extends ViewHolder {

        NewGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(v -> startActivity(CreateGroupActivity.newChatPickerIntent(itemView.getContext())));
        }
    }

    class ContactViewHolder extends ViewHolder {

        final private ImageView avatarView;
        final private TextView nameView;
        final private TextView phoneView;

        private Contact contact;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.avatar);
            nameView = itemView.findViewById(R.id.name);
            phoneView = itemView.findViewById(R.id.phone);
            itemView.setOnClickListener(v -> {
                Intent result = new Intent();
                result.putExtra(RESULT_SELECTED_ID,  Preconditions.checkNotNull(contact.userId).rawId());
                result.putExtra(RESULT_SELECTED_CONTACT, contact);
                setResult(RESULT_OK, result);
                finish();
            });
        }

        void bindTo(@NonNull Contact contact, List<String> filterTokens) {
            this.contact = contact;
            avatarLoader.load(avatarView, Preconditions.checkNotNull(contact.userId), false);
            if (filterTokens != null && !filterTokens.isEmpty()) {
                String name = contact.getDisplayName();
                CharSequence formattedName = FilterUtils.formatMatchingText(ContactsActivity.this, name, filterTokens);
                if (formattedName != null) {
                    nameView.setText(formattedName);
                } else {
                    nameView.setText(name);
                }
            } else {
                nameView.setText(contact.getDisplayName());
            }
            phoneView.setText(contact.getDisplayPhone());
        }
    }

    class InviteViewHolder extends ViewHolder {

        InviteViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(v -> onInviteFriends());
        }
    }

}
