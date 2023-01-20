package com.halloapp.katchup;

import android.content.Context;
import android.content.Intent;
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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.WorkInfo;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.halloapp.Constants;
import com.halloapp.MainActivity;
import com.halloapp.R;
import com.halloapp.proto.server.UsernameResponse;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.HalloBottomSheetDialog;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.avatar.AvatarPreviewActivity;
import com.halloapp.ui.camera.CameraActivity;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.ui.settings.SettingsProfileViewModel;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.widget.TextDrawable;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.UsernameResponseIq;
import com.halloapp.xmpp.util.Observable;

import java.util.List;
import java.util.Locale;

public class SetupUsernameProfileActivity extends HalloActivity {

    private static final String USERNAME_PREFIX = "@";
    private static final int DEBOUNCE_DELAY_MS = 300;
    private static final float ENTRY_USABLE_WIDTH_RATIO = 0.95f;
    private static final String LEGAL_CHARACTERS_REGEX = "^[\\p{IsAlphabetic}\\p{IsDigit}_.]+$";

    private static final String EXTRA_NAME = "name";
    private static final int CODE_CHANGE_AVATAR = 1;

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
    private ImageView tempAvatarView;

    private View changeAvatarView;
    private View changeAvatarLayout;

    private AvatarLoader avatarLoader;

    private SettingsProfileViewModel viewModel;
    private Runnable debounceRunnable;
    private Observable<UsernameResponseIq> checkUsernameIsAvailable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setup_profile_username);

        avatarLoader = AvatarLoader.getInstance();

        final int nameTextSize = getResources().getDimensionPixelSize(R.dimen.registration_name_text_size);
        final int nameTextMinSize = getResources().getDimensionPixelSize(R.dimen.registration_name_text_min_size);
        final int regEntryHorizontalPadding = getResources().getDimensionPixelSize(R.dimen.reg_name_field_horizontal_padding);
        final int textColor = getResources().getColor(R.color.black_80);

        viewModel = new ViewModelProvider(this).get(SettingsProfileViewModel.class);

        final String name = Preconditions.checkNotNull(getIntent().getStringExtra(EXTRA_NAME));
        viewModel.setTempName(name);

        avatarView = findViewById(R.id.avatar);
        tempAvatarView = findViewById(R.id.temp_avatar);
        changeAvatarView = findViewById(R.id.change_avatar);
        changeAvatarLayout = findViewById(R.id.change_avatar_camera_btn);
        usernameUniquenessInfo = findViewById(R.id.username_uniqueness_info);
        usernameError = findViewById(R.id.username_error);

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

        viewModel.getTempAvatar().observe(this, avatar -> {
            tempAvatarView.setVisibility(View.VISIBLE);
            if (avatar != null) {
                tempAvatarView.setImageBitmap(avatar);
            } else {
                tempAvatarView.setImageResource(R.drawable.avatar_placeholder);
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

        viewModel.getHasAvatarSet().observe(this, this::setPhotoSelectOptions);
        viewModel.getUsername().observe(this, username -> usernameEditText.setText(username));

        nextButton.setOnClickListener(v -> {
            final String username = StringUtils.preparePostText(Preconditions.checkNotNull(usernameEditText.getText()).toString());
            if (TextUtils.isEmpty(username)) {
                SnackbarHelper.showWarning(this, R.string.name_must_be_specified);
                usernameEditText.requestFocus();
                return;
            }
            // TODO(josh): move this to the actual end point of onboarding once it is implemented
            Analytics.getInstance().logOnboardingFinish();
            viewModel.saveProfile();
        });
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

    private boolean validateUsernameInput(@NonNull String username) {
        String errorString = null;
        if (!username.isEmpty()) {
            if (!Character.isAlphabetic(username.charAt(0))) {
                errorString = getResources().getString(R.string.reg_username_error_leading_char_invalid);
            } else if (!username.matches(LEGAL_CHARACTERS_REGEX)) {
                errorString = getResources().getString(R.string.reg_username_error_bad_expression);
            } else if (username.length() < Constants.MIN_USERNAME_LENGTH) {
                errorString = getResources().getString(R.string.reg_username_error_too_short);
            }
        }

        if (errorString != null) {
            usernameUniquenessInfo.setVisibility(View.GONE);
            usernameError.setVisibility(View.VISIBLE);
            usernameError.setText(errorString);
            return false;
        } else {
            usernameUniquenessInfo.setVisibility(View.VISIBLE);
            usernameError.setVisibility(View.GONE);
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
                    int reason = response != null ? response.reason : UsernameResponseIq.Reason.UNKNOWN;
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
        usernameUniquenessInfo.setVisibility(View.VISIBLE);
        usernameError.setVisibility(View.GONE);
    }

    private void onUsernameIsAvailableError(@UsernameResponseIq.Reason int reason, @NonNull String username) {
        usernameUniquenessInfo.setVisibility(View.GONE);
        usernameError.setVisibility(View.VISIBLE);
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
        usernameError.setText(errorText);
    }

    private void setPhotoSelectOptions(boolean hasAvatar) {
        if (hasAvatar) {
            changeAvatarLayout.setVisibility(View.GONE);
            // TODO: Analytics.getInstance().logOnboardingSetAvatar();
            // TODO(vasil): Uncomment these listeners once we have the avatar edit activity ready.
            /*changeAvatarView.setOnClickListener(avatarOptionsListener);
            avatarView.setOnClickListener(avatarOptionsListener);
            tempAvatarView.setOnClickListener(avatarOptionsListener);*/
        } else {
            changeAvatarLayout.setVisibility(View.VISIBLE);
            /*changeAvatarView.setOnClickListener(setAvatarListener);
            avatarView.setOnClickListener(setAvatarListener);
            tempAvatarView.setOnClickListener(setAvatarListener);*/
        }
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
