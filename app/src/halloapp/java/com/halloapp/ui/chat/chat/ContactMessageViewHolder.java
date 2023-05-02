package com.halloapp.ui.chat.chat;

import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.R;
import com.halloapp.content.Message;
import com.halloapp.proto.clients.Contact;
import com.halloapp.proto.clients.ContactCard;
import com.halloapp.ui.contacts.ViewContactCardActivity;
import com.halloapp.util.ContactCardUtils;
import com.halloapp.util.logs.Log;

public class ContactMessageViewHolder extends MessageViewHolder {

    private final View previewContainer;
    private final ImageView avatarView;
    private final TextView nameView;

    private final View contactActionContainer;

    ContactMessageViewHolder(@NonNull View itemView, @NonNull MessageViewHolderParent parent) {
        super(itemView, parent);

        previewContainer = itemView.findViewById(R.id.preview_container);
        avatarView = itemView.findViewById(R.id.avatar);
        nameView = itemView.findViewById(R.id.contact_name);

        contactActionContainer = itemView.findViewById(R.id.contact_action);
        contactActionContainer.setVisibility(View.GONE);
        contactActionContainer.setOnClickListener(v -> {

        });
        previewContainer.setOnClickListener(v -> {
            ContactCard contactCard = ContactCardUtils.deserializeContactCard(message.text);
            if (contactCard != null) {
                v.getContext().startActivity(ViewContactCardActivity.viewContactCard(v.getContext(), contactCard));
            } else {
                Log.e("ContactMessageViewHolder/onClick contact card is not valid");
            }
        });
    }

    @Override
    protected void fillView(@NonNull Message message, boolean changed) {
        if (changed) {
            ContactCard contactCard = ContactCardUtils.deserializeContactCard(message.text);
            bindContactCard(contactCard);
        }
    }

    private void bindContactCard(@Nullable ContactCard contactCard) {
        if (contactCard != null) {
            Contact contact = contactCard.getContacts(0);
            nameView.setText(contact.getName());
            if (!contact.getPhoto().isEmpty()) {
                byte[] data = contact.getPhoto().toByteArray();
                avatarView.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
            } else {
                avatarView.setImageResource(R.drawable.avatar_person);
            }
        } else {
            avatarView.setImageResource(R.drawable.avatar_person);
            nameView.setText("");
        }
    }
}
