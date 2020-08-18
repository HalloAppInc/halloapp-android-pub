package com.halloapp.ui.groups;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.content.Mention;
import com.halloapp.id.UserId;
import com.halloapp.ui.ContentComposerViewModel;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.SystemUiVisibility;
import com.halloapp.ui.chat.ChatActivity;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;
import com.halloapp.widget.SnackbarHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CreateGroupActivity extends HalloActivity {
    private static final String EXTRA_USER_IDS = "user_ids";

    private CreateGroupViewModel viewModel;

    private EditText nameEditText;

    private List<UserId> userIds;

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
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.create_group_menu, menu);
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

                ProgressDialog createGroupDialog = ProgressDialog.show(this, null, getString(R.string.create_group_in_progress, name), true);
                createGroupDialog.show();
                viewModel.createGroup(name, userIds).observe(this, groupInfo -> {
                    createGroupDialog.cancel();
                    if (groupInfo != null) {
                        final Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                        intent.putExtra(ChatActivity.EXTRA_CHAT_ID, groupInfo.groupId);
                        startActivity(intent);
                        finish();
                    } else {
                        SnackbarHelper.showWarning(this, R.string.failed_create_group);
                        nameEditText.setEnabled(true);
                        nameEditText.requestFocus();
                    }
                });
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
