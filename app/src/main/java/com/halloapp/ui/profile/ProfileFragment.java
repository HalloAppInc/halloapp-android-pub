package com.halloapp.ui.profile;

import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.halloapp.widget.ActionBarShadowProvider;

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

        postsView.addOnScrollListener(new ActionBarShadowProvider((AppCompatActivity) Preconditions.checkNotNull(getActivity())));

        Preconditions.checkNotNull((SimpleItemAnimator) postsView.getItemAnimator()).setSupportsChangeAnimations(false);

        final View headerView = getLayoutInflater().inflate(R.layout.profile_header, container, false);
        final TextView nameView = headerView.findViewById(R.id.name);
        nameView.setText(PhoneNumberUtils.formatNumber("+" + Preferences.getInstance(Preconditions.checkNotNull(getContext())).getUser(), null));
        adapter.addHeader(headerView);

        postsView.setAdapter(adapter);

        return root;
    }
}