package com.halloapp.ui.groups;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.WorkInfo;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.id.GroupId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.markdown.MarkdownUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.CenterToast;
import com.halloapp.widget.SnackbarHelper;

import java.util.List;

import io.noties.markwon.editor.MarkwonEditor;
import io.noties.markwon.editor.MarkwonEditorTextWatcher;

public class EditGroupDescriptionActivity extends HalloActivity {

    private static final String EXTRA_GROUP_ID = "group_id";

    public static Intent openEditGroupDescription(@NonNull Context context, @NonNull GroupId groupId) {
        Preconditions.checkNotNull(groupId);
        Intent intent = new Intent(context, EditGroupDescriptionActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        return intent;
    }

    private EditGroupDescriptionViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final GroupId groupId = getIntent().getParcelableExtra(EXTRA_GROUP_ID);
        if (groupId == null) {
            finish();
            Log.e("EditGroupDescriptionActivity/onCreate must provide a group id");
            return;
        }

        setTitle(R.string.group_description_title);

        setContentView(R.layout.activity_edit_group_description);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        ProgressBar progressBar = findViewById(R.id.progress);
        EditText descriptionField = findViewById(R.id.edit_description);
        View saveBtn = findViewById(R.id.save);

        final MarkwonEditor editor = MarkdownUtils.createMarkwonEditor(this);
        descriptionField.addTextChangedListener(MarkwonEditorTextWatcher.withProcess(editor));

        viewModel = new ViewModelProvider(this, new EditGroupDescriptionViewModel.Factory(getApplication(), groupId)).get(EditGroupDescriptionViewModel.class);

        viewModel.getName().observe(this, descriptionField::setText);
        viewModel.canSave().observe(this, canSave -> saveBtn.setEnabled(Boolean.TRUE.equals(canSave)));

        descriptionField.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.MAX_GROUP_DESCRIPTION_LENGTH)});
        descriptionField.addTextChangedListener(new TextWatcher() {
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
                viewModel.setTempDescription(s.toString());
            }
        });

        viewModel.getSaveProfileWorkInfo().observe(this, new Observer<List<WorkInfo>>() {

            boolean running;

            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                for (WorkInfo workInfo : workInfos) {
                    final WorkInfo.State state = workInfo.getState();
                    Log.i("EditGroupDescriptionActivity: work " + workInfo.getId() + " " + state);
                    if (state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED) {
                        descriptionField.setEnabled(false);
                        saveBtn.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.VISIBLE);
                        running = true;
                    } else if (running) {
                        progressBar.setVisibility(View.GONE);
                        if (state == WorkInfo.State.FAILED) {
                            SnackbarHelper.showWarning(EditGroupDescriptionActivity.this, R.string.failed_update_group_description);
                            descriptionField.setEnabled(true);
                            descriptionField.requestFocus();
                            saveBtn.setVisibility(View.VISIBLE);
                        } else if (state == WorkInfo.State.SUCCEEDED) {
                            CenterToast.show(getBaseContext(), R.string.group_description_updated);
                            setResult(RESULT_OK);
                            finish();
                        }
                        running = false;
                    }
                }
            }
        });

        saveBtn.setOnClickListener(v -> viewModel.saveGroup());
    }
}
