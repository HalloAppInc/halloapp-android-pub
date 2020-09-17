package com.halloapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.halloapp.R;
import com.halloapp.contacts.Contact;

public abstract class InfoBottomSheetDialogFragment extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.info_bottom_sheet, container, false);

        TextView title = view.findViewById(R.id.title);
        TextView info = view.findViewById(R.id.info);
        title.setText(getTitle());
        info.setText(getText());

        Button okButton = view.findViewById(R.id.button2);
        okButton.setOnClickListener(v -> {
            onOkButtonPress();
        });
        return view;
    }

    protected void onOkButtonPress() {
        dismiss();
    }

    protected abstract CharSequence getTitle();
    protected abstract CharSequence getText();
}
