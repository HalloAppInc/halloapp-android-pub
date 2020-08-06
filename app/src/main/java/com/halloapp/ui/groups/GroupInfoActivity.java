package com.halloapp.ui.groups;

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
import com.halloapp.content.ContentDb;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.chat.ChatActivity;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.CenterToast;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.groups.GroupsApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupInfoActivity extends HalloActivity {

    private static final String GROUP_ID = "group_id";

    public static Intent viewGroup(@NonNull Context context, @NonNull GroupId groupId) {
        Intent intent = new Intent(context, GroupInfoActivity.class);
        intent.putExtra(GROUP_ID, groupId);
        return intent;
    }

    private Me me;
    private GroupsApi groupsApi;
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
        groupsApi = GroupsApi.getInstance(this);
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

        membersView = findViewById(R.id.members);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        membersView.setLayoutManager(layoutManager);
        membersView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
        membersView.setAdapter(adapter);

        final View headerView = getLayoutInflater().inflate(R.layout.profile_header, membersView, false);
        adapter.addHeader(headerView);

        groupNameView = headerView.findViewById(R.id.name);

        final GroupViewModel viewModel = new ViewModelProvider(this, new GroupViewModel.Factory(getApplication(), groupId)).get(GroupViewModel.class);

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

    private void leaveGroup() {
        groupsApi.leaveGroup(groupId)
                .onResponse(response -> {
                    // TODO(jack): should exit from this and parent activity
                })
                .onError(err -> {
                    Log.e("Failed to leave group", err);
                    CenterToast.show(getBaseContext(), R.string.failed_remove_member);
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
            groupsApi.addRemoveMembers(groupId, null, Collections.singletonList(userId))
                    .onResponse(success -> {
                        if (!success) {
                            removalFailure(null);
                        }
                    })
                    .onError(this::removalFailure);
        }

        private void removalFailure(Exception err) {
            Log.e("Failed to remove member", err);
            CenterToast.show(getBaseContext(), R.string.failed_remove_member);
        }
    }
}
