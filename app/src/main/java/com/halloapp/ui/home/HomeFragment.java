package com.halloapp.ui.home;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.InitialPagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.halloapp.BuildConfig;
import com.halloapp.R;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.content.PostThumbnailLoader;
import com.halloapp.ui.ActivityCenterActivity;
import com.halloapp.ui.MainActivity;
import com.halloapp.ui.MainNavFragment;
import com.halloapp.ui.PostsFragment;
import com.halloapp.ui.contacts.ContactPermissionBottomSheetDialog;
import com.halloapp.ui.invites.InviteContactsActivity;
import com.halloapp.util.logs.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.widget.BadgedDrawable;
import com.halloapp.widget.NestedHorizontalScrollHelper;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class HomeFragment extends PostsFragment implements MainNavFragment, EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION = 1;

    private static final int NEW_POSTS_BANNER_DISAPPEAR_TIME_MS = 5000;

    private HomeViewModel viewModel;
    private BadgedDrawable notificationDrawable;
    private PostThumbnailLoader postThumbnailLoader;

    private boolean scrollUpOnDataLoaded;
    private boolean restoreStateOnDataLoaded;

    private RecyclerView postsView;
    private View newPostsView;
    private FrameLayout welcomeNuxContainer;
    private View nuxWelcome;

    private View contactsNag;
    private Button contactsSettingsButton;

    private View inviteView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = requireContext();
        postThumbnailLoader = new PostThumbnailLoader(context, context.getResources().getDimensionPixelSize(R.dimen.comment_history_thumbnail_size));
        Log.d("HomeFragment: onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("HomeFragment: onDestroy");
        postThumbnailLoader.destroy();
    }

    private LinearLayoutManager layoutManager;

    @Override
    public void resetScrollPosition() {
        layoutManager.scrollToPosition(0);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentViewGroup = container;

        setHasOptionsMenu(true);

        final View root = inflater.inflate(R.layout.fragment_home, container, false);
        postsView = root.findViewById(R.id.posts);
        final View emptyView = root.findViewById(android.R.id.empty);
        newPostsView = root.findViewById(R.id.new_posts);

        welcomeNuxContainer = root.findViewById(R.id.welcome_nux_container);

        newPostsView.setOnClickListener(v -> {
            scrollUpOnDataLoaded = true;
            viewModel.reloadPostsAt(Long.MAX_VALUE);
        });
        layoutManager = new LinearLayoutManager(getContext());
        postsView.setLayoutManager(layoutManager);
        postsView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);

        inviteView = root.findViewById(R.id.home_invite);
        inviteView.setOnClickListener(v -> openInviteFlow());
        postsView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                refreshInviteNux();
            }
        });

        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        viewModel.loadSavedState(savedInstanceState);

        if (viewModel.getSavedScrollState() != null) {
            restoreStateOnDataLoaded = true;
        }

        viewModel.unseenHomePosts.getLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean hasUnseen) {
                if (hasUnseen) {
                    showNewPostsBanner();
                }
                viewModel.unseenHomePosts.getLiveData().removeObserver(this);
            }
        });

        viewModel.postList.observe(getViewLifecycleOwner(), posts -> adapter.submitList(posts, () -> {
            if (posts instanceof InitialPagedList) {
                return;
            }
            Log.i("HomeFragment: post list updated " + posts);
            if (viewModel.checkPendingOutgoing() || scrollUpOnDataLoaded) {
                scrollUpOnDataLoaded = false;
                postsView.scrollToPosition(0);
                newPostsView.setVisibility(View.GONE);
                onScrollToTop();
            } else if (viewModel.checkPendingIncoming()) {
                final View childView = layoutManager.getChildAt(0);
                final boolean scrolled = childView == null || layoutManager.getPosition(childView) != 0;
                if (scrolled) {
                    showNewPostsBanner();
                } else {
                    scrollUpOnDataLoaded = false;
                    postsView.scrollToPosition(0);
                    newPostsView.setVisibility(View.GONE);
                    onScrollToTop();
                }
            }
            emptyView.setVisibility(posts.size() == 0 ? View.VISIBLE : View.GONE);
            if (restoreStateOnDataLoaded) {
                layoutManager.onRestoreInstanceState(viewModel.getSavedScrollState());
                restoreStateOnDataLoaded = false;
            }
            refreshInviteNux();
        }));
        viewModel.socialHistory.getLiveData().observe(getViewLifecycleOwner(), commentHistoryData -> {
            if (notificationDrawable != null) {
                updateSocialHistory(commentHistoryData);
            }
        });

        viewModel.showWelcomeNux.getLiveData().observe(getViewLifecycleOwner(), shouldShow -> {
            if (shouldShow == null) {
                return;
            }
            if (shouldShow) {
                if (nuxWelcome == null) {
                    nuxWelcome = LayoutInflater.from(requireContext()).inflate(R.layout.nux_welcome, welcomeNuxContainer, true);
                }
            } else {
                welcomeNuxContainer.removeAllViews();
                nuxWelcome = null;
            }
        });
        contactsNag = root.findViewById(R.id.contacts_nag);
        contactsNag.setOnClickListener(v -> {}); // Don't let touches pass through
        contactsSettingsButton = contactsNag.findViewById(R.id.settings_btn);
        contactsSettingsButton.setOnClickListener(v -> {
            if (EasyPermissions.permissionPermanentlyDenied(requireActivity(), Manifest.permission.READ_CONTACTS)) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                startActivity(intent);
            } else {
                // You can directly ask for the permission.
                final String[] perms = {Manifest.permission.READ_CONTACTS};
                requestPermissions(perms, REQUEST_CODE_ASK_CONTACTS_PERMISSION);
            }
        });

        updateContactsNag();

        NestedHorizontalScrollHelper.applyDefaultScrollRatio(postsView);

        Preconditions.checkNotNull((SimpleItemAnimator) postsView.getItemAnimator()).setSupportsChangeAnimations(false);

        postsView.setAdapter(adapter);

        return root;
    }

    private void onScrollToTop() {
        viewModel.onScrollToTop();
        newPostsView.setVisibility(View.GONE);
    }

    private void updateContactsNag() {
        if (EasyPermissions.hasPermissions(requireContext(), Manifest.permission.READ_CONTACTS)) {
            contactsNag.setVisibility(View.GONE);
        } else {
            contactsNag.setVisibility(View.VISIBLE);
            if (EasyPermissions.permissionPermanentlyDenied(this, Manifest.permission.READ_CONTACTS)) {
                contactsSettingsButton.setText(R.string.go_to_settings);
            } else {
                contactsSettingsButton.setText(R.string.continue_button);
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        postsView.addOnScrollListener(new ActionBarShadowOnScrollListener((AppCompatActivity) requireActivity()) {
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                final RecyclerView.LayoutManager layoutManager = Preconditions.checkNotNull(recyclerView.getLayoutManager());
                final View childView = layoutManager.getChildAt(0);
                if (childView != null && layoutManager.getPosition(childView) == 0) {
                    onScrollToTop();
                }
            }
        });
    }

    private void showNewPostsBanner() {
        if (newPostsView.getVisibility() != View.VISIBLE) {
            newPostsView.setVisibility(View.VISIBLE);
            final float initialTranslation = -getResources().getDimension(R.dimen.details_media_list_height);
            newPostsView.setTranslationY(initialTranslation);
            newPostsView.animate().setDuration(200).translationY(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    newPostsView.setTranslationY(0);
                }
            }).start();
        }
        newPostsView.removeCallbacks(this::hideNewPostsBanner);
        newPostsView.postDelayed(this::hideNewPostsBanner, NEW_POSTS_BANNER_DISAPPEAR_TIME_MS);
    }

    private void hideNewPostsBanner() {
        Context context = getContext();
        if (context == null) {
            return;
        }
        final float initialTranslation = -context.getResources().getDimension(R.dimen.details_media_list_height);
        newPostsView.animate().setDuration(200).translationY(initialTranslation).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                newPostsView.setVisibility(View.GONE);
            }
        }).start();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (viewModel != null && layoutManager != null) {
            viewModel.saveInstanceState(outState);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        saveScrollState();
    }

    private void refreshInviteNux() {
        int lastItem = adapter.getItemCount() - 1;
        if (lastItem == layoutManager.findLastVisibleItemPosition()) {
            View v = layoutManager.findViewByPosition(lastItem);
            if (v != null && v.getBottom() > inviteView.getTop()) {
                int dist = v.getBottom() - inviteView.getTop();
                int height = inviteView.getHeight() / 2;
                if (dist > height) {
                    inviteView.setVisibility(View.INVISIBLE);
                } else {
                    float alpha = 1.0f - ((float) dist / (float) height);
                    inviteView.setVisibility(View.VISIBLE);
                    inviteView.setAlpha(alpha);
                }
            } else {
                inviteView.setAlpha(1.0f);
                inviteView.setVisibility(View.VISIBLE);
            }
        } else {
            inviteView.setVisibility(View.INVISIBLE);
        }
    }

    private void saveScrollState() {
        if (viewModel != null && layoutManager != null) {
            viewModel.saveScrollState(layoutManager.onSaveInstanceState());
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.home_menu, menu);
        final MenuItem notificationsMenuItem = menu.findItem(R.id.notifications);
        notificationDrawable = new BadgedDrawable(
                requireContext(),
                notificationsMenuItem.getIcon(),
                getResources().getColor(R.color.badge_text),
                getResources().getColor(R.color.badge_background),
                getResources().getColor(R.color.window_background),
                getResources().getDimension(R.dimen.badge));
        updateSocialHistory(viewModel.socialHistory.getLiveData().getValue());
        notificationsMenuItem.setIcon(notificationDrawable);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.notifications) {
            startActivity(new Intent(requireContext(), ActivityCenterActivity.class));
            if (nuxWelcome != null) {
                welcomeNuxContainer.setVisibility(View.GONE);
                viewModel.closeWelcomeNux();
            }
            return true;
        } else if (item.getItemId() == R.id.invite) {
            openInviteFlow();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openInviteFlow() {
        if (EasyPermissions.hasPermissions(requireContext(), Manifest.permission.READ_CONTACTS)) {
            startActivity(new Intent(requireContext(), InviteContactsActivity.class));
        } else {
            ContactPermissionBottomSheetDialog.showRequest(requireActivity().getSupportFragmentManager(), MainActivity.REQUEST_CODE_ASK_CONTACTS_PERMISSION_INVITE);
        }
    }

    private void updateSocialHistory(@Nullable HomeViewModel.SocialHistory socialHistory) {
        boolean hideBadge = socialHistory == null || socialHistory.unseenCount == 0;
        notificationDrawable.setBadge(hideBadge ? "" : "•");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_CONTACTS_PERMISSION: {
                ContactsSync.getInstance(requireContext()).startAddressBookListener();
                ContactsSync.getInstance(requireContext()).startContactsSync(true);
                updateContactsNag();
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).refreshFab();
                }
                break;
            }
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        updateContactsNag();
    }
}
