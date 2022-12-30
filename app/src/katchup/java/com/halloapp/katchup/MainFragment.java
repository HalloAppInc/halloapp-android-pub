package com.halloapp.katchup;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
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

import com.halloapp.Constants;
import com.halloapp.MainActivity;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.ContentDb;
import com.halloapp.content.KatchupPost;
import com.halloapp.content.MomentPost;
import com.halloapp.content.Post;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.katchup.avatar.KAvatarLoader;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.proto.server.MomentNotification;
import com.halloapp.ui.HalloFragment;
import com.halloapp.ui.HeaderFooterAdapter;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.moments.MomentsStackLayout;
import com.halloapp.ui.posts.PostListDiffer;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.FollowSuggestionsResponseIq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainFragment extends HalloFragment {

    private MediaThumbnailLoader mediaThumbnailLoader;
    private ContactLoader contactLoader = new ContactLoader();
    private final KAvatarLoader kAvatarLoader = KAvatarLoader.getInstance();

    private MainViewModel viewModel;
    private ViewGroup parentViewGroup;
    private RecyclerView listView;
    private PostAdapter adapter;
    private TextView followingButton;
    private TextView nearbyButton;
    private View myPostHeader;
    private View followingEmpty;
    private View postYourOwn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);

        parentViewGroup = container;

        final Point point = new Point();
        requireActivity().getWindowManager().getDefaultDisplay().getSize(point);
        mediaThumbnailLoader = new MediaThumbnailLoader(requireContext(), Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));

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
        ImageView avatarView = root.findViewById(R.id.avatar);
        kAvatarLoader.load(avatarView, UserId.ME);

        listView = root.findViewById(R.id.recycler_view);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireContext());
        listView.setLayoutManager(layoutManager);
        adapter = new PostAdapter();
        listView.setAdapter(adapter);

        followingEmpty = root.findViewById(R.id.following_empty);
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
        followingButton.setOnClickListener(v -> setFollowingSelected(true));
        nearbyButton = root.findViewById(R.id.nearby);
        nearbyButton.setOnClickListener(v -> setFollowingSelected(false));
        setFollowingSelected(true);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        viewModel.postList.observe(getViewLifecycleOwner(), posts -> {
            Log.d("MainFragment got new post list " + posts);
            adapter.submitList(posts, () -> {
                updateEmptyState();
            });
        });

        adapter.addMomentsHeader();
        viewModel.momentList.getLiveData().observe(getViewLifecycleOwner(), moments -> {
            adapter.setMoments(moments);
            updateEmptyState();
        });

        // TODO(jack): Determine why onCreateView is receiving a null container, which causes the layout params to not be set
        myPostHeader = adapter.addHeader(R.layout.header_my_post);
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
                adapter.hideHeader();
            } else {
                adapter.showHeader();
                mediaThumbnailLoader.load(myPostHeader.findViewById(R.id.image), post.media.get(1));
                mediaThumbnailLoader.load(myPostHeader.findViewById(R.id.selfie_preview), post.media.get(0));
                View selfieContainer = myPostHeader.findViewById(R.id.selfie_container);
                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) selfieContainer.getLayoutParams();
                float posX = ((KatchupPost) post).selfieX;
                float posY = ((KatchupPost) post).selfieY;
                layoutParams.horizontalBias = posX;
                layoutParams.verticalBias = posY;
                selfieContainer.setLayoutParams(layoutParams);
            }
            updateEmptyState();
        });

        viewModel.suggestedUsers.observe(getViewLifecycleOwner(), users -> {
            if (users.size() > 0) {
                ImageView avatar = root.findViewById(R.id.suggested_avatar_1);
                FollowSuggestionsResponseIq.Suggestion suggestion = users.get(0);
                kAvatarLoader.load(avatar, suggestion.info.userId, suggestion.info.avatarId);
                avatar.setOnClickListener(v -> startActivity(ViewKatchupProfileActivity.viewProfile(requireContext(), suggestion.info.userId)));
            }
            if (users.size() > 1) {
                ImageView avatar = root.findViewById(R.id.suggested_avatar_2);
                FollowSuggestionsResponseIq.Suggestion suggestion = users.get(1);
                kAvatarLoader.load(avatar, suggestion.info.userId, suggestion.info.avatarId);
                avatar.setOnClickListener(v -> startActivity(ViewKatchupProfileActivity.viewProfile(requireContext(), suggestion.info.userId)));
            }
            if (users.size() > 2) {
                ImageView avatar = root.findViewById(R.id.suggested_avatar_3);
                FollowSuggestionsResponseIq.Suggestion suggestion = users.get(2);
                kAvatarLoader.load(avatar, suggestion.info.userId, suggestion.info.avatarId);
                avatar.setOnClickListener(v -> startActivity(ViewKatchupProfileActivity.viewProfile(requireContext(), suggestion.info.userId)));
            }
        });

        View findPeople = root.findViewById(R.id.find_people);
        findPeople.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) getActivity();
            activity.previousScreen();
        });

        return root;
    }

    private void updateEmptyState() {
        List<Post> postList = viewModel.postList.getValue();
        List<KatchupPost> momentList = viewModel.momentList.getLiveData().getValue();
        Post myPost = viewModel.myPost.getLiveData().getValue();
        boolean hasPosts = (postList != null && !postList.isEmpty()) || (momentList != null && !momentList.isEmpty()) || myPost != null;
        followingEmpty.setVisibility(hasPosts ? View.GONE : View.VISIBLE);
        listView.setVisibility(hasPosts ? View.VISIBLE : View.GONE);
    }

    private void setFollowingSelected(boolean followingSelected) {
        int selectedTextColor = getResources().getColor(R.color.white);
        int unselectedTextColor = getResources().getColor(R.color.black);
        Drawable selectedBackgroundDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.selected_feed_type_background);
        Drawable unselectedBackgroundDrawable = null;
        followingButton.setBackground(followingSelected ? selectedBackgroundDrawable : unselectedBackgroundDrawable);
        followingButton.setTextColor(followingSelected ? selectedTextColor : unselectedTextColor);
        nearbyButton.setBackground(followingSelected ? unselectedBackgroundDrawable : selectedBackgroundDrawable);
        nearbyButton.setTextColor(followingSelected ? unselectedTextColor : selectedTextColor);
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
            public void onIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId, @Nullable GroupId groupId) {
                Log.d("MainFragment content observer post marked seen " + postId);
                dataSourceFactory.invalidateLatestDataSource();
                momentList.invalidate();
            }
        };

        private final KatchupPostsDataSource.Factory dataSourceFactory;
        final LiveData<PagedList<Post>> postList;
        final ComputableLiveData<Post> myPost;
        final ComputableLiveData<List<KatchupPost>> momentList;
        final MutableLiveData<List<FollowSuggestionsResponseIq.Suggestion>> suggestedUsers = new MutableLiveData<>();

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
                    return contentDb.getPost(unlockingPost);
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

                    Comparator<FollowSuggestionsResponseIq.Suggestion> comparator = (o1, o2) -> o2.rank - o1.rank;
                    Collections.sort(suggestions, comparator);

                    ContactsDb.getInstance().updateUserNames(names);

                    suggestedUsers.postValue(suggestions);
                }
            }).onError(error -> {
                Log.e("Suggestion fetch got error", error);
            });
        }

        @Override
        protected void onCleared() {
            contentDb.removeObserver(contentObserver);
        }
    }

    private class PostAdapter extends HeaderFooterAdapter<Post> {

        private final PostListDiffer postListDiffer;

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
            public KAvatarLoader getAvatarLoader() {
                return kAvatarLoader;
            }

            @Override
            public void startActivity(Intent intent) {
                MainFragment.this.startActivity(intent);
            }
        };

        public PostAdapter() {
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

            setHasStableIds(true);

            final ListUpdateCallback listUpdateCallback = createUpdateCallback();

            postListDiffer = new PostListDiffer(listUpdateCallback);
            setDiffer(postListDiffer);
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
                ((KatchupPostViewHolder) holder).bindTo(post, false);
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
