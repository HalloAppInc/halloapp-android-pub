package com.halloapp.ui.profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.content.Message;
import com.halloapp.id.UserId;
import com.halloapp.ui.PostsFragment;
import com.halloapp.ui.settings.SettingsActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.settings.SettingsProfile;
import com.halloapp.util.logs.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.widget.NestedHorizontalScrollHelper;
import com.halloapp.widget.SnackbarHelper;

public class ProfileFragment extends PostsFragment {

    private static final String ARG_SELECTED_PROFILE_USER_ID = "view_user_id";

    private final Me me = Me.getInstance();
    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();

    private ImageView avatarView;
    private TextView nameView;
    private TextView subtitleView;

    private MenuItem blockMenuItem;

    private UserId profileUserId;

    protected LinearLayoutManager layoutManager;

    private ProfileViewModel viewModel;

    public static ProfileFragment newProfileFragment(@NonNull UserId userId) {
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SELECTED_PROFILE_USER_ID, userId.rawId());
        profileFragment.setArguments(args);
        return profileFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ProfileFragment: onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ProfileFragment: onDestroy");
    }

    @Override
    public void onDestroyView() {
        if (viewModel != null && layoutManager != null) {
            viewModel.saveScrollState(layoutManager.onSaveInstanceState());
        }
        super.onDestroyView();
    }

    @LayoutRes
    protected int getLayout() {
        return R.layout.fragment_profile;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        final View root = inflater.inflate(getLayout(), container, false);
        final RecyclerView postsView = root.findViewById(R.id.posts);
        final TextView emptyView = root.findViewById(R.id.empty_profile_text);
        final View emptyContainer = root.findViewById(android.R.id.empty);

        layoutManager = new LinearLayoutManager(getContext());
        postsView.setLayoutManager(layoutManager);
        postsView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
        NestedHorizontalScrollHelper.applyDefaultScrollRatio(postsView);

        profileUserId = UserId.ME;
        Bundle args = getArguments();
        if (args != null) {
            String extraUserId = args.getString(ARG_SELECTED_PROFILE_USER_ID);
            if (extraUserId != null) {
                profileUserId = new UserId(extraUserId);
            }
        }

        viewModel = new ViewModelProvider(requireActivity(), new ProfileViewModel.Factory(requireActivity().getApplication(), profileUserId)).get(ProfileViewModel.class);
        viewModel.postList.observe(getViewLifecycleOwner(), posts -> adapter.submitList(posts, () -> emptyContainer.setVisibility(posts.size() == 0 ? View.VISIBLE : View.GONE)));
        if (viewModel.getSavedScrollState() != null) {
            layoutManager.onRestoreInstanceState(viewModel.getSavedScrollState());
        }
        postsView.addOnScrollListener(new ActionBarShadowOnScrollListener((AppCompatActivity) requireActivity()));

        Preconditions.checkNotNull((SimpleItemAnimator) postsView.getItemAnimator()).setSupportsChangeAnimations(false);

        final View headerView = getLayoutInflater().inflate(R.layout.profile_header, container, false);
        subtitleView = headerView.findViewById(R.id.subtitle);
        nameView = headerView.findViewById(R.id.name);
        viewModel.getSubtitle().observe(getViewLifecycleOwner(), s -> {
            subtitleView.setText(s);
            if (s == null) {
                subtitleView.setVisibility(View.GONE);
            } else {
                subtitleView.setVisibility(View.VISIBLE);
            }
        });
        if (profileUserId.isMe()) {
            me.name.observe(getViewLifecycleOwner(), nameView::setText);
        } else {
            viewModel.getContact().observe(getViewLifecycleOwner(), contact -> {
                String name = contact.getDisplayName();
                nameView.setText(name);
                if (contact.addressBookName == null) {
                    emptyView.setText(getString(R.string.contact_profile_not_friends, name));
                } else {
                    emptyView.setText(getString(R.string.contact_profile_empty, name));
                }
            });
        }

        viewModel.getIsBlocked().observe(getViewLifecycleOwner(), this::updateMenu);

        avatarView = headerView.findViewById(R.id.avatar);
        avatarLoader.load(avatarView, profileUserId, false);

        if (profileUserId.isMe()) {
            final View.OnClickListener editProfileClickListener = v -> {
                openProfileEditor();;
            };
            avatarView.setOnClickListener(editProfileClickListener);
            nameView.setOnClickListener(editProfileClickListener);
        }

        adapter.addHeader(headerView);

        postsView.setAdapter(adapter);

        return root;
    }

    private void openProfileEditor() {
        startActivity(new Intent(requireContext(), SettingsProfile.class));
    }

    @Override
    public void onResume() {
        super.onResume();
        avatarLoader.load(avatarView, profileUserId, false);
        adapter.notifyDataSetChanged();
    }

    private void updateMenu(Boolean isBlocked) {
        if (blockMenuItem != null) {
            if (isBlocked == null || !isBlocked) {
                blockMenuItem.setTitle(R.string.block);
            } else {
                blockMenuItem.setTitle(R.string.unblock);
            }
        }
    }

    @Override
    protected boolean shouldOpenProfileOnNamePress() {
        return false;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (!profileUserId.isMe()) {
            inflater.inflate(R.menu.other_profile_menu, menu);
            blockMenuItem = menu.findItem(R.id.block);
            updateMenu(viewModel.getIsBlocked().getValue());
        }
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings: {
                startActivity(new Intent(getContext(), SettingsActivity.class));
                return true;
            }
            case R.id.block: {
                Boolean isBlocked = viewModel.getIsBlocked().getValue();
                if (isBlocked == null || !isBlocked) {
                    blockContact();
                } else {
                    unBlockContact();
                }
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void blockContact() {
        String chatName = nameView.getText().toString();
        ProgressDialog blockDialog = ProgressDialog.show(requireContext(), null, getString(R.string.blocking_user_in_progress, chatName), true);
        blockDialog.show();

        viewModel.blockContact(profileUserId).observe(this, success -> {
            if (success == null) {
                return;
            }
            blockDialog.cancel();
            if (success) {
                SnackbarHelper.showInfo(nameView, getString(R.string.blocking_user_successful, chatName));
                blockMenuItem.setTitle(getString(R.string.unblock));
                viewModel.sendSystemMessage(Message.USAGE_BLOCK, profileUserId);
            } else {
                SnackbarHelper.showWarning(nameView, getString(R.string.blocking_user_failed_check_internet, chatName));
            }
        });
    }

    private void unBlockContact() {
        String chatName = nameView.getText().toString();
        ProgressDialog unblockDialog = ProgressDialog.show(requireContext(), null, getString(R.string.unblocking_user_in_progress, chatName), true);
        unblockDialog.show();
        viewModel.unblockContact(new UserId(profileUserId.rawId())).observe(this, success -> {
            if (success == null) {
                return;
            }
            unblockDialog.cancel();
            if (success) {
                SnackbarHelper.showInfo(nameView, getString(R.string.unblocking_user_successful, chatName));
                blockMenuItem.setTitle(getString(R.string.block));
                viewModel.sendSystemMessage(Message.USAGE_UNBLOCK, profileUserId);
            } else {
                SnackbarHelper.showWarning(nameView, getString(R.string.unblocking_user_failed_check_internet, chatName));
            }
        });
    }
}
