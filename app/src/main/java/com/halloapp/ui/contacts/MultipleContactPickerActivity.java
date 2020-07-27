package com.halloapp.ui.contacts;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.id.UserId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MultipleContactPickerActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION = 1;

    private static final String EXTRA_TITLE_RES = "title_res";
    private static final String EXTRA_SELECTED_IDS = "selected_ids";
    public static final String EXTRA_RESULT_SELECTED_IDS = "result_selected_ids";

    private final ContactsAdapter adapter = new ContactsAdapter();
    private final AvatarLoader avatarLoader = AvatarLoader.getInstance(this);
    private ContactsViewModel viewModel;
    private TextView emptyView;

    private HashSet<UserId> initialSelectedContacts;
    private HashSet<UserId> selectedContacts;

    private @DrawableRes int selectionIcon;

    public static Intent newPickerIntent(@NonNull Context context, @Nullable Collection<UserId> selectedIds) {
        return newPickerIntent(context, selectedIds, 0);
    }

    public static Intent newPickerIntent(@NonNull Context context, @Nullable Collection<UserId> selectedIds, @StringRes int title) {
        Intent intent = new Intent(context, MultipleContactPickerActivity.class);
        if (selectedIds != null) {
            intent.putParcelableArrayListExtra(EXTRA_SELECTED_IDS, new ArrayList<>(selectedIds));
        }
        intent.putExtra(EXTRA_TITLE_RES, title);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_contact_picker);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final RecyclerView listView = findViewById(android.R.id.list);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter);
        listView.addItemDecoration(new ContactsSectionItemDecoration(
                getResources().getDimension(R.dimen.contacts_list_item_header_width),
                getResources().getDimension(R.dimen.contacts_list_item_height),
                getResources().getDimension(R.dimen.contacts_list_item_header_text_size),
                getResources().getColor(R.color.contacts_list_item_header_text_color),
                adapter::getSectionName));
        listView.addOnScrollListener(new ActionBarShadowOnScrollListener(this));

        FloatingActionButton fab = findViewById(R.id.saveFab);
        fab.setOnClickListener(v -> {
            saveResult();
        });
        emptyView = findViewById(android.R.id.empty);

        viewModel = new ViewModelProvider(this).get(ContactsViewModel.class);
        viewModel.contactList.getLiveData().observe(this, adapter::setContacts);

        ArrayList<UserId> preselected = getIntent().getParcelableArrayListExtra(EXTRA_SELECTED_IDS);
        if (preselected != null) {
            selectedContacts = new HashSet<>(preselected);
        } else {
            selectedContacts = new HashSet<>();
        }
        initialSelectedContacts = new HashSet<>(selectedContacts);

        @StringRes int title = getIntent().getIntExtra(EXTRA_TITLE_RES, 0);
        if (title != 0) {
            setTitle(title);
        }
        selectionIcon = R.drawable.ic_check;
        loadContacts();
    }

    private boolean didSelectionChange() {
        for (UserId id : initialSelectedContacts) {
            if (!selectedContacts.contains(id)) {
                return true;
            }
        }
        if (initialSelectedContacts.size() != selectedContacts.size()) {
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (didSelectionChange()) {
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

    private void updateTitle() {
        if (getSupportActionBar() != null) {
            if (selectedContacts == null || selectedContacts.size() == 0) {
                getSupportActionBar().setSubtitle(getString(R.string.no_contacts_selected_subtitle));
            } else {
                getSupportActionBar().setSubtitle(getResources().getQuantityString(R.plurals.contact_selection_subtitle, selectedContacts.size(), selectedContacts.size()));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contacts_menu, menu);
        final MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String text) {
                searchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(final String text) {
                adapter.getFilter().filter(text);
                return false;
            }
        });
        return true;
    }

    private void saveResult() {
        Intent i = new Intent();
        i.putParcelableArrayListExtra(EXTRA_RESULT_SELECTED_IDS, new ArrayList<>(selectedContacts));
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case R.id.refresh_contacts: {
                ContactsSync.getInstance(this).startContactsSync(true);
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
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
        final String[] perms = {Manifest.permission.READ_CONTACTS};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, getString(R.string.contacts_permission_rationale),
                    REQUEST_CODE_ASK_CONTACTS_PERMISSION, perms);
        } else {
            viewModel.contactList.invalidate();
        }
    }

    private class ContactsAdapter extends RecyclerView.Adapter<ContactsActivity.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter, Filterable {

        private List<Contact> contacts = new ArrayList<>();
        private List<Contact> filteredContacts;
        private CharSequence filterText;
        private List<String> filterTokens;

        void setContacts(@NonNull List<Contact> contacts) {
            this.contacts = contacts;
            getFilter().filter(filterText);
            updateTitle();
        }

        void setFilteredContacts(@NonNull List<Contact> contacts, CharSequence filterText) {
            this.filteredContacts = contacts;
            this.filterText = filterText;
            this.filterTokens = getFilterTokens(filterText);
            notifyDataSetChanged();
        }

        @Override
        public @NonNull
        ContactsActivity.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ContactViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_select_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ContactsActivity.ViewHolder holder, int position) {
            if (position < getFilteredContactsCount()) {
                holder.bindTo(filteredContacts.get(position), filterTokens);
            }
        }

        @Override
        public int getItemCount() {
            return getFilteredContactsCount();
        }

        @NonNull
        @Override
        public String getSectionName(int position) {
            if (filteredContacts == null || position >= filteredContacts.size()) {
                return "";
            }
            final String name = filteredContacts.get(position).getDisplayName();
            if (TextUtils.isEmpty(name)) {
                return "";
            }
            final int codePoint = name.codePointAt(0);
            return Character.isAlphabetic(codePoint) ? new String(Character.toChars(codePoint)).toUpperCase(Locale.getDefault()) : "#";
        }

        @Override
        public Filter getFilter() {
            return new ContactsFilter(contacts);
        }

        private int getFilteredContactsCount() {
            return filteredContacts == null ? 0 : filteredContacts.size();
        }
    }

    private class ContactsFilter extends Filter {

        private final List<Contact> contacts;

        ContactsFilter(@NonNull List<Contact> contacts) {
            this.contacts = contacts;
        }

        @Override
        protected FilterResults performFiltering(@Nullable CharSequence prefix) {
            final FilterResults results = new FilterResults();
            final List<String> filterTokens = getFilterTokens(prefix);
            if (filterTokens == null) {
                results.values = contacts;
                results.count = contacts.size();
            } else {
                final ArrayList<Contact> filteredContacts = new ArrayList<>();
                for (Contact contact : contacts) {
                    final String name = contact.getDisplayName();
                    final List<String> words = getFilterTokens(name);
                    if (words != null) {
                        boolean match = true;
                        for (String filterToken : filterTokens) {
                            boolean tokenMatch = false;
                            for (String word : words) {
                                if (word.startsWith(filterToken)) {
                                    tokenMatch = true;
                                    break;
                                }
                            }
                            if (!tokenMatch) {
                                match = false;
                                break;
                            }
                        }
                        if (match) {
                            filteredContacts.add(contact);
                        }
                    }
                }
                results.values = filteredContacts;
                results.count = filteredContacts.size();
            }
            return results;
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

    static @Nullable List<String> getFilterTokens(final @Nullable CharSequence filterText) {
        if (TextUtils.isEmpty(filterText)) {
            return null;
        }
        final List<String> filterTokens = new ArrayList<>();
        final BreakIterator boundary = BreakIterator.getWordInstance();
        final String filterTextString = filterText.toString();
        boundary.setText(filterTextString);
        int start = boundary.first();
        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
            if (end > start) {
                filterTokens.add(filterTextString.substring(start, end).toLowerCase());
            }
        }
        return filterTokens;
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
                if (selectedContacts.contains(contact.userId)) {
                    selectedContacts.remove(contact.userId);
                } else {
                    selectedContacts.add(contact.userId);
                }
                updateSelectionIcon();
                updateTitle();
            });
        }

        private void updateSelectionIcon() {
            boolean selected = selectedContacts.contains(contact.userId);
            if (selected) {
                selectionView.setImageResource(selectionIcon);
            } else {
                selectionView.setImageDrawable(null);
            }
            selectionView.setSelected(selected);
        }

        void bindTo(@NonNull Contact contact, List<String> filterTokens) {
            this.contact = contact;
           updateSelectionIcon();
            avatarLoader.load(avatarView, Preconditions.checkNotNull(contact.userId));
            if (filterTokens != null && !filterTokens.isEmpty()) {
                SpannableString formattedName = null;
                final BreakIterator boundary = BreakIterator.getWordInstance();
                final String name = contact.getDisplayName();
                boundary.setText(name);
                int start = boundary.first();
                for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
                    if (end <= start) {
                        continue;
                    }
                    final String word = name.substring(start, end).toLowerCase();
                    for (String filterToken : filterTokens) {
                        if (word.startsWith(filterToken)) {
                            if (formattedName == null) {
                                formattedName = new SpannableString(name);
                            }
                            formattedName.setSpan(new ForegroundColorSpan(ContextCompat.getColor(itemView.getContext(), R.color.search_highlight)), start, Math.min(end, start + filterToken.length()), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                }
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
