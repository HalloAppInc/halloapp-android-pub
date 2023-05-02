package com.halloapp.ui.contacts;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkInfo;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.permissions.PermissionUtils;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.SystemUiVisibility;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.avatar.DeviceAvatarLoader;
import com.halloapp.ui.invites.InviteContactsActivity;
import com.halloapp.util.FilterUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class SelectDeviceContactActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION = 1;

    public static final String EXTRA_SELECTED_CONTACT = "selected_contact";

    private static final String EXTRA_TITLE_RES = "title_res";

    private final ContactsAdapter adapter = new ContactsAdapter();
    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();

    private DeviceAvatarLoader deviceAvatarLoader;

    private SelectContactsViewModel viewModel;
    private TextView emptyView;
    private ProgressBar progressBar;

    private View infoBtn;

    public static Intent select(@NonNull Context context) {
        Intent i = new Intent(context, SelectDeviceContactActivity.class);

        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        deviceAvatarLoader = new DeviceAvatarLoader(this);

        setContentView(R.layout.activity_select_device_contact);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        int titleRes = getIntent().getIntExtra(EXTRA_TITLE_RES, 0);
        if (titleRes != 0) {
            setTitle(titleRes);
        }

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

        infoBtn = findViewById(R.id.info_btn);
        infoBtn.setVisibility(View.GONE);

        viewModel = new ViewModelProvider(this).get(SelectContactsViewModel.class);
        viewModel.contactList.getLiveData().observe(this, adapter::setContacts);

        loadContacts();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (deviceAvatarLoader != null) {
            deviceAvatarLoader.destroy();
            deviceAvatarLoader = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_contacts_menu, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
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
            viewModel.contactList.invalidate();
        }
    }

    private class ContactsAdapter extends RecyclerView.Adapter<ViewHolder> implements FastScrollRecyclerView.SectionedAdapter, Filterable {

        private List<Contact> contacts = new ArrayList<>();
        private List<Contact> filteredContacts;
        private CharSequence filterText;
        private List<String> filterTokens;

        private boolean inSearch;

        void setContacts(@NonNull List<Contact> contacts) {
            this.contacts = contacts;
            getFilter().filter(filterText);
        }

        void setFilteredContacts(@NonNull List<Contact> contacts, CharSequence filterText) {
            this.filteredContacts = contacts;
            this.filterText = filterText;
            this.filterTokens = FilterUtils.getFilterTokens(filterText);
            notifyDataSetChanged();
        }

        @Override
        public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.i("ContactsAdapter.onCreateViewHolder " + viewType);
            return new ContactViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (position < getFilteredContactsCount()) {
                holder.bindTo(filteredContacts.get(position), filterTokens);
            }
        }

        public void setInSearch(boolean inSearch) {
            this.inSearch = inSearch;
        }

        @Override
        public int getItemCount() {
            return getFilteredContactsCount() + ((!inSearch) ? 1 : 0);
        }

        @NonNull
        @Override
        public String getSectionName(int position) {
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
            boolean emptyConstraint = TextUtils.isEmpty(constraint);
            adapter.setInSearch(!emptyConstraint);
            if (filteredContacts.isEmpty() && !emptyConstraint) {
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
                result.putExtra(EXTRA_SELECTED_CONTACT, contact);
                setResult(RESULT_OK, result);
                finish();
            });
        }

        void bindTo(@NonNull Contact contact, List<String> filterTokens) {
            this.contact = contact;
            if (contact.userId != null) {
                avatarLoader.load(avatarView, contact.userId, false);
                deviceAvatarLoader.cancel(avatarView);
            } else {
                avatarLoader.cancel(avatarView);
                deviceAvatarLoader.load(avatarView, contact.addressBookPhone);
            }
            if (filterTokens != null && !filterTokens.isEmpty()) {
                String name = contact.getDisplayName();
                CharSequence formattedName = FilterUtils.formatMatchingText(SelectDeviceContactActivity.this, name, filterTokens);
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

}
