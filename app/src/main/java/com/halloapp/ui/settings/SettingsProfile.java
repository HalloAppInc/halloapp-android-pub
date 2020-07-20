package com.halloapp.ui.settings;

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
import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.contacts.UserId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.UserNameActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;
import com.halloapp.widget.CenterToast;
import com.halloapp.xmpp.Connection;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class SettingsProfile extends HalloActivity {

    private static final int CODE_CHANGE_AVATAR = 1;

    private SettingsProfileViewModel viewModel;

    private AvatarLoader avatarLoader;

    private TextView nameView;
    private ImageView avatarView;

    private String userName;

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
        final View changeAvatarView = findViewById(R.id.change_avatar);
        final View saveButton = findViewById(R.id.save);
        final View progressBar = findViewById(R.id.progress);

        viewModel.getName().observe(this, text -> {
            this.userName = text;
            nameView.setText(text);
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
                saveButton.setEnabled(!s.toString().equals(userName));
            }
        });

        final View.OnClickListener changeAvatarListener = v -> {
            Log.d("ProfileFragment request change avatar");
            final Intent intent = new Intent(this, MediaPickerActivity.class);
            intent.putExtra(MediaPickerActivity.EXTRA_PICKER_PURPOSE, MediaPickerActivity.PICKER_PURPOSE_AVATAR);
            startActivityForResult(intent, CODE_CHANGE_AVATAR);
        };
        changeAvatarView.setOnClickListener(changeAvatarListener);
        avatarView.setOnClickListener(changeAvatarListener);

        WorkManager.getInstance(this).getWorkInfosForUniqueWorkLiveData(SetNameWorker.WORK_NAME).observe(this, new Observer<List<WorkInfo>>() {

            boolean running;

            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                for (WorkInfo workInfo : workInfos) {
                    final WorkInfo.State state = workInfo.getState();
                    Log.i("UserNameActivity: work " + workInfo.getId() + " " + state);
                    if (state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED) {
                        nameView.setEnabled(false);
                        saveButton.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        running = true;
                    } else if (running) {
                        progressBar.setVisibility(View.GONE);
                        if (state == WorkInfo.State.FAILED) {
                            CenterToast.show(getBaseContext(), R.string.failed_set_name);
                            nameView.setEnabled(true);
                            nameView.requestFocus();
                            saveButton.setVisibility(View.VISIBLE);
                        } else if (state == WorkInfo.State.SUCCEEDED) {
                            CenterToast.show(getBaseContext(), R.string.name_updated);
                            setResult(RESULT_OK);
                            finish();
                        }
                        running = false;
                    }
                }
            }
        });

        saveButton.setOnClickListener(v -> sendName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        avatarLoader.load(avatarView, UserId.ME);
    }

    private void sendName() {
        final String name = StringUtils.preparePostText(Preconditions.checkNotNull(nameView.getText()).toString());
        if (TextUtils.isEmpty(name)) {
            CenterToast.show(this, R.string.name_must_be_specified);
            nameView.requestFocus();
            return;
        }

        final Data data = new Data.Builder().putString(SetNameWorker.WORKER_PARAM_NAME, name).build();
        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(UserNameActivity.SetNameWorker.class).setInputData(data).build();
        WorkManager.getInstance(this).enqueueUniqueWork(SetNameWorker.WORK_NAME, ExistingWorkPolicy.REPLACE, workRequest);
    }

    public static class SetNameWorker extends Worker {

        private static final String WORK_NAME = "set-name";

        private static final String WORKER_PARAM_NAME = "name";

        public SetNameWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @Override
        public @NonNull Result doWork() {
            final String name = getInputData().getString(WORKER_PARAM_NAME);
            if (TextUtils.isEmpty(name)) {
                return Result.failure();
            }
            try {
                final Boolean result = Connection.getInstance().sendName(name).get();
                if (Boolean.TRUE.equals(result)) {
                    Me.getInstance(getApplicationContext()).saveName(name);
                    return Result.success();
                } else {
                    return Result.failure();
                }
            } catch (ExecutionException | InterruptedException e) {
                Log.e("SetNameWorker", e);
                return Result.failure();
            }
        }
    }
}
