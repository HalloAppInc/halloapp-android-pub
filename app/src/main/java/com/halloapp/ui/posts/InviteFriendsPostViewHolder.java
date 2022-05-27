package com.halloapp.ui.posts;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.avatar.DeviceAvatarLoader;
import com.halloapp.ui.invites.InviteContactsActivity;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.HorizontalSpaceDecoration;

import java.util.List;

public class InviteFriendsPostViewHolder extends ViewHolderWithLifecycle {

    private final InviteCardAdapter adapter;

    public interface Host {
        void sendInvite(Contact contact);
        DeviceAvatarLoader getAvatarLoader();
    }

    private Host host;

    private final LiveData<List<Contact>> inviteListLiveData;

    public InviteFriendsPostViewHolder(@NonNull LiveData<List<Contact>> inviteList, @NonNull View itemView, @Nullable Host host) {
        super(itemView);
        this.inviteListLiveData = inviteList;
        this.host = host;
        RecyclerView cardRv = itemView.findViewById(R.id.invite_card_rv);

        LinearLayoutManager lm = new LinearLayoutManager(cardRv.getContext(), LinearLayoutManager.HORIZONTAL, false);
        cardRv.setLayoutManager(lm);

        adapter = new InviteCardAdapter();
        cardRv.setAdapter(adapter);
        cardRv.addItemDecoration(new HorizontalSpaceDecoration(cardRv.getContext().getResources().getDimensionPixelSize(R.dimen.invite_card_spacing)));

    }

    @Override
    public void markAttach() {
        super.markAttach();
        if (this.inviteListLiveData != null) {
            inviteListLiveData.observe(this, adapter::setContacts);
        }
    }

    @Override
    public void markDetach() {
        super.markDetach();
        if (this.inviteListLiveData != null) {
            inviteListLiveData.removeObservers(this);
        }
    }

    private class InviteCardViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameView;
        private final TextView captionView;
        private final ImageView avatarView;

        private Contact contact;

        public InviteCardViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.invite_name);
            captionView = itemView.findViewById(R.id.invite_caption);
            avatarView = itemView.findViewById(R.id.invite_avatar);
            View closeButton = itemView.findViewById(R.id.close_btn);
            closeButton.setOnClickListener(v -> {
                if (contact != null) {
                    ContactsDb.getInstance().dismissSuggestedContact(contact);
                }
            });
            View inviteButton = itemView.findViewById(R.id.invite_button);
            inviteButton.setOnClickListener(v -> {
                if (contact != null && host != null) {
                    host.sendInvite(contact);
                }
            });
        }

        public void bind(Contact contact) {
            this.contact = contact;
            nameView.setText(contact.getDisplayName());
            captionView.setText(captionView.getContext().getResources().getQuantityString(R.plurals.friends_on_halloapp, (int) contact.numPotentialFriends, (int) contact.numPotentialFriends));
            if (host != null) {
                DeviceAvatarLoader loader = host.getAvatarLoader();
                if (loader != null) {
                    loader.load(avatarView, contact.addressBookPhone);
                }
            }
        }
    }

    private static class SearchCardViewHolder extends RecyclerView.ViewHolder {

        private final View searchButton;

        public SearchCardViewHolder(@NonNull View itemView) {
            super(itemView);

            searchButton = itemView.findViewById(R.id.search_button);
            searchButton.setOnClickListener(v -> {
                final Intent intent = new Intent(searchButton.getContext(), InviteContactsActivity.class);
                intent.putExtra(InviteContactsActivity.EXTRA_SHOW_KEYBOARD, true);
                searchButton.getContext().startActivity(intent);
            });
        }
    }

    private class InviteCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int CARD_CONTACT = 0;
        private static final int CARD_SEARCH = 1;

        private List<Contact> contacts;

        public InviteCardAdapter() {
            setHasStableIds(true);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == CARD_CONTACT) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.invite_card_item, parent, false);
                return new InviteCardViewHolder(view);
            } else {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.invite_card_search_item, parent, false);
                return new SearchCardViewHolder(view);
            }
        }

        public void setContacts(List<Contact> contacts) {
            Log.i("InviteFriendsPostViewHolder/setContacts contacts set count=" + (contacts == null ? "null" : contacts.size()));
            this.contacts = contacts;
            notifyDataSetChanged();
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof InviteCardViewHolder) {
                ((InviteCardViewHolder) holder).bind(contacts.get(position));
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position >= contacts.size()) {
                return CARD_SEARCH;
            }
            return CARD_CONTACT;
        }

        @Override
        public long getItemId(int position) {
            if (position >= contacts.size()) {
                return -1;
            } else {
                Contact contact = contacts.get(position);
                return contact.getAddressBookId();
            }
        }

        @Override
        public int getItemCount() {
            return contacts == null ? 0 : (contacts.size() + 1);
        }
    }
}
