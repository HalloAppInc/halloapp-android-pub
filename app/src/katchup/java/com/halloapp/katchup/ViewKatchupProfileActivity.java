package com.halloapp.katchup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.halloapp.R;
import com.halloapp.id.UserId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.MediaPagerAdapter;
import com.halloapp.ui.mediaexplorer.MediaExplorerActivity;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;

public class ViewKatchupProfileActivity extends HalloActivity {

    private static final String EXTRA_USER_ID = "user_id";

    public static Intent viewProfile(@NonNull Context context, @NonNull UserId userId) {
        Preconditions.checkNotNull(userId);
        Intent intent = new Intent(context, ViewProfileActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserId userId = getIntent().getParcelableExtra(EXTRA_USER_ID);
        if (userId == null) {
            finish();
            Log.e("ViewProfileActivity/onCreate must provide a user id");
            return;
        }

        setContentView(R.layout.activity_view_profile);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.profile_fragment_placeholder, NewProfileFragment.newProfileFragment(userId))
                .commit();
    }
}
