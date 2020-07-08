package com.halloapp.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.BidiFormatter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.halloapp.R;
import com.halloapp.contacts.UserId;
import com.halloapp.ui.PostsFragment;
import com.halloapp.ui.SettingsActivity;
import com.halloapp.ui.UserNameActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.ActionBarShadowOnScrollListener;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends PostsFragment {

    private static final int CODE_CHANGE_AVATAR = 1;
    private static final String ARG_SELECTED_PROFILE_USER_ID = "view_user_id";

    private ImageView avatarView;

    private UserId profileUserId;

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

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        final View root = inflater.inflate(R.layout.fragment_profile, container, false);
        final RecyclerView postsView = root.findViewById(R.id.posts);
        final View emptyView = root.findViewById(android.R.id.empty);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        postsView.setLayoutManager(layoutManager);
        postsView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);

        profileUserId = UserId.ME;
        Bundle args = getArguments();
        if (args != null) {
            String extraUserId = args.getString(ARG_SELECTED_PROFILE_USER_ID);
            if (extraUserId != null) {
                profileUserId = new UserId(extraUserId);
            }
        }

        final ProfileViewModel viewModel = new ViewModelProvider(this, new ProfileViewModel.Factory(requireActivity().getApplication(), profileUserId)).get(ProfileViewModel.class);
        viewModel.postList.observe(getViewLifecycleOwner(), posts -> adapter.submitList(posts, () -> emptyView.setVisibility(posts.size() == 0 ? View.VISIBLE : View.GONE)));

        postsView.addOnScrollListener(new ActionBarShadowOnScrollListener((AppCompatActivity) requireActivity()));

        Preconditions.checkNotNull((SimpleItemAnimator) postsView.getItemAnimator()).setSupportsChangeAnimations(false);

        final View headerView = getLayoutInflater().inflate(R.layout.profile_header, container, false);
        final TextView nameView = headerView.findViewById(R.id.name);
        final TextView phoneView = headerView.findViewById(R.id.phone);
        final ImageView changeAvatarView = headerView.findViewById(R.id.change_avatar);
        final View changeNameView = headerView.findViewById(R.id.change_name);
        viewModel.getPhone().observe(getViewLifecycleOwner(), phone -> {
            if (phone == null) {
                phoneView.setVisibility(View.GONE);
            } else {
                phoneView.setVisibility(View.VISIBLE);
                phoneView.setText(
                        BidiFormatter.getInstance().unicodeWrap(PhoneNumberUtils.formatNumber("+" + phone, null)));
            }
        });
        viewModel.getName().observe(getViewLifecycleOwner(), nameView::setText);

        avatarView = headerView.findViewById(R.id.avatar);
        AvatarLoader.getInstance(requireContext()).load(avatarView, profileUserId);

        if (profileUserId.isMe()) {
            final View.OnClickListener changeAvatarListener = v -> {
                Log.d("ProfileFragment request change avatar");
                final Intent intent = new Intent(getContext(), MediaPickerActivity.class);
                intent.putExtra(MediaPickerActivity.EXTRA_PICKER_PURPOSE, MediaPickerActivity.PICKER_PURPOSE_AVATAR);
                startActivityForResult(intent, CODE_CHANGE_AVATAR);
            };
            changeAvatarView.setOnClickListener(changeAvatarListener);
            avatarView.setOnClickListener(changeAvatarListener);

            final View.OnClickListener changeNameListener = v -> startActivity(new Intent(getContext(), UserNameActivity.class));
            headerView.findViewById(R.id.name).setOnClickListener(changeNameListener);
            changeNameView.setOnClickListener(changeNameListener);
        } else {
            changeAvatarView.setVisibility(View.GONE);
            changeNameView.setVisibility(View.GONE);
            phoneView.setVisibility(View.GONE);
        }

        adapter.addHeader(headerView);

        postsView.setAdapter(adapter);

        return root;
    }

    @Override
    protected boolean shouldOpenProfileOnNamePress() {
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_CHANGE_AVATAR && resultCode == RESULT_OK) {
            AvatarLoader.getInstance(getContext()).load(avatarView, UserId.ME);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (profileUserId.isMe()) {
            inflater.inflate(R.menu.profile_menu, menu);
        }
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case R.id.settings: {
                startActivity(new Intent(getContext(), SettingsActivity.class));
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

}
