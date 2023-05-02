package com.halloapp.ui.groups;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.WorkInfo;

import com.halloapp.R;
import com.halloapp.id.GroupId;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.contacts.MultipleContactPickerActivity;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;

import java.util.ArrayList;

public class GroupCreationPickerActivity extends MultipleContactPickerActivity {

    public static final String RESULT_GROUP_ID = "group_id";
    public static final String RESULT_MEMBER_COUNT = "member_count";

    private static final String EXTRA_SMALL_AVATAR_PATH = "small_avatar_path";
    private static final String EXTRA_LARGE_AVATAR_PATH = "large_avatar_path";
    private static final String EXTRA_GROUP_NAME = "group_name";
    private static final String EXTRA_SELECTED_GROUP_EXPIRY = "group_expiry";
    private static final String EXTRA_IS_GROUP_CHAT = "is_group_chat";

    private String groupName;
    private String smallAvatarPath;
    private String largeAvatarPath;
    private boolean groupChat = false;

    private CreateGroupViewModel viewModel;

    private ProgressDialog createGroupDialog;

    private boolean waitingForResult = false;

    public static Intent newFeedGroup(@NonNull Context context, @NonNull String name, @Nullable String smallAvatar, @Nullable String largeAvatarPath, @Nullable Integer groupExpiry) {
        Intent intent = new Intent(context, GroupCreationPickerActivity.class);
        intent.putExtra(EXTRA_TITLE_RES, R.string.group_picker_title);
        intent.putExtra(EXTRA_ACTION_RES, R.string.button_create_group);
        intent.putExtra(EXTRA_ONLY_FRIENDS, false);
        intent.putExtra(EXTRA_ALLOW_EMPTY_SELECTION, true);
        intent.putExtra(EXTRA_SMALL_AVATAR_PATH, smallAvatar);
        intent.putExtra(EXTRA_LARGE_AVATAR_PATH, largeAvatarPath);
        if (groupExpiry != null) {
            intent.putExtra(EXTRA_SELECTED_GROUP_EXPIRY, groupExpiry);
        }
        intent.putExtra(EXTRA_GROUP_NAME, name);
        return intent;
    }

    public static Intent newGroupChat(@NonNull Context context, @NonNull String name, @Nullable String smallAvatar, @Nullable String largeAvatarPath) {
        Intent intent = new Intent(context, GroupCreationPickerActivity.class);
        intent.putExtra(EXTRA_TITLE_RES, R.string.group_picker_title);
        intent.putExtra(EXTRA_ACTION_RES, R.string.button_create_group);
        intent.putExtra(EXTRA_ONLY_FRIENDS, false);
        intent.putExtra(EXTRA_ALLOW_EMPTY_SELECTION, true);
        intent.putExtra(EXTRA_SMALL_AVATAR_PATH, smallAvatar);
        intent.putExtra(EXTRA_LARGE_AVATAR_PATH, largeAvatarPath);
        intent.putExtra(EXTRA_GROUP_NAME, name);
        intent.putExtra(EXTRA_IS_GROUP_CHAT, true);
        return intent;
    }

    private final ServerProps serverProps = ServerProps.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        groupName = getIntent().getStringExtra(EXTRA_GROUP_NAME);
        smallAvatarPath = getIntent().getStringExtra(EXTRA_SMALL_AVATAR_PATH);
        largeAvatarPath = getIntent().getStringExtra(EXTRA_LARGE_AVATAR_PATH);
        groupChat = getIntent().getBooleanExtra(EXTRA_IS_GROUP_CHAT, false);
        int groupExpiry = getIntent().getIntExtra(EXTRA_SELECTED_GROUP_EXPIRY, SelectGroupExpiryDialogFragment.OPTION_30_DAYS);

        viewModel = new ViewModelProvider(this, new CreateGroupViewModel.Factory(getApplication())).get(CreateGroupViewModel.class);
        viewModel.setContentExpiry(groupExpiry);

        viewModel.setAvatar(smallAvatarPath, largeAvatarPath);

        viewModel.getCreateGroupWorkInfo().observe(this, workInfos -> {
            for (WorkInfo workInfo : workInfos) {
                final WorkInfo.State state = workInfo.getState();
                Log.i("GroupCreationPickerActivity: work " + workInfo.getId() + " " + state);
                if (state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED) {
                    if (createGroupDialog == null) {
                        createGroupDialog = ProgressDialog.show(GroupCreationPickerActivity.this, null, getString(R.string.create_group_in_progress, groupName), true);
                        createGroupDialog.show();
                    }
                    waitingForResult = true;
                } else if (waitingForResult) {
                    if (createGroupDialog != null) {
                        createGroupDialog.cancel();
                    }
                    if (state == WorkInfo.State.FAILED) {
                        SnackbarHelper.showWarning(GroupCreationPickerActivity.this, R.string.failed_create_group);
                    } else if (state == WorkInfo.State.SUCCEEDED) {
                        String rawGroupId = workInfo.getOutputData().getString(CreateGroupViewModel.CreateGroupWorker.WORKER_OUTPUT_GROUP_ID);
                        int memberCount = workInfo.getOutputData().getInt(CreateGroupViewModel.CreateGroupWorker.WORKER_OUTPUT_MEMBER_COUNT, 1);
                        GroupId groupId = new GroupId(Preconditions.checkNotNull(rawGroupId));
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(RESULT_GROUP_ID, groupId);
                        resultIntent.putExtra(RESULT_MEMBER_COUNT, memberCount);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                    waitingForResult = false;
                }
            }
        });
    }

    @Override
    protected int getMaxSelection() {
        return serverProps.getMaxGroupSize();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.finish) {
            waitingForResult = true;
            Log.i("GroupCreationPickerActivity/onOptionsItemSelected starting group creation");
            if (groupChat) {
                viewModel.createGroupChat(groupName, new ArrayList<>(selectedContacts));
            } else {
                viewModel.createFeedGroup(groupName, new ArrayList<>(selectedContacts));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
