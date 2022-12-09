package com.halloapp.katchup;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.MainActivity;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.RegistrationRequestActivity;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.ui.HalloFragment;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;

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

        View insertTestPost = root.findViewById(R.id.insert_test_post);
        insertTestPost.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), SelfiePostComposerActivity.class));
        });

        View deleteAccount = root.findViewById(R.id.delete_account);
        deleteAccount.setOnClickListener(v -> {
            Connection.getInstance().deleteAccount(Me.getInstance().getPhone(), null).onResponse(res -> {
                startActivity(new Intent(requireContext(), RegistrationRequestActivity.class));
            }).onError(err -> {
                Log.e("Failed to delete account", err);
            });
        });

        return root;
    }
}
