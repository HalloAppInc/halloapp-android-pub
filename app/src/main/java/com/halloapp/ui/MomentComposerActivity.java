package com.halloapp.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Media;
import com.halloapp.content.MomentManager;
import com.halloapp.content.Post;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.media.MediaUtils;
import com.halloapp.props.ServerProps;
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
    public static final String EXTRA_SHOW_PSA_TAG = "show_psa_tag";

    private final ServerProps serverProps = ServerProps.getInstance();

    private MediaThumbnailLoader fullThumbnailLoader;

    private View send;
    private ImageView imageView;
    private EditText psaTagEditText;

    private MomentComposerViewModel viewModel;

    private boolean showPsaTag;

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
        send.setEnabled(false);
        psaTagEditText = findViewById(R.id.psa_tag);

        showPsaTag = getIntent().getBooleanExtra(EXTRA_SHOW_PSA_TAG, false);
        if (showPsaTag) {
            setTitle("New PSA moment");
        }

        final Uri uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);

        viewModel = new ViewModelProvider(this,
                new MomentComposerViewModel.Factory(getApplication(), uri, getIntent().getParcelableExtra(EXTRA_TARGET_MOMENT_USER_ID))).get(MomentComposerViewModel.class);

        viewModel.editMedia.observe(this, media -> {
            send.setEnabled(true);
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

        if (serverProps.isPsaAdmin() && showPsaTag) {
            psaTagEditText.setVisibility(View.VISIBLE);
        }

        send.setOnClickListener(v -> {
            boolean warned = Boolean.TRUE.equals(viewModel.warnedAboutReplacingMoment.getValue());
            Editable psa = psaTagEditText.getText();
            String psaTag = psa == null ? null : psa.toString();
            if (!warned && Boolean.TRUE.equals(MomentManager.getInstance().isUnlockedLiveData().getValue()) && !showPsaTag) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MomentComposerActivity.this);
                builder.setTitle(R.string.heads_up_title);
                builder.setMessage(R.string.new_moment_replace);
                builder.setPositiveButton(R.string.ok, (d, e) -> {
                    Preferences.getInstance().applyMomentsReplaceWarned();
                    viewModel.prepareContent(ActivityUtils.supportsWideColor(this), psaTag);
                });
                builder.setNegativeButton(R.string.cancel, null);
                builder.show();
            } else{
                viewModel.prepareContent(ActivityUtils.supportsWideColor(this), psaTag);
            }
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
