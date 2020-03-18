package com.halloapp.ui.messages;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.posts.LoadPostsHistoryWorker;
import com.halloapp.ui.AdapterWithLifecycle;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.chat.ChatActivity;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.xmpp.Connection;

import java.util.List;

public class MessagesFragment extends Fragment {

    private final ContactsAdapter adapter = new ContactsAdapter();
    private final AvatarLoader avatarLoader = AvatarLoader.getInstance(Connection.getInstance(), getContext());

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        final View root = inflater.inflate(R.layout.fragment_messages, container, false);
        final RecyclerView chatsView = root.findViewById(R.id.chats);
        final View emptyView = root.findViewById(android.R.id.empty);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        chatsView.setLayoutManager(layoutManager);
        chatsView.setAdapter(adapter);

        final MessagesViewModel viewModel = new ViewModelProvider(this).get(MessagesViewModel.class);
        viewModel.contactsList.getLiveData().observe(this, contacts -> {
            adapter.setContacts(contacts);
            emptyView.setVisibility(contacts.size() == 0 ? View.VISIBLE : View.GONE);
        });

        chatsView.addOnScrollListener(new ActionBarShadowOnScrollListener((AppCompatActivity) Preconditions.checkNotNull(getActivity())));

        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.messages_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case R.id.refresh_contacts: {
                ContactsSync.getInstance(Preconditions.checkNotNull(getContext())).startContactsSync(true);
                LoadPostsHistoryWorker.loadPostsHistory(getContext()); // TODO (ds): remove
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private class ContactsAdapter extends AdapterWithLifecycle<ContactsAdapter.ViewHolder> {

        private List<Contact> contacts;

        void setContacts(List<Contact> contacts) {
            this.contacts = contacts;
            notifyDataSetChanged();
        }

        @Override
        public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bindTo(contacts.get(position));
        }

        @Override
        public int getItemCount() {
            return contacts == null ? 0 : contacts.size();
        }

        class ViewHolder extends ViewHolderWithLifecycle {

            final ImageView avatarView;
            final TextView nameView;
            final TextView infoView;

            Contact contact;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                avatarView = itemView.findViewById(R.id.avatar);
                nameView = itemView.findViewById(R.id.name);
                infoView = itemView.findViewById(R.id.info);
                itemView.setOnClickListener(v -> {
                    if (contact.userId != null) {
                        startActivity(new Intent(getContext(), ChatActivity.class).putExtra(ChatActivity.EXTRA_CHAT_ID, contact.userId.rawId()));
                    }
                });
            }

            void bindTo(Contact contact) {
                this.contact = contact;
                avatarView.setImageResource(R.drawable.avatar_person); // TODO (ds): load contact image
                avatarLoader.load(avatarView, contact.userId);
                nameView.setText(contact.getDisplayName());
                infoView.setText(contact.getInternationalPhone());
            }
        }
    }
}