package com.halloapp.ui.contacts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.R;
import com.halloapp.ui.HalloBottomSheetDialogFragment;

public class FavoritesNuxBottomSheetDialogFragment extends HalloBottomSheetDialogFragment {

    public static FavoritesNuxBottomSheetDialogFragment newInstance() {
        return new FavoritesNuxBottomSheetDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.favorites_nux_bottom_sheet, container, false);

        Button setButton = view.findViewById(R.id.edit_favorites);
        setButton.setOnClickListener(v -> {
            startActivity(EditFavoritesActivity.openFavorites(requireContext()));
            dismiss();
        });
        Button notNow = view.findViewById(R.id.not_now);
        notNow.setOnClickListener(v -> {
            dismiss();
        });
        return view;
    }

    @Override
    public int getTheme() {
        return R.style.RoundedBottomSheetDialog;
    }
}
