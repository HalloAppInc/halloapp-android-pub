package com.halloapp.ui;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.halloapp.R;
import com.halloapp.ui.privacy.FeedPrivacyActivity;

public class FavoritesInfoDialogFragment extends DialogFragment {

    private static final String ARG_MY_POST = "my_post";

    public static FavoritesInfoDialogFragment newInstance(boolean ownPost) {
        Bundle args = new Bundle();
        args.putBoolean(ARG_MY_POST, ownPost);
        FavoritesInfoDialogFragment dialogFragment = new FavoritesInfoDialogFragment();
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_favorites_info, null);

        TextView messageView = view.findViewById(R.id.dialog_message);

        View cancel = view.findViewById(R.id.ok);
        View settings = view.findViewById(R.id.settings);

        Bundle args = requireArguments();
        messageView.setText(args.getBoolean(ARG_MY_POST, true) ? R.string.contact_favorites_explanation : R.string.contact_favorites_explanation_others);

        cancel.setOnClickListener(v -> {
            dismiss();
        });
        settings.setOnClickListener(v -> {
            startActivity(FeedPrivacyActivity.editFeedPrivacy(v.getContext(), false));
            dismiss();
        });
        builder.setView(view);

        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }
}
