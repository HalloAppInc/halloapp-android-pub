package com.halloapp.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.halloapp.R;
import com.halloapp.id.UserId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.logs.Log;

public class ViewProfileActivity extends HalloActivity {

    private static final String EXTRA_USER_ID = "user_id";

    public static Intent viewProfile(@NonNull Context context, @NonNull UserId userId) {
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

        setTitle("");
        setContentView(R.layout.activity_view_profile);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.profile_fragment_placeholder, ProfileFragment.newProfileFragment(userId))
                .commit();

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(getResources().getDimension(R.dimen.action_bar_elevation));
        }
    }
}
