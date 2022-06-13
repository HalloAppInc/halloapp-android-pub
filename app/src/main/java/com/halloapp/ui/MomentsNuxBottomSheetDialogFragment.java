package com.halloapp.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.Preferences;
import com.halloapp.R;

public class MomentsNuxBottomSheetDialogFragment extends HalloBottomSheetDialogFragment {

    public static MomentsNuxBottomSheetDialogFragment newInstance() {
        return new MomentsNuxBottomSheetDialogFragment();
    }

    public interface Parent {
        void onMomentNuxDismissed();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.moments_nux_bottom_sheet, container, false);

        Button setButton = view.findViewById(R.id.ok);
        setButton.setOnClickListener(v -> {
            dismiss();
        });
        Preferences.getInstance().setMomentsNuxShown();
        return view;
    }

    @Override
    public int getTheme() {
        return R.style.RoundedBottomSheetDialog;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() instanceof Parent) {
            ((Parent) getActivity()).onMomentNuxDismissed();
        }
    }
}
