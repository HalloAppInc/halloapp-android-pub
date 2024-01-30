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
import com.halloapp.contacts.SocialLink;
import com.halloapp.id.UserId;
import com.halloapp.permissions.PermissionUtils;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.contacts.EditFavoritesActivity;
import com.halloapp.ui.contacts.ViewFriendsListActivity;
import com.halloapp.ui.invites.InviteContactsActivity;
import com.halloapp.ui.profile.LinksBottomSheetDialogFragment;
import com.halloapp.ui.settings.SettingsActivity;
import com.halloapp.ui.settings.SettingsProfile;
import com.halloapp.util.DialogFragmentUtils;
import com.halloapp.widget.ActionBarShadowOnScrollListener;

import java.text.NumberFormat;
import java.util.ArrayList;

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

        TextView myFriendRequestsCount = root.findViewById(R.id.friend_requests_count);
        viewModel.getFriendRequestsCount().observe(getViewLifecycleOwner(), count -> {
            myFriendRequestsCount.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            myFriendRequestsCount.setText(NumberFormat.getInstance().format(count));
        });

        View socialMediaEmpty = root.findViewById(R.id.social_media_empty);
        View socialMediaAdd = root.findViewById(R.id.social_media_add);
        socialMediaEmpty.setOnClickListener(v -> startActivity(new Intent(v.getContext(), SettingsProfile.class)));
        socialMediaAdd.setOnClickListener(v -> startActivity(new Intent(v.getContext(), SettingsProfile.class)));

        View socialMedia = root.findViewById(R.id.social_media);
        View instagram = root.findViewById(R.id.instagram);
        View x = root.findViewById(R.id.x_twitter);
        View tiktok = root.findViewById(R.id.tiktok);
        View youtube = root.findViewById(R.id.youtube);
        View userLink = root.findViewById(R.id.user_link);
        viewModel.getLinks().observe(getViewLifecycleOwner(), links -> {
            if (links == null || links.isEmpty()) {
                socialMediaEmpty.setVisibility(View.VISIBLE);
                socialMedia.setVisibility(View.GONE);
            } else {
                socialMediaEmpty.setVisibility(View.GONE);
                socialMedia.setVisibility(View.VISIBLE);
                ArrayList<SocialLink> instagramLinks = new ArrayList<>();
                ArrayList<SocialLink> xLinks = new ArrayList<>();
                ArrayList<SocialLink> tiktokLinks = new ArrayList<>();
                ArrayList<SocialLink> youtubeLinks = new ArrayList<>();
                ArrayList<SocialLink> userLinks = new ArrayList<>();
                for (SocialLink link : links) {
                    switch (link.type) {
                        case SocialLink.Type.INSTAGRAM:
                            instagramLinks.add(link);
                            break;
                        case SocialLink.Type.X:
                            xLinks.add(link);
                            break;
                        case SocialLink.Type.TIKTOK:
                            tiktokLinks.add(link);
                            break;
                        case SocialLink.Type.YOUTUBE:
                            youtubeLinks.add(link);
                            break;
                        default:
                            userLinks.add(link);
                            break;
                    }
                }

                instagram.setOnClickListener(v -> DialogFragmentUtils.showDialogFragmentOnce(LinksBottomSheetDialogFragment.newInstance(instagramLinks), getParentFragmentManager()));
                x.setOnClickListener(v -> DialogFragmentUtils.showDialogFragmentOnce(LinksBottomSheetDialogFragment.newInstance(xLinks), getParentFragmentManager()));
                tiktok.setOnClickListener(v -> DialogFragmentUtils.showDialogFragmentOnce(LinksBottomSheetDialogFragment.newInstance(tiktokLinks), getParentFragmentManager()));
                youtube.setOnClickListener(v -> DialogFragmentUtils.showDialogFragmentOnce(LinksBottomSheetDialogFragment.newInstance(youtubeLinks), getParentFragmentManager()));
                userLink.setOnClickListener(v -> DialogFragmentUtils.showDialogFragmentOnce(LinksBottomSheetDialogFragment.newInstance(userLinks), getParentFragmentManager()));

                instagram.setVisibility(instagramLinks.isEmpty() ? View.GONE : View.VISIBLE);
                x.setVisibility(xLinks.isEmpty() ? View.GONE : View.VISIBLE);
                tiktok.setVisibility(tiktokLinks.isEmpty() ? View.GONE : View.VISIBLE);
                youtube.setVisibility(youtubeLinks.isEmpty() ? View.GONE : View.VISIBLE);
                userLink.setVisibility(userLinks.isEmpty() ? View.GONE : View.VISIBLE);
            }
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

        TextView usernameView = root.findViewById(R.id.username);
        TextView nameView = root.findViewById(R.id.name);

        avatarView = root.findViewById(R.id.avatar);

        viewModel.getName().observe(getViewLifecycleOwner(), nameView::setText);
        viewModel.getUsername().observe(getViewLifecycleOwner(), username -> {
            usernameView.setText(username == null ? null : "@" + username);
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
        viewModel.computeLinks();
    }
}
