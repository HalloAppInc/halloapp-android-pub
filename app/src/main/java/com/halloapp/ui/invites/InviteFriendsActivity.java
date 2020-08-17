package com.halloapp.ui.invites;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.avatar.DeviceAvatarLoader;
import com.halloapp.ui.contacts.ContactsSectionItemDecoration;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.invites.InvitesResponseIq;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class InviteFriendsActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION = 1;

    private final ContactsAdapter adapter = new ContactsAdapter();

    private AvatarLoader avatarLoader;
    private DeviceAvatarLoader deviceAvatarLoader;
    private InviteFriendsViewModel viewModel;
    private TextView emptyView;
    private RecyclerView listView;

    private Snackbar banner;

    private boolean sendingEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);

        avatarLoader = AvatarLoader.getInstance(this);
        deviceAvatarLoader = new DeviceAvatarLoader(this);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        listView = findViewById(android.R.id.list);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter);
        listView.addItemDecoration(new ContactsSectionItemDecoration(
                getResources().getDimension(R.dimen.contacts_list_item_header_width),
                getResources().getDimension(R.dimen.contacts_list_item_height),
                getResources().getDimension(R.dimen.contacts_list_item_header_text_size),
                getResources().getColor(R.color.contacts_list_item_header_text_color),
                adapter::getSectionName));
        listView.addOnScrollListener(new ActionBarShadowOnScrollListener(this));

        emptyView = findViewById(android.R.id.empty);

        viewModel = new ViewModelProvider(this).get(InviteFriendsViewModel.class);
        viewModel.getContactList().observe(this, adapter::setContacts);

        View progress = findViewById(R.id.progress);

        viewModel.getInviteCount().observe(this, nullableCount -> {
            if (banner != null) {
                banner.dismiss();
            }
            if (nullableCount == null) {
                progress.setVisibility(View.VISIBLE);
                setSendingEnabled(false);
                banner = null;
            } else if (nullableCount == InviteFriendsViewModel.RESPONSE_RETRYABLE) {
                banner = SnackbarHelper.showIndefinitely(listView, R.string.invite_info_fetch_internet);
                banner.setAction(R.string.try_again, v -> {
                   banner.dismiss();
                   viewModel.refreshInvites();
                });
                listView.setPadding(listView.getPaddingLeft(), listView.getPaddingTop(), listView.getPaddingRight(), banner.getView().getHeight());
                progress.setVisibility(View.GONE);
                addBannerCallback();
                setSendingEnabled(false);
            } else if (nullableCount > 0) {
                progress.setVisibility(View.GONE);
                banner = SnackbarHelper.showIndefinitely(listView, getResources().getQuantityString(R.plurals.invites_remaining, nullableCount, nullableCount));
                addBannerCallback();
                setSendingEnabled(true);
            } else {
                progress.setVisibility(View.GONE);
                banner = SnackbarHelper.showIndefinitely(listView, R.string.no_invites_remaining);
                addBannerCallback();
                setSendingEnabled(false);
            }
        });
        loadContacts();
    }

    private void addBannerCallback() {
        if (banner == null) {
            return;
        }
        banner.addCallback(new Snackbar.Callback() {

            @Override
            public void onShown(Snackbar sb) {
                super.onShown(sb);
                listView.setPadding(listView.getPaddingLeft(), listView.getPaddingTop(), listView.getPaddingRight(), banner.getView().getHeight());
            }

            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                listView.setPadding(listView.getPaddingLeft(), listView.getPaddingTop(), listView.getPaddingRight(), 0);
                banner.removeCallback(this);
            }
        });
    }

    private void setSendingEnabled(boolean enabled) {
        sendingEnabled = enabled;
        adapter.notifyDataSetChanged();
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
        getMenuInflater().inflate(R.menu.device_contacts_menu, menu);
        final MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String text) {
                searchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(final String text) {
                adapter.getFilter().filter(text);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case R.id.refresh_contacts: {
                ContactsSync.getInstance(this).startContactsSync(true);
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
        final String[] perms = {Manifest.permission.READ_CONTACTS};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, getString(R.string.contacts_permission_rationale),
                    REQUEST_CODE_ASK_CONTACTS_PERMISSION, perms);
        } else {
            viewModel.refreshContacts();
        }
    }

    private void sendInvite(String phone) {
        ProgressDialog dialog = ProgressDialog.show(this, null, getString(R.string.invite_creation_in_progress));
        viewModel.sendInvite(phone).observe(this, nullableResult -> {
            dialog.cancel();
            if (nullableResult != InvitesResponseIq.Result.SUCCESS) {
                showErrorDialog(nullableResult);
            } else {
                onSuccessfulInvite(phone);
            }
        });
    }

    private void onSuccessfulInvite(String phone) {
        String inviteText = getString(R.string.invite_text);
        Intent chooser = IntentUtils.createSmsChooserIntent(this, getString(R.string.invite_a_friend), phone, inviteText);
        startActivity(chooser);
    }

    private void showErrorDialog(@Nullable @InvitesResponseIq.Result Integer result) {
        @StringRes int errorMessageRes;
        if (result == null) {
            errorMessageRes = R.string.invite_failed_internet;
        } else {
            switch (result) {
                case InvitesResponseIq.Result.EXISTING_USER:
                    errorMessageRes = R.string.invite_failed_existing_user;
                    break;
                case InvitesResponseIq.Result.INVALID_NUMBER:
                    errorMessageRes = R.string.invite_failed_invalid_number;
                    break;
                case InvitesResponseIq.Result.NO_INVITES_LEFT:
                    errorMessageRes = R.string.invite_failed_no_invites;
                    break;
                case InvitesResponseIq.Result.NO_ACCOUNT:
                case InvitesResponseIq.Result.UNKNOWN:
                default:
                    errorMessageRes = R.string.invite_failed_unknown;
                    break;
            }
        }
        AlertDialog dialog = new AlertDialog.Builder(this).setMessage(errorMessageRes).setPositiveButton(R.string.ok, null).create();
        dialog.show();
    }

    private class ContactsAdapter extends RecyclerView.Adapter<ContactViewHolder> implements FastScrollRecyclerView.SectionedAdapter, Filterable {

        private List<Contact> contacts = new ArrayList<>();
        private List<Contact> filteredContacts;
        private CharSequence filterText;
        private List<String> filterTokens;

        void setContacts(@NonNull List<Contact> contacts) {
            this.contacts = contacts;
            getFilter().filter(filterText);
        }

        void setFilteredContacts(@NonNull List<Contact> contacts, CharSequence filterText) {
            this.filteredContacts = contacts;
            this.filterText = filterText;
            this.filterTokens = getFilterTokens(filterText);
            notifyDataSetChanged();
        }

        @Override
        public @NonNull ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ContactViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
            if (position < getFilteredContactsCount()) {
                holder.bindTo(filteredContacts.get(position), filterTokens);
            }
        }

        @Override
        public int getItemCount() {
            return getFilteredContactsCount();
        }

        @NonNull
        @Override
        public String getSectionName(int position) {
            if (filteredContacts == null || position >= filteredContacts.size()) {
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

    class ContactViewHolder extends RecyclerView.ViewHolder {

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
                if (contact != null) {
                    AlertDialog dialog =
                            new AlertDialog.Builder(itemView.getContext())
                                    .setMessage(getString(R.string.invite_confirmation_message, contact.getDisplayName()))
                                    .setPositiveButton(R.string.invite_action, (dialog1, which) -> sendInvite(contact.normalizedPhone))
                                    .setNegativeButton(R.string.cancel, null).create();
                    dialog.show();
                }
            });
        }

        void bindTo(@NonNull Contact contact, List<String> filterTokens) {
            if (!sendingEnabled || contact.userId != null || contact.normalizedPhone == null) {
                itemView.setAlpha(0.54f);
                itemView.setClickable(false);
            } else {
                itemView.setAlpha(1);
                itemView.setClickable(true);
            }
            this.contact = contact;
            if (contact.userId == null) {
                if (contact.normalizedPhone != null) {
                    deviceAvatarLoader.load(avatarView, contact.normalizedPhone);
                }
            } else {
                avatarLoader.load(avatarView, contact.userId);
            }
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
                            @ColorInt int searchHighlightColor = ContextCompat.getColor(itemView.getContext(), R.color.search_highlight);
                            int spanEnd = Math.min(end, start + filterToken.length());
                            formattedName.setSpan(new ForegroundColorSpan(searchHighlightColor), start, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
            if (contact.userId != null) {
                phoneView.setText(R.string.invite_already_a_user_subtitle);
            } else if (contact.normalizedPhone == null) {
                phoneView.setText(R.string.invite_invalid_phone_number);
            } else {
                phoneView.setText(contact.getDisplayPhone());
            }
        }
    }
}
