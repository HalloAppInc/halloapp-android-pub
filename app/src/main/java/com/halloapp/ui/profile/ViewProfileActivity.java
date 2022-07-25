package com.halloapp.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.SharedElementCallback;

import com.halloapp.R;
import com.halloapp.id.UserId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.MediaPagerAdapter;
import com.halloapp.ui.mediaexplorer.MediaExplorerActivity;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;

import java.util.List;
import java.util.Map;

public class ViewProfileActivity extends HalloActivity {

    private static final String EXTRA_USER_ID = "user_id";

    public static Intent viewProfile(@NonNull Context context, @NonNull UserId userId) {
        Preconditions.checkNotNull(userId);
        Intent intent = new Intent(context, ViewProfileActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        return intent;
    }

    private final SharedElementCallback sharedElementCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            for (String name : names) {
                View view = MediaPagerAdapter.getTransitionView(findViewById(R.id.profile_fragment_placeholder), name);
                if (view != null) {
                    sharedElements.put(name, view);
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        supportRequestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setExitSharedElementCallback(sharedElementCallback);

        UserId userId = getIntent().getParcelableExtra(EXTRA_USER_ID);
        if (userId == null) {
            finish();
            Log.e("ViewProfileActivity/onCreate must provide a user id");
            return;
        }
        if (userId.isMe()) {
            setTitle(R.string.my_posts);
        } else {
            setTitle("");
        }
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

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.hasExtra(MediaExplorerActivity.EXTRA_CONTENT_ID) && data.hasExtra(MediaExplorerActivity.EXTRA_SELECTED)) {
            String contentId = data.getStringExtra(MediaExplorerActivity.EXTRA_CONTENT_ID);
            int position = data.getIntExtra(MediaExplorerActivity.EXTRA_SELECTED, 0);
            View root = findViewById(R.id.profile_fragment_placeholder);

            if (root != null && contentId != null) {
                postponeEnterTransition();
                MediaPagerAdapter.preparePagerForTransition(root, contentId, position, this::startPostponedEnterTransition);
            }
        }
    }
}
