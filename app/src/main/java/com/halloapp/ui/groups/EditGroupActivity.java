package com.halloapp.ui.groups;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.WorkInfo;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.id.GroupId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.HalloBottomSheetDialog;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.avatar.AvatarPreviewActivity;
import com.halloapp.ui.camera.CameraActivity;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.util.logs.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;
import com.halloapp.widget.CenterToast;
import com.halloapp.widget.SnackbarHelper;

import java.util.List;

public class EditGroupActivity extends HalloActivity {
    private static final String EXTRA_GROUP_ID = "group_id";

    private static final int CODE_CHANGE_AVATAR = 1;

    private EditGroupActivityViewModel viewModel;

    private AvatarLoader avatarLoader;

    private GroupId groupId;

    private TextView nameView;
    private ImageView avatarView;
    private View changeAvatarView;
    private View saveButton;
    private View progressBar;
    private ImageView tempAvatarView;

    public static Intent openEditGroup(@NonNull Context context, @NonNull GroupId groupId) {
        Preconditions.checkNotNull(groupId);
        Intent intent = new Intent(context, EditGroupActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId.rawId());
        return intent;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        avatarLoader = AvatarLoader.getInstance();

        groupId = new GroupId(Preconditions.checkNotNull(getIntent().getStringExtra(EXTRA_GROUP_ID)));
        viewModel = new ViewModelProvider(this, new EditGroupActivityViewModel.Factory(getApplication(), groupId)).get(EditGroupActivityViewModel.class);

        nameView = findViewById(R.id.edit_name);
        avatarView = findViewById(R.id.avatar);
        tempAvatarView = findViewById(R.id.temp_avatar);
        changeAvatarView = findViewById(R.id.change_avatar);
        saveButton = findViewById(R.id.save);
        progressBar = findViewById(R.id.progress);

        viewModel.getName().observe(this, text -> nameView.setText(text));

        viewModel.canSave().observe(this, saveButton::setEnabled);
        viewModel.getTempAvatar().observe(this, avatar -> {
            avatarView.setVisibility(View.INVISIBLE);
            tempAvatarView.setVisibility(View.VISIBLE);
            if (avatar != null) {
                tempAvatarView.setImageBitmap(avatar);
            } else {
                tempAvatarView.setImageResource(R.drawable.avatar_groups_placeholder);
            }
        });

        avatarLoader.load(avatarView, groupId, false);

        nameView.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.MAX_GROUP_NAME_LENGTH)});
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

        viewModel.getHasAvatarSet().observe(this, (avatarSet) -> {
            setPhotoSelectOptions(avatarSet);
        });

        changeAvatarView.setOnClickListener(avatarOptionsListener);
        avatarView.setOnClickListener(avatarOptionsListener);

        viewModel.getSaveProfileWorkInfo().observe(this, new Observer<List<WorkInfo>>() {

            boolean running;

            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                for (WorkInfo workInfo : workInfos) {
                    final WorkInfo.State state = workInfo.getState();
                    Log.i("EditGroupActivity: work " + workInfo.getId() + " " + state);
                    if (state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED) {
                        nameView.setEnabled(false);
                        saveButton.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.VISIBLE);
                        running = true;
                    } else if (running) {
                        progressBar.setVisibility(View.GONE);
                        if (state == WorkInfo.State.FAILED) {
                            SnackbarHelper.showWarning(EditGroupActivity.this, R.string.failed_update_group);
                            nameView.setEnabled(true);
                            nameView.requestFocus();
                            saveButton.setVisibility(View.VISIBLE);
                        } else if (state == WorkInfo.State.SUCCEEDED) {
                            CenterToast.show(getBaseContext(), R.string.group_updated);
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
            viewModel.saveGroup();
        });
    }

    @Override
    public void onBackPressed() {
        if (!viewModel.hasChanges()) {
            super.onBackPressed();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.alert_discard_changes_message);
            builder.setPositiveButton(R.string.action_discard, (dialog, which) -> finish());
            builder.setNegativeButton(R.string.cancel, null);
            builder.create().show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        avatarLoader.load(avatarView, groupId, false);
    }

    final View.OnClickListener setAvatarListener = v -> {
        final Intent intent = MediaPickerActivity.pickGroupAvatar(this);
        startActivityForResult(intent, CODE_CHANGE_AVATAR);
    };

    final View.OnClickListener avatarOptionsListener = v -> {
        final BottomSheetDialog bottomSheetDialog = new HalloBottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.profile_avatar_change);
        TextView titleView = bottomSheetDialog.findViewById(R.id.avatar_change_title);
        Preconditions.checkNotNull(titleView).setText(R.string.group_avatar_picker_title);
        View cameraButton = bottomSheetDialog.findViewById(R.id.profile_avatar_take_photo);
        cameraButton.setOnClickListener(view -> {
            final Intent intent = new Intent(this, CameraActivity.class);
            intent.putExtra(CameraActivity.EXTRA_PURPOSE, CameraActivity.PURPOSE_GROUP_AVATAR);
            startActivityForResult(intent, CODE_CHANGE_AVATAR);
            bottomSheetDialog.hide();
        });
        View mediaButton = bottomSheetDialog.findViewById(R.id.profile_avatar_picture_select);
        mediaButton.setOnClickListener(view -> {
            final Intent intent = MediaPickerActivity.pickGroupAvatar(this);
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
            changeAvatarView.setVisibility(View.GONE);
            changeAvatarView.setOnClickListener(avatarOptionsListener);
            avatarView.setOnClickListener(avatarOptionsListener);
            tempAvatarView.setOnClickListener(avatarOptionsListener);
        } else {
            changeAvatarView.setVisibility(View.VISIBLE);
            changeAvatarView.setOnClickListener(setAvatarListener);
            avatarView.setOnClickListener(setAvatarListener);
            tempAvatarView.setOnClickListener(setAvatarListener);
        }
    }
}
