package com.halloapp.ui;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
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

import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.id.UserId;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.archive.ArchiveActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.contacts.ContactPermissionBottomSheetDialog;
import com.halloapp.ui.invites.InviteContactsActivity;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.ui.settings.SettingsNotifications;
import com.halloapp.ui.settings.SettingsPrivacy;
import com.halloapp.ui.settings.SettingsProfile;
import com.halloapp.util.StringUtils;
import com.halloapp.widget.ActionBarShadowOnScrollListener;

import pub.devrel.easypermissions.EasyPermissions;

public class SettingsHomeFragment extends HalloFragment implements MainNavFragment {

    private MyProfileViewModel viewModel;

    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();

    private ImageView avatarView;
    private NestedScrollView scrollView;

    @Override
    public void resetScrollPosition() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(MyProfileViewModel.class);

        View root = inflater.inflate(R.layout.fragment_settings_home, container, false);
        scrollView = root.findViewById(R.id.container);

        View profileContainer = root.findViewById(R.id.profile_container);
        profileContainer.setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), SettingsProfile.class));
        });

        View about = root.findViewById(R.id.about);
        about.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.ABOUT_PAGE_URL));
            startActivity(intent);
        });
        View myPosts = root.findViewById(R.id.my_posts);
        myPosts.setOnClickListener(v -> {
            startActivity(ViewProfileActivity.viewProfile(v.getContext(), UserId.ME));
        });
        View archivedPosts = root.findViewById(R.id.archived_posts);
        archivedPosts.setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), ArchiveActivity.class));
        });
        archivedPosts.setVisibility(ServerProps.getInstance().getIsInternalUser() ? View.VISIBLE : View.GONE);


        View help = root.findViewById(R.id.help);
        help.setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), HelpActivity.class));
        });

        View invite = root.findViewById(R.id.invite);
        invite.setOnClickListener(v -> {
            final String[] perms = {Manifest.permission.READ_CONTACTS};
            if (!EasyPermissions.hasPermissions(requireContext(), perms)) {
                ContactPermissionBottomSheetDialog.showRequest(requireActivity().getSupportFragmentManager(), MainActivity.REQUEST_CODE_ASK_CONTACTS_PERMISSION_INVITE);
            } else {
                startActivity(new Intent(requireContext(), InviteContactsActivity.class));
            }
        });

        View share = root.findViewById(R.id.share);
        share.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_halloapp_text));
            intent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(intent, null);
            startActivity(shareIntent);
        });

        View privacy = root.findViewById(R.id.privacy);
        privacy.setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), SettingsPrivacy.class));
        });

        View notifications = root.findViewById(R.id.notifications);
        notifications.setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), SettingsNotifications.class));
        });

        View account = root.findViewById(R.id.account);
        account.setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), AccountActivity.class));
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
