package com.halloapp.ui.profile;

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

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.WorkInfo;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.HalloBottomSheetDialog;
import com.halloapp.ui.InitialSyncActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.avatar.AvatarPreviewActivity;
import com.halloapp.ui.camera.CameraActivity;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.ui.settings.SettingsProfileViewModel;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;

import java.util.List;

public class SetupProfileActivity extends HalloActivity {

    private static final int CODE_CHANGE_AVATAR = 1;

    private EditText nameEditText;
    private View nextButton;

    private ImageView avatarView;
    private ImageView tempAvatarView;

    private View changeAvatarView;
    private View changeAvatarLayout;

    private AvatarLoader avatarLoader;

    private SettingsProfileViewModel viewModel;

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

        nextButton = findViewById(R.id.next);
        nextButton.setEnabled(false);
        nameEditText = findViewById(R.id.name);
        final TextView counterView = findViewById(R.id.counter);
        nameEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.MAX_NAME_LENGTH)});
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                counterView.setText(getString(R.string.counter, s.length(), Constants.MAX_NAME_LENGTH));
            }

            @Override
            public void afterTextChanged(Editable s) {
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
        nameEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.MAX_NAME_LENGTH)});
        nameEditText.addTextChangedListener(new TextWatcher() {
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
                viewModel.setTempName(s.toString());
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
                        nextButton.setEnabled(false);
                        running = true;
                    } else if (running) {
                        if (state == WorkInfo.State.FAILED) {
                            SnackbarHelper.showWarning(SetupProfileActivity.this, R.string.failed_update_profile);
                            nameEditText.setEnabled(true);
                            nameEditText.requestFocus();
                            nextButton.setEnabled(true);
                        } else if (state == WorkInfo.State.SUCCEEDED) {
                            startActivity(new Intent(SetupProfileActivity.this, InitialSyncActivity.class));
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
            if (TextUtils.isEmpty(name)) {
                SnackbarHelper.showWarning(this, R.string.name_must_be_specified);
                nameEditText.requestFocus();
                return;
            }
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

    private void setPhotoSelectOptions(boolean hasAvatar) {
        if (hasAvatar) {
            changeAvatarLayout.setVisibility(View.GONE);
            changeAvatarView.setOnClickListener(avatarOptionsListener);
            avatarView.setOnClickListener(avatarOptionsListener);
            tempAvatarView.setOnClickListener(avatarOptionsListener);
        } else {
            changeAvatarLayout.setVisibility(View.VISIBLE);
            changeAvatarView.setOnClickListener(setAvatarListener);
            avatarView.setOnClickListener(setAvatarListener);
            tempAvatarView.setOnClickListener(setAvatarListener);
        }
    }

    private void updateNextButton() {
        boolean nameValid = !TextUtils.isEmpty(nameEditText.getText().toString());

        nextButton.setEnabled(nameValid);
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
