package com.halloapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.R;
import com.halloapp.ui.home.HomeViewModel;

public class FeedNuxBottomSheetDialogFragment extends InfoBottomSheetDialogFragment {

    private HomeViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        return v;
    }

    @Override
    protected void onOkButtonPress() {
        super.onOkButtonPress();
        viewModel.closeFeedNux();
    }

    @Override
    protected CharSequence getTitle() {
        return getText(R.string.feed_nux_bottom_sheet_title);
    }

    @Override
    protected CharSequence getText() {
        return getText(R.string.feed_nux_bottom_sheet_text);
    }
}
