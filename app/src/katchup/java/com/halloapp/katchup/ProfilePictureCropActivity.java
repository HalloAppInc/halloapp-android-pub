package com.halloapp.katchup;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.media.MediaUtils;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.CropPhotoView;

import java.io.File;
import java.io.IOException;

public class ProfilePictureCropActivity extends HalloActivity {

    public static final String EXTRA_PICTURE = "picture";
    public static final String EXTRA_LARGE_PICTURE = "large_picture";

    public static Intent open(@NonNull Context context, @NonNull Uri uri) {
        Intent intent = new Intent(context, ProfilePictureCropActivity.class);
        intent.setData(uri);

        return intent;
    }

    private MediaThumbnailLoader mediaLoader;
    private ProfilePictureCropViewModel viewModel;
    private CropPhotoView profilePictureView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_picture_crop);

        final Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        mediaLoader = new MediaThumbnailLoader(this, Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));

        View prev = findViewById(R.id.prev);
        prev.setOnClickListener(v -> finish());

        View done = findViewById(R.id.done);
        done.setOnClickListener(v -> {
            viewModel.getCrop().observe(this, crop -> {
                if (crop != null) {
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_PICTURE, crop.small);
                    intent.putExtra(EXTRA_LARGE_PICTURE, crop.large);

                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        });

        profilePictureView = findViewById(R.id.profile_picture);
        profilePictureView.setSinglePointerDragStartDisabled(false);
        profilePictureView.setReturnToMinScaleOnUp(false);
        profilePictureView.setGridEnabled(false);
        profilePictureView.setOnCropListener(rect -> viewModel.setCropRect(rect));

        Uri uri = getIntent().getData();
        viewModel = new ViewModelProvider(this, new ProfilePictureCropViewModel.Factory(uri)).get(ProfilePictureCropViewModel.class);
        viewModel.media.observe(this, media -> {
            if (media != null) {
                mediaLoader.load(profilePictureView, media);
            }
        });
    }

    public static class CropResult {
        Uri small, large;

        CropResult(Uri small, Uri large) {
            this.small = small;
            this.large = large;
        }
    }

    public static class ProfilePictureCropViewModel extends ViewModel {

        public final MutableLiveData<Media> media = new MutableLiveData<>();

        private final BgWorkers bgWorkers = BgWorkers.getInstance();
        private RectF cropRect;

        public ProfilePictureCropViewModel(@Nullable Uri uri) {
            if (uri != null) {
                load(uri);
            }
        }

        private void load(@NonNull Uri uri) {
            bgWorkers.execute(() -> {
                File file = new File(uri.getPath());
                media.postValue(Media.createFromFile(Media.MEDIA_TYPE_IMAGE, file));
            });
        }

        private void setCropRect(RectF cropRect) {
            this.cropRect = cropRect;
        }

        private LiveData<CropResult> getCrop() {
            MutableLiveData<CropResult> result = new MutableLiveData<>();

            Media item = media.getValue();
            if (item == null) {
                return result;
            }

            bgWorkers.execute(() -> {
                File original = item.file;
                File small = FileStore.getInstance().getTmpFile("avatar-small");
                File large = FileStore.getInstance().getTmpFile("avatar-large");

                try {
                    MediaUtils.cropImage(original, small, cropRect, Constants.MAX_AVATAR_DIMENSION);
                    MediaUtils.cropImage(original, large, cropRect, Constants.MAX_LARGE_AVATAR_DIMENSION);
                    result.postValue(new CropResult(Uri.fromFile(small), Uri.fromFile(large)));
                } catch (IOException e) {
                    Log.e("failed to crop image", e);
                }
            });

            return result;
        }

        public static class Factory implements ViewModelProvider.Factory {
            private final Uri uri;

            public Factory(@Nullable Uri uri) {
                this.uri = uri;
            }

            @Override
            public @NonNull
            <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(ProfilePictureCropViewModel.class)) {
                    //noinspection unchecked
                    return (T) new ProfilePictureCropViewModel(uri);
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        }
    }
}
