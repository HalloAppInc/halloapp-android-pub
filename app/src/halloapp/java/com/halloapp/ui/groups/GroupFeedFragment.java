package com.halloapp.ui.groups;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.halloapp.R;
import com.halloapp.content.Post;
import com.halloapp.id.GroupId;
import com.halloapp.media.VoiceNotePlayer;
import com.halloapp.ui.PostsFragment;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.widget.FabExpandOnScrollListener;
import com.halloapp.widget.NestedHorizontalScrollHelper;

public class GroupFeedFragment extends PostsFragment {
    private static final String ARG_GROUP_ID = "group_id";
    private static final String ARG_TARGET_POST_TIMESTAMP = "target_post_timestamp";

    private GroupId groupId;

    protected LinearLayoutManager layoutManager;

    private GroupFeedViewModel viewModel;

    private RecyclerView postsView;

    private Long scrollToTimestamp = null;

    public static GroupFeedFragment newInstance(@NonNull GroupId groupId) {
        return newInstance(groupId, null);
    }

    public static GroupFeedFragment newInstance(@NonNull GroupId groupId, @Nullable Long targetTimestamp) {
        GroupFeedFragment feedFragment = new GroupFeedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId.rawId());
        if (targetTimestamp != null) {
            args.putLong(ARG_TARGET_POST_TIMESTAMP, targetTimestamp);
        }
        feedFragment.setArguments(args);
        return feedFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("GroupFeedFragment: onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("GroupFeedFragment: onDestroy");
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
        return R.layout.fragment_group_feed;
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return postsView;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentViewGroup = container;

        setHasOptionsMenu(true);

        final View root = inflater.inflate(getLayout(), container, false);
        postsView = root.findViewById(R.id.posts);
        final View emptyContainer = root.findViewById(android.R.id.empty);

        layoutManager = new LinearLayoutManager(getContext());
        postsView.setLayoutManager(layoutManager);
        postsView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
        postsView.addOnScrollListener(new FabExpandOnScrollListener((AppCompatActivity) requireActivity()));
        NestedHorizontalScrollHelper.applyDefaultScrollRatio(postsView);

        Bundle args = getArguments();
        if (args != null) {
            String extraUserId = args.getString(ARG_GROUP_ID);
            if (extraUserId != null) {
                groupId = GroupId.fromNullable(extraUserId);
            }
            if (args.containsKey(ARG_TARGET_POST_TIMESTAMP)) {
                scrollToTimestamp = args.getLong(ARG_TARGET_POST_TIMESTAMP);
            }
        }

        if (groupId == null) {
            throw new IllegalArgumentException("You must specify a group id for a group feed fragment");
        }
        viewModel = new ViewModelProvider(requireActivity(), new GroupFeedViewModel.Factory(groupId)).get(GroupFeedViewModel.class);
        viewModel.postList.observe(getViewLifecycleOwner(), posts -> adapter.submitList(posts, () -> {
            if (viewModel.checkLoadedOutgoingPost()) {
                postsView.scrollToPosition(0);
            }
            if (scrollToTimestamp != null) {
                int index = -1;
                for (int i = 0; i < posts.size(); i++) {
                    Post post = posts.get(i);
                    if (post != null && post.timestamp == scrollToTimestamp) {
                        index = i;
                        break;
                    }
                }
                if (index == -1) {
                    viewModel.reloadPostsAt(scrollToTimestamp);
                    scrollToTimestamp = null;
                } else {
                    postsView.scrollToPosition(index + adapter.getHeaderCount());
                }
            }
            emptyContainer.setVisibility(posts.size() == 0 ? View.VISIBLE : View.GONE);
        }));
        if (viewModel.getSavedScrollState() != null) {
            layoutManager.onRestoreInstanceState(viewModel.getSavedScrollState());
        }

        Preconditions.checkNotNull((SimpleItemAnimator) postsView.getItemAnimator()).setSupportsChangeAnimations(false);

        viewModel.group.getLiveData().observe(getViewLifecycleOwner(), chat -> {
            if (chat == null) {
                return;
            }
            GroupTheme theme = GroupTheme.getTheme(chat.theme);
            root.setBackgroundColor(ContextCompat.getColor(requireContext(), theme.bgColor));
            adapter.applyTheme(chat.theme);
        });

        adapter.setShowGroup(false);
        postsView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        postsView.addOnScrollListener(new ActionBarShadowOnScrollListener((AppCompatActivity) requireActivity()));
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    protected boolean shouldOpenProfileOnNamePress() {
        return true;
    }

    @Override
    protected VoiceNotePlayer getVoiceNotePlayer() {
        return viewModel.getVoiceNotePlayer();
    }
}
