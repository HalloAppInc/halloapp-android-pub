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
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShareDestinationSelectableListView extends LinearLayout {
    private SelectionAdapter selectionAdapter;
    private RecyclerView selectionView;

    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();
    private OnToggleSelectionListener toggleSelectionListener;

    public interface OnToggleSelectionListener {
        void onToggleSelection(@NonNull ShareDestination destination);
    }

    public ShareDestinationSelectableListView(@NonNull Context context) {
        super(context);
        init();
    }

    public ShareDestinationSelectableListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ShareDestinationSelectableListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(LinearLayout.VERTICAL);
        inflate(getContext(), R.layout.share_destination_selectable_list, this);

        selectionAdapter = new SelectionAdapter();
        selectionView = findViewById(R.id.share_selection);
        selectionView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        selectionView.setAdapter(selectionAdapter);
    }

    public void setOnToggleSelectionListener(OnToggleSelectionListener listener) {
        toggleSelectionListener = listener;
    }

    private void notifyOnToggleSelection(ShareDestination destination) {
        if (toggleSelectionListener != null) {
            toggleSelectionListener.onToggleSelection(destination);
        }
    }

    public void submitDestinationList(@NonNull List<ShareDestination> destinationList) {
        selectionAdapter.setDestinations(destinationList);
    }

    public void submitSelectionList(@NonNull List<ShareDestination> selectionList) {
        selectionAdapter.setSelection(selectionList);
    }

    private int getTextColor(boolean selected) {
        return getResources().getColor(selected ? R.color.color_secondary : R.color.primary_text);
    }

    private static abstract class SelectableViewHolder extends RecyclerView.ViewHolder {
        SelectableViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        abstract void bindTo(@NonNull DestinationItem item);
    }

    private class SelectableMyContactsViewHolder extends SelectableViewHolder {
        private ShareDestination destination;

        private final View avatarContainerView;
        private final ImageView avatarView;
        private final TextView nameView;
        private final ImageView checkboxView;

        SelectableMyContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(v -> notifyOnToggleSelection(destination));

            avatarView = itemView.findViewById(R.id.avatar);
            avatarView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    float radius = view.getResources().getDimension(R.dimen.compact_share_item_avatar_rect_radius);
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
                }
            });
            avatarView.setClipToOutline(true);
            avatarView.setImageResource(R.drawable.ic_privacy_my_contacts);

            avatarContainerView = itemView.findViewById(R.id.avatar_container);
            nameView = itemView.findViewById(R.id.name);
            checkboxView = itemView.findViewById(R.id.checkbox);
        }

        @Override
        void bindTo(@NonNull DestinationItem item) {
            destination = item.destination;

            nameView.setTextColor(getTextColor(item.selected));
            checkboxView.setVisibility(item.selected ? VISIBLE : GONE);
            final String myFriendsText = getResources().getString(R.string.compact_share_my_friends, item.destination.size);
            nameView.setText(myFriendsText);

            avatarView.setSelected(item.selected);
            avatarContainerView.setSelected(item.selected);
        }
    }

    private class SelectableGroupViewHolder extends SelectableViewHolder {
        final private ImageView avatarView;
        final private TextView nameView;
        private final ImageView checkboxView;

        private ShareDestination destination;

        SelectableGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(v -> notifyOnToggleSelection(destination));

            avatarView = itemView.findViewById(R.id.avatar);
            nameView = itemView.findViewById(R.id.name);
            checkboxView = itemView.findViewById(R.id.checkbox);
        }

        @Override
        void bindTo(@NonNull DestinationItem item) {
            destination = item.destination;

            avatarLoader.load(avatarView, Preconditions.checkNotNull(destination.id), false);
            avatarView.setSelected(item.selected);
            nameView.setText(destination.name);
            nameView.setTextColor(getTextColor(item.selected));
            checkboxView.setVisibility(item.selected ? VISIBLE : GONE);
        }
    }

    private class SelectableContactViewHolder extends SelectableViewHolder {
        final private ImageView avatarView;
        final private TextView nameView;
        private final ImageView checkboxView;

        private ShareDestination destination;

        SelectableContactViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(v -> notifyOnToggleSelection(destination));

            avatarView = itemView.findViewById(R.id.avatar);
            nameView = itemView.findViewById(R.id.name);
            checkboxView = itemView.findViewById(R.id.checkbox);
        }

        @Override
        void bindTo(@NonNull DestinationItem item) {
            destination = item.destination;

            avatarLoader.load(avatarView, Preconditions.checkNotNull(destination.id), false);
            avatarView.setSelected(item.selected);
            nameView.setText(destination.name);
            nameView.setTextColor(getTextColor(item.selected));
            checkboxView.setVisibility(item.selected ? VISIBLE : GONE);
        }
    }

    private static class DestinationItem {
        public final ShareDestination destination;
        public final boolean selected;

        private DestinationItem(ShareDestination destination, boolean selected) {
            this.destination = destination;
            this.selected = selected;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final DestinationItem that = (DestinationItem) o;
            return selected == that.selected && Objects.equals(destination, that.destination);
        }

        @Override
        public int hashCode() {
            return Objects.hash(destination, selected);
        }
    }

    private class SelectionAdapter extends ListAdapter<DestinationItem, SelectableViewHolder> {
        private static final int ITEM_MY_CONTACTS = 0;
        private static final int ITEM_GROUP = 1;
        private static final int ITEM_CONTACT = 2;

        private List<ShareDestination> destinations = new ArrayList<>();
        private List<ShareDestination> selection = new ArrayList<>();

        public SelectionAdapter() {
            super(DIFF_CALLBACK);
        }

        void setDestinations(@NonNull List<ShareDestination> destinations) {
            this.destinations = destinations;
            rebuildAndSubmitList();
        }

        void setSelection(@NonNull List<ShareDestination> selection) {
            this.selection = selection;
            rebuildAndSubmitList();
        }

        private void rebuildAndSubmitList() {
            final List<DestinationItem> destinationItemList = rebuildItemList();
            submitList(destinationItemList);
        }

        private List<DestinationItem> rebuildItemList() {
            final ArrayList<DestinationItem> items = new ArrayList<>(destinations.size());
            for (ShareDestination dest : destinations) {
                if (dest.type != ShareDestination.TYPE_FAVORITES) {
                    items.add(new DestinationItem(dest, selection.contains(dest)));
                }
            }
            return items;
        }

        @NonNull
        @Override
        public SelectableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case ITEM_MY_CONTACTS:
                    return new SelectableMyContactsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.share_destination_home_selectable_item, parent, false));
                case ITEM_CONTACT:
                    return new SelectableContactViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.share_destination_contact_selectable_item, parent, false));
                case ITEM_GROUP:
                    return new SelectableGroupViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.share_destination_group_selectable_item, parent, false));
            }

            throw new IllegalArgumentException();
        }

        @Override
        public void onBindViewHolder(@NonNull SelectableViewHolder holder, int position) {
            holder.bindTo(getItem(position));
        }

        @Override
        public int getItemViewType(int position) {
            switch (getItem(position).destination.type) {
                case ShareDestination.TYPE_MY_CONTACTS:
                    return ITEM_MY_CONTACTS;
                case ShareDestination.TYPE_GROUP:
                    return ITEM_GROUP;
                case ShareDestination.TYPE_CONTACT:
                    return ITEM_CONTACT;
            }

            return -1;
        }
    }

    private static final DiffUtil.ItemCallback<DestinationItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<DestinationItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull DestinationItem oldItem, @NonNull DestinationItem newItem) {
            return oldItem.destination.equals(newItem.destination);
        }

        @Override
        public boolean areContentsTheSame(@NonNull DestinationItem oldItem, @NonNull DestinationItem newItem) {
            return oldItem.selected == newItem.selected && oldItem.destination.size == newItem.destination.size;
        }
    };
}
