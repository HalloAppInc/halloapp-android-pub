package com.halloapp.katchup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.AppContext;
import com.halloapp.MainActivity;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.RegistrationRequestActivity;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.proto.server.MomentNotification;
import com.halloapp.ui.HalloFragment;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.util.logs.LogProvider;
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

        View forceContactSyncButton = root.findViewById(R.id.force_contact_sync);
        forceContactSyncButton.setOnClickListener(v -> {
            ContactsSync.getInstance().forceFullContactsSync();
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

        View dailyNotification = root.findViewById(R.id.fake_notification);
        dailyNotification.setOnClickListener(v -> {
            dailyNotification.postDelayed(()->{
                KatchupConnectionObserver.getInstance(AppContext.getInstance().get()).onMomentNotificationReceived(
                        MomentNotification.newBuilder()
                                .setNotificationId(5)
                                .setPrompt("Cool prompt")
                                .setTimestamp(System.currentTimeMillis() / 1000L)
                                .setType(MomentNotification.Type.LIVE_CAMERA)
                                .build(), null
                );
            }, 2);
        });

        View deleteAccount = root.findViewById(R.id.delete_account);
        deleteAccount.setOnClickListener(v -> {
            Connection.getInstance().deleteAccount(Me.getInstance().getPhone(), null).onResponse(res -> {
                startActivity(new Intent(requireContext(), RegistrationRequestActivity.class));
            }).onError(err -> {
                Log.e("Failed to delete account", err);
            });
        });

        View expirationButton = root.findViewById(R.id.expiration_activity);
        expirationButton.setOnClickListener(v -> {
            Intent intent = AppExpirationActivity.open(requireContext(), 14);
            startActivity(intent);
        });

        View blockedButton = root.findViewById(R.id.blocked_users);
        blockedButton.setOnClickListener(v -> {
            Intent intent = BlockedUsersActivity.open(requireContext());
            startActivity(intent);
        });

        View sendLogs = root.findViewById(R.id.send_logs);
        sendLogs.setOnClickListener(v -> {
            Log.sendErrorReport("User sent logs");

            ProgressDialog progressDialog = ProgressDialog.show(requireContext(), null, getString(R.string.preparing_logs));
            LogProvider.openLogIntent(requireContext()).observe(getViewLifecycleOwner(), intent -> {
                startActivity(intent);
                progressDialog.dismiss();
            });
        });

        return root;
    }
}
