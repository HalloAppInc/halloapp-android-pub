package com.halloapp.ui.contacts;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.protobuf.ByteString;
import com.halloapp.R;
import com.halloapp.proto.clients.Contact;
import com.halloapp.proto.clients.ContactAddress;
import com.halloapp.proto.clients.ContactCard;
import com.halloapp.proto.clients.ContactEmail;
import com.halloapp.proto.clients.ContactPhone;
import com.halloapp.util.ContactCardUtils;

import java.util.ArrayList;
import java.util.List;

public class ContactCardAdapter extends RecyclerView.Adapter<ContactCardAdapter.ViewHolder> {

    private Bitmap profilePhoto;
    private byte[] photoBytes;
    private String name;

    private List<Item> items;

    public ContactCardAdapter() {
        this.allowSelection = false;
    }

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_FIELD = 1;

    private boolean allowSelection;

    public ContactCardAdapter(boolean allowSelection) {
        this.allowSelection = allowSelection;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_contact_card_header, parent, false));
        }
        return new FieldViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_contact_card_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_HEADER;
        }
        return VIEW_TYPE_FIELD;
    }

    @Override
    public int getItemCount() {
        return 1 + (items == null ? 0 : items.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind(int position) {}
    }

    public class HeaderViewHolder extends ViewHolder {

        private final TextView nameView;
        private final ImageView avatarView;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);

            nameView = itemView.findViewById(R.id.name);
            avatarView = itemView.findViewById(R.id.avatar);
        }

        public void bind(int position) {
            nameView.setText(name);
            if (profilePhoto == null) {
                avatarView.setImageResource(R.drawable.avatar_person);
            } else {
                avatarView.setImageBitmap(profilePhoto);
            }
        }
    }

    public class FieldViewHolder extends ViewHolder {

        private final TextView labelView;
        private final TextView dataView;
        private final CheckBox checkBox;

        private int position;

        public FieldViewHolder(@NonNull View itemView) {
            super(itemView);

            labelView = itemView.findViewById(R.id.label);
            dataView = itemView.findViewById(R.id.data);
            checkBox = itemView.findViewById(R.id.checkbox);

            if (allowSelection) {
                itemView.setOnClickListener(v -> {
                    if (items != null) {
                        Item item = items.get(position - 1);
                        item.selected = !item.selected;
                        notifyItemChanged(position);
                    }
                });
                checkBox.setVisibility(View.VISIBLE);
            } else {
                checkBox.setVisibility(View.GONE);
            }

        }

        public void bind(int position) {
            int listPos = position - 1;
            if (items != null) {
                Item item = items.get(listPos);
                labelView.setText(item.getLabel(labelView.getResources()));
                dataView.setText(item.getData());
                checkBox.setChecked(item.selected);
            }
            this.position = position;
        }
    }

    private abstract static class Item {
        boolean selected = true;

        public abstract CharSequence getLabel(Resources resources);
        public abstract String getData();
    }

    private static class EmailItem extends Item {

        ContactEmail email;

        public EmailItem(ContactEmail email) {
            this.email = email;
        }

        @Override
        public CharSequence getLabel(Resources resources) {
            return resources.getString(R.string.email_label);
        }

        @Override
        public String getData() {
            return email.getAddress();
        }
    }

    private static class AddressItem extends Item {
        ContactAddress address;

        public AddressItem(ContactAddress address) {
            this.address = address;
        }

        @Override
        public CharSequence getLabel(Resources resources) {
            return ContactCardUtils.getAddressLabel(resources, address.getLabel());
        }

        @Override
        public String getData() {
            return address.getAddress();
        }
    }

    private static class TelephoneItem extends Item {
        ContactPhone telephone;

        public TelephoneItem(ContactPhone telephone) {
            this.telephone = telephone;
        }

        @Override
        public CharSequence getLabel(Resources resources) {
            return ContactCardUtils.getTelephoneLabel(resources, telephone.getLabel());
        }

        @Override
        public String getData() {
            return PhoneNumberUtils.formatNumber(telephone.getNumber());
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setContactCard(ContactCard contactCard) {
        if (contactCard.getContactsCount() == 0) {
            profilePhoto = null;
            name = null;
            items = new ArrayList<>();
            return;
        }
        Contact contact = contactCard.getContacts(0);
        if (contact.getPhoto().isEmpty()) {
            profilePhoto = null;
            photoBytes = null;
        } else {
            byte[] data = contact.getPhoto().toByteArray();
            photoBytes = data;
            profilePhoto = BitmapFactory.decodeByteArray(data,0,data.length);
        }
        name = contact.getName();
        items = new ArrayList<>();
        for (ContactPhone telephone : contact.getNumbersList()) {
            items.add(new TelephoneItem(telephone));
        }
        for (ContactEmail email : contact.getEmailsList()) {
            items.add(new EmailItem(email));
        }
        for (ContactAddress address : contact.getAddressesList()) {
            items.add(new AddressItem(address));
        }
        notifyDataSetChanged();
    }

    @NonNull
    public ContactCard serializeContactCard() {
        ContactCard.Builder builder = ContactCard.newBuilder();
        Contact.Builder contactBuilder = Contact.newBuilder();
        contactBuilder.setName(name);
        if (photoBytes != null) {
            contactBuilder.setPhoto(ByteString.copyFrom(photoBytes));
        }
        for (Item item : items) {
            if (!item.selected) {
                continue;
            }
            if (item instanceof EmailItem) {
                contactBuilder.addEmails(((EmailItem) item).email);
            } else if (item instanceof AddressItem) {
                contactBuilder.addAddresses(((AddressItem) item).address);
            } else if (item instanceof TelephoneItem) {
                contactBuilder.addNumbers(((TelephoneItem) item).telephone);
            }
        }
        builder.addContacts(contactBuilder.build());
        return builder.build();
    }
}
