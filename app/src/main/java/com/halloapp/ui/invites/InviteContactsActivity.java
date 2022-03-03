package com.halloapp.ui.invites;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkInfo;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.contacts.InviteContactsAdapter;
import com.halloapp.permissions.PermissionUtils;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.SystemUiVisibility;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.KeyboardUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.invites.InvitesResponseIq;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class InviteContactsActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {

    public static final String EXTRA_SEARCH_TEXT = "search_text";

    private static final int OPEN_INVITE_COUNT = 10_000;

    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION = 1;

    private final InviteContactsAdapter adapter = new InviteContactsAdapter();

    private InviteContactsViewModel viewModel;
    private TextView emptyView;
    private RecyclerView listView;
    private View refreshProgressView;
    private TextView bannerView;

    private final Rect editHitRect = new Rect();
    private EditText searchBox;

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

        searchBox = findViewById(R.id.search_text);
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
        String searchText = getIntent().getStringExtra(EXTRA_SEARCH_TEXT);
        if (searchText != null) {
            searchBox.setText(searchText);
            searchBox.setSelection(searchText.length());
        }
        adapter.setParent(new InviteContactsAdapter.InviteContactsAdapterParent() {
            @Override
            public void onInvite(@NonNull Contact contact) {
                sendInvite(contact);
            }

            @Override
            public void onFiltered(@NonNull CharSequence constraint, @NonNull List<Contact> contacts) {
                if (contacts.isEmpty() && !TextUtils.isEmpty(constraint)) {
                    emptyView.setVisibility(View.VISIBLE);
                    emptyView.setText(getString(R.string.contact_search_empty, constraint));
                } else {
                    emptyView.setVisibility(View.GONE);
                }
            }
        });
        bannerView = findViewById(R.id.banner);

        listView = findViewById(android.R.id.list);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter);
        View searchContainer = findViewById(R.id.search_container);
        searchContainer.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                listView.setPadding(0, searchContainer.getHeight() + ((ViewGroup.MarginLayoutParams)searchContainer.getLayoutParams()).topMargin, 0, 0);
            }
        });

        emptyView = findViewById(android.R.id.empty);
        refreshProgressView = findViewById(R.id.refresh_progress);

        viewModel = new ViewModelProvider(this).get(InviteContactsViewModel.class);
        viewModel.getContactList().observe(this, adapter::setContacts);

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

    private void setSendingEnabled(boolean enabled) {
        adapter.setSendingEnabled(enabled);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            searchBox.getGlobalVisibleRect(editHitRect);
            if (!editHitRect.contains((int) ev.getX(), (int) ev.getY())) {
                KeyboardUtils.hideSoftKeyboard(searchBox);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_contacts_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh_contacts) {
            refreshProgressView.setVisibility(View.VISIBLE);
            final ContactsSync contactsSync = ContactsSync.getInstance();
            contactsSync.cancelContactsSync();
            contactsSync.getWorkInfoLiveData()
                    .observe(this, workInfos -> {
                        if (workInfos != null) {
                            for (WorkInfo workInfo : workInfos) {
                                if (workInfo.getId().equals(contactsSync.getLastSyncRequestId())) {
                                    if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                        refreshProgressView.setVisibility(View.GONE);
                                    } else if (workInfo.getState().isFinished()) {
                                        refreshProgressView.setVisibility(View.GONE);
                                        SnackbarHelper.showWarning(this, R.string.refresh_contacts_failed);
                                    }
                                    break;
                                }
                            }
                        }
                    });
            ContactsSync.getInstance().forceFullContactsSync();
            return true;
        } else if (item.getItemId() == R.id.share_link) {
            startActivity(IntentUtils.createShareDlIntent());
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

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        finish();
    }

    private void loadContacts() {
        if (PermissionUtils.hasOrRequestContactPermissions(this, REQUEST_CODE_ASK_CONTACTS_PERMISSION)) {
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
        return getString(R.string.invite_text_with_name_and_number, contact.getShortName(), contact.getDisplayPhone(), Constants.DOWNLOAD_LINK_URL);
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
}
