package com.halloapp.ui.contacts;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.id.UserId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.SystemUiVisibility;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.widget.SnackbarHelper;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MultipleContactPickerActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION = 1;

    private static final String EXTRA_TITLE_RES = "title_res";
    private static final String EXTRA_ACTION_RES = "action_res";
    private static final String EXTRA_SELECTED_IDS = "selected_ids";
    private static final String EXTRA_MAX_SELECTION = "max_selection";
    public static final String EXTRA_RESULT_SELECTED_IDS = "result_selected_ids";

    private final ContactsAdapter adapter = new ContactsAdapter();
    private final SelectedAdapter avatarsAdapter = new SelectedAdapter();
    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();
    private final ContactLoader contactLoader = new ContactLoader();
    private ContactsViewModel viewModel;
    private TextView emptyView;
    private EditText searchBox;
    private RecyclerView avatarsView;

    private HashSet<UserId> initialSelectedContacts;
    private HashSet<UserId> selectedContacts;
    private Map<UserId, Contact> contactMap = new HashMap<>();

    private @DrawableRes int selectionIcon;
    private int maxSelection = -1;

    private MenuItem finishMenuItem;

    public static Intent newPickerIntent(@NonNull Context context, @Nullable Collection<UserId> selectedIds, @StringRes int title) {
        return newPickerIntent(context, selectedIds, title, R.string.action_save, null);
    }

    public static Intent newPickerIntent(@NonNull Context context, @Nullable Collection<UserId> selectedIds, @StringRes int title, @StringRes int action, @Nullable Integer maxSelection) {
        Intent intent = new Intent(context, MultipleContactPickerActivity.class);
        if (selectedIds != null) {
            if (maxSelection != null && maxSelection >= 1 && selectedIds.size() > maxSelection) {
                Log.e("Starting selection larger than max allowed size");
            }
            intent.putParcelableArrayListExtra(EXTRA_SELECTED_IDS, new ArrayList<>(selectedIds));
        }
        intent.putExtra(EXTRA_TITLE_RES, title);
        intent.putExtra(EXTRA_ACTION_RES, action);
        intent.putExtra(EXTRA_MAX_SELECTION, maxSelection);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_multi_contact_picker);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

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
        searchBox.requestFocus();

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

        emptyView = findViewById(android.R.id.empty);

        avatarsView = findViewById(R.id.avatars);
        final RecyclerView.LayoutManager avatarsLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        avatarsView.setLayoutManager(avatarsLayoutManager);
        avatarsView.setAdapter(avatarsAdapter);

        viewModel = new ViewModelProvider(this).get(ContactsViewModel.class);
        viewModel.contactList.getLiveData().observe(this, contacts -> {
            Map<UserId, Contact> map = new HashMap<>();
            for (Contact contact : contacts) {
                map.put(contact.userId, contact);
            }
            this.contactMap = map;
            adapter.setContacts(contacts);
            avatarsAdapter.setUserIds(selectedContacts);
        });

        ArrayList<UserId> preselected = getIntent().getParcelableArrayListExtra(EXTRA_SELECTED_IDS);
        if (preselected != null) {
            selectedContacts = new HashSet<>(preselected);
        } else {
            selectedContacts = new HashSet<>();
        }
        initialSelectedContacts = new HashSet<>(selectedContacts);
        avatarsAdapter.setUserIds(initialSelectedContacts);

        maxSelection = getIntent().getIntExtra(EXTRA_MAX_SELECTION, -1);

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

    private String getActionText() {
        @StringRes int string = getIntent().getIntExtra(EXTRA_ACTION_RES, R.string.action_save);
        return getString(string);
    }

    private void updateToolbar() {
        boolean hasSelection = selectedContacts != null && selectedContacts.size() != 0;
        if (getSupportActionBar() != null) {
            if (hasSelection) {
                getSupportActionBar().setSubtitle(getResources().getQuantityString(R.plurals.contact_selection_subtitle, selectedContacts.size(), selectedContacts.size()));
            } else {
                getSupportActionBar().setSubtitle(getString(R.string.no_contacts_selected_subtitle));
            }
        }
        if (finishMenuItem != null) {
            SpannableString ss = new SpannableString(getActionText());
            ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getBaseContext(), hasSelection ? R.color.color_secondary : R.color.button_disabled_background)), 0, ss.length(), 0);
            finishMenuItem.setTitle(ss);
            finishMenuItem.setEnabled(hasSelection);
        }
        if (avatarsView != null) {
            avatarsView.setVisibility(hasSelection ? View.VISIBLE : View.GONE);
        }
    }

    private void clearSearchBar() {
        searchBox.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.multiple_contact_picker_menu, menu);
        finishMenuItem = menu.findItem(R.id.finish);
        updateToolbar();
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
            case R.id.finish: {
                saveResult();
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
            updateToolbar();
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
                } else if (maxSelection >= 1 && selectedContacts.size() >= maxSelection) {
                    SnackbarHelper.showWarning(MultipleContactPickerActivity.this, getResources().getQuantityString(R.plurals.contact_maximum_selection, maxSelection, maxSelection));
                } else {
                    selectedContacts.add(contact.userId);
                }
                avatarsAdapter.setUserIds(selectedContacts);
                updateSelectionIcon();
                updateToolbar();
                clearSearchBar();
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

    static class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void bindTo(@NonNull UserId userId) {
        }
    }

    class AvatarViewHolder extends ViewHolder {

        ImageView avatar;
        ImageView remove;
        TextView name;

        AvatarViewHolder(@NonNull View itemView) {
            super(itemView);

            avatar = itemView.findViewById(R.id.avatar);
            remove = itemView.findViewById(R.id.remove);
            name = itemView.findViewById(R.id.name);
        }

        void bindTo(@NonNull UserId userId) {
            avatarLoader.load(avatar, userId);
            contactLoader.load(name, userId, new ViewDataLoader.Displayer<TextView, Contact>() {
                @Override
                public void showResult(@NonNull TextView view, @Nullable Contact result) {
                    if (result != null) {
                        view.setText(result.getShortName());
                    }
                }

                @Override
                public void showLoading(@NonNull TextView view) {
                    view.setText("");
                }
            });

            remove.setOnClickListener(v -> {
                selectedContacts.remove(userId);
                adapter.notifyDataSetChanged();
                avatarsAdapter.setUserIds(selectedContacts);
                updateToolbar();
            });
        }
    }

    private class SelectedAdapter extends RecyclerView.Adapter<ViewHolder> {
        private List<Contact> contacts = new ArrayList<>();

        void setUserIds(@NonNull Set<UserId> contacts) {
            List<UserId> list = new ArrayList<>(contacts);
            List<Contact> result = new ArrayList<>();
            for (UserId userId : list) {
                Contact contact = contactMap.get(userId);
                if (contact != null) {
                    result.add(contact);
                }
            }
            Collections.sort(result, new Comparator<Contact>() {
                @Override
                public int compare(Contact o1, Contact o2) {
                    return o1.getDisplayName().compareTo(o2.getDisplayName());
                }
            });
            this.contacts = result;
            notifyDataSetChanged();
        }

        @Override
        public @NonNull
        ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new AvatarViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.avatar_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (position < contacts.size()) {
                Contact contact = contacts.get(position);
                holder.bindTo(contacts.get(position).userId);
            }
        }

        @Override
        public int getItemCount() {
            return contacts.size();
        }

    }
}
