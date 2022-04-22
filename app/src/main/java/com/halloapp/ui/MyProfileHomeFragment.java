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

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.StorageUsageActivity;
import com.halloapp.id.UserId;
import com.halloapp.permissions.PermissionUtils;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.archive.ArchiveActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.contacts.EditFavoritesActivity;
import com.halloapp.ui.contacts.ViewMyContactsActivity;
import com.halloapp.ui.invites.InviteContactsActivity;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.ui.settings.SettingsActivity;
import com.halloapp.ui.settings.SettingsNotifications;
import com.halloapp.ui.settings.SettingsPrivacy;
import com.halloapp.ui.settings.SettingsProfile;
import com.halloapp.ui.settings.SettingsVoiceVideo;
import com.halloapp.util.IntentUtils;
import com.halloapp.widget.ActionBarShadowOnScrollListener;

import java.util.Locale;

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
        about.setOnClickListener(v -> IntentUtils.openUrlInBrowser(about, "de".equals(Locale.getDefault().getLanguage()) ? Constants.GERMAN_ABOUT_URL : Constants.ABOUT_PAGE_URL));
        View myPosts = root.findViewById(R.id.my_posts);
        myPosts.setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), ViewMyPostsActivity.class));
        });

        View help = root.findViewById(R.id.help);
        help.setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), HelpActivity.class));
        });

        View myContacts = root.findViewById(R.id.my_contacts);
        myContacts.setOnClickListener(v -> {
            startActivity(ViewMyContactsActivity.viewMyContacts(v.getContext()));
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

        TextView number = root.findViewById(R.id.number);
        TextView name = root.findViewById(R.id.name);

        avatarView = root.findViewById(R.id.avatar);

        viewModel.getName().observe(getViewLifecycleOwner(), name::setText);
        viewModel.getPhone().observe(getViewLifecycleOwner(), number::setText);

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
