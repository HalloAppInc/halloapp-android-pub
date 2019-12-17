package com.halloapp.ui.home;

import android.os.Bundle;
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
import androidx.core.util.Preconditions;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.posts.Post;
import com.halloapp.R;
import com.halloapp.posts.PostsDb;
import com.halloapp.widget.BadgedDrawable;

import java.util.UUID;

public class HomeFragment extends Fragment {

    private PostsAdapter adapter = new PostsAdapter();
    private BadgedDrawable notificationDrawable;

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
            case R.id.add_post: {
                final Post post = new Post(
                        0,
                        "16505553000@s.halloapp.net",
                        "",
                        UUID.randomUUID().toString().replaceAll("-", ""),
                        "",
                        0,
                        System.currentTimeMillis(),
                        Post.POST_STATE_SENT,
                        Post.POST_TYPE_TEXT,
                        "This is a comment for my post I made on " +
                                DateUtils.formatDateTime(getContext(), System.currentTimeMillis(),
                                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_ALL),
                        "");
                PostsDb.getInstance(Preconditions.checkNotNull(getContext())).addPost(post);
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {

        final ImageView avatarView;
        final TextView nameView;
        final TextView timeView;
        final ImageView imageView;
        final TextView captionView;
        final View commentView;
        final View messageView;

        PostViewHolder(final @NonNull View v) {
            super(v);
            avatarView = v.findViewById(R.id.avatar);
            nameView = v.findViewById(R.id.name);
            timeView = v.findViewById(R.id.time);
            imageView = v.findViewById(R.id.image);
            captionView = v.findViewById(R.id.caption);
            commentView = v.findViewById(R.id.comment);
            messageView = v.findViewById(R.id.message);
        }

        void bindTo(final @NonNull Post post) {
            captionView.setText(post.text);

            avatarView.setImageResource(R.drawable.avatar_person); // testing-only
            nameView.setText("Ded Pihtov"); // testing-only
            timeView.setText("1h"); // testing-only
            imageView.setImageResource(post.rowId % 3 == 0 ? R.drawable.test0 : (post.rowId % 3 == 1 ? R.drawable.test1 : R.drawable.test2)); // testing-only

            commentView.setOnClickListener(v -> {
                // TODO (ds): start comment activity
            });
            messageView.setOnClickListener(v -> {
                // TODO (ds): start message activity
            });
        }
    }

    static class PostsAdapter extends PagedListAdapter<Post, PostViewHolder> {

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