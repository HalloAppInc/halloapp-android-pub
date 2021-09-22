package com.halloapp.ui.avatar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.logs.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.CenterToast;
import com.halloapp.widget.CropPhotoView;
import com.halloapp.widget.SnackbarHelper;

public class AvatarPreviewActivity extends HalloActivity {

    public static final String RESULT_AVATAR_WIDTH = "avatar_width";
    public static final String RESULT_AVATAR_HEIGHT = "avatar_height";
    public static final String RESULT_AVATAR_HASH = "avatar_hash";
    public static final String RESULT_AVATAR_FILE_PATH = "avatar_file_path";

    public static final String RESULT_LARGE_AVATAR_WIDTH = "large_avatar_width";
    public static final String RESULT_LARGE_AVATAR_HEIGHT = "large_avatar_height";
    public static final String RESULT_LARGE_AVATAR_HASH = "large_avatar_hash";
    public static final String RESULT_LARGE_AVATAR_FILE_PATH = "large_avatar_file_path";

    public static Intent open(@NonNull Context context, @NonNull Uri uri, boolean forGroup) {
        final Intent intent = new Intent(context, AvatarPreviewActivity.class);
        intent.setData(uri);
        intent.putExtra(AvatarPreviewActivity.EXTRA_AVATAR_PURPOSE, forGroup ? AVATAR_PURPOSE_GROUP : AVATAR_PURPOSE_USER);
        return intent;
    }

    private static final String EXTRA_AVATAR_PURPOSE = "avatar_purpose";

    private static final int AVATAR_PURPOSE_USER = 1;
    private static final int AVATAR_PURPOSE_GROUP = 2;

    private AvatarPreviewViewModel viewModel;
    private MediaThumbnailLoader mediaThumbnailLoader;
    private CropPhotoView imageView;
    private AvatarCropOverlay cropOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_set_avatar);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        boolean forGroup = getIntent().getIntExtra(EXTRA_AVATAR_PURPOSE, AVATAR_PURPOSE_USER) == AVATAR_PURPOSE_GROUP;
        setTitle(forGroup ? R.string.group_avatar_picker_title : R.string.avatar_picker_title);

        final Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        mediaThumbnailLoader = new MediaThumbnailLoader(this, Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));

        final View setButton = findViewById(R.id.done);
        setButton.setOnClickListener(v -> {
            viewModel.preparePost().observe(this, result -> {
                if (result != null) {
                    Intent resultIntent = new Intent();

                    resultIntent.putExtra(RESULT_AVATAR_HASH, result.first.hash);
                    resultIntent.putExtra(RESULT_AVATAR_HEIGHT, result.first.height);
                    resultIntent.putExtra(RESULT_AVATAR_WIDTH, result.first.width);
                    resultIntent.putExtra(RESULT_AVATAR_FILE_PATH, result.first.filePath);

                    resultIntent.putExtra(RESULT_LARGE_AVATAR_HASH, result.second.hash);
                    resultIntent.putExtra(RESULT_LARGE_AVATAR_HEIGHT, result.second.height);
                    resultIntent.putExtra(RESULT_LARGE_AVATAR_WIDTH, result.second.width);
                    resultIntent.putExtra(RESULT_LARGE_AVATAR_FILE_PATH, result.second.filePath);

                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    CenterToast.show(getApplicationContext(), R.string.could_not_set_avatar);
                }
            });
        });

        imageView = findViewById(R.id.image);

        final View resetButton = findViewById(R.id.reset);
        resetButton.setOnClickListener(v -> imageView.getAttacher().update());

        final View progressView = findViewById(R.id.progress);

        final Uri uri = getIntent().getData();
        if (uri != null) {
            progressView.setVisibility(View.VISIBLE);
            setButton.setEnabled(false);

            viewModel = new ViewModelProvider(this, new AvatarPreviewViewModel.AvatarPreviewViewModelFactory(getApplication(), uri)).get(AvatarPreviewViewModel.class);
            viewModel.media.observe(this, mediaItem -> {
                progressView.setVisibility(View.GONE);
                if (mediaItem != null) {
                    imageView.setSinglePointerDragStartDisabled(false);
                    imageView.setReturnToMinScaleOnUp(false);
                    imageView.setOnCropListener(rect -> viewModel.setCropRect(rect));
                    imageView.setGridEnabled(false);
                    mediaThumbnailLoader.load(imageView, mediaItem);
                    imageView.setVisibility(View.VISIBLE);
                } else {
                    SnackbarHelper.showWarning(this, R.string.failed_to_load_media);
                }
                setButton.setEnabled(true);
            });
        } else {
            Log.e("AvatarPreviewActivity no uris provided");
            finish();
        }

        cropOverlay = findViewById(R.id.cropOverlay);

        cropOverlay.setForm(forGroup ? AvatarCropOverlay.Form.SQUARE : AvatarCropOverlay.Form.CIRCLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaThumbnailLoader.destroy();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.profile_photo_crop_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.rotate) {
            imageView.setRotationBy(270);
            viewModel.rotate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
