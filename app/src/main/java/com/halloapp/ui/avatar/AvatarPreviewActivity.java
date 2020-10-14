package com.halloapp.ui.avatar;

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

    private AvatarPreviewViewModel viewModel;
    private MediaThumbnailLoader mediaThumbnailLoader;
    private CropPhotoView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_set_avatar);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        mediaThumbnailLoader = new MediaThumbnailLoader(this, Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));

        final View setButton = findViewById(R.id.done);
        setButton.setOnClickListener(v -> {
            viewModel.preparePost().observe(this, result -> {
                if (result != null) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(RESULT_AVATAR_HASH, result.hash);
                    resultIntent.putExtra(RESULT_AVATAR_HEIGHT, result.height);
                    resultIntent.putExtra(RESULT_AVATAR_WIDTH, result.width);
                    resultIntent.putExtra(RESULT_AVATAR_FILE_PATH, result.filePath);
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
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case R.id.rotate: {
                imageView.setRotationBy(270);
                viewModel.rotate();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }
}
