package com.halloapp.katchup;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.RelationshipInfo;
import com.halloapp.katchup.avatar.KAvatarLoader;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.BgWorkers;

import java.util.ArrayList;
import java.util.List;

public class BlockedUsersActivity extends HalloActivity {

    public static Intent open(Context context) {
        return new Intent(context, BlockedUsersActivity.class);
    }

    BlockedUsersViewModel viewModel;
    BlockedUsersAdapter adapter = new BlockedUsersAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_users);

        View prev = findViewById(R.id.prev);
        prev.setOnClickListener(v -> finish());

        RecyclerView listView = findViewById(R.id.recycler_view);
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setAdapter(adapter);

        View emptyView = findViewById(android.R.id.empty);

        viewModel = new ViewModelProvider(this).get(BlockedUsersViewModel.class);
        viewModel.users.observe(this, users -> {
            emptyView.setVisibility(users != null && users.size() > 0 ? View.GONE : View.VISIBLE);
            adapter.setItems(users);
        });
    }

    public class BlockedUserViewHolder extends RecyclerView.ViewHolder {

        ImageView avatarView;
        TextView nameView, usernameView;

        public BlockedUserViewHolder(@NonNull View itemView) {
            super(itemView);

            avatarView = itemView.findViewById(R.id.avatar);
            nameView = itemView.findViewById(R.id.name);
            usernameView = itemView.findViewById(R.id.username);
        }

        public void bindTo(RelationshipInfo info) {
            KAvatarLoader.getInstance().load(avatarView, info.userId, info.avatarId);
            nameView.setText(info.name);
            usernameView.setText("@" + info.username);

            itemView.setOnClickListener(v -> {
                startActivity(ViewKatchupProfileActivity.viewProfile(itemView.getContext(), info.userId));
            });
        }
    }

    public class BlockedUsersAdapter extends RecyclerView.Adapter<BlockedUserViewHolder> {

        private List<RelationshipInfo> users = new ArrayList<>();

        public void setItems(List<RelationshipInfo> users) {
            this.users = users;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public BlockedUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blocked_user_item, parent, false);
            return new BlockedUserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BlockedUserViewHolder holder, int position) {
            holder.bindTo(users.get(position));
        }

        @Override
        public int getItemCount() {
            return users != null ? users.size() : 0;
        }
    }

    public static class BlockedUsersViewModel extends AndroidViewModel {
        public final MutableLiveData<List<RelationshipInfo>> users = new MutableLiveData<>();

        private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
            @Override
            public void onRelationshipsChanged() {
                loadBlockedUsers();
            }
        };

        public BlockedUsersViewModel(@NonNull Application application) {
            super(application);
            ContactsDb.getInstance().addObserver(contactsObserver);
            loadBlockedUsers();
        }

        @Override
        protected void onCleared() {
            ContactsDb.getInstance().removeObserver(contactsObserver);
        }


        private void loadBlockedUsers() {
            BgWorkers.getInstance().execute(() -> {
                List<RelationshipInfo> blocked = ContactsDb.getInstance().getRelationships(RelationshipInfo.Type.BLOCKED);
                users.postValue(blocked);
            });
        }
    }
}
