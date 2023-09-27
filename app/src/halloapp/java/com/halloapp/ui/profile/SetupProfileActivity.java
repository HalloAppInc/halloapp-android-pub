package com.halloapp.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.WorkInfo;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.id.UserId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.HalloBottomSheetDialog;
import com.halloapp.ui.InitialSyncActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.avatar.AvatarPreviewActivity;
import com.halloapp.ui.camera.CameraActivity;
import com.halloapp.ui.contacts.ViewFriendsListActivity;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.ui.settings.SettingsProfileViewModel;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.UsernameResponseIq;
import com.halloapp.xmpp.util.Observable;

import java.util.List;
import java.util.Locale;

public class SetupProfileActivity extends HalloActivity {

    private static final int CODE_CHANGE_AVATAR = 1;
    private static final int DEBOUNCE_DELAY_MS = 300;

    private static final String EXTRA_NAME = "name";
    private static final String EXTRA_IS_EXISTING_USER = "is_existing_user";

    private EditText nameEditText;
    private EditText usernameEditText;
    private TextView usernameError;
    private View nextButton;

    private ImageView avatarView;
    private ImageView tempAvatarView;

    private View changeAvatarView;
    private View changeAvatarLayout;

    private AvatarLoader avatarLoader;

    private SettingsProfileViewModel viewModel;

    private Runnable debounceRunnable;
    private Observable<UsernameResponseIq> checkUsernameIsAvailable;

    public static Intent pickUsername(@NonNull Context context, @NonNull String name) {
        Intent intent = new Intent(context, SetupProfileActivity.class);
        intent.putExtra(EXTRA_NAME, name);
        intent.putExtra(EXTRA_IS_EXISTING_USER, true);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setup_profile);

        avatarLoader = AvatarLoader.getInstance();

        viewModel = new ViewModelProvider(this).get(SettingsProfileViewModel.class);

        avatarView = findViewById(R.id.avatar);
        tempAvatarView = findViewById(R.id.temp_avatar);
        changeAvatarView = findViewById(R.id.change_avatar);
        changeAvatarLayout = findViewById(R.id.change_avatar_camera_btn);

        final boolean isExistingUser = getIntent().getBooleanExtra(EXTRA_IS_EXISTING_USER, false);

        nextButton = findViewById(R.id.next);
        nextButton.setEnabled(false);
        nameEditText = findViewById(R.id.name);
        nameEditText.setText(getIntent().getStringExtra(EXTRA_NAME));

        usernameEditText = findViewById(R.id.username);

        final TextView nameCounterView = findViewById(R.id.name_counter);
        final TextView usernameCounterView = findViewById(R.id.username_counter);

        usernameError = findViewById(R.id.username_error);

        nameEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.MAX_NAME_LENGTH)});
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                nameCounterView.setText(getString(R.string.counter, s.length(), Constants.MAX_NAME_LENGTH));
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null) {
                    return;
                }

                viewModel.setTempName(s.toString());
                updateNextButton();
            }
        });
        nameEditText.requestFocus();

        viewModel.getTempAvatar().observe(this, avatar -> {
            tempAvatarView.setVisibility(View.VISIBLE);
            if (avatar != null) {
                tempAvatarView.setImageBitmap(avatar);
            } else {
                tempAvatarView.setImageResource(R.drawable.avatar_person);
            }
        });
        usernameEditText.setFilters(new InputFilter[] {
                (source, start, end, dest, dstart, dend) -> String.valueOf(source).toLowerCase(Locale.US),
                new InputFilter.LengthFilter(Constants.MAX_USERNAME_LENGTH)
        });
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                usernameCounterView.setText(getString(R.string.counter, s.length(), Constants.MAX_NAME_LENGTH));
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null) {
                    return;
                }

                final String username = s.toString();

                nextButton.setEnabled(false);

                if (debounceRunnable != null) {
                    nextButton.removeCallbacks(debounceRunnable);
                    debounceRunnable = null;
                }
                cancelCheckUsernameIsAvailable();
                if (validateUsernameInput(username)) {
                    debounceRunnable = () -> checkUsernameAvailability(username);
                    nextButton.postDelayed(debounceRunnable, DEBOUNCE_DELAY_MS);
                    viewModel.setTempUsername(s.toString());
                }
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
                        nameEditText.setEnabled(false);
                        usernameEditText.setEnabled(false);
                        nextButton.setEnabled(false);
                        running = true;
                    } else if (running) {
                        if (state == WorkInfo.State.FAILED) {
                            SnackbarHelper.showWarning(SetupProfileActivity.this, R.string.failed_update_profile);
                            nameEditText.setEnabled(true);
                            usernameEditText.setEnabled(true);
                            nameEditText.requestFocus();
                            nextButton.setEnabled(true);
                        } else if (state == WorkInfo.State.SUCCEEDED) {
                            if (!isExistingUser) {
                                startActivity(new Intent(SetupProfileActivity.this, InitialSyncActivity.class));
                            } else {
                                startActivity(new Intent(SetupProfileActivity.this, ViewFriendsListActivity.class));
                            }
                            finish();
                        }
                        running = false;
                    }
                }
            }
        });

        viewModel.getHasAvatarSet().observe(this, this::setPhotoSelectOptions);

        nextButton.setOnClickListener(v -> {
            final String name = StringUtils.preparePostText(Preconditions.checkNotNull(nameEditText.getText()).toString());
            final String username = StringUtils.preparePostText(Preconditions.checkNotNull(usernameEditText.getText()).toString());

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(username)) {
                SnackbarHelper.showWarning(this, R.string.name_must_be_specified);
                nameEditText.requestFocus();
                return;
            }
            viewModel.saveProfile();
        });
    }

    private void showError(@NonNull String errorMessage) {
        usernameError.setVisibility(View.VISIBLE);
        usernameError.setText(errorMessage);
    }

    private void hideError() {
        usernameError.setVisibility(View.INVISIBLE);
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
        Log.d("SetupProfileActivity.checkUsernameAvailability: username=" + username);
        cancelCheckUsernameIsAvailable();
        if (!TextUtils.isEmpty(username)) {
            checkUsernameIsAvailable = Connection.getInstance().checkUsernameIsAvailable(username).onResponse(response -> {
                if (response != null && response.success) {
                    runOnUiThread(this::onUsernameIsAvailable);
                } else {
                    final int reason = response != null ? response.reason : UsernameResponseIq.Reason.UNKNOWN;
                    Log.e("SetupProfileActivity.checkUsernameAvailability UsernameRequest.IS_AVAILABLE call failed with reason=" + reason);
                    runOnUiThread(() -> onUsernameIsAvailableError(reason, username));
                }
            }).onError(e -> {
                Log.e("SetupProfileActivity.checkUsernameAvailability: UsernameRequest.IS_AVAILABLE call failed", e);
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
    
    final View.OnClickListener setAvatarListener = v -> {
        final Intent intent = MediaPickerActivity.pickAvatar(this);
        startActivityForResult(intent, CODE_CHANGE_AVATAR);
    };

    final View.OnClickListener avatarOptionsListener = v -> {
        final BottomSheetDialog bottomSheetDialog = new HalloBottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.profile_avatar_change);
        View cameraButton = bottomSheetDialog.findViewById(R.id.profile_avatar_take_photo);
        cameraButton.setOnClickListener(view -> {
            final Intent intent = new Intent(this, CameraActivity.class);
            intent.putExtra(CameraActivity.EXTRA_PURPOSE, CameraActivity.PURPOSE_USER_AVATAR);
            startActivityForResult(intent, CODE_CHANGE_AVATAR);
            bottomSheetDialog.hide();
        });
        View mediaButton = bottomSheetDialog.findViewById(R.id.profile_avatar_picture_select);
        mediaButton.setOnClickListener(view -> {
            final Intent intent = MediaPickerActivity.pickAvatar(this);
            startActivityForResult(intent, CODE_CHANGE_AVATAR);
            bottomSheetDialog.hide();
        });
        View removeAvatar = bottomSheetDialog.findViewById(R.id.profile_avatar_remove_avatar);
        removeAvatar.setOnClickListener(view -> {
            viewModel.removeAvatar();
            bottomSheetDialog.hide();
        });
        bottomSheetDialog.show();
    };

    private void setPhotoSelectOptions(boolean hasAvatar) {
        if (hasAvatar) {
            changeAvatarLayout.setVisibility(View.GONE);
            changeAvatarView.setOnClickListener(avatarOptionsListener);
            avatarView.setOnClickListener(avatarOptionsListener);
            tempAvatarView.setOnClickListener(avatarOptionsListener);
            avatarLoader.load(avatarView, UserId.ME, false);
        } else {
            changeAvatarLayout.setVisibility(View.VISIBLE);
            changeAvatarView.setOnClickListener(setAvatarListener);
            avatarView.setOnClickListener(setAvatarListener);
            tempAvatarView.setOnClickListener(setAvatarListener);
        }
    }

    private void updateNextButton() {
        boolean namesValid = !TextUtils.isEmpty(nameEditText.getText().toString()) && !TextUtils.isEmpty(usernameEditText.getText().toString());

        nextButton.setEnabled(namesValid);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {
            case CODE_CHANGE_AVATAR: {
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        int height = data.getIntExtra(AvatarPreviewActivity.RESULT_AVATAR_HEIGHT, - 1);
                        int width = data.getIntExtra(AvatarPreviewActivity.RESULT_AVATAR_WIDTH, -1);
                        String filePath = data.getStringExtra(AvatarPreviewActivity.RESULT_AVATAR_FILE_PATH);
                        String largeFilePath = data.getStringExtra(AvatarPreviewActivity.RESULT_LARGE_AVATAR_FILE_PATH);
                        if (filePath != null && largeFilePath != null && width > 0 && height > 0) {
                            viewModel.setTempAvatar(filePath, largeFilePath, width, height);
                        }
                    }
                }
                break;
            }
        }
    }
}
