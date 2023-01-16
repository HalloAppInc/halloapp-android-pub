package com.halloapp.katchup;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.id.UserId;
import com.halloapp.katchup.avatar.KAvatarLoader;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.proto.server.UserProfile;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.DialogFragmentUtils;
import com.halloapp.util.FileUtils;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ProfileEditActivity extends AppCompatActivity {

    public static Intent open(Context context) {
        return new Intent(context, ProfileEditActivity.class);
    }

    private static final int REQUEST_CODE_GALLERY = 1;
    private static final int REQUEST_CODE_CAMERA = 2;
    private static final int REQUEST_CODE_EDIT_PICTURE = 3;

    private ProfileEditViewModel viewModel;
    private ImageView profilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        final Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        MediaThumbnailLoader mediaLoader = new MediaThumbnailLoader(this, Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));

        View prev = findViewById(R.id.prev);
        prev.setOnClickListener(v -> finish());

        View done = findViewById(R.id.done);
        done.setOnClickListener(v -> viewModel.save(this::finish));

        profilePicture = findViewById(R.id.profile_picture);
        profilePicture.setOnClickListener(v -> updateProfilePicture());

        View profilePictureCamera = findViewById(R.id.profile_picture_camera);
        profilePictureCamera.setOnClickListener(v -> updateProfilePicture());

        KAvatarLoader.getInstance().loadLarge(profilePicture, UserId.ME, null);

        viewModel = new ViewModelProvider(this, new ProfileEditViewModel.Factory(getApplication())).get(ProfileEditViewModel.class);

        viewModel.profilePicture.observe(this, media -> {
            if (media != null) {
                mediaLoader.load(profilePicture, media);
            } else {
                profilePicture.setImageDrawable(KAvatarLoader.getInstance().getDefaultAvatar(this, UserId.ME));
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK && data != null) {
            viewModel.copyUneditedProfilePicture(data.getData()).observe(this, uri -> {
                startActivityForResult(ProfilePictureCropActivity.open(this, uri), REQUEST_CODE_EDIT_PICTURE);
            });
        } else if  (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            viewModel.setUneditedProfilePicture(uri);
            startActivityForResult(ProfilePictureCropActivity.open(this, uri), REQUEST_CODE_EDIT_PICTURE);
        } else if (requestCode == REQUEST_CODE_EDIT_PICTURE && resultCode == Activity.RESULT_OK && data != null) {
            Uri small = data.getParcelableExtra(ProfilePictureCropActivity.EXTRA_PICTURE);
            Uri large = data.getParcelableExtra(ProfilePictureCropActivity.EXTRA_LARGE_PICTURE);

            if (small != null && large != null) {
                viewModel.setProfilePicture(small, large);
            }
        }
    }

    private void updateProfilePicture() {
        ProfilePictureBottomSheetDialogFragment bottomSheetDialogFragment = new ProfilePictureBottomSheetDialogFragment(action -> {
            switch (action) {
                case ProfilePictureBottomSheetDialogFragment.ACTION_GALLERY:
                    openGallery();
                    break;
                case ProfilePictureBottomSheetDialogFragment.ACTION_CAMERA:
                    openCamera();
                    break;
                case ProfilePictureBottomSheetDialogFragment.ACTION_REMOVE:
                    viewModel.removeProfilePicture();
                    break;
            }
        });

        DialogFragmentUtils.showDialogFragmentOnce(bottomSheetDialogFragment, getSupportFragmentManager());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_GALLERY);
    }

    private void openCamera() {
        startActivityForResult(ProfilePictureCameraActivity.open(this), REQUEST_CODE_CAMERA);
    }

    private static class UserProfileInfo {
        private final UserId userId;
        private final String avatarId;
        private final String name;
        private final String username;
        private final String bio;
        private final int linkCount;

        public UserProfileInfo(@NonNull UserId userId, @Nullable String avatarId, String name, String username, String bio, int linkCount) {
            this.userId = userId;
            this.avatarId = avatarId;
            this.name = name;
            this.username = username;
            this.bio = bio;
            this.linkCount = linkCount;
        }
    }

    public static class ProfileEditViewModel extends AndroidViewModel {

        private final Me me = Me.getInstance();
        private final Connection connection = Connection.getInstance();
        private final BgWorkers bgWorkers = BgWorkers.getInstance();
        private final KAvatarLoader kAvatarLoader = KAvatarLoader.getInstance();

        public final MutableLiveData<UserProfileInfo> profile = new MutableLiveData<>();
        public final MutableLiveData<Media> profilePicture = new MutableLiveData<>();

        private Uri uneditedProfilePicture;
        private Uri smallProfilePicture;
        private Uri largeProfilePicture;
        private boolean removeProfilePicture;
        private boolean isSaving = false;

        public ProfileEditViewModel(@NonNull Application application) {
            super(application);
            loadUserProfileInfo();
        }

        @Override
        protected void onCleared() {
            super.onCleared();

            bgWorkers.execute(() -> {
                if (uneditedProfilePicture != null) {
                    clearFile(uneditedProfilePicture);
                }

                if (smallProfilePicture != null) {
                    clearFile(smallProfilePicture);
                }

                if (largeProfilePicture != null) {
                    clearFile(largeProfilePicture);
                }
            });
        }

        @WorkerThread
        private void clearFile(@NonNull Uri uri) {
            File file = new File(uri.getPath());

            if (file.exists()) {
                file.delete();
            }
        }

        public void save(Runnable completionRunnable) {
            if (isSaving) {
                return;
            }

            isSaving = true;

            bgWorkers.execute(() -> {
                if (removeProfilePicture) {
                    removeAvatar();
                } else if (smallProfilePicture != null && largeProfilePicture != null) {
                    updateProfilePicture();
                }

                isSaving = false;
                completionRunnable.run();
            });
        }

        public void setUneditedProfilePicture(@NonNull Uri uri) {
            uneditedProfilePicture = uri;
        }

        public LiveData<Uri> copyUneditedProfilePicture(@NonNull Uri uri) {
            MutableLiveData<Uri> result = new MutableLiveData<>();

            bgWorkers.execute(() -> {
                File file = FileStore.getInstance().getTmpFile(RandomId.create() + "." + Media.getFileExt(Media.MEDIA_TYPE_IMAGE));

                FileUtils.uriToFile(getApplication(), uri, file);
                uneditedProfilePicture = Uri.fromFile(file);

                result.postValue(uneditedProfilePicture);
            });

            return result;
        }

        public void setProfilePicture(@NonNull Uri small, @NonNull Uri large) {
            removeProfilePicture = false;

            bgWorkers.execute(() -> {
                if (smallProfilePicture != null) {
                    clearFile(smallProfilePicture);
                }

                if (largeProfilePicture != null) {
                    clearFile(largeProfilePicture);
                }

                smallProfilePicture = small;
                largeProfilePicture = large;

                File file = new File(large.getPath());
                profilePicture.postValue(Media.createFromFile(Media.MEDIA_TYPE_IMAGE, file));
            });
        }

        public void removeProfilePicture() {
            removeProfilePicture = true;
            profilePicture.setValue(null);
        }

        private void loadUserProfileInfo() {
            bgWorkers.execute(() -> {
                UserId userId = new UserId(me.getUser());

                connection.getKatchupUserProfileInfo(userId, null).onResponse(res -> {
                    if (res == null) {
                        Log.e("Failed to get profile info, empty response");
                        return;
                    }

                    UserProfile userProfile = res.getUserProfileResult().getProfile();
                    String name = userProfile.getName();
                    String username = userProfile.getName();
                    String bio = userProfile.getBio();

                    UserProfileInfo profileInfo = new UserProfileInfo(userId, userProfile.getAvatarId(), name, username, bio, userProfile.getLinksList().size());
                    profile.postValue(profileInfo);
                }).onError(err -> {
                    Log.e("Failed to get profile info", err);
                });
            });
        }

        @WorkerThread
        private void updateProfilePicture() {
            File smallPictureFile = new File(smallProfilePicture.getPath());
            File largePictureFile = new File(largeProfilePicture.getPath());

            try (FileInputStream smallInputStream = new FileInputStream(smallPictureFile);
                 FileInputStream largeInputStream = new FileInputStream(largePictureFile)) {
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int count;

                while ((count = smallInputStream.read(buf)) != -1) {
                    byteStream.write(buf, 0, count);
                }
                byte[] smallPictureBytes = byteStream.toByteArray();
                byteStream.reset();

                while ((count = largeInputStream.read(buf)) != -1) {
                    byteStream.write(buf, 0, count);
                }
                byte[] largePictureBytes = byteStream.toByteArray();

                String avatarId = connection.setAvatar(smallPictureBytes, largePictureBytes).await();
                if (avatarId == null) {
                    return;
                }

                kAvatarLoader.reportMyAvatarChanged(avatarId);
            } catch (IOException e) {
                Log.e("ProfileEditViewModel.updateProfilePicture: Failed to copy avatar files", e);
            } catch (InterruptedException | ObservableErrorException e) {
                Log.e("ProfileEditViewModel.updateProfilePicture: Upload interrupted", e);
            }
        }

        @WorkerThread
        private void removeAvatar() {
            connection.removeAvatar();
            kAvatarLoader.removeMyAvatar();
        }

        public static class Factory implements ViewModelProvider.Factory {
            private final Application application;

            public Factory(@NonNull Application application) {
                this.application = application;
            }

            @Override
            public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(ProfileEditViewModel.class)) {
                    //noinspection unchecked
                    return (T) new ProfileEditViewModel(application);
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        }
    }
}
