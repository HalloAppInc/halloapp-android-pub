package com.halloapp.ui.groups;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.server.ExpiryInfo;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.HalloBottomSheetDialog;
import com.halloapp.ui.HeaderFooterAdapter;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.avatar.ViewAvatarActivity;
import com.halloapp.ui.chat.ChatActivity;
import com.halloapp.ui.contacts.MultipleContactPickerActivity;
import com.halloapp.ui.markdown.MarkdownUtils;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.DialogFragmentUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ClickableMovementMethod;
import com.halloapp.widget.LimitingTextView;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.groups.MemberElement;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupInfoActivity extends HalloActivity implements SelectGroupExpiryDialogFragment.Host {

    public static final int RESULT_CODE_EXIT_CHAT = RESULT_FIRST_USER;

    private static final String GROUP_ID = "group_id";

    private static final int REQUEST_CODE_ADD_MEMBERS = 1;

    public static Intent viewGroup(@NonNull Context context, @NonNull GroupId groupId) {
        Preconditions.checkNotNull(groupId);
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
    private View changeAvatarView;
    private View addMembersView;
    private View inviteLinkView;
    private View addDivider;
    private View leaveGroup;

    private MenuItem leaveMenuItem;

    private int selectedExpiry = -1;

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

        addDivider = findViewById(R.id.add_divider);

        avatarView = findViewById(R.id.avatar);
        avatarLoader.load(avatarView, groupId, false);

        changeAvatarView = findViewById(R.id.change_avatar);
        changeAvatarView.setOnClickListener(v -> startActivity(EditGroupActivity.openEditGroup(this, groupId)));
        viewModel.getChatIsActive().observe(this, active -> changeAvatarView.setVisibility(Boolean.TRUE.equals(active) ? View.VISIBLE : View.GONE));

        inviteLinkView = findViewById(R.id.invite_link_container);
        inviteLinkView.setOnClickListener(v -> startActivity(GroupInviteLinkActivity.newIntent(this, groupId)));

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
            Intent addIntent = MultipleContactPickerActivity.newPickerIntentAddOnly(this, excludeIds, maxGroupSize, R.string.add_members, R.string.action_add);
            String inviteLink = viewModel.getGroupInviteLink();
            if (inviteLink != null) {
                addIntent.putExtra(MultipleContactPickerActivity.EXTRA_INVITE_LINK, inviteLink);
            }
            startActivityForResult(addIntent, REQUEST_CODE_ADD_MEMBERS);
        });

        View.OnClickListener openEditGroupListener = v -> {
            if (getChatIsActive()) {
                startActivity(EditGroupActivity.openEditGroup(this, groupId));
            } else {
                SnackbarHelper.showWarning(avatarView, R.string.failed_no_longer_member);
            }
        };
        groupNameView = findViewById(R.id.name);
        avatarView.setOnClickListener(v -> ViewAvatarActivity.viewAvatarWithTransition(this, avatarView, groupId));

        leaveGroup = findViewById(R.id.leave_group);
        View descriptionContainer = findViewById(R.id.description_container);
        LimitingTextView descriptionTv = findViewById(R.id.description);
        View descriptionPlaceholder = findViewById(R.id.description_placeholder);
        View nameContainer = findViewById(R.id.name_container);
        View bgContainer = findViewById(R.id.group_background);
        CircleImageView bgColorPreview = findViewById(R.id.bg_color_preview);

        TextView groupExpiryDesc = findViewById(R.id.group_expiry_description);
        ImageView groupExpiryIcon = findViewById(R.id.group_expiry_icon);
        View expirationContainer = findViewById(R.id.expiration_container);
        expirationContainer.setOnClickListener(v -> {
            DialogFragmentUtils.showDialogFragmentOnce(SelectGroupExpiryDialogFragment.newInstance(selectedExpiry), getSupportFragmentManager());
        });

        if (ServerProps.getInstance().getIsInternalUser()) {
            expirationContainer.setVisibility(View.VISIBLE);
        } else {
            expirationContainer.setVisibility(View.GONE);
        }
        TextView groupBgDesc = findViewById(R.id.group_background_description);
        TextView memberTitle = findViewById(R.id.member_title);
        bgContainer.setVisibility(View.VISIBLE);
        bgContainer.setOnClickListener(v -> {
            if (getChatIsActive()) {
                Intent i = GroupBackgroundActivity.newIntent(this, groupId);
                startActivity(i);
            } else {
                SnackbarHelper.showWarning(bgContainer, R.string.failed_no_longer_member);
            }
        });
        nameContainer.setOnClickListener(openEditGroupListener);
        descriptionTv.setOnReadMoreListener((view, limit) -> false);
        ClickableMovementMethod.apply(descriptionTv);
        descriptionContainer.setOnClickListener(v -> {
            if (getChatIsActive()) {
                startActivity(EditGroupDescriptionActivity.openEditGroupDescription(this, groupId));
            } else {
                SnackbarHelper.showWarning(avatarView, R.string.failed_no_longer_member);
            }
        });

        leaveGroup.setOnClickListener(v -> askLeaveGroup());

        viewModel.getGroup().observe(this, group -> {
            if (group == null) {
                Log.w("GroupInfoActivity got null chat for " + groupId);
                return;
            }
            groupNameView.setText(group.name);
            groupBgDesc.setText(group.theme == 0 ? R.string.group_background_default : R.string.group_background_color);
            GroupTheme theme = GroupTheme.getTheme(group.theme);
            bgColorPreview.setImageDrawable(new ColorDrawable(ContextCompat.getColor(this, theme.bgColor)));
            if (TextUtils.isEmpty(group.groupDescription)) {
                descriptionTv.setVisibility(View.GONE);
                descriptionPlaceholder.setVisibility(View.VISIBLE);
            } else {
                descriptionTv.setVisibility(View.VISIBLE);
                descriptionPlaceholder.setVisibility(View.GONE);
                descriptionTv.setText(MarkdownUtils.formatMarkdown(descriptionTv.getContext(), group.groupDescription));
            }
            if (group.expiryInfo != null) {
                switch (group.expiryInfo.getExpiryType()) {
                    case NEVER:
                        groupExpiryDesc.setText(R.string.expiration_never);
                        groupExpiryIcon.setImageResource(R.drawable.ic_content_no_expiry);
                        selectedExpiry = SelectGroupExpiryDialogFragment.OPTION_NEVER;
                        break;
                    case EXPIRES_IN_SECONDS:
                        long seconds = group.expiryInfo.getExpiresInSeconds();
                        groupExpiryDesc.setText(TimeFormatter.formatExpirationDuration(this, (int) seconds));
                        groupExpiryIcon.setImageResource(R.drawable.ic_content_expiry);
                        if (seconds == Constants.SECONDS_PER_DAY) {
                            selectedExpiry = SelectGroupExpiryDialogFragment.OPTION_24_HOURS;
                        } else if (seconds == Constants.SECONDS_PER_DAY * 30) {
                            selectedExpiry = SelectGroupExpiryDialogFragment.OPTION_30_DAYS;
                        } else {
                            selectedExpiry = -1;
                        }
                        break;
                    default:
                        selectedExpiry = -1;
                }
            }
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
        addDivider.setVisibility(both ? View.VISIBLE : View.GONE);
        inviteLinkView.setVisibility(both ? View.VISIBLE : View.GONE);
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
        leaveMenuItem = menu.findItem(R.id.leave);

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
                finish();
            }
        });
    }

    @Override
    public void onExpirySelected(int selectedOption) {
        ProgressDialog changeExpiryDialog = ProgressDialog.show(this, null, getString(R.string.change_group_expiry_in_progress), true);
        changeExpiryDialog.show();
        ExpiryInfo expiryInfo = null;
        switch (selectedOption) {
            case SelectGroupExpiryDialogFragment.OPTION_24_HOURS:
                expiryInfo = ExpiryInfo.newBuilder().setExpiresInSeconds(Constants.SECONDS_PER_DAY).setExpiryType(ExpiryInfo.ExpiryType.EXPIRES_IN_SECONDS).build();
                break;
            case SelectGroupExpiryDialogFragment.OPTION_30_DAYS:
                expiryInfo = ExpiryInfo.newBuilder().setExpiresInSeconds(Constants.SECONDS_PER_DAY * 30).setExpiryType(ExpiryInfo.ExpiryType.EXPIRES_IN_SECONDS).build();
                break;
            case SelectGroupExpiryDialogFragment.OPTION_NEVER:
                expiryInfo = ExpiryInfo.newBuilder().setExpiryType(ExpiryInfo.ExpiryType.NEVER).build();
                break;
        }
        if (expiryInfo != null) {
            viewModel.changeExpiry(expiryInfo).observe(this, success -> {
                changeExpiryDialog.cancel();
                if (success == null || !success) {
                    SnackbarHelper.showWarning(this, R.string.group_expiry_change_failure);
                } else {
                    SnackbarHelper.showInfo(this, R.string.group_expiry_changed);
                }
            });
        } else {
            changeExpiryDialog.cancel();
        }
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
                    return findViewById(android.R.id.content);
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
            itemView.setOnClickListener(v -> showGroupMemberOptionsDialog(itemView.getContext(), groupMember));
        }

        private void viewProfile(UserId userId) {
            startActivity(ViewProfileActivity.viewProfile(getBaseContext(), userId));
        }

        private void openChat(UserId userId) {
            final Intent contentIntent = ChatActivity.open(GroupInfoActivity.this, userId);
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

        private void showGroupMemberOptionsDialog(Context context, GroupViewModel.GroupMember member) {
            final BottomSheetDialog dialog = new HalloBottomSheetDialog(context);
            dialog.setContentView(R.layout.group_info_member_bottom_sheet);

            TextView memberName = dialog.findViewById(R.id.member_name);
            TextView makeAdminText = dialog.findViewById(R.id.make_admin_text);
            View viewProfileLayout = Preconditions.checkNotNull(dialog.findViewById(R.id.view_profile));
            View messageMemberLayout = Preconditions.checkNotNull(dialog.findViewById(R.id.message));
            View makeAdminLayout = Preconditions.checkNotNull(dialog.findViewById(R.id.make_admin));
            View removeMemberLayout = Preconditions.checkNotNull(dialog.findViewById(R.id.remove_member));

            contactLoader.load(Preconditions.checkNotNull(memberName), member.memberInfo.userId, false);
            boolean allowMessage = member.contact.addressBookName != null;
            messageMemberLayout.setVisibility(allowMessage ? View.VISIBLE : View.GONE);

            if (getUserIsAdmin() && getChatIsActive()) {
                removeMemberLayout.setVisibility(View.VISIBLE);
                makeAdminLayout.setVisibility(View.VISIBLE);
                Preconditions.checkNotNull(makeAdminText).setText(getResources().getString(MemberElement.Type.ADMIN.equals(member.memberInfo.type) ? R.string.group_demote_from_admin : R.string.group_promote_to_admin));
            } else {
                removeMemberLayout.setVisibility(View.GONE);
                makeAdminLayout.setVisibility(View.GONE);
            }
            viewProfileLayout.setOnClickListener(v -> {
                viewProfile(member.memberInfo.userId);
                dialog.dismiss();
            });
            messageMemberLayout.setOnClickListener(v -> {
                openChat(member.memberInfo.userId);
                dialog.dismiss();
            });
            removeMemberLayout.setOnClickListener(v -> {
                removeMember(member.memberInfo.userId);
                dialog.dismiss();
            });
            makeAdminLayout.setOnClickListener(v -> {
                promoteDemoteAdmin(member.memberInfo);
                dialog.dismiss();
            });
            dialog.show();
        }
    }
}
