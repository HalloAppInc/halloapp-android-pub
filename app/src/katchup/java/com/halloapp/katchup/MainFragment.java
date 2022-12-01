package com.halloapp.katchup;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.MainActivity;
import com.halloapp.R;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.content.PostsDataSource;
import com.halloapp.id.UserId;
import com.halloapp.ui.HalloFragment;
import com.halloapp.ui.HeaderFooterAdapter;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.posts.PostListDiffer;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;

public class MainFragment extends HalloFragment {

    private MainViewModel viewModel;
    private ViewGroup parentViewGroup;
    private RecyclerView listView;
    private PostAdapter adapter;
    private TextView followingButton;
    private TextView nearbyButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);

        parentViewGroup = container;

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

        listView = root.findViewById(R.id.recycler_view);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireContext());
        listView.setLayoutManager(layoutManager);
        adapter = new PostAdapter();
        listView.setAdapter(adapter);

        followingButton = root.findViewById(R.id.following);
        followingButton.setOnClickListener(v -> setFollowingSelected(true));
        nearbyButton = root.findViewById(R.id.nearby);
        nearbyButton.setOnClickListener(v -> setFollowingSelected(false));
        setFollowingSelected(true);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        viewModel.postList.observe(getViewLifecycleOwner(), posts -> {
            adapter.submitList(posts, null);
        });

        return root;
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

        private final PostsDataSource.Factory dataSourceFactory;
        final LiveData<PagedList<Post>> postList;

        public MainViewModel(@NonNull Application application) {
            super(application);

            // TODO(jack): Add observer to invalidate data source
//            contentDb.addObserver(contentObserver);
            dataSourceFactory = new PostsDataSource.Factory(contentDb, UserId.ME);
            postList = new LivePagedListBuilder<>(dataSourceFactory, 50).build();
        }
    }

    private static class KatchupPostViewHolder extends ViewHolderWithLifecycle {
        public KatchupPostViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bindTo(@NonNull Post post) {

        }
    }

    private class PostAdapter extends HeaderFooterAdapter<Post> {

        private final PostListDiffer postListDiffer;

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
            return new KatchupPostViewHolder(LayoutInflater.from(requireContext()).inflate(R.layout.post_item_katchup, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolderWithLifecycle holder, int position) {
            Post post = Preconditions.checkNotNull(getItem(position));
            if (holder instanceof KatchupPostViewHolder) {
                ((KatchupPostViewHolder) holder).bindTo(post);
            }
        }
    }
}
