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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.SharedElementCallback;
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
import com.halloapp.ui.ContentComposerActivity;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.MediaPagerAdapter;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.mediaexplorer.MediaExplorerActivity;
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
    private static final String KEY_SHOW_INVITE_SHEET = "show_invite_sheet";
    private static final int INTERACTION_TIMEOUT_MS = 30_000;

    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_VIEW_GROUP_INFO = 2;

    private static final String EXTRA_GROUP_ID = "group_id";
    private static final String EXTRA_FROM_CONTENT_COMPOSER = "from_content_composer";
    private static final String EXTRA_SHOW_INVITE_BOTTOM_SHEET = "show_invite_sheet";
    private static final String EXTRA_TARGET_TIMESTAMP = "target_post_timestamp";

    public static Intent viewFeed(@NonNull Context context, @NonNull GroupId groupId) {
        Preconditions.checkNotNull(groupId);
        Intent intent = new Intent(context, ViewGroupFeedActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        return intent;
    }

    public static Intent viewFeed(@NonNull Context context, @NonNull GroupId groupId, long timestamp) {
        Preconditions.checkNotNull(groupId);
        Intent intent = new Intent(context, ViewGroupFeedActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        intent.putExtra(EXTRA_TARGET_TIMESTAMP, timestamp);
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

        Long scrollToPost = null;
        if (getIntent().hasExtra(EXTRA_TARGET_TIMESTAMP)) {
            scrollToPost = getIntent().getLongExtra(EXTRA_TARGET_TIMESTAMP, 0);
        }

        viewModel = new ViewModelProvider(getViewModelStore(), new GroupFeedViewModel.Factory(groupId)).get(GroupFeedViewModel.class);

        setContentView(R.layout.activity_view_group_feed);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.profile_fragment_placeholder, GroupFeedFragment.newInstance(groupId, scrollToPost))
                .commit();

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        titleView = findViewById(R.id.title);
        subtitleView = findViewById(R.id.subtitle);
        ImageView avatarView = findViewById(R.id.avatar);

        View toolbarTitleContainer = findViewById(R.id.toolbar_text_container);
        toolbarTitleContainer.setOnClickListener(v -> {
            startActivityForResult(FeedGroupInfoActivity.viewGroup(this, groupId), REQUEST_CODE_VIEW_GROUP_INFO);
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_SHOW_INVITE_SHEET)) {
            showGroupInviteSheet = savedInstanceState.getBoolean(KEY_SHOW_INVITE_SHEET);
        } else {
            showGroupInviteSheet = getIntent().getBooleanExtra(EXTRA_SHOW_INVITE_BOTTOM_SHEET, false);
        }

        viewModel.group.getLiveData().observe(this, group -> {
            if (group != null) {
                titleView.setText(group.name);
                avatarLoader.load(avatarView, groupId, false);
                ContentDb.getInstance().setGroupSeen(group.groupId);
                if (showGroupInviteSheet) {
                    if (TextUtils.isEmpty(group.inviteToken)) {
                        Log.e("ViewGroupFeedActivity/no invite link token for group, not showing invite sheet!");
                    } else {
                        DialogFragmentUtils.showDialogFragmentOnce(InviteGroupBottomSheetDialogFragment.newInstance(group.inviteToken), getSupportFragmentManager());
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
        fabView.setMainFabIcon(R.drawable.ic_plus_expanded, R.string.new_post, R.string.post_fab_label);
        fabView.setOnFabClickListener(v -> {
            Intent intent = new Intent(this, ContentComposerActivity.class);
            intent.putExtra(ContentComposerActivity.EXTRA_GROUP_ID, groupId);
            startActivity(intent);
        });

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
        outState.putBoolean(KEY_SHOW_INVITE_SHEET, showGroupInviteSheet);
    }

    private void markUserInteracted() {
        userInteracted = true;
        setResult(RESULT_OK);
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.hasExtra(MediaExplorerActivity.EXTRA_CONTENT_ID) && data.hasExtra(MediaExplorerActivity.EXTRA_SELECTED)) {
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
                if (result == FeedGroupInfoActivity.RESULT_CODE_EXIT_CHAT) {
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
