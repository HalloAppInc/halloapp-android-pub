package com.halloapp.ui.groups;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.GroupId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class GroupInfoActivity extends HalloActivity {

    private static final String GROUP_ID = "group_id";

    public static Intent viewGroup(@NonNull Context context, @NonNull GroupId groupId) {
        Intent intent = new Intent(context, GroupInfoActivity.class);
        intent.putExtra(GROUP_ID, groupId);
        return intent;
    }

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

        private ImageView avatar;
        private TextView name;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);

            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.name);
        }

        void bindTo(@NonNull MemberInfo member) {
            contactLoader.load(name, member.userId);
            avatarLoader.load(avatar, member.userId);
        }
    }
}
