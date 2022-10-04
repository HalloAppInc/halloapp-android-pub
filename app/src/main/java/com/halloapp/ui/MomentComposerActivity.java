package com.halloapp.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Outline;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.transition.Fade;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.TransitionManager;

import com.halloapp.Constants;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.MomentManager;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.props.ServerProps;
import com.halloapp.util.ActivityUtils;
import com.halloapp.util.Preconditions;

import java.util.ArrayList;

public class MomentComposerActivity extends HalloActivity {

    private static final String EXTRA_TARGET_MOMENT_USER_ID = "target_moment_user_id";
    public static final String EXTRA_SHOW_PSA_TAG = "show_psa_tag";
    public static final String EXTRA_SELFIE_MEDIA_INDEX = "selfie_media_index";

    private static final int SECOND_IMAGE_ENTRANCE_DELAY = 1000;

    private final ServerProps serverProps = ServerProps.getInstance();

    private MediaThumbnailLoader fullThumbnailLoader;

    private View send;
    private View close;
    private ImageView imageViewFirst;
    private ImageView imageViewSecond;
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

        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setEnterTransition(new Fade());
        getWindow().getAllowEnterTransitionOverlap();

        setContentView(R.layout.activity_moment_composer);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        fullThumbnailLoader = new MediaThumbnailLoader(this, Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));

        setTitle(R.string.moment_title);

        imageViewFirst = findViewById(R.id.image_first);
        imageViewSecond = findViewById(R.id.image_second);
        send = findViewById(R.id.send);
        send.setEnabled(false);
        psaTagEditText = findViewById(R.id.psa_tag);
        close = findViewById(R.id.close);

        Drawable cardBackgroundDrawable = ContextCompat.getDrawable(this, R.drawable.camera_card_background);
        float cardRadius = getResources().getDimension(R.dimen.camera_card_border_radius);
        CardView card = findViewById(R.id.card);
        card.setBackground(cardBackgroundDrawable);
        card.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cardRadius);
            }
        });

        float mediaRadius = getResources().getDimension(R.dimen.camera_preview_border_radius);
        View imageContainer = findViewById(R.id.image_container);
        imageContainer.setClipToOutline(true);
        imageContainer.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), mediaRadius);
            }
        });

        showPsaTag = getIntent().getBooleanExtra(EXTRA_SHOW_PSA_TAG, false);
        if (showPsaTag) {
            setTitle("New PSA moment");
        }

        // Required for android:elevation to work. The close btn is an image view without background.
        // Elevation requires either a background or an outline provider.
        close.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), (view.getWidth() / 2f));
            }
        });
        close.setClipToOutline(true);

        ArrayList<Uri> uris = getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (uris == null || uris.isEmpty()) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this, new MomentComposerViewModel.Factory(
                getApplication(),
                uris,
                getIntent().getParcelableExtra(EXTRA_TARGET_MOMENT_USER_ID),
                getIntent().getIntExtra(EXTRA_SELFIE_MEDIA_INDEX, -1)
                ))
                .get(MomentComposerViewModel.class);

        viewModel.contactsCount.getLiveData().observe(this, this::setSubtitleContactsCount);

        viewModel.editMedia.observe(this, media -> {
            if (media.size() > 0) {
                fullThumbnailLoader.load(imageViewFirst, media.get(0).original);
            }

            // animate hiding the second image when present
            if (imageViewSecond.getDrawable() != null && media.size() == 1) {
                TransitionManager.beginDelayedTransition((ViewGroup) imageContainer);
            }

            imageViewSecond.setVisibility(View.GONE);
            close.setVisibility(View.GONE);

            if (media.size() > 1) {
                fullThumbnailLoader.load(imageViewSecond, media.get(1).original);

                imageContainer.postDelayed(() -> {
                    TransitionManager.beginDelayedTransition((ViewGroup) imageContainer);
                    imageViewSecond.setVisibility(View.VISIBLE);
                    close.setVisibility(View.VISIBLE);
                    send.setEnabled(true);
                }, SECOND_IMAGE_ENTRANCE_DELAY);
            } else if (media.size() > 0) {
                send.setEnabled(true);
            }
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
            if (!warned && MomentManager.getInstance().isUnlockedLiveData().getValue().isUnlocked() && !showPsaTag) {
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

        close.setOnClickListener(v -> viewModel.removeAdditionalMedia());
    }

    @Override
    public void setTitle(int titleId) {
        TextView titleTextView = findViewById(R.id.title);
        titleTextView.setText(titleId);
    }

    private void setSubtitleContactsCount(long count) {
        TextView subtitleTextView = findViewById(R.id.subtitle_contacts_count);
        subtitleTextView.setText(getResources().getString(R.string.all_contacts_count, count));
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
