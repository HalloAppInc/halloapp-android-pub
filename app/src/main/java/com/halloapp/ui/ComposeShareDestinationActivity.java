package com.halloapp.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Outline;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.id.ChatId;
import com.halloapp.permissions.PermissionUtils;
import com.halloapp.permissions.PermissionWatcher;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.contacts.EditFavoritesActivity;
import com.halloapp.ui.contacts.ViewMyContactsActivity;
import com.halloapp.ui.groups.CreateGroupActivity;
import com.halloapp.ui.share.ShareDestination;
import com.halloapp.ui.share.ShareViewModel;
import com.halloapp.util.FilterUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ContactPermissionsBannerView;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class ComposeShareDestinationActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {
    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION = 1;

    private static final String EXTRA_DESTINATIONS = "destinations";

    private ShareViewModel shareViewModel;
    private TextView emptyView;
    private View shareButton;

    private ContactPermissionsBannerView contactPermissionsBannerView;

    private final DestinationsAdapter adapter = new DestinationsAdapter();
    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();
    private final PermissionWatcher permissionWatcher = PermissionWatcher.getInstance();

    public static Intent newComposeShareDestination(@NonNull Context context, List<ShareDestination> selectionList) {
        final Intent intent = new Intent(context, ComposeShareDestinationActivity.class);
        intent.putParcelableArrayListExtra(EXTRA_DESTINATIONS, new ArrayList<>(selectionList));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_compose_share_destination);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final EditText searchBox = findViewById(R.id.search_text);
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

        emptyView = findViewById(R.id.empty);
        shareButton = findViewById(R.id.share_button);
        shareButton.setOnClickListener(view -> selectDestinations());

        contactPermissionsBannerView = findViewById(R.id.contact_permissions_banner);
        contactPermissionsBannerView.bind(permissionWatcher, this);
        contactPermissionsBannerView.setOnClickListener(v -> {
            PermissionUtils.hasOrRequestContactPermissions(this, REQUEST_CODE_ASK_CONTACTS_PERMISSION);
        });

        final RecyclerView listView = findViewById(R.id.list);
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setAdapter(adapter);

        final float scrolledElevation = getResources().getDimension(R.dimen.action_bar_elevation);
        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                final float elevation = listView.computeVerticalScrollOffset() > 0 ? scrolledElevation : 0;
                final View searchBox = findViewById(R.id.search);
                if (searchBox.getElevation() != elevation) {
                    searchBox.setElevation(elevation);
                }
            }
        });

        shareViewModel = new ViewModelProvider(this, new ShareViewModel.Factory(getApplication(), false)).get(ShareViewModel.class);

        final List<ShareDestination> selectionList = getIntent().getParcelableArrayListExtra(EXTRA_DESTINATIONS);
        if (selectionList != null && selectionList.size() > 0) {
            shareViewModel.selectionList.setValue(selectionList);
        }

        shareViewModel.destinationListAndRecency.getLiveData().observe(this, destinationListAndRecency -> adapter.setDestinationsAndRecency(destinationListAndRecency.getDestinationList(), destinationListAndRecency.getRecentDestinationIdList()));
        shareViewModel.frequentDestinationIdList.getLiveData().observe(this, adapter::setFrequentDestinationIds);
        shareViewModel.selectionList.observe(this, this::setSelection);
        shareViewModel.feedPrivacyLiveData.getLiveData().observe(this, adapter::setPrivacy);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PermissionUtils.hasOrRequestContactPermissions(this, REQUEST_CODE_ASK_CONTACTS_PERMISSION)) {
            shareViewModel.invalidate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_group) {
            startActivity(CreateGroupActivity.newFeedPickerIntent(this));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void selectDestinations() {
        final List<ShareDestination> destinations = shareViewModel.selectionList.getValue();
        if (destinations == null || destinations.isEmpty()) {
            Log.w("ComposeShareDestinationActivity: no destination set");
        } else {
            final Intent resultData = new Intent();
            resultData.putParcelableArrayListExtra(ContentComposerActivity.EXTRA_DESTINATIONS, new ArrayList<>(destinations));
            setResult(RESULT_OK, resultData);
            finish();
        }
    }

    private void viewContacts() {
        startActivity(ViewMyContactsActivity.viewMyContacts(this));
    }

    private void editFavorites() {
        startActivity(EditFavoritesActivity.openFavorites(this));
    }

    private void setSelection(@NonNull List<ShareDestination> selection) {
        adapter.setSelection(selection);
        shareButton.setEnabled(selection.size() > 0);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> list) {
        if (requestCode == REQUEST_CODE_ASK_CONTACTS_PERMISSION) {
            shareViewModel.invalidate();
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

        void bindTo(@NonNull DestinationItem item) {}
    }

    class ItemViewHolder extends ViewHolder {
        final private ImageView avatarView;
        final private TextView nameView;
        final private ImageView selectedView;
        final private ImageView unselectedView;

        private DestinationItem item;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.avatar);
            nameView = itemView.findViewById(R.id.name);
            selectedView = itemView.findViewById(R.id.selected);
            unselectedView = itemView.findViewById(R.id.unselected);
            itemView.setOnClickListener(v -> shareViewModel.toggleSelection(item.destination));
        }

        @Override
        void bindTo(@NonNull DestinationItem item) {
            this.item = item;
            avatarLoader.load(avatarView, Preconditions.checkNotNull(item.destination.id), false);

            if (item.filterTokens != null && !item.filterTokens.isEmpty()) {
                String name = item.destination.name;
                CharSequence formattedName = FilterUtils.formatMatchingText(ComposeShareDestinationActivity.this, name, item.filterTokens);
                if (formattedName != null) {
                    nameView.setText(formattedName);
                } else {
                    nameView.setText(name);
                }
            } else {
                nameView.setText(item.destination.name);
            }

            selectedView.setVisibility(item.selected ? View.VISIBLE : View.GONE);
            unselectedView.setVisibility(item.selected ? View.GONE : View.VISIBLE);
        }
    }

    class HomeViewHolder extends ViewHolder {

        final private ImageView homeSelectedView;
        final private ImageView homeUnselectedView;
        final private ImageView favoritesSelectedView;
        final private ImageView favoritesUnselectedView;
        private DestinationItem item;

        HomeViewHolder(@NonNull View itemView) {
            super(itemView);

            homeSelectedView = itemView.findViewById(R.id.contacts_selected);
            homeUnselectedView = itemView.findViewById(R.id.contacts_unselected);
            favoritesSelectedView = itemView.findViewById(R.id.favorites_selected);
            favoritesUnselectedView = itemView.findViewById(R.id.favorites_unselected);

            ImageView contactsIcon = itemView.findViewById(R.id.contacts_icon);
            contactsIcon.setClipToOutline(true);

            ImageView favoritesIcon = itemView.findViewById(R.id.favorites_icon);
            favoritesIcon.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    float radius = getResources().getDimension(R.dimen.compose_share_destination_row_round_icon_radius);
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
                }
            });
            favoritesIcon.setClipToOutline(true);

            final View contactsItem = itemView.findViewById(R.id.my_contacts);
            contactsItem.setOnClickListener(v -> changeFeedTarget(PrivacyList.Type.ALL));
            final View favoritesItem = itemView.findViewById(R.id.favorites);
            favoritesItem.setOnClickListener(v -> changeFeedTarget(PrivacyList.Type.ONLY));

            final TextView browseContactsView = itemView.findViewById(R.id.view_my_contacts);
            browseContactsView.setText(getResources().getString(R.string.compose_share_view_contacts).toUpperCase(Locale.getDefault()));
            browseContactsView.setOnClickListener(view -> viewContacts());
            final TextView editFavoritesView = itemView.findViewById(R.id.edit_favorites);
            editFavoritesView.setText(getResources().getString(R.string.compose_share_edit_favorites).toUpperCase(Locale.getDefault()));
            editFavoritesView.setOnClickListener(view -> editFavorites());
        }

        private void updateNumberOfContacts(int numberOfContacts) {
            final TextView contactsNumberView = itemView.findViewById(R.id.my_contacts_number);
            final String text = numberOfContacts >= 0 ? getResources().getQuantityString(R.plurals.compose_share_contacts_number, numberOfContacts, numberOfContacts) : "";
            contactsNumberView.setText(text);
        }

        private void updateNumberOfFavorites(int numberOfFavorites) {
            final TextView favoritesNumberView = itemView.findViewById(R.id.favorites_number);
            final String text = numberOfFavorites >= 0 ? getResources().getQuantityString(R.plurals.compose_share_contacts_number, numberOfFavorites, numberOfFavorites) : "";
            favoritesNumberView.setText(text);
        }

        private void changeFeedTarget(@NonNull @PrivacyList.Type String newTarget) {
            shareViewModel.toggleHomeSelection(newTarget);
            if (!newTarget.equals(shareViewModel.getFeedTarget().getValue())) {
                shareViewModel.setSelectedFeedTarget(newTarget);
            }
        }

        @Override
        void bindTo(@NonNull DestinationItem item) {
            this.item = item;
            if (item.selected) {
                if (item.destination.type == ShareDestination.TYPE_FAVORITES) {
                    homeSelectedView.setVisibility(View.GONE);
                    homeUnselectedView.setVisibility(View.VISIBLE);
                    favoritesSelectedView.setVisibility(View.VISIBLE);
                    favoritesUnselectedView.setVisibility(View.GONE);
                } else {
                    homeSelectedView.setVisibility(View.VISIBLE);
                    homeUnselectedView.setVisibility(View.GONE);
                    favoritesSelectedView.setVisibility(View.GONE);
                    favoritesUnselectedView.setVisibility(View.VISIBLE);
                }
            } else {
                homeSelectedView.setVisibility(View.GONE);
                homeUnselectedView.setVisibility(View.VISIBLE);
                favoritesSelectedView.setVisibility(View.GONE);
                favoritesUnselectedView.setVisibility(View.VISIBLE);
            }
            updateNumberOfContacts(item.numberOfContacts);
            updateNumberOfFavorites(item.numberOfFavorites);
        }
    }

    class HeaderViewHolder extends ViewHolder {
        final private TextView nameView;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.name);
        }

        @Override
        void bindTo(@NonNull DestinationItem item) {
            if (item.type == DestinationItem.ITEM_FEED_HEADER) {
                nameView.setText(R.string.compose_share_feeds_header);
            } else if (item.type == DestinationItem.ITEM_GROUPS_HEADER) {
                nameView.setText(R.string.your_groups);
            } else if (item.type == DestinationItem.ITEM_CONTACTS_HEADER) {
                nameView.setText(R.string.your_contacts_title);
            } else if (item.type == DestinationItem.ITEM_FREQUENTS_HEADER) {
                nameView.setText(R.string.compose_share_frequents_header);
            } else if (item.type == DestinationItem.ITEM_RECENTS_HEADER) {
                nameView.setText(R.string.compose_share_recents_header);
            }
        }
    }

    private static class DestinationItem {
        private static final int ITEM_HOME_FEED = 0;
        private static final int ITEM_GROUP = 1;
        private static final int ITEM_CONTACT = 2;
        private static final int ITEM_FEED_HEADER = 3;
        private static final int ITEM_GROUPS_HEADER = 4;
        private static final int ITEM_CONTACTS_HEADER = 5;
        private static final int ITEM_FREQUENTS_HEADER = 6;
        private static final int ITEM_RECENTS_HEADER = 7;

        public final int type;
        public final ShareDestination destination;
        public final boolean selected;
        public List<String> filterTokens;
        public final int numberOfContacts;
        public final int numberOfFavorites;

        private DestinationItem(ShareDestination destination, boolean selected, List<String> filterTokens) {
            this.type = itemType(destination.type);
            this.destination = destination;
            this.selected = selected;
            this.filterTokens = filterTokens;
            this.numberOfContacts = -1;
            this.numberOfFavorites = -1;
        }

        private DestinationItem(ShareDestination destination, boolean selected, List<String> filterTokens, int numberOfContacts, int numberOfFavorites) {
            this.type = itemType(destination.type);
            this.destination = destination;
            this.selected = selected;
            this.filterTokens = filterTokens;
            this.numberOfContacts = numberOfContacts;
            this.numberOfFavorites = numberOfFavorites;
        }

        private DestinationItem(int type) {
            this.type = type;
            this.destination = null;
            this.selected = false;
            this.numberOfContacts = -1;
            this.numberOfFavorites = -1;
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
            return type == that.type && selected == that.selected && Objects.equals(destination, that.destination) && Objects.equals(filterTokens, that.filterTokens) && numberOfContacts == that.numberOfContacts && numberOfFavorites == that.numberOfFavorites;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, destination, selected, filterTokens, numberOfContacts, numberOfFavorites);
        }
    }

    private class DestinationsAdapter extends ListAdapter<DestinationItem, ViewHolder> implements Filterable {
        private List<ShareDestination> destinations = new ArrayList<>();
        private List<ShareDestination> selection = new ArrayList<>();
        private List<ChatId> frequentDestinationIds = new ArrayList<>();
        private List<ChatId> recentDestinationIds = new ArrayList<>();
        private CharSequence filterText;
        private List<String> filterTokens;
        private FeedPrivacy privacy;

        DestinationsAdapter() {
            super(DIFF_CALLBACK);
        }

        @Override
        public Filter getFilter() {
            return new DestinationsFilter(destinations);
        }

        void setDestinationsAndRecency(@NonNull List<ShareDestination> destinations, @NonNull List<ChatId> recentDestinationIds) {
            this.destinations = destinations;
            this.recentDestinationIds = recentDestinationIds;
            getFilter().filter(filterText);
        }

        void setFrequentDestinationIds(@NonNull List<ChatId> frequentDestinationIds) {
            this.frequentDestinationIds = frequentDestinationIds;
            getFilter().filter(filterText);
        }

        void setSelection(@NonNull List<ShareDestination> selection) {
            this.selection = selection;
            getFilter().filter(filterText);
        }

        void setFilteredDestinations(@NonNull List<ShareDestination> destinations, CharSequence filterText) {
            this.filterText = filterText;
            this.filterTokens = FilterUtils.getFilterTokens(filterText);

            submitList(build(destinations, !TextUtils.isEmpty(filterText)));
        }

        @NonNull
        private List<DestinationItem> build(@NonNull List<ShareDestination> destinations, boolean isSearching) {
            boolean hasGroupHeader = false;
            boolean hasContactHeader = false;
            final ArrayList<DestinationItem> items = new ArrayList<>(destinations.size() + 1);
            final Map<ChatId, ShareDestination> destinationIdMap = new HashMap<>();
            final Set<ShareDestination> shownDestinations = new HashSet<>();

            for (ShareDestination dest : destinations) {
                if (dest.id != null) {
                    destinationIdMap.put(dest.id, dest);
                }
            }

            if (!isSearching) {
                items.add(new DestinationItem(DestinationItem.ITEM_FEED_HEADER));
            }

            for (ShareDestination dest : destinations) {
                if (dest.type == ShareDestination.TYPE_MY_CONTACTS || dest.type == ShareDestination.TYPE_FAVORITES) {
                    items.add(new DestinationItem(dest, selection.contains(dest), filterTokens, dest.size, privacy.onlyList.size()));
                    shownDestinations.add(dest);
                }
            }

            final List<ShareDestination> frequentDestinations = new ArrayList<>();
            for (ChatId destinationId : frequentDestinationIds) {
                ShareDestination dest = destinationIdMap.get(destinationId);
                if (dest != null) {
                    frequentDestinations.add(dest);
                }
            }
            if (frequentDestinations.size() > 0) {
                items.add(new DestinationItem(DestinationItem.ITEM_FREQUENTS_HEADER));
                for (ShareDestination dest : frequentDestinations) {
                    shownDestinations.add(dest);
                    items.add(new DestinationItem(dest, selection.contains(dest), filterTokens));
                }
            }

            final List<ShareDestination> recentDestinations = new ArrayList<>();
            for (ChatId destinationId : recentDestinationIds) {
                ShareDestination dest = destinationIdMap.get(destinationId);
                if (dest != null) {
                    recentDestinations.add(dest);
                }
            }
            if (recentDestinations.size() > 0) {
                items.add(new DestinationItem(DestinationItem.ITEM_RECENTS_HEADER));
                for (ShareDestination dest : recentDestinations) {
                    shownDestinations.add(dest);
                    items.add(new DestinationItem(dest, selection.contains(dest), filterTokens));
                }
            }

            for (ShareDestination dest : destinations) {
                if (shownDestinations.contains(dest)) {
                    continue;
                } else {
                    shownDestinations.add(dest);
                }
                if (!hasGroupHeader && dest.type == ShareDestination.TYPE_GROUP) {
                    hasGroupHeader = true;
                    items.add(new DestinationItem(DestinationItem.ITEM_GROUPS_HEADER));
                }
                if (!hasContactHeader && dest.type == ShareDestination.TYPE_CONTACT) {
                    hasContactHeader = true;
                    items.add(new DestinationItem(DestinationItem.ITEM_CONTACTS_HEADER));
                }
                items.add(new DestinationItem(dest, selection.contains(dest), filterTokens));
            }

            return items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case DestinationItem.ITEM_HOME_FEED:
                    return new HomeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.compose_share_destination_home, parent, false));
                case DestinationItem.ITEM_FEED_HEADER:
                case DestinationItem.ITEM_GROUPS_HEADER:
                case DestinationItem.ITEM_CONTACTS_HEADER:
                case DestinationItem.ITEM_FREQUENTS_HEADER:
                case DestinationItem.ITEM_RECENTS_HEADER:
                    return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.compose_share_destination_header, parent, false));
                case DestinationItem.ITEM_CONTACT:
                    return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.compose_share_destination_contact, parent, false));
                case DestinationItem.ITEM_GROUP:
                    return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.compose_share_destination_group, parent, false));
            }

            throw new IllegalArgumentException();
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bindTo(getItem(position));
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).type;
        }

        public void setPrivacy(FeedPrivacy privacy) {
            this.privacy = privacy;
            notifyItemChanged(1);
        }
    }

    private static final DiffUtil.ItemCallback<DestinationItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<DestinationItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull DestinationItem oldItem, @NonNull DestinationItem newItem) {
            if (oldItem.type == newItem.type && oldItem.type == DestinationItem.ITEM_HOME_FEED) {
                return oldItem.numberOfContacts == newItem.numberOfContacts && oldItem.numberOfFavorites == newItem.numberOfFavorites;
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

