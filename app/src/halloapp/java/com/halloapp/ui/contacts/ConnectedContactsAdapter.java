package com.halloapp.ui.contacts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.id.UserId;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.FilterUtils;

import java.util.HashSet;
import java.util.List;

public class ConnectedContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private List<Contact> contacts;

    private List<Contact> filteredContacts;
    private CharSequence filterText;
    private List<String> filterTokens;
    private final HashSet<UserId> selectedContacts = new HashSet<>();
    private final @DrawableRes int selectionIcon = R.drawable.ic_check;

    private static final int ITEM_CONNECTED_HEADER = 0;
    private static final int ITEM_CONTACT = 1;
    private static final int ITEM_EXPLANATION_FOOTER = 2;

    public interface Parent {
        @NonNull LifecycleOwner getLifecycleOwner();
    }

    private Parent parent;

    public ConnectedContactsAdapter(Parent parent) {
        this.parent = parent;
    }

    public void setContacts(@NonNull List<Contact> contacts) {
        this.contacts = contacts;
        getFilter().filter(filterText);
    }

    void setFilteredContacts(@NonNull List<Contact> contacts, CharSequence filterText) {
        this.filteredContacts = contacts;
        this.filterText = filterText;
        this.filterTokens = FilterUtils.getFilterTokens(filterText);
        notifyDataSetChanged();
    }

    private int getFilteredContactsCount() {
        return filteredContacts == null ? 0 : filteredContacts.size();
    }

    public HashSet<UserId> getSelectedFriends() {
        return selectedContacts;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewParent, int viewType) {
        switch (viewType) {
            case ITEM_CONTACT: {
                View view = LayoutInflater.from(viewParent.getContext()).inflate(R.layout.item_connected_contact, viewParent, false);
                return new ContactViewHolder(view, parent);
            }
            case ITEM_CONNECTED_HEADER: {
                View view = LayoutInflater.from(viewParent.getContext()).inflate(R.layout.item_connected_contacts_header, viewParent, false);
                return new HeaderViewHolder(view);
            }
            case ITEM_EXPLANATION_FOOTER: {
                View view = LayoutInflater.from(viewParent.getContext()).inflate(R.layout.item_connected_contacts_footer, viewParent, false);
                return new FooterViewHolder(view);
            }
        }
        throw new RuntimeException("invalid view holder type");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ContactViewHolder) {
            ContactViewHolder cv = (ContactViewHolder) holder;
            cv.bindContact(filteredContacts.get(position - 1), filterTokens);
        } else if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(contacts);
        } else if (holder instanceof FooterViewHolder) {
            ((FooterViewHolder) holder).bind(contacts);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ITEM_CONNECTED_HEADER;
        }
        if (filteredContacts == null || position > filteredContacts.size()) {
            return ITEM_EXPLANATION_FOOTER;
        }
        return ITEM_CONTACT;
    }

    @Override
    public int getItemCount() {
        return 2 + getFilteredContactsCount();
    }

    @Override
    public Filter getFilter() {
        return new ContactsFilter(contacts);
    }

    private class ContactsFilter extends FilterUtils.ItemFilter<Contact> {

        public ContactsFilter(@NonNull List<Contact> contacts) {
            super(contacts);
        }

        @Override
        protected String itemToString(Contact contact) {
            return contact.getDisplayName();
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            final List<Contact> filteredContacts = (List<Contact>) results.values;
            setFilteredContacts(filteredContacts, constraint);
        }
    }

    private class ContactViewHolder extends RecyclerView.ViewHolder {

        private ImageView avatarView;
        private TextView nameView;
        private TextView usernameView;

        private ImageView selectionView;

        private Contact contact;

        private Parent parent;

        public ContactViewHolder(@NonNull View itemView, Parent parent) {
            super(itemView);

            this.parent = parent;

            avatarView = itemView.findViewById(R.id.avatar);
            nameView = itemView.findViewById(R.id.name);
            usernameView = itemView.findViewById(R.id.username);

            selectionView = itemView.findViewById(R.id.selection_indicator);
            itemView.setOnClickListener(v -> {
                if (contact == null || contact.userId == null) {
                    return;
                }
                if (selectedContacts.contains(contact.userId)) {
                    selectedContacts.remove(contact.userId);
                } else {
                    selectedContacts.add(contact.userId);
                }
                updateSelectionIcon();
            });
        }

        private void updateSelectionIcon() {
            boolean selected = selectedContacts.contains(contact.userId);
            selectionView.setImageResource(selected ? selectionIcon : 0);
            selectionView.setVisibility(View.VISIBLE);
            selectionView.setSelected(selected);
        }

        public void bindContact(Contact contact, List<String> filterTokens) {
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
            usernameView.setText(contact.getUsername());
            if (contact.userId != null) {
                AvatarLoader.getInstance().load(avatarView, contact.userId);
            } else {
                AvatarLoader.getInstance().cancel(avatarView);
            }
        }
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView numConnectionsTitle;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);

            numConnectionsTitle = itemView.findViewById(R.id.num_connections);
        }

        public void bind(List<Contact> contacts) {
            String str;
            if (contacts == null || contacts.size() == 0) {
                str = numConnectionsTitle.getResources().getString(R.string.no_connections_on_hallo);
            } else {
                str = numConnectionsTitle.getResources().getQuantityString(R.plurals.friend_connections_on_hallo, contacts.size(), contacts.size());
            }
            numConnectionsTitle.setText(str);
        }
    }

    private static class FooterViewHolder extends RecyclerView.ViewHolder {

        private final TextView connectionsPostVisibility;

        public FooterViewHolder(@NonNull View itemView) {
            super(itemView);

            connectionsPostVisibility = itemView.findViewById(R.id.post_visibility_disclaimer);
        }

        public void bind(List<Contact> contacts) {
            String str;
            if (contacts == null || contacts.size() == 0) {
                str = connectionsPostVisibility.getResources().getString(R.string.no_connections_on_hallo_explanation);
            } else {
                str = connectionsPostVisibility.getResources().getString(R.string.friend_connections_on_hallo_explanation);
            }
            connectionsPostVisibility.setText(str);
        }
    }
}
