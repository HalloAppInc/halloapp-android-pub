package com.halloapp.ui.groups;

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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.id.GroupId;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.SystemUiVisibility;
import com.halloapp.ui.avatar.AvatarPreviewActivity;
import com.halloapp.ui.chat.chat.ChatActivity;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.util.DialogFragmentUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;

public class CreateGroupActivity extends HalloActivity implements SelectGroupExpiryDialogFragment.Host {

    private static final int CODE_CHANGE_AVATAR = 1;
    private static final int REQUEST_CODE_SELECT_CONTACTS = 2;

    private static final String EXTRA_CREATE_GROUP_CHAT = "create_group_chat";

    private final ServerProps serverProps = ServerProps.getInstance();

    private CreateGroupViewModel viewModel;

    private boolean createChat = false;

    private EditText nameEditText;
    private ShapeableImageView avatarView;

    private MenuItem nextMenuItem;

    public static Intent newFeedPickerIntent(@NonNull Context context) {
        return new Intent(context, CreateGroupActivity.class);
    }

    public static Intent newChatPickerIntent(@NonNull Context context) {
        Intent i = new Intent(context, CreateGroupActivity.class);
        i.putExtra(EXTRA_CREATE_GROUP_CHAT, true);
        return i;
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

        createChat = getIntent().getBooleanExtra(EXTRA_CREATE_GROUP_CHAT, false);

        viewModel = new ViewModelProvider(this, new CreateGroupViewModel.Factory(getApplication())).get(CreateGroupViewModel.class);

        nameEditText = findViewById(R.id.edit_name);
        avatarView = findViewById(R.id.avatar);
        if (createChat) {
            avatarView.setShapeAppearanceModel(ShapeAppearanceModel.builder(this,  R.style.CircularImageView, 0).build());
        } else {
            avatarView.setShapeAppearanceModel(ShapeAppearanceModel.builder(this,  R.style.RoundedGroupAvatarImageView, 0).build());
        }
        final View changeAvatarView = findViewById(R.id.change_avatar);

        View groupExpiryContainer = findViewById(R.id.expire_content_container);
        groupExpiryContainer.setOnClickListener(v -> {
            Integer expiry = viewModel.getContentExpiry().getValue();
            DialogFragmentUtils.showDialogFragmentOnce(SelectGroupExpiryDialogFragment.newInstance(expiry == null ? SelectGroupExpiryDialogFragment.OPTION_30_DAYS : expiry), getSupportFragmentManager());
        });

        if (ServerProps.getInstance().isGroupExpiryEnabled() && !createChat) {
            groupExpiryContainer.setVisibility(View.VISIBLE);
        } else {
            groupExpiryContainer.setVisibility(View.GONE);
        }

        TextView groupExpiryDesc = findViewById(R.id.group_expiry_description);
        ImageView groupExpiryIcon = findViewById(R.id.group_expiry_icon);

        viewModel.getContentExpiry().observe(this, setting -> {
            switch (setting) {
                case SelectGroupExpiryDialogFragment.OPTION_24_HOURS:
                    groupExpiryIcon.setImageResource(R.drawable.ic_content_expiry);
                    groupExpiryDesc.setText(R.string.expiration_day);
                    break;
                case SelectGroupExpiryDialogFragment.OPTION_NEVER:
                    groupExpiryDesc.setText(R.string.expiration_never);
                    groupExpiryIcon.setImageResource(R.drawable.ic_content_no_expiry);
                    break;
                case SelectGroupExpiryDialogFragment.OPTION_30_DAYS:
                default:
                    groupExpiryIcon.setImageResource(R.drawable.ic_content_expiry);
                    groupExpiryDesc.setText(R.string.expiration_month);
                    break;

            }
        });

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
    }

    private void updateAction(boolean enabled) {
        if (nextMenuItem != null) {
            SpannableString ss = new SpannableString(getString(R.string.next));
            ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getBaseContext(), enabled ? R.color.color_secondary : R.color.disabled_text)), 0, ss.length(), 0);
            nextMenuItem.setTitle(ss);
            nextMenuItem.setEnabled(enabled);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {
            case REQUEST_CODE_SELECT_CONTACTS:
                if (resultCode == RESULT_OK) {
                    if (data == null) {
                        Log.e("CreateGroupActivity/onActivityResult missing resulting group id");
                        finish();
                        break;
                    }
                    GroupId groupId = data.getParcelableExtra(GroupCreationPickerActivity.RESULT_GROUP_ID);
                    int memberCount = data.getIntExtra(GroupCreationPickerActivity.RESULT_MEMBER_COUNT, 1);
                    if (groupId != null) {
                        if (createChat) {
                            startActivity(ChatActivity.open(getApplicationContext(), groupId));
                        } else {
                            startActivity(ViewGroupFeedActivity.openGroupPostCreation(getApplicationContext(), groupId, memberCount < serverProps.getMaxMemberForInviteSheet()));
                        }
                    }
                    finish();
                }
                break;
            case CODE_CHANGE_AVATAR: {
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        int height = data.getIntExtra(AvatarPreviewActivity.RESULT_AVATAR_HEIGHT, - 1);
                        int width = data.getIntExtra(AvatarPreviewActivity.RESULT_AVATAR_WIDTH, -1);
                        String filePath = data.getStringExtra(AvatarPreviewActivity.RESULT_AVATAR_FILE_PATH);
                        String largeFilePath = data.getStringExtra(AvatarPreviewActivity.RESULT_LARGE_AVATAR_FILE_PATH);
                        if (filePath != null && largeFilePath != null && width > 0 && height > 0) {
                            viewModel.setAvatar(filePath, largeFilePath);
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
        nextMenuItem = menu.findItem(R.id.next);
        updateAction(nameEditText != null && !TextUtils.isEmpty(nameEditText.getText()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.next) {
            final String name = StringUtils.preparePostText(Preconditions.checkNotNull(nameEditText.getText()).toString());
            if (TextUtils.isEmpty(name)) {
                SnackbarHelper.showWarning(this, R.string.name_must_be_specified);
                nameEditText.requestFocus();
                return true;
            }
            Intent memberSelection;
            if (createChat) {
                memberSelection = GroupCreationPickerActivity.newGroupChat(this, name, viewModel.getAvatarFile(), viewModel.getLargeAvatarFile());
            } else {
                memberSelection = GroupCreationPickerActivity.newFeedGroup(this, name, viewModel.getAvatarFile(), viewModel.getLargeAvatarFile(), viewModel.getContentExpiry().getValue());
            }
            startActivityForResult(memberSelection, REQUEST_CODE_SELECT_CONTACTS);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onExpirySelected(int selectedOption) {
        viewModel.setContentExpiry(selectedOption);
    }
}
