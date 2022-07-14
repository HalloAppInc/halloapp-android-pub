package com.halloapp.ui.groups;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.halloapp.R;

public class SelectGroupExpiryDialogFragment extends DialogFragment {

    public static final int OPTION_24_HOURS = 0;
    public static final int OPTION_30_DAYS = 1;
    public static final int OPTION_NEVER = 2;

    public static SelectGroupExpiryDialogFragment newInstance(int selection) {
        Bundle args = new Bundle();
        args.putInt(ARG_SELECTED_EXPIRATION, selection);
        SelectGroupExpiryDialogFragment instance = new SelectGroupExpiryDialogFragment();
        instance.setArguments(args);

        return instance;
    }

    public interface Host {
        void onExpirySelected(int selectedOption);
    }

    private static final String ARG_SELECTED_EXPIRATION = "selected_expiration";

    private Host host;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof Host) {
            host = (Host) parentFragment;
            return;
        }
        if (context instanceof Host) {
            host = (Host) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        host = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.group_post_expiry_title);

        String[] options = new String[]{
                getString(R.string.expiration_day),
                getString(R.string.expiration_month),
                getString(R.string.expiration_never)
        };

        int selectedOption = requireArguments().getInt(ARG_SELECTED_EXPIRATION, 0);

        builder.setSingleChoiceItems(options, selectedOption, (dialog, item) -> {
            if (item != selectedOption) {
                if (host != null) {
                    host.onExpirySelected(item);
                }
            }
            dismiss();
        });
        return builder.create();
    }
}
