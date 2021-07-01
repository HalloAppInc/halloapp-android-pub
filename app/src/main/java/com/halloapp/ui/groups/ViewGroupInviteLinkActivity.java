package com.halloapp.ui.groups;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.halloapp.contacts.ContactLoader;
import com.halloapp.groups.GroupInfo;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.UserId;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.SystemUiVisibility;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.CenterToast;

import java.util.ArrayList;
import java.util.List;

public class ViewGroupInviteLinkActivity extends HalloActivity {

    private ViewGroupInviteLinkViewModel viewModel;

    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();
    private ContactLoader contactLoader;

    private ContactsAdapter contactsAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contactLoader = new ContactLoader();

        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this));

        String code = tryParseInviteLink(getIntent().getData());

        if (code == null) {
            finish();
            Log.e("ViewGroupInviteLinkActivity/onCreate no invite link code");
            return;
        }

        viewModel = new ViewModelProvider(this, new ViewGroupInviteLinkViewModel.Factory(getApplication(), code)).get(ViewGroupInviteLinkViewModel.class);

        setContentView(R.layout.activity_view_invite_link);

        TextView groupNameTv = findViewById(R.id.group_name);
        TextView groupDescriptionTv = findViewById(R.id.group_description);
        ImageView groupAvatarView = findViewById(R.id.group_avatar);

        View progressContainer = findViewById(R.id.progress_container);
        TextView progressMessage = findViewById(R.id.progress_text);

        TextView errorMessage = findViewById(R.id.error_msg);

        View linkPreviewContainer = findViewById(R.id.link_preview_container);

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
                if (result) {
                    startActivity(ViewGroupFeedActivity.viewFeed(this, viewModel.getInvitePreview().getValue().groupInfo.groupId));
                    finish();
                } else {
                    CenterToast.show(this, R.string.invite_link_failed_to_join);
                    finish();
                }
            });
        });
        View cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(v -> {
            finish();
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
                List<UserId> userIds = new ArrayList<>();
                for (MemberInfo memberInfo : groupInfo.members) {
                    if (memberInfo.userId.isMe()) {
                        startActivity(ViewGroupFeedActivity.viewFeed(this, groupInfo.groupId));
                        finish();
                        return;
                    }
                    userIds.add(memberInfo.userId);
                }
                joinGroup.setVisibility(View.VISIBLE);
                linkPreviewContainer.setVisibility(View.VISIBLE);
                progressContainer.setVisibility(View.INVISIBLE);
                groupNameTv.setText(groupInfo.name);
                groupDescriptionTv.setVisibility(TextUtils.isEmpty(groupInfo.description) ? View.GONE : View.VISIBLE);
                groupDescriptionTv.setText(groupInfo.description);
                avatarLoader.load(groupAvatarView, groupInfo.groupId, groupInfo.avatar);
                participantHeader.setText(getResources().getQuantityString(R.plurals.group_feed_members, groupInfo.members.size(), groupInfo.members.size()));
                contactsAdapter.setContactList(userIds);
            } else {
                progressContainer.setVisibility(View.INVISIBLE);
                errorMessage.setVisibility(View.VISIBLE);
                errorMessage.setText(R.string.invite_link_load_failed);
            }
        });
    }

    private class ContactViewHolder extends RecyclerView.ViewHolder {

        ImageView avatar;
        TextView name;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);

            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.name);
        }

        void bindTo(@NonNull UserId userId) {
            avatarLoader.load(avatar, userId, false);
            contactLoader.load(name, userId, new ViewDataLoader.Displayer<TextView, Contact>() {
                @Override
                public void showResult(@NonNull TextView view, @Nullable Contact result) {
                    if (result != null) {
                        view.setText(result.getShortName());
                    }
                }

                @Override
                public void showLoading(@NonNull TextView view) {
                    view.setText("");
                }
            });
        }
    }

    private class ContactsAdapter extends RecyclerView.Adapter<ContactViewHolder> {

        private List<UserId> contactList;

        public void setContactList(@Nullable List<UserId> contactList) {
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
        if (uri.getHost().toLowerCase().startsWith("invite.")) {
            return uri.getLastPathSegment();
        }
        if (!"invite".equalsIgnoreCase(uri.getLastPathSegment())) {
            return null;
        }
        return uri.getQueryParameter("g");
    }
}
