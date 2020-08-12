package com.halloapp.ui.groups;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.chat.ChatActivity;
import com.halloapp.ui.contacts.MultipleContactPickerActivity;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.CenterToast;

import java.util.ArrayList;
import java.util.List;

public class GroupInfoActivity extends HalloActivity {

    public static final int RESULT_CODE_LEAVE_GROUP = RESULT_FIRST_USER;

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

    private MemberAdapter adapter = new MemberAdapter();

    private TextView groupNameView;
    private RecyclerView membersView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        me = Me.getInstance();
        avatarLoader = AvatarLoader.getInstance(this);
        contactLoader = new ContactLoader(this);

        groupId = getIntent().getParcelableExtra(GROUP_ID);
        if (groupId == null) {
            finish();
            Log.e("GroupInfoActivity/onCreate must provide a group id");
            return;
        }

        setContentView(R.layout.activity_group_info);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle("");

        viewModel = new ViewModelProvider(this, new GroupViewModel.Factory(getApplication(), groupId)).get(GroupViewModel.class);

        membersView = findViewById(R.id.members);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        membersView.setLayoutManager(layoutManager);
        membersView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
        membersView.setAdapter(adapter);

        final View headerView = getLayoutInflater().inflate(R.layout.profile_header, membersView, false);
        adapter.addHeader(headerView);

        final View addMembersView = getLayoutInflater().inflate(R.layout.add_members_item, membersView, false);
        adapter.addHeader(addMembersView);
        addMembersView.setOnClickListener(v -> {
            startActivityForResult(MultipleContactPickerActivity.newPickerIntent(this, null, R.string.add_members), REQUEST_CODE_ADD_MEMBERS);
        });

        groupNameView = headerView.findViewById(R.id.name);

        viewModel.getChat().observe(this, chat -> {
            groupNameView.setText(chat.name);
        });

        viewModel.getMembers().observe(this, members -> {
            List<MemberInfo> otherMembers = new ArrayList<>();
            for (MemberInfo member : members) {
                if (!member.userId.rawId().equals(me.getUser())) {
                    otherMembers.add(member);
                }
            }
            adapter.submitMembers(otherMembers);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.group_info_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case R.id.leave: {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getBaseContext().getString(R.string.leave_group_confirmation));
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.yes, (dialog, which) -> leaveGroup());
                builder.setNegativeButton(R.string.no, null);
                builder.show();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_ADD_MEMBERS:
                if (resultCode == RESULT_OK && data != null) {
                    List<UserId> userIds = data.getParcelableArrayListExtra(MultipleContactPickerActivity.EXTRA_RESULT_SELECTED_IDS);

                    ProgressDialog addMembersDialog = ProgressDialog.show(this, null, getString(R.string.add_members_in_progress), true);
                    addMembersDialog.show();
                    viewModel.addMembers(userIds).observe(this, success -> {
                        addMembersDialog.cancel();
                        if (success == null || !success) {
                            CenterToast.show(getBaseContext(), R.string.failed_add_members);
                        }
                    });
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void leaveGroup() {
        ProgressDialog leaveGroupDialog = ProgressDialog.show(this, null, getString(R.string.leavel_group_in_progress), true);
        leaveGroupDialog.show();
        viewModel.leaveGroup().observe(this, success -> {
            leaveGroupDialog.cancel();
            if (success == null || !success) {
                CenterToast.show(getBaseContext(), R.string.failed_leave_group);
            } else {
                setResult(RESULT_CODE_LEAVE_GROUP);
                finish();
            }
        });
    }

    private class MemberAdapter extends RecyclerView.Adapter<ViewHolderWithLifecycle> {

        private static final int TYPE_MEMBER = 1;

        private List<View> headers = new ArrayList<>();
        private List<MemberInfo> members = new ArrayList<>();

        void addHeader(View header) {
            headers.add(header);
        }

        void submitMembers(List<MemberInfo> members) {
            this.members = members;
            notifyDataSetChanged();
        }

        private MemberInfo getItem(int position) {
            return position < headers.size() ? null : members.get(position - headers.size());
        }

        @Override
        public int getItemCount() {
            return headers.size() + members.size();
        }

        @Override
        public int getItemViewType(int position) {
            // negative view types are headers
            if (position < headers.size()) {
                return -position - 1;
            } else {
                return TYPE_MEMBER;
            }
        }

        @Override
        public long getItemId(int position) {
            return position < headers.size() ? -position : Preconditions.checkNotNull(getItem(position)).rowId;
        }

        @Override
        public @NonNull ViewHolderWithLifecycle onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType < 0) {
                return new HeaderViewHolder(headers.get(-viewType - 1));
            } else {
                final @LayoutRes int layoutRes = R.layout.group_member;
                final View layout = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
                return new MemberViewHolder(layout);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolderWithLifecycle holder, int position) {
            if (holder instanceof MemberViewHolder) {
                ((MemberViewHolder)holder).bindTo(Preconditions.checkNotNull(getItem(position)));
            }
        }
    }

    private static class HeaderViewHolder extends ViewHolderWithLifecycle {

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private class MemberViewHolder extends ViewHolderWithLifecycle {

        private View itemView;
        private ImageView avatar;
        private TextView name;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);

            this.itemView = itemView;
            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.name);
        }

        void bindTo(@NonNull MemberInfo member) {
            contactLoader.load(name, member.userId, false);
            avatarLoader.load(avatar, member.userId);
            itemView.setOnClickListener(v -> {
                Context context = getBaseContext();
                String[] options = new String[] {
                        context.getString(R.string.view_profile),
                        context.getString(R.string.message),
                        context.getString(R.string.group_remove_member), // TODO(jack): ensure user is admin
                };
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
            viewModel.removeMember(userId).observe(this, success -> {
                removeMemberDialog.cancel();
                if (success == null || !success) {
                    CenterToast.show(getBaseContext(), R.string.failed_remove_member);
                }
            });
        }
    }
}
