package com.halloapp.ui;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.UserId;
import com.halloapp.ui.chat.ChatActivity;
import com.halloapp.ui.privacy.HideFuturePostsDialogFragment;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.DialogFragmentUtils;

public class ContactMenuBottomSheetDialogFragment extends BottomSheetDialogFragment {

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
        final View hidePosts = view.findViewById(R.id.hide_posts);
        message.setOnClickListener(v ->{
            final Intent intent = new Intent(requireContext(), ChatActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(ChatActivity.EXTRA_CHAT_ID, contact.userId);
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
        hidePosts.setOnClickListener(v -> {
            dismiss();
            DialogFragmentUtils.showDialogFragmentOnce(HideFuturePostsDialogFragment.newHideFromDialog(contact), requireActivity().getSupportFragmentManager());
        });
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static class ContactMenuViewModel extends AndroidViewModel {

        private ContactsDb contactsDb;

        private ComputableLiveData<Contact> contactComputableLiveData;

        public ContactMenuViewModel(@NonNull Application application, @NonNull UserId userId) {
            super(application);
            this.contactsDb = ContactsDb.getInstance();

            contactComputableLiveData = new ComputableLiveData<Contact>() {
                @Override
                protected Contact compute() {
                    return contactsDb.getContact(userId);
                }
            };
        }

        public LiveData<Contact> getContact() {
            return contactComputableLiveData.getLiveData();
        }

        public static class Factory implements ViewModelProvider.Factory {

            private final Application application;
            private final UserId profileUserId;

            Factory(@NonNull Application application, @NonNull UserId profileUserId) {
                this.application = application;
                this.profileUserId = profileUserId;
            }

            @Override
            public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(ContactMenuViewModel.class)) {
                    //noinspection unchecked
                    return (T) new ContactMenuViewModel(application, profileUserId);
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        }
    }
}
