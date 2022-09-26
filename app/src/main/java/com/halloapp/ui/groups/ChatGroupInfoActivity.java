package com.halloapp.ui.groups;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.R;
import com.halloapp.id.GroupId;
import com.halloapp.util.Preconditions;

public class ChatGroupInfoActivity extends BaseGroupInfoActivity {

    public static Intent viewGroup(@NonNull Context context, @NonNull GroupId groupId) {
        Preconditions.checkNotNull(groupId);
        Intent intent = new Intent(context, ChatGroupInfoActivity.class);
        intent.putExtra(GROUP_ID, groupId);
        return intent;
    }

    private GroupChatInfoViewModel viewModel;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_group_chat_info;
    }

    @Override
    protected BaseGroupInfoViewModel getViewModel() {
        if (viewModel != null) {
            return viewModel;
        }
        viewModel = new ViewModelProvider(this, new GroupChatInfoViewModel.Factory(getApplication(), groupId)).get(GroupChatInfoViewModel.class);
        return viewModel;
    }
}
