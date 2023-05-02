package com.halloapp.katchup;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.Notifications;
import com.halloapp.R;
import com.halloapp.id.UserId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;

public class ViewKatchupProfileActivity extends HalloActivity {

    private static final String EXTRA_USER_ID = "user_id";

    public static Intent viewProfile(@NonNull Context context, @NonNull UserId userId) {
        Preconditions.checkNotNull(userId);
        Intent intent = new Intent(context, ViewKatchupProfileActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);

        UserId userId = getIntent().getParcelableExtra(EXTRA_USER_ID);
        if (userId == null && getIntent().getData() == null) {
            finish();
            Log.e("ViewProfileActivity/onCreate must provide a user id or username");
            return;
        }

        if (getIntent().getBooleanExtra(Notifications.EXTRA_IS_NOTIFICATION, false)) {
            Analytics.getInstance().notificationOpened(getIntent().getStringExtra(Notifications.EXTRA_NOTIFICATION_TYPE));
        }

        setContentView(R.layout.activity_view_profile);

        if (userId != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.profile_fragment_placeholder, NewProfileFragment.newProfileFragment(userId))
                    .commit();
        } else {
            String path = getIntent().getData().getPath();
            String username = path.substring(1);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.profile_fragment_placeholder, NewProfileFragment.newProfileFragment(username))
                    .commit();
        }
    }
}
