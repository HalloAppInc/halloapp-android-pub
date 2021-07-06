package com.halloapp.ui.invites;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
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
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.SystemUiVisibility;
import com.halloapp.ui.contacts.ContactPermissionBottomSheetDialog;
import com.halloapp.util.FilterUtils;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.invites.InvitesResponseIq;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import pub.devrel.easypermissions.EasyPermissions;

public class InviteContactsActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {

    public static final String EXTRA_SEARCH_TEXT = "search_text";

    private static final int OPEN_INVITE_COUNT = 10_000;

    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION = 1;

    private final ContactsAdapter adapter = new ContactsAdapter();

    private InviteContactsViewModel viewModel;
    private TextView emptyView;
    private RecyclerView listView;

    private TextView bannerView;
    private Snackbar banner;

    private boolean sendingEnabled;

    private String smsPackageName;

    private Drawable waIcon;
    private Drawable smsIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_invite_contacts);

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
        String searchText = getIntent().getStringExtra(EXTRA_SEARCH_TEXT);
        if (searchText != null) {
            searchBox.setText(searchText);
            searchBox.setSelection(searchText.length());
        }
        bannerView = findViewById(R.id.banner);

        listView = findViewById(android.R.id.list);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter);

        emptyView = findViewById(android.R.id.empty);

        viewModel = new ViewModelProvider(this).get(InviteContactsViewModel.class);
        viewModel.getContactList().observe(this, adapter::setContacts);
        viewModel.inviteOptions.getLiveData().observe(this, adapter::setInviteOptions);
        viewModel.waContacts.getLiveData().observe(this, adapter::setWAContacts);

        View progress = findViewById(R.id.progress);

        viewModel.getInviteCountAndRefreshTime().observe(this, inviteAndRefresh -> {
            if (inviteAndRefresh == null) {
                progress.setVisibility(View.VISIBLE);
                setSendingEnabled(false);
            } else if (inviteAndRefresh.getInvitesRemaining() == InviteContactsViewModel.RESPONSE_RETRYABLE) {
                bannerView.setText(R.string.invite_info_fetch_internet);
                progress.setVisibility(View.GONE);
                setSendingEnabled(false);
                bannerView.setVisibility(View.VISIBLE);
            } else if (inviteAndRefresh.getInvitesRemaining() >= OPEN_INVITE_COUNT) {
                progress.setVisibility(View.GONE);
                bannerView.setVisibility(View.GONE);
                setSendingEnabled(true);
            } else if (inviteAndRefresh.getInvitesRemaining() > 0) {
                progress.setVisibility(View.GONE);
                bannerView.setText(getResources().getQuantityString(R.plurals.invites_remaining,
                            inviteAndRefresh.getInvitesRemaining(), inviteAndRefresh.getInvitesRemaining()));
                setSendingEnabled(true);
                bannerView.setVisibility(View.VISIBLE);
            } else {
                progress.setVisibility(View.GONE);
                long inviteRefreshTime = inviteAndRefresh.getTimeTillRefresh() * 1000L + System.currentTimeMillis();
                String serverTimeRef = DateUtils.formatDateTime(this, inviteRefreshTime, DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE);
                bannerView.setText(getString(R.string.server_invite_refresh_time, serverTimeRef));
                setSendingEnabled(false);
                bannerView.setVisibility(View.VISIBLE);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_contacts_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh_contacts) {
            ContactsSync.getInstance(this).startContactsSync(true);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        finish();
    }

    private void loadContacts() {
        final String[] perms = {Manifest.permission.READ_CONTACTS};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            ContactPermissionBottomSheetDialog.showRequest(getSupportFragmentManager(), REQUEST_CODE_ASK_CONTACTS_PERMISSION);
        } else {
            viewModel.refreshContacts();
        }
    }

    private void sendInvite(@NonNull Contact contact) {
        if (contact.normalizedPhone == null) {
            Log.e("InvitecontactsActivity/sendInvite null contact phone");
            return;
        }
        ProgressDialog dialog = ProgressDialog.show(this, null, getString(R.string.invite_creation_in_progress));
        viewModel.sendInvite(contact).observe(this, nullableResult -> {
            dialog.cancel();
            if (nullableResult != InvitesResponseIq.Result.SUCCESS) {
                showErrorDialog(nullableResult);
            } else {
                onSuccessfulInvite(contact);
            }
        });
    }

    private String getInviteText(@NonNull Contact contact) {
        return getString(R.string.invite_text_with_name_and_number, contact.getShortName(), contact.getDisplayPhone());
    }

    private void sendInviteSms(@NonNull Contact contact) {
        if (contact.normalizedPhone == null) {
            Log.e("InvitecontactsActivity/sendInviteSms null contact phone");
            return;
        }
        ProgressDialog dialog = ProgressDialog.show(this, null, getString(R.string.invite_creation_in_progress));
        viewModel.sendInvite(contact).observe(this, nullableResult -> {
            dialog.cancel();
            if (nullableResult != InvitesResponseIq.Result.SUCCESS) {
                showErrorDialog(nullableResult);
            } else {
                Intent smsIntent = IntentUtils.createSmsIntent(contact.normalizedPhone, getInviteText(contact));
                startActivity(smsIntent);
            }
        });
    }

    private void sendInviteWA(@NonNull Contact contact) {
        if (contact.normalizedPhone == null) {
            Log.e("InvitecontactsActivity/sendInviteWA null contact phone");
            return;
        }
        ProgressDialog dialog = ProgressDialog.show(this, null, getString(R.string.invite_creation_in_progress));
        viewModel.sendInvite(contact).observe(this, nullableResult -> {
            dialog.cancel();
            if (nullableResult != InvitesResponseIq.Result.SUCCESS) {
                showErrorDialog(nullableResult);
            } else {
                Intent smsIntent = IntentUtils.createWhatsAppIntent(contact.normalizedPhone, getInviteText(contact), false);
                startActivity(smsIntent);
            }
        });
    }

    private void onSuccessfulInvite(@NonNull Contact contact) {
        Intent chooser = IntentUtils.createSmsChooserIntent(this, getString(R.string.invite_friend_chooser_title, contact.getShortName()), Preconditions.checkNotNull(contact.normalizedPhone), getInviteText(contact));
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
        private Set<String> waContacts;

        private InviteContactsViewModel.InviteOptions inviteOptions;

        void setContacts(@NonNull List<Contact> contacts) {
            this.contacts = contacts;
            getFilter().filter(filterText);
        }

        void setInviteOptions(@Nullable InviteContactsViewModel.InviteOptions inviteOptions) {
            this.inviteOptions = inviteOptions;
            notifyDataSetChanged();
        }

        void setFilteredContacts(@NonNull List<Contact> contacts, CharSequence filterText) {
            this.filteredContacts = contacts;
            this.filterText = filterText;
            this.filterTokens = FilterUtils.getFilterTokens(filterText);
            notifyDataSetChanged();
        }

        void setWAContacts(@NonNull Set<String> waContacts) {
            this.waContacts = waContacts;
            notifyDataSetChanged();
        }

        @Override
        public @NonNull ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ContactViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_invite_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
            if (position < getFilteredContactsCount()) {
                holder.bindTo(filteredContacts.get(position), filterTokens, inviteOptions, waContacts);
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

    private class ContactsFilter extends FilterUtils.ItemFilter<Contact> {

        ContactsFilter(@NonNull List<Contact> contacts) {
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

    class ContactViewHolder extends RecyclerView.ViewHolder {

        final private TextView nameView;
        final private TextView phoneView;
        final private TextView captionView;
        final private ImageView waView;
        final private ImageView smsView;
        final private View pendingView;

        private Contact contact;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            captionView = itemView.findViewById(R.id.potential_friends);
            nameView = itemView.findViewById(R.id.name);
            phoneView = itemView.findViewById(R.id.phone);
            waView = itemView.findViewById(R.id.wa_btn);
            smsView = itemView.findViewById(R.id.sms_btn);
            pendingView = itemView.findViewById(R.id.pending_btn);
            itemView.setOnClickListener(v -> {
                if (contact != null) {
                    sendInvite(contact);
                }
            });
        }

        void bindTo(@NonNull Contact contact, List<String> filterTokens, @Nullable InviteContactsViewModel.InviteOptions inviteOptions, @Nullable Set<String> waContacts) {
            boolean canSend = (sendingEnabled || contact.invited) && contact.userId == null;
            if (canSend) {
                itemView.setAlpha(1);
                itemView.setClickable(true);
            } else {
                itemView.setAlpha(0.54f);
                itemView.setClickable(false);
            }
            this.contact = contact;
            if (filterTokens != null && !filterTokens.isEmpty()) {
                final String name = contact.getDisplayName();
                CharSequence formattedName = FilterUtils.formatMatchingText(itemView.getContext(), name, filterTokens);

                if (formattedName != null) {
                    nameView.setText(formattedName);
                } else {
                    nameView.setText(name);
                }
            } else {
                nameView.setText(contact.getDisplayName());
            }
            if (contact.normalizedPhone == null) {
                phoneView.setText(R.string.invite_invalid_phone_number);
            } else {
                phoneView.setText(contact.getDisplayPhone());
            }
            if (contact.userId != null) {
                captionView.setVisibility(View.VISIBLE);
                captionView.setText(getString(R.string.invite_already_on_halloapp));
            } else if (contact.numPotentialFriends > 0) {
                captionView.setText(getResources().getQuantityString(R.plurals.friends_on_halloapp, (int) contact.numPotentialFriends, (int) contact.numPotentialFriends));
                captionView.setVisibility(View.VISIBLE);
            } else {
                captionView.setVisibility(View.GONE);
            }

            if (inviteOptions == null || contact.invited || contact.userId != null) {
                waView.setVisibility(View.GONE);
                smsView.setVisibility(View.GONE);
                pendingView.setVisibility((contact.invited && contact.userId == null) ? View.VISIBLE : View.GONE);
            } else {
                pendingView.setVisibility(View.GONE);
                if (waContacts != null && waContacts.contains(contact.normalizedPhone) && inviteOptions.hasWA) {
                    waView.setVisibility(View.VISIBLE);
                    if (waIcon == null) {
                        try {
                            waIcon = waView.getContext().getPackageManager().getApplicationIcon("com.whatsapp");
                        }
                        catch (PackageManager.NameNotFoundException e) {
                            Log.e("InviteContactsActivity/onBindViewHolder wa package not found");
                        }
                    }
                    waView.setImageDrawable(waIcon);
                    waView.setOnClickListener(v -> {
                        if (!canSend) {
                            return;
                        }
                        sendInviteWA(contact);
                    });
                } else {
                    waView.setVisibility(View.GONE);
                }
                if (inviteOptions.defaultSms == null) {
                    smsView.setVisibility(View.GONE);
                } else {
                    smsView.setVisibility(View.VISIBLE);
                    if (smsPackageName == null || !smsPackageName.equalsIgnoreCase(inviteOptions.defaultSms)) {
                        try {
                            smsIcon = smsView.getContext().getPackageManager().getApplicationIcon(inviteOptions.defaultSms);
                            smsPackageName = inviteOptions.defaultSms;
                        }
                        catch (PackageManager.NameNotFoundException e) {
                            Log.e("InviteContactsActivity/onBindViewHolder failed to load icon for package " + inviteOptions.defaultSms);
                        }
                    }
                    smsView.setImageDrawable(smsIcon);
                    smsView.setOnClickListener(v -> {
                        if (!canSend) {
                            return;
                        }
                        sendInviteSms(contact);
                    });
                }
            }
        }
    }
}
