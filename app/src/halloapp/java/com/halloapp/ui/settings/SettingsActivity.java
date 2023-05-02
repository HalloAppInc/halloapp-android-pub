package com.halloapp.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.halloapp.R;
import com.halloapp.StorageUsageActivity;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.AccountActivity;
import com.halloapp.ui.DarkModeDialog;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.Preconditions;

public class SettingsActivity extends HalloActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        View privacy = findViewById(R.id.privacy);
        privacy.setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), SettingsPrivacy.class));
        });
        View darkMode = findViewById(R.id.theme);
        darkMode.setOnClickListener(v -> {
            DarkModeDialog dialog = new DarkModeDialog(this);
            dialog.show();
        });

        View storageUsage = findViewById(R.id.storage);
        storageUsage.setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), StorageUsageActivity.class));
        });

        View notifications = findViewById(R.id.notifications);
        notifications.setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), SettingsNotifications.class));
        });

        View account = findViewById(R.id.account);
        account.setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), AccountActivity.class));
        });

    }
}
