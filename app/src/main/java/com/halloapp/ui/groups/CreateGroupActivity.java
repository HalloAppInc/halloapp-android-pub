package com.halloapp.ui.groups;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.contacts.UserId;
import com.halloapp.content.ContentDb;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.chat.ChatActivity;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;
import com.halloapp.widget.CenterToast;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.groups.GroupsApi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CreateGroupActivity extends HalloActivity {
    private static final String EXTRA_USER_IDS = "user_ids";

    private final Connection connection = Connection.getInstance();
    private final GroupsApi groupsApi = new GroupsApi(connection);

    private ContentDb contentDb;

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
        setContentView(R.layout.activity_create_group);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        contentDb = ContentDb.getInstance(getApplicationContext());

        userIds = getIntent().getParcelableArrayListExtra(EXTRA_USER_IDS);
        if (userIds == null) {
            userIds = new ArrayList<>();
        }

        nameEditText = findViewById(R.id.name);
        nameEditText.requestFocus();

        final TextView counterView = findViewById(R.id.counter);
        final View createButton = findViewById(R.id.create);
        final View updateProgress = findViewById(R.id.progress);

        nameEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.MAX_GROUP_NAME_LENGTH)});
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                counterView.setText(getString(R.string.counter, s.length(), Constants.MAX_GROUP_NAME_LENGTH));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        createButton.setOnClickListener(v -> {
            final String name = StringUtils.preparePostText(Preconditions.checkNotNull(nameEditText.getText()).toString());
            if (TextUtils.isEmpty(name)) {
                CenterToast.show(this, R.string.name_must_be_specified);
                nameEditText.requestFocus();
                return;
            }

            nameEditText.setEnabled(false);
            createButton.setVisibility(View.GONE);
            updateProgress.setVisibility(View.VISIBLE);
            groupsApi.createGroup(name, userIds)
                    .onResponse(groupInfo -> {
                        contentDb.addGroupChat(groupInfo.gid, groupInfo.name, () -> {
                            final Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                            intent.putExtra(ChatActivity.EXTRA_CHAT_ID, groupInfo.gid);
                            startActivity(intent);
                            finish();
                        });
                    })
                    .onError(error -> {
                        Log.e("Create group failed", error);
                        CenterToast.show(getBaseContext(), R.string.failed_create_group);
                        nameEditText.setEnabled(true);
                        nameEditText.requestFocus();
                        createButton.setVisibility(View.VISIBLE);
                        updateProgress.setVisibility(View.GONE);
                    });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("CreateGroupActivity.onDestroy");
    }

}
