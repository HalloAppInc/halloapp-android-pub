package com.halloapp.ui.groups;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.HeaderFooterAdapter;
import com.halloapp.ui.SystemUiVisibility;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.chat.ChatActivity;
import com.halloapp.ui.contacts.MultipleContactPickerActivity;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.groups.MemberElement;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupInfoActivity extends HalloActivity {

    public static final int RESULT_CODE_EXIT_CHAT = RESULT_FIRST_USER;

    private static final String GROUP_ID = "group_id";

    private static final int REQUEST_CODE_ADD_MEMBERS = 1;

    public static Intent viewGroup(@NonNull Context context, @NonNull GroupId groupId) {
        Intent intent = new Intent(context, GroupInfoActivity.class);
        intent.putExtra(GROUP_ID, groupId);
        return intent;
    }

    private GroupViewModel viewModel;

    private Me me;
    private AvatarLoader avatarLoader;
    private ContactLoader contactLoader;

    private GroupId groupId;

    private final MemberAdapter adapter = new MemberAdapter();

    private TextView groupNameView;
    private RecyclerView membersView;
    private ImageView avatarView;
    private View addMembersView;
    private View leaveGroup;

    private MenuItem deleteMenuItem;
    private MenuItem leaveMenuItem;
    private MenuItem inviteLinkMenuItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        me = Me.getInstance();
        avatarLoader = AvatarLoader.getInstance();
        contactLoader = new ContactLoader();

        groupId = getIntent().getParcelableExtra(GROUP_ID);
        if (groupId == null) {
            finish();
            Log.e("GroupInfoActivity/onCreate must provide a group id");
            return;
        }

        setContentView(R.layout.activity_group_info);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        viewModel = new ViewModelProvider(this, new GroupViewModel.Factory(getApplication(), groupId)).get(GroupViewModel.class);

        membersView = findViewById(R.id.members);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        membersView.setLayoutManager(layoutManager);
        membersView.setAdapter(adapter);

        avatarView = findViewById(R.id.avatar);
        avatarLoader.load(avatarView, groupId, false);

        addMembersView = findViewById(R.id.add_members);
        addMembersView.setOnClickListener(v -> {
            List<GroupViewModel.GroupMember> members = viewModel.getMembers().getValue();
            List<UserId> excludeIds = new ArrayList<>();
            if (members != null) {
                for (GroupViewModel.GroupMember member : members) {
                    excludeIds.add(member.memberInfo.userId);
                }
            }
            int maxGroupSize = ServerProps.getInstance().getMaxGroupSize() - excludeIds.size();
            startActivityForResult(MultipleContactPickerActivity.newPickerIntentExclude(this, excludeIds, maxGroupSize, R.string.add_members, false), REQUEST_CODE_ADD_MEMBERS);
        });

        View.OnClickListener openEditGroupListener = v -> {
            if (getChatIsActive()) {
                startActivity(EditGroupActivity.openEditGroup(this, groupId));
            } else {
                SnackbarHelper.showWarning(avatarView, R.string.failed_no_longer_member);
            }
        };
        groupNameView = findViewById(R.id.name);
        avatarView.setOnClickListener(openEditGroupListener);

        leaveGroup = findViewById(R.id.leave_group);
        View nameContainer = findViewById(R.id.name_container);
        View bgContainer = findViewById(R.id.group_background);
        CircleImageView bgColorPreview = findViewById(R.id.bg_color_preview);

        TextView groupBgDesc = findViewById(R.id.group_background_description);
        TextView memberTitle = findViewById(R.id.member_title);

        if (ServerProps.getInstance().getIsInternalUser()) {
            bgContainer.setVisibility(View.VISIBLE);
        } else {
            bgContainer.setVisibility(View.GONE);
        }

        bgContainer.setOnClickListener(v -> {
            if (getChatIsActive()) {
                Intent i = GroupBackgroundActivity.newIntent(this, groupId);
                startActivity(i);
            } else {
                SnackbarHelper.showWarning(bgContainer, R.string.failed_no_longer_member);
            }
        });
        nameContainer.setOnClickListener(openEditGroupListener);

        leaveGroup.setOnClickListener(v -> {
            askLeaveGroup();
        });

        viewModel.getChat().observe(this, chat -> {
            groupNameView.setText(chat.name);
            groupBgDesc.setText(chat.theme == 0 ? R.string.group_background_default : R.string.group_background_color);
            GroupTheme theme = GroupTheme.getTheme(chat.theme);
            bgColorPreview.setImageDrawable(new ColorDrawable(ContextCompat.getColor(this, theme.bgColor)));
        });

        viewModel.getMembers().observe(this, members -> {
            adapter.submitMembers(members);
            memberTitle.setText(getString(R.string.group_info_members, members.size()));
        });

        viewModel.getUserIsAdmin().observe(this, userIsAdmin -> updateVisibilities());
        viewModel.getChatIsActive().observe(this, chatIsActive -> updateVisibilities());
    }

    private void updateVisibilities() {
        boolean userIsAdmin = getUserIsAdmin();
        boolean chatIsActive = getChatIsActive();
        boolean both = userIsAdmin && chatIsActive;
        addMembersView.setVisibility(both ? View.VISIBLE : View.GONE);
        if (deleteMenuItem != null) {
            deleteMenuItem.setVisible(both);
        }
        if (inviteLinkMenuItem != null) {
            inviteLinkMenuItem.setVisible(both && ServerProps.getInstance().getGroupInviteLinksEnabled());
        }
        if (leaveMenuItem != null) {
            leaveMenuItem.setVisible(chatIsActive);
        }
        if (leaveGroup != null) {
            leaveGroup.setVisibility(chatIsActive ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        avatarLoader.load(avatarView, groupId, false);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.group_info_menu, menu);
        deleteMenuItem = menu.findItem(R.id.delete);
        leaveMenuItem = menu.findItem(R.id.leave);
        inviteLinkMenuItem = menu.findItem(R.id.invite_link);

        updateVisibilities();
        return true;
    }

    private boolean getUserIsAdmin() {
        Boolean userIsAdmin = viewModel.getUserIsAdmin().getValue();
        return userIsAdmin != null && userIsAdmin;
    }

    private boolean getChatIsActive() {
        Boolean chatIsActive = viewModel.getChatIsActive().getValue();
        return chatIsActive != null && chatIsActive;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.leave) {
            askLeaveGroup();
            return true;
        } else if (item.getItemId() == R.id.invite_link) {
            startActivity(GroupInviteLinkActivity.newIntent(this, groupId));
        }
        return super.onOptionsItemSelected(item);
    }

    private void askLeaveGroup() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getBaseContext().getString(R.string.leave_group_confirmation));
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.yes, (dialog, which) -> leaveGroup());
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {
            case REQUEST_CODE_ADD_MEMBERS:
                if (resultCode == RESULT_OK && data != null) {
                    List<UserId> userIds = data.getParcelableArrayListExtra(MultipleContactPickerActivity.EXTRA_RESULT_SELECTED_IDS);

                    ProgressDialog addMembersDialog = ProgressDialog.show(this, null, getString(R.string.add_members_in_progress), true);
                    addMembersDialog.show();
                    viewModel.addMembers(userIds).observe(this, success -> {
                        addMembersDialog.cancel();
                        if (success == null || !success) {
                            SnackbarHelper.showWarning(this, R.string.failed_add_members);
                        }
                    });
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void leaveGroup() {
        ProgressDialog leaveGroupDialog = ProgressDialog.show(this, null, getString(R.string.leave_group_in_progress), true);
        leaveGroupDialog.show();
        viewModel.leaveGroup().observe(this, success -> {
            leaveGroupDialog.cancel();
            if (success == null || !success) {
                SnackbarHelper.showWarning(this, R.string.failed_leave_group);
            } else {
                setResult(RESULT_CODE_EXIT_CHAT);
                finish();
            }
        });
    }

    private class MemberAdapter extends HeaderFooterAdapter<GroupViewModel.GroupMember> {

        public MemberAdapter() {
            super(new HeaderFooterAdapter.HeaderFooterAdapterParent() {
                @NonNull
                @Override
                public Context getContext() {
                    return membersView.getContext();
                }

                @NonNull
                @Override
                public ViewGroup getParentViewGroup() {
                    return (ViewGroup) findViewById(android.R.id.content);
                }
            });
        }

        private static final int TYPE_MEMBER = 1;

        void submitMembers(List<GroupViewModel.GroupMember> members) {
            submitItems(members);
        }

        @Override
        public long getIdForItem(@NonNull GroupViewModel.GroupMember groupMember) {
            return groupMember.memberInfo.rowId;
        }

        @Override
        public int getViewTypeForItem(@NonNull GroupViewModel.GroupMember memberInfo) {
            return TYPE_MEMBER;
        }

        @NonNull
        @Override
        public ViewHolderWithLifecycle createViewHolderForViewType(@NonNull ViewGroup parent, int viewType) {
            final @LayoutRes int layoutRes = R.layout.group_member;
            final View layout = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
            return new MemberViewHolder(layout);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolderWithLifecycle holder, int position) {
            if (holder instanceof MemberViewHolder) {
                ((MemberViewHolder)holder).bindTo(Preconditions.checkNotNull(getItem(position)));
            }
        }
    }

    private class MemberViewHolder extends ViewHolderWithLifecycle {

        private final View itemView;
        private final ImageView avatar;
        private final TextView name;
        private final TextView admin;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);

            this.itemView = itemView;
            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.name);
            admin = itemView.findViewById(R.id.admin);
        }

        void bindTo(@NonNull GroupViewModel.GroupMember groupMember) {
            MemberInfo member = groupMember.memberInfo;
            admin.setVisibility(MemberElement.Type.ADMIN.equals(member.type) ? View.VISIBLE : View.GONE);
            if (member.userId.isMe() || member.userId.rawId().equals(me.getUser())) {
                contactLoader.cancel(name);
                name.setText(R.string.me);
                avatarLoader.load(avatar, UserId.ME, false);
                itemView.setOnClickListener(null);
                itemView.setClickable(false);
                return;
            }

            contactLoader.load(name, member.userId, false);
            avatarLoader.load(avatar, member.userId, false);
            itemView.setOnClickListener(v -> {
                Context context = getBaseContext();
                List<String> optionsList = new ArrayList<>();
                optionsList.add(context.getString(R.string.view_profile));
                if (groupMember.contact.addressBookName != null) {
                    optionsList.add(context.getString(R.string.message));
                }
                if (getUserIsAdmin() && getChatIsActive()) {
                    optionsList.add(context.getString(R.string.group_remove_member));
                    optionsList.add(context.getString(MemberElement.Type.ADMIN.equals(member.type) ? R.string.group_demote_from_admin : R.string.group_promote_to_admin));
                }
                String[] options = new String[optionsList.size()];
                optionsList.toArray(options);
                AlertDialog dialog =
                        new AlertDialog.Builder(v.getContext())
                        .setItems(options, (d, which) -> {
                            UserId userId = member.userId;
                            switch (which) {
                                case 0: {
                                    viewProfile(userId);
                                    break;
                                }
                                case 1: {
                                    openChat(userId);
                                    break;
                                }
                                case 2: {
                                    removeMember(userId);
                                    break;
                                }
                                case 3: {
                                    promoteDemoteAdmin(member);
                                    break;
                                }
                            }
                        }).create();
                dialog.show();
            });
        }

        private void viewProfile(UserId userId) {
            startActivity(ViewProfileActivity.viewProfile(getBaseContext(), userId));
        }

        private void openChat(UserId userId) {
            final Intent contentIntent = new Intent(getBaseContext(), ChatActivity.class);
            contentIntent.putExtra(ChatActivity.EXTRA_CHAT_ID, userId);
            startActivity(contentIntent);
        }

        private void removeMember(UserId userId) {
            ProgressDialog removeMemberDialog = ProgressDialog.show(GroupInfoActivity.this, null, getString(R.string.remove_member_in_progress), true);
            removeMemberDialog.show();
            viewModel.removeMember(userId).observe(GroupInfoActivity.this, success -> {
                removeMemberDialog.cancel();
                if (success == null || !success) {
                    SnackbarHelper.showWarning(GroupInfoActivity.this, R.string.failed_remove_member);
                }
            });
        }

        private void promoteDemoteAdmin(MemberInfo member) {
            boolean isAdmin = MemberElement.Type.ADMIN.equals(member.type);
            ProgressDialog promoteDemoteDialog = ProgressDialog.show(GroupInfoActivity.this, null, getString(isAdmin ? R.string.demote_admin_in_progress : R.string.promote_admin_in_progress), true);
            promoteDemoteDialog.show();
            if (isAdmin) {
                viewModel.demoteAdmin(member.userId).observe(GroupInfoActivity.this, success -> {
                    promoteDemoteDialog.cancel();
                    if (success == null || !success) {
                        SnackbarHelper.showWarning(GroupInfoActivity.this, R.string.failed_demote_admin);
                    }
                });
            } else {
                viewModel.promoteAdmin(member.userId).observe(GroupInfoActivity.this, success -> {
                    promoteDemoteDialog.cancel();
                    if (success == null || !success) {
                        SnackbarHelper.showWarning(GroupInfoActivity.this, R.string.failed_promote_admin);
                    }
                });
            }
        }
    }
}
