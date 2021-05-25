package com.halloapp.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.halloapp.BuildConfig;
import com.halloapp.R;
import com.halloapp.content.PostThumbnailLoader;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.ActivityCenterActivity;
import com.halloapp.ui.FeedNuxBottomSheetDialogFragment;
import com.halloapp.ui.MainNavFragment;
import com.halloapp.ui.PostsFragment;
import com.halloapp.ui.invites.InviteContactsActivity;
import com.halloapp.util.DialogFragmentUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.widget.BadgedDrawable;
import com.halloapp.widget.NestedHorizontalScrollHelper;

import pub.devrel.easypermissions.EasyPermissions;

public class HomeFragment extends PostsFragment implements MainNavFragment {

    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION = 1;

    private HomeViewModel viewModel;
    private BadgedDrawable notificationDrawable;
    private PostThumbnailLoader postThumbnailLoader;

    private boolean scrollUpOnDataLoaded;

    private RecyclerView postsView;
    private View newPostsView;
    private FrameLayout welcomeNuxContainer;
    private FrameLayout nuxActivityCenterContainer;
    private FrameLayout nuxFeedContainer;
    private View nuxFeed;
    private View nuxActivityCenter;
    private View nuxWelcome;

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

        final View contactPermsNag = root.findViewById(R.id.contact_permissions_nag);
        final Button contactsContinue = contactPermsNag.findViewById(R.id.continue_btn);
        final View contactsNotNow = contactPermsNag.findViewById(R.id.not_now);
        final TextView contactsInfo = contactPermsNag.findViewById(R.id.contact_permissions_info);

        nuxActivityCenterContainer = root.findViewById(R.id.activity_center_nux_container);
        welcomeNuxContainer = root.findViewById(R.id.welcome_nux_container);
        nuxFeedContainer = root.findViewById(R.id.post_nux_container);

        newPostsView.setOnClickListener(v -> {
            scrollUpOnDataLoaded = true;
            viewModel.reloadPostsAt(Long.MAX_VALUE);
        });
        layoutManager = new LinearLayoutManager(getContext());
        postsView.setLayoutManager(layoutManager);
        postsView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);

        inviteView = root.findViewById(R.id.home_invite);
        inviteView.setOnClickListener(v -> startActivity(new Intent(requireContext(), InviteContactsActivity.class)));
        postsView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                refreshInviteNux();
            }
        });

        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        if (viewModel.getSavedScrollState() != null) {
            layoutManager.onRestoreInstanceState(viewModel.getSavedScrollState());
        }
        postsView.post(this::refreshInviteNux);
        viewModel.postList.observe(getViewLifecycleOwner(), posts -> adapter.submitList(posts, () -> {
            if (viewModel.checkPendingOutgoing() || scrollUpOnDataLoaded) {
                scrollUpOnDataLoaded = false;
                postsView.scrollToPosition(0);
                newPostsView.setVisibility(View.GONE);
            } else if (viewModel.checkPendingIncoming()) {
                final View childView = layoutManager.getChildAt(0);
                final boolean scrolled = childView == null || layoutManager.getPosition(childView) != 0;
                if (scrolled) {
                    if (newPostsView.getVisibility() != View.VISIBLE) {
                        newPostsView.setVisibility(View.VISIBLE);
                        newPostsView.setTranslationY(-getResources().getDimension(R.dimen.details_media_list_height));
                        newPostsView.animate().setDuration(200).translationY(0).start();
                    }
                } else {
                    scrollUpOnDataLoaded = false;
                    postsView.scrollToPosition(0);
                    newPostsView.setVisibility(View.GONE);
                }
            }
            emptyView.setVisibility(posts.size() == 0 ? View.VISIBLE : View.GONE);
        }));
        viewModel.socialHistory.getLiveData().observe(getViewLifecycleOwner(), commentHistoryData -> {
            if (notificationDrawable != null) {
                updateSocialHistory(commentHistoryData);
            }
        });
        if (!ServerProps.getInstance().getIsInternalUser()) {
            viewModel.showFeedNux.getLiveData().observe(getViewLifecycleOwner(), shouldShow -> {
                if (shouldShow == null) {
                    return;
                }
                if (shouldShow) {
                    if (nuxFeed == null) {
                        nuxFeed = LayoutInflater.from(requireContext()).inflate(R.layout.nux_home_feed, nuxFeedContainer, true);
                        View learnMore = nuxFeed.findViewById(R.id.learn_more);
                        View close = nuxFeed.findViewById(R.id.nux_close);
                        close.setOnClickListener(v -> {
                            viewModel.closeFeedNux();
                        });
                        learnMore.setOnClickListener(v -> {
                            DialogFragmentUtils.showDialogFragmentOnce(new FeedNuxBottomSheetDialogFragment(), getChildFragmentManager());
                        });
                    }
                    nuxFeedContainer.setVisibility(View.VISIBLE);
                    postsView.post(() -> {
                        postsView.setPadding(0, nuxFeedContainer.getHeight(), 0, postsView.getPaddingBottom());
                        postsView.scrollToPosition(0);
                    });
                } else {
                    TransitionManager.beginDelayedTransition((ViewGroup) root);
                    nuxFeedContainer.setVisibility(View.GONE);
                    if (nuxFeed == null) {
                        return;
                    }
                    postsView.setPadding(0, 0, 0, postsView.getPaddingBottom());
                }
            });

            viewModel.showActivityCenterNux.getLiveData().observe(getViewLifecycleOwner(), shouldShow -> {
                if (shouldShow == null) {
                    return;
                }
                if (shouldShow) {
                    if (nuxActivityCenter == null) {
                        nuxActivityCenter = LayoutInflater.from(requireContext()).inflate(R.layout.nux_activity_center, nuxActivityCenterContainer, true);
                    }
                } else {
                    nuxActivityCenterContainer.removeAllViews();
                    nuxActivityCenter = null;
                }
            });
        } else {
            viewModel.showWelcomeNux.getLiveData().observe(getViewLifecycleOwner(), shouldShow -> {
                if (shouldShow == null) {
                    return;
                }
                if (shouldShow) {
                    if (nuxWelcome == null) {
                        nuxWelcome = LayoutInflater.from(requireContext()).inflate(R.layout.nux_welcome, welcomeNuxContainer, true);
                        viewModel.markWelcomeNuxShown();
                    }
                } else {
                    welcomeNuxContainer.removeAllViews();
                    nuxWelcome = null;
                }
            });
        }

        viewModel.showContactsPermissionsNag().observe(getViewLifecycleOwner(), showNag -> {
            if (showNag == null || !showNag) {
                contactPermsNag.setVisibility(View.GONE);
            } else {
                contactPermsNag.setVisibility(View.VISIBLE);
            }
        });

        contactsNotNow.setOnClickListener(v -> {
            viewModel.hideContactsNag();
        });

        contactsContinue.setOnClickListener(v -> {
            viewModel.hideContactsNag();

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

        if (EasyPermissions.permissionPermanentlyDenied(requireActivity(), Manifest.permission.READ_CONTACTS)) {
            contactsInfo.setText(R.string.home_contact_permissions_nag_permanently_blocked);
            contactsContinue.setText(R.string.settings);
        } else {
            contactsInfo.setText(R.string.home_contact_permissions_nag);
            contactsContinue.setText(R.string.continue_button);
        }

        NestedHorizontalScrollHelper.applyDefaultScrollRatio(postsView);

        Preconditions.checkNotNull((SimpleItemAnimator) postsView.getItemAnimator()).setSupportsChangeAnimations(false);

        postsView.setAdapter(adapter);

        return root;
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
                    newPostsView.setVisibility(View.GONE);
                }
            }
        });
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

    @Override
    public void onDestroyView() {
        if (viewModel != null && layoutManager != null) {
            viewModel.saveScrollState(layoutManager.onSaveInstanceState());
        }
        super.onDestroyView();
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
            if (nuxActivityCenter != null) {
                viewModel.closeActivityCenterNux();
            }
            if (nuxWelcome != null) {
                welcomeNuxContainer.setVisibility(View.GONE);
                viewModel.closeWelcomeNux();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateSocialHistory(@Nullable HomeViewModel.SocialHistory socialHistory) {
        boolean hideBadge = socialHistory == null || socialHistory.unseenCount == 0;
        notificationDrawable.setBadge(hideBadge ? "" : "•");
        if (!hideBadge) {
            nuxActivityCenterContainer.setVisibility(View.VISIBLE);
        } else {
            nuxActivityCenterContainer.setVisibility(View.GONE);
        }
    }
}
