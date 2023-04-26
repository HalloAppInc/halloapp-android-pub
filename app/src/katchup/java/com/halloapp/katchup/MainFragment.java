package com.halloapp.katchup;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.halloapp.ConnectionObservers;
import com.halloapp.Constants;
import com.halloapp.MainActivity;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.RelationshipInfo;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.KatchupPost;
import com.halloapp.content.MomentManager;
import com.halloapp.content.MomentUnlockStatus;
import com.halloapp.content.Post;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.katchup.avatar.KAvatarLoader;
import com.halloapp.katchup.media.ExternalSelfieLoader;
import com.halloapp.katchup.ui.KatchupShareExternallyView;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.ExternalMediaThumbnailLoader;
import com.halloapp.ui.HalloFragment;
import com.halloapp.ui.HeaderFooterAdapter;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.posts.PostListDiffer;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ShareExternallyView;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.FollowSuggestionsResponseIq;
import com.halloapp.xmpp.util.Observable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public class MainFragment extends HalloFragment implements EasyPermissions.PermissionCallbacks {
    private static final int REQUEST_LOCATION_PERMISSION = 0;
    private static final int REQUEST_POST_COMPOSER = 1;

    public static final String COMPOSER_VIEW_TRANSITION_NAME = "composer-view-transition-name";

    // TODO(vasil): tune distance and time interval if needed
    private static final float DISTANCE_THRESHOLD_METERS = 50;
    private static final long LOCATION_UPDATE_INTERVAL_MS = TimeUnit.MINUTES.toMillis(30);

    private MediaThumbnailLoader mediaThumbnailLoader;
    private ExternalMediaThumbnailLoader externalMediaThumbnailLoader;
    private ExternalSelfieLoader externalSelfieLoader;
    private ContactLoader contactLoader = new ContactLoader();
    private final KAvatarLoader kAvatarLoader = KAvatarLoader.getInstance();
    private final PublicContentCache publicContentCache = PublicContentCache.getInstance();
    private final Set<UserId> followedUsers = new HashSet<>();

    private MainViewModel viewModel;
    private ViewGroup parentViewGroup;
    private ImageView avatarView;
    private View followingTab;
    private RecyclerView followingListView;
    private PostAdapter followingListAdapter;
    private View publicTab;
    private RecyclerView publicListView;
    private PostAdapter publicListAdapter;
    private TextView followingButton;
    private TextView discoverButton;
    private View myPostHeader;
    private View uploadingProgressView;
    private ImageView composerTransitionView;
    private View followingEmpty;
    private View postYourOwn;
    private View publicFailedContainer;
    private TextView publicFailedText;
    private View tapToRefresh;
    private View discoverRefresh;
    private View tapToRequestLocation;
    private View tapToEnableNotifications;
    private View onlyOwnPost;
    private View updatedFeedView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ShareBannerPopupWindow shareBannerPopupWindow;

    private final ContactsDb.Observer contactsDbObserver = new ContactsDb.BaseObserver() {
        @Override
        public void onRelationshipRemoved(@NonNull RelationshipInfo relationshipInfo) {
            if (relationshipInfo.relationshipType == RelationshipInfo.Type.FOLLOWING) {
                followedUsers.remove(relationshipInfo.userId);
                publicTab.post(() -> {
                    publicListAdapter.notifyDataSetChanged();
                });
            }
        }
    };

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            Log.d("MainFragment.LocationListener.onLocationChanged latitude=" + location.getLatitude() + " longitude=" + location.getLongitude());
            viewModel.updateLocation(location);
        }

        @Override
        public void onLocationChanged(@NonNull List<Location> locations) {
            Log.d("MainFragment.LocationListener.onLocationChanged locations.size=" + locations.size());
            if (!locations.isEmpty()) {
                onLocationChanged(locations.get(locations.size() - 1));
            }
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            Log.d("MainFragment.LocationListener.onProviderEnabled provider=" + provider);
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            Log.d("MainFragment.LocationListener.onProviderDisabled provider=" + provider);
        }

        // These two are required on older devices to prevent java.lang.AbstractMethodError
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override
        public void onFlushComplete(int requestCode) {}
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);

        parentViewGroup = container;

        final Point point = new Point();
        requireActivity().getWindowManager().getDefaultDisplay().getSize(point);
        mediaThumbnailLoader = new MediaThumbnailLoader(requireContext(), Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));
        externalMediaThumbnailLoader = new ExternalMediaThumbnailLoader(requireContext(), Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));
        externalSelfieLoader = new ExternalSelfieLoader();

        View prev = root.findViewById(R.id.prev);
        prev.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) getActivity();
            activity.previousScreen();
        });
        View next = root.findViewById(R.id.next);
        next.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) getActivity();
            activity.nextScreen();
        });
        root.findViewById(R.id.avatars).setOnClickListener(v -> {
            MainActivity activity = (MainActivity) getActivity();
            activity.previousScreen();
        });
        root.findViewById(R.id.only_own_avatars).setOnClickListener(v -> {
            MainActivity activity = (MainActivity) getActivity();
            activity.previousScreen();
        });

        // Do not allow taps to pass through the protective background
        root.findViewById(R.id.protective_background).setOnClickListener(v -> {});

        avatarView = root.findViewById(R.id.avatar);
        kAvatarLoader.load(avatarView, UserId.ME);

        followingTab = root.findViewById(R.id.following_tab);
        publicTab = root.findViewById(R.id.discover_tab);

        followingListView = root.findViewById(R.id.following_list);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        followingListView.setLayoutManager(layoutManager);
        followingListAdapter = new PostAdapter(false);
        followingListView.setAdapter(followingListAdapter);
        followingListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                notifyPostsSeen(layoutManager, recyclerView, false);
            }
        });

        publicListView = root.findViewById(R.id.public_list);
        final LinearLayoutManager publicLayoutManager = new LinearLayoutManager(requireContext());
        publicListView.setLayoutManager(publicLayoutManager);
        publicListAdapter = new PostAdapter(true);
        publicListView.setAdapter(publicListAdapter);

        publicListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() >= publicListAdapter.getItemCount() - 2) {
                    viewModel.maybeFetchMoreFeed();
                }

                notifyPostsSeen(publicLayoutManager, recyclerView, true);
            }
        });

        View title = root.findViewById(R.id.title);
        title.setOnClickListener(v -> {
            boolean followingTabSelected = Boolean.TRUE.equals(viewModel.followingTabSelected.getValue());
            if (followingTabSelected) {
                layoutManager.scrollToPosition(0);
            } else {
                publicLayoutManager.scrollToPosition(0);
            }
        });

        followingEmpty = root.findViewById(R.id.following_empty);
        onlyOwnPost = root.findViewById(R.id.only_own_post);
        postYourOwn = root.findViewById(R.id.post_your_own);
        postYourOwn.setOnClickListener(v -> {
            Analytics.getInstance().tappedPostButtonFromEmptyState();
            startComposerActivity();
        });

        followingButton = root.findViewById(R.id.following);
        followingButton.setOnClickListener(v -> viewModel.setFollowingSelected(true));
        discoverButton = root.findViewById(R.id.discover);
        discoverButton.setOnClickListener(v -> {
            boolean hasUpdatedFeed = Boolean.TRUE.equals(publicContentCache.getHasUpdatedFeed().getValue());
            if (hasUpdatedFeed) {
                viewModel.updatePublicFeed();
                publicListView.scrollToPosition(0);
            } else {
                viewModel.maybeRefreshPublicFeed();
            }
            viewModel.setFollowingSelected(false);
        });

        viewModel = new ViewModelProvider(requireActivity(), new MainViewModel.MainViewModelFactory(getActivity().getApplication(), externalMediaThumbnailLoader)).get(MainViewModel.class);
        viewModel.followingTabSelected.observe(getViewLifecycleOwner(), selected -> {
            boolean followingSelected = Boolean.TRUE.equals(selected);
            setFollowingSelected(followingSelected);
            notifyPostsSeen(followingSelected ? layoutManager : publicLayoutManager, followingSelected ? followingListView : publicListView, !followingSelected);
        });

        TextView newFollowerCount = root.findViewById(R.id.new_follower_count);
        viewModel.unseenFollowerCount.getLiveData().observe(getViewLifecycleOwner(), count -> {
            if (count > 0) {
                newFollowerCount.setVisibility(View.VISIBLE);
                newFollowerCount.setText("+" + count);
            } else {
                newFollowerCount.setVisibility(View.GONE);
            }
        });

        swipeRefreshLayout = root.findViewById(R.id.swipe_refresh_layout);
        int extraOffset = getResources().getDimensionPixelOffset(R.dimen.feed_protective_background_height);
        swipeRefreshLayout.setProgressViewOffset(false, swipeRefreshLayout.getProgressViewStartOffset() + extraOffset, swipeRefreshLayout.getProgressViewEndOffset() + extraOffset);
        swipeRefreshLayout.setOnRefreshListener(() -> viewModel.refreshPublicFeed());
        viewModel.refreshing.observe(getViewLifecycleOwner(), refreshing -> {
            swipeRefreshLayout.setRefreshing(Boolean.TRUE.equals(refreshing));
            if (Boolean.TRUE.equals(refreshing)) {
                publicListView.scrollToPosition(0);
            }
        });

        discoverRefresh = root.findViewById(R.id.discover_refresh);
        discoverRefresh.setOnClickListener(v -> {
            viewModel.showCachedItems();
            publicListView.scrollToPosition(0);
        });
        viewModel.restarted.observe(getViewLifecycleOwner(), restarted -> {
            discoverRefresh.setVisibility(restarted ? View.VISIBLE : View.GONE);
        });

        updatedFeedView = root.findViewById(R.id.updated_feed_dot);
        publicContentCache.getHasUpdatedFeed().observe(getViewLifecycleOwner(), hasUpdatedFeed -> {
            updatedFeedView.setVisibility(hasUpdatedFeed ? View.VISIBLE : View.GONE);
        });

        tapToRequestLocation = root.findViewById(R.id.request_location_access);
        tapToRequestLocation.setOnClickListener(v -> {
            requestLocationPermission();
        });
        tapToEnableNotifications = root.findViewById(R.id.enable_notifications);
        tapToEnableNotifications.setOnClickListener(v -> {
            Notifications.openNotificationSettings(requireActivity());
        });

        publicFailedContainer = root.findViewById(R.id.public_feed_failed_container);
        publicFailedText = root.findViewById(R.id.public_feed_failed_text);
        tapToRefresh = root.findViewById(R.id.tap_to_refresh);
        tapToRefresh.setOnClickListener(v -> {
            viewModel.refreshPublicFeed();
        });
        viewModel.getPublicFeedLoadFailed().observe(getViewLifecycleOwner(), failed -> {
            boolean hasFailed = Boolean.TRUE.equals(failed);
            if (hasFailed) {
                publicFailedContainer.setVisibility(View.VISIBLE);
                publicFailedText.setText(R.string.failed_load_public_feed);
                publicListView.setVisibility(View.GONE);
            }
        });

        viewModel.postList.observe(getViewLifecycleOwner(), posts -> {
            Log.d("MainFragment got new post list " + posts);
            followingListAdapter.submitList(posts, () -> {
                updateEmptyState();
                notifyPostsSeen(layoutManager, followingListView, false);
            });
        });

        viewModel.publicFeed.observe(getViewLifecycleOwner(), posts -> {
            Log.d("MainFragment got new public post list " + posts);
            if (posts != null && posts.isEmpty() && !swipeRefreshLayout.isRefreshing()) {
                Log.w("Public feed fetch was empty");
                publicFailedContainer.setVisibility(View.VISIBLE);
                publicFailedText.setText(R.string.failed_empty_public_feed);
                publicListView.setVisibility(View.GONE);
            } else {
                publicFailedContainer.setVisibility(View.GONE);
                publicListView.setVisibility(View.VISIBLE);
            }
            publicListAdapter.submitItems(posts);
        });

        followingListAdapter.addMomentsHeader();
        viewModel.momentList.getLiveData().observe(getViewLifecycleOwner(), moments -> {
            followingListAdapter.setMoments(moments);
            updateEmptyState();
        });

        // TODO(jack): Determine why onCreateView is receiving a null container, which causes the layout params to not be set
        myPostHeader = followingListAdapter.addHeader(R.layout.header_my_post);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myPostHeader.setLayoutParams(lp);
        myPostHeader.findViewById(R.id.card_view).setOnClickListener(v -> {
            Post post = viewModel.myPost.getLiveData().getValue();
            if (post != null) {
                startActivity(ViewKatchupCommentsActivity.viewPost(requireContext(), post));
            }
        });
        composerTransitionView = myPostHeader.findViewById(R.id.image);
        composerTransitionView.setTransitionName(COMPOSER_VIEW_TRANSITION_NAME);
        uploadingProgressView = myPostHeader.findViewById(R.id.uploading_progress);

        viewModel.myPost.getLiveData().observe(getViewLifecycleOwner(), post -> {
            if (post == null) {
                followingListAdapter.hideHeader();
            } else {
                followingListAdapter.showHeader();
                mediaThumbnailLoader.load(myPostHeader.findViewById(R.id.image), post.media.get(1));
                mediaThumbnailLoader.load(myPostHeader.findViewById(R.id.selfie_preview), post.media.get(0));
                TextView badgeCount = myPostHeader.findViewById(R.id.badge_count);
                badgeCount.setVisibility(post.commentCount > 0 ? View.VISIBLE : View.GONE);
                badgeCount.setText(String.format(Locale.getDefault(), "%d", post.commentCount));
            }
            updateEmptyState();
        });

        viewModel.suggestedUsers.observe(getViewLifecycleOwner(), users -> {
            // TODO(jack): Extract a class for displaying avatars like this to avoid this duplication
            View avatarClip1 = root.findViewById(R.id.suggested_avatar_clip_1);
            avatarClip1.setVisibility(users.size() > 0 ? View.VISIBLE : View.GONE);
            if (users.size() > 0) {
                ImageView avatar = root.findViewById(R.id.suggested_avatar_1);
                FollowSuggestionsResponseIq.Suggestion suggestion = users.get(0);
                kAvatarLoader.load(avatar, suggestion.info.userId, suggestion.info.avatarId);
            }
            View avatarClip2 = root.findViewById(R.id.suggested_avatar_clip_2);
            avatarClip2.setVisibility(users.size() > 1 ? View.VISIBLE : View.GONE);
            if (users.size() > 1) {
                ImageView avatar = root.findViewById(R.id.suggested_avatar_2);
                FollowSuggestionsResponseIq.Suggestion suggestion = users.get(1);
                kAvatarLoader.load(avatar, suggestion.info.userId, suggestion.info.avatarId);
            }
            View avatarClip3 = root.findViewById(R.id.suggested_avatar_clip_3);
            avatarClip3.setVisibility(users.size() > 2 ? View.VISIBLE : View.GONE);
            if (users.size() > 2) {
                ImageView avatar = root.findViewById(R.id.suggested_avatar_3);
                FollowSuggestionsResponseIq.Suggestion suggestion = users.get(2);
                kAvatarLoader.load(avatar, suggestion.info.userId, suggestion.info.avatarId);
            }

            View onlyOwnAvatarClip1 = root.findViewById(R.id.only_own_suggested_avatar_clip_1);
            onlyOwnAvatarClip1.setVisibility(users.size() > 0 ? View.VISIBLE : View.GONE);
            if (users.size() > 0) {
                ImageView avatar = root.findViewById(R.id.only_own_suggested_avatar_1);
                FollowSuggestionsResponseIq.Suggestion suggestion = users.get(0);
                kAvatarLoader.load(avatar, suggestion.info.userId, suggestion.info.avatarId);
            }
            View onlyOwnAvatarClip2 = root.findViewById(R.id.only_own_suggested_avatar_clip_2);
            onlyOwnAvatarClip2.setVisibility(users.size() > 1 ? View.VISIBLE : View.GONE);
            if (users.size() > 1) {
                ImageView avatar = root.findViewById(R.id.only_own_suggested_avatar_2);
                FollowSuggestionsResponseIq.Suggestion suggestion = users.get(1);
                kAvatarLoader.load(avatar, suggestion.info.userId, suggestion.info.avatarId);
            }
            View onlyOwnAvatarClip3 = root.findViewById(R.id.only_own_suggested_avatar_clip_3);
            onlyOwnAvatarClip3.setVisibility(users.size() > 2 ? View.VISIBLE : View.GONE);
            if (users.size() > 2) {
                ImageView avatar = root.findViewById(R.id.only_own_suggested_avatar_3);
                FollowSuggestionsResponseIq.Suggestion suggestion = users.get(2);
                kAvatarLoader.load(avatar, suggestion.info.userId, suggestion.info.avatarId);
            }
        });

        View findPeople = root.findViewById(R.id.find_people);
        findPeople.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) getActivity();
            activity.previousScreen();
        });

        MomentManager.getInstance().isUnlockedLiveData().observe(getViewLifecycleOwner(), momentUnlockStatus -> {
            uploadingProgressView.setVisibility(momentUnlockStatus.isUnlocking() ? View.VISIBLE : View.GONE);
            boolean followingSelected = Boolean.TRUE.equals(viewModel.followingTabSelected.getValue());
            notifyPostsSeen(followingSelected ? layoutManager : publicLayoutManager, followingSelected ? followingListView : publicListView, !followingSelected);
        });

        ContactsDb.getInstance().addObserver(contactsDbObserver);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        Notifications.getInstance(requireActivity()).clearMomentNotifications();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ContactsDb.getInstance().removeObserver(contactsDbObserver);
    }

    private void notifyPostsSeen(@NonNull LinearLayoutManager layoutManager, @NonNull RecyclerView recyclerView, boolean publicFeed) {
        if (Objects.equals(publicFeed, viewModel.followingTabSelected.getValue())) {
            return;
        }

        MomentUnlockStatus momentUnlockStatus = MomentManager.getInstance().isUnlockedLiveData().getValue();
        if (momentUnlockStatus == null || !momentUnlockStatus.isUnlocked()) {
            return;
        }

        int first = layoutManager.findFirstVisibleItemPosition();
        int last = layoutManager.findLastVisibleItemPosition();
        for (int i=first; i<=last; i++) {
            RecyclerView.ViewHolder vh = recyclerView.findViewHolderForLayoutPosition(i);
            if (vh instanceof KatchupPostViewHolder) {
                KatchupPostViewHolder kpvh = (KatchupPostViewHolder) vh;
                if (!kpvh.seenReceiptSent) {
                    kpvh.seenReceiptSent = true;
                    sendSeenReceipt((KatchupPost) kpvh.post);
                }
            }
        }
    }

    private void showShareBannerAfterTransition() {
        requireActivity().getWindow().getSharedElementReenterTransition().addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                transition.removeListener(this);
                showShareBanner();
            }

            @Override
            public void onTransitionCancel(Transition transition) {
                transition.removeListener(this);
                showShareBanner();
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }
        });
    }

    private void showShareBanner() {
        Post post = viewModel.myPost.getLiveData().getValue();

        if (post != null) {
            if (shareBannerPopupWindow != null) {
                shareBannerPopupWindow.dismiss();
            }

            shareBannerPopupWindow = new ShareBannerPopupWindow(requireContext(), post);
            shareBannerPopupWindow.show(myPostHeader);
        }
    }

    private void sendSeenReceipt(@NonNull KatchupPost post) {
        String feedType = Boolean.TRUE.equals(viewModel.followingTabSelected.getValue()) ? "following" : "public";
        Analytics.getInstance().seenPost(post.id, post.contentType, post.notificationId, feedType);

        Connection.getInstance().sendPostSeenReceipt(post.senderUserId, post.id);
    }

    @Override
    public void onResume() {
        super.onResume();
        Analytics.getInstance().openScreen(
                Boolean.TRUE.equals(viewModel.followingTabSelected.getValue()) ? "followingFeed" : "publicFeed");
        kAvatarLoader.load(avatarView, UserId.ME);
        if (hasLocationPermission()) {
            Log.d("MainFragment.onResume hasLocationPermission=true");
            tapToRequestLocation.setVisibility(View.GONE);
            startLocationUpdates();
        } else {
            Log.d("MainFragment.onResume hasLocationPermission=false");
            tapToRequestLocation.setVisibility(View.VISIBLE);
        }
        tapToEnableNotifications.setVisibility(NotificationManagerCompat.from(requireActivity()).areNotificationsEnabled() ? View.GONE : View.VISIBLE);
        if (Boolean.FALSE.equals(viewModel.followingTabSelected.getValue())) {
            viewModel.maybeRefreshPublicFeed();
        }
        viewModel.unseenFollowerCount.invalidate();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            startLocationUpdates();
            tapToRequestLocation.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        // EasyPermissions.permissionPermanentlyDenied returns true when the "Ask every time" permission option is chosen.
        // So do this check here instead of in requestLocationPermission to allow the normal request code to fire first.
        if (requestCode == REQUEST_LOCATION_PERMISSION && EasyPermissions.permissionPermanentlyDenied(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AppSettingsDialog.Builder(this)
                    .setRationale(getString(R.string.location_access_request_rationale_denied))
                    .setNegativeButton(R.string.permission_negative_button_text)
                    .build().show();
        }
    }

    public void onActivityReenter(int resultCode, Intent data) {
        boolean followingTabSelected = Boolean.TRUE.equals(viewModel.followingTabSelected.getValue());

        if (resultCode == Activity.RESULT_OK && followingTabSelected && data != null && data.getBooleanExtra(SelfiePostComposerActivity.EXTRA_COMPOSER_TRANSITION, false)) {
            ViewGroup.LayoutParams params = composerTransitionView.getLayoutParams();
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            composerTransitionView.setLayoutParams(params);

            requireActivity().postponeEnterTransition();

            composerTransitionView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    composerTransitionView.getViewTreeObserver().removeOnPreDrawListener(this);
                    showShareBannerAfterTransition();
                    requireActivity().startPostponedEnterTransition();

                    return true;
                }
            });
        }
    }

    public void startComposerActivity() {
        boolean followingTabSelected = Boolean.TRUE.equals(viewModel.followingTabSelected.getValue());

        if (followingTabSelected) {
            // avoid the share element showing during the activity start transition
            ViewGroup.LayoutParams params = composerTransitionView.getLayoutParams();
            params.height = 0;
            composerTransitionView.setLayoutParams(params);
        }

        BgWorkers.getInstance().execute(() -> {
            Intent intent = SelfiePostComposerActivity.startFromApp(requireContext());

            composerTransitionView.post(() -> {
                if (followingTabSelected) {
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(requireActivity(), composerTransitionView, composerTransitionView.getTransitionName());
                    startActivityForResult(intent, REQUEST_POST_COMPOSER, options.toBundle());
                } else {
                    startActivity(intent);
                }
            });
        });
    }

    private void updateEmptyState() {
        List<Post> postList = viewModel.postList.getValue();
        List<KatchupPost> momentList = viewModel.momentList.getLiveData().getValue();
        Post myPost = viewModel.myPost.getLiveData().getValue();
        boolean hasOtherPosts = (postList != null && !postList.isEmpty()) || (momentList != null && !momentList.isEmpty());
        boolean hasPosts = hasOtherPosts || myPost != null;
        onlyOwnPost.setVisibility(!hasOtherPosts && hasPosts ? View.VISIBLE : View.GONE);
        followingEmpty.setVisibility(hasPosts ? View.GONE : View.VISIBLE);
        followingListView.setVisibility(hasPosts ? View.VISIBLE : View.GONE);

        // The RV does not reach behind the header if there are no other posts, so do not add that extra padding in that case
        followingListView.setPadding(
                followingListView.getPaddingLeft(),
                hasOtherPosts ? getResources().getDimensionPixelSize(R.dimen.feed_protective_background_height) : 0,
                followingListView.getPaddingRight(),
                followingListView.getPaddingBottom()
        );
    }

    private void setFollowingSelected(boolean followingSelected) {
        int selectedTextColor = getResources().getColor(R.color.white);
        int unselectedTextColor = getResources().getColor(R.color.black);
        Analytics.getInstance().openScreen(followingSelected ? "followingFeed" : "publicFeed");
        Drawable selectedBackgroundDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.selected_feed_type_background);
        Drawable unselectedBackgroundDrawable = null;
        followingButton.setBackground(followingSelected ? selectedBackgroundDrawable : unselectedBackgroundDrawable);
        followingButton.setTextColor(followingSelected ? selectedTextColor : unselectedTextColor);
        discoverButton.setBackground(followingSelected ? unselectedBackgroundDrawable : selectedBackgroundDrawable);
        discoverButton.setTextColor(followingSelected ? unselectedTextColor : selectedTextColor);
        followingTab.setVisibility(followingSelected ? View.VISIBLE : View.GONE);
        publicTab.setVisibility(followingSelected ? View.GONE : View.VISIBLE);
    }

    private boolean hasLocationPermission() {
        return EasyPermissions.hasPermissions(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) ||
                EasyPermissions.hasPermissions(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestLocationPermission() {
        EasyPermissions.requestPermissions(
                new PermissionRequest.Builder(this, REQUEST_LOCATION_PERMISSION, Manifest.permission.ACCESS_FINE_LOCATION)
                        .setRationale(R.string.location_access_request_rationale)
                        .setNegativeButtonText(R.string.permission_negative_button_text)
                        .build());
    }

    private void startLocationUpdates() {
        final LocationManager locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = null;
        if (EasyPermissions.hasPermissions(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationProvider = LocationManager.GPS_PROVIDER;
        } else if (EasyPermissions.hasPermissions(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationProvider = LocationManager.NETWORK_PROVIDER;
        }
        if (locationProvider != null) {
            Log.d("MainFragment.startLocationUpdates provider=" + locationProvider);
            final Location lastLocation = locationManager.getLastKnownLocation(locationProvider);
            if (lastLocation != null) {
                Log.d("MainFragment.startLocationUpdates latitude=" + lastLocation.getLatitude() + " longitude=" + lastLocation.getLongitude());
                viewModel.updateLocation(locationManager.getLastKnownLocation(locationProvider));
            }
            locationManager.requestLocationUpdates(locationProvider, LOCATION_UPDATE_INTERVAL_MS, DISTANCE_THRESHOLD_METERS, locationListener);
        } else {
            Log.sendErrorReport("MainFragment.startLocationUpdates failed to find a locationProvider");
        }
    }

    private void stopLocationUpdates() {
        final LocationManager locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);
    }

    public static class MainViewModel extends AndroidViewModel {

        private final ContentDb contentDb = ContentDb.getInstance();
        private final ConnectionObservers connectionObservers = ConnectionObservers.getInstance();

        private final PublicContentCache.Observer cacheObserver = new PublicContentCache.DefaultObserver() {
            @Override
            public void onPostRemoved(@NonNull Post post) {
                Log.d("MainFragment content cache observer post removed");
                List<Post> posts = publicFeed.getValue();
                posts.remove(post);
                publicFeed.postValue(posts);
            }

            @Override
            public void onCommentsAdded(@NonNull List<Comment> comment) {
                Log.d("MainFragment cache observer comments added");

                List<Post> posts = publicFeed.getValue();
                if (posts == null) {
                    return;
                }
                publicFeed.postValue(posts);
            }

            @Override
            public void onCommentRetracted(@NonNull Comment comment) {
                Log.d("MainFragment cache observer comment retracted");

                List<Post> posts = publicFeed.getValue();
                if (posts == null) {
                    return;
                }
                publicFeed.postValue(posts);
            }
        };

        private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {
            @Override
            public void onPostAdded(@NonNull Post post) {
                Log.d("MainFragment content observer post added " + post);
                if (post.senderUserId.isMe()) {
                    myPost.invalidate();
                } else {
                    momentList.invalidate();
                }
                dataSourceFactory.invalidateLatestDataSource();
            }

            @Override
            public void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {
                Log.d("MainFragment content observer post updated " + postId);
                if (senderUserId.isMe()) {
                    myPost.invalidate();
                } else {
                    dataSourceFactory.invalidateLatestDataSource();
                    momentList.invalidate();
                }
            }

            @Override
            public void onPostRetracted(@NonNull Post post) {
                Log.d("MainFragment content observer post retracted " + post);
                if (post.senderUserId.isMe()) {
                    myPost.invalidate();
                } else {
                    dataSourceFactory.invalidateLatestDataSource();
                    momentList.invalidate();
                }
            }

            @Override
            public void onCommentAdded(@NonNull Comment comment) {
                Log.d("MainFragment content observer comment added " + comment);
                Post post = myPost.getLiveData().getValue();
                if (post != null && comment.postId.equals(post.id)) {
                    myPost.invalidate();
                } else {
                    PagedList<Post> followingPosts = postList.getValue();
                    if (followingPosts == null) {
                        return;
                    }
                    for (Post followingPost : followingPosts) {
                        if (comment.postId.equals(followingPost.id)) {
                            dataSourceFactory.invalidateLatestDataSource();
                            return;
                        }
                    }

                    List<Post> publicPosts = publicFeed.getValue();
                    if (publicPosts == null) {
                        return;
                    }
                    publicFeed.postValue(publicPosts);
                }
            }

            @Override
            public void onCommentRetracted(@NonNull Comment comment) {
                Log.d("MainFragment content observer comment retracted " + comment);
                Post post = myPost.getLiveData().getValue();
                if (post != null && comment.postId.equals(post.id)) {
                    myPost.invalidate();
                } else {
                    PagedList<Post> followingPosts = postList.getValue();
                    if (followingPosts == null) {
                        return;
                    }
                    for (Post followingPost : followingPosts) {
                        if (comment.postId.equals(followingPost.id)) {
                            dataSourceFactory.invalidateLatestDataSource();
                            return;
                        }
                    }

                    List<Post> publicPosts = publicFeed.getValue();
                    if (publicPosts == null) {
                        return;
                    }
                    publicFeed.postValue(publicPosts);
                }
            }

            @Override
            public void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId, @Nullable GroupId parentGroup) {
                Log.d("MainFragment content observer comments seen for " + postId);
                Post post = myPost.getLiveData().getValue();
                if (post != null && postId.equals(post.id)) {
                    myPost.invalidate();
                }
            }

            @Override
            public void onIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId, @Nullable GroupId groupId) {
                Log.d("MainFragment content observer post marked seen " + postId);
                dataSourceFactory.invalidateLatestDataSource();
                momentList.invalidate();
            }

            @Override
            public void onPostsExpired() {
                dataSourceFactory.invalidateLatestDataSource();
                momentList.invalidate();
                myPost.invalidate();
            }
        };

        private final Connection.Observer connectionObserver = new Connection.Observer() {
            @Override
            public void onConnected() {
                fetchSuggestions();
            }
        };

        private final ExternalMediaThumbnailLoader externalMediaThumbnailLoader;
        private final KatchupPostsDataSource.Factory dataSourceFactory;
        // TODO(michelle): Move publicFeed to PublicContentCache
        final MutableLiveData<List<Post>> publicFeed = new MutableLiveData<>();
        final MutableLiveData<Boolean> followingTabSelected = new MutableLiveData<>(true);
        final LiveData<PagedList<Post>> postList;
        final ComputableLiveData<Post> myPost;
        final ComputableLiveData<List<KatchupPost>> momentList;
        final MutableLiveData<List<FollowSuggestionsResponseIq.Suggestion>> suggestedUsers = new MutableLiveData<>();
        private final MutableLiveData<Boolean> publicFeedLoadFailed = new MutableLiveData<>(false);
        final MutableLiveData<Boolean> restarted = new MutableLiveData<>(false);
        final MutableLiveData<Boolean> refreshing = new MutableLiveData<>(true);
        final ComputableLiveData<Integer> unseenFollowerCount;

        private boolean publicFeedFetchInProgress;
        private List<Post> items = new ArrayList<>();
        private String lastCursor;
        private Location location;
        private long lastPublicFeedFetchTimestamp;
        private final PublicContentCache publicContentCache = PublicContentCache.getInstance();

        public MainViewModel(@NonNull Application application, @NonNull ExternalMediaThumbnailLoader externalMediaThumbnailLoader) {
            super(application);

            PublicContentCache.getInstance().addObserver(cacheObserver);
            contentDb.addObserver(contentObserver);
            connectionObservers.addObserver(connectionObserver);
            this.externalMediaThumbnailLoader = externalMediaThumbnailLoader;
            dataSourceFactory = new KatchupPostsDataSource.Factory(contentDb);
            postList = new LivePagedListBuilder<>(dataSourceFactory, 50).build();

            myPost = new ComputableLiveData<Post>() {
                @Override
                protected Post compute() {
                    String unlockingPost = contentDb.getUnlockingMomentId();
                    if (unlockingPost == null) {
                        return null;
                    }
                    Post post = contentDb.getPost(unlockingPost);
                    if (post != null) {
                        post.commentCount = contentDb.getCommentCount(post.id, false, false);
                    }
                    return post;
                }
            };

            momentList = new ComputableLiveData<List<KatchupPost>>() {
                @Override
                protected List<KatchupPost> compute() {
                    Log.d("MainFragment computing new moment list");
                    List<Post> posts = contentDb.getAllUnseenPosts();
                    List<KatchupPost> ret = new ArrayList<>();
                    for (Post post : posts) {
                        ret.add((KatchupPost) post);
                    }
                    return ret;
                }
            };

            unseenFollowerCount = new ComputableLiveData<Integer>() {
                @Override
                protected Integer compute() {
                    return ContactsDb.getInstance().getUnseenFollowerCount();
                }
            };

            fetchSuggestions();
        }

        private MutableLiveData<Boolean> getPublicFeedLoadFailed() {
            return publicFeedLoadFailed;
        }

        private void fetchSuggestions() {
            Connection.getInstance().requestFollowSuggestions().onResponse(response -> {
                if (!response.success) {
                    Log.e("Suggestion fetch was not successful");
                } else {
                    Map<UserId, String> names = new HashMap<>();
                    Map<UserId, String> usernames = new HashMap<>();
                    Map<UserId, String> avatars = new HashMap<>();
                    List<FollowSuggestionsResponseIq.Suggestion> suggestions = new ArrayList<>();

                    for (FollowSuggestionsResponseIq.Suggestion suggestion : response.suggestions) {
                        names.put(suggestion.info.userId, suggestion.info.name);
                        usernames.put(suggestion.info.userId, suggestion.info.username);
                        avatars.put(suggestion.info.userId, suggestion.info.avatarId);
                        suggestions.add(suggestion);
                    }

                    Comparator<FollowSuggestionsResponseIq.Suggestion> comparator = (o1, o2) -> o1.rank - o2.rank;
                    Collections.sort(suggestions, comparator);

                    ContactsDb contactsDb = ContactsDb.getInstance();
                    contactsDb.updateUserNames(names);
                    contactsDb.updateUserUsernames(usernames);
                    contactsDb.updateUserAvatars(avatars);

                    suggestedUsers.postValue(suggestions);
                }
            }).onError(error -> {
                Log.e("Suggestion fetch got error", error);
            });
        }

        private void updatePublicFeed() {
            Log.d("MainFragment.updatePublicFeed");
            refreshing.postValue(false);
            lastCursor = publicContentCache.getCursor();
            publicContentCache.markFeedUpdateComplete();
            showCachedItems();
        }

        private void fetchPublicFeed() {
            Log.d("MainFragment.fetchPublicFeed");
            publicFeedFetchInProgress = true;
            BgWorkers.getInstance().execute(() -> {
                final boolean showDevContent = Preferences.getInstance().getShowDevContent();
                final Double latitude = location != null ? location.getLatitude() : null;
                final Double longitude = location != null ? location.getLongitude() : null;
                Connection.getInstance().requestPublicFeed(this.lastCursor, latitude, longitude, showDevContent).onResponse(response -> {
                    Preferences.getInstance().setGeotag(response.geotags.size() <= 0 ? null : response.geotags.get(0));
                    Analytics.getInstance().updateGeotag();
                    refreshing.postValue(false);
                    lastPublicFeedFetchTimestamp = System.currentTimeMillis();
                    if (!response.success) {
                        Log.e("Public feed fetch was not successful");
                        publicFeedFetchInProgress = false;
                        publicFeedLoadFailed.postValue(true);
                    } else {
                        lastCursor = response.cursor;
                        Log.d("Public feed last cursor updated to " + lastCursor);
                        List<Post> posts = publicContentCache.processPublicFeedItems(response.items, response.restarted);

                        if (response.restarted) {
                            restarted.postValue(true);
                        } else {
                            items.addAll(posts);
                            publicFeed.postValue(items);
                        }
                        publicFeedFetchInProgress = false;
                        publicFeedLoadFailed.postValue(false);
                    }
                }).onError(error -> {
                    Log.e("Failed to fetch public feed", error);
                    publicFeedFetchInProgress = false;
                    publicFeedLoadFailed.postValue(true);
                });
            });
        }

        public void showCachedItems() {
            List<Post> cachedItems = publicContentCache.processCachedItems();
            items.clear();
            items.addAll(cachedItems);
            publicFeed.postValue(items);
            restarted.postValue(false);
        }

        public void maybeFetchMoreFeed() {
            if (publicFeedFetchInProgress) {
                Log.d("MainFragment.maybeFetchMoreFeed fetch already in progress");
                return;
            }
            if (TextUtils.isEmpty(lastCursor)) {
                Log.d("MainFragment.maybeFetchMoreFeed skipping fetch due to empty cursor");
                return;
            }
            if (Boolean.TRUE.equals(restarted.getValue())) {
                Log.d("MainFragment.maybeFetchMoreFeed skipping fetch due to cursor restart");
                return;
            }
            fetchPublicFeed();
        }

        public void maybeRefreshPublicFeed() {
            if (lastCursor != null && lastCursor.equals(publicContentCache.getCursor())) {
                Log.d("MainFragment.maybeRefreshPublicFeed skipping since current cursor has already being handled");
                return;
            }
            if (System.currentTimeMillis() - lastPublicFeedFetchTimestamp > ServerProps.getInstance().getPublicFeedRefreshIntervalSeconds() * 1000L) {
                Log.d("MainFragment.maybeRefreshPublicFeed refreshing since refresh interval has passed");
                refreshPublicFeed();
            }
        }

        public void refreshPublicFeed() {
            Log.d("MainFragment.refreshPublicFeed");
            items.clear();
            refreshing.postValue(true);
            publicFeed.postValue(items);
            lastCursor = null;
            fetchPublicFeed();
        }

        public void setFollowingSelected(boolean selected) {
            followingTabSelected.postValue(selected);
        }

        public void updateLocation(@Nullable Location updatedLocation) {
            if ((location == null && updatedLocation == null) ||
                    (location != null && updatedLocation != null && updatedLocation.distanceTo(location) < DISTANCE_THRESHOLD_METERS)) {
                return;
            }

            location = updatedLocation;
        }

        @Override
        protected void onCleared() {
            PublicContentCache.getInstance().removeObserver(cacheObserver);
            contentDb.removeObserver(contentObserver);
            connectionObservers.removeObserver(connectionObserver);
        }

        public static class MainViewModelFactory implements ViewModelProvider.Factory {

            private final Application application;
            private final ExternalMediaThumbnailLoader externalMediaThumbnailLoader;

            public MainViewModelFactory(@NonNull Application application, @NonNull ExternalMediaThumbnailLoader externalMediaThumbnailLoader) {
                this.application = application;
                this.externalMediaThumbnailLoader = externalMediaThumbnailLoader;
            }

            @Override
            public <T extends ViewModel> T create(Class<T> modelClass) {
                if (modelClass.isAssignableFrom(MainViewModel.class)) {
                    //noinspection unchecked
                    return (T) new MainViewModel(application, externalMediaThumbnailLoader);
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        }
    }

    private class PostAdapter extends HeaderFooterAdapter<Post> {

        private final PostListDiffer postListDiffer;
        private final boolean publicPosts;

        private KatchupStackLayout momentsHeaderView;

        private final KatchupPostViewHolder.KatchupViewHolderParent katchupViewHolderParent = new KatchupPostViewHolder.KatchupViewHolderParent() {
            @Override
            public ContactLoader getContactLoader() {
                return contactLoader;
            }

            @Override
            public MediaThumbnailLoader getMediaThumbnailLoader() {
                return mediaThumbnailLoader;
            }

            @Override
            public MediaThumbnailLoader getExternalMediaThumbnailLoader() {
                return externalMediaThumbnailLoader;
            }

            @Override
            public ExternalSelfieLoader getExternalSelfieLoader() {
                return externalSelfieLoader;
            }

            @Override
            public KAvatarLoader getAvatarLoader() {
                return kAvatarLoader;
            }

            @Override
            public void startActivity(Intent intent) {
                MainFragment.this.startActivity(intent);
            }

            @Override
            public void startComposerActivity() {
                MainFragment.this.startComposerActivity();
            }

            @Override
            public Observable<Boolean> followUser(UserId userId) {
                return RelationshipApi.getInstance().requestFollowUser(userId).map(input -> {
                    if (Boolean.TRUE.equals(input)) {
                        followedUsers.add(userId);
                    }
                    return input;
                });
            }

            @Override
            public boolean wasUserFollowed(UserId userId) {
                return followedUsers.contains(userId);
            }
        };

        public PostAdapter(boolean publicPosts) {
            super(new HeaderFooterAdapter.HeaderFooterAdapterParent() {
                @NonNull
                @Override
                public Context getContext() {
                    return requireContext();
                }

                @NonNull
                @Override
                public ViewGroup getParentViewGroup() {
                    return parentViewGroup;
                }
            });

            this.publicPosts = publicPosts;

            setHasStableIds(true);

            final ListUpdateCallback listUpdateCallback = createUpdateCallback();

            postListDiffer = new PostListDiffer(listUpdateCallback);
            if (!publicPosts) {
                setDiffer(postListDiffer);
            }
        }

        protected ListUpdateCallback createUpdateCallback() {
            final AdapterListUpdateCallback adapterCallback = new AdapterListUpdateCallback(this);
            return new ListUpdateCallback() {

                public void onInserted(int position, int count) {
                    position = translateToAdapterPos(position);
                    adapterCallback.onInserted(position, count);
                }

                public void onRemoved(int position, int count) {
                    position = translateToAdapterPos(position);
                    adapterCallback.onRemoved(position, count);
                }

                public void onMoved(int fromPosition, int toPosition) {
                    fromPosition = translateToAdapterPos(fromPosition);
                    toPosition = translateToAdapterPos(toPosition);
                    adapterCallback.onMoved(fromPosition, toPosition);
                }

                public void onChanged(int position, int count, @Nullable Object payload) {
                    position = translateToAdapterPos(position);
                    adapterCallback.onChanged(position, count, payload);
                }
            };
        }

        protected int translateToAdapterPos(int position) {
            position += getHeaderCount();
            return position;
        }

        @Override
        public long getIdForItem(Post post) {
            return post.rowId;
        }

        @Override
        public int getViewTypeForItem(Post post) {
            return 0;
        }

        @NonNull
        @Override
        public ViewHolderWithLifecycle createViewHolderForViewType(@NonNull ViewGroup parent, int viewType) {
            return new KatchupPostViewHolder(LayoutInflater.from(requireContext()).inflate(R.layout.post_item_katchup, parent, false), katchupViewHolderParent);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolderWithLifecycle holder, int position) {
            if (holder instanceof KatchupPostViewHolder) {
                Post post = Preconditions.checkNotNull(getItem(position));
                ((KatchupPostViewHolder) holder).bindTo(post, false, publicPosts);
            }
        }

        private void hideHeader() {
            ViewGroup.LayoutParams params = myPostHeader.getLayoutParams();
            if (params == null) {
                params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
            } else {
                params.height = 0;
            }

            myPostHeader.setLayoutParams(params);
        }

        private void showHeader() {
            ViewGroup.LayoutParams params = myPostHeader.getLayoutParams();
            if (params == null) {
                params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
            } else {
                params.height = RecyclerView.LayoutParams.WRAP_CONTENT;
            }

            myPostHeader.setLayoutParams(params);
        }

        public void addMomentsHeader() {
            momentsHeaderView = (KatchupStackLayout) addHeader(R.layout.katchup_stack);
            momentsHeaderView.load(katchupViewHolderParent);

            hideMoments();
        }

        private void hideMoments() {
            ViewGroup.LayoutParams params = momentsHeaderView.getLayoutParams();
            if (params == null) {
                params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
            } else {
                params.height = 0;
            }

            momentsHeaderView.setLayoutParams(params);
        }

        private void showMoments() {
            ViewGroup.LayoutParams params = momentsHeaderView.getLayoutParams();
            if (params == null) {
                params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
            } else {
                params.height = RecyclerView.LayoutParams.WRAP_CONTENT;
            }

            momentsHeaderView.setLayoutParams(params);
        }

        public void setMoments(List<KatchupPost> moments) {
            if (moments != null && moments.size() > 0) {
                momentsHeaderView.bindTo(moments);
                showMoments();
            } else {
                hideMoments();
            }
        }
    }

    class ShareBannerPopupWindow extends PopupWindow {
        public ShareBannerPopupWindow(@NonNull Context context, @NonNull Post post) {
            super(context);

            setWidth(ViewGroup.LayoutParams.MATCH_PARENT);

            View root = LayoutInflater.from(context).inflate(R.layout.share_banner, null, false);
            setContentView(root);

            KatchupShareExternallyView shareExternallyView = root.findViewById(R.id.list);
            shareExternallyView.setListener(new ShareExternallyView.ShareListener() {
                @Override
                public void onOpenShare() {
                    share(root, null, post);
                }

                @Override
                public void onShareTo(ShareExternallyView.ShareTarget target) {
                    share(root, target.getPackageName(), post);
                }
            });

            setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            setOutsideTouchable(true);
            setFocusable(false);
        }

        public void show(@NonNull View anchor) {
            View contentView = getContentView();
            contentView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            showAsDropDown(anchor, 0, -contentView.getMeasuredHeight() - anchor.getHeight());
        }

        private void share(@NonNull View view, @Nullable String targetPackage, @NonNull Post post) {
            Context context = view.getContext();
            ProgressDialog progressDialog = ProgressDialog.show(context, null, getString(R.string.share_moment_progress));

            ShareIntentHelper.shareExternallyWithPreview(context, targetPackage, post, true).observe(getViewLifecycleOwner(), intent -> {
                progressDialog.dismiss();

                if (intent != null) {
                    startActivity(intent);
                    dismiss();
                } else {
                    SnackbarHelper.showWarning(view, R.string.external_share_failed);
                }
            });
        }
    }
}
