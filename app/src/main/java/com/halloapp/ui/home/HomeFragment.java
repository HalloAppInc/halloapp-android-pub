package com.halloapp.ui.home;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Preconditions;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.Connection;
import com.halloapp.posts.Post;
import com.halloapp.R;
import com.halloapp.posts.PostsDb;
import com.halloapp.posts.PostsImageLoader;
import com.halloapp.widget.BadgedDrawable;

import java.util.UUID;

public class HomeFragment extends Fragment {

    private PostsAdapter adapter = new PostsAdapter();
    private BadgedDrawable notificationDrawable;
    private PostsImageLoader postsImageLoader;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postsImageLoader = new PostsImageLoader(Preconditions.checkNotNull(getContext()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        postsImageLoader.destroy();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        final View root = inflater.inflate(R.layout.fragment_home, container, false);
        final RecyclerView postsView = root.findViewById(R.id.posts);

        final HomeViewModel viewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        viewModel.postList.observe(this, posts -> adapter.submitList(posts, () -> postsView.scrollToPosition(0)));

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        postsView.setLayoutManager(layoutManager);

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
                getResources().getColor(R.color.badge_text_color),
                getResources().getColor(R.color.badge_background_color),
                getResources().getDimension(R.dimen.badge));
        notificationDrawable.setBadge("2"); // testing-only
        notificationsMenuItem.setIcon(notificationDrawable);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.notifications: {
                notificationDrawable.setBadge("3"); // testing-only
                // TODO (ds): open notifications
                return true;
            }
            case R.id.add_post_text: {
                final Post post = new Post(
                        0,
                        Connection.FEED_JID.toString(),
                        "",
                        UUID.randomUUID().toString().replaceAll("-", ""),
                        "",
                        0,
                        System.currentTimeMillis(),
                        Post.POST_STATE_OUTGOING_SENDING,
                        Post.POST_TYPE_TEXT,
                        "Please read my post I made on " +
                                DateUtils.formatDateTime(getContext(), System.currentTimeMillis(),
                                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_ALL),
                        "");
                PostsDb.getInstance(Preconditions.checkNotNull(getContext())).addPost(post);
                return true;
            }
            case R.id.add_post_image: {
                final Post post = new Post(
                        0,
                        Connection.FEED_JID.toString(),
                        "",
                        UUID.randomUUID().toString().replaceAll("-", ""),
                        "",
                        0,
                        System.currentTimeMillis(),
                        Post.POST_STATE_OUTGOING_SENDING,
                        Post.POST_TYPE_IMAGE,
                        "This is a comment for my post I made on " +
                                DateUtils.formatDateTime(getContext(), System.currentTimeMillis(),
                                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_ALL),
                        "https://cdn.pixabay.com/photo/2019/09/25/15/12/chapel-4503926_640.jpg");
                PostsDb.getInstance(Preconditions.checkNotNull(getContext())).addPost(post);
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private class PostViewHolder extends RecyclerView.ViewHolder {

        final ImageView avatarView;
        final TextView nameView;
        final TextView timeView;
        final View progressView;
        final ImageView imageView;
        final TextView textView;
        final View commentButton;
        final View messageButton;

        PostViewHolder(final @NonNull View v) {
            super(v);
            avatarView = v.findViewById(R.id.avatar);
            nameView = v.findViewById(R.id.name);
            timeView = v.findViewById(R.id.time);
            progressView = v.findViewById(R.id.progress);
            imageView = v.findViewById(R.id.image);
            textView = v.findViewById(R.id.text);
            commentButton = v.findViewById(R.id.comment);
            messageButton = v.findViewById(R.id.message);
        }

        void bindTo(final @NonNull Post post) {

            avatarView.setImageResource(R.drawable.avatar_person); // testing-only
            nameView.setText(post.isOutgoing() ? nameView.getContext().getString(R.string.me) : post.senderJid); // testing-only
            if (post.state < Post.POST_STATE_OUTGOING_SENT) {
                progressView.setVisibility(View.VISIBLE);
                timeView.setVisibility(View.GONE);
            } else {
                progressView.setVisibility(View.GONE);
                timeView.setVisibility(View.VISIBLE);
                timeView.setText("1h"); // testing-only
            }
            if (post.type == Post.POST_TYPE_TEXT) {
                imageView.setVisibility(View.GONE);
            } else {
                imageView.setVisibility(View.VISIBLE);
                postsImageLoader.load(imageView, post);
            }

            textView.setText(post.text);
            if (TextUtils.isEmpty(post.text)) {
                textView.setVisibility(View.GONE);
            } else {
                textView.setVisibility(View.VISIBLE);
            }

            commentButton.setOnClickListener(v -> {
                // TODO (ds): start comment activity
            });
            messageButton.setOnClickListener(v -> {
                // TODO (ds): start message activity
            });
            nameView.setOnClickListener(v -> PostsDb.getInstance(Preconditions.checkNotNull(getContext())).deletePost(post)); // testing-only
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

        @NonNull
        @Override
        public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
            holder.bindTo(Preconditions.checkNotNull(getItem(position)));
        }
    }
}