package com.halloapp.ui.groups;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.groups.GroupInfo;
import com.halloapp.groups.GroupsSync;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.GroupId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.InitialSyncActivity;
import com.halloapp.RegistrationRequestActivity;
import com.halloapp.ui.SystemUiVisibility;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.CenterToast;

import java.util.List;
import java.util.Locale;

public class ViewGroupInviteLinkActivity extends HalloActivity {

    private ViewGroupInviteLinkViewModel viewModel;

    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();

    private ContactsAdapter contactsAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this));

        String code = tryParseInviteLink(getIntent().getData());

        if (code == null) {
            finish();
            Log.e("ViewGroupInviteLinkActivity/onCreate no invite link code");
            return;
        }

        viewModel = new ViewModelProvider(this, new ViewGroupInviteLinkViewModel.Factory(getApplication(), code)).get(ViewGroupInviteLinkViewModel.class);

        viewModel.registrationStatus.getLiveData().observe(this, checkResult -> {
            if (checkResult == null) {
                return;
            }
            if (!checkResult.registered) {
                Log.i("ViewGroupInviteLinkActivity/checkRegistration: not registered");
                Intent regIntent = RegistrationRequestActivity.register(getBaseContext(), checkResult.lastSyncTime);
                startActivity(regIntent);
                overridePendingTransition(0, 0);
                finish();
            } else if (checkResult.lastSyncTime <= 0) {
                Log.i("ViewGroupInviteLinkActivity/checkRegistration: not synced");
                startActivity(new Intent(getBaseContext(), InitialSyncActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });

        setContentView(R.layout.activity_view_invite_link);

        TextView groupNameTv = findViewById(R.id.group_name);
        TextView groupDescriptionTv = findViewById(R.id.group_description);
        ImageView groupAvatarView = findViewById(R.id.group_avatar);

        View progressContainer = findViewById(R.id.progress_container);
        TextView progressMessage = findViewById(R.id.progress_text);

        TextView errorMessage = findViewById(R.id.error_msg);

        View linkPreviewContainer = findViewById(R.id.link_preview_container);

        Button cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(v -> finish());

        View joinGroup = findViewById(R.id.join_group);
        joinGroup.setOnClickListener(v -> {
            progressContainer.setVisibility(View.VISIBLE);
            linkPreviewContainer.setVisibility(View.INVISIBLE);
            joinGroup.setVisibility(View.GONE);
            progressMessage.setText(R.string.invite_link_joining);
            viewModel.joinGroup().observe(this, result -> {
                if (result == null) {
                    return;
                }
                if (result.isSuccess()) {
                    finish();
                    startActivity(ViewGroupFeedActivity.viewFeed(this, new GroupId(result.getResult().getGid())));
                } else {
                    Log.w("Failed to join group by invite link: " + result.getError());
                    progressContainer.setVisibility(View.INVISIBLE);
                    errorMessage.setVisibility(View.VISIBLE);
                    cancel.setText(getString(R.string.ok));
                    if ("max_group_size".equals(result.getError())) {
                        errorMessage.setText(getString(R.string.invite_link_failed_to_join_full));
                    } else if ("invalid_invite".equals(result.getError())) {
                        errorMessage.setText(getString(R.string.invite_link_failed_to_join_invalid));
                    } else if ("already_member".equals(result.getError())) {
                        GroupsSync.getInstance(this).forceGroupSync();
                        startActivity(ViewGroupFeedActivity.viewFeed(this, viewModel.getInvitePreview().getValue().groupInfo.groupId));
                    } else if ("admin_removed".equals(result.getError())) {
                        errorMessage.setText(getString(R.string.invite_link_failed_to_join_removed));
                    } else {
                        CenterToast.show(this, R.string.invite_link_failed_to_join);
                        finish();
                    }
                }
            });
        });


        TextView participantHeader = findViewById(R.id.participants_header);
        RecyclerView participantView = findViewById(R.id.participants_rv);
        participantView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        contactsAdapter = new ContactsAdapter();
        participantView.setAdapter(contactsAdapter);

        initBottomSheetBehavior();

        viewModel.getInvitePreview().observe(this, inviteLinkResult -> {
            if (inviteLinkResult.groupInfo != null) {
                GroupInfo groupInfo = inviteLinkResult.groupInfo;
                for (MemberInfo memberInfo : groupInfo.members) {
                    if (memberInfo.userId.isMe()) {
                        startActivity(ViewGroupFeedActivity.viewFeed(this, groupInfo.groupId));
                        finish();
                        return;
                    }
                }
                joinGroup.setVisibility(View.VISIBLE);
                linkPreviewContainer.setVisibility(View.VISIBLE);
                progressContainer.setVisibility(View.INVISIBLE);
                groupNameTv.setText(groupInfo.name);
                groupDescriptionTv.setVisibility(TextUtils.isEmpty(groupInfo.description) ? View.GONE : View.VISIBLE);
                groupDescriptionTv.setText(groupInfo.description);
                avatarLoader.load(groupAvatarView, groupInfo.groupId, groupInfo.avatar);
                participantHeader.setText(getResources().getQuantityString(R.plurals.group_feed_members, groupInfo.members.size(), groupInfo.members.size()));
                contactsAdapter.setContactList(inviteLinkResult.contactList);
            } else {
                progressContainer.setVisibility(View.INVISIBLE);
                errorMessage.setVisibility(View.VISIBLE);
                errorMessage.setText(R.string.invite_link_load_failed);
            }
        });
    }

    private class ContactViewHolder extends RecyclerView.ViewHolder {

        final ImageView avatar;
        final TextView name;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);

            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.name);
        }

        void bindTo(@NonNull Contact contact) {
            if (contact.userId == null) {
                avatarLoader.cancel(avatar);
                name.setText("");
                return;
            }
            avatarLoader.load(avatar, contact.userId, false);
            name.setText(contact.getShortName());
        }
    }

    private class ContactsAdapter extends RecyclerView.Adapter<ContactViewHolder> {

        private List<Contact> contactList;

        public void setContactList(@Nullable List<Contact> contactList) {
            this.contactList = contactList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ContactViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.invite_participant_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
            if (contactList == null) {
                return;
            }
            holder.bindTo(contactList.get(position));
        }

        @Override
        public int getItemCount() {
            return contactList == null ? 0 : contactList.size();
        }
    }

    private void initBottomSheetBehavior() {
        BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.container));
        // Expanded by default
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setSkipCollapsed(true);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    finish();
                    overridePendingTransition(0, 0);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    @Nullable
    private String tryParseInviteLink(@Nullable Uri uri) {
        if (uri == null || uri.getHost() == null) {
            return null;
        }
        if (uri.getHost().toLowerCase(Locale.ROOT).startsWith("invite.")) {
            return uri.getLastPathSegment();
        }
        if (!"invite".equalsIgnoreCase(uri.getLastPathSegment())) {
            return null;
        }
        return uri.getQueryParameter("g");
    }
}
