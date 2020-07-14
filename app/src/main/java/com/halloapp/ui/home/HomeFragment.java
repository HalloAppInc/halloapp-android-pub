package com.halloapp.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.halloapp.R;
import com.halloapp.content.PostThumbnailLoader;
import com.halloapp.ui.CommentsActivity;
import com.halloapp.ui.SocialHistoryPopup;
import com.halloapp.ui.PostsFragment;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.widget.BadgedDrawable;

public class HomeFragment extends PostsFragment {

    private HomeViewModel viewModel;
    private BadgedDrawable notificationDrawable;
    private SocialHistoryPopup socialHistoryPopup;
    private PostThumbnailLoader postThumbnailLoader;

    private boolean scrollUpOnDataLoaded;

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        final View root = inflater.inflate(R.layout.fragment_home, container, false);
        final RecyclerView postsView = root.findViewById(R.id.posts);
        final View emptyView = root.findViewById(android.R.id.empty);
        final View newPostsView = root.findViewById(R.id.new_posts);

        newPostsView.setOnClickListener(v -> {
            scrollUpOnDataLoaded = true;
            viewModel.reloadPostsAt(Long.MAX_VALUE);
        });

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        postsView.setLayoutManager(layoutManager);
        postsView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
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

        Preconditions.checkNotNull((SimpleItemAnimator) postsView.getItemAnimator()).setSupportsChangeAnimations(false);

        postsView.setAdapter(adapter);

        socialHistoryPopup = new SocialHistoryPopup(requireContext(), postThumbnailLoader, root.findViewById(R.id.popup_anchor));
        socialHistoryPopup.setOnItemClickListener(commentsGroup -> {
            socialHistoryPopup.dismiss();
            final HomeViewModel.SocialHistory commentHistoryData = viewModel.socialHistory.getLiveData().getValue();
            if (commentHistoryData != null) {
                final Intent intent = new Intent(getContext(), CommentsActivity.class);
                intent.putExtra(CommentsActivity.EXTRA_POST_SENDER_USER_ID, commentsGroup.postSenderUserId.rawId());
                intent.putExtra(CommentsActivity.EXTRA_POST_ID, commentsGroup.postId);
                intent.putExtra(CommentsActivity.EXTRA_SHOW_KEYBOARD, false);
                startActivity(intent);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        socialHistoryPopup.destroy();
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
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case R.id.notifications: {
                if (!socialHistoryPopup.isShowing() && getView() != null) {
                    socialHistoryPopup.show(getView().getHeight() * 9 / 10);
                }
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void updateSocialHistory(@Nullable HomeViewModel.SocialHistory socialHistory) {
        notificationDrawable.setBadge((socialHistory == null || socialHistory.unseenCount == 0) ? "" : "•");
        socialHistoryPopup.setSocialHistory(socialHistory);
    }
}
