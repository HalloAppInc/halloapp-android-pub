package com.halloapp.katchup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.MainActivity;
import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.RelationshipInfo;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.katchup.avatar.KAvatarLoader;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.proto.server.FollowStatus;
import com.halloapp.proto.server.Link;
import com.halloapp.proto.server.UserProfile;
import com.halloapp.ui.BlurManager;
import com.halloapp.ui.HalloFragment;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.Connection;

import java.util.List;

import eightbitlab.com.blurview.BlurView;

public class NewProfileFragment extends HalloFragment {

    private static final String ARG_SELECTED_PROFILE_USER_ID = "view_user_id";
    private static final int NUM_MOMENTS_DISPLAYED = 4;

    public static NewProfileFragment newProfileFragment(@NonNull UserId userId) {
        NewProfileFragment newProfileFragment = new NewProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SELECTED_PROFILE_USER_ID, userId.rawId());
        newProfileFragment.setArguments(args);
        return newProfileFragment;
    }

    private NewProfileViewModel viewModel;

    private TextView title;
    private ImageButton more;
    private JellybeanClipView clipView;
    private ImageView profilePicture;
    private TextView name;
    private TextView username;
    private TextView userBio;
    private TextView addBio;
    private ImageView tiktok;
    private ImageView instagram;
    private TextView followButton;
    private View followsYou;
    private TextView featuredPostsInfo;
    private TextView calendar;

    private UserId profileUserId;

    private LinearLayout relationshipInfo;
    private LinearLayout archiveContent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_new_profile, container, false);
        MediaThumbnailLoader mediaThumbnailLoader = new MediaThumbnailLoader(requireContext(), 2 * getResources().getDimensionPixelSize(R.dimen.katchup_profile_archive_dim));

        View prev = root.findViewById(R.id.prev);
        prev.setOnClickListener(v -> {
            Activity activity = getActivity();
            if (activity instanceof MainActivity) {
                ((MainActivity) activity).previousScreen();
            } else {
                activity.onBackPressed();
            }
        });
        View next = root.findViewById(R.id.next);
        next.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) getActivity();
            activity.nextScreen();
        });

        profileUserId = UserId.ME;
        Bundle args = getArguments();
        if (args != null) {
            String extraUserId = args.getString(ARG_SELECTED_PROFILE_USER_ID);
            if (extraUserId != null) {
                profileUserId = new UserId(extraUserId);
            }
        }

        viewModel = new ViewModelProvider(requireActivity(), new NewProfileFragment.Factory(profileUserId)).get(NewProfileViewModel.class);

        title = root.findViewById(R.id.title);
        more = root.findViewById(R.id.more);
        clipView = root.findViewById(R.id.clip_view);
        profilePicture = root.findViewById(R.id.profile_picture);
        name = root.findViewById(R.id.name);
        username = root.findViewById(R.id.username);
        userBio = root.findViewById(R.id.user_bio);
        addBio = root.findViewById(R.id.add_bio);
        tiktok = root.findViewById(R.id.tiktok);
        instagram = root.findViewById(R.id.instagram);
        followButton = root.findViewById(R.id.follow_button);
        followsYou = root.findViewById(R.id.follows_you);
        featuredPostsInfo = root.findViewById(R.id.featured_posts_info);
        calendar = root.findViewById(R.id.calendar);

        relationshipInfo = root.findViewById(R.id.relationship_info);
        archiveContent = root.findViewById(R.id.blur_archive_content);

        boolean isMe = profileUserId.isMe();

        title.setVisibility(isMe ? View.VISIBLE : View.INVISIBLE);
        next.setVisibility(isMe ? View.VISIBLE : View.GONE);
        more.setVisibility(isMe ? View.GONE : View.VISIBLE);
        followButton.setVisibility(isMe ? View.GONE : View.VISIBLE);
        relationshipInfo.setVisibility(isMe ? View.GONE : View.VISIBLE);
        featuredPostsInfo.setVisibility(isMe ? View.VISIBLE : View.GONE);
        calendar.setVisibility(isMe ? View.VISIBLE : View.GONE);

        viewModel.getUserProfileInfo().observe(getViewLifecycleOwner(), profileInfo -> {
            updateFollowButton(profileInfo);
            KAvatarLoader.getInstance().load(profilePicture, profileUserId, profileInfo.avatarId);
            String usernameText = "@" + profileInfo.username;
            name.setText(profileInfo.name);
            username.setText(usernameText);
            followsYou.setVisibility(profileInfo.follower ? View.VISIBLE : View.GONE);

            String bio = profileInfo.bio;
            userBio.setText(bio);
            if (isMe && bio.isEmpty()) {
                addBio.setVisibility(View.VISIBLE);
                userBio.setVisibility(View.GONE);
            }

            List<Post> archiveMoments = profileInfo.archiveMoments;
            for (int i = 0; i < Math.min(archiveMoments.size(), NUM_MOMENTS_DISPLAYED); i++) {
                setProfileMoments(archiveContent, archiveMoments.get(i), mediaThumbnailLoader);
            }
        });

        viewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error == NewProfileViewModel.ERROR_FAILED_FOLLOW) {
                SnackbarHelper.showWarning(requireActivity(), R.string.failed_to_follow);
            } else if (error == NewProfileViewModel.ERROR_FAILED_UNFOLLOW) {
                SnackbarHelper.showWarning(requireActivity(), R.string.failed_to_unfollow);
            } else if (error == NewProfileViewModel.ERROR_FAILED_BLOCK) {
                SnackbarHelper.showWarning(requireActivity(), R.string.failed_to_block);
            } else if (error == NewProfileViewModel.ERROR_FAILED_UNBLOCK) {
                SnackbarHelper.showWarning(requireActivity(), R.string.failed_to_unblock);
            }
        });

        more.setOnClickListener(this::showMenu);

        //TODO(justin): add on-click listener to tiktok/insta to open up apps, add click listener to pfp, bio, etc to edit user info
        //TODO(justin): add click listener to clicking on featured posts info button
        calendar.setOnClickListener(view -> {
            startActivity(new Intent(requireContext(), ArchiveActivity.class));
        });
        return root;
    }

    private void updateFollowButton(UserProfileInfo profileInfo) {
        if (profileInfo.following) {
            followButton.setText(R.string.unfollow_profile);
        } else if (profileInfo.follower) {
            followButton.setText(R.string.follow_back_profile);
        } else {
            followButton.setText(R.string.follow_profile);
        }

        followButton.setTextColor(getResources().getColor(profileInfo.following ? R.color.white_50 : R.color.black));
        followButton.setBackground(ContextCompat.getDrawable(requireContext(), profileInfo.following ? R.drawable.unfollow_profile_button_background : R.drawable.follow_profile_button_background));
        followButton.setOnClickListener(v -> {
            if (profileInfo.following) {
                viewModel.unfollowUser();
            } else {
                viewModel.followUser();
            }
        });
    }

    private void setProfileMoments(LinearLayout layout, Post post, MediaThumbnailLoader mediaThumbnailLoader) {
        CardView archiveMomentView = (CardView) LayoutInflater.from(getContext()).inflate(R.layout.archive_moments_profile, layout, false);

        TextView date = archiveMomentView.findViewById(R.id.archive_moment_date);
        BlurView blurView = archiveMomentView.findViewById(R.id.blur_view);
        ImageView image = archiveMomentView.findViewById(R.id.archive_moment_image);
        FrameLayout imageContainer = archiveMomentView.findViewById(R.id.image_container);

        mediaThumbnailLoader.load(image, post.media.get(1));
        if (!profileUserId.isMe() && !ContentDb.getInstance().getMomentUnlockStatus().isUnlocked()) {
            BlurManager.getInstance().setupMomentBlur(blurView, imageContainer);
        }
        date.setText(TimeFormatter.formatRelativeTimeForKatchup(requireContext(), post.timestamp));
        layout.addView(archiveMomentView, 0);
    }

    private void showMenu(View v) {
        PopupMenu menu = new PopupMenu(requireContext(), v);
        menu.inflate(R.menu.katchup_profile);

        UserProfileInfo profileInfo = viewModel.getUserProfileInfo().getValue();

        if (profileInfo != null) {
            menu.getMenu().findItem(R.id.block).setVisible(!profileInfo.blocked);
            menu.getMenu().findItem(R.id.unblock).setVisible(profileInfo.blocked);
        } else {
            menu.getMenu().findItem(R.id.block).setVisible(false);
            menu.getMenu().findItem(R.id.unblock).setVisible(false);
        }

        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.block) {
                viewModel.blockUser();
            } else if (item.getItemId() == R.id.unblock) {
                viewModel.unblockUser();
            }

            return false;
        });

        menu.show();
    }

    private static class UserProfileInfo {
        private final UserId userId;
        private final String name;
        private final String username;
        private final String bio;
        private final String tiktok;
        private final String instagram;
        private final String avatarId;
        private final boolean follower; // is uid my follower
        private boolean following; // am I following uid
        private boolean blocked; // have I blocked uid
        private final List<Post> archiveMoments;

        public UserProfileInfo(@NonNull UserId userId, String name, String username, String bio, @Nullable String tiktok, @Nullable String instagram, @Nullable List<Post> archiveMoments, @Nullable String avatarId, boolean follower, boolean following, boolean blocked) {
            this.userId = userId;
            this.name = name;
            this.username = username;
            this.bio = bio;
            this.tiktok = tiktok;
            this.instagram = instagram;
            this.archiveMoments = archiveMoments;
            this.avatarId = avatarId;
            this.follower = follower;
            this.following = following;
            this.blocked = blocked;
        }
    }

    public static class NewProfileViewModel extends ViewModel {
        public static int ERROR_FAILED_FOLLOW = 1;
        public static int ERROR_FAILED_UNFOLLOW = 2;
        public static int ERROR_FAILED_BLOCK = 3;
        public static int ERROR_FAILED_UNBLOCK = 4;


        private final Me me = Me.getInstance();
        private final ContactsDb contactsDb = ContactsDb.getInstance();
        private final ContentDb contentDb = ContentDb.getInstance();
        private final Connection connection = Connection.getInstance();
        private final BgWorkers bgWorkers = BgWorkers.getInstance();
        private final RelationshipApi relationshipApi = RelationshipApi.getInstance();

        private final UserId userId;

        public final MutableLiveData<UserProfileInfo> item = new MutableLiveData<>();
        public final MutableLiveData<Integer> error = new MutableLiveData<>();

        public NewProfileViewModel(UserId userId) {
            this.userId = userId;
            computeUserProfileInfo(userId);
        }

        private void computeUserProfileInfo(UserId userId) {
            // TODO(jack): fix the hard coded 16 (limit should be for posts, not rows)
            bgWorkers.execute(() -> {
                List<Post> archiveMoments = contentDb.getPosts(null, 16, false, userId, null);
                connection.getKatchupUserProfileInfo(userId.isMe() ? new UserId(me.getUser()) : userId, null).onResponse(res -> {

                    UserProfile userProfile = res.getUserProfileResult().getProfile();
                    String name = userProfile.getName();
                    String username = userProfile.getUsername();
                    String bio = userProfile.getBio();
                    String tiktok = null;
                    String instagram = null;

                    for (Link link : userProfile.getLinksList()) {
                        if (link.getType() == Link.Type.TIKTOK) {
                            tiktok = link.getText();
                        } else if (link.getType() == Link.Type.INSTAGRAM) {
                            instagram = link.getText();
                        }
                    }

                    boolean follower = userProfile.getFollowerStatus().equals(FollowStatus.FOLLOWING);
                    boolean following = userProfile.getFollowingStatus().equals(FollowStatus.FOLLOWING);
                    boolean blocked = contactsDb.getRelationship(userId, RelationshipInfo.Type.BLOCKED) != null;

                    UserProfileInfo userProfileInfo = new UserProfileInfo(userId, name, username, bio, tiktok, instagram, archiveMoments, userProfile.getAvatarId(), follower, following, blocked);
                    item.postValue(userProfileInfo);
                }).onError(err -> {
                    Log.e("Failed to get profile info", err);
                });
            });
        }

        public void unfollowUser() {
            relationshipApi.requestUnfollowUser(userId).onResponse(success -> {
                if (Boolean.TRUE.equals(success)) {
                    UserProfileInfo profileInfo = item.getValue();
                    if (profileInfo != null) {
                        profileInfo.following = false;
                        item.postValue(profileInfo);
                    }
                } else {
                    Log.w("Unfollow failed for " + userId);
                    error.postValue(ERROR_FAILED_UNFOLLOW);
                }
            }).onError(err -> {
                Log.e("Failed to unfollow user", err);
                error.postValue(ERROR_FAILED_UNFOLLOW);
            });
        }

        public void followUser() {
            relationshipApi.requestFollowUser(userId).onResponse(success -> {
                if (Boolean.TRUE.equals(success)) {
                    UserProfileInfo profileInfo = item.getValue();
                    if (profileInfo != null) {
                        profileInfo.following = true;
                        item.postValue(profileInfo);
                    }
                } else {
                    Log.w("Follow failed for " + userId);
                    error.postValue(ERROR_FAILED_FOLLOW);
                }
            }).onError(err -> {
                Log.e("Failed to follow user", err);
                error.postValue(ERROR_FAILED_FOLLOW);
            });
        }

        public void blockUser() {
            relationshipApi.requestBlockUser(userId).onResponse(success -> {
                if (Boolean.TRUE.equals(success)) {
                    UserProfileInfo profileInfo = item.getValue();
                    if (profileInfo != null) {
                        profileInfo.blocked = true;
                        item.postValue(profileInfo);
                    }
                } else {
                    Log.w("Block failed for " + userId);
                    error.postValue(ERROR_FAILED_BLOCK);
                }
            }).onError(err -> {
                Log.e("Failed to block user", err);
                error.postValue(ERROR_FAILED_BLOCK);
            });
        }

        public void unblockUser() {
            relationshipApi.requestUnblockUser(userId).onResponse(success -> {
                if (Boolean.TRUE.equals(success)) {
                    UserProfileInfo profileInfo = item.getValue();
                    if (profileInfo != null) {
                        profileInfo.blocked = false;
                        item.postValue(profileInfo);
                    }
                } else {
                    Log.w("Unblock failed for " + userId);
                    error.postValue(ERROR_FAILED_UNBLOCK);
                }
            }).onError(err -> {
                Log.e("Failed to unblock user", err);
                error.postValue(ERROR_FAILED_UNBLOCK);
            });
        }

        public LiveData<UserProfileInfo> getUserProfileInfo() {
            return item;
        }
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final UserId profileUserId;

        public Factory(@NonNull UserId profileUserId) {
            this.profileUserId = profileUserId;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(NewProfileViewModel.class)) {
                //noinspection unchecked
                return (T) new NewProfileViewModel(profileUserId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
