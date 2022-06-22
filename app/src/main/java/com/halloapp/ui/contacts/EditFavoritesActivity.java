package com.halloapp.ui.contacts;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.id.UserId;
import com.halloapp.permissions.PermissionUtils;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.SystemUiVisibility;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.FilterUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.SnackbarHelper;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class EditFavoritesActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION = 1;

    public static final String EXTRA_FOR_SINGLE_POST = "for_single_post";

    private final ContactsAdapter adapter = new ContactsAdapter();
    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();
    private EditFavoritesViewModel viewModel;
    private TextView emptyView;
    private EditText searchBox;
    private RecyclerView listView;
    private TextView titleView;

    private TextView editButton;

    private HashSet<UserId> initialSelectedContacts;
    protected LinkedHashSet<UserId> selectedContacts;

    private HashSet<UserId> favorites;

    private @DrawableRes int selectionIcon;

    private boolean editFavorites = false;
    private boolean addFavorites = false;
    private boolean hasChanges = false;

    private boolean forSinglePost;

    public static Intent openFavorites(@NonNull Context context) {
        return openFavorites(context, false);
    }

    public static Intent openFavorites(@NonNull Context context, boolean forSinglePost) {
        Intent i = new Intent(context, EditFavoritesActivity.class);
        i.putExtra(EXTRA_FOR_SINGLE_POST, forSinglePost);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_edit_favorites);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        titleView = toolbar.findViewById(R.id.toolbar_title);
        editButton = findViewById(R.id.edit_btn);
        editButton.setOnClickListener(v -> {
            if (!editFavorites) {
                editFavorites = true;
                titleView.setText(R.string.edit_favorites_title);
                editButton.setText(R.string.done);
                hasChanges = false;
                adapter.collapseFavorites();
                adapter.refreshSections();
            } else {
                if (hasChanges) {
                    List<UserId> newFavorites = new ArrayList<>(selectedContacts);
                    viewModel.saveFavorites(newFavorites).observe(this, done -> {
                        if (done != null) {
                            if (done) {
                                Toast.makeText(this, R.string.feed_privacy_update_success, Toast.LENGTH_LONG).show();
                                if (addFavorites) {
                                    setResult(RESULT_OK);
                                    finish();
                                } else {
                                    favorites.clear();
                                    favorites.addAll(newFavorites);
                                    exitEditMode();
                                }
                            } else {
                                SnackbarHelper.showWarning(this, R.string.feed_privacy_update_failure);
                            }
                        }
                    });
                } else {
                    exitEditMode();
                }
            }
        });
        editButton.setVisibility(View.VISIBLE);

        searchBox = findViewById(R.id.search_text);
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

        listView = findViewById(android.R.id.list);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter);
        listView.addItemDecoration(new ContactsSectionItemDecoration(
                getResources().getDimension(R.dimen.contacts_list_item_header_width),
                getResources().getDimension(R.dimen.contacts_list_item_height),
                getResources().getDimension(R.dimen.contacts_list_item_header_text_size),
                getResources().getColor(R.color.contacts_list_item_header_text_color),
                adapter::getSectionName));

        emptyView = findViewById(android.R.id.empty);

        forSinglePost = getIntent().getBooleanExtra(EXTRA_FOR_SINGLE_POST, false);

        favorites = new HashSet<>();
        selectedContacts = new LinkedHashSet<>();
        initialSelectedContacts = new HashSet<>(selectedContacts);

        viewModel = new ViewModelProvider(this, new EditFavoritesViewModel.Factory(getApplication(), initialSelectedContacts)).get(EditFavoritesViewModel.class);
        viewModel.favoritesList.getLiveData().observe(this, priv -> {
            if (priv == null || priv.onlyList == null || priv.onlyList.isEmpty()) {
                addFavoritesMode();
            } else {
                favorites = new HashSet<>(priv.onlyList);
                selectedContacts = new LinkedHashSet<>(priv.onlyList);
                initialSelectedContacts = new HashSet<>(selectedContacts);
                adapter.notifyDataSetChanged();
            }
        });
        viewModel.contactList.getLiveData().observe(this, adapter::setContacts);
        selectionIcon = R.drawable.ic_check;
        loadContacts();
    }

    private void exitEditMode() {
        titleView.setText(R.string.contact_favorites);
        editButton.setText(R.string.edit);
        editFavorites = false;
        adapter.refreshSections();
    }

    private void addFavoritesMode() {
        addFavorites = true;
        editFavorites = true;
        titleView.setText(R.string.add_favorites_title);
        editButton.setText(R.string.done);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if (editFavorites && hasChanges) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.alert_discard_changes_message);
            builder.setNegativeButton(R.string.cancel, null);
            builder.setPositiveButton(R.string.action_discard, (dialog, which) -> {
                finish();
            });
            builder.create().show();
        } else {
            super.onBackPressed();
        }
    }

    private void updateToolbar() {
    }

    private void clearSearchBar() {
        searchBox.setText("");
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> list) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {
            case REQUEST_CODE_ASK_CONTACTS_PERMISSION: {
                loadContacts();
                break;
            }
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            //noinspection SwitchStatementWithTooFewBranches
            switch (requestCode) {
                case REQUEST_CODE_ASK_CONTACTS_PERMISSION: {
                    new AppSettingsDialog.Builder(this)
                            .setRationale(getString(R.string.contacts_permission_rationale_denied))
                            .build().show();
                    break;
                }
            }
        }
    }

    private void loadContacts() {
        if (PermissionUtils.hasOrRequestContactPermissions(this, REQUEST_CODE_ASK_CONTACTS_PERMISSION)) {
            viewModel.contactList.invalidate();
        }
    }

    private class ContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter, Filterable {

        private static final int COLLAPSED_FAVORITES_MAX = 5;

        private static final int TYPE_CONTACT = 1;
        private static final int TYPE_FAVORITES_HEADER = 2;
        private static final int TYPE_MY_CONTACTS_HEADER = 3;
        private static final int TYPE_SHOW_MORE = 4;

        private List<Contact> contacts = new ArrayList<>();

        private List<Contact> favoriteSection = new ArrayList<>();

        private List<Contact> filteredContacts;
        private CharSequence filterText;
        private List<String> filterTokens;

        private boolean favoritesCollapsed = true;

        private int favoritesHeaderIndex = -1;
        private int myContactsHeaderIndex = -1;
        private int moreIndex = -1;

        void setContacts(@NonNull List<Contact> contacts) {
            this.contacts = contacts;
            getFilter().filter(filterText);
            updateToolbar();
        }

        public void refreshSections() {
            getFilter().filter(filterText);
        }

        void setFilteredContacts(@NonNull List<Contact> contacts, CharSequence filterText) {
            contacts = new ArrayList<>(contacts);
            this.filteredContacts = contacts;
            this.filterText = filterText;
            this.filterTokens = FilterUtils.getFilterTokens(filterText);
            favoriteSection.clear();
            moreIndex = -1;
            ListIterator<Contact> contactListIterator = contacts.listIterator();
            while (contactListIterator.hasNext()) {
                Contact contact = contactListIterator.next();
                if (favorites.contains(contact.userId)) {
                    favoriteSection.add(contact);
                    contactListIterator.remove();
                }
            }
            if (favoritesCollapsed && editFavorites) {
                if (favoriteSection.size() > COLLAPSED_FAVORITES_MAX) {
                    favoriteSection = favoriteSection.subList(0, COLLAPSED_FAVORITES_MAX);
                    moreIndex = COLLAPSED_FAVORITES_MAX;
                }
            }
            favoritesHeaderIndex = favoriteSection.isEmpty() ? -1 : 0;
            myContactsHeaderIndex = favoriteSection.isEmpty() ? 0 : favoriteSection.size() + 1;
            notifyDataSetChanged();
        }

        @Override
        public @NonNull
        RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_CONTACT) {
                return new ContactViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_select_item, parent, false));
            } else if (viewType == TYPE_SHOW_MORE) {
                return new MoreViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.favorites_view_more, parent, false));
            } else {
                return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorites_header, parent, false));
            }
        }

        private Contact getOffsetContact(int position) {
            if (myContactsHeaderIndex >= 0) {
                if (position > myContactsHeaderIndex) {
                    position--;
                }
            }
            if (favoritesHeaderIndex >= 0) {
                position--;
            }
            Contact contact;
            if (position < favoriteSection.size()) {
                contact = favoriteSection.get(position);
            } else {
                contact = filteredContacts.get(position - favoriteSection.size());
            }
            return contact;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ContactsActivity.ViewHolder) {
                Contact contact = getOffsetContact(position);
                ((ContactsActivity.ViewHolder) holder).bindTo(contact, filterTokens);
            } else if (holder instanceof HeaderViewHolder) {
                if (position == favoritesHeaderIndex) {
                    ((HeaderViewHolder) holder).setText((editFavorites || !forSinglePost) ? R.string.contact_favorites : R.string.setting_feed_privacy_single_post_title);
                } else if (position == myContactsHeaderIndex) {
                    ((HeaderViewHolder) holder).setText(R.string.my_ha_contacts);
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == myContactsHeaderIndex) {
                return TYPE_MY_CONTACTS_HEADER;
            }
            if (position == favoritesHeaderIndex) {
                return TYPE_FAVORITES_HEADER;
            }
            if (position == moreIndex) {
                return TYPE_SHOW_MORE;
            }
            return TYPE_CONTACT;
        }

        private int getFavoritesItemCount() {
            return favoriteSection.size();
        }

        @Override
        public int getItemCount() {
            return getFavoritesItemCount() + getFilteredContactsCount() + (favoritesHeaderIndex < 0 ? 0 : 1) + ((myContactsHeaderIndex < 0 || !editFavorites) ? 0 : 1);
        }

        @NonNull
        @Override
        public String getSectionName(int position) {
            if (position == favoritesHeaderIndex || position == myContactsHeaderIndex) {
                return "";
            }
            if (favoritesHeaderIndex >= 0) {
                if (position > favoritesHeaderIndex) {
                    position--;
                }
            }
            if (position < favoriteSection.size()) {
                return "";
            }
            if (myContactsHeaderIndex >= 0) {
                if (position >= myContactsHeaderIndex) {
                    position--;
                }
            }
            position -= favoriteSection.size();
            if (filteredContacts == null || position >= filteredContacts.size()) {
                return "";
            }
            Contact contact = filteredContacts.get(position);
            final String name = contact.getDisplayName();
            if (TextUtils.isEmpty(name)) {
                return "";
            }
            final int codePoint = name.codePointAt(0);
            return Character.isAlphabetic(codePoint) ? new String(Character.toChars(codePoint)).toUpperCase(Locale.getDefault()) : "#";
        }

        public void collapseFavorites() {
            favoritesCollapsed = true;
        }

        public void showMore() {
            favoritesCollapsed = false;
            refreshSections();
        }

        @Override
        public Filter getFilter() {
            return new ContactsFilter(contacts);
        }

        private int getFilteredContactsCount() {
            if (editFavorites) {
                return filteredContacts == null ? 0 : filteredContacts.size();
            }
            return 0;
        }
    }

    private class ContactsFilter extends FilterUtils.ItemFilter<Contact> {

        ContactsFilter(@NonNull List<Contact> contacts) {
            super(contacts);
        }

        @Override
        protected String itemToString(Contact contact) {
            return contact.getDisplayName();
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            final List<Contact> filteredContacts = (List<Contact>) results.values;
            adapter.setFilteredContacts(filteredContacts, constraint);
            if (filteredContacts.isEmpty() && !TextUtils.isEmpty(constraint)) {
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText(getString(R.string.contact_search_empty, constraint));
            } else {
                emptyView.setVisibility(View.GONE);
            }
        }
    }

    class MoreViewHolder extends RecyclerView.ViewHolder {

        public MoreViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(v -> {
                adapter.showMore();
            });
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView headerTv;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);

            headerTv = itemView.findViewById(R.id.section_header);
        }

        public void setText(@StringRes int res) {
            headerTv.setText(res);
        }
    }

    class ContactViewHolder extends ContactsActivity.ViewHolder {

        final private ImageView avatarView;
        final private TextView nameView;
        final private TextView phoneView;
        final private ImageView selectionView;

        private Contact contact;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.avatar);
            nameView = itemView.findViewById(R.id.name);
            phoneView = itemView.findViewById(R.id.phone);
            selectionView = itemView.findViewById(R.id.selection_indicator);
            itemView.setOnClickListener(v -> {
                if (contact == null) {
                    return;
                }
                if (!editFavorites) {
                    return;
                }
                hasChanges = true;
                if (selectedContacts.contains(contact.userId)) {
                    selectedContacts.remove(contact.userId);
                } else {
                    selectedContacts.add(contact.userId);
                }
                updateSelectionIcon();
                updateToolbar();
                clearSearchBar();
            });
        }

        private void updateSelectionIcon() {
            if (editFavorites) {
                boolean selected = selectedContacts.contains(contact.userId);
                if (selected) {
                    selectionView.setImageResource(selectionIcon);
                } else {
                    selectionView.setImageDrawable(null);
                }
                selectionView.setVisibility(View.VISIBLE);
                selectionView.setSelected(selected);
            } else {
                selectionView.setVisibility(View.GONE);
            }
        }

        void bindTo(@NonNull Contact contact, List<String> filterTokens) {
            itemView.setAlpha(1);
            itemView.setClickable(true);
            this.contact = contact;
            updateSelectionIcon();
            avatarLoader.load(avatarView, Preconditions.checkNotNull(contact.userId), false);
            if (filterTokens != null && !filterTokens.isEmpty()) {
                String name = contact.getDisplayName();
                CharSequence formattedName = FilterUtils.formatMatchingText(EditFavoritesActivity.this, name, filterTokens);
                if (formattedName != null) {
                    nameView.setText(formattedName);
                } else {
                    nameView.setText(name);
                }
            } else {
                nameView.setText(contact.getDisplayName());
            }
            phoneView.setText(contact.getDisplayPhone());
        }
    }
}
