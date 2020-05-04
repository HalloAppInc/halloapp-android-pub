package com.halloapp.ui;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;
import com.halloapp.widget.CenterToast;
import com.halloapp.xmpp.Connection;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class UserNameActivity extends AppCompatActivity {

    private EditText nameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("UserNameActivity.onCreate");
        setContentView(R.layout.activity_user_name);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        nameEditText = findViewById(R.id.name);
        nameEditText.requestFocus();

        final TextView counterView = findViewById(R.id.counter);
        final View updateButton = findViewById(R.id.update);
        final View updateProgress = findViewById(R.id.progress);

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
            }
        });

        Me.getInstance(this).name.observe(this, nameEditText::setText);
        AsyncTask.execute(() -> Me.getInstance(this).getName());

        WorkManager.getInstance(this).getWorkInfosForUniqueWorkLiveData(SetNameWorker.WORK_NAME).observe(this, new Observer<List<WorkInfo>>() {

            boolean running;

            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                for (WorkInfo workInfo : workInfos) {
                    final WorkInfo.State state = workInfo.getState();
                    Log.i("UserNameActivity: work " + workInfo.getId() + " " + state);
                    if (state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED) {
                        nameEditText.setEnabled(false);
                        updateButton.setVisibility(View.GONE);
                        updateProgress.setVisibility(View.VISIBLE);
                        running = true;
                    } else if (running) {
                        if (state == WorkInfo.State.FAILED) {
                            CenterToast.show(getBaseContext(), R.string.failed_set_name);
                            nameEditText.setEnabled(true);
                            nameEditText.requestFocus();
                            updateButton.setVisibility(View.VISIBLE);
                            updateProgress.setVisibility(View.GONE);
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

        updateButton.setOnClickListener(v -> sendName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("UserNameActivity.onDestroy");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void sendName() {
        final String name = StringUtils.preparePostText(Preconditions.checkNotNull(nameEditText.getText()).toString());
        if (TextUtils.isEmpty(name)) {
            CenterToast.show(this, R.string.name_must_be_specified);
            nameEditText.requestFocus();
            return;
        }

        final Data data = new Data.Builder().putString(SetNameWorker.WORKER_PARAM_NAME, name).build();
        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SetNameWorker.class).setInputData(data).build();
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
