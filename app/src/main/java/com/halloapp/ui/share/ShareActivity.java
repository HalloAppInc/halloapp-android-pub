package com.halloapp.ui.share;

import android.content.Intent;
import android.graphics.Outline;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.permissions.PermissionUtils;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.ui.ContentComposerActivity;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.SystemUiVisibility;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.mediaedit.MediaEditActivity;
import com.halloapp.util.FilterUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class ShareActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION = 1;
    private static final int REQUEST_CODE_COMPOSE = 2;

    protected ShareViewModel viewModel;
    private TextView emptyView;

    private final DestinationsAdapter adapter = new DestinationsAdapter();
    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_share);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        setTitle(getTitleText());

        EditText searchBox = findViewById(R.id.search_text);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.getFilter().filter(s.toString());
            }
        });

        emptyView = findViewById(android.R.id.empty);

        final RecyclerView listView = findViewById(android.R.id.list);
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setAdapter(adapter);

        final ShareDestinationListView selectionListView = findViewById(R.id.selection);
        selectionListView.setOnRemoveListener(destination -> viewModel.toggleSelection(destination));

        final View selectionDivider = findViewById(R.id.selection_divider);

        final TextView nextView = findViewById(R.id.next);
        nextView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                float radius = getResources().getDimension(R.dimen.share_destination_next_radius);
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
            }
        });
        nextView.setClipToOutline(true);
        nextView.setOnClickListener(v -> next());
        nextView.setVisibility(View.GONE);

        viewModel = new ViewModelProvider(this, new ShareViewModel.Factory(getApplication(), showOnlyChats())).get(ShareViewModel.class);
        viewModel.destinationListAndRecency.getLiveData().observe(this, destinationListAndRecency -> adapter.setDestinations(destinationListAndRecency.getDestinationList()));
        viewModel.selectionList.observe(this, selection -> {
            nextView.setVisibility(selection.size() > 0 ? View.VISIBLE : View.GONE);
            adapter.setSelection(selection);
            selectionListView.submitList(selection);
            selectionDivider.setVisibility(selection.size() > 0 ? View.VISIBLE : View.GONE);
        });
        viewModel.feedPrivacyLiveData.getLiveData().observe(this, adapter::setPrivacy);
        load();
    }

    protected @StringRes int getTitleText() {
        return R.string.app_name;
    }

    protected boolean showOnlyChats() {
        return false;
    }

    private void load() {
        if (PermissionUtils.hasOrRequestContactPermissions(this, REQUEST_CODE_ASK_CONTACTS_PERMISSION)) {
            viewModel.destinationListAndRecency.invalidate();
        }
    }

    protected void next() {
        List<ShareDestination> destinations = viewModel.selectionList.getValue();
        if (destinations == null || destinations.size() == 0) {
            return;
        }

        Intent intent = ContentComposerActivity.newSharePost(this, destinations);

        ArrayList<Uri> uris;
        if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
            final Uri uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
            if (uri != null) {
                uris = new ArrayList<>(Collections.singleton(uri));
            } else {
                uris = null;
            }
        } else {
            uris = getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        }

        intent.putExtra(MediaEditActivity.EXTRA_MEDIA, uris);
        intent.putExtra(Intent.EXTRA_TEXT, getIntent().getStringExtra(Intent.EXTRA_TEXT));

        startActivityForResult(intent, REQUEST_CODE_COMPOSE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_COMPOSE) {
            if (resultCode == RESULT_OK) {
                finish();
            } else if (data != null) {
                ArrayList<ShareDestination> destinations = data.getParcelableArrayListExtra(ContentComposerActivity.EXTRA_DESTINATIONS);
                if (destinations != null) {
                    viewModel.updateSelectionList(destinations);
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> list) {
        if (requestCode == REQUEST_CODE_ASK_CONTACTS_PERMISSION) {
            load();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            if (requestCode == REQUEST_CODE_ASK_CONTACTS_PERMISSION) {
                new AppSettingsDialog.Builder(this)
                        .setRationale(getString(R.string.contacts_permission_rationale_denied))
                        .build().show();
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void bindTo(@NonNull DestinationItem item) {
        }
    }

    class ItemViewHolder extends ViewHolder {
        final private ImageView avatarView;
        final private TextView nameView;
        final private ImageView selectedView;

        private DestinationItem item;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.avatar);
            nameView = itemView.findViewById(R.id.name);
            selectedView = itemView.findViewById(R.id.selected);
            itemView.setOnClickListener(v -> viewModel.toggleSelection(item.destination));
        }

        @Override
        void bindTo(@NonNull DestinationItem item) {
            this.item = item;
            avatarLoader.load(avatarView, Preconditions.checkNotNull(item.destination.id), false);

            if (item.filterTokens != null && !item.filterTokens.isEmpty()) {
                String name = item.destination.name;
                CharSequence formattedName = FilterUtils.formatMatchingText(ShareActivity.this, name, item.filterTokens);
                if (formattedName != null) {
                    nameView.setText(formattedName);
                } else {
                    nameView.setText(name);
                }
            } else {
                nameView.setText(item.destination.name);
            }

            selectedView.setVisibility(item.selected ? View.VISIBLE : View.INVISIBLE);
        }
    }

    class HomeViewHolder extends ViewHolder {

        final private ImageView homeSelectedView;
        final private ImageView favoritesSelectedView;
        private DestinationItem item;

        HomeViewHolder(@NonNull View itemView) {
            super(itemView);

            homeSelectedView = itemView.findViewById(R.id.contacts_selected);
            favoritesSelectedView = itemView.findViewById(R.id.favorites_selected);

            ImageView contactsIcon = itemView.findViewById(R.id.contacts_icon);
            contactsIcon.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    float radius = getResources().getDimension(R.dimen.share_destination_item_radius);
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
                }
            });
            contactsIcon.setClipToOutline(true);

            ImageView favoritesIcon = itemView.findViewById(R.id.favorites_icon);
            favoritesIcon.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    float radius = getResources().getDimension(R.dimen.share_destination_item_radius);
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
                }
            });
            favoritesIcon.setClipToOutline(true);

            View favoritesItem = itemView.findViewById(R.id.favorites);
            favoritesItem.setOnClickListener(v -> {
                changeFeedTarget(PrivacyList.Type.ONLY);
            });
            View contactsItem = itemView.findViewById(R.id.my_contacts);
            contactsItem.setOnClickListener(v -> {
                changeFeedTarget(PrivacyList.Type.ALL);
            });

        }

        private void changeFeedTarget(@NonNull @PrivacyList.Type String newTarget) {
            viewModel.toggleHomeSelection(newTarget);
            if (!newTarget.equals(viewModel.getFeedTarget().getValue())) {
                viewModel.setSelectedFeedTarget(newTarget);
            }
        }

        void bindTo(@NonNull DestinationItem item) {
            this.item = item;
            if (item.selected) {
                if (item.destination.type == ShareDestination.TYPE_FAVORITES) {
                    favoritesSelectedView.setVisibility(View.VISIBLE);
                    homeSelectedView.setVisibility(View.INVISIBLE);
                } else {
                    homeSelectedView.setVisibility(View.VISIBLE);
                    favoritesSelectedView.setVisibility(View.INVISIBLE);
                }
            } else {
                favoritesSelectedView.setVisibility(View.INVISIBLE);
                homeSelectedView.setVisibility(View.INVISIBLE);
            }
        }
    }

    static class HeaderViewHolder extends ViewHolder {
        final private TextView nameView;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.name);
        }

        public void setText(int resid) {
            nameView.setText(resid);
        }
    }

    class ShowAllGroupsViewHolder extends ViewHolder {
        ShowAllGroupsViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(v -> adapter.showAllGroups());
        }
    }

    private static class DestinationItem {
        private static final int ITEM_HOME_FEED = 0;
        private static final int ITEM_GROUP = 1;
        private static final int ITEM_CONTACT = 2;
        private static final int ITEM_GROUPS_HEADER = 3;
        private static final int ITEM_ALL_GROUPS = 4;
        private static final int ITEM_CONTACTS_HEADER = 5;

        public final int type;
        public final ShareDestination destination;
        public final boolean selected;
        public List<String> filterTokens;

        private DestinationItem(ShareDestination destination, boolean selected, List<String> filterTokens) {
            this.type = itemType(destination.type);
            this.destination = destination;
            this.selected = selected;
            this.filterTokens = filterTokens;
        }

        private DestinationItem(int type) {
            this.type = type;
            this.destination = null;
            this.selected = false;
        }

        public static int itemType(int destinationType) {
            switch (destinationType) {
                case ShareDestination.TYPE_MY_CONTACTS:
                case ShareDestination.TYPE_FAVORITES:
                    return DestinationItem.ITEM_HOME_FEED;
                case ShareDestination.TYPE_GROUP:
                    return DestinationItem.ITEM_GROUP;
                case ShareDestination.TYPE_CONTACT:
                    return DestinationItem.ITEM_CONTACT;
            }

            throw new IllegalArgumentException();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DestinationItem that = (DestinationItem) o;
            return type == that.type && selected == that.selected && Objects.equals(destination, that.destination) && Objects.equals(filterTokens, that.filterTokens);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, destination, selected, filterTokens);
        }
    }

    private class DestinationsAdapter extends ListAdapter<DestinationItem, ViewHolder> implements Filterable {
        private static final int MIN_SHOWN_GROUPS = 6;

        private List<ShareDestination> destinations = new ArrayList<>();
        final private List<ShareDestination> limitedDestinations = new ArrayList<>();
        private List<ShareDestination> selection = new ArrayList<>();
        private CharSequence filterText;
        private List<String> filterTokens;
        private FeedPrivacy privacy;
        private boolean hideAdditionalGroups = true;

        DestinationsAdapter() {
            super(DIFF_CALLBACK);
        }

        @Override
        public Filter getFilter() {
            return new DestinationsFilter(destinations);
        }

        void setDestinations(@NonNull List<ShareDestination> destinations) {
            this.destinations = destinations;

            computeLimitedDestinations();
            getFilter().filter(filterText);
        }

        void setSelection(@NonNull List<ShareDestination> selection) {
            this.selection = selection;
            getFilter().filter(filterText);
        }

        void setFilteredDestinations(@NonNull List<ShareDestination> destinations, CharSequence filterText) {
            this.filterText = filterText;
            this.filterTokens = FilterUtils.getFilterTokens(filterText);

            if (!isSearching() && hideAdditionalGroups) {
                submitList(build(limitedDestinations));
            } else {
                submitList(build(destinations));
            }
        }

        void showAllGroups() {
            hideAdditionalGroups = false;
            getFilter().filter(filterText);
        }

        private void computeLimitedDestinations() {
            limitedDestinations.clear();

            int groupCount = 0;
            for (ShareDestination destination : destinations) {
                if (destination.type == ShareDestination.TYPE_GROUP) {
                    if (groupCount < MIN_SHOWN_GROUPS) {
                        limitedDestinations.add(destination);
                    }

                    groupCount++;
                } else {
                    limitedDestinations.add(destination);
                }
            }

            if (groupCount <= MIN_SHOWN_GROUPS) {
                hideAdditionalGroups = false;
            }
        }

        @NonNull
        private List<DestinationItem> build(@NonNull List<ShareDestination> destinations) {
            boolean hasGroupHeader = false;
            boolean hasContactHeader = false;
            ArrayList<DestinationItem> items = new ArrayList<>(destinations.size());

            for (int i = 0; i < destinations.size(); i++) {
                ShareDestination dest = destinations.get(i);

                if (!hasGroupHeader && dest.type == ShareDestination.TYPE_GROUP) {
                    hasGroupHeader = true;
                    items.add(new DestinationItem(DestinationItem.ITEM_GROUPS_HEADER));
                }

                if (!hasContactHeader && dest.type == ShareDestination.TYPE_CONTACT) {
                    hasContactHeader = true;
                    items.add(new DestinationItem(DestinationItem.ITEM_CONTACTS_HEADER));
                }

                items.add(new DestinationItem(dest, selection.contains(dest), filterTokens));

                if (dest.type == DestinationItem.ITEM_GROUP && (i == destinations.size() - 1 || destinations.get(i + 1).type != DestinationItem.ITEM_GROUP)) {
                    if (hideAdditionalGroups && !isSearching()) {
                        items.add(new DestinationItem(DestinationItem.ITEM_ALL_GROUPS));
                    }
                }
            }

            return items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case DestinationItem.ITEM_HOME_FEED:
                    return new HomeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.share_destination_home, parent, false));
                case DestinationItem.ITEM_GROUPS_HEADER:
                case DestinationItem.ITEM_CONTACTS_HEADER:
                    return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.share_destination_header, parent, false));
                case DestinationItem.ITEM_ALL_GROUPS:
                    return new ShowAllGroupsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.share_destination_show_all, parent, false));
                case DestinationItem.ITEM_CONTACT:
                    return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.share_destination_contact, parent, false));
                case DestinationItem.ITEM_GROUP:
                    return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.share_destination_group, parent, false));
            }

            throw new IllegalArgumentException();
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (holder instanceof ShowAllGroupsViewHolder) {
                return;
            }

            if (holder instanceof HeaderViewHolder) {
                HeaderViewHolder header = ((HeaderViewHolder) holder);

                if (getItem(position).type == DestinationItem.ITEM_GROUPS_HEADER) {
                    header.setText(R.string.your_groups);
                } else if (getItem(position).type == DestinationItem.ITEM_CONTACTS_HEADER) {
                    header.setText(R.string.recent_contacts);
                }

                return;
            }

            if (holder instanceof HomeViewHolder) {
                HomeViewHolder homeHolder = (HomeViewHolder) holder;
                homeHolder.bindTo(getItem(position));
                return;
            }

            holder.bindTo(getItem(position));
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).type;
        }

        private boolean isSearching() {
            return filterText != null && filterText.length() > 0;
        }

        public void setPrivacy(FeedPrivacy privacy) {
            this.privacy = privacy;
            notifyItemChanged(0);
        }
    }

    private static final DiffUtil.ItemCallback<DestinationItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<DestinationItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull DestinationItem oldItem, @NonNull DestinationItem newItem) {
            if (oldItem.type == newItem.type && oldItem.type == DestinationItem.ITEM_HOME_FEED) {
                return true;
            }
            return oldItem.type == newItem.type && Objects.equals(oldItem.destination, newItem.destination);
        }

        @Override
        public boolean areContentsTheSame(@NonNull DestinationItem oldItem, @NonNull DestinationItem newItem) {
            return oldItem.equals(newItem);
        }
    };

    private class DestinationsFilter extends FilterUtils.ItemFilter<ShareDestination> {

        public DestinationsFilter(@NonNull List<ShareDestination> destinations) {
            super(destinations);
        }

        @Override
        protected String itemToString(ShareDestination destination) {
            return destination.name;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            List<ShareDestination> filtered = (List<ShareDestination>) results.values;
            adapter.setFilteredDestinations(filtered, constraint);

            if (filtered.isEmpty() && !TextUtils.isEmpty(constraint)) {
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText(getString(R.string.contact_search_empty, constraint));
            } else {
                emptyView.setVisibility(View.GONE);
            }
        }
    }
}

