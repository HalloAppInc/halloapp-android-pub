package com.halloapp.newapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.R;
import com.halloapp.ui.HalloFragment;

public class MainFragment extends HalloFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);

        View prev = root.findViewById(R.id.prev);
        prev.setOnClickListener(v -> {
            NewMainActivity activity = (NewMainActivity) getActivity();
            activity.previousScreen();
        });
        View next = root.findViewById(R.id.next);
        next.setOnClickListener(v -> {
            NewMainActivity activity = (NewMainActivity) getActivity();
            activity.nextScreen();
        });

        return root;
    }
}
