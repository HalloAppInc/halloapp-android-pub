package com.halloapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.MainActivity;
import com.halloapp.R;
import com.halloapp.id.UserId;
import com.halloapp.permissions.PermissionUtils;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.contacts.EditFavoritesActivity;
import com.halloapp.ui.contacts.ViewFriendsListActivity;
import com.halloapp.ui.invites.InviteContactsActivity;
import com.halloapp.ui.settings.SettingsActivity;
import com.halloapp.ui.settings.SettingsProfile;
import com.halloapp.widget.ActionBarShadowOnScrollListener;

public class MyProfileHomeFragment extends HalloFragment implements MainNavFragment {

    private MyProfileViewModel viewModel;

    private AvatarLoader avatarLoader;

    private ImageView avatarView;
    private NestedScrollView scrollView;

    @Override
    public void resetScrollPosition() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(MyProfileViewModel.class);

        avatarLoader = AvatarLoader.getInstance();
        View root = inflater.inflate(R.layout.fragment_my_profile_home, container, false);
        scrollView = root.findViewById(R.id.container);

        View profileContainer = root.findViewById(R.id.profile_container);
        profileContainer.setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), SettingsProfile.class));
        });

        View about = root.findViewById(R.id.about);
        about.setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), AboutActivity.class));
        });
        View myPosts = root.findViewById(R.id.my_posts);
        myPosts.setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), ViewMyPostsActivity.class));
        });

        View help = root.findViewById(R.id.help);
        help.setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), HelpActivity.class));
        });

        View myFriends = root.findViewById(R.id.my_friends);
        myFriends.setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), ViewFriendsListActivity.class));
        });

        View favorites = root.findViewById(R.id.favorites);
        favorites.setOnClickListener(v -> {
            startActivity(EditFavoritesActivity.openFavorites(v.getContext()));
        });

        View invite = root.findViewById(R.id.invite);
        invite.setOnClickListener(v -> {
            if (PermissionUtils.hasOrRequestContactPermissions(requireActivity(), MainActivity.REQUEST_CODE_ASK_CONTACTS_PERMISSION_INVITE)) {
                startActivity(new Intent(requireContext(), InviteContactsActivity.class));
            }
        });

        View settings = root.findViewById(R.id.settings);
        settings.setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), SettingsActivity.class));
        });

        TextView username = root.findViewById(R.id.username);
        TextView name = root.findViewById(R.id.name);

        avatarView = root.findViewById(R.id.avatar);

        viewModel.getName().observe(getViewLifecycleOwner(), name::setText);
        viewModel.getUsername().observe(getViewLifecycleOwner(), username::setText);

        avatarLoader.load(avatarView, UserId.ME, false);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ActionBarShadowOnScrollListener scrollListener = new ActionBarShadowOnScrollListener((AppCompatActivity) requireActivity());
        scrollListener.resetElevation();

        scrollView.setOnScrollChangeListener(scrollListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        avatarLoader.load(avatarView, UserId.ME, false);
    }
}
