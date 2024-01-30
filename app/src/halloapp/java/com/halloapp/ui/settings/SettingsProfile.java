package com.halloapp.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkInfo;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.contacts.SocialLink;
import com.halloapp.id.UserId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.HalloBottomSheetDialog;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.avatar.AvatarPreviewActivity;
import com.halloapp.ui.camera.CameraActivity;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.util.logs.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;
import com.halloapp.widget.CenterToast;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.UsernameResponseIq;
import com.halloapp.xmpp.util.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SettingsProfile extends HalloActivity {

    private static final int CODE_CHANGE_AVATAR = 1;
    private static final int MAX_LINKS_SIZE = 5;
    private static final int DEBOUNCE_DELAY_MS = 300;

    private SettingsProfileViewModel viewModel;

    private AvatarLoader avatarLoader;

    private TextView nameView, nameCounterView, usernameView, usernameCounterView, usernameError;
    private RecyclerView linksView;
    private View addMoreView;
    private ImageView avatarView;
    private FrameLayout changeAvatarLayout;
    private View changeAvatarView;
    private ImageView tempAvatarView;
    private View saveButton;

    private Runnable debounceRunnable;
    private Observable<UsernameResponseIq> checkUsernameIsAvailable;

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
                        String largeFilePath = data.getStringExtra(AvatarPreviewActivity.RESULT_LARGE_AVATAR_FILE_PATH);
                        if (filePath != null && largeFilePath != null && width > 0 && height > 0) {
                            viewModel.setTempAvatar(filePath, largeFilePath, width, height);
                        }
                    }
                }
                break;
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_profile);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        avatarLoader = AvatarLoader.getInstance();

        viewModel = new ViewModelProvider(this).get(SettingsProfileViewModel.class);

        nameView = findViewById(R.id.edit_name);
        nameCounterView = findViewById(R.id.name_counter);
        usernameView = findViewById(R.id.username);
        usernameCounterView = findViewById(R.id.username_counter);
        usernameError = findViewById(R.id.username_error);
        addMoreView = findViewById(R.id.add_more);

        linksView = findViewById(R.id.links);
        LinksAdapter adapter = new LinksAdapter();
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        linksView.setLayoutManager(layoutManager);
        linksView.setAdapter(adapter);

        viewModel.getLinks().observe(this, links -> {
            addMoreView.setVisibility(links.size() < MAX_LINKS_SIZE ? View.VISIBLE : View.GONE);
            adapter.setItems(links);
        });

        avatarView = findViewById(R.id.avatar);
        tempAvatarView = findViewById(R.id.temp_avatar);
        changeAvatarView = findViewById(R.id.change_avatar);
        changeAvatarLayout = findViewById(R.id.change_avatar_camera_btn);
        saveButton = findViewById(R.id.save);
        final View progressBar = findViewById(R.id.progress);

        viewModel.getName().observe(this, text -> nameView.setText(text));
        viewModel.getUsername().observe(this, text -> usernameView.setText(text));

        viewModel.canSave().observe(this, saveButton::setEnabled);
        viewModel.getTempAvatar().observe(this, avatar -> {
            tempAvatarView.setVisibility(View.VISIBLE);
            if (avatar != null) {
                tempAvatarView.setImageBitmap(avatar);
            } else {
                tempAvatarView.setImageResource(R.drawable.avatar_person);
            }
        });

        avatarLoader.load(avatarView, UserId.ME, false);

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
                viewModel.setTempName(s.toString());
                nameCounterView.setText(String.format(Locale.getDefault(), "%d", s.toString().length()));
            }
        });

        usernameView.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.MAX_NAME_LENGTH)});
        usernameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null) {
                    return;
                }

                final String tempUsername = s.toString();
                saveButton.setEnabled(false);

                if (debounceRunnable != null) {
                    saveButton.removeCallbacks(debounceRunnable);
                    debounceRunnable = null;
                }
                cancelCheckUsernameIsAvailable();
                if (!showUsernameErrors(tempUsername) && !tempUsername.equals(viewModel.getUsername().getValue())) {
                    debounceRunnable = () -> checkUsernameAvailability(tempUsername);
                    saveButton.postDelayed(debounceRunnable, DEBOUNCE_DELAY_MS);
                }
                usernameCounterView.setText(String.format(Locale.getDefault(), "%d", s.toString().length()));
            }
        });

        viewModel.getSaveProfileWorkInfo().observe(this, new Observer<List<WorkInfo>>() {

            boolean running;

            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                for (WorkInfo workInfo : workInfos) {
                    final WorkInfo.State state = workInfo.getState();
                    Log.i("SettingsProfile: work " + workInfo.getId() + " " + state);
                    if (state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED) {
                        nameView.setEnabled(false);
                        saveButton.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.VISIBLE);
                        running = true;
                    } else if (running) {
                        progressBar.setVisibility(View.GONE);
                        if (state == WorkInfo.State.FAILED) {
                            SnackbarHelper.showWarning(SettingsProfile.this, R.string.failed_update_profile);
                            nameView.setEnabled(true);
                            nameView.requestFocus();
                            saveButton.setVisibility(View.VISIBLE);
                        } else if (state == WorkInfo.State.SUCCEEDED) {
                            CenterToast.show(getBaseContext(), R.string.profile_updated);
                            setResult(RESULT_OK);
                            finish();
                        }
                        running = false;
                    }
                }
            }
        });

        saveButton.setOnClickListener(v -> {
            final String name = StringUtils.preparePostText(Preconditions.checkNotNull(nameView.getText()).toString());
            if (TextUtils.isEmpty(name)) {
                SnackbarHelper.showWarning(this, R.string.name_must_be_specified);
                nameView.requestFocus();
                return;
            }
            viewModel.saveProfile();
        });

        viewModel.getHasAvatarSet().observe(this, (avatarSet) -> {
            setPhotoSelectOptions(avatarSet);
        });

        addMoreView.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(v.getContext(), v);
            menu.inflate(R.menu.social_media_links_menu);
            menu.setOnMenuItemClickListener(item -> {
                switch(item.getItemId()) {
                    case R.id.instagram:
                        viewModel.addLink(new SocialLink("instagram.com/", SocialLink.Type.INSTAGRAM));
                        break;
                    case R.id.x_twitter:
                        viewModel.addLink(new SocialLink("x.com/", SocialLink.Type.X));
                        break;
                    case R.id.tiktok:
                        viewModel.addLink(new SocialLink("tiktok.com/", SocialLink.Type.TIKTOK));
                        break;
                    case R.id.youtube:
                        viewModel.addLink(new SocialLink("youtube.com/", SocialLink.Type.YOUTUBE));
                        break;
                    case R.id.link:
                        viewModel.addLink(new SocialLink("https://", SocialLink.Type.USER_DEFINED));
                        break;
                }
                return true;
            });
            menu.show();
        });
    }

    final View.OnClickListener setAvatarListener = v -> {
        final Intent intent = MediaPickerActivity.pickAvatar(this);
        startActivityForResult(intent, CODE_CHANGE_AVATAR);
    };

    final View.OnClickListener avatarOptionsListener = v -> {
        final BottomSheetDialog bottomSheetDialog = new HalloBottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.profile_avatar_change);
        View cameraButton = bottomSheetDialog.findViewById(R.id.profile_avatar_take_photo);
        cameraButton.setOnClickListener(view -> {
            final Intent intent = new Intent(this, CameraActivity.class);
            intent.putExtra(CameraActivity.EXTRA_PURPOSE, CameraActivity.PURPOSE_USER_AVATAR);
            startActivityForResult(intent, CODE_CHANGE_AVATAR);
            bottomSheetDialog.hide();
        });
        View mediaButton = bottomSheetDialog.findViewById(R.id.profile_avatar_picture_select);
        mediaButton.setOnClickListener(view -> {
            final Intent intent = MediaPickerActivity.pickAvatar(this);
            startActivityForResult(intent, CODE_CHANGE_AVATAR);
            bottomSheetDialog.hide();
        });
        View removeAvatar = bottomSheetDialog.findViewById(R.id.profile_avatar_remove_avatar);
        removeAvatar.setOnClickListener(view -> {
            viewModel.removeAvatar();
            bottomSheetDialog.hide();
        });
        bottomSheetDialog.show();
    };

    private void setPhotoSelectOptions(boolean hasAvatar) {
        if (hasAvatar) {
            changeAvatarLayout.setVisibility(View.GONE);
            changeAvatarView.setOnClickListener(avatarOptionsListener);
            avatarView.setOnClickListener(avatarOptionsListener);
            tempAvatarView.setOnClickListener(avatarOptionsListener);
        } else {
            changeAvatarLayout.setVisibility(View.VISIBLE);
            changeAvatarView.setOnClickListener(setAvatarListener);
            avatarView.setOnClickListener(setAvatarListener);
            tempAvatarView.setOnClickListener(setAvatarListener);
        }
    }

    @Override
    public void onBackPressed() {
        if (!viewModel.hasChanges()) {
            super.onBackPressed();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.alert_discard_changes_message);
            builder.setPositiveButton(R.string.action_discard, (dialog, which) -> finish());
            builder.setNegativeButton(R.string.cancel, null);
            builder.create().show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        avatarLoader.load(avatarView, UserId.ME, false);
    }

    private boolean showUsernameErrors(@NonNull String username) {
        String errorString = null;
        if (!username.isEmpty()) {
            if (!Character.isAlphabetic(username.charAt(0))) {
                errorString = getResources().getString(R.string.reg_username_error_leading_char_invalid);
            } else if (!username.matches(Constants.USERNAME_CHARACTERS_REGEX)) {
                errorString = getResources().getString(R.string.reg_username_error_bad_expression);
            } else if (username.length() < Constants.MIN_USERNAME_LENGTH) {
                errorString = getResources().getString(R.string.reg_username_error_too_short);
            }
        }
        usernameError.setText(errorString);
        usernameError.setVisibility(errorString == null ? View.GONE : View.VISIBLE);
        return errorString != null;
    }

    private void checkUsernameAvailability(@NonNull String username) {
        Log.d("SettingsProfile.checkUsernameAvailability: username=" + username);
        cancelCheckUsernameIsAvailable();
        if (!TextUtils.isEmpty(username)) {
            checkUsernameIsAvailable = Connection.getInstance().checkUsernameIsAvailable(username).onResponse(response -> {
                if (response != null && response.success) {
                    runOnUiThread(() -> onUsernameIsAvailable(username));
                } else {
                    final int reason = response != null ? response.reason : UsernameResponseIq.Reason.UNKNOWN;
                    Log.e("SettingsProfile.checkUsernameAvailability UsernameRequest.IS_AVAILABLE call failed with reason=" + reason);
                    runOnUiThread(() -> onUsernameIsAvailableError(reason, username));
                }
            }).onError(e -> {
                Log.e("SettingsProfile.checkUsernameAvailability: UsernameRequest.IS_AVAILABLE call failed", e);
                runOnUiThread(() -> onUsernameIsAvailableError(UsernameResponseIq.Reason.UNKNOWN, username));
            });
        }
    }

    private void onUsernameIsAvailable(@NonNull String username) {
        viewModel.setTempUsername(username);
        saveButton.setEnabled(true);
        usernameError.setVisibility(View.GONE);
    }

    private void onUsernameIsAvailableError(@UsernameResponseIq.Reason int reason, @NonNull String username) {
        final String errorText;
        switch (reason) {
            case UsernameResponseIq.Reason.TOO_SHORT:
                errorText = getResources().getString(R.string.reg_username_error_too_short);
                break;
            case UsernameResponseIq.Reason.TOO_LONG:
                errorText = getResources().getString(R.string.reg_username_error_too_long);
                break;
            case UsernameResponseIq.Reason.BAD_EXPRESSION:
                errorText = getResources().getString(R.string.reg_username_error_bad_expression);
                break;
            case UsernameResponseIq.Reason.NOT_UNIQUE:
                errorText = getResources().getString(R.string.reg_username_error_not_unique, username);
                break;
            case UsernameResponseIq.Reason.UNKNOWN:
                errorText = getResources().getString(R.string.reg_username_error_unknown);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + reason);
        }
        usernameError.setVisibility(View.VISIBLE);
        usernameError.setText(errorText);
    }

    private void cancelCheckUsernameIsAvailable() {
        if (checkUsernameIsAvailable != null) {
            checkUsernameIsAvailable.cancel();
            checkUsernameIsAvailable = null;
        }
    }
    
    public class LinksAdapter extends RecyclerView.Adapter<LinkViewHolder> {

        private List<SocialLink> items = new ArrayList<>();

        public LinksAdapter() {}

        public void setItems(@NonNull List<SocialLink> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public LinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new LinkViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.social_media_edit_link_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull LinkViewHolder holder, int position) {
            holder.bindTo(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    public class LinkViewHolder extends RecyclerView.ViewHolder {

        private SocialLink link;
        private final EditText linkView;
        private final ImageView iconView;
        private final ImageView closeView;

        public LinkViewHolder(@NonNull View itemView) {
            super(itemView);
            linkView = itemView.findViewById(R.id.link);
            iconView = itemView.findViewById(R.id.icon);
            closeView = itemView.findViewById(R.id.close);
            linkView.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (s != null && link != null) {
                        if (!s.toString().startsWith(link.getPrefix())) {
                            linkView.setText(link.getPrefix());
                            linkView.setSelection(link.getPrefix().length());
                        } else {
                            viewModel.setTempLinks(link, s.toString());
                        }
                    }
                }
            });
        }

        public void bindTo(@NonNull SocialLink link) {
            this.link = link;
            switch (link.type) {
                case (SocialLink.Type.TIKTOK):
                    iconView.setImageResource(R.drawable.ic_tiktok);
                    break;
                case (SocialLink.Type.INSTAGRAM):
                    iconView.setImageResource(R.drawable.ic_instagram);
                    break;
                case (SocialLink.Type.X):
                    iconView.setImageResource(R.drawable.ic_twitter_x);
                    break;
                case (SocialLink.Type.YOUTUBE):
                    iconView.setImageResource(R.drawable.ic_youtube);
                    break;
                default:
                    iconView.setImageResource(R.drawable.ic_link);
            }
            String linkText = link.text.startsWith(link.getPrefix()) ? link.text : link.getPrefix() + link.text;
            linkView.setText(linkText);
            closeView.setOnClickListener(v -> viewModel.removeLink(link));
        }
    }
}
