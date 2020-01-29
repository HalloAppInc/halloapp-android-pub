package com.halloapp.ui.profile;

import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Preconditions;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.ui.PostsFragment;
import com.halloapp.util.Log;

public class ProfileFragment extends PostsFragment {

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

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_profile, container, false);
        final RecyclerView postsView = root.findViewById(R.id.posts);
        final View emptyView = root.findViewById(android.R.id.empty);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        postsView.setLayoutManager(layoutManager);

        final ProfileViewModel viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        viewModel.postList.observe(this, posts -> adapter.submitList(posts, () -> {
            emptyView.setVisibility(posts.size() == 0 ? View.VISIBLE : View.GONE);
        }));

        final float scrolledElevation = getResources().getDimension(R.dimen.action_bar_elevation);
        postsView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                final View childView = layoutManager.getChildAt(0);
                final boolean scrolled = childView == null || !(childView.getTop() == 0 && layoutManager.getPosition(childView) == 0);
                final AppCompatActivity activity = Preconditions.checkNotNull((AppCompatActivity)getActivity());
                final ActionBar actionBar = Preconditions.checkNotNull(activity.getSupportActionBar());
                final float elevation = scrolled ? scrolledElevation : 0;
                if (actionBar.getElevation() != elevation) {
                    actionBar.setElevation(elevation);
                }
            }
        });

        Preconditions.checkNotNull((SimpleItemAnimator) postsView.getItemAnimator()).setSupportsChangeAnimations(false);

        final View headerView = getLayoutInflater().inflate(R.layout.profile_header, container, false);
        final TextView nameView = headerView.findViewById(R.id.name);
        nameView.setText(PhoneNumberUtils.formatNumber("+" + Preferences.getInstance(Preconditions.checkNotNull(getContext())).getUser(), null));
        adapter.addHeader(headerView);

        postsView.setAdapter(adapter);

        return root;
    }
}