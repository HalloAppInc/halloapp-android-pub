package com.halloapp.ui.privacy;

import android.os.Bundle;
import android.view.View;

import android.widget.TextView;

import androidx.annotation.Nullable;

import androidx.appcompat.app.ActionBar;


import com.halloapp.R;

import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.contacts.EditFavoritesActivity;
import com.halloapp.ui.contacts.ViewMyContactsActivity;

public class FeedPrivacyActivity extends HalloActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_feed_privacy);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(getResources().getDimension(R.dimen.action_bar_elevation));
        }

        TextView headerTitle = findViewById(R.id.header);
        headerTitle.setText(R.string.setting_feed_privacy_title);

        View onlyShareWith = findViewById(R.id.only_share_with);
        View myContacts = findViewById(R.id.my_contacts);
        myContacts.setOnClickListener(v -> {
            startActivity(ViewMyContactsActivity.viewMyContacts(this));
        });
        onlyShareWith.setOnClickListener(v -> {
            startActivity(EditFavoritesActivity.openFavorites(this));
        });
    }
}
