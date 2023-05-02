package com.halloapp.ui.contacts;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.ui.avatar.DeviceAvatarLoader;
import com.halloapp.util.FilterUtils;

import java.util.ArrayList;
import java.util.List;

public class NewInviteContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private List<Contact> contacts = new ArrayList<>();
    private List<Contact> filteredContacts;
    private CharSequence filterText;
    private List<String> filterTokens;

    private boolean sendingEnabled;

    private InviteContactsAdapterParent listener;

    private static final int TYPE_CONTACT = 1;
    private static final int TYPE_HEADER = 2;

    public interface InviteContactsAdapterParent {
        void onInvite(@NonNull Contact contact);
        void onFiltered(@NonNull CharSequence constraint, @NonNull List<Contact> contacts);
        DeviceAvatarLoader getDeviceAvatarLoader();
    }

    public void setContacts(@NonNull List<Contact> contacts) {
        this.contacts = contacts;
        getFilter().filter(filterText);
    }

    public void setFilteredContacts(@NonNull List<Contact> contacts, CharSequence filterText) {
        this.filteredContacts = contacts;
        this.filterText = filterText;
        this.filterTokens = FilterUtils.getFilterTokens(filterText);
        notifyDataSetChanged();
    }

    public void setSendingEnabled(boolean enabled) {
        sendingEnabled = enabled;
        notifyDataSetChanged();
    }

    public void setParent(@Nullable InviteContactsAdapterParent parent) {
        this.listener = parent;
    }

    @Override
    public @NonNull
    RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_CONTACT) {
            return new ContactViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_invite_item_v2, parent, false));
        } else {
            return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_invite_header, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int adapterPosition) {
        if (holder instanceof ContactViewHolder) {
            int position = adapterPosition - 1;
            if (position < getFilteredContactsCount()) {
                ((ContactViewHolder)holder).bindTo(filteredContacts.get(position), filterTokens);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        }
        return TYPE_CONTACT;
    }

    @Override
    public int getItemCount() {
        int contacts = getFilteredContactsCount();
        if (contacts == 0) {
            return 0;
        }
        return contacts + 1;
    }

    @Override
    public Filter getFilter() {
        return new ContactsFilter(contacts);
    }

    private int getFilteredContactsCount() {
        return filteredContacts == null ? 0 : filteredContacts.size();
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {

        final private TextView nameView;
        final private TextView phoneView;
        final private TextView captionView;
        final private View inviteView;
        private ImageView avatarView;

        private Contact contact;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            captionView = itemView.findViewById(R.id.potential_friends);
            nameView = itemView.findViewById(R.id.name);
            phoneView = itemView.findViewById(R.id.phone);
            inviteView = itemView.findViewById(R.id.add_btn);
            avatarView = itemView.findViewById(R.id.avatar);
            itemView.setOnClickListener(v -> {
                if (contact != null) {
                    if (listener != null) {
                        listener.onInvite(contact);
                    }
                }
            });
        }

        void bindTo(@NonNull Contact contact, List<String> filterTokens) {
            if (listener != null) {
                listener.getDeviceAvatarLoader().load(avatarView, contact.addressBookPhone);
            }
            boolean canSend = (sendingEnabled || contact.invited) && contact.userId == null;
            if (canSend) {
                itemView.setAlpha(1);
                itemView.setClickable(true);
            } else {
                itemView.setAlpha(0.54f);
                itemView.setClickable(false);
            }
            this.contact = contact;
            if (filterTokens != null && !filterTokens.isEmpty()) {
                final String name = contact.getDisplayName();
                CharSequence formattedName = FilterUtils.formatMatchingText(itemView.getContext(), name, filterTokens);

                if (formattedName != null) {
                    nameView.setText(formattedName);
                } else {
                    nameView.setText(name);
                }
            } else {
                nameView.setText(contact.getDisplayName());
            }
            if (contact.normalizedPhone == null) {
                phoneView.setText(R.string.invite_invalid_phone_number);
            } else {
                phoneView.setText(contact.getDisplayPhone());
            }
            if (contact.userId != null) {
                captionView.setVisibility(View.VISIBLE);
                captionView.setText(captionView.getContext().getString(R.string.invite_already_on_halloapp));
                inviteView.setVisibility(View.GONE);
            } else if (contact.numPotentialFriends > 0) {
                captionView.setText(captionView.getContext().getResources().getQuantityString(R.plurals.friends_on_halloapp, (int) contact.numPotentialFriends, (int) contact.numPotentialFriends));
                captionView.setVisibility(View.VISIBLE);
                inviteView.setVisibility(View.VISIBLE);
            } else {
                captionView.setVisibility(View.GONE);
                inviteView.setVisibility(View.VISIBLE);
            }

            inviteView.setEnabled(!contact.invited);
        }
    }

    private class ContactsFilter extends FilterUtils.ItemFilter<Contact> {

        ContactsFilter(@NonNull List<Contact> contacts) {
            super(contacts);
        }

        @Override
        protected String itemToString(Contact contact) {
            return contact.getDisplayName();
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            final List<Contact> tokenFilteredContacts = (List<Contact>) results.values;
            final List<Contact> bannedStringsRemoved = new ArrayList<>();
            for (Contact contact : tokenFilteredContacts) {
                boolean allowed = true;
                List<String> tokens = FilterUtils.getFilterTokens(itemToString(contact));
                if (TextUtils.isEmpty(constraint) && tokens != null) {
                    for (String token : Constants.BANNED_INVITE_SUGGEST_TOKENS) {
                        if (tokens.contains(token)) {
                            allowed = false;
                            break;
                        }
                    }
                }
                if (allowed) {
                    bannedStringsRemoved.add(contact);
                }
            }
            if (listener != null) {
                listener.onFiltered(constraint, bannedStringsRemoved);
            }
            setFilteredContacts(bannedStringsRemoved, constraint);
        }
    }
}
