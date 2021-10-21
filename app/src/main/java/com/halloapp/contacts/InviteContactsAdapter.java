package com.halloapp.contacts;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.util.FilterUtils;
import com.halloapp.util.IntentUtils;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InviteContactsAdapter extends RecyclerView.Adapter<InviteContactsAdapter.ContactViewHolder> implements FastScrollRecyclerView.SectionedAdapter, Filterable {

    private List<Contact> contacts = new ArrayList<>();
    private List<Contact> filteredContacts;
    private CharSequence filterText;
    private List<String> filterTokens;

    private boolean sendingEnabled;
    private boolean showHeader;

    private InviteContactsAdapterParent listener;

    private static final int TYPE_CONTACT = 1;
    private static final int TYPE_INVITE_VIA_LINK = 2;

    public interface InviteContactsAdapterParent {
        void onInvite(@NonNull Contact contact);
        void onFiltered(@NonNull CharSequence constraint, @NonNull List<Contact> contacts);
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

    public void setShowHeader(boolean showHeader) {
        this.showHeader = showHeader;
    }

    public void setParent(@Nullable InviteContactsAdapterParent parent) {
        this.listener = parent;
    }

    @Override
    public @NonNull
    ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_CONTACT) {
            return new ContactViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_invite_item, parent, false));
        } else {
            return new InviteLinkViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_invite_link_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        position -= 1;
        if (holder instanceof InviteLinkViewHolder) {
            ((InviteLinkViewHolder)holder).bind(showHeader);
            return;
        }
        if (position < getFilteredContactsCount()) {
            holder.bindTo(filteredContacts.get(position), filterTokens);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_INVITE_VIA_LINK;
        }
        return TYPE_CONTACT;
    }

    @Override
    public int getItemCount() {
        return getFilteredContactsCount() + 1;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        if (filteredContacts == null || position >= filteredContacts.size()) {
            return "";
        }
        final String name = filteredContacts.get(position).getDisplayName();
        if (TextUtils.isEmpty(name)) {
            return "";
        }
        final int codePoint = name.codePointAt(0);
        return Character.isAlphabetic(codePoint) ? new String(Character.toChars(codePoint)).toUpperCase(Locale.getDefault()) : "#";
    }

    @Override
    public Filter getFilter() {
        return new ContactsFilter(contacts);
    }

    private int getFilteredContactsCount() {
        return filteredContacts == null ? 0 : filteredContacts.size();
    }


    class InviteLinkViewHolder extends ContactViewHolder {

        final View container;
        final View header;
        final TextView linkView;

        InviteLinkViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.invite_container);
            header = itemView.findViewById(R.id.invite_header);
            linkView = itemView.findViewById(R.id.download_link);
            container.setOnClickListener(v -> {
                container.getContext().startActivity(IntentUtils.createShareDlIntent());
            });
            linkView.setText(Constants.DOWNLOAD_LINK_URL);
        }

        void bind(boolean showHeader) {
            header.setVisibility(showHeader ? View.VISIBLE : View.GONE);
        }
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {

        final private TextView nameView;
        final private TextView phoneView;
        final private TextView captionView;
        final private View inviteView;

        private Contact contact;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            captionView = itemView.findViewById(R.id.potential_friends);
            nameView = itemView.findViewById(R.id.name);
            phoneView = itemView.findViewById(R.id.phone);
            inviteView = itemView.findViewById(R.id.add_btn);
            itemView.setOnClickListener(v -> {
                if (contact != null) {
                    if (listener != null) {
                        listener.onInvite(contact);
                    }
                }
            });
        }

        void bindTo(@NonNull Contact contact, List<String> filterTokens) {
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
            } else if (contact.numPotentialFriends > 0) {
                captionView.setText(captionView.getContext().getResources().getQuantityString(R.plurals.friends_on_halloapp, (int) contact.numPotentialFriends, (int) contact.numPotentialFriends));
                captionView.setVisibility(View.VISIBLE);
            } else {
                captionView.setVisibility(View.GONE);
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
            final List<Contact> filteredContacts = (List<Contact>) results.values;
            if (listener != null) {
                listener.onFiltered(constraint, filteredContacts);
            }
            setFilteredContacts(filteredContacts, constraint);
        }
    }
}
