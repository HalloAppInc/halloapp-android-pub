package com.halloapp.ui.contacts;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.InviteHelper;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.avatar.DeviceAvatarLoader;
import com.halloapp.ui.invites.InviteContactsViewModel;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.invites.InvitesResponseIq;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class ViewMyNetworkActivity extends HalloActivity {

    private ConnectedContactsAdapter connectedContactsAdapter;
    private NewInviteContactsAdapter newInviteContactsAdapter;

    private ConcatAdapter concatAdapter;

    private ContactsViewModel contactsViewModel;
    private InviteContactsViewModel viewModel;

    private DeviceAvatarLoader deviceAvatarLoader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_my_network);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(R.string.my_contacts_title);

        RecyclerView rv = findViewById(R.id.invite_rv);
        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(lm);

        deviceAvatarLoader = new DeviceAvatarLoader(this);

        connectedContactsAdapter = new ConnectedContactsAdapter(() -> ViewMyNetworkActivity.this);
        newInviteContactsAdapter = new NewInviteContactsAdapter();
        newInviteContactsAdapter.setSendingEnabled(true);
        newInviteContactsAdapter.setParent(new NewInviteContactsAdapter.InviteContactsAdapterParent() {
            @Override
            public void onInvite(@NonNull Contact contact) {
                sendInvite(contact);
            }

            @Override
            public void onFiltered(@NonNull CharSequence constraint, @NonNull List<Contact> contacts) {
            }

            @Override
            public DeviceAvatarLoader getDeviceAvatarLoader() {
                return deviceAvatarLoader;
            }
        });
        concatAdapter = new ConcatAdapter(connectedContactsAdapter, newInviteContactsAdapter);
        rv.setAdapter(concatAdapter);

        contactsViewModel = new ViewModelProvider(this).get(ContactsViewModel.class);
        contactsViewModel.contactList.getLiveData().observe(this, contacts -> {
            if (contacts == null) {
                return;
            }
            connectedContactsAdapter.setContacts(contacts);
        });

        viewModel = new ViewModelProvider(this).get(InviteContactsViewModel.class);
        viewModel.getContactList().observe(this, contacts -> {
            if (contacts != null) {
                List<Contact> newContacts = new ArrayList<>(contacts);
                ListIterator<Contact> listIterator = newContacts.listIterator();
                while (listIterator.hasNext()) {
                    Contact c = listIterator.next();
                    if (c.userId != null) {
                        listIterator.remove();
                    }
                }
                newInviteContactsAdapter.setContacts(newContacts);
            } else {
                newInviteContactsAdapter.setContacts(new ArrayList<>());
            }
        });

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
                connectedContactsAdapter.getFilter().filter(s.toString());
                newInviteContactsAdapter.getFilter().filter(s.toString());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deviceAvatarLoader.destroy();
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
                InviteHelper.showInviteIqErrorDialog(this, nullableResult);
            } else {
                onSuccessfulInvite(contact);
            }
        });
    }

    private void onSuccessfulInvite(@NonNull Contact contact) {
        InviteHelper.sendInviteExternal(this, contact);
    }
}
