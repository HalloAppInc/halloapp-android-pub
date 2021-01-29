package com.halloapp.ui.groups;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaUtils;
import com.halloapp.ui.ContentComposerActivity;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.MainActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.camera.CameraActivity;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.util.Preconditions;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ViewGroupFeedActivity extends HalloActivity {

    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_VIEW_GROUP_INFO = 2;

    private static final String EXTRA_GROUP_ID = "group_id";

    public static Intent viewFeed(@NonNull Context context, @NonNull GroupId groupId) {
        Intent intent = new Intent(context, ViewGroupFeedActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        return intent;
    }

    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();

    private GroupFeedViewModel viewModel;

    private GroupId groupId;

    private SpeedDialView fabView;

    private boolean scrollUpOnDataLoaded;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("");
        groupId = getIntent().getParcelableExtra(EXTRA_GROUP_ID);
        if (groupId == null) {
            finish();
            Log.e("ViewGroupFeedActivity/onCreate must provide a valid group id");
            return;
        }

        viewModel = new ViewModelProvider(this, new GroupFeedViewModel.Factory(groupId)).get(GroupFeedViewModel.class);

        setContentView(R.layout.activity_view_group_feed);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.profile_fragment_placeholder, GroupFeedFragment.newInstance(groupId))
                .commit();

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        TextView titleView = findViewById(R.id.title);
        TextView subtitleView = findViewById(R.id.subtitle);
        ImageView avatarView = findViewById(R.id.avatar);

        View toolbarTitleContainer = findViewById(R.id.toolbar_text_container);
        toolbarTitleContainer.setOnClickListener(v -> {
            startActivityForResult(GroupInfoActivity.viewGroup(this, groupId), REQUEST_CODE_VIEW_GROUP_INFO);
        });

        viewModel.chat.getLiveData().observe(this, chat -> {
            if (chat != null) {
                titleView.setText(chat.name);
            } else {
                titleView.setText(null);
            }
        });

        avatarLoader.load(avatarView, groupId, false);

        fabView = findViewById(R.id.speed_dial);
        fabView.getMainFab().setRippleColor(ContextCompat.getColor(this, R.color.white_20));
        fabView.setOnActionSelectedListener(actionItem -> {
            onFabActionSelected(actionItem.getId());
            return true;
        });
        addFabItem(fabView, R.id.add_post_gallery, R.drawable.ic_image, R.string.gallery_post);
        addFabItem(fabView, R.id.add_post_camera, R.drawable.ic_camera, R.string.camera_post);
        addFabItem(fabView, R.id.add_post_text, R.drawable.ic_text, R.string.text_post);

        BottomNavigationView bottomNav = findViewById(R.id.nav_view);
        bottomNav.setSelectedItemId(R.id.navigation_groups);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            Intent homeIntent = new Intent(this, MainActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            String navTarget = MainActivity.NAV_TARGET_FEED;
            if (item.getItemId() == R.id.navigation_home) {
                navTarget = MainActivity.NAV_TARGET_FEED;
            } else if (item.getItemId() == R.id.navigation_messages) {
                navTarget = MainActivity.NAV_TARGET_MESSAGES;
            } else if (item.getItemId() == R.id.navigation_profile) {
                navTarget = MainActivity.NAV_TARGET_PROFILE;
            } else if (item.getItemId() == R.id.navigation_groups) {
                navTarget = MainActivity.NAV_TARGET_GROUPS;
            }
            homeIntent.putExtra(MainActivity.EXTRA_NAV_TARGET, navTarget);
            startActivity(homeIntent);
            finish();
            overridePendingTransition(0, 0);
            return true;
        });

        final View newPostsView = findViewById(R.id.new_posts);
        newPostsView.setOnClickListener(v -> {
            scrollUpOnDataLoaded = true;
            viewModel.reloadPostsAt(Long.MAX_VALUE);
        });
    }

    private static void addFabItem(@NonNull SpeedDialView fabView, @IdRes int id, @DrawableRes int icon, @StringRes int label) {
        final View itemView = fabView.addActionItem(
                new SpeedDialActionItem.Builder(id, icon)
                        .setFabSize(FloatingActionButton.SIZE_NORMAL)
                        .setFabBackgroundColor(ContextCompat.getColor(fabView.getContext(), R.color.fab_background))
                        .setFabImageTintColor(ContextCompat.getColor(fabView.getContext(), android.R.color.white))
                        .create());
        Preconditions.checkNotNull(itemView).findViewById(R.id.sd_fab).setContentDescription(fabView.getContext().getString(label));
    }

    private void onFabActionSelected(@IdRes int id) {
        if (id == R.id.add_post_text) {
            Intent intent = new Intent(this, ContentComposerActivity.class);
            intent.putExtra(ContentComposerActivity.EXTRA_GROUP_ID, groupId);
            startActivity(intent);
        } else if (id == R.id.add_post_gallery) {
            final Intent intent = new Intent(this, MediaPickerActivity.class);
            intent.putExtra(MediaPickerActivity.EXTRA_PICKER_PURPOSE, MediaPickerActivity.PICKER_PURPOSE_POST);
            intent.putExtra(MediaPickerActivity.EXTRA_GROUP_ID, groupId);
            startActivity(intent);
        } else if (id == R.id.add_post_camera) {
            final Intent intent = new Intent(this, CameraActivity.class);
            intent.putExtra(CameraActivity.EXTRA_GROUP_ID, groupId);
            startActivity(intent);
        }
        fabView.close(false);
    }

    @Override
    public void onActivityResult(final int request, final int result, final Intent data) {
        super.onActivityResult(request, result, data);
        switch (request) {
            case REQUEST_CODE_CAPTURE_IMAGE: {
                if (result == Activity.RESULT_OK) {
                    final Intent intent = new Intent(this, ContentComposerActivity.class);
                    intent.putExtra(ContentComposerActivity.EXTRA_GROUP_ID, groupId);
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
                            new ArrayList<>(Collections.singleton(MediaUtils.getImageCaptureUri(this))));
                    startActivity(intent);
                }
                break;
            }
            case REQUEST_CODE_VIEW_GROUP_INFO: {
                if (result == GroupInfoActivity.RESULT_CODE_EXIT_CHAT) {
                    finish();
                }
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (fabView.isOpen()) {
            fabView.close();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // Allow dismissal of FAB menu on scroll
        if (fabView.isOpen()) {
            if (ev.getX() < fabView.getX()
                    || ev.getX() > fabView.getX() + fabView.getWidth()
                    || ev.getY() > fabView.getY() + fabView.getHeight()
                    || ev.getY() < fabView.getY()) {
                fabView.close();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

}
