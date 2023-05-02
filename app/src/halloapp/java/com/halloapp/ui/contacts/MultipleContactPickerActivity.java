package com.halloapp.ui.contacts;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
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
import com.halloapp.nux.InviteGroupBottomSheetDialogFragment;
import com.halloapp.permissions.PermissionUtils;
import com.halloapp.permissions.PermissionWatcher;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.SystemUiVisibility;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.DialogFragmentUtils;
import com.halloapp.util.FilterUtils;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.AllowContactsView;
import com.halloapp.widget.SnackbarHelper;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MultipleContactPickerActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION = 1;

    public static final String EXTRA_SELECTED_IDS = "selected_ids";
    protected static final String EXTRA_TITLE_RES = "title_res";
    protected static final String EXTRA_ACTION_RES = "action_res";
    protected static final String EXTRA_DISABLED_IDS = "disabled_ids";
    protected static final String EXTRA_MAX_SELECTION = "max_selection";
    protected static final String EXTRA_ONLY_FRIENDS = "only_friends";
    protected static final String EXTRA_ALLOW_EMPTY_SELECTION = "allow_empty_selection";
    public static final String EXTRA_INVITE_LINK = "group_invite_link";
    public static final String EXTRA_RESULT_SELECTED_IDS = "result_selected_ids";

    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();
    private final PermissionWatcher permissionWatcher = PermissionWatcher.getInstance();

    private final ContactsAdapter adapter = new ContactsAdapter();
    private final SelectedAdapter avatarsAdapter = new SelectedAdapter();

    private final ContactLoader contactLoader = new ContactLoader(userId -> {
        startActivity(ViewProfileActivity.viewProfile(this, userId));
        return null;
    });
    private ContactsViewModel viewModel;
    private TextView emptyView;
    private EditText searchBox;
    private RecyclerView listView;
    private RecyclerView avatarsView;

    private View searchContainer;
    private AllowContactsView allowContactsView;

    private HashSet<UserId> initialSelectedContacts;
    protected LinkedHashSet<UserId> selectedContacts;
    private Map<UserId, Contact> contactMap = new HashMap<>();
    private final ArrayList<UserId> disabledContacts = new ArrayList<>();

    private @DrawableRes int selectionIcon;
    private int maxSelection = -1;
    private boolean allowEmptySelection;

    private boolean showedInviteBottomSheet;
    private String groupInviteLink;

    private MenuItem finishMenuItem;

    public static Intent newPickerIntentAllowEmpty(@NonNull Context context, @Nullable Collection<UserId> selectedIds, @StringRes int title) {
        Intent intent = newPickerIntent(context, selectedIds, title, R.string.action_save, null);
        intent.putExtra(EXTRA_ALLOW_EMPTY_SELECTION, true);
        return intent;
    }
    public static Intent newPickerIntentAddOnly(@NonNull Context context, @Nullable Collection<UserId> disabledIds, @Nullable Integer maxSelection, @StringRes int title, @StringRes int action) {
        Intent intent = newPickerIntent(context, null, title, action, maxSelection);
        if (disabledIds != null) {
            intent.putParcelableArrayListExtra(EXTRA_DISABLED_IDS, new ArrayList<>(disabledIds));
        }
        return intent;
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

        allowContactsView = findViewById(R.id.contact_permissions_view);
        allowContactsView.setOnAllowClick(v -> {
            PermissionUtils.hasOrRequestContactPermissions(this, REQUEST_CODE_ASK_CONTACTS_PERMISSION);
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
        searchContainer = findViewById(R.id.search_container);

        avatarsView = findViewById(R.id.avatars);
        final LinearLayoutManager avatarsLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        avatarsView.setLayoutManager(avatarsLayoutManager);
        avatarsView.setAdapter(avatarsAdapter);

        ArrayList<UserId> preselected = getIntent().getParcelableArrayListExtra(EXTRA_SELECTED_IDS);
        if (preselected != null) {
            selectedContacts = new LinkedHashSet<>(preselected);
        } else {
            selectedContacts = new LinkedHashSet<>();
        }
        initialSelectedContacts = new HashSet<>(selectedContacts);
        avatarsAdapter.setUserIds(initialSelectedContacts);

        viewModel = new ViewModelProvider(this, new ContactsViewModel.Factory(getApplication(), initialSelectedContacts)).get(ContactsViewModel.class);

        ArrayList<UserId> disabledInput = getIntent().getParcelableArrayListExtra(EXTRA_DISABLED_IDS);
        if (disabledInput != null) {
            disabledContacts.addAll(disabledInput);
        }
        groupInviteLink = getIntent().getStringExtra(EXTRA_INVITE_LINK);
        viewModel.contactList.getLiveData().observe(this, allContacts -> {
            Map<UserId, Contact> map = new HashMap<>();
            for (Contact contact : allContacts) {
                map.put(contact.userId, contact);
            }
            this.contactMap = map;
            adapter.setContacts(allContacts);
            avatarsAdapter.setUserIds(selectedContacts);
            if (allContacts.isEmpty() && !TextUtils.isEmpty(groupInviteLink) && !showedInviteBottomSheet) {
                showedInviteBottomSheet = true;
                DialogFragmentUtils.showDialogFragmentOnce(InviteGroupBottomSheetDialogFragment.newInstance(groupInviteLink), getSupportFragmentManager());
            }
        });
        adapter.setInviteLink(groupInviteLink);

        maxSelection = getIntent().getIntExtra(EXTRA_MAX_SELECTION, -1);
        allowEmptySelection = getIntent().getBooleanExtra(EXTRA_ALLOW_EMPTY_SELECTION, false);

        @StringRes int title = getIntent().getIntExtra(EXTRA_TITLE_RES, 0);
        if (title != 0) {
            setTitle(title);
        }
        selectionIcon = R.drawable.ic_check;

        permissionWatcher.getPermissionLiveData(Manifest.permission.READ_CONTACTS).observe(this, enabled -> {
            if (enabled == null || !enabled) {
                searchContainer.setVisibility(View.GONE);
                allowContactsView.setVisibility(View.VISIBLE);
            } else {
                searchContainer.setVisibility(View.VISIBLE);
                allowContactsView.setVisibility(View.GONE);
            }
        });

        loadContacts();
    }

    protected int getMaxSelection() {
        return maxSelection;
    }

    private boolean didSelectionChange() {
        for (UserId id : initialSelectedContacts) {
            if (!selectedContacts.contains(id)) {
                return true;
            }
        }
        return initialSelectedContacts.size() != selectedContacts.size();
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
        boolean allowSelection = hasSelection || allowEmptySelection;
        if (finishMenuItem != null) {
            SpannableString ss = new SpannableString(getActionText());
            ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getBaseContext(), allowSelection ? R.color.color_secondary : R.color.disabled_text)), 0, ss.length(), 0);
            finishMenuItem.setTitle(ss);
            finishMenuItem.setEnabled(allowSelection);
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
        if (item.getItemId() == R.id.finish) {
            saveResult();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

        private static final int TYPE_CONTACT = 1;
        private static final int TYPE_INVITE_LINK = 2;

        private List<Contact> contacts = new ArrayList<>();
        private List<Contact> filteredContacts;
        private CharSequence filterText;
        private List<String> filterTokens;

        private String inviteLink;

        void setInviteLink(@Nullable String inviteLink) {
            this.inviteLink = inviteLink;
        }

        void setContacts(@NonNull List<Contact> contacts) {
            this.contacts = contacts;
            getFilter().filter(filterText);
            updateToolbar();
        }

        void setFilteredContacts(@NonNull List<Contact> contacts, CharSequence filterText) {
            this.filteredContacts = contacts;
            this.filterText = filterText;
            this.filterTokens = FilterUtils.getFilterTokens(filterText);
            notifyDataSetChanged();
        }

        @Override
        public @NonNull
        RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_CONTACT) {
                return new ContactViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_select_item, parent, false));
            } else {
                return new InviteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_group_invite_link, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ContactsActivity.ViewHolder) {
                if (position < getFilteredContactsCount()) {
                    Contact contact = filteredContacts.get(position);
                    ((ContactsActivity.ViewHolder)holder).bindTo(contact, filterTokens);
                }
            } else if (holder instanceof InviteViewHolder) {
                ((InviteViewHolder) holder).bind(inviteLink);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (inviteLink != null && position == getItemCount() - 1) {
                return TYPE_INVITE_LINK;
            }
            return TYPE_CONTACT;
        }

        @Override
        public int getItemCount() {
            return getFilteredContactsCount() + (inviteLink == null ? 0 : 1);
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

    static class InviteViewHolder extends RecyclerView.ViewHolder {

        private String inviteLink;

        public InviteViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(v -> {
                if (inviteLink != null) {
                    itemView.getContext().startActivity(IntentUtils.createShareTextIntent(inviteLink));
                }
            });
        }

        public void bind(@Nullable String inviteLink) {
            this.inviteLink = inviteLink;
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
                if (disabledContacts.contains(contact.userId)) {
                    return;
                }
                if (selectedContacts.contains(contact.userId)) {
                    selectedContacts.remove(contact.userId);
                    avatarsAdapter.removeUserId(contact.userId);
                } else if (getMaxSelection() >= 1 && selectedContacts.size() >= getMaxSelection()) {
                    SnackbarHelper.showWarning(listView, getResources().getQuantityString(R.plurals.contact_maximum_selection, getMaxSelection(), getMaxSelection()));
                } else {
                    selectedContacts.add(contact.userId);
                    avatarsAdapter.addUserId(contact.userId);
                    avatarsView.scrollToPosition(selectedContacts.size() - 1);
                }
                updateSelectionIcon();
                updateToolbar();
                clearSearchBar();
            });
        }

        private void updateSelectionIcon() {
            boolean selected = selectedContacts.contains(contact.userId) || disabledContacts.contains(contact.userId);
            if (selected) {
                selectionView.setImageResource(selectionIcon);
            } else {
                selectionView.setImageDrawable(null);
            }
            selectionView.setSelected(selected);
        }

        void bindTo(@NonNull Contact contact, List<String> filterTokens) {
            boolean disabled = disabledContacts.contains(contact.userId);
            if (disabled) {
                itemView.setAlpha(0.54f);
                itemView.setClickable(false);
            } else {
                itemView.setAlpha(1);
                itemView.setClickable(true);
            }
            this.contact = contact;
            updateSelectionIcon();
            avatarLoader.load(avatarView, Preconditions.checkNotNull(contact.userId), false);
            if (filterTokens != null && !filterTokens.isEmpty()) {
                String name = contact.getDisplayName();
                CharSequence formattedName = FilterUtils.formatMatchingText(MultipleContactPickerActivity.this, name, filterTokens);
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

        final ImageView avatar;
        final ImageView remove;
        final TextView name;

        AvatarViewHolder(@NonNull View itemView) {
            super(itemView);

            avatar = itemView.findViewById(R.id.avatar);
            remove = itemView.findViewById(R.id.remove);
            name = itemView.findViewById(R.id.name);
        }

        void bindTo(@NonNull UserId userId) {
            avatarLoader.load(avatar, userId, false);
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
        private final HashMap<String, Long> userIdMap = new HashMap<>();

        private long idIndex = 0;

        SelectedAdapter() {
            setHasStableIds(true);
        }

        void removeUserId(UserId userId) {
            ListIterator<Contact> contactListIterator = contacts.listIterator();
            int index = -1;
            while(contactListIterator.hasNext()) {
                int tmpIndex = contactListIterator.nextIndex();
                Contact c = contactListIterator.next();
                if (userId.equals(c.userId)) {
                    contactListIterator.nextIndex();
                    contactListIterator.remove();
                    index = tmpIndex;
                    break;
                }
            }
            if (index != -1) {
                notifyItemRemoved(index);
            }

        }

        void addUserId(UserId userId) {
            Contact contact = contactMap.get(userId);
            if (contact != null) {
                contacts.add(contact);
                String id = userId.rawId();
                if (!userIdMap.containsKey(id)) {
                    userIdMap.put(userId.rawId(), idIndex++);
                }
            }
            notifyItemInserted(contacts.size() - 1);
        }

        void setUserIds(@NonNull Set<UserId> contacts) {
            List<Contact> result = new ArrayList<>();
            for (UserId userId : contacts) {
                Contact contact = contactMap.get(userId);
                if (contact != null) {
                    result.add(contact);
                    String id = userId.rawId();
                    if (!userIdMap.containsKey(id)) {
                        userIdMap.put(userId.rawId(), idIndex++);
                    }
                }
            }
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
                holder.bindTo(Preconditions.checkNotNull(contact.userId));
            }
        }

        @Override
        public long getItemId(int position) {
            UserId userId = contacts.get(position).userId;
            if (userId == null) {
                return -1;
            }
            Long id = userIdMap.get(userId.rawId());
            if (id != null) {
                return id;
            }
            return -1;
        }

        @Override
        public int getItemCount() {
            return contacts.size();
        }

    }
}
