package com.halloapp.ui.groups;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.halloapp.R;
import com.halloapp.id.GroupId;
import com.halloapp.media.MediaUtils;
import com.halloapp.ui.ContentComposerActivity;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.camera.CameraActivity;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.util.logs.Log;
import com.halloapp.util.Preconditions;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.util.ArrayList;
import java.util.Collections;

public class ViewGroupFeedActivity extends HalloActivity {

    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;

    private static final String EXTRA_GROUP_ID = "group_id";

    public static Intent viewFeed(@NonNull Context context, @NonNull GroupId groupId) {
        Intent intent = new Intent(context, ViewGroupFeedActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        return intent;
    }

    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();

    private GroupFeedViewModel viewModel;

    private TextView titleView;
    private ImageView avatarView;

    private GroupId groupId;

    private SpeedDialView fabView;

    private boolean scrollUpOnDataLoaded;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        groupId = getIntent().getParcelableExtra(EXTRA_GROUP_ID);
        if (groupId == null) {
            finish();
            Log.e("ViewGroupFeedActivity/onCreate must provide a valid group id");
            return;
        }

        viewModel = new ViewModelProvider(this, new GroupFeedViewModel.Factory(getApplication(), groupId)).get(GroupFeedViewModel.class);

        setContentView(R.layout.activity_view_group_feed);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.profile_fragment_placeholder, GroupFeedFragment.newInstance(groupId))
                .commit();

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        titleView = findViewById(R.id.title);
        avatarView = findViewById(R.id.avatar);

        fabView = findViewById(R.id.speed_dial);
        fabView.getMainFab().setRippleColor(ContextCompat.getColor(this, R.color.white_20));
        fabView.setOnActionSelectedListener(actionItem -> {
            onFabActionSelected(actionItem.getId());
            return true;
        });
        addFabItem(fabView, R.id.add_post_gallery, R.drawable.ic_image, R.string.gallery_post);
        addFabItem(fabView, R.id.add_post_camera, R.drawable.ic_camera, R.string.camera_post);
        addFabItem(fabView, R.id.add_post_text, R.drawable.ic_text, R.string.text_post);

        final View newPostsView = findViewById(R.id.new_posts);
        newPostsView.setOnClickListener(v -> {
            scrollUpOnDataLoaded = true;
            viewModel.reloadPostsAt(Long.MAX_VALUE);
        });

        viewModel.chat.getLiveData().observe(this, chat -> {
            if (chat != null) {
                setTitle(chat.name);
            }
        });

        avatarLoader.load(avatarView, groupId, false);
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
        switch (id) {
            case R.id.add_post_text: {
                Intent intent = new Intent(this, ContentComposerActivity.class);
                intent.putExtra(ContentComposerActivity.EXTRA_GROUP_ID, groupId);
                startActivity(intent);
                break;
            }
            case R.id.add_post_gallery: {
                final Intent intent = new Intent(this, MediaPickerActivity.class);
                intent.putExtra(MediaPickerActivity.EXTRA_PICKER_PURPOSE, MediaPickerActivity.PICKER_PURPOSE_POST);
                intent.putExtra(MediaPickerActivity.EXTRA_GROUP_ID, groupId);
                startActivity(intent);
                break;
            }
            case R.id.add_post_camera: {
                final Intent intent = new Intent(this, CameraActivity.class);
                intent.putExtra(CameraActivity.EXTRA_GROUP_FEED_ID, groupId);
                startActivity(intent);
                break;
            }
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

    @Override
    protected void onResume() {
        super.onResume();
        avatarLoader.load(avatarView, groupId, false);
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        titleView.setText(title);
    }
}
