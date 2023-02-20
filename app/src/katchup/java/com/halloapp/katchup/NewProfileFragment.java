package com.halloapp.katchup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
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
import androidx.transition.TransitionManager;

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
import com.halloapp.proto.server.BasicUserProfile;
import com.halloapp.proto.server.FollowStatus;
import com.halloapp.proto.server.Link;
import com.halloapp.proto.server.UserProfile;
import com.halloapp.ui.BlurManager;
import com.halloapp.ui.HalloFragment;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.DialogFragmentUtils;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.Connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import eightbitlab.com.blurview.BlurView;

public class NewProfileFragment extends HalloFragment {

    private static final String ARG_SELECTED_PROFILE_USER_ID = "view_user_id";
    private static final String ARG_SELECTED_PROFILE_USERNAME = "view_username";
    private static final int NUM_MOMENTS_DISPLAYED = 4;

    public static NewProfileFragment newProfileFragment(@NonNull UserId userId) {
        NewProfileFragment newProfileFragment = new NewProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SELECTED_PROFILE_USER_ID, userId.rawId());
        newProfileFragment.setArguments(args);
        return newProfileFragment;
    }

    public static NewProfileFragment newProfileFragment(@NonNull String username) {
        NewProfileFragment newProfileFragment = new NewProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SELECTED_PROFILE_USERNAME, username);
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
    private ImageView snapchat;
    private ImageView link;
    private TextView followButton;
    private View followsYou;
    private TextView calendar;

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
        View settings = root.findViewById(R.id.settings);
        settings.setOnClickListener(v -> startActivity(SettingsActivity.open(requireContext())));

        UserId profileUserId = null;
        String profileUsername = null;
        Bundle args = getArguments();
        if (args != null) {
            String extraUserId = args.getString(ARG_SELECTED_PROFILE_USER_ID);
            if (extraUserId != null) {
                profileUserId = new UserId(extraUserId);
            }
            profileUsername = args.getString(ARG_SELECTED_PROFILE_USERNAME);
        }

        viewModel = new ViewModelProvider(requireActivity(), new NewProfileFragment.Factory(profileUserId, profileUsername)).get(NewProfileViewModel.class);

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
        snapchat = root.findViewById(R.id.snapchat);
        link = root.findViewById(R.id.link);
        followButton = root.findViewById(R.id.follow_button);
        followsYou = root.findViewById(R.id.follows_you);
        calendar = root.findViewById(R.id.calendar);
        TextView relevantFollowersView = root.findViewById(R.id.relevant_followers);

        relationshipInfo = root.findViewById(R.id.relationship_info);
        archiveContent = root.findViewById(R.id.blur_archive_content);

        View contentView = root.findViewById(R.id.content);
        View progressView = root.findViewById(R.id.progress);
        contentView.setVisibility(View.GONE);
        progressView.setVisibility(View.VISIBLE);

        if (profileUserId != null && !profileUserId.isMe()) {
            title.setVisibility(View.INVISIBLE);
            settings.setVisibility(View.GONE);
            more.setVisibility(View.VISIBLE);
        }

        viewModel.getUserProfileInfo().observe(getViewLifecycleOwner(), profileInfo -> {

            boolean isMe = profileInfo.userId.isMe();

            KAvatarLoader.getInstance().loadLarge(profilePicture, profileInfo.userId, profileInfo.avatarId);

            title.setVisibility(isMe ? View.VISIBLE : View.INVISIBLE);
            settings.setVisibility(isMe ? View.VISIBLE : View.GONE);
            more.setVisibility(isMe ? View.GONE : View.VISIBLE);
            followButton.setVisibility(isMe ? View.GONE : View.VISIBLE);
            relationshipInfo.setVisibility(isMe ? View.GONE : View.VISIBLE);
            calendar.setVisibility(isMe ? View.VISIBLE : View.GONE);

            if (isMe) {
                clipView.setOnClickListener(v -> openProfileEdit());
                name.setOnClickListener(v -> openProfileEdit());
                username.setOnClickListener(v -> openProfileEdit());
                addBio.setOnClickListener(v -> openProfileEdit());
                userBio.setOnClickListener(v -> openProfileEdit());
            }

            updateLinks(profileInfo);
            updateFollowButton(profileInfo);
            String usernameText = "@" + profileInfo.username;
            name.setText(profileInfo.name);
            username.setText(usernameText);
            followsYou.setVisibility(profileInfo.follower ? View.VISIBLE : View.GONE);

            String bio = profileInfo.bio;
            userBio.setText(bio);
            addBio.setVisibility(isMe && TextUtils.isEmpty(bio) ? View.VISIBLE : View.GONE);
            userBio.setVisibility(TextUtils.isEmpty(bio) ? View.GONE : View.VISIBLE);

            if (isMe) {
                relevantFollowersView.setVisibility(View.GONE);
            } else {
                if (profileInfo.relevantFollowers.size() > 0) {
                    relevantFollowersView.setVisibility(View.VISIBLE);
                    relevantFollowersView.setText(formatRelevantFollowers(profileInfo));
                } else {
                    relevantFollowersView.setVisibility(View.GONE);
                }
            }

            archiveContent.removeAllViews();
            List<Post> archiveMoments = profileInfo.archiveMoments;
            for (int i = 0; i < Math.min(archiveMoments.size(), NUM_MOMENTS_DISPLAYED); i++) {
                setProfileMoments(profileInfo.userId, archiveContent, archiveMoments.get(i), mediaThumbnailLoader);
            }

            if (contentView.getVisibility() != View.VISIBLE) {
                TransitionManager.beginDelayedTransition((ViewGroup) contentView.getParent());
                contentView.setVisibility(View.VISIBLE);
                progressView.setVisibility(View.GONE);
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
            } else if (error == NewProfileViewModel.ERROR_FAILED_REMOVE_FOLLOWER) {
                SnackbarHelper.showWarning(requireActivity(), R.string.failed_to_remove_follower);
            }
        });

        more.setOnClickListener(this::showMenu);

        //TODO(justin): add on-click listener to tiktok/insta to open up apps, add click listener to pfp, bio, etc to edit user info
        //TODO(justin): add click listener to clicking on featured posts info button
        calendar.setOnClickListener(view -> {
            startActivity(new Intent(requireContext(), ArchiveActivity.class));
        });

        relevantFollowersView.setOnClickListener(v -> showRelevantFollowers());

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.computeUserProfileInfo();
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

    private void setProfileMoments(UserId profileUserId, LinearLayout layout, Post post, MediaThumbnailLoader mediaThumbnailLoader) {
        CardView archiveMomentView = (CardView) LayoutInflater.from(getContext()).inflate(R.layout.archive_moments_profile, layout, false);

        archiveMomentView.setOnClickListener(v -> startActivity(ViewKatchupCommentsActivity.viewPost(requireContext(), post)));

        TextView date = archiveMomentView.findViewById(R.id.archive_moment_date);
        BlurView blurView = archiveMomentView.findViewById(R.id.blur_view);
        ImageView image = archiveMomentView.findViewById(R.id.archive_moment_image);
        FrameLayout imageContainer = archiveMomentView.findViewById(R.id.image_container);

        mediaThumbnailLoader.load(image, post.media.get(1));
        if (!profileUserId.isMe() && !ContentDb.getInstance().getMomentUnlockStatus().isUnlocked()) {
            BlurManager.getInstance().setupMomentBlur(blurView, imageContainer);
        }
        date.setText(DateUtils.formatDateTime(requireContext(), post.timestamp, DateUtils.FORMAT_NO_YEAR|DateUtils.FORMAT_ABBREV_MONTH).toLowerCase(Locale.getDefault()));
        layout.addView(archiveMomentView, 0);
    }

    private void updateLinks(@NonNull UserProfileInfo profileInfo) {
        link.setVisibility(TextUtils.isEmpty(profileInfo.link) ? View.GONE : View.VISIBLE);
        tiktok.setVisibility(TextUtils.isEmpty(profileInfo.tiktok) ? View.GONE : View.VISIBLE);
        instagram.setVisibility(TextUtils.isEmpty(profileInfo.instagram) ? View.GONE : View.VISIBLE);
        snapchat.setVisibility(TextUtils.isEmpty(profileInfo.snapchat) ? View.GONE : View.VISIBLE);

        link.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(profileInfo.link)) {
                IntentUtils.openUrlInBrowser(requireActivity(), profileInfo.link);
            }
        });

        instagram.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(profileInfo.instagram)) {
                String username = profileInfo.instagram.startsWith("@") ? profileInfo.instagram.substring(1) : profileInfo.instagram;
                String link = "https://www.instagram.com/" + username;
                IntentUtils.openUrlInBrowser(requireActivity(), link);
            }
        });

        tiktok.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(profileInfo.tiktok)) {
                String username = profileInfo.tiktok.startsWith("@") ? profileInfo.tiktok : "@" + profileInfo.tiktok;
                String link = "https://www.tiktok.com/" + username;
                IntentUtils.openUrlInBrowser(requireActivity(), link);
            }
        });

        snapchat.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(profileInfo.snapchat)) {
                String username = profileInfo.snapchat.startsWith("@") ? profileInfo.snapchat.substring(1) : profileInfo.snapchat;
                String link = "https://www.snapchat.com/add/" + username;
                IntentUtils.openUrlInBrowser(requireActivity(), link);
            }
        });
    }

    private CharSequence formatRelevantFollowers(UserProfileInfo profileInfo) {
        // TODO: Improve internationalization support
        int relevantCount = 0;

        StringBuilder builder = new StringBuilder();
        for (BasicUserProfile profile : profileInfo.relevantFollowers) {
            String username = profile.getUsername();

            if (!username.startsWith("@")) {
                username = "@" + username;
            }

            builder.append(username);
            relevantCount++;

            if (relevantCount < profileInfo.relevantFollowers.size()) {
                builder.append(", ");
            }

            if (relevantCount == 2) {
                break;
            }
        }

        if (relevantCount < profileInfo.relevantFollowers.size()) {
            builder.append(getString(R.string.mutuals_who_follow_user_others, profileInfo.relevantFollowers.size() - relevantCount));
        }

        String content = builder.toString();
        String result = getResources().getQuantityString(R.plurals.mutuals_who_follow_user, profileInfo.relevantFollowers.size(), content);
        int index = result.indexOf(content);

        SpannableString spannableResult = new SpannableString(result);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(getResources().getColor(R.color.white));
        spannableResult.setSpan(colorSpan, index, index + content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableResult;
    }

    private void showMenu(View v) {
        PopupMenu menu = new PopupMenu(requireContext(), v);
        menu.inflate(R.menu.katchup_profile);

        UserProfileInfo profileInfo = viewModel.getUserProfileInfo().getValue();

        if (profileInfo != null) {
            menu.getMenu().findItem(R.id.block).setVisible(!profileInfo.blocked);
            menu.getMenu().findItem(R.id.unblock).setVisible(profileInfo.blocked);
            menu.getMenu().findItem(R.id.report).setVisible(true);
            menu.getMenu().findItem(R.id.remove_follower).setVisible(profileInfo.follower);
        } else {
            menu.getMenu().findItem(R.id.block).setVisible(false);
            menu.getMenu().findItem(R.id.unblock).setVisible(false);
            menu.getMenu().findItem(R.id.report).setVisible(false);
            menu.getMenu().findItem(R.id.remove_follower).setVisible(false);
        }

        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.block) {
                viewModel.blockUser();
            } else if (item.getItemId() == R.id.unblock) {
                viewModel.unblockUser();
            } else if (item.getItemId() == R.id.report) {
                startActivity(ReportActivity.open(requireContext(), viewModel.getUserProfileInfo().getValue().userId, null));
            } else if (item.getItemId() == R.id.remove_follower) {
                viewModel.removeFollower();
            }

            return false;
        });

        menu.show();
    }

    private void showRelevantFollowers() {
        UserProfileInfo profileInfo = viewModel.getUserProfileInfo().getValue();

        if (profileInfo != null) {
            RelevantFollowersBottomSheetDialogFragment fragment = new RelevantFollowersBottomSheetDialogFragment(profileInfo.relevantFollowers);
            DialogFragmentUtils.showDialogFragmentOnce(fragment, getChildFragmentManager());
        }
    }

    private void openProfileEdit() {
        startActivity(ProfileEditActivity.open(requireContext()));
    }

    private static class UserProfileInfo {
        private final UserId userId;
        private final String name;
        private final String username;
        private final String bio;
        private final String tiktok;
        private final String instagram;
        private final String link;
        private final String snapchat;
        private final String avatarId;
        private boolean follower; // is uid my follower
        private boolean following; // am I following uid
        private boolean blocked; // have I blocked uid
        private final List<Post> archiveMoments;

        private final List<BasicUserProfile> relevantFollowers;

        public UserProfileInfo(@NonNull UserId userId, String name, String username, String bio, @Nullable String link, @Nullable String tiktok, @Nullable String instagram, @Nullable String snapchat, @Nullable String avatarId, @Nullable List<Post> archiveMoments, boolean follower, boolean following, boolean blocked, List<BasicUserProfile> relevantFollowers) {
            this.userId = userId;
            this.name = name;
            this.username = username;
            this.bio = bio;
            this.link = link;
            this.tiktok = tiktok;
            this.instagram = instagram;
            this.snapchat = snapchat;
            this.avatarId = avatarId;
            this.archiveMoments = archiveMoments;
            this.follower = follower;
            this.following = following;
            this.blocked = blocked;
            this.relevantFollowers = relevantFollowers;
        }
    }

    public static class NewProfileViewModel extends ViewModel {
        public static final int ERROR_FAILED_FOLLOW = 1;
        public static final int ERROR_FAILED_UNFOLLOW = 2;
        public static final int ERROR_FAILED_BLOCK = 3;
        public static final int ERROR_FAILED_UNBLOCK = 4;
        public static final int ERROR_FAILED_REMOVE_FOLLOWER = 5;


        private final Me me = Me.getInstance();
        private final ContactsDb contactsDb = ContactsDb.getInstance();
        private final ContentDb contentDb = ContentDb.getInstance();
        private final Connection connection = Connection.getInstance();
        private final BgWorkers bgWorkers = BgWorkers.getInstance();
        private final RelationshipApi relationshipApi = RelationshipApi.getInstance();

        private final UserId userId;
        private final String username;

        public final MutableLiveData<UserProfileInfo> item = new MutableLiveData<>();
        public final MutableLiveData<Integer> error = new MutableLiveData<>();

        public NewProfileViewModel(UserId userId, String username) {
            this.userId = userId;
            this.username = username;
        }

        public void computeUserProfileInfo() {
            // TODO(jack): fix the hard coded 16 (limit should be for posts, not rows)
            bgWorkers.execute(() -> {
                List<Post> posts = username != null ? new ArrayList<>() : contentDb.getPosts(null, 16, false, userId, null);
                List<Post> archiveMoments = new ArrayList<>();
                for (Post post : posts) {
                    if (post.type == Post.TYPE_KATCHUP) {
                        archiveMoments.add(post);
                    }
                }
                connection.getKatchupUserProfileInfo(username != null ? null : userId.isMe() ? new UserId(me.getUser()) : userId, username).onResponse(res -> {

                    UserProfile userProfile = res.getUserProfileResult().getProfile();
                    UserId profileUserId = new UserId(Long.toString(userProfile.getUid()));
                    if (Me.getInstance().getUser().equals(profileUserId.rawId())) {
                        profileUserId = UserId.ME;
                        Analytics.getInstance().openScreen("ownProfile");
                    } else {
                        Analytics.getInstance().openScreen("profile");
                    }
                    String name = userProfile.getName();
                    String username = userProfile.getUsername();
                    String bio = userProfile.getBio();
                    String userDefinedLink = null;
                    String tiktok = null;
                    String instagram = null;
                    String snapchat = null;

                    for (Link link : userProfile.getLinksList()) {
                        if (link.getType() == Link.Type.TIKTOK) {
                            tiktok = link.getText();
                        } else if (link.getType() == Link.Type.INSTAGRAM) {
                            instagram = link.getText();
                        } else if (link.getType() == Link.Type.SNAPCHAT) {
                            snapchat = link.getText();
                        } else if (link.getType() == Link.Type.USER_DEFINED) {
                            userDefinedLink = link.getText();
                        }
                    }

                    boolean follower = userProfile.getFollowerStatus().equals(FollowStatus.FOLLOWING);
                    boolean following = userProfile.getFollowingStatus().equals(FollowStatus.FOLLOWING);
                    boolean blocked = contactsDb.getRelationship(profileUserId, RelationshipInfo.Type.BLOCKED) != null;

                    UserProfileInfo userProfileInfo = new UserProfileInfo(
                            profileUserId,
                            name,
                            username,
                            bio,
                            userDefinedLink,
                            tiktok,
                            instagram,
                            snapchat,
                            userProfile.getAvatarId(),
                            archiveMoments,
                            follower,
                            following,
                            blocked,
                            userProfile.getRelevantFollowersList());
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

        public void removeFollower() {
            relationshipApi.requestRemoveFollower(userId).onResponse(success -> {
                if (Boolean.TRUE.equals(success)) {
                    UserProfileInfo profileInfo = item.getValue();
                    if (profileInfo != null) {
                        profileInfo.follower = false;
                        item.postValue(profileInfo);
                    }
                } else {
                    Log.w("Remove follower failed for " + userId);
                    error.postValue(ERROR_FAILED_REMOVE_FOLLOWER);
                }
            }).onError(err -> {
                Log.e("Failed to remove follower", err);
                error.postValue(ERROR_FAILED_REMOVE_FOLLOWER);
            });
        }

        public LiveData<UserProfileInfo> getUserProfileInfo() {
            return item;
        }
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final UserId profileUserId;
        private final String username;

        public Factory(@Nullable UserId profileUserId, @Nullable String username) {
            if (profileUserId == null && username == null) {
                profileUserId = UserId.ME;
            }
            this.profileUserId = profileUserId;
            this.username = username;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(NewProfileViewModel.class)) {
                //noinspection unchecked
                return (T) new NewProfileViewModel(profileUserId, username);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
