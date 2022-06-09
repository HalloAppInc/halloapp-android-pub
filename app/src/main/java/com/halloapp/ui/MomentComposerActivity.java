package com.halloapp.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.R;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Media;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.media.MediaUtils;
import com.halloapp.ui.chat.ChatActivity;
import com.halloapp.ui.groups.ViewGroupFeedActivity;
import com.halloapp.ui.mediaedit.MediaEditActivity;
import com.halloapp.ui.share.ShareDestination;
import com.halloapp.util.ActivityUtils;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class MomentComposerActivity extends HalloActivity {

    private static final String EXTRA_TARGET_MOMENT_USER_ID = "target_moment_user_id";

    private MediaThumbnailLoader fullThumbnailLoader;

    private ImageView imageView;

    private View send;

    private MomentComposerViewModel viewModel;

    @NonNull
    public static Intent unlockMoment(@NonNull Context context, @Nullable UserId postSenderUserId) {
        Intent i = new Intent(context, MomentComposerActivity.class);
        i.putExtra(EXTRA_TARGET_MOMENT_USER_ID, postSenderUserId);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_moment_composer);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        fullThumbnailLoader = new MediaThumbnailLoader(this, Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));

        setTitle(R.string.moment_title);

        View changePriv = findViewById(R.id.change_privacy);
        changePriv.setEnabled(false);

        imageView = findViewById(R.id.image);
        send = findViewById(R.id.send);

        final Uri uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);

        viewModel = new ViewModelProvider(this,
                new MomentComposerViewModel.Factory(getApplication(), uri, getIntent().getParcelableExtra(EXTRA_TARGET_MOMENT_USER_ID))).get(MomentComposerViewModel.class);

        viewModel.editMedia.observe(this, media -> {
            fullThumbnailLoader.load(imageView, media.get(0).original);
        });

        viewModel.contentItems.observe(this, contentItems -> {
            if (contentItems == null || contentItems.size() == 0) {
                return;
            }

            for (ContentItem item : contentItems) {
                item.addToStorage(ContentDb.getInstance());
            }

            setResult(RESULT_OK);
            finish();
        });

        send.setOnClickListener(v -> {
            viewModel.prepareContent(ActivityUtils.supportsWideColor(this));
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fullThumbnailLoader != null) {
            fullThumbnailLoader.destroy();
            fullThumbnailLoader = null;
        }

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

}
