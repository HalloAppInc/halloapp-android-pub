package com.halloapp.ui.profile;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.halloapp.R;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.MyProfileHomeFragment;

public class MyProfileActivity extends HalloActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.title_profile);

        setContentView(R.layout.activity_view_profile);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.profile_fragment_placeholder, new MyProfileHomeFragment())
                .commit();

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(getResources().getDimension(R.dimen.action_bar_elevation));
        }
    }

}
