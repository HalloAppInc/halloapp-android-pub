package com.halloapp.katchup;

import android.app.Application;
import android.graphics.Outline;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.RelationshipInfo;
import com.halloapp.id.UserId;
import com.halloapp.ui.HalloFragment;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;

import java.util.ArrayList;
import java.util.List;

public class SettingsBlockedFragment  extends HalloFragment {
    private BlockedUsersViewModel viewModel;
    private final BlockedUsersAdapter adapter = new BlockedUsersAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_blocked, container, false);

        RecyclerView listView = root.findViewById(R.id.recycler_view);
        listView.setLayoutManager(new LinearLayoutManager(requireContext()));
        listView.setAdapter(adapter);

        View emptyView = root.findViewById(android.R.id.empty);

        viewModel = new ViewModelProvider(this).get(BlockedUsersViewModel.class);
        viewModel.users.observe(getViewLifecycleOwner(), users -> {
            emptyView.setVisibility(users != null && users.size() > 0 ? View.GONE : View.VISIBLE);
            adapter.setItems(users);
        });

        return root;
    }

    public class BlockedUserViewHolder extends RecyclerView.ViewHolder {

        TextView nameView, usernameView, unblockBtn;

        public BlockedUserViewHolder(@NonNull View itemView) {
            super(itemView);

            nameView = itemView.findViewById(R.id.name);
            usernameView = itemView.findViewById(R.id.username);
            unblockBtn = itemView.findViewById(R.id.unblock);

            unblockBtn.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), view.getHeight());
                }
            });
            unblockBtn.setClipToOutline(true);
        }

        public void bindTo(RelationshipInfo info) {
            nameView.setText(info.name);
            usernameView.setText("@" + info.username);

            unblockBtn.setOnClickListener(v -> {
                viewModel.unblockUser(info.userId).observe(getViewLifecycleOwner(), success -> {
                    if (!success) {
                        SnackbarHelper.showWarning(requireActivity(), R.string.failed_to_unblock);
                    }
                });
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

        private final RelationshipApi relationshipApi = RelationshipApi.getInstance();

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

        public LiveData<Boolean> unblockUser(UserId userId) {
            MutableLiveData<Boolean> result = new MutableLiveData<>();

            relationshipApi.requestUnblockUser(userId).onResponse(success -> {
                if (Boolean.FALSE.equals(success)) {
                    Log.w("Unblock failed for " + userId);
                }
                result.postValue(success);
            }).onError(err -> {
                Log.e("Failed to unblock user", err);
                result.postValue(false);
            });

            return result;
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
