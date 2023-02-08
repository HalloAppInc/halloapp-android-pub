package com.halloapp.katchup;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.MainActivity;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.id.UserId;
import com.halloapp.katchup.avatar.KAvatarLoader;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.DialogFragmentUtils;
import com.halloapp.util.FileUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.widget.TextDrawable;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.UsernameResponseIq;
import com.halloapp.xmpp.util.Observable;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SetupUsernameProfileActivity extends HalloActivity {

    private static final String USERNAME_PREFIX = "@";
    private static final int DEBOUNCE_DELAY_MS = 300;
    private static final float ENTRY_USABLE_WIDTH_RATIO = 0.95f;

    private static final String EXTRA_NAME = "name";
    private static final int REQUEST_CODE_GALLERY = 1;
    private static final int REQUEST_CODE_CAMERA = 2;
    private static final int REQUEST_CODE_EDIT_PICTURE = 3;

    public static Intent open(Context context, @NonNull String name) {
        Intent intent = new Intent(context, SetupUsernameProfileActivity.class);
        intent.putExtra(EXTRA_NAME, name);
        return intent;
    }

    private TextDrawable usernamePrefixDrawable;
    private TextView usernameEditHint;
    private EditText usernameEditText;
    private View nextButton;
    private TextView usernameUniquenessInfo;
    private TextView usernameError;

    private ImageView avatarView;
    private ImageView avatarPlaceholder;

    private SetupUsernameViewModel viewModel;
    private Runnable debounceRunnable;
    private Observable<UsernameResponseIq> checkUsernameIsAvailable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setup_profile_username);

        final KAvatarLoader avatarLoader = KAvatarLoader.getInstance();
        final Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        final MediaThumbnailLoader mediaLoader = new MediaThumbnailLoader(this, Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));

        Analytics.getInstance().openScreen("onboardingUsername");

        final int nameTextSize = getResources().getDimensionPixelSize(R.dimen.registration_name_text_size);
        final int nameTextMinSize = getResources().getDimensionPixelSize(R.dimen.registration_name_text_min_size);
        final int regEntryHorizontalPadding = getResources().getDimensionPixelSize(R.dimen.reg_name_field_horizontal_padding);
        final int textColor = getResources().getColor(R.color.black_80);

        viewModel = new ViewModelProvider(this).get(SetupUsernameViewModel.class);

        final String name = Preconditions.checkNotNull(getIntent().getStringExtra(EXTRA_NAME));
        viewModel.setTempName(name);

        avatarPlaceholder = findViewById(R.id.avatar_placeholder);
        avatarView = findViewById(R.id.avatar);
        usernameUniquenessInfo = findViewById(R.id.username_uniqueness_info);
        usernameError = findViewById(R.id.username_error);

        final View avatarFrame = findViewById(R.id.avatar_frame);
        avatarFrame.setOnClickListener(v -> updateProfilePicture());

        nextButton = findViewById(R.id.next);
        nextButton.setEnabled(false);
        usernameEditHint = findViewById(R.id.username_hint);
        usernameEditText = findViewById(R.id.username);
        usernameEditText.setFilters(new InputFilter[] {
                (source, start, end, dest, dstart, dend) -> String.valueOf(source).toLowerCase(Locale.getDefault()),
                new InputFilter.LengthFilter(Constants.MAX_USERNAME_LENGTH)
        });
        final int prefixWidth = (int) StaticLayout.getDesiredWidth(USERNAME_PREFIX, usernameEditText.getPaint());
        usernamePrefixDrawable = new TextDrawable(USERNAME_PREFIX, nameTextSize, nameTextMinSize, 0, textColor);
        usernamePrefixDrawable.setBounds(0, 0, prefixWidth, nameTextSize);
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null) {
                    return;
                }

                final String username = s.toString();
                if (username.length() > 0) {
                    usernameEditHint.setVisibility(View.INVISIBLE);
                    usernameEditText.setCompoundDrawables(usernamePrefixDrawable, null, null, null);
                } else {
                    usernameEditHint.setVisibility(View.VISIBLE);
                    usernameEditText.setCompoundDrawables(null, null, null, null);
                }

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

                final float textWidth = StaticLayout.getDesiredWidth(username, usernameEditText.getPaint());
                final float availableWidth = displayMetrics.widthPixels - nameTextSize - 2 * regEntryHorizontalPadding;
                final float scaleFactor = availableWidth / textWidth;
                final int candidateTextSize = (int) (usernameEditText.getTextSize() * scaleFactor * ENTRY_USABLE_WIDTH_RATIO);
                usernameEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, Math.max(nameTextMinSize, Math.min(nameTextSize, candidateTextSize)));
                nextButton.setEnabled(false);

                if (debounceRunnable != null) {
                    nextButton.removeCallbacks(debounceRunnable);
                    debounceRunnable = null;
                }
                cancelCheckUsernameIsAvailable();
                if (validateUsernameInput(username)) {
                    debounceRunnable = () -> checkUsernameAvailability(username);
                    nextButton.postDelayed(debounceRunnable, DEBOUNCE_DELAY_MS);
                }
            }
        });
        usernameEditText.requestFocus();

        BgWorkers.getInstance().execute(() -> {
            if (avatarLoader.hasAvatar()) {
                runOnUiThread(() -> {
                    avatarView.setVisibility(View.VISIBLE);
                    avatarPlaceholder.setVisibility(View.GONE);
                    avatarLoader.loadLarge(avatarView, UserId.ME, null);
                });
            }
        });
        viewModel.profilePicture.observe(this, media -> {
            if (media != null) {
                avatarView.setVisibility(View.VISIBLE);
                avatarPlaceholder.setVisibility(View.GONE);
                // Cropped files are always saved in the same place. As a result we need to clear the cache and tag, to force mediaLoader reload of the new image.
                mediaLoader.cancel(avatarView);
                mediaLoader.remove(media.file);
                mediaLoader.load(avatarView, media);
            } else {
                avatarView.setVisibility(View.GONE);
                avatarPlaceholder.setVisibility(View.VISIBLE);
            }
        });
        viewModel.getSaveProfileWorkInfo().observe(this, new Observer<List<WorkInfo>>() {

            boolean running;

            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                for (WorkInfo workInfo : workInfos) {
                    final WorkInfo.State state = workInfo.getState();
                    Log.i("SettingsProfile: work " + workInfo.getId() + " " + state);
                    if (state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED) {
                        usernameEditText.setEnabled(false);
                        nextButton.setEnabled(false);
                        running = true;
                    } else if (running) {
                        if (state == WorkInfo.State.FAILED) {
                            SnackbarHelper.showWarning(SetupUsernameProfileActivity.this, R.string.failed_update_profile);
                            usernameEditText.setEnabled(true);
                            usernameEditText.requestFocus();
                            nextButton.setEnabled(true);
                        } else if (state == WorkInfo.State.SUCCEEDED) {
                            startActivity(new Intent(SetupUsernameProfileActivity.this, MainActivity.class));
                            finish();
                        }
                        running = false;
                    }
                }
            }
        });

        nextButton.setOnClickListener(v -> {
            final String username = StringUtils.preparePostText(Preconditions.checkNotNull(usernameEditText.getText()).toString());
            if (TextUtils.isEmpty(username)) {
                SnackbarHelper.showWarning(this, R.string.name_must_be_specified);
                usernameEditText.requestFocus();
                return;
            }
            viewModel.save();
        });
    }

    private void showError(@NonNull String errorMessage) {
        usernameUniquenessInfo.setVisibility(View.GONE);
        usernameError.setVisibility(View.VISIBLE);
        usernameError.setText(errorMessage);
    }

    private void hideError() {
        usernameUniquenessInfo.setVisibility(View.VISIBLE);
        usernameError.setVisibility(View.GONE);
    }

    private boolean validateUsernameInput(@NonNull String username) {
        String errorString = null;
        if (!username.isEmpty()) {
            if (!Character.isAlphabetic(username.charAt(0))) {
                errorString = getResources().getString(R.string.reg_username_error_leading_char_invalid);
            } else if (!username.matches(Constants.USERNAME_CHARACTERS_REGEX)) {
                errorString = getResources().getString(R.string.reg_username_error_bad_expression);
            } else if (username.length() < Constants.MIN_USERNAME_LENGTH) {
                errorString = getResources().getString(R.string.reg_username_error_too_short);
            }
        }

        if (errorString != null) {
            showError(errorString);
            return false;
        } else {
            hideError();
            return true;
        }
    }

    private void cancelCheckUsernameIsAvailable() {
        if (checkUsernameIsAvailable != null) {
            checkUsernameIsAvailable.cancel();
            checkUsernameIsAvailable = null;
        }
    }

    private void checkUsernameAvailability(@NonNull String username) {
        Log.d("SetupUsernameProfileActivity.checkUsernameAvailability: username=" + username);
        viewModel.setTempUsername(username);
        cancelCheckUsernameIsAvailable();
        if (!TextUtils.isEmpty(username)) {
            checkUsernameIsAvailable = Connection.getInstance().checkUsernameIsAvailable(username).onResponse(response -> {
                if (response != null && response.success) {
                    Analytics.getInstance().logOnboardingEnteredUsername(true, UsernameResponseIq.Reason.UNKNOWN);
                    runOnUiThread(this::onUsernameIsAvailable);
                } else {
                    final int reason = response != null ? response.reason : UsernameResponseIq.Reason.UNKNOWN;
                    Log.e("SetupUsernameProfileActivity.checkUsernameAvailability UsernameRequest.IS_AVAILABLE call failed with reason=" + reason);
                    Analytics.getInstance().logOnboardingEnteredUsername(false, reason);
                    runOnUiThread(() -> onUsernameIsAvailableError(reason, username));
                }
            }).onError(e -> {
                Log.e("SetupUsernameProfileActivity.checkUsernameAvailability: UsernameRequest.IS_AVAILABLE call failed", e);
                runOnUiThread(() -> onUsernameIsAvailableError(UsernameResponseIq.Reason.UNKNOWN, username));
            });
        }
    }

    private void onUsernameIsAvailable() {
        nextButton.setEnabled(true);
        hideError();
    }

    private void onUsernameIsAvailableError(@UsernameResponseIq.Reason int reason, @NonNull String username) {
        final String errorText;
        switch (reason) {
            case UsernameResponseIq.Reason.TOO_SHORT:
                errorText = getResources().getString(R.string.reg_username_error_too_short);
                break;
            case UsernameResponseIq.Reason.TOO_LONG:
                errorText = getResources().getString(R.string.reg_username_error_too_long);
                break;
            case UsernameResponseIq.Reason.BAD_EXPRESSION:
                errorText = getResources().getString(R.string.reg_username_error_bad_expression);
                break;
            case UsernameResponseIq.Reason.NOT_UNIQUE:
                errorText = getResources().getString(R.string.reg_username_error_not_unique, username);
                break;
            case UsernameResponseIq.Reason.UNKNOWN:
                errorText = getResources().getString(R.string.reg_username_error_unknown);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + reason);
        }
        showError(errorText);
    }

    private void updateProfilePicture() {
        ProfilePictureBottomSheetDialogFragment bottomSheetDialogFragment = new ProfilePictureBottomSheetDialogFragment(action -> {
            switch (action) {
                case ProfilePictureBottomSheetDialogFragment.ACTION_GALLERY:
                    Analytics.getInstance().openScreen("onboardingEditAvatar");
                    openGallery();
                    break;
                case ProfilePictureBottomSheetDialogFragment.ACTION_CAMERA:
                    Analytics.getInstance().openScreen("onboardingEditAvatar");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK && data != null) {
            viewModel.copyUneditedProfilePicture(data.getData()).observe(this, uri -> {
                startActivityForResult(ProfilePictureCropActivity.open(this, uri), REQUEST_CODE_EDIT_PICTURE);
            });
        } else if  (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK && data != null) {
            final Uri uri = data.getData();
            viewModel.setUneditedProfilePicture(uri);
            startActivityForResult(ProfilePictureCropActivity.open(this, uri), REQUEST_CODE_EDIT_PICTURE);
        } else if (requestCode == REQUEST_CODE_EDIT_PICTURE && resultCode == Activity.RESULT_OK && data != null) {
            final Uri small = data.getParcelableExtra(ProfilePictureCropActivity.EXTRA_PICTURE);
            final Uri large = data.getParcelableExtra(ProfilePictureCropActivity.EXTRA_LARGE_PICTURE);

            if (small != null && large != null) {
                viewModel.setProfilePicture(small, large);
            }
        }
    }

    public static class SetupUsernameViewModel extends AndroidViewModel {
        private final Me me = Me.getInstance();
        private final BgWorkers bgWorkers = BgWorkers.getInstance();
        private final WorkManager workManager;

        public final MutableLiveData<String> tempName = new MutableLiveData<>();
        public final MutableLiveData<String> tempUsername = new MutableLiveData<>();
        public final MutableLiveData<Media> profilePicture = new MutableLiveData<>();

        private Uri uneditedProfilePicture;
        private Uri smallProfilePicture;
        private Uri largeProfilePicture;
        private boolean removeProfilePicture;

        public SetupUsernameViewModel(@NonNull Application application) {
            super(application);
            workManager = WorkManager.getInstance(application);
        }

        public void setTempName(String name) {
            tempName.setValue(name);
        }

        public void setTempUsername(String username) {
            tempUsername.setValue(username);
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
            final File file = new File(uri.getPath());

            if (file.exists()) {
                file.delete();
            }
        }

        public void save() {
            final Data.Builder builder = new Data.Builder();
            final String tempNameValue = tempName.getValue();
            if (tempNameValue != null && !tempNameValue.equals(me.name.getValue())) {
                builder.putString(UpdateProfileWorker.WORKER_PARAM_NAME, tempNameValue);
            }
            final String tempUsernameValue = tempUsername.getValue();
            if (tempUsernameValue != null && !tempUsernameValue.equals(me.username.getValue())) {
                builder.putString(UpdateProfileWorker.WORKER_PARAM_USERNAME, tempUsernameValue);
            }
            if (smallProfilePicture != null && largeProfilePicture != null) {
                builder.putString(UpdateProfileWorker.WORKER_PARAM_AVATAR_FILE, smallProfilePicture.getPath());
                builder.putString(UpdateProfileWorker.WORKER_PARAM_LARGE_AVATAR_FILE, largeProfilePicture.getPath());
            }
            if (removeProfilePicture) {
                builder.putBoolean(UpdateProfileWorker.WORKER_PARAM_AVATAR_REMOVAL, true);
            }
            final Data data = builder.build();
            final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(UpdateProfileWorker.class).setInputData(data).build();
            workManager.enqueueUniqueWork(UpdateProfileWorker.WORK_NAME, ExistingWorkPolicy.REPLACE, workRequest);
        }

        public LiveData<List<WorkInfo>> getSaveProfileWorkInfo() {
            return workManager.getWorkInfosForUniqueWorkLiveData(UpdateProfileWorker.WORK_NAME);
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

                final File file = new File(large.getPath());
                profilePicture.postValue(Media.createFromFile(Media.MEDIA_TYPE_IMAGE, file));
            });
        }

        public void removeProfilePicture() {
            removeProfilePicture = true;
            profilePicture.setValue(null);
        }
    }

    public static class UpdateProfileWorker extends Worker {
        private static final String WORK_NAME = "set-name";

        private static final String WORKER_PARAM_AVATAR_FILE = "avatar_file";
        private static final String WORKER_PARAM_LARGE_AVATAR_FILE = "large_avatar_file";
        private static final String WORKER_PARAM_NAME = "name";
        private static final String WORKER_PARAM_USERNAME = "username";
        private static final String WORKER_PARAM_AVATAR_REMOVAL = "avatar_removal";

        private final KAvatarLoader kAvatarLoader = KAvatarLoader.getInstance();
        private final Connection connection = Connection.getInstance();

        public UpdateProfileWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @Override
        @WorkerThread
        public @NonNull Result doWork() {
            final String name = getInputData().getString(WORKER_PARAM_NAME);
            final String username = getInputData().getString(WORKER_PARAM_USERNAME);
            final String avatarFilePath = getInputData().getString(WORKER_PARAM_AVATAR_FILE);
            final String largeAvatarFilePath = getInputData().getString(WORKER_PARAM_LARGE_AVATAR_FILE);
            final boolean avatarDeleted = getInputData().getBoolean(WORKER_PARAM_AVATAR_REMOVAL,false);
            try {
                final Me me = Me.getInstance();
                final boolean nameSet, usernameSet;
                if (!TextUtils.isEmpty(username)) {
                    UsernameResponseIq responseIq = connection.sendUsername(Preconditions.checkNotNull(username)).await();
                    if (responseIq.success) {
                        me.saveUsername(username);
                    }
                    usernameSet = responseIq.success;
                } else {
                    usernameSet = !TextUtils.isEmpty(me.getUsername());
                }
                if (!TextUtils.isEmpty(name)) {
                    connection.sendName(Preconditions.checkNotNull(name)).await();
                    me.saveName(name);
                    nameSet = true;
                } else {
                    nameSet = !TextUtils.isEmpty(me.getName());
                }
                if (nameSet && usernameSet) {
                    Preferences.getInstance().setProfileSetup(true);
                }
                if (avatarFilePath != null && largeAvatarFilePath != null) {
                    try {
                        final String avatarId = updateProfilePicture(avatarFilePath, largeAvatarFilePath);
                        if (avatarId == null) {
                            Analytics.getInstance().logOnboardingSetAvatar(false);
                            return Result.failure();
                        }
                    } catch (IOException e) {
                        Log.e("Failed to get base64", e);
                        Analytics.getInstance().logOnboardingSetAvatar(false);
                        return Result.failure();
                    } catch (InterruptedException | ObservableErrorException e) {
                        Log.e("Avatar upload interrupted", e);
                        Analytics.getInstance().logOnboardingSetAvatar(false);
                        return Result.failure();
                    }
                }
                if (avatarDeleted) {
                    kAvatarLoader.removeMyAvatar();
                    connection.removeAvatar();
                }
                Analytics.getInstance().logOnboardingSetAvatar(true);
                return Result.success();
            } catch (InterruptedException | ObservableErrorException e) {
                Log.e("UpdateProfileWorker", e);
                Analytics.getInstance().logOnboardingSetAvatar(false);
                return Result.failure();
            }
        }

        @WorkerThread
        private String updateProfilePicture(@NonNull String smallPicturePath, @NonNull String largPicturePath) throws IOException, ObservableErrorException, InterruptedException {
            String avatarId;
            final File smallPictureFile = new File(smallPicturePath);
            final File largePictureFile = new File(largPicturePath);

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

                avatarId = connection.setAvatar(smallPictureBytes, largePictureBytes).await();
                if (avatarId != null) {
                    kAvatarLoader.reportMyAvatarChanged(avatarId);
                }
            }
            return avatarId;
        }
    }
}
