package com.halloapp.ui.archive;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.util.Size;
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
import com.halloapp.util.logs.Log;
import com.halloapp.widget.LimitingTextView;
import com.halloapp.widget.SquareImageView;

import java.io.IOException;

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

    public class ArchiveSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {

        RecyclerView recyclerView;

        public ArchiveSpanSizeLookup(View view) {
            this.recyclerView = (RecyclerView) view;
        }

        private ArchiveRecyclerAdapter getAdapter() {
            return (ArchiveRecyclerAdapter) recyclerView.getAdapter();
        }
        private GridLayoutManager getLayoutManager() {
            return (GridLayoutManager) recyclerView.getLayoutManager();
        }
        @Override
        public int getSpanSize(int position) {
            final ArchiveRecyclerAdapter adapter = getAdapter();
            final int count = getLayoutManager().getSpanCount();

            if (adapter.getItemViewType(position) == ArchiveItem.TYPE_HEADER) {
                return count;
            } else {
                return 1;
            }
        }
    }

    public class ArchiveRecyclerViewHolder extends ViewHolderWithLifecycle {

        TextView titleView;
        LimitingTextView name;
        TextContentLoader textContentLoader;

        FrameLayout pictureFrameLayout;
        TextView durationText;
        SquareImageView imageView;

        public ArchiveRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.title);
            name = itemView.findViewById(R.id.archive_item_name);
            pictureFrameLayout = itemView.findViewById(R.id.archive_thumbnail_frame);
            durationText = itemView.findViewById(R.id.archive_duration);
            imageView = itemView.findViewById(R.id.archive_thumbnail);

            textContentLoader = new TextContentLoader(itemView.getContext());
        }
        public void bindTo(ArchiveItem archiveItem) {
            if (archiveItem.type == ArchiveItem.TYPE_HEADER) {
                titleView.setText(archiveItem.title);
            } else {
                Post post = archiveItem.post;
                View.OnClickListener clickListener = view -> {
                    Intent intent = new Intent(view.getContext(), PostContentActivity.class);
                    intent.putExtra(PostContentActivity.EXTRA_POST_ID, post.id);
                    intent.putExtra(PostContentActivity.EXTRA_IS_ARCHIVED, true);
                    startActivity(intent);
                };

                name.setText(getString(R.string.post_retracted_by_me));
                itemView.setOnClickListener(clickListener);
                if (post.text != null) {
                    name.setLineLimit(TEXTVIEW_LINE_LIMIT);
                    name.setOnClickListener(clickListener);
                    pictureFrameLayout.setVisibility(View.INVISIBLE);
                    textContentLoader.load(name, post);
                }
                if (post.media.size() > 0) {
                    Media media = post.media.get(0);
                    pictureFrameLayout.setVisibility(View.VISIBLE);
                    pictureFrameLayout.setOnClickListener(clickListener);
                    if (media.type == Media.MEDIA_TYPE_IMAGE) {
                        mediaThumbnailLoader.load(imageView, post.media.get(0));
                    } else if (media.type == Media.MEDIA_TYPE_VIDEO) {
                        bgWorkers.execute(() -> {
                            Long duration = MediaUtils.getVideoDuration(media.file);
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
    }

    public class ArchiveRecyclerAdapter extends PagedListAdapter<ArchiveItem, ArchiveRecyclerViewHolder> {

        protected ArchiveRecyclerAdapter() {
            super(new DiffUtil.ItemCallback<ArchiveItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull ArchiveItem oldItem, @NonNull ArchiveItem newItem) {
                    if (oldItem.type != newItem.type) {
                        return false;
                    } else if (oldItem.type == ArchiveItem.TYPE_ITEM) {
                        return oldItem.post.equals(newItem.post);
                    } else {
                        return oldItem.title.equals(newItem.title);
                    }
                }

                @Override
                public boolean areContentsTheSame(@NonNull ArchiveItem oldItem, @NonNull ArchiveItem newItem) {
                    if (oldItem.type != newItem.type) {
                        return false;
                    } else if (oldItem.type == ArchiveItem.TYPE_ITEM) {
                        return oldItem.post.equals(newItem.post);
                    } else {
                        return oldItem.title.equals(newItem.title);
                    }
                }
            });
        }

        @NonNull
        @Override
        public ArchiveRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch(viewType) {
                case ArchiveItem.TYPE_HEADER:
                    return new ArchiveRecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.archive_header, parent, false));
                case ArchiveItem.TYPE_ITEM:
                    return new ArchiveRecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.archive_item, parent, false));
                default:
                    throw new java.lang.IllegalArgumentException("Can't get layout for unknown archive item type");
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ArchiveRecyclerViewHolder holder, int position) {
            holder.bindTo(getItem(position));
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).type;
        }
    }
}
