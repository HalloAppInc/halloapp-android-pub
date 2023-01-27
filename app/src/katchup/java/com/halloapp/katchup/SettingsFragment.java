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
import com.halloapp.Constants;
import com.halloapp.MainActivity;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.RegistrationRequestActivity;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.server.MomentNotification;
import com.halloapp.ui.HalloFragment;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.util.logs.LogProvider;
import com.halloapp.xmpp.Connection;

public class SettingsFragment extends HalloFragment {

    private final ServerProps serverProps = ServerProps.getInstance();

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
        debugButton.setVisibility(serverProps.getIsInternalUser() ? View.VISIBLE : View.GONE);
        debugButton.setOnClickListener(v -> {
            Preconditions.checkNotNull(null);
        });

        View forceContactSyncButton = root.findViewById(R.id.force_contact_sync);
        forceContactSyncButton.setVisibility(serverProps.getIsInternalUser() ? View.VISIBLE : View.GONE);
        forceContactSyncButton.setOnClickListener(v -> {
            ContactsSync.getInstance().forceFullContactsSync();
        });

        View forceRelationshipSyncButton = root.findViewById(R.id.force_relationship_sync);
        forceRelationshipSyncButton.setVisibility(serverProps.getIsInternalUser() ? View.VISIBLE : View.GONE);
        forceRelationshipSyncButton.setOnClickListener(v -> {
                    Preferences.getInstance().setLastFullRelationshipSyncTime(0);
                    RelationshipSyncWorker.schedule(requireContext());
        });

        View termsOfServiceButton = root.findViewById(R.id.terms_of_service);
        termsOfServiceButton.setOnClickListener(v -> IntentUtils.openUrlInBrowser(termsOfServiceButton, Constants.KATCHUP_TERMS_LINK));

        View privacyPolicyButton = root.findViewById(R.id.privacy_policy);
        privacyPolicyButton.setOnClickListener(v -> IntentUtils.openUrlInBrowser(privacyPolicyButton, Constants.KATCHUP_PRIVACY_NOTICE_LINK));

        View dailyNotification = root.findViewById(R.id.fake_notification);
        dailyNotification.setVisibility(serverProps.getIsInternalUser() ? View.VISIBLE : View.GONE);
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
                Me.getInstance().resetRegistration();
                startActivity(new Intent(requireContext(), RegistrationRequestActivity.class));
            }).onError(err -> {
                Log.e("Failed to delete account", err);
            });
        });

        View expirationButton = root.findViewById(R.id.expiration_activity);
        expirationButton.setVisibility(serverProps.getIsInternalUser() ? View.VISIBLE : View.GONE);
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
