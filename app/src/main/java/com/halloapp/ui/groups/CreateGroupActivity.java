package com.halloapp.ui.groups;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.BidiFormatter;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkInfo;

import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.SystemUiVisibility;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.avatar.AvatarPreviewActivity;
import com.halloapp.ui.contacts.ContactsSectionItemDecoration;
import com.halloapp.ui.contacts.MultipleContactPickerActivity;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class CreateGroupActivity extends HalloActivity {
    private static final String EXTRA_USER_IDS = "user_ids";

    public static final String RESULT_GROUP_ID = "group_id";
    public static final String RESULT_USER_IDS = "group_user_ids";

    private static final int CODE_CHANGE_AVATAR = 1;

    private CreateGroupViewModel viewModel;

    private EditText nameEditText;
    private ImageView avatarView;
    private RecyclerView membersView;
    private ProgressDialog createGroupDialog;

    private final ContactsAdapter adapter = new ContactsAdapter();

    private ArrayList<UserId> userIds;

    private MenuItem createMenuItem;

    private boolean waitingForResult = false;

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

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_create_group);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        userIds = getIntent().getParcelableArrayListExtra(EXTRA_USER_IDS);
        if (userIds == null) {
            userIds = new ArrayList<>();
        }

        Intent cancelIntent = new Intent();
        cancelIntent.putParcelableArrayListExtra(MultipleContactPickerActivity.EXTRA_SELECTED_IDS, new ArrayList<>(userIds));
        setResult(RESULT_CANCELED, cancelIntent);

        viewModel = new ViewModelProvider(this, new CreateGroupViewModel.Factory(getApplication(), userIds)).get(CreateGroupViewModel.class);

        nameEditText = findViewById(R.id.edit_name);
        avatarView = findViewById(R.id.avatar);
        final View changeAvatarView = findViewById(R.id.change_avatar);
        membersView = findViewById(R.id.members);

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

        viewModel.getAvatar().observe(this, bitmap -> avatarView.setImageBitmap(bitmap));

        changeAvatarView.setOnClickListener(v -> {
            Log.d("CreateGroupActivity request change avatar");
            final Intent intent = MediaPickerActivity.pickGroupAvatar(this);
            startActivityForResult(intent, CODE_CHANGE_AVATAR);
        });


        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        membersView.setLayoutManager(layoutManager);
        membersView.setAdapter(adapter);
        membersView.addItemDecoration(new ContactsSectionItemDecoration(
                getResources().getDimension(R.dimen.contacts_list_item_header_width),
                getResources().getDimension(R.dimen.contacts_list_item_height),
                getResources().getDimension(R.dimen.contacts_list_item_header_text_size),
                getResources().getColor(R.color.contacts_list_item_header_text_color),
                adapter::getSectionName));
        viewModel.getContacts().observe(this, adapter::setContacts);

        viewModel.getCreateGroupWorkInfo().observe(this, new Observer<List<WorkInfo>>() {

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
                        waitingForResult = true;
                    } else if (waitingForResult) {
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
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra(RESULT_GROUP_ID, groupId);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        }
                        waitingForResult = false;
                    }
                }
            }
        });
    }

    private void updateAction(boolean enabled) {
        if (createMenuItem != null) {
            SpannableString ss = new SpannableString(getString(R.string.button_create_group));
            ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getBaseContext(), enabled ? R.color.color_secondary : R.color.disabled_text)), 0, ss.length(), 0);
            createMenuItem.setTitle(ss);
            createMenuItem.setEnabled(enabled);
        }
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
        if (item.getItemId() == R.id.create) {
            final String name = StringUtils.preparePostText(Preconditions.checkNotNull(nameEditText.getText()).toString());
            if (TextUtils.isEmpty(name)) {
                SnackbarHelper.showWarning(this, R.string.name_must_be_specified);
                nameEditText.requestFocus();
                return true;
            }

            waitingForResult = true;
            nameEditText.setEnabled(false);
            Log.i("CreateGroupActivity/onOptionsItemSelected starting group creation");
            viewModel.createGroup(name, userIds);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ContactsAdapter extends RecyclerView.Adapter<ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {
        private static final int ITEM_TYPE_CONTACT = 1;
        private static final int ITEM_TYPE_MEMBERS_HEADER = 2;

        private List<Contact> contacts = new ArrayList<>();

        void setContacts(@NonNull List<Contact> contacts) {
            this.contacts = contacts;
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? ITEM_TYPE_MEMBERS_HEADER : ITEM_TYPE_CONTACT;
        }

        @Override
        public @NonNull
        ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case ITEM_TYPE_CONTACT: {
                    return new ContactViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false));
                }
                case ITEM_TYPE_MEMBERS_HEADER: {
                    return new MembersHeaderHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.members_header, parent, false));
                }
                default: {
                    throw new IllegalArgumentException("Invalid view type " + viewType);
                }
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (position != 0) {
                holder.bindTo(contacts.get(position - 1));
            }
        }

        @Override
        public int getItemCount() {
            return contacts.size() + 1;
        }

        @NonNull
        @Override
        public String getSectionName(int position) {
            if (position <= 1 || contacts == null || position >= contacts.size() + 1) {
                return "";
            }
            final String name = contacts.get(position - 1).getDisplayName();
            if (TextUtils.isEmpty(name)) {
                return "";
            }
            final int codePoint = name.codePointAt(0);
            return Character.isAlphabetic(codePoint) ? new String(Character.toChars(codePoint)).toUpperCase(Locale.getDefault()) : "#";
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void bindTo(Contact contact) {
        }
    }

    static class ContactViewHolder extends ViewHolder {
        private final Me me;
        private final AvatarLoader avatarLoader;

        final private ImageView avatarView;
        final private TextView nameView;
        final private TextView phoneView;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.avatar);
            nameView = itemView.findViewById(R.id.name);
            phoneView = itemView.findViewById(R.id.phone);

            me = Me.getInstance();
            avatarLoader = AvatarLoader.getInstance();
        }

        void bindTo(@NonNull Contact contact) {
            avatarLoader.load(avatarView, Preconditions.checkNotNull(contact.userId));
            if (contact.userId.isMe()) {
                nameView.setText(R.string.me);
                phoneView.setText(BidiFormatter.getInstance().unicodeWrap(PhoneNumberUtils.formatNumber("+" + me.getPhone(), null)));
            } else {
                nameView.setText(contact.getDisplayName());
                phoneView.setText(contact.getDisplayPhone());
            }
        }
    }

    class MembersHeaderHolder extends ViewHolder {

        MembersHeaderHolder(@NonNull View itemView) {
            super(itemView);

            TextView membersTextView = itemView.findViewById(R.id.members_header_text);
            membersTextView.setText(getString(R.string.members_header, Integer.toString(userIds.size() + 1)));
        }
    }
}
