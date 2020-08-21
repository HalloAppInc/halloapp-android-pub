package com.halloapp.ui.groups;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.WorkInfo;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.SystemUiVisibility;
import com.halloapp.ui.avatar.AvatarPreviewActivity;
import com.halloapp.ui.chat.ChatActivity;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;
import com.halloapp.widget.SnackbarHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CreateGroupActivity extends HalloActivity {
    private static final String EXTRA_USER_IDS = "user_ids";

    private static final int CODE_CHANGE_AVATAR = 1;

    private CreateGroupViewModel viewModel;

    private EditText nameEditText;
    private ImageView avatarView;
    private ProgressDialog createGroupDialog;

    private List<UserId> userIds;

    private MenuItem createMenuItem;

    public static Intent newPickerIntent(@NonNull Context context, @Nullable Collection<UserId> userIds) {
        Intent intent = new Intent(context, CreateGroupActivity.class);
        if (userIds != null) {
            intent.putParcelableArrayListExtra(EXTRA_USER_IDS, new ArrayList<>(userIds));
        }
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("CreateGroupActivity.onCreate");

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_create_group);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        viewModel = new ViewModelProvider(this).get(CreateGroupViewModel.class);

        userIds = getIntent().getParcelableArrayListExtra(EXTRA_USER_IDS);
        if (userIds == null) {
            userIds = new ArrayList<>();
        }

        nameEditText = findViewById(R.id.edit_name);
        avatarView = findViewById(R.id.avatar);
        final View changeAvatarView = findViewById(R.id.change_avatar);

        nameEditText.requestFocus();
        nameEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.MAX_GROUP_NAME_LENGTH)});
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateAction(!TextUtils.isEmpty(s.toString()));
            }
        });

        viewModel.getAvatar().observe(this, bitmap -> {
            avatarView.setImageBitmap(bitmap);
        });

        changeAvatarView.setOnClickListener(v -> {
            Log.d("CreateGroupActivity request change avatar");
            final Intent intent = new Intent(this, MediaPickerActivity.class);
            intent.putExtra(MediaPickerActivity.EXTRA_PICKER_PURPOSE, MediaPickerActivity.PICKER_PURPOSE_AVATAR);
            startActivityForResult(intent, CODE_CHANGE_AVATAR);
        });

        viewModel.getCreateGroupWorkInfo().observe(this, new Observer<List<WorkInfo>>() {

            boolean running;

            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                for (WorkInfo workInfo : workInfos) {
                    final WorkInfo.State state = workInfo.getState();
                    Log.i("CreateGroupActivity: work " + workInfo.getId() + " " + state);
                    if (state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED) {
                        if (createGroupDialog == null) {
                            createGroupDialog = ProgressDialog.show(CreateGroupActivity.this, null, getString(R.string.create_group_in_progress, nameEditText.getText().toString()), true);
                            createGroupDialog.show();
                        }
                        running = true;
                    } else if (running) {
                        if (createGroupDialog != null) {
                            createGroupDialog.cancel();
                        }
                        if (state == WorkInfo.State.FAILED) {
                            SnackbarHelper.showWarning(CreateGroupActivity.this, R.string.failed_create_group);
                            nameEditText.setEnabled(true);
                            nameEditText.requestFocus();
                        } else if (state == WorkInfo.State.SUCCEEDED) {
                            String rawGroupId = workInfo.getOutputData().getString(CreateGroupViewModel.CreateGroupWorker.WORKER_OUTPUT_GROUP_ID);
                            GroupId groupId = new GroupId(Preconditions.checkNotNull(rawGroupId));
                            final Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                            intent.putExtra(ChatActivity.EXTRA_CHAT_ID, groupId);
                            startActivity(intent);
                            finish();
                        }
                        running = false;
                    }
                }
            }
        });
    }

    private void updateAction(boolean enabled) {
        if (createMenuItem != null) {
            SpannableString ss = new SpannableString(getString(R.string.button_create_group));
            ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getBaseContext(), enabled ? R.color.color_secondary : R.color.black_30)), 0, ss.length(), 0);
            createMenuItem.setTitle(ss);
            createMenuItem.setEnabled(enabled);
        }
    }

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
                            viewModel.setAvatar(filePath);
                        }
                    }
                }
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.create_group_menu, menu);
        createMenuItem = menu.findItem(R.id.create);
        updateAction(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create: {
                final String name = StringUtils.preparePostText(Preconditions.checkNotNull(nameEditText.getText()).toString());
                if (TextUtils.isEmpty(name)) {
                    SnackbarHelper.showWarning(this, R.string.name_must_be_specified);
                    nameEditText.requestFocus();
                    return true;
                }

                nameEditText.setEnabled(false);
                viewModel.createGroup(name, userIds);
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("CreateGroupActivity.onDestroy");
    }

}
