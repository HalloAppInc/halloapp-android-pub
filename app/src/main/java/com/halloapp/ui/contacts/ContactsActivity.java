package com.halloapp.ui.contacts;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.SystemUiVisibility;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.chat.ChatActivity;
import com.halloapp.ui.groups.CreateGroupActivity;
import com.halloapp.ui.invites.InviteFriendsActivity;
import com.halloapp.util.logs.Log;
import com.halloapp.util.Preconditions;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class ContactsActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION = 1;
    private static final int REQUEST_CODE_CREATE_GROUP_SELECT_CONTACTS = 2;
    private static final int REQUEST_CODE_CREATE_GROUP = 3;

    private static final String EXTRA_SHOW_INVITE = "show_invite_option";
    private static final String EXTRA_EXCLUDE_UIDS = "excluded_uids";
    public static final String RESULT_SELECTED_ID = "selected_id";
    public static final String RESULT_SELECTED_CONTACT = "selected_contact";

    private final ContactsAdapter adapter = new ContactsAdapter();
    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();
    private final ServerProps serverProps = ServerProps.getInstance();

    private ContactsViewModel viewModel;
    private TextView emptyView;

    public static Intent createBlocklistContactPicker(@NonNull Context context, @Nullable List<UserId> disabledUserIds) {
        Intent intent = new Intent(context, ContactsActivity.class);
        intent.putExtra(EXTRA_SHOW_INVITE, false);
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

        viewModel = new ViewModelProvider(this).get(ContactsViewModel.class);
        viewModel.contactList.getLiveData().observe(this, adapter::setContacts);

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
        switch (item.getItemId()) {
            case R.id.refresh_contacts: {
                ContactsSync.getInstance(this).startContactsSync(true);
                return true;
            }
            case R.id.invite_friends: {
                onInviteFriends();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
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
        if (Constants.INVITES_ENABLED) {
            final Intent intent = new Intent(this, InviteFriendsActivity.class);
            startActivity(intent);
        } else {
            final Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.invite_text));
            intent.setType("text/plain");
            startActivity(Intent.createChooser(intent, getText(R.string.share_via)));

        }
    }

    private void onCreateGroup(@Nullable Collection<UserId> initialIds) {
        startActivityForResult(MultipleContactPickerActivity.newPickerIntent(this, initialIds, R.string.group_picker_title, R.string.next, serverProps.getMaxGroupSize(), false), REQUEST_CODE_CREATE_GROUP_SELECT_CONTACTS);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CREATE_GROUP_SELECT_CONTACTS:
                if (resultCode == RESULT_OK && data != null) {
                    List<UserId> userIds = data.getParcelableArrayListExtra(MultipleContactPickerActivity.EXTRA_RESULT_SELECTED_IDS);
                    startActivityForResult(CreateGroupActivity.newPickerIntent(this, userIds), REQUEST_CODE_CREATE_GROUP);
                }
                break;
            case REQUEST_CODE_CREATE_GROUP: {
                if (resultCode == RESULT_OK) {
                    if (data == null) {
                        Log.e("ContactsActivity/onActivityResult missing resulting group id");
                        finish();
                        break;
                    }
                    GroupId groupId = data.getParcelableExtra(CreateGroupActivity.RESULT_GROUP_ID);
                    final Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                    intent.putExtra(ChatActivity.EXTRA_CHAT_ID, groupId);
                    startActivity(intent);
                    finish();
                } else {
                    List<UserId> userIds = null;
                    if (data != null){
                        userIds = data.getParcelableArrayListExtra(CreateGroupActivity.RESULT_USER_IDS);
                    }
                    onCreateGroup(userIds);
                }
                break;
            }
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void loadContacts() {
        final String[] perms = {Manifest.permission.READ_CONTACTS};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, getString(R.string.contacts_permission_rationale),
                    REQUEST_CODE_ASK_CONTACTS_PERMISSION, perms);
        } else {
            viewModel.contactList.invalidate();
        }
    }

    private static final int ITEM_TYPE_CONTACT = 0;
    private static final int ITEM_TYPE_INVITE = 1;
    private static final int ITEM_TYPE_GROUP = 2;

    private class ContactsAdapter extends RecyclerView.Adapter<ViewHolder> implements FastScrollRecyclerView.SectionedAdapter, Filterable {

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
            this.filterTokens = getFilterTokens(filterText);
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            if (shouldShowCreateGroupItem()) {
                if (position == 0) {
                    return ITEM_TYPE_GROUP;
                }
                return position < getFilteredContactsCount() + 1 ? ITEM_TYPE_CONTACT : ITEM_TYPE_INVITE;
            } else {
                return position < getFilteredContactsCount() ? ITEM_TYPE_CONTACT : ITEM_TYPE_INVITE;
            }
        }

        @Override
        public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.i("ContactsAdapter.onCreateViewHolder " + viewType);
            switch (viewType) {
                case ITEM_TYPE_GROUP: {
                    return new CreateGroupViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.create_group_item, parent, false));
                }
                case ITEM_TYPE_INVITE: {
                    return new InviteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.invite_item, parent, false));
                }
                case ITEM_TYPE_CONTACT: {
                    return new ContactViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false));
                }
                default: {
                    throw new IllegalArgumentException("Invalid view type " + viewType);
                }
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (shouldShowCreateGroupItem()) {
                if (position > 0 && position < getFilteredContactsCount() + 1) {
                    holder.bindTo(filteredContacts.get(position - 1), filterTokens);
                }
            } else {
                if (position < getFilteredContactsCount()) {
                    holder.bindTo(filteredContacts.get(position), filterTokens);
                }
            }
        }

        private boolean shouldShowCreateGroupItem() {
            return serverProps.getGroupsEnabled() && TextUtils.isEmpty(filterText);
        }

        protected void setInviteVisible(boolean visible) {
            showInviteOption = visible;
        }

        @Override
        public int getItemCount() {
            return getFilteredContactsCount() + ((showInviteOption && TextUtils.isEmpty(filterText)) ? 1 : 0) + (shouldShowCreateGroupItem() ? 1 : 0);
        }

        @NonNull
        @Override
        public String getSectionName(int position) {
            int contactsPosition = position - (shouldShowCreateGroupItem() ? 1 : 0);
            if (contactsPosition < 0 || filteredContacts == null || contactsPosition >= filteredContacts.size()) {
                return "";
            }
            final String name = filteredContacts.get(contactsPosition).getDisplayName();
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

    private class ContactsFilter extends Filter {

        private final List<Contact> contacts;

        ContactsFilter(@NonNull List<Contact> contacts) {
            this.contacts = contacts;
        }

        @Override
        protected FilterResults performFiltering(@Nullable CharSequence prefix) {
            final FilterResults results = new FilterResults();
            final List<String> filterTokens = getFilterTokens(prefix);
            if (filterTokens == null) {
                results.values = contacts;
                results.count = contacts.size();
            } else {
                final ArrayList<Contact> filteredContacts = new ArrayList<>();
                for (Contact contact : contacts) {
                    final String name = contact.getDisplayName();
                    final List<String> words = getFilterTokens(name);
                    if (words != null) {
                        boolean match = true;
                        for (String filterToken : filterTokens) {
                            boolean tokenMatch = false;
                            for (String word : words) {
                                if (word.startsWith(filterToken)) {
                                    tokenMatch = true;
                                    break;
                                }
                            }
                            if (!tokenMatch) {
                                match = false;
                                break;
                            }
                        }
                        if (match) {
                            filteredContacts.add(contact);
                        }
                    }
                }
                results.values = filteredContacts;
                results.count = filteredContacts.size();
            }
            return results;
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

    static @Nullable List<String> getFilterTokens(final @Nullable CharSequence filterText) {
        if (TextUtils.isEmpty(filterText)) {
            return null;
        }
        final List<String> filterTokens = new ArrayList<>();
        final BreakIterator boundary = BreakIterator.getWordInstance();
        final String filterTextString = filterText.toString();
        boundary.setText(filterTextString);
        int start = boundary.first();
        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
            if (end > start) {
                filterTokens.add(filterTextString.substring(start, end).toLowerCase());
            }
        }
        return filterTokens;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void bindTo(@NonNull Contact contact, List<String> filterTokens) {
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
            avatarLoader.load(avatarView, Preconditions.checkNotNull(contact.userId));
            if (filterTokens != null && !filterTokens.isEmpty()) {
                SpannableString formattedName = null;
                final BreakIterator boundary = BreakIterator.getWordInstance();
                final String name = contact.getDisplayName();
                boundary.setText(name);
                int start = boundary.first();
                for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
                    if (end <= start) {
                        continue;
                    }
                    final String word = name.substring(start, end).toLowerCase();
                    for (String filterToken : filterTokens) {
                        if (word.startsWith(filterToken)) {
                            if (formattedName == null) {
                                formattedName = new SpannableString(name);
                            }
                            formattedName.setSpan(new ForegroundColorSpan(ContextCompat.getColor(itemView.getContext(), R.color.search_highlight)), start, Math.min(end, start + filterToken.length()), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                }
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
            itemView.setOnClickListener(v -> {
                onInviteFriends();
            });
        }
    }

    class CreateGroupViewHolder extends ViewHolder {

        CreateGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(v -> {
                onCreateGroup(null);
            });
        }
    }
}
