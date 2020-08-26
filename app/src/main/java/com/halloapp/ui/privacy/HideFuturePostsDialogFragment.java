package com.halloapp.ui.privacy;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.SnackbarHelper;


public class HideFuturePostsDialogFragment extends DialogFragment {

    private static final String ARG_CONTACT = "contact";

    public static HideFuturePostsDialogFragment newHideFromDialog(@NonNull Contact contact)
    {
        HideFuturePostsDialogFragment fragment = new HideFuturePostsDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_CONTACT, contact);
        fragment.setArguments(args);

        return fragment;
    }

    private HideFuturePostsViewModel viewModel;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = requireArguments();
        Contact contact = Preconditions.checkNotNull(args.getParcelable(ARG_CONTACT));

        viewModel = new ViewModelProvider(requireActivity()).get(HideFuturePostsViewModel.class);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.are_you_sure_dialog_title);
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_hide_future_posts, null);
        TextView messageView = view.findViewById(R.id.dialog_message);
        View progress = view.findViewById(R.id.progress);
        progress.setVisibility(View.INVISIBLE);
        View buttonContainer = view.findViewById(R.id.button_container);
        View cancel = view.findViewById(R.id.cancel);
        View hide = view.findViewById(R.id.hide);

        viewModel.inProgress().observe(requireActivity(), inProgress -> {
            if (inProgress == null) {
                return;
            }
            if (inProgress) {
                buttonContainer.setVisibility(View.INVISIBLE);
                progress.setVisibility(View.VISIBLE);
                messageView.setVisibility(View.INVISIBLE);
            }
        });
        cancel.setOnClickListener(v -> {
            dismiss();
        });
        hide.setOnClickListener(v -> {
            FragmentActivity activity = requireActivity();
            viewModel.hideContact(contact).observe(activity, success -> {
                if (success == null) {
                    return;
                }
                if (success) {
                    SnackbarHelper.showInfo(activity, getString(R.string.feed_privacy_hide_success, contact.getDisplayName()));
                } else {
                    SnackbarHelper.showWarning(activity, getString(R.string.feed_privacy_hide_failure));
                }
                dismiss();
            });
        });
        messageView.setText(getString(R.string.hide_future_posts_confirmation, contact.getDisplayName()));
        builder.setView(view);

        return builder.create();
    }
}
