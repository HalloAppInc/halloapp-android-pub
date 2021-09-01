package com.halloapp.ui.archive;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.media.MediaUtils;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.PostContentActivity;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.LimitingTextView;
import com.halloapp.widget.SquareImageView;

public class ArchiveActivity extends HalloActivity  {

    private RecyclerView archiveRecyclerView;
    private ArchiveViewModel viewModel;
    private ArchiveRecyclerAdapter adapter;
    private LinearLayout emptyListView;

    private static final int GRID_SPAN = 3;
    private static final int TEXTVIEW_LINE_LIMIT = 5;

    private final BgWorkers bgWorkers = BgWorkers.getInstance();

    private MediaThumbnailLoader mediaThumbnailLoader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(getResources().getDimension(R.dimen.action_bar_elevation));
        }

        emptyListView = findViewById(R.id.archive_empty);
        emptyListView.setVisibility(View.GONE);

        archiveRecyclerView = findViewById(R.id.archive_recycler);

        GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), GRID_SPAN);
        layoutManager.setSpanSizeLookup(new ArchiveSpanSizeLookup(archiveRecyclerView));
        archiveRecyclerView.setLayoutManager(layoutManager);

        viewModel = new ViewModelProvider(this).get(ArchiveViewModel.class);
        adapter = new ArchiveRecyclerAdapter();

        viewModel.archiveItemList.observe(this, postList -> {
            adapter.submitList(postList);
            if (postList.isEmpty()) {
                emptyListView.setVisibility(View.VISIBLE);
            } else {
                emptyListView.setVisibility(View.GONE);
            }
        });
        archiveRecyclerView.setAdapter(adapter);

        mediaThumbnailLoader = new MediaThumbnailLoader(this, 2 * (int) getResources().getDimension(R.dimen.media_gallery_grid_size));
    }

    public static class ArchiveSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {

        final RecyclerView recyclerView;

        public ArchiveSpanSizeLookup(View view) {
            this.recyclerView = (RecyclerView) view;
        }

        @Override
        public int getSpanSize(int position) {
            return 1;
        }
    }

    public class ArchiveRecyclerViewHolder extends ViewHolderWithLifecycle {

        final TextView titleView;
        final LimitingTextView name;
        final TextContentLoader textContentLoader;

        final FrameLayout pictureFrameLayout;
        final TextView durationText;
        final SquareImageView imageView;

        Post post;

        public ArchiveRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.title);
            name = itemView.findViewById(R.id.archive_item_name);
            pictureFrameLayout = itemView.findViewById(R.id.archive_thumbnail_frame);
            durationText = itemView.findViewById(R.id.archive_duration);
            imageView = itemView.findViewById(R.id.archive_thumbnail);

            textContentLoader = new TextContentLoader();

            View.OnClickListener clickListener = view -> {
                Intent intent = new Intent(view.getContext(), PostContentActivity.class);
                intent.putExtra(PostContentActivity.EXTRA_POST_ID, post.id);
                intent.putExtra(PostContentActivity.EXTRA_IS_ARCHIVED, true);
                startActivity(intent);
            };
            itemView.setOnClickListener(clickListener);
            name.setOnClickListener(clickListener);
            pictureFrameLayout.setOnClickListener(clickListener);
        }

        public void bindTo(@NonNull Post post) {
            this.post = post;
            name.setText(getString(R.string.post_retracted_by_me));
            if (post.text != null) {
                name.setLineLimit(TEXTVIEW_LINE_LIMIT);
                pictureFrameLayout.setVisibility(View.INVISIBLE);
                textContentLoader.load(name, post);
            }
            if (post.media.size() > 0) {
                Media media = post.media.get(0);
                pictureFrameLayout.setVisibility(View.VISIBLE);
                if (media.type == Media.MEDIA_TYPE_IMAGE) {
                    mediaThumbnailLoader.load(imageView, post.media.get(0));
                } else if (media.type == Media.MEDIA_TYPE_VIDEO) {
                    bgWorkers.execute(() -> {
                        long duration = MediaUtils.getVideoDuration(media.file);
                        durationText.post(() -> {
                            durationText.setVisibility(View.VISIBLE);
                            durationText.setText(DateUtils.formatElapsedTime(duration / 1000));
                        });
                    });
                    mediaThumbnailLoader.load(imageView, media);
                }
                name.setText(R.string.couldnt_load_thumbnail);
            }
        }
    }

    public class ArchiveRecyclerAdapter extends PagedListAdapter<Post, ArchiveRecyclerViewHolder> {

        protected ArchiveRecyclerAdapter() {
            super(new DiffUtil.ItemCallback<Post>() {
                @Override
                public boolean areItemsTheSame(@NonNull Post oldPost, @NonNull Post newPost) {
                    return oldPost == newPost;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Post oldPost, @NonNull Post newPost) {
                    return oldPost.equals(newPost);
                }
            });
        }

        @NonNull
        @Override
        public ArchiveRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ArchiveRecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.archive_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ArchiveRecyclerViewHolder holder, int position) {
            holder.bindTo(Preconditions.checkNotNull(getItem(position)));
        }
    }
}
