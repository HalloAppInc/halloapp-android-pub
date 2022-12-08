package com.halloapp.katchup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.MainActivity;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.ui.HalloFragment;
import com.halloapp.util.Preconditions;

public class SettingsFragment extends HalloFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        View prev = root.findViewById(R.id.prev);
        prev.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) getActivity();
            activity.previousScreen();
        });

        View debugButton = root.findViewById(R.id.debug);
        debugButton.setOnClickListener(v -> {
            Preconditions.checkNotNull(null);
        });

        View forceRelationshipSyncButton = root.findViewById(R.id.force_relationship_sync);
        forceRelationshipSyncButton.setOnClickListener(v -> {
            Preferences.getInstance().setLastFullRelationshipSyncTime(0);
            RelationshipSyncWorker.schedule(requireContext());
        });

        return root;
    }
}
