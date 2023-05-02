package com.halloapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.id.UserId;
import com.halloapp.ui.chat.chat.ChatActivity;
import com.halloapp.ui.profile.ViewProfileActivity;

public class ContactMenuBottomSheetDialogFragment extends HalloBottomSheetDialogFragment {

    private static final String ARG_CONTACT = "contact";
    private static final String ARG_POST_ID = "post_id";

    public static ContactMenuBottomSheetDialogFragment newInstance(@NonNull Contact contact, @NonNull String postId) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_CONTACT, contact);
        args.putString(ARG_POST_ID, postId);
        ContactMenuBottomSheetDialogFragment dialogFragment = new ContactMenuBottomSheetDialogFragment();
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle args = requireArguments();
        Contact contact = args.getParcelable(ARG_CONTACT);
        String postId = args.getString(ARG_POST_ID);

        final View view = inflater.inflate(R.layout.contact_menu_bottom_sheet, container, false);

        final TextView contactName = view.findViewById(R.id.contact_name);
        contactName.setText(contact.getDisplayName());

        final View message = view.findViewById(R.id.message);
        final View viewProfile = view.findViewById(R.id.view_profile);
        if (TextUtils.isEmpty(contact.addressBookName)) {
            message.setVisibility(View.GONE);
        } else {
            message.setVisibility(View.VISIBLE);
        }
        message.setOnClickListener(v ->{
            final Intent intent = ChatActivity.open(requireContext(), contact.userId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (postId != null) {
                intent.putExtra(ChatActivity.EXTRA_REPLY_POST_ID, postId);
                intent.putExtra(ChatActivity.EXTRA_REPLY_POST_SENDER_ID, UserId.ME);
                intent.putExtra(ChatActivity.EXTRA_REPLY_POST_MEDIA_INDEX, 0);
            }
            requireContext().startActivity(intent);
            dismiss();
        });
        viewProfile.setOnClickListener(v -> {
            requireContext().startActivity(ViewProfileActivity.viewProfile(requireContext(), contact.userId));
            dismiss();
        });
        return view;
    }
}
