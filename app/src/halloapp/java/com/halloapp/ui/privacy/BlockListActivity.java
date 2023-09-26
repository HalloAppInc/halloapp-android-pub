package com.halloapp.ui.privacy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.id.UserId;
import com.halloapp.permissions.PermissionUtils;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.contacts.ContactsActivity;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class BlockListActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_CHOOSE_BLOCKED_CONTACT = 0;

    private BlockListViewModel viewModel;

    private BlocklistAdapter adapter;

    private AvatarLoader avatarLoader;

    private final List<UserId> blockedUsers = new ArrayList<>();

    private View emptyContainer;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {
            case REQUEST_CHOOSE_BLOCKED_CONTACT:
                if (resultCode == RESULT_OK && data != null) {
                    Contact selectedContact = data.getParcelableExtra(ContactsActivity.RESULT_SELECTED_CONTACT);
                    if (selectedContact != null) {
                        blockContact(selectedContact);
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_blocklist);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(getResources().getDimension(R.dimen.action_bar_elevation));
        }

        avatarLoader = AvatarLoader.getInstance();
        viewModel = new ViewModelProvider(this).get(BlockListViewModel.class);

        RecyclerView recyclerView = findViewById(R.id.block_list_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BlocklistAdapter();
        recyclerView.setAdapter(adapter);

        emptyContainer = findViewById(R.id.empty);

        viewModel.getBlockList().observe(this, this::setBlockedContacts);
    }

    private void setBlockedContacts(@Nullable List<Contact> contacts) {
        blockedUsers.clear();
        if (contacts != null) {
            for (Contact contact : contacts) {
                blockedUsers.add(contact.userId);
            }
        }
        emptyContainer.setVisibility(blockedUsers.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.setBlockedContacts(contacts);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Drawable icon = Preconditions.checkNotNull(ContextCompat.getDrawable(this, R.drawable.ic_add));
        icon.setTint(ContextCompat.getColor(this, R.color.primary_text));
        menu.add(Menu.NONE, 0, Menu.NONE, R.string.menu_item_block_user).setIcon(icon).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    private void pickContactToBlock() {
        startActivityForResult(ContactsActivity.createBlocklistContactPicker(this, blockedUsers), REQUEST_CHOOSE_BLOCKED_CONTACT);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 0) {
            pickContactToBlock();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void unblockContact(@NonNull Contact contact) {
        if (contact.userId == null) {
            Log.e("BlockListActivity/unblockContact tried to unblock a contact with null userId");
            return;
        }
        final String name = contact.getDisplayName();
        ProgressDialog unblockDialog = ProgressDialog.show(this, null, getString(R.string.unblocking_user_in_progress, name), true);
        unblockDialog.show();
        viewModel.unblockContact(contact.userId).observe(this, success -> {
            if (success == null) {
                return;
            }
            unblockDialog.cancel();
            if (success) {
                SnackbarHelper.showInfo(this, getString(R.string.unblocking_user_successful, name));
            } else {
                SnackbarHelper.showWarning(this, getString(R.string.unblocking_user_failed_check_internet, contact.getDisplayName()));
            }
        });
    }

    private void blockContact(@NonNull Contact contact) {
        if (contact.userId == null) {
            Log.e("BlockListActivity/blockContact tried to block a contact with null userId");
            return;
        }
        final String name = contact.getDisplayName();
        ProgressDialog blockDialog = ProgressDialog.show(this, null, getString(R.string.blocking_user_in_progress, name), true);
        blockDialog.show();
        viewModel.blockContact(contact.userId).observe(this, success -> {
            if (success == null) {
                return;
            }
            blockDialog.cancel();
            if (success) {
                SnackbarHelper.showInfo(this, getString(R.string.blocking_user_successful, contact.getDisplayName()));
            } else {
                SnackbarHelper.showWarning(this, getString(R.string.blocking_user_failed_check_internet, contact.getDisplayName()));
            }
        });
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_CHOOSE_BLOCKED_CONTACT) {
            pickContactToBlock();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    private class ContactViewHolder extends RecyclerView.ViewHolder {
        final private ImageView avatarView;
        final private TextView nameView;
        final private TextView usernameView;

        private Contact contact;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.avatar);
            nameView = itemView.findViewById(R.id.name);
            usernameView = itemView.findViewById(R.id.username);
            itemView.setOnClickListener(v -> {
                if (contact == null || contact.userId == null) {
                    return;
                }
                final Context context = itemView.getContext();
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(context.getString(R.string.unblock_user_confirmation, contact.getDisplayName()));
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.unblock, (dialog, which) -> unblockContact(contact));
                builder.setNegativeButton(R.string.cancel, null);
                builder.show();
            });
        }

        void bindTo(@NonNull Contact contact) {
            this.contact = contact;
            avatarLoader.load(avatarView, Preconditions.checkNotNull(contact.userId));
            nameView.setText(contact.getDisplayName());
            usernameView.setText(contact.getUsername());
        }
    }

    private class BlocklistAdapter extends RecyclerView.Adapter<ContactViewHolder> {

        private List<Contact> blockedContacts;

        void setBlockedContacts(@Nullable List<Contact> blockedContacts) {
            this.blockedContacts = blockedContacts;
            notifyDataSetChanged();
        }

        @Override
        public @NonNull
        ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ContactViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.block_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
            holder.bindTo(blockedContacts.get(position));
        }

        @Override
        public int getItemCount() {
            return blockedContacts == null ? 0 : blockedContacts.size();
        }
    }
}
