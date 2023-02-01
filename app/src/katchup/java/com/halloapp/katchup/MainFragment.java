package com.halloapp.katchup;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.Constants;
import com.halloapp.MainActivity;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.KatchupPost;
import com.halloapp.content.MomentManager;
import com.halloapp.content.MomentUnlockStatus;
import com.halloapp.content.Post;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.katchup.avatar.KAvatarLoader;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.proto.clients.Container;
import com.halloapp.proto.server.MomentInfo;
import com.halloapp.proto.server.MomentNotification;
import com.halloapp.proto.server.PublicFeedItem;
import com.halloapp.ui.ExternalMediaThumbnailLoader;
import com.halloapp.ui.HalloFragment;
import com.halloapp.ui.HeaderFooterAdapter;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.posts.PostListDiffer;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.FollowSuggestionsResponseIq;
import com.halloapp.xmpp.feed.FeedContentParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public class MainFragment extends HalloFragment implements EasyPermissions.PermissionCallbacks {
    private static final int REQUEST_LOCATION_PERMISSION = 0;

    // TODO(vasil): tune distance and time interval if needed
    private static final float DISTANCE_THRESHOLD_METERS = 50;
    private static final long LOCATION_UPDATE_INTERVAL_MS = TimeUnit.MINUTES.toMillis(30);

    private MediaThumbnailLoader mediaThumbnailLoader;
    private MediaThumbnailLoader externalMediaThumbnailLoader;
    private ContactLoader contactLoader = new ContactLoader();
    private final KAvatarLoader kAvatarLoader = KAvatarLoader.getInstance();

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
    private View followingEmpty;
    private View postYourOwn;
    private View publicFailed;
    private View tapToRefresh;
    private View discoverRefresh;
    private View requestLocation;
    private View onlyOwnPost;

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

        followingEmpty = root.findViewById(R.id.following_empty);
        onlyOwnPost = root.findViewById(R.id.only_own_post);
        postYourOwn = root.findViewById(R.id.post_your_own);
        postYourOwn.setOnClickListener(v -> {
            BgWorkers.getInstance().execute(() -> {
                Context context = postYourOwn.getContext();
                Preferences preferences = Preferences.getInstance();
                int type = preferences.getMomentNotificationType();
                long notificationId = preferences.getMomentNotificationId();
                long timestamp = preferences.getMomentNotificationTimestamp();
                Intent contentIntent;
                if (type == MomentNotification.Type.LIVE_CAMERA_VALUE) {
                    contentIntent = SelfiePostComposerActivity.startCapture(context, notificationId, timestamp);
                } else if (type == MomentNotification.Type.TEXT_POST_VALUE) {
                    contentIntent = SelfiePostComposerActivity.startText(context, notificationId, timestamp);
                } else if (type == MomentNotification.Type.PROMPT_POST_VALUE) {
                    contentIntent = SelfiePostComposerActivity.startPrompt(context, notificationId, timestamp);
                } else {
                    throw new IllegalStateException("Unexpected moment notification type " + type);
                }
                postYourOwn.post(() -> startActivity(contentIntent));
            });
        });

        followingButton = root.findViewById(R.id.following);
        followingButton.setOnClickListener(v -> viewModel.setFollowingSelected(true));
        discoverButton = root.findViewById(R.id.discover);
        discoverButton.setOnClickListener(v -> viewModel.setFollowingSelected(false));

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        viewModel.followingTabSelected.observe(getViewLifecycleOwner(), selected -> {
            boolean followingSelected = Boolean.TRUE.equals(selected);
            setFollowingSelected(followingSelected);
            notifyPostsSeen(followingSelected ? layoutManager : publicLayoutManager, followingSelected ? followingListView : publicListView, !followingSelected);
        });

        discoverRefresh = root.findViewById(R.id.discover_refresh);
        discoverRefresh.setOnClickListener(v -> {
            viewModel.showCachedItems();
            publicListView.scrollToPosition(0);
        });
        viewModel.restarted.observe(getViewLifecycleOwner(), restarted -> {
            discoverRefresh.setVisibility(restarted ? View.VISIBLE : View.GONE);
        });

        requestLocation = root.findViewById(R.id.request_location_access);
        requestLocation.setOnClickListener(v -> {
            requestLocationPermission();
        });

        publicFailed = root.findViewById(R.id.public_feed_load_failed);
        tapToRefresh = root.findViewById(R.id.tap_to_refresh);
        tapToRefresh.setOnClickListener(v -> {
            viewModel.refreshPublicFeed();
        });
        viewModel.publicFeedLoadFailed.observe(getViewLifecycleOwner(), failed -> {
            publicFailed.setVisibility(Boolean.TRUE.equals(failed) ? View.VISIBLE : View.GONE);
            publicListView.setVisibility(Boolean.TRUE.equals(failed) ? View.GONE : View.VISIBLE);
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
            boolean followingSelected = Boolean.TRUE.equals(viewModel.followingTabSelected.getValue());
            notifyPostsSeen(followingSelected ? layoutManager : publicLayoutManager, followingSelected ? followingListView : publicListView, !followingSelected);
        });

        return root;
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
                    sendSeenReceipt(kpvh.post);
                }
            }
        }
    }

    private void sendSeenReceipt(@NonNull Post post) {
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
            requestLocation.setVisibility(View.GONE);
            startLocationUpdates();
        } else {
            Log.d("MainFragment.onResume hasLocationPermission=false");
            requestLocation.setVisibility(View.VISIBLE);
        }
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
            requestLocation.setVisibility(View.GONE);
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

    private void updateEmptyState() {
        List<Post> postList = viewModel.postList.getValue();
        List<KatchupPost> momentList = viewModel.momentList.getLiveData().getValue();
        Post myPost = viewModel.myPost.getLiveData().getValue();
        boolean hasOtherPosts = (postList != null && !postList.isEmpty()) || (momentList != null && !momentList.isEmpty());
        boolean hasPosts = hasOtherPosts || myPost != null;
        onlyOwnPost.setVisibility(!hasOtherPosts && hasPosts ? View.VISIBLE : View.GONE);
        followingEmpty.setVisibility(hasPosts ? View.GONE : View.VISIBLE);
        followingListView.setVisibility(hasPosts ? View.VISIBLE : View.GONE);
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
        if (!followingSelected) {
            viewModel.maybeFetchInitialFeed();
        }
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
                }
            }

            @Override
            public void onCommentRetracted(@NonNull Comment comment) {
                Log.d("MainFragment content observer comment retracted " + comment);
                Post post = myPost.getLiveData().getValue();
                if (post != null && comment.postId.equals(post.id)) {
                    myPost.invalidate();
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

        private final KatchupPostsDataSource.Factory dataSourceFactory;
        final MutableLiveData<List<Post>> publicFeed = new MutableLiveData<>();
        final MutableLiveData<Boolean> followingTabSelected = new MutableLiveData<>(true);
        final LiveData<PagedList<Post>> postList;
        final ComputableLiveData<Post> myPost;
        final ComputableLiveData<List<KatchupPost>> momentList;
        final MutableLiveData<List<FollowSuggestionsResponseIq.Suggestion>> suggestedUsers = new MutableLiveData<>();
        final MutableLiveData<Boolean> publicFeedLoadFailed = new MutableLiveData<>(false);
        final MutableLiveData<Boolean> restarted = new MutableLiveData<>(false);

        private boolean publicFeedFetchInProgress;
        private long postIndex = 0;
        private List<Post> items = new ArrayList<>();
        private String lastCursor;
        private List<Post> cachedItems = new ArrayList<>();
        private Location location;
        private boolean initialPublicFeedFetched;

        public MainViewModel(@NonNull Application application) {
            super(application);

            contentDb.addObserver(contentObserver);
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

            fetchSuggestions();
        }

        private void fetchSuggestions() {
            Connection.getInstance().requestFollowSuggestions().onResponse(response -> {
                if (!response.success) {
                    Log.e("Suggestion fetch was not successful");
                } else {
                    Map<UserId, String> names = new HashMap<>();
                    List<FollowSuggestionsResponseIq.Suggestion> suggestions = new ArrayList<>();

                    for (FollowSuggestionsResponseIq.Suggestion suggestion : response.suggestions) {
                        names.put(suggestion.info.userId, suggestion.info.name);
                        suggestions.add(suggestion);
                    }

                    Comparator<FollowSuggestionsResponseIq.Suggestion> comparator = (o1, o2) -> o1.rank - o2.rank;
                    Collections.sort(suggestions, comparator);

                    ContactsDb.getInstance().updateUserNames(names);

                    suggestedUsers.postValue(suggestions);
                }
            }).onError(error -> {
                Log.e("Suggestion fetch got error", error);
            });
        }

        private void fetchPublicFeed() {
            Log.d("MainFragment.fetchPublicFeed");
            publicFeedFetchInProgress = true;
            final Double latitude = location != null ? location.getLatitude() : null;
            final Double longitude = location != null ? location.getLongitude() : null;
            Connection.getInstance().requestPublicFeed(this.lastCursor, latitude, longitude).onResponse(response -> {
                if (!response.success) {
                    Log.e("Public feed fetch was not successful");
                    publicFeedFetchInProgress = false;
                    publicFeedLoadFailed.postValue(true);
                } else {
                    lastCursor = response.cursor;
                    Log.d("Public feed last cursor updated to " + lastCursor);

                    if (response.restarted) {
                        postIndex = 0;
                    }

                    Map<UserId, String> namesMap = new HashMap<>();
                    List<Post> posts = new ArrayList<>();
                    Map<String, List<Comment>> commentMap = new HashMap<>();
                    FeedContentParser feedContentParser = new FeedContentParser(Me.getInstance());
                    String meUser = Me.getInstance().getUser();
                    for (PublicFeedItem item : response.items) {
                        try {
                            UserId publisherUserId = new UserId(Long.toString(item.getPost().getPublisherUid()));
                            Container container = Container.parseFrom(item.getPost().getPayload());
                            namesMap.put(publisherUserId, item.getPost().getPublisherName());
                            KatchupPost post = feedContentParser.parseKatchupPost(
                                    item.getPost().getId(),
                                    publisherUserId,
                                    item.getPost().getTimestamp() * 1000L,
                                    container.getKMomentContainer(),
                                    false
                            );
                            MomentInfo momentInfo = item.getPost().getMomentInfo();
                            post.timeTaken = momentInfo.getTimeTaken();
                            post.numSelfieTakes = (int) momentInfo.getNumSelfieTakes();
                            post.numTakes = (int) momentInfo.getNumTakes();
                            post.notificationId = momentInfo.getNotificationId();
                            post.notificationTimestamp = momentInfo.getNotificationTimestamp() * 1000L;
                            post.rowId = postIndex++;

                            List<Comment> comments = new ArrayList<>();
                            for (com.halloapp.proto.server.Comment protoComment : item.getCommentsList()) {
                                try {
                                    Container commentContainer = Container.parseFrom(protoComment.getPayload());
                                    String userIdStr = Long.toString(protoComment.getPublisherUid());
                                    Comment comment = feedContentParser.parseComment(
                                            protoComment.getId(),
                                            protoComment.getParentCommentId(),
                                            meUser.equals(userIdStr) ? UserId.ME : new UserId(userIdStr),
                                            protoComment.getTimestamp() * 1000L,
                                            commentContainer.getCommentContainer(),
                                            false
                                    );
                                    comments.add(comment);
                                } catch (InvalidProtocolBufferException e) {
                                    Log.e("Failed to parse container for public feed comment");
                                }
                            }
                            commentMap.put(post.id, comments);

                            post.commentCount = comments.size();
                            posts.add(post);
                        } catch (InvalidProtocolBufferException e) {
                            Log.e("Failed to parse container for public feed post");
                        }
                    }

                    if (response.restarted) {
                        restarted.postValue(true);
                        cachedItems.addAll(posts);
                    } else {
                        items.addAll(posts);
                        publicFeed.postValue(items);
                    }
                    publicFeedFetchInProgress = false;
                    publicFeedLoadFailed.postValue(false);
                    PublicContentCache.getInstance().insertContent(posts, commentMap);
                    ContactsDb.getInstance().updateUserNames(namesMap);
                }
            }).onError(error -> {
                Log.e("Failed to fetch public feed", error);
                publicFeedFetchInProgress = false;
                publicFeedLoadFailed.postValue(true);
            });
        }

        public void showCachedItems() {
            items.clear();
            items.addAll(cachedItems);
            cachedItems.clear();
            publicFeed.postValue(items);
            restarted.postValue(false);
        }

        public void maybeFetchInitialFeed() {
            if (!initialPublicFeedFetched) {
                initialPublicFeedFetched = true;
                fetchPublicFeed();
            }
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

        public void refreshPublicFeed() {
            Log.d("MainFragment.refreshPublicFeed");
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
            contentDb.removeObserver(contentObserver);
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
            public KAvatarLoader getAvatarLoader() {
                return kAvatarLoader;
            }

            @Override
            public void startActivity(Intent intent) {
                MainFragment.this.startActivity(intent);
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
}
