package com.halloapp.ui.home;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.LongSparseArray;
import androidx.core.util.Preconditions;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.halloapp.R;
import com.halloapp.contacts.ContactNameLoader;
import com.halloapp.media.MediaUtils;
import com.halloapp.posts.Media;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;
import com.halloapp.posts.PostsImageLoader;
import com.halloapp.ui.CommentsActivity;
import com.halloapp.ui.PostComposerActivity;
import com.halloapp.util.Log;
import com.halloapp.util.TimeUtils;
import com.halloapp.widget.BadgedDrawable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.relex.circleindicator.CircleIndicator;

public class HomeFragment extends Fragment {

    private static final int REQUEST_CODE_PICK_IMAGE = 1;
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 2;

    private final PostsAdapter adapter = new PostsAdapter();
    private BadgedDrawable notificationDrawable;
    private PostsImageLoader postsImageLoader;
    private ContactNameLoader contactNameLoader;

    private LongSparseArray<Integer> mediaPagerPositionMap = new LongSparseArray<>();

    private long refreshTimestampsTime = Long.MAX_VALUE;
    private final Runnable refreshTimestampsRunnable = () -> {
        Log.v("HomeFragment: refreshing timestamps at " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US).format(new Date(System.currentTimeMillis())));
        refreshTimestampsTime = Long.MAX_VALUE;
        adapter.notifyDataSetChanged();
    };
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("HomeFragment: onCreate");
        postsImageLoader = new PostsImageLoader(Preconditions.checkNotNull(getContext()));
        contactNameLoader = new ContactNameLoader(Preconditions.checkNotNull(getContext()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("HomeFragment: onDestroy");
        postsImageLoader.destroy();
        contactNameLoader.destroy();
        mainHandler.removeCallbacks(refreshTimestampsRunnable);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        final View root = inflater.inflate(R.layout.fragment_home, container, false);
        final RecyclerView postsView = root.findViewById(R.id.posts);
        final View emptyView = root.findViewById(android.R.id.empty);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        postsView.setLayoutManager(layoutManager);

        final HomeViewModel viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        viewModel.postList.observe(this, posts -> adapter.submitList(posts, () -> {
            final View childView = layoutManager.getChildAt(0);
            final boolean scrolled = childView == null || !(childView.getTop() == 0 && layoutManager.getPosition(childView) == 0);
            if (viewModel.checkPendingOutgoing() || !scrolled) {
                postsView.scrollToPosition(0);
            } else if (viewModel.checkPendingIncoming()) {
                postsView.smoothScrollBy(0, -getResources().getDimensionPixelSize(R.dimen.incoming_post_scroll_up));
            }
            emptyView.setVisibility(posts.size() == 0 ? View.VISIBLE : View.GONE);
        }));

        final float scrolledElevation = getResources().getDimension(R.dimen.action_bar_elevation);
        postsView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                final View childView = layoutManager.getChildAt(0);
                final boolean scrolled = childView == null || !(childView.getTop() == 0 && layoutManager.getPosition(childView) == 0);
                final AppCompatActivity activity = Preconditions.checkNotNull((AppCompatActivity)getActivity());
                final ActionBar actionBar = Preconditions.checkNotNull(activity.getSupportActionBar());
                final float elevation = scrolled ? scrolledElevation : 0;
                if (actionBar.getElevation() != elevation) {
                    actionBar.setElevation(elevation);
                }
            }
        });

        Preconditions.checkNotNull((SimpleItemAnimator) postsView.getItemAnimator()).setSupportsChangeAnimations(false);

        postsView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.home_menu, menu);
        final MenuItem notificationsMenuItem = menu.findItem(R.id.notifications);
        notificationDrawable = new BadgedDrawable(
                Preconditions.checkNotNull(getContext()),
                notificationsMenuItem.getIcon(),
                getResources().getColor(R.color.badge_text),
                getResources().getColor(R.color.badge_background),
                getResources().getColor(R.color.window_background),
                getResources().getDimension(R.dimen.badge));
        notificationsMenuItem.setIcon(notificationDrawable);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.notifications: {
                // testing-only
                String badge = notificationDrawable.getBadge();
                if (TextUtils.isEmpty(badge)) {
                    notificationDrawable.setBadge("1");
                } else {
                    int newBadge = (Integer.parseInt(badge) + 1) % 10;
                    notificationDrawable.setBadge(newBadge == 0 ? null : Integer.toString(newBadge));
                }
                // TODO (ds): open notifications
                return true;
            }
            case R.id.add_post_text: {
                startActivity(new Intent(getContext(), PostComposerActivity.class));
                return true;
            }
            case R.id.add_post_gallery: {
                getImageFromGallery();
                return true;
            }
            case R.id.add_post_camera: {
                getImageFromCamera();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onActivityResult(final int request, final int result, final Intent data) {
        super.onActivityResult(request, result, data);
        switch (request) {
            case REQUEST_CODE_PICK_IMAGE: {
                if (result == Activity.RESULT_OK) {
                    if (data == null) {
                        Log.e("HomeFragment.onActivityResult.REQUEST_CODE_PICK_IMAGE: no data");
                        Toast.makeText(getContext(), R.string.bad_image, Toast.LENGTH_SHORT).show();
                    } else {
                        final ArrayList<Uri> uris = new ArrayList<>();
                        final ClipData clipData = data.getClipData();
                        if (clipData != null) {
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                uris.add(clipData.getItemAt(i).getUri());
                            }
                        } else {
                            final Uri uri = data.getData();
                            if (uri != null) {
                                uris.add(uri);
                            }
                        }
                        if (!uris.isEmpty()) {
                            final Intent intent = new Intent(getContext(), PostComposerActivity.class);
                            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                            startActivity(intent);
                        } else {
                            Log.e("HomeFragment.onActivityResult.REQUEST_CODE_PICK_IMAGE: no uri");
                            Toast.makeText(getContext(), R.string.bad_image, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
            }
            case REQUEST_CODE_CAPTURE_IMAGE: {
                if (result == Activity.RESULT_OK) {
                    final Intent intent = new Intent(getContext(), PostComposerActivity.class);
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
                            new ArrayList<>(Collections.singleton(MediaUtils.getImageCaptureUri(Preconditions.checkNotNull(getContext())))));
                    startActivity(intent);
                }
                break;
            }
        }
    }

    private void getImageFromGallery() {
        final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        if (Build.VERSION.SDK_INT >= 26) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toURI()); // TODO (ds): doesn't seem to work properly, need to investigate
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    private void getImageFromCamera() {
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, MediaUtils.getImageCaptureUri(Preconditions.checkNotNull(getContext())));
        startActivityForResult(intent, REQUEST_CODE_CAPTURE_IMAGE);
    }

    private void scheduleTimestampRefresh(long postTimestamp) {
        long refreshTime = TimeUtils.getRefreshTime(postTimestamp);
        if (refreshTime < refreshTimestampsTime) {
            refreshTimestampsTime = refreshTime;
            Log.v("HomeFragment: will refresh timestamps at " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US).format(new Date(refreshTimestampsTime)));
            mainHandler.removeCallbacks(refreshTimestampsRunnable);
            mainHandler.postDelayed(refreshTimestampsRunnable, refreshTimestampsTime - System.currentTimeMillis());
        }
    }

    private class PostViewHolder extends RecyclerView.ViewHolder {

        final ImageView avatarView;
        final TextView nameView;
        final TextView timeView;
        final View progressView;
        final ViewPager mediaPagerView;
        final CircleIndicator mediaPagerIndicator;
        final TextView textView;
        final View commentButton;
        final View messageButton;
        final View commentsIndicator;

        PostViewHolder(final @NonNull View v) {
            super(v);
            avatarView = v.findViewById(R.id.avatar);
            nameView = v.findViewById(R.id.name);
            timeView = v.findViewById(R.id.time);
            progressView = v.findViewById(R.id.progress);
            mediaPagerView = v.findViewById(R.id.media_pager);
            mediaPagerIndicator = v.findViewById(R.id.media_pager_indicator);
            textView = v.findViewById(R.id.text);
            commentButton = v.findViewById(R.id.comment);
            messageButton = v.findViewById(R.id.message);
            commentsIndicator = v.findViewById(R.id.comments_indicator);
        }

        void bindTo(final @NonNull Post post) {

            avatarView.setImageResource(R.drawable.avatar_person); // testing-only
            if (post.isOutgoing()) {
                nameView.setText(nameView.getContext().getString(R.string.me));
            } else {
                contactNameLoader.load(nameView, post.senderUserId);
            }
            if (!post.transferred) {
                progressView.setVisibility(View.VISIBLE);
                timeView.setVisibility(View.GONE);
            } else {
                progressView.setVisibility(View.GONE);
                timeView.setVisibility(View.VISIBLE);
                timeView.setText(TimeUtils.formatTimeDiff(timeView.getContext(), System.currentTimeMillis() - post.timestamp));
                scheduleTimestampRefresh(post.timestamp);
            }
            if (post.media.isEmpty()) {
                mediaPagerView.setVisibility(View.GONE);
                mediaPagerIndicator.setVisibility(View.GONE);
            } else {
                mediaPagerView.setVisibility(View.VISIBLE);
                final PostMediaPagerAdapter mediaPagerAdapter = new PostMediaPagerAdapter(post.media);
                mediaPagerView.setAdapter(mediaPagerAdapter);
                mediaPagerView.setPageMargin(Preconditions.checkNotNull(getContext()).getResources().getDimensionPixelSize(R.dimen.media_pager_margin));
                mediaPagerView.clearOnPageChangeListeners();
                if (post.media.size() > 1) {
                    mediaPagerIndicator.setVisibility(View.VISIBLE);
                    mediaPagerIndicator.setViewPager(mediaPagerView);
                } else {
                    mediaPagerIndicator.setVisibility(View.GONE);
                }
                mediaPagerView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    }

                    @Override
                    public void onPageSelected(int position) {
                        if (position == 0) {
                            mediaPagerPositionMap.remove(post.rowId);
                        } else {
                            mediaPagerPositionMap.put(post.rowId, position);
                        }
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {
                    }
                });
                final Integer selPos = mediaPagerPositionMap.get(post.rowId);
                mediaPagerView.setCurrentItem(selPos == null ? 0 : selPos);
            }

            textView.setText(post.text);
            if (TextUtils.isEmpty(post.text)) {
                textView.setVisibility(View.GONE);
            } else {
                textView.setVisibility(View.VISIBLE);
            }

            if (post.unseenCommentCount > 0) {
                commentsIndicator.setVisibility(View.VISIBLE);
                commentsIndicator.setBackgroundResource(R.drawable.new_comments_indicator);
            } else if (post.commentCount > 0) {
                commentsIndicator.setVisibility(View.VISIBLE);
                commentsIndicator.setBackgroundResource(R.drawable.old_comments_indicator);
            } else {
                commentsIndicator.setVisibility(View.GONE);
            }

            commentButton.setOnClickListener(v -> {
                final Intent intent = new Intent(getContext(), CommentsActivity.class);
                intent.putExtra(CommentsActivity.EXTRA_POST_SENDER_USER_ID, post.senderUserId.rawId());
                intent.putExtra(CommentsActivity.EXTRA_POST_ID, post.postId);
                startActivity(intent);

            });
            messageButton.setOnClickListener(v -> {
                // TODO (ds): start message activity
            });
        }


        private class PostMediaPagerAdapter extends PagerAdapter {

            final List<Media> media;

            PostMediaPagerAdapter(@NonNull List<Media> media) {
                this.media = media;
            }

            @Override
            public @NonNull Object instantiateItem(@NonNull ViewGroup container, int position) {
                final View view = getLayoutInflater().inflate(R.layout.media_pager_item, container, false);
                final ImageView imageView = view.findViewById(R.id.image);
                final Media mediaItem = media.get(position);
                if (mediaItem.height > mediaItem.width) {
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
                postsImageLoader.load(imageView, mediaItem);
                container.addView(view);
                return view;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object view) {
                container.removeView((View) view);
            }

            public void finishUpdate(@NonNull ViewGroup container) {
                container.requestLayout();
            }

            @Override
            public int getCount() {
                return media.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }
        }
    }

    private static final DiffUtil.ItemCallback<Post> DIFF_CALLBACK = new DiffUtil.ItemCallback<Post>() {

        @Override
        public boolean areItemsTheSame(Post oldItem, Post newItem) {
            // The ID property identifies when items are the same.
            return oldItem.rowId == newItem.rowId;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Post oldItem, @NonNull Post newItem) {
            return oldItem.equals(newItem);
        }
    };

    private class PostsAdapter extends PagedListAdapter<Post, PostViewHolder> {

        PostsAdapter() {
            super(DIFF_CALLBACK);
            setHasStableIds(true);
        }

        public long getItemId(int position) {
            return Preconditions.checkNotNull(getItem(position)).rowId;
        }

        @Override
        public @NonNull PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
            holder.bindTo(Preconditions.checkNotNull(getItem(position)));
        }
    }
}