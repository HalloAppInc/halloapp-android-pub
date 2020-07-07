package com.halloapp.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.CenterToast;
import com.theartofdev.edmodo.cropper.CropImageView;

public class CropImageActivity extends HalloActivity {

    public static final String EXTRA_OUTPUT = "output";
    public static final String EXTRA_STATE = "state";

    private CropImageView cropImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ImageCropActivity: onCreate");
        setContentView(R.layout.activity_image_crop);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        cropImageView = findViewById(R.id.image);
        cropImageView.setMinAspectRatio(1 / Constants.MAX_IMAGE_ASPECT_RATIO);

        findViewById(R.id.reset).setOnClickListener(v -> cropImageView.resetCropRect());
        findViewById(R.id.done).setOnClickListener(v -> {
            cropImageView.saveCroppedImageAsync(getIntent().getParcelableExtra(EXTRA_OUTPUT),
                    Bitmap.CompressFormat.JPEG, Constants.JPEG_QUALITY, Constants.MAX_IMAGE_DIMENSION, Constants.MAX_IMAGE_DIMENSION, CropImageView.RequestSizeOptions.RESIZE_INSIDE);
        });

        cropImageView.setOnSetImageUriCompleteListener((view, uri, error) -> {
            if (error != null) {
                CenterToast.show(this, R.string.failed_to_load_media);
                finish();
            } else {
                final Parcelable state = getIntent().getParcelableExtra(EXTRA_STATE);
                if (state == null) {
                    Rect rect = cropImageView.getWholeImageRect();
                    final int w;
                    final int h;
                    if (cropImageView.getRotatedDegrees() % 180 == 0) {
                        w = rect.width();
                        h = rect.height();
                    } else {
                        w = rect.height();
                        h = rect.width();
                    }
                    final Rect cropRect;
                    if (h > Constants.MAX_IMAGE_ASPECT_RATIO * w) {
                        int padding = (int) ((h - Constants.MAX_IMAGE_ASPECT_RATIO * w) / 2);
                        cropRect = new Rect(0, padding, w, h - padding);
                    } else {
                        cropRect = new Rect(0, 0, w, h);
                    }
                    cropImageView.setCropRect(cropRect);
                }
            }
        });
        cropImageView.setOnCropImageCompleteListener((view, result) -> {
            if (result.getError() == null) {
                final Intent intent = new Intent();
                intent.putExtra(EXTRA_STATE, cropImageView.onSaveInstanceState());
                intent.setData(result.getOriginalUri());
                //cropImageView.getCropRect()
                setResult(RESULT_OK, intent);
            } else {
                CenterToast.show(this, R.string.failed_to_load_media);
            }
            finish();
        });

        final Parcelable state = getIntent().getParcelableExtra(EXTRA_STATE);
        if (state != null) {
            cropImageView.onRestoreInstanceState(state);
        } else {
            cropImageView.setImageUriAsync(getIntent().getData());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ImageCropActivity: onDestroy");
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.image_crop_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.rotate: {
                cropImageView.rotateImage(270);
                return true;
            }
            case R.id.flip: {
                cropImageView.flipImageHorizontally();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }
}
