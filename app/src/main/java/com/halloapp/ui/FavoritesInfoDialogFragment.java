package com.halloapp.ui;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.halloapp.R;
import com.halloapp.ui.privacy.FeedPrivacyActivity;

public class FavoritesInfoDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_favorites_info, null);

        View cancel = view.findViewById(R.id.ok);
        View settings = view.findViewById(R.id.settings);

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
