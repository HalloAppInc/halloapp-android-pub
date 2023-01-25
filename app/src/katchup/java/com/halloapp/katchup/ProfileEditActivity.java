package com.halloapp.katchup;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.halloapp.proto.server.Link;
import com.halloapp.proto.server.UserProfile;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.DialogFragmentUtils;
import com.halloapp.util.FileUtils;
import com.halloapp.util.KeyboardUtils;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.SetBioResponseIq;
import com.halloapp.xmpp.SetLinkResponseIq;
import com.halloapp.xmpp.UsernameResponseIq;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class ProfileEditActivity extends AppCompatActivity {

    public static Intent open(@NonNull Context context) {
        return new Intent(context, ProfileEditActivity.class);
    }

    private static final int REQUEST_CODE_GALLERY = 1;
    private static final int REQUEST_CODE_CAMERA = 2;
    private static final int REQUEST_CODE_EDIT_PICTURE = 3;
    private static final int REQUEST_CODE_EDIT_BIO = 4;
    private static final int REQUEST_CODE_EDIT_LINKS = 5;

    private ProfileEditViewModel viewModel;
    private ImageView profilePicture;
    private EditText usernameView;
    private EditText nameView;

    private final TextWatcher nameTextChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            viewModel.setName(s.toString());
        }
    };

    private final TextWatcher usernameTextChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            viewModel.setUsername(s.toString());
        }
    };

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
        done.setOnClickListener(v -> {
            KeyboardUtils.hideSoftKeyboard(nameView);

            viewModel.save().observe(this, error -> {
                if (error == null) {
                    finish();
                } else {
                    SnackbarHelper.showWarning(this, error);
                }
            });
        });

        profilePicture = findViewById(R.id.profile_picture);
        profilePicture.setOnClickListener(v -> updateProfilePicture());

        View profilePictureCamera = findViewById(R.id.profile_picture_camera);
        profilePictureCamera.setOnClickListener(v -> updateProfilePicture());

        usernameView = findViewById(R.id.username);
        nameView = findViewById(R.id.name);
        EditText bioView = findViewById(R.id.bio);
        TextView linksView = findViewById(R.id.links);

        nameView.addTextChangedListener(nameTextChangedListener);
        usernameView.addTextChangedListener(usernameTextChangedListener);

        KAvatarLoader.getInstance().loadLarge(profilePicture, UserId.ME, null);

        viewModel = new ViewModelProvider(this, new ProfileEditViewModel.Factory(getApplication())).get(ProfileEditViewModel.class);

        viewModel.profilePicture.observe(this, media -> {
            if (media != null) {
                mediaLoader.cancel(profilePicture);
                mediaLoader.remove(media.file);
                mediaLoader.load(profilePicture, media);
            } else {
                profilePicture.setImageDrawable(KAvatarLoader.getInstance().getDefaultAvatar(this, UserId.ME));
            }
        });

        viewModel.profile.observe(this, profile -> {
            if (!Objects.equals(profile.name, nameView.getText().toString())) {
                nameView.setText(profile.name);
            }

            if (!Objects.equals(profile.username, usernameView.getText().toString())) {
                usernameView.setText(profile.username);
            }

            if (!Objects.equals(profile.bio, bioView.getText().toString())) {
                bioView.setText(profile.bio);
            }

            int count = profile.getLinkCount();

            if (count > 0) {
                linksView.setText(String.format(Locale.getDefault(), "%d", count));
            } else {
                linksView.setText(R.string.links_hint);
            }
        });

        viewModel.isUsernameValid.observe(this, valid -> {
            if (valid == null || valid) {
                usernameView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white_40)));
                done.setEnabled(true);
            } else {
                usernameView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.color_primary)));
                done.setEnabled(false);
            }
        });

        bioView.setOnClickListener(v -> {
            UserProfileInfo profileInfo = viewModel.profile.getValue();

            if (profileInfo != null) {
                startActivityForResult(ProfileBioEditActivity.open(this, profileInfo.bio), REQUEST_CODE_EDIT_BIO);
            }
        });

        linksView.setOnClickListener(v -> {
            UserProfileInfo profileInfo = viewModel.profile.getValue();

            if (profileInfo != null) {
                Intent intent = ProfileLinksActivity.open(this, profileInfo.link, profileInfo.tiktok, profileInfo.instagram, profileInfo.snapchat);
                startActivityForResult(intent, REQUEST_CODE_EDIT_LINKS);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK || data == null) {
            return;
        }

        if (requestCode == REQUEST_CODE_GALLERY) {
            viewModel.copyUneditedProfilePicture(data.getData()).observe(this, uri -> {
                startActivityForResult(ProfilePictureCropActivity.open(this, uri), REQUEST_CODE_EDIT_PICTURE);
            });
        } else if  (requestCode == REQUEST_CODE_CAMERA) {
            Uri uri = data.getData();
            viewModel.setUneditedProfilePicture(uri);
            startActivityForResult(ProfilePictureCropActivity.open(this, uri), REQUEST_CODE_EDIT_PICTURE);
        } else if (requestCode == REQUEST_CODE_EDIT_PICTURE) {
            Uri small = data.getParcelableExtra(ProfilePictureCropActivity.EXTRA_PICTURE);
            Uri large = data.getParcelableExtra(ProfilePictureCropActivity.EXTRA_LARGE_PICTURE);

            if (small != null && large != null) {
                viewModel.setProfilePicture(small, large);
            }
        } else if (requestCode == REQUEST_CODE_EDIT_BIO) {
            viewModel.setBio(data.getStringExtra(ProfileBioEditActivity.EXTRA_BIO));
        } else if (requestCode == REQUEST_CODE_EDIT_LINKS) {
            String link = data.getStringExtra(ProfileLinksActivity.EXTRA_LINK);
            String tiktok = data.getStringExtra(ProfileLinksActivity.EXTRA_TIKTOK);
            String instagram = data.getStringExtra(ProfileLinksActivity.EXTRA_INSTAGRAM);
            String snapchat = data.getStringExtra(ProfileLinksActivity.EXTRA_SNAPCHAT);

            viewModel.setLinks(link, tiktok, instagram, snapchat);
        }
    }

    private void updateProfilePicture() {
        UserProfileInfo profileInfo = viewModel.profile.getValue();
        if (profileInfo == null) {
            return;
        }

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
        private UserId userId;
        private String avatarId;
        private String name;
        private String username;
        private String bio;
        private String link;
        private String tiktok;
        private String instagram;
        private String snapchat;

        public UserProfileInfo(@NonNull UserId userId, @Nullable String avatarId, String name, String username, String bio, String link, String tiktok, String instagram, String snapchat) {
            this.userId = userId;
            this.avatarId = avatarId;
            this.name = name;
            this.username = username;
            this.bio = bio;
            this.link = link;
            this.tiktok = tiktok;
            this.instagram = instagram;
            this.snapchat = snapchat;
        }

        public int getLinkCount() {
            int count = 0;

            if (!TextUtils.isEmpty(link)) {
                count++;
            }

            if (!TextUtils.isEmpty(instagram)) {
                count++;
            }

            if (!TextUtils.isEmpty(tiktok)) {
                count++;
            }

            if (!TextUtils.isEmpty(snapchat)) {
                count++;
            }

            return count;
        }
    }

    public static class ProfileEditViewModel extends AndroidViewModel {
        private static final long USERNAME_AVAILABILITY_DELAY = 300;

        private final Me me = Me.getInstance();
        private final Connection connection = Connection.getInstance();
        private final BgWorkers bgWorkers = BgWorkers.getInstance();
        private final KAvatarLoader kAvatarLoader = KAvatarLoader.getInstance();

        public final MutableLiveData<UserProfileInfo> profile = new MutableLiveData<>();
        public final MutableLiveData<Media> profilePicture = new MutableLiveData<>();
        public final MutableLiveData<Boolean> isUsernameValid = new MutableLiveData<>();

        private UserProfileInfo originalProfile;
        private Uri uneditedProfilePicture;
        private Uri smallProfilePicture;
        private Uri largeProfilePicture;
        private boolean removeProfilePicture;
        private boolean isSaving = false;
        private Timer usernameAvailabilityTimer;

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

        @NonNull
        public LiveData<String> save() {
            MutableLiveData<String> error = new MutableLiveData<>();
            UserProfileInfo profileInfo = profile.getValue();

            if (isSaving || profileInfo == null) {
                return error;
            }

            isSaving = true;

            bgWorkers.execute(() -> {
                try {
                    if (removeProfilePicture) {
                        removeAvatar();
                    } else if (smallProfilePicture != null && largeProfilePicture != null) {
                        updateProfilePicture();
                    }

                    if (profileInfo.name != null && !TextUtils.isEmpty(profileInfo.name.trim()) && !profileInfo.name.equals(originalProfile.name)) {
                        connection.sendName(profileInfo.name.trim()).await();
                    }

                    if (profileInfo.username != null && !TextUtils.isEmpty(profileInfo.username.trim()) && !profileInfo.username.equals(originalProfile.username)) {
                        String username = profileInfo.username.trim();
                        if (isUsernameValid(username)) {
                            UsernameResponseIq res = connection.sendUsername(username).await();

                            if (!res.success) {
                                switch (res.reason) {
                                    case UsernameResponseIq.Reason.TOO_SHORT:
                                        Log.d("ProfileEditViewModel.save: username too short");
                                        error.postValue(getApplication().getResources().getString(R.string.reg_username_error_too_short));
                                        break;
                                    case UsernameResponseIq.Reason.TOO_LONG:
                                        Log.d("ProfileEditViewModel.save: username too long");
                                        error.postValue(getApplication().getResources().getString(R.string.reg_username_error_too_long));
                                        break;
                                    case UsernameResponseIq.Reason.BAD_EXPRESSION:
                                        Log.d("ProfileEditViewModel.save: username bad expression");
                                        error.postValue(getApplication().getResources().getString(R.string.reg_username_error_bad_expression));
                                        break;
                                    case UsernameResponseIq.Reason.NOT_UNIQUE:
                                        Log.d("ProfileEditViewModel.save: username not unique");
                                        error.postValue(getApplication().getResources().getString(R.string.reg_username_error_not_unique, username));
                                        break;
                                    case UsernameResponseIq.Reason.UNKNOWN:
                                        Log.d("ProfileEditViewModel.save: username failed with unknown reason");
                                        error.postValue(getApplication().getResources().getString(R.string.reg_username_error_unknown));
                                        break;
                                    default:
                                        Log.e("ProfileEditViewModel.save: username failed with unexpected reason " + res.reason);
                                        error.postValue(getApplication().getResources().getString(R.string.reg_username_error_unknown));
                                }

                                isSaving = false;
                                return;
                            }
                        }
                    }

                    if (profileInfo.bio != null && !profileInfo.bio.equals(originalProfile.bio)) {
                        SetBioResponseIq res = connection.sendBio(profileInfo.bio.trim()).await();

                        if (!res.success) {
                            switch (res.reason) {
                                case SetBioResponseIq.Reason.TOO_LONG:
                                    Log.d("ProfileEditViewModel.save: bio too long");
                                    error.postValue(getApplication().getResources().getString(R.string.bio_too_long));
                                    break;
                                case SetBioResponseIq.Reason.UNKNOWN:
                                    Log.d("ProfileEditViewModel.save: bio failed with unknown reason");
                                    error.postValue(getApplication().getResources().getString(R.string.error_unknown));
                                    break;
                                default:
                                    Log.e("ProfileEditViewModel.save: bio failed with unexpected reason " + res.reason);
                                    error.postValue(getApplication().getResources().getString(R.string.error_unknown));
                            }

                            isSaving = false;
                            return;
                        }
                    }

                    if (profileInfo.tiktok != null && !profileInfo.tiktok.equals(originalProfile.tiktok)) {
                        SetLinkResponseIq res = connection.sendTikTokLink(profileInfo.tiktok).await();

                        if (!res.success) {
                            switch (res.reason) {
                                case SetLinkResponseIq.Reason.UNKNOWN:
                                    Log.d("ProfileEditViewModel.save: tiktok link failed with unknown reason");
                                    error.postValue(getApplication().getResources().getString(R.string.error_unknown));
                                    break;
                                case SetLinkResponseIq.Reason.BAD_TYPE:
                                    Log.e("ProfileEditViewModel.save: tiktok link with bad type");
                                    break;
                                default:
                                    Log.e("ProfileEditViewModel.save: tiktok link failed with unexpected reason " + res.reason);
                                    error.postValue(getApplication().getResources().getString(R.string.error_unknown));
                            }

                            isSaving = false;
                            return;
                        }
                    }

                    if (profileInfo.instagram != null && !profileInfo.instagram.equals(originalProfile.instagram)) {
                        SetLinkResponseIq res = connection.sendInstagramLink(profileInfo.instagram).await();

                        if (!res.success) {
                            switch (res.reason) {
                                case SetLinkResponseIq.Reason.UNKNOWN:
                                    Log.d("ProfileEditViewModel.save: instagram link failed with unknown reason");
                                    error.postValue(getApplication().getResources().getString(R.string.error_unknown));
                                    break;
                                case SetLinkResponseIq.Reason.BAD_TYPE:
                                    Log.e("ProfileEditViewModel.save: instagram link with bad type");
                                    break;
                                default:
                                    Log.e("ProfileEditViewModel.save: instagram link failed with unexpected reason " + res.reason);
                                    error.postValue(getApplication().getResources().getString(R.string.error_unknown));
                            }

                            isSaving = false;
                            return;
                        }
                    }

                    if (profileInfo.snapchat != null && !profileInfo.snapchat.equals(originalProfile.snapchat)) {
                        SetLinkResponseIq res = connection.sendSnapchatLink(profileInfo.snapchat).await();

                        if (!res.success) {
                            switch (res.reason) {
                                case SetLinkResponseIq.Reason.UNKNOWN:
                                    Log.d("ProfileEditViewModel.save: snapchat link failed with unknown reason");
                                    error.postValue(getApplication().getResources().getString(R.string.error_unknown));
                                    break;
                                case SetLinkResponseIq.Reason.BAD_TYPE:
                                    Log.e("ProfileEditViewModel.save: snapchat link with bad type");
                                    break;
                                default:
                                    Log.e("ProfileEditViewModel.save: snapchat link failed with unexpected reason " + res.reason);
                                    error.postValue(getApplication().getResources().getString(R.string.error_unknown));
                            }

                            isSaving = false;
                            return;
                        }
                    }

                    if (profileInfo.link != null && !profileInfo.link.equals(originalProfile.link)) {
                        SetLinkResponseIq res = connection.sendUserDefinedLink(profileInfo.link).await();

                        if (!res.success) {
                            switch (res.reason) {
                                case SetLinkResponseIq.Reason.UNKNOWN:
                                    Log.d("ProfileEditViewModel.save: user defined link failed with unknown reason");
                                    error.postValue(getApplication().getResources().getString(R.string.error_unknown));
                                    break;
                                case SetLinkResponseIq.Reason.BAD_TYPE:
                                    Log.e("ProfileEditViewModel.save: user defined link with bad type");
                                    break;
                                default:
                                    Log.e("ProfileEditViewModel.save: user defined link failed with unexpected reason " + res.reason);
                                    error.postValue(getApplication().getResources().getString(R.string.error_unknown));
                            }

                            isSaving = false;
                            return;
                        }
                    }
                } catch (InterruptedException | ObservableErrorException e) {
                    Log.e("ProfileEditViewModel.save: upload interrupted", e);
                    error.postValue(getApplication().getResources().getString(R.string.error_unknown));
                    isSaving = false;
                    return;
                }

                isSaving = false;
                error.postValue(null);
            });

            return error;
        }

        public void setUneditedProfilePicture(@NonNull Uri uri) {
            if (uneditedProfilePicture != null && !uneditedProfilePicture.equals(uri)) {
                final Uri uriToRemove = uneditedProfilePicture;
                bgWorkers.execute(() -> clearFile(uriToRemove));
            }
            uneditedProfilePicture = uri;
        }

        public LiveData<Uri> copyUneditedProfilePicture(@NonNull Uri uri) {
            MutableLiveData<Uri> result = new MutableLiveData<>();

            bgWorkers.execute(() -> {
                File file = FileStore.getInstance().getTmpFile(RandomId.create() + "." + Media.getFileExt(Media.MEDIA_TYPE_IMAGE));

                FileUtils.uriToFile(getApplication(), uri, file);
                if (uneditedProfilePicture != null) {
                    clearFile(uneditedProfilePicture);
                }
                uneditedProfilePicture = Uri.fromFile(file);

                result.postValue(uneditedProfilePicture);
            });

            return result;
        }

        public void setProfilePicture(@NonNull Uri small, @NonNull Uri large) {
            removeProfilePicture = false;

            bgWorkers.execute(() -> {
                if (smallProfilePicture != null && !smallProfilePicture.equals(small)) {
                    clearFile(smallProfilePicture);
                }

                if (largeProfilePicture != null && !largeProfilePicture.equals(large)) {
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
                    String username = userProfile.getUsername();
                    String bio = userProfile.getBio();
                    String link = null, instagram = null, snapchat = null, tiktok = null;

                    for (Link linkItem : userProfile.getLinksList()) {
                        if (linkItem.getType() == Link.Type.TIKTOK) {
                            tiktok = linkItem.getText();
                        } else if (linkItem.getType() == Link.Type.INSTAGRAM) {
                            instagram = linkItem.getText();
                        } else if (linkItem.getType() == Link.Type.SNAPCHAT) {
                            snapchat = linkItem.getText();
                        } else if (linkItem.getType() == Link.Type.USER_DEFINED) {
                            link = linkItem.getText();
                        }
                    }

                    originalProfile = new UserProfileInfo(userId, userProfile.getAvatarId(), name, username, bio, link, tiktok, instagram, snapchat);
                    profile.postValue(new UserProfileInfo(userId, userProfile.getAvatarId(), name, username, bio, link, tiktok, instagram, snapchat));
                }).onError(err -> {
                    Log.e("Failed to get profile info", err);
                });
            });
        }

        public void setName(String name) {
            UserProfileInfo profileInfo = profile.getValue();

            if (profileInfo != null) {
                profileInfo.name = name;
                profile.postValue(profileInfo);
            }
        }

        public void setUsername(String username) {
            if (!isUsernameValid(username.trim())) {
                isUsernameValid.postValue(false);
                return;
            }

            UserProfileInfo profileInfo = profile.getValue();

            if (profileInfo != null) {
                profileInfo.username = username;
                profile.postValue(profileInfo);

                if (usernameAvailabilityTimer != null) {
                    usernameAvailabilityTimer.cancel();
                }

                if (!TextUtils.isEmpty(username) && !username.equals(originalProfile.username)) {
                    usernameAvailabilityTimer = new Timer();
                    usernameAvailabilityTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            connection.checkUsernameIsAvailable(username).onResponse(res -> {
                                if (res != null) {
                                    isUsernameValid.postValue(res.success);
                                }
                            });
                        }
                    }, USERNAME_AVAILABILITY_DELAY);
                }
            }
        }

        private boolean isUsernameValid(String username) {
            return !TextUtils.isEmpty(username) &&
                    Character.isAlphabetic(username.charAt(0)) &&
                    username.matches(Constants.USERNAME_CHARACTERS_REGEX) &&
                    username.length() >= Constants.MIN_USERNAME_LENGTH &&
                    username.length() <= Constants.MAX_USERNAME_LENGTH;
        }

        public void setBio(String bio) {
            UserProfileInfo profileInfo = profile.getValue();

            if (profileInfo != null) {
                profileInfo.bio = bio;
                profile.postValue(profileInfo);
            }
        }

        public void setLinks(@Nullable String link, @Nullable String tiktok, @Nullable String instagram, @Nullable String snapchat) {
            UserProfileInfo profileInfo = profile.getValue();

            if (profileInfo != null) {
                profileInfo.link = link;
                profileInfo.tiktok = tiktok;
                profileInfo.instagram = instagram;
                profileInfo.snapchat = snapchat;
                profile.postValue(profileInfo);
            }
        }

        @WorkerThread
        private void updateProfilePicture() throws InterruptedException, ObservableErrorException {
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
