package com.halloapp.ui;

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
import com.halloapp.id.GroupId;
import com.halloapp.media.MediaUtils;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.camera.CameraActivity;
import com.halloapp.ui.groups.GroupFeedFragment;
import com.halloapp.ui.groups.GroupFeedViewModel;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.util.ArrayList;
import java.util.Collections;

public class ViewMyPostsActivity extends HalloActivity {

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
    }
}
