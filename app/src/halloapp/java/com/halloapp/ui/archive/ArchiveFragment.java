package com.halloapp.ui.archive;

import android.app.ActivityOptions;
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
import com.halloapp.ui.HalloFragment;
import com.halloapp.ui.MediaPagerAdapter;
import com.halloapp.ui.PostContentActivity;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.util.Preconditions;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.widget.SquareImageView;
import com.halloapp.widget.LimitingTextView;

import java.io.File;

public class ArchiveFragment extends HalloFragment {

    private RecyclerView archiveRecyclerView;
    private ArchiveViewModel viewModel;
    private ArchiveRecyclerAdapter adapter;
    private LinearLayout emptyListView;

    private static final int GRID_SPAN = 3;
    private static final int TEXTVIEW_LINE_LIMIT = 5;

    private MediaThumbnailLoader mediaThumbnailLoader;
    private DurationLoader durationLoader;

    public static ArchiveFragment newInstance() {
        return new ArchiveFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaThumbnailLoader = new MediaThumbnailLoader(requireContext(), 2 * (int) getResources().getDimension(R.dimen.media_gallery_grid_size));
        durationLoader = new DurationLoader();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.activity_archive, container, false);

        emptyListView = root.findViewById(R.id.archive_empty);
        emptyListView.setVisibility(View.GONE);

        archiveRecyclerView = root.findViewById(R.id.archive_recycler);

        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), GRID_SPAN);
        layoutManager.setSpanSizeLookup(new ArchiveActivity.ArchiveSpanSizeLookup(archiveRecyclerView));
        archiveRecyclerView.setLayoutManager(layoutManager);

        viewModel = new ViewModelProvider(requireActivity()).get(ArchiveViewModel.class);
        adapter = new ArchiveRecyclerAdapter();

        viewModel.archiveItemList.observe(getViewLifecycleOwner(), postList -> {
            adapter.submitList(postList);
            if (postList.isEmpty()) {
                emptyListView.setVisibility(View.VISIBLE);
            } else {
                emptyListView.setVisibility(View.GONE);
            }
        });
        archiveRecyclerView.setAdapter(adapter);
        return root;
    }

    public static class DurationLoader extends ViewDataLoader<TextView, Long, File> {
        public void load(TextView view, File file) {
            final ViewDataLoader.Displayer<TextView, Long> displayer = new Displayer<TextView, Long>() {
                @Override
                public void showResult(@NonNull TextView view, @Nullable @org.jetbrains.annotations.Nullable Long result) {
                    if (result != null) {
                        view.setText(DateUtils.formatElapsedTime(result / 1000));
                    }
                }

                @Override
                public void showLoading(@NonNull TextView view) {
                    view.setText("");
                }
            };

            load(view, () -> MediaUtils.getVideoDuration(file), displayer, file, null);
        }
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
                Intent intent = PostContentActivity.openArchived(view.getContext(), post.id);

                ActivityOptions options;
                if (post.media.size() > 0) {
                    imageView.setTransitionName(MediaPagerAdapter.getTransitionName(post.id, 0));
                    options = ActivityOptions.makeSceneTransitionAnimation(requireActivity(), imageView, imageView.getTransitionName());
                } else {
                    options = ActivityOptions.makeSceneTransitionAnimation(requireActivity());
                }

                startActivity(intent, options.toBundle());
            };
            itemView.setOnClickListener(clickListener);
            name.setOnClickListener(clickListener);
            pictureFrameLayout.setOnClickListener(clickListener);
        }

        public void bindTo(@NonNull Post post) {
            this.post = post;
            durationLoader.cancel(durationText);
            name.setText(getString(R.string.post_retracted_by_me));
            if (post.text != null) {
                name.setLineLimit(TEXTVIEW_LINE_LIMIT);
                pictureFrameLayout.setVisibility(View.INVISIBLE);
                textContentLoader.load(name, post);
            }
            if (post.media.size() > 0) {
                Media media = post.media.get(0);
                pictureFrameLayout.setVisibility(View.VISIBLE);
                name.setText("");
                if (media.type == Media.MEDIA_TYPE_IMAGE) {
                    durationText.setVisibility(View.GONE);
                    mediaThumbnailLoader.load(imageView, post.media.get(0));
                } else if (media.type == Media.MEDIA_TYPE_VIDEO) {
                    durationText.setVisibility(View.VISIBLE);
                    durationLoader.load(durationText, media.file);
                    mediaThumbnailLoader.load(imageView, media);
                }
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
