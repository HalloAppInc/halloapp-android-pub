package com.halloapp.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Outline;
import android.net.Uri;
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
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.R;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.media.MediaUtils;
import com.halloapp.permissions.PermissionUtils;
import com.halloapp.permissions.PermissionWatcher;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.contacts.EditFavoritesActivity;
import com.halloapp.ui.contacts.ViewMyContactsActivity;
import com.halloapp.ui.groups.CreateGroupActivity;
import com.halloapp.ui.share.ShareDestination;
import com.halloapp.ui.share.ShareViewModel;
import com.halloapp.util.ActivityUtils;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.FilterUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ContactPermissionsBannerView;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class ComposeShareDestinationActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {
    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION = 1;

    public static final String EXTRA_CHAT_ID = "chat_id";
    public static final String EXTRA_GROUP_ID = "group_id";
    public static final String EXTRA_MEDIA = "media";
    public static final String EXTRA_STATE = "state";
    public static final String EXTRA_REPLY_POST_ID = "reply_post_id";
    public static final String EXTRA_REPLY_POST_MEDIA_INDEX = "reply_post_media_index";
    public static final String EXTRA_POST_TEXT = "post_text";
    public static final String EXTRA_MENTIONS = "mentions";
    public static final String EXTRA_VOICE_DRAFT = "voice_draft";

    private FirebaseAnalytics firebaseAnalytics;

    private ChatId chatId;
    private GroupId groupId;
    private String postText;
    private List<Mention> mentions;

    private ShareViewModel shareViewModel;
    private ContentComposerViewModel composerViewModel;
    private TextView emptyView;
    private View shareButton;

    private ContactPermissionsBannerView contactPermissionsBannerView;

    private final DestinationsAdapter adapter = new DestinationsAdapter();
    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();
    private final PermissionWatcher permissionWatcher = PermissionWatcher.getInstance();

    public static Intent newComposeShareDestination(
            @NonNull Context context,
            @Nullable ChatId chatId,
            @Nullable GroupId groupId,
            @NonNull ArrayList<Uri> uris,
            @NonNull Bundle editStates,
            @Nullable Uri voiceDrafUri,
            @Nullable String replyPostId,
            int replyPostMediaIndex,
            @Nullable String postText,
            @Nullable List<Mention> mentions
    ) {
        final Intent intent = new Intent(context, ComposeShareDestinationActivity.class);
        intent.putParcelableArrayListExtra(EXTRA_MEDIA, uris);
        intent.putExtra(EXTRA_STATE, editStates);
        if (chatId != null) {
            intent.putExtra(EXTRA_CHAT_ID, chatId);
        }
        if (groupId != null) {
            intent.putExtra(EXTRA_GROUP_ID, groupId);
        }
        if (voiceDrafUri != null) {
            intent.putExtra(EXTRA_VOICE_DRAFT, voiceDrafUri);
        }
        if (replyPostId != null) {
            intent.putExtra(EXTRA_REPLY_POST_ID, replyPostId);
            intent.putExtra(EXTRA_REPLY_POST_MEDIA_INDEX, replyPostMediaIndex);
        }
        if (postText != null) {
            intent.putExtra(EXTRA_POST_TEXT, postText);
        }
        if (mentions != null && !mentions.isEmpty()) {
            intent.putParcelableArrayListExtra(EXTRA_MENTIONS, new ArrayList<>(mentions));
        }
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_compose_share_destination);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

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
        shareButton.setOnClickListener(view -> share());

        contactPermissionsBannerView = findViewById(R.id.contact_permissions_banner);
        contactPermissionsBannerView.bind(permissionWatcher, this);
        contactPermissionsBannerView.setOnClickListener(v -> {
            PermissionUtils.hasOrRequestContactPermissions(this, REQUEST_CODE_ASK_CONTACTS_PERMISSION);
        });

        final RecyclerView listView = findViewById(R.id.list);
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setAdapter(adapter);

        final List<Uri> uris = getIntent().getParcelableArrayListExtra(EXTRA_MEDIA);
        final Bundle editStates = getIntent().getParcelableExtra(EXTRA_STATE);
        final Uri voiceDraftUri = getIntent().getParcelableExtra(EXTRA_VOICE_DRAFT);
        chatId = getIntent().getParcelableExtra(EXTRA_CHAT_ID);
        groupId = getIntent().getParcelableExtra(EXTRA_GROUP_ID);
        final String replyPostId = getIntent().getStringExtra(EXTRA_REPLY_POST_ID);
        final int replyPostMediaIndex = getIntent().getIntExtra(EXTRA_REPLY_POST_MEDIA_INDEX, -1);
        postText = getIntent().getStringExtra(EXTRA_POST_TEXT);
        mentions = getIntent().getParcelableArrayListExtra(EXTRA_MENTIONS);

        shareViewModel = new ViewModelProvider(this, new ShareViewModel.Factory(getApplication(), false)).get(ShareViewModel.class);
        composerViewModel = new ViewModelProvider(this, new ContentComposerViewModel.Factory(getApplication(), chatId, groupId, uris, editStates, voiceDraftUri, null, replyPostId, replyPostMediaIndex)).get(ContentComposerViewModel.class);

        shareViewModel.destinationList.getLiveData().observe(this, adapter::setDestinations);
        shareViewModel.selectionList.observe(this, this::setSelection);
        shareViewModel.feedPrivacyLiveData.getLiveData().observe(this, adapter::setPrivacy);

        composerViewModel.contentItems.observe(this, this::onContentItemsPosted);
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

    private void share() {
        final List<ShareDestination> destinations = composerViewModel.getDestinationsList();
        if (destinations == null || destinations.isEmpty()) {
            Log.w("ComposeShareDestinationActivity: no destination set");
        } else if (TextUtils.isEmpty(postText) && composerViewModel.getEditMedia() == null) {
            Log.w("ComposeShareDestinationActivity: cannot send empty content");
        } else {
            final boolean supportsWideColor = ActivityUtils.supportsWideColor(this);
            composerViewModel.prepareContent(chatId, groupId, postText.trim(), mentions, supportsWideColor);
        }
    }

    private void onContentItemsPosted(@Nullable List<ContentItem> contentItems) {
        if (contentItems == null || contentItems.size() == 0) {
            return;
        }
        for (ContentItem item : contentItems) {
            if (item.urlPreview != null) {
                BgWorkers.getInstance().execute(() -> {
                    if (item.urlPreview.imageMedia != null) {
                        final File imagePreview = FileStore.getInstance().getMediaFile(RandomId.create() + "." + Media.getFileExt(Media.MEDIA_TYPE_IMAGE));
                        try {
                            MediaUtils.transcodeImage(item.urlPreview.imageMedia.file, imagePreview, null, Constants.MAX_IMAGE_DIMENSION, Constants.JPEG_QUALITY, false);
                            item.urlPreview.imageMedia.file = imagePreview;
                        } catch (IOException e) {
                            Log.e("failed to transcode url preview image", e);
                            item.urlPreview.imageMedia = null;
                        }
                    }
                    item.addToStorage(ContentDb.getInstance());
                });
            } else {
                item.addToStorage(ContentDb.getInstance());
            }
        }

        firebaseAnalytics.logEvent("post_sent", null);
        setResult(RESULT_OK);
        finish();
    }

    private void viewContacts() {
        startActivity(ViewMyContactsActivity.viewMyContacts(this));
    }

    private void editFavorites() {
        startActivity(EditFavoritesActivity.openFavorites(this));
    }

    private void setSelection(@NonNull List<ShareDestination> selection) {
        adapter.setSelection(selection);
        composerViewModel.setDestinationsList(selection);
        shareButton.setVisibility(selection.size() > 0 ? View.VISIBLE : View.GONE);
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

        void setDestinations(@NonNull List<ShareDestination> destinations) {
            this.destinations = destinations;
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
            ArrayList<DestinationItem> items = new ArrayList<>(destinations.size() + 1);

            if (!isSearching) {
                items.add(new DestinationItem(DestinationItem.ITEM_FEED_HEADER));
            }

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

                if (dest.type == ShareDestination.TYPE_MY_CONTACTS || dest.type == ShareDestination.TYPE_FAVORITES) {
                    items.add(new DestinationItem(dest, selection.contains(dest), filterTokens, dest.size, privacy.onlyList.size()));
                } else {
                    items.add(new DestinationItem(dest, selection.contains(dest), filterTokens));
                }
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

