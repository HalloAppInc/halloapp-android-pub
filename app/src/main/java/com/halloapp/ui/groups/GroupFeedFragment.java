package com.halloapp.ui.groups;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.halloapp.R;
import com.halloapp.id.GroupId;
import com.halloapp.ui.PostsFragment;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.widget.NestedHorizontalScrollHelper;

public class GroupFeedFragment extends PostsFragment {
    private static final String ARG_GROUP_ID = "group_id";

    private GroupId groupId;

    protected LinearLayoutManager layoutManager;

    private AvatarLoader avatarLoader = AvatarLoader.getInstance();

    private GroupFeedViewModel viewModel;

    public static GroupFeedFragment newInstance(@NonNull GroupId groupId) {
        GroupFeedFragment feedFragment = new GroupFeedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId.rawId());
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

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        final View root = inflater.inflate(getLayout(), container, false);
        final RecyclerView postsView = root.findViewById(R.id.posts);
        final View emptyContainer = root.findViewById(android.R.id.empty);

        layoutManager = new LinearLayoutManager(getContext());
        postsView.setLayoutManager(layoutManager);
        postsView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
        NestedHorizontalScrollHelper.applyDefaultScrollRatio(postsView);

        Bundle args = getArguments();
        if (args != null) {
            String extraUserId = args.getString(ARG_GROUP_ID);
            if (extraUserId != null) {
                groupId = GroupId.fromNullable(extraUserId);
            }
        }

        if (groupId == null) {
            throw new IllegalArgumentException("You must specify a group id for a group feed fragment");
        }
        viewModel = new ViewModelProvider(requireActivity(), new GroupFeedViewModel.Factory(groupId)).get(GroupFeedViewModel.class);
        viewModel.postList.observe(getViewLifecycleOwner(), posts -> adapter.submitList(posts, () -> emptyContainer.setVisibility(posts.size() == 0 ? View.VISIBLE : View.GONE)));
        if (viewModel.getSavedScrollState() != null) {
            layoutManager.onRestoreInstanceState(viewModel.getSavedScrollState());
        }
        postsView.addOnScrollListener(new ActionBarShadowOnScrollListener((AppCompatActivity) requireActivity()));

        Preconditions.checkNotNull((SimpleItemAnimator) postsView.getItemAnimator()).setSupportsChangeAnimations(false);

        final View headerView = getLayoutInflater().inflate(R.layout.profile_header, container, false);
        TextView subtitleView = headerView.findViewById(R.id.subtitle);
        TextView nameView = headerView.findViewById(R.id.name);
        viewModel.chat.getLiveData().observe(getViewLifecycleOwner(), chat -> {
            if (chat != null) {
                nameView.setText(chat.name);
            } else {
                nameView.setText(null);
            }
        });
        viewModel.members.getLiveData().observe(getViewLifecycleOwner(), memberList -> {
            if (memberList == null) {
                subtitleView.setVisibility(View.GONE);
            } else {
                subtitleView.setVisibility(View.VISIBLE);
                subtitleView.setText(getResources().getQuantityString(R.plurals.group_feed_members, memberList.size(), memberList.size()));
            }
        });
        ImageView avatarView = headerView.findViewById(R.id.avatar);

        avatarLoader.load(avatarView, groupId, false);

        adapter.addHeader(headerView);

        postsView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    protected boolean shouldOpenProfileOnNamePress() {
        return false;
    }
}
