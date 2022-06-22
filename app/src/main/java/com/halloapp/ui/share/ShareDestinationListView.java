package com.halloapp.ui.share;

import android.content.Context;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.Preconditions;

import java.util.List;

public class ShareDestinationListView extends LinearLayout {
    private SelectionAdapter selectionAdapter;
    private RecyclerView selectionView;

    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();
    private OnRemoveListener removeListener;

    public interface OnRemoveListener {
        void onRemove(@NonNull ShareDestination destination);
    }

    public ShareDestinationListView(@NonNull Context context) {
        super(context);
        init();
    }

    public ShareDestinationListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ShareDestinationListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(LinearLayout.VERTICAL);
        inflate(getContext(), R.layout.share_destination_list, this);

        selectionAdapter = new SelectionAdapter();
        selectionView = findViewById(R.id.share_selection);
        selectionView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        selectionView.setAdapter(selectionAdapter);
    }

    public void setOnRemoveListener(OnRemoveListener listener) {
        removeListener = listener;
    }

    private void notifyOnRemove(ShareDestination destination) {
        if (removeListener != null) {
            removeListener.onRemove(destination);
        }
    }

    public void submitList(List<ShareDestination> destinationList) {
        boolean moveToTheEnd = selectionAdapter.getItemCount() < destinationList.size();

        selectionAdapter.submitList(destinationList, () -> {
            if (moveToTheEnd) {
                selectionView.scrollToPosition(destinationList.size() - 1);
            }

            post(() -> setVisibility(destinationList.size() == 0 ? View.GONE : View.VISIBLE));
        });
    }

    private static abstract class SelectedViewHolder extends RecyclerView.ViewHolder {
        SelectedViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        abstract void bindTo(@NonNull ShareDestination destination);
    }

    private class SelectedFavoritesViewHolder extends SelectedViewHolder {
        private ShareDestination destination;

        private final ImageView avatarView;
        private final TextView nameView;

        SelectedFavoritesViewHolder(@NonNull View itemView) {
            super(itemView);

            avatarView = itemView.findViewById(R.id.avatar);
            avatarView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    float radius = view.getResources().getDimension(R.dimen.share_destination_item_radius);
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
                }
            });
            avatarView.setClipToOutline(true);
            nameView = itemView.findViewById(R.id.name);

            View removeView = itemView.findViewById(R.id.remove);
            removeView.setOnClickListener(v -> notifyOnRemove(destination));
            avatarView.setBackgroundColor(ContextCompat.getColor(avatarView.getContext(), R.color.favorites_yellow));
            avatarView.setImageResource(R.drawable.ic_privacy_favorites);
            nameView.setText(R.string.contact_favorites);
        }

        @Override
        void bindTo(@NonNull ShareDestination destination) {
            this.destination = destination;
        }
    }

    private class SelectedMyContactsViewHolder extends SelectedViewHolder {
        private ShareDestination destination;

        private final ImageView avatarView;
        private final TextView nameView;

        SelectedMyContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            avatarView = itemView.findViewById(R.id.avatar);
            avatarView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    float radius = view.getResources().getDimension(R.dimen.share_destination_item_radius);
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
                }
            });
            avatarView.setClipToOutline(true);
            nameView = itemView.findViewById(R.id.name);

            View removeView = itemView.findViewById(R.id.remove);
            removeView.setOnClickListener(v -> notifyOnRemove(destination));
            avatarView.setBackgroundColor(ContextCompat.getColor(avatarView.getContext(), R.color.color_secondary));
            avatarView.setImageResource(R.drawable.ic_privacy_my_contacts);
            nameView.setText(R.string.my_contacts_title);
        }

        @Override
        void bindTo(@NonNull ShareDestination destination) {
            this.destination = destination;
        }
    }

    private class SelectedItemViewHolder extends SelectedViewHolder {
        final private ImageView avatarView;
        final private TextView nameView;

        private ShareDestination destination;

        SelectedItemViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.avatar);
            nameView = itemView.findViewById(R.id.name);

            View removeView = itemView.findViewById(R.id.remove);
            removeView.setOnClickListener(v -> notifyOnRemove(destination));
        }

        @Override
        void bindTo(@NonNull ShareDestination destination) {
            this.destination = destination;
            avatarLoader.load(avatarView, Preconditions.checkNotNull(destination.id), false);
            nameView.setText(destination.name);
        }
    }

    private class SelectionAdapter extends ListAdapter<ShareDestination, SelectedViewHolder> {
        private static final int ITEM_MY_CONTACTS = 0;
        private static final int ITEM_FAVORITES = 3;
        private static final int ITEM_GROUP = 1;
        private static final int ITEM_CONTACT = 2;

        public SelectionAdapter() {
            super(DIFF_CALLBACK);
        }

        @NonNull
        @Override
        public SelectedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case ITEM_MY_CONTACTS:
                    return new SelectedMyContactsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.share_destination_home_selected, parent, false));
                case ITEM_FAVORITES:
                    return new SelectedFavoritesViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.share_destination_home_selected, parent, false));
                case ITEM_CONTACT:
                    return new SelectedItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.share_destination_contact_selected, parent, false));
                case ITEM_GROUP:
                    return new SelectedItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.share_destination_group_selected, parent, false));
            }

            throw new IllegalArgumentException();
        }

        @Override
        public void onBindViewHolder(@NonNull SelectedViewHolder holder, int position) {
            holder.bindTo(getItem(position));
        }

        @Override
        public int getItemViewType(int position) {
            switch (getItem(position).type) {
                case ShareDestination.TYPE_MY_CONTACTS:
                    return ITEM_MY_CONTACTS;
                case ShareDestination.TYPE_FAVORITES:
                    return ITEM_FAVORITES;
                case ShareDestination.TYPE_GROUP:
                    return ITEM_GROUP;
                case ShareDestination.TYPE_CONTACT:
                    return ITEM_CONTACT;
            }

            return -1;
        }
    }

    private static final DiffUtil.ItemCallback<ShareDestination> DIFF_CALLBACK = new DiffUtil.ItemCallback<ShareDestination>() {
        @Override
        public boolean areItemsTheSame(@NonNull ShareDestination oldItem, @NonNull ShareDestination newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull ShareDestination oldItem, @NonNull ShareDestination newItem) {
            return true;
        }
    };
}
