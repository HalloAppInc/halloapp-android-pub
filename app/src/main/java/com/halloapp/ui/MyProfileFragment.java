package com.halloapp.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.R;
import com.halloapp.id.UserId;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.invites.InviteFriendsActivity;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.ui.settings.SettingsActivity;
import com.halloapp.ui.settings.SettingsProfile;
import com.halloapp.util.StringUtils;
import com.halloapp.widget.ActionBarShadowOnScrollListener;

public class MyProfileFragment extends HalloFragment implements MainNavFragment {

    private MyProfileViewModel viewModel;

    private AvatarLoader avatarLoader = AvatarLoader.getInstance();

    private ImageView avatarView;

    @Override
    public void resetScrollPosition() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(MyProfileViewModel.class);

        View root = inflater.inflate(R.layout.fragment_my_profile, container, false);

        NestedScrollView scrollView = root.findViewById(R.id.container);
        scrollView.setOnScrollChangeListener(new ActionBarShadowOnScrollListener((AppCompatActivity) requireActivity()));

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
            startActivity(ViewProfileActivity.viewProfile(v.getContext(), UserId.ME));
        });

        View settings = root.findViewById(R.id.settings);
        settings.setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), SettingsActivity.class));
        });

        View help = root.findViewById(R.id.help);
        help.setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), HelpActivity.class));
        });

        View invite = root.findViewById(R.id.invite);
        invite.setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), InviteFriendsActivity.class));
        });

        TextView number = root.findViewById(R.id.number);
        TextView name = root.findViewById(R.id.name);

        avatarView = root.findViewById(R.id.avatar);

        viewModel.getName().observe(getViewLifecycleOwner(), name::setText);
        viewModel.getPhone().observe(getViewLifecycleOwner(), phoneNumber -> {
            number.setText(StringUtils.formatPhoneNumber(phoneNumber));
        });

        avatarLoader.load(avatarView, UserId.ME, false);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        avatarLoader.load(avatarView, UserId.ME, false);
    }
}
