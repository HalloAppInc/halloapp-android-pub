package com.halloapp.katchup;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.id.UserId;
import com.halloapp.katchup.avatar.KAvatarLoader;
import com.halloapp.proto.server.BasicUserProfile;
import com.halloapp.ui.HalloBottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class RelevantFollowersBottomSheetDialogFragment extends HalloBottomSheetDialogFragment {

    private final KAvatarLoader kAvatarLoader = KAvatarLoader.getInstance();
    private final MutualUsersAdapter adapter = new MutualUsersAdapter();

    public RelevantFollowersBottomSheetDialogFragment(@NonNull List<BasicUserProfile> users) {
        adapter.setItems(users);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.relevant_followers_bottom_sheet, container, false);

        RecyclerView listView = root.findViewById(R.id.recycler_view);
        listView.setLayoutManager(new LinearLayoutManager(requireContext()));
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        return root;
    }

    private void onUserSelected(@NonNull UserId profileUserId) {
        Intent intent = ViewKatchupProfileActivity.viewProfile(requireContext(), profileUserId);
        startActivity(intent);

        dismiss();
    }

    public class MutualUserViewHolder extends RecyclerView.ViewHolder {

        private final ImageView avatarView;
        private final TextView nameView, usernameView, mutualCountView;

        public MutualUserViewHolder(@NonNull View itemView) {
            super(itemView);

            avatarView = itemView.findViewById(R.id.avatar);
            nameView = itemView.findViewById(R.id.name);
            usernameView = itemView.findViewById(R.id.username);
            mutualCountView = itemView.findViewById(R.id.mutuals);
        }

        public void bindTo(@NonNull BasicUserProfile profile) {
            UserId profileUserId = new UserId(Long.toString(profile.getUid()));

            nameView.setText(profile.getName());
            usernameView.setText("@" + profile.getUsername());
            mutualCountView.setText(String.valueOf(profile.getNumMutualFollowing()));

            kAvatarLoader.load(avatarView, profileUserId);

            itemView.setOnClickListener(v -> onUserSelected(profileUserId));
        }
    }

    public class MutualUsersAdapter extends RecyclerView.Adapter<MutualUserViewHolder> {

        private List<BasicUserProfile> users = new ArrayList<>();

        @SuppressLint("NotifyDataSetChanged")
        public void setItems(List<BasicUserProfile> users) {
            this.users = users;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MutualUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mutual_item, parent, false);
            return new MutualUserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MutualUserViewHolder holder, int position) {
            holder.bindTo(users.get(position));
        }

        @Override
        public int getItemCount() {
            return users != null ? users.size() : 0;
        }
    }
}
