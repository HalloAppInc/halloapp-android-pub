package com.halloapp.ui.groups;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.R;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.contacts.MultipleContactPickerActivity;
import com.halloapp.util.logs.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

public class GroupCreationPickerActivity extends MultipleContactPickerActivity {

    private static final int REQUEST_CODE_SELECT_CONTACTS = 1;

    public static Intent newIntent(@NonNull Context context, @Nullable Collection<UserId> selectedIds) {
        Intent intent = new Intent(context, GroupCreationPickerActivity.class);
        if (selectedIds != null) {
            intent.putParcelableArrayListExtra(EXTRA_SELECTED_IDS, new ArrayList<>(selectedIds));
        }
        intent.putExtra(EXTRA_TITLE_RES, R.string.group_picker_title);
        intent.putExtra(EXTRA_ACTION_RES, R.string.next);
        intent.putExtra(EXTRA_ONLY_FRIENDS, false);
        intent.putExtra(EXTRA_ALLOW_EMPTY_SELECTION, true);
        return intent;
    }

    private final ServerProps serverProps = ServerProps.getInstance();

    @Override
    protected int getMaxSelection() {
        return serverProps.getMaxGroupSize();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.finish) {
            startActivityForResult(CreateGroupActivity.newPickerIntent(this, selectedContacts), REQUEST_CODE_SELECT_CONTACTS);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {
            case REQUEST_CODE_SELECT_CONTACTS:
                if (resultCode == RESULT_OK) {
                    if (data == null) {
                        Log.e("ContactsActivity/onActivityResult missing resulting group id");
                        finish();
                        break;
                    }
                    GroupId groupId = data.getParcelableExtra(CreateGroupActivity.RESULT_GROUP_ID);
                    if (groupId != null) {
                        startActivity(ViewGroupFeedActivity.viewFeed(getApplicationContext(), groupId));
                    }
                    finish();
                } else if (data != null) {
                    List<UserId> userIds = data.getParcelableArrayListExtra(EXTRA_SELECTED_IDS);
                    selectedContacts = new LinkedHashSet<>(userIds);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
