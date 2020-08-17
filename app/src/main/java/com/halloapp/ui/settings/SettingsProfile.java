package com.halloapp.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.WorkInfo;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.id.UserId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.avatar.AvatarPreviewActivity;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;
import com.halloapp.widget.CenterToast;
import com.halloapp.widget.SnackbarHelper;

import java.util.List;

public class SettingsProfile extends HalloActivity {

    private static final int CODE_CHANGE_AVATAR = 1;

    private SettingsProfileViewModel viewModel;

    private AvatarLoader avatarLoader;

    private TextView nameView;
    private ImageView avatarView;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CODE_CHANGE_AVATAR: {
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        int height = data.getIntExtra(AvatarPreviewActivity.RESULT_AVATAR_HEIGHT, - 1);
                        int width = data.getIntExtra(AvatarPreviewActivity.RESULT_AVATAR_WIDTH, -1);
                        String filePath = data.getStringExtra(AvatarPreviewActivity.RESULT_AVATAR_FILE_PATH);
                        if (filePath != null && width > 0 && height > 0) {
                            viewModel.setTempAvatar(filePath, width, height);
                        }
                    }
                }
                break;
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_profile);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        avatarLoader = AvatarLoader.getInstance(this);

        viewModel = new ViewModelProvider(this).get(SettingsProfileViewModel.class);

        nameView = findViewById(R.id.edit_name);
        avatarView = findViewById(R.id.avatar);
        ImageView tempAvatarView = findViewById(R.id.temp_avatar);
        final View changeAvatarView = findViewById(R.id.change_avatar);
        final View saveButton = findViewById(R.id.save);
        final View progressBar = findViewById(R.id.progress);

        viewModel.getName().observe(this, text -> {
            nameView.setText(text);
        });

        viewModel.canSave().observe(this, saveButton::setEnabled);
        viewModel.getTempAvatar().observe(this, avatar -> {
            tempAvatarView.setImageBitmap(avatar);
            if (avatar != null) {
                tempAvatarView.setVisibility(View.VISIBLE);
            } else {
                tempAvatarView.setVisibility(View.GONE);
            }
        });

        avatarLoader.load(avatarView, UserId.ME);

        nameView.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.MAX_NAME_LENGTH)});
        nameView.addTextChangedListener(new TextWatcher() {
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

        final View.OnClickListener changeAvatarListener = v -> {
            Log.d("SettingsProfile request change avatar");
            final Intent intent = new Intent(this, MediaPickerActivity.class);
            intent.putExtra(MediaPickerActivity.EXTRA_PICKER_PURPOSE, MediaPickerActivity.PICKER_PURPOSE_AVATAR);
            startActivityForResult(intent, CODE_CHANGE_AVATAR);
        };
        changeAvatarView.setOnClickListener(changeAvatarListener);
        avatarView.setOnClickListener(changeAvatarListener);

        viewModel.getSaveProfileWorkInfo().observe(this, new Observer<List<WorkInfo>>() {

            boolean running;

            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                for (WorkInfo workInfo : workInfos) {
                    final WorkInfo.State state = workInfo.getState();
                    Log.i("SettingsProfile: work " + workInfo.getId() + " " + state);
                    if (state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED) {
                        nameView.setEnabled(false);
                        saveButton.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.VISIBLE);
                        running = true;
                    } else if (running) {
                        progressBar.setVisibility(View.GONE);
                        if (state == WorkInfo.State.FAILED) {
                            SnackbarHelper.showWarning(SettingsProfile.this, R.string.failed_update_profile);
                            nameView.setEnabled(true);
                            nameView.requestFocus();
                            saveButton.setVisibility(View.VISIBLE);
                        } else if (state == WorkInfo.State.SUCCEEDED) {
                            CenterToast.show(getBaseContext(), R.string.profile_updated);
                            setResult(RESULT_OK);
                            finish();
                        }
                        running = false;
                    }
                }
            }
        });

        saveButton.setOnClickListener(v -> {
            final String name = StringUtils.preparePostText(Preconditions.checkNotNull(nameView.getText()).toString());
            if (TextUtils.isEmpty(name)) {
                SnackbarHelper.showWarning(this, R.string.name_must_be_specified);
                nameView.requestFocus();
                return;
            }
            viewModel.saveProfile();
        });
    }

    @Override
    public void onBackPressed() {
        if (!viewModel.hasChanges()) {
            super.onBackPressed();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.alert_discard_changes_message);
            builder.setPositiveButton(R.string.action_discard, (dialog, which) -> {
                finish();
            });
            builder.setNegativeButton(R.string.cancel, null);
            builder.create().show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        avatarLoader.load(avatarView, UserId.ME);
    }
}
