    package com.halloapp.ui.groups;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.SharedElementCallback;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.TransitionManager;

import com.halloapp.BuildConfig;
import com.halloapp.Debug;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.content.ContentDb;
import com.halloapp.id.GroupId;
import com.halloapp.media.MediaUtils;
import com.halloapp.nux.InviteGroupBottomSheetDialogFragment;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.ContentComposerActivity;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.MediaPagerAdapter;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.camera.CameraActivity;
import com.halloapp.ui.mediaexplorer.MediaExplorerActivity;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.util.DialogFragmentUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.FabExpandOnScrollListener;
import com.halloapp.widget.HACustomFab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ViewGroupFeedActivity extends HalloActivity implements FabExpandOnScrollListener.Host {

    private static final String KEY_INTERACTED_WITH = "interacted_with";
    private static final int INTERACTION_TIMEOUT_MS = 30_000;

    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_VIEW_GROUP_INFO = 2;

    private static final String EXTRA_GROUP_ID = "group_id";
    private static final String EXTRA_FROM_CONTENT_COMPOSER = "from_content_composer";
    private static final String EXTRA_SHOW_INVITE_BOTTOM_SHEET = "show_invite_sheet";

    public static Intent viewFeed(@NonNull Context context, @NonNull GroupId groupId) {
        Preconditions.checkNotNull(groupId);
        Intent intent = new Intent(context, ViewGroupFeedActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        return intent;
    }

    public static Intent viewFeed(@NonNull Context context, @NonNull GroupId groupId, boolean fromComposer) {
        Intent intent = viewFeed(context, groupId);
        intent.putExtra(EXTRA_FROM_CONTENT_COMPOSER, fromComposer);
        return intent;
    }

    public static Intent openGroupPostCreation(@NonNull Context context, @NonNull GroupId groupId, boolean showInviteSheet) {
        Intent intent = viewFeed(context, groupId);
        intent.putExtra(EXTRA_SHOW_INVITE_BOTTOM_SHEET, showInviteSheet);
        return intent;
    }

    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();

    private GroupFeedViewModel viewModel;

    private GroupId groupId;

    private HACustomFab fabView;

    private TextView titleView;
    private TextView subtitleView;

    private boolean showGroupInviteSheet;

    private final Runnable hideSubtitle = () -> {
        if (subtitleView != null) {
            TransitionManager.beginDelayedTransition((ViewGroup) subtitleView.getParent());
            subtitleView.setVisibility(View.GONE);
        }
    };

    private final SharedElementCallback sharedElementCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            for (String name : names) {
                View view = MediaPagerAdapter.getTransitionView(findViewById(R.id.profile_fragment_placeholder), name);
                if (view != null) {
                    sharedElements.put(name, view);
                }
            }
        }
    };

    private boolean userInteracted = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        supportRequestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setExitSharedElementCallback(sharedElementCallback);

        setTitle("");
        groupId = getIntent().getParcelableExtra(EXTRA_GROUP_ID);
        if (groupId == null) {
            finish();
            Log.e("ViewGroupFeedActivity/onCreate must provide a valid group id");
            return;
        }

        viewModel = new ViewModelProvider(getViewModelStore(), new GroupFeedViewModel.Factory(groupId)).get(GroupFeedViewModel.class);

        setContentView(R.layout.activity_view_group_feed);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.profile_fragment_placeholder, GroupFeedFragment.newInstance(groupId))
                .commit();

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        titleView = findViewById(R.id.title);
        subtitleView = findViewById(R.id.subtitle);
        ImageView avatarView = findViewById(R.id.avatar);

        View toolbarTitleContainer = findViewById(R.id.toolbar_text_container);
        toolbarTitleContainer.setOnClickListener(v -> {
            startActivityForResult(GroupInfoActivity.viewGroup(this, groupId), REQUEST_CODE_VIEW_GROUP_INFO);
        });

        showGroupInviteSheet = getIntent().getBooleanExtra(EXTRA_SHOW_INVITE_BOTTOM_SHEET, false);

        viewModel.chat.getLiveData().observe(this, chat -> {
            if (chat != null) {
                titleView.setText(chat.name);
                avatarLoader.load(avatarView, groupId, false);
                ContentDb.getInstance().setGroupSeen((GroupId) chat.chatId);
                if (showGroupInviteSheet) {
                    if (TextUtils.isEmpty(chat.inviteToken)) {
                        Log.e("ViewGroupFeedActivity/no invite link token for group, not showing invite sheet!");
                    } else {
                        DialogFragmentUtils.showDialogFragmentOnce(InviteGroupBottomSheetDialogFragment.newInstance(chat.inviteToken), getSupportFragmentManager());
                    }
                    showGroupInviteSheet = false;
                }
            } else {
                titleView.setText(null);
            }
        });
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_INTERACTED_WITH)) {
            if (savedInstanceState.getBoolean(KEY_INTERACTED_WITH)) {
                markUserInteracted();
            }
        }
        if (getIntent().getBooleanExtra(EXTRA_FROM_CONTENT_COMPOSER, false)) {
            markUserInteracted();
        }
        avatarLoader.load(avatarView, groupId, false);

        fabView = findViewById(R.id.ha_fab);
        fabView.setOnActionSelectedListener(new HACustomFab.OnActionSelectedListener() {
            @Override
            public void onActionSelected(int actionId) {
                onFabActionSelected(actionId);
            }

            @Override
            public void onOverlay(boolean visible) {
                ConstraintLayout root = findViewById(R.id.container);
                for (int i = 0; i < root.getChildCount(); i++) {
                    View child = root.getChildAt(i);
                    if (child.getId() == R.id.ha_fab) {
                        continue;
                    }
                    if (visible) {
                        ViewCompat.setImportantForAccessibility(child, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
                    } else {
                        ViewCompat.setImportantForAccessibility(child, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
                    }
                }
            }
        });
        fabView.setMainFabIcon(R.drawable.ic_plus_expanded, R.string.new_post, R.string.post_fab_label);
        fabView.addSubFab(R.id.add_post_gallery, R.drawable.ic_image, R.string.gallery_post);
        if (ServerProps.getInstance().getVoicePostsEnabled()) {
            fabView.addSubFab(R.id.add_post_voice, R.drawable.ic_voice_post, R.string.voice_post);
        }
        fabView.addSubFab(R.id.add_post_text, R.drawable.ic_text, R.string.text_post);
        fabView.addSubFab(R.id.add_post_camera, R.drawable.ic_camera, R.string.camera_post);

        viewModel.members.getLiveData().observe(this, members -> {
            if (members == null) {
                fabView.hide();
                return;
            }
            for (Contact contact : members) {
                if (contact.userId != null && contact.userId.isMe()) {
                    fabView.show();
                    return;
                }
            }
            fabView.hide();
        });

        final View newPostsView = findViewById(R.id.new_posts);
        newPostsView.setOnClickListener(v -> viewModel.reloadPostsAt(Long.MAX_VALUE));

        titleView.postDelayed(this::markUserInteracted, INTERACTION_TIMEOUT_MS);
    }

    @Override
    protected void onResume() {
        super.onResume();

        subtitleView.removeCallbacks(hideSubtitle);
        subtitleView.setVisibility(View.VISIBLE);
        subtitleView.postDelayed(hideSubtitle, 3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        titleView.removeCallbacks(this::markUserInteracted);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra(EXTRA_FROM_CONTENT_COMPOSER, false)) {
            markUserInteracted();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_INTERACTED_WITH, userInteracted);
    }

    private void onFabActionSelected(@IdRes int id) {
        if (id == R.id.add_post_text) {
            Intent intent = new Intent(this, ContentComposerActivity.class);
            intent.putExtra(ContentComposerActivity.EXTRA_GROUP_ID, groupId);
            startActivity(intent);
        } else if (id == R.id.add_post_gallery) {
            final Intent intent = MediaPickerActivity.pickForPost(this, groupId);
            startActivity(intent);
        } else if (id == R.id.add_post_camera) {
            final Intent intent = new Intent(this, CameraActivity.class);
            intent.putExtra(CameraActivity.EXTRA_GROUP_ID, groupId);
            startActivity(intent);
        } else if (id == R.id.add_post_voice) {
            Intent i = new Intent(this, ContentComposerActivity.class);
            i.putExtra(CameraActivity.EXTRA_GROUP_ID, groupId);
            i.putExtra(ContentComposerActivity.EXTRA_VOICE_NOTE_POST, true);
            startActivity(i);
        }
        fabView.close(false);
    }

    private void markUserInteracted() {
        userInteracted = true;
        setResult(RESULT_OK);
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        if (resultCode == RESULT_OK && data.hasExtra(MediaExplorerActivity.EXTRA_CONTENT_ID) && data.hasExtra(MediaExplorerActivity.EXTRA_SELECTED)) {
            String contentId = data.getStringExtra(MediaExplorerActivity.EXTRA_CONTENT_ID);
            int position = data.getIntExtra(MediaExplorerActivity.EXTRA_SELECTED, 0);
            View root = findViewById(R.id.profile_fragment_placeholder);

            if (root != null && contentId != null) {
                postponeEnterTransition();
                MediaPagerAdapter.preparePagerForTransition(root, contentId, position, this::startPostponedEnterTransition);
            }
        }
    }

    @Override
    public void onActivityResult(final int request, final int result, final Intent data) {
        super.onActivityResult(request, result, data);
        switch (request) {
            case REQUEST_CODE_CAPTURE_IMAGE: {
                if (result == Activity.RESULT_OK) {
                    final Intent intent = new Intent(this, ContentComposerActivity.class);
                    intent.putExtra(ContentComposerActivity.EXTRA_GROUP_ID, groupId);
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
                            new ArrayList<>(Collections.singleton(MediaUtils.getImageCaptureUri(this))));
                    startActivity(intent);
                }
                break;
            }
            case REQUEST_CODE_VIEW_GROUP_INFO: {
                if (result == GroupInfoActivity.RESULT_CODE_EXIT_CHAT) {
                    finish();
                }
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (fabView.isOpen()) {
            fabView.close();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (BuildConfig.DEBUG && keyCode == KeyEvent.KEYCODE_BACK) {
            Debug.showGroupDebugMenu(this, fabView, groupId);
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public HACustomFab getFab() {
        return fabView;
    }
}
