package com.halloapp.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.halloapp.posts.PostsImageLoader;
import com.halloapp.ui.PostComposerActivity;
import com.halloapp.widget.BadgedDrawable;

public class HomeFragment extends Fragment {

    private static final int REQUEST_CODE_PICK_IMAGE = 1;

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
                getResources().getColor(R.color.badge_text),
                getResources().getColor(R.color.badge_background),
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
                startActivity(new Intent(getContext(), PostComposerActivity.class));
                return true;
            }
            case R.id.add_post_image: {
                pickImage();
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
                        Toast.makeText(getContext(), R.string.bad_image, Toast.LENGTH_SHORT).show();
                    } else {
                        final Uri uri = data.getData();
                        if (uri != null) {
                            final Intent intent = new Intent(getContext(), PostComposerActivity.class);
                            intent.setData(uri);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getContext(), R.string.bad_image, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
            }
        }
    }

    private void pickImage() {
        final Intent pickIntent = new Intent(Intent.ACTION_PICK);
        pickIntent.setDataAndType(android.provider.MediaStore.Video.Media.INTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(pickIntent, REQUEST_CODE_PICK_IMAGE);
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