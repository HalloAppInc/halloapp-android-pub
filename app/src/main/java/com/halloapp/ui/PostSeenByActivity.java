package com.halloapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.invites.InviteContactsActivity;
import com.halloapp.ui.settings.SettingsPrivacy;
import com.halloapp.util.DialogFragmentUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.widget.SnackbarHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PostSeenByActivity extends HalloActivity {

    public static final String EXTRA_POST_ID = "post_id";

    private final ContactsAdapter adapter = new ContactsAdapter();
    private PostSeenByViewModel viewModel;
    private MediaThumbnailLoader mediaThumbnailLoader;
    private AvatarLoader avatarLoader;
    private TimestampRefresher timestampRefresher;

    private String postId;

    private Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_seen_by);
        setTitle(R.string.seen_by);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final RecyclerView seenByView = findViewById(R.id.seen_by_list);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        seenByView.setLayoutManager(layoutManager);
        seenByView.setAdapter(adapter);
        seenByView.addOnScrollListener(new ActionBarShadowOnScrollListener(this));

        postId = Preconditions.checkNotNull(getIntent().getStringExtra(EXTRA_POST_ID));

        viewModel = new ViewModelProvider(this, new PostSeenByViewModel.Factory(getApplication(), postId)).get(PostSeenByViewModel.class);
        viewModel.seenByList.getLiveData().observe(this, adapter::setSeenBy);

        mediaThumbnailLoader = new MediaThumbnailLoader(this, 2 * getResources().getDimensionPixelSize(R.dimen.details_media_list_height));
        avatarLoader = AvatarLoader.getInstance(this);

        viewModel.postDeleted.observe(this, deleted -> {
            if (Boolean.TRUE.equals(deleted)) {
                finish();
            }
        });

        viewModel.post.getLiveData().observe(this, this::updatePost);

        timestampRefresher = new ViewModelProvider(this).get(TimestampRefresher.class);
        timestampRefresher.refresh.observe(this, value -> adapter.notifyDataSetChanged());
    }

    public void updatePost(Post post) {
        this.post = post;
        adapter.setPrivacyFooterVisible(post != null && post.getParentGroup() == null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaThumbnailLoader.destroy();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.post_seen_by_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.retract) {
            onRetractPost();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onRetractPost() {
        if (post != null) {
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.retract_post_confirmation))
                    .setCancelable(true)
                    .setPositiveButton(R.string.yes, (dialog, which) ->
                            ContentDb.getInstance().retractPost(post))
                    .setNegativeButton(R.string.no, null)
                    .show();
        }
    }

    interface ListItem {

        int getType();
    }

    static class ContactListItem implements ListItem {
        final @NonNull Contact contact;
        final long timestamp;

        ContactListItem(@NonNull Contact contact, long timestamp) {
            this.contact = contact;
            this.timestamp = timestamp;
        }

        @Override
        public int getType() {
            return ContactsAdapter.VIEW_TYPE_CONTACT;
        }
    }

    static class HeaderListItem implements ListItem {
        final String title;

        HeaderListItem(String title) {
            this.title = title;
        }

        @Override
        public int getType() {
            return ContactsAdapter.VIEW_TYPE_HEADER;
        }
    }

    static class DividerListItem implements ListItem {

        @Override
        public int getType() {
            return ContactsAdapter.VIEW_TYPE_DIVIDER;
        }
    }

    static class ManagePrivacyListItem implements ListItem {

        @Override
        public int getType() {
            return ContactsAdapter.VIEW_TYPE_MANAGE_PRIVACY;
        }
    }

    static class InviteFriendsListItem implements ListItem {

        @Override
        public int getType() {
            return ContactsAdapter.VIEW_TYPE_INVITE_FRIENDS;
        }
    }

    static class ExpandListItem implements ListItem {

        @Override
        public int getType() {
            return ContactsAdapter.VIEW_TYPE_EXPAND;
        }
    }

    static class EmptyListItem implements ListItem {
        final boolean sentTo;

        EmptyListItem(boolean sentTo) {
            this.sentTo = sentTo;
        }

        @Override
        public int getType() {
            return ContactsAdapter.VIEW_TYPE_EMPTY;
        }
    }

    private class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

        private static final int MAX_CONTACTS_BEFORE_COLLAPSE = 12;
        private static final int INITIAL_CONTACTS_COLLAPSED = 10;

        static final int VIEW_TYPE_HEADER = 0;
        static final int VIEW_TYPE_CONTACT = 1;
        static final int VIEW_TYPE_EMPTY = 2;
        static final int VIEW_TYPE_DIVIDER = 3;
        static final int VIEW_TYPE_INVITE_FRIENDS = 4;
        static final int VIEW_TYPE_MANAGE_PRIVACY = 5;
        static final int VIEW_TYPE_EXPAND = 6;

        private List<PostSeenByViewModel.SeenByContact> seenByContacts;

        private final List<ListItem> listItems = new ArrayList<>();

        private boolean expanded = false;
        private boolean hasPrivacyFooter = false;

        void setSeenBy(List<PostSeenByViewModel.SeenByContact> seenByContacts) {
            this.seenByContacts = seenByContacts;
            createListItems();
            notifyDataSetChanged();
        }

        private void createListItems() {
            listItems.clear();
            final Set<UserId> seenByUserIds = new HashSet<>();
            boolean emptyViewedBy = seenByContacts == null || seenByContacts.isEmpty();
            if (!emptyViewedBy) {
                int count = 0;
                int limit = (seenByContacts.size() > MAX_CONTACTS_BEFORE_COLLAPSE) ? INITIAL_CONTACTS_COLLAPSED : seenByContacts.size();
                Iterator<PostSeenByViewModel.SeenByContact> iterator = seenByContacts.iterator();
                while (iterator.hasNext() && (expanded || count < limit)) {
                    PostSeenByViewModel.SeenByContact seenByContact = iterator.next();
                    listItems.add(new ContactListItem(seenByContact.contact, seenByContact.timestamp));
                    seenByUserIds.add(seenByContact.contact.userId);
                    count++;
                }
                if (!expanded && count < seenByContacts.size()) {
                    listItems.add(new ExpandListItem());
                }
            } else {
                listItems.add(new EmptyListItem(false));
            }
            listItems.add(new DividerListItem());
            if (hasPrivacyFooter) {
                listItems.add(new ManagePrivacyListItem());
                listItems.add(new InviteFriendsListItem());
            }
        }

        void setPrivacyFooterVisible(boolean footerVisible) {
            if (this.hasPrivacyFooter != footerVisible) {
                this.hasPrivacyFooter = footerVisible;
                createListItems();
                notifyDataSetChanged();
            }
        }

        @Override
        public int getItemViewType(int position) {
            return listItems.get(position).getType();
        }

        @Override
        public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case VIEW_TYPE_HEADER: {
                    return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.seen_by_header_item, parent, false));
                }
                case VIEW_TYPE_CONTACT: {
                    return new ContactViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.seen_by_contact_item, parent, false));
                }
                case VIEW_TYPE_EMPTY: {
                    return new EmptyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.seen_by_empty_item, parent, false));
                }
                case VIEW_TYPE_DIVIDER: {
                    return new DividerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.seen_by_divider_item, parent, false));
                }
                case VIEW_TYPE_MANAGE_PRIVACY: {
                    return new ManagePrivacyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.seen_by_manage_privacy, parent, false));
                }
                case VIEW_TYPE_INVITE_FRIENDS: {
                    return new InviteFriendsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.seen_by_invite_friends, parent, false));
                }
                case VIEW_TYPE_EXPAND: {
                    return new ExpandViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.seen_by_view_more, parent, false));
                }
            }
            throw new IllegalStateException("unknown item type");
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            //noinspection unchecked
            holder.bindTo(listItems.get(position));
        }

        @Override
        public int getItemCount() {
            return listItems.size();
        }

        private class ViewHolder<LI extends ListItem> extends RecyclerView.ViewHolder {

            ViewHolder(@NonNull View itemView) {
                super(itemView);
            }


            void bindTo(LI listItem) {
            }
        }

        private class ContactViewHolder extends ViewHolder<ContactListItem> {

            final ImageView avatarView;
            final TextView nameView;
            final TextView timeView;
            final TextView phoneView;
            final View menuView;

            Contact contact;

            ContactViewHolder(@NonNull View itemView) {
                super(itemView);
                avatarView = itemView.findViewById(R.id.avatar);
                nameView = itemView.findViewById(R.id.name);
                timeView = itemView.findViewById(R.id.time);
                menuView = itemView.findViewById(R.id.menu);
                phoneView = itemView.findViewById(R.id.phone);
                itemView.setOnClickListener(v -> {
                    ContactMenuBottomSheetDialogFragment bs = ContactMenuBottomSheetDialogFragment.newInstance(contact, postId);
                    DialogFragmentUtils.showDialogFragmentOnce(bs, getSupportFragmentManager());
                });
                menuView.setOnClickListener(v -> {
                    final PopupMenu menu = new PopupMenu(menuView.getContext(), menuView);
                    getMenuInflater().inflate(R.menu.contact_menu, menu.getMenu());
                    menu.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.block) {
                            SnackbarHelper.showInfo(itemView, R.string.block);
                            return true;
                        }
                        return false;
                    });
                    menu.show();
                });
                menuView.setVisibility(View.GONE);
            }

            @Override
            void bindTo(@NonNull ContactListItem item) {
                contact = item.contact;
                avatarLoader.load(avatarView, Preconditions.checkNotNull(item.contact.userId));
                nameView.setText(contact.getDisplayName());
                phoneView.setText(contact.getDisplayPhone());
                timeView.setText("");
                if (item.timestamp == -1) {
                    itemView.setAlpha(0.6f);
                } else {
                    itemView.setAlpha(1.0f);
                }
            }
        }

        private class HeaderViewHolder extends ViewHolder<HeaderListItem> {

            final TextView titleView;

            HeaderViewHolder(@NonNull View itemView) {
                super(itemView);
                titleView = itemView.findViewById(R.id.title);
            }

            @Override
            void bindTo(@NonNull HeaderListItem item) {
                titleView.setText(item.title);
            }
        }

        private class DividerViewHolder extends ViewHolder<DividerListItem> {

            DividerViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

        private class EmptyViewHolder extends ViewHolder<EmptyListItem> {

            private final TextView emptyText;

            EmptyViewHolder(@NonNull View itemView) {
                super(itemView);

                emptyText = itemView.findViewById(R.id.empty_text);
            }

            @Override
            void bindTo(@NonNull EmptyListItem item) {
                emptyText.setText(R.string.empty_viewed_by);
            }
        }

        private class ExpandViewHolder extends ViewHolder<ExpandListItem> {

            ExpandViewHolder(@NonNull View itemView) {
                super(itemView);

                itemView.setOnClickListener(v -> {
                    expanded = true;
                    createListItems();
                    adapter.notifyDataSetChanged();
                });
            }
        }

        private class ManagePrivacyViewHolder extends ViewHolder<ManagePrivacyListItem> {

            ManagePrivacyViewHolder(@NonNull View itemView) {
                super(itemView);
                itemView.setOnClickListener(v -> {
                    startActivity(SettingsPrivacy.openFeedPrivacy(v.getContext()));
                });
            }
        }

        private class InviteFriendsViewHolder extends ViewHolder<InviteFriendsListItem> {

            InviteFriendsViewHolder(@NonNull View itemView) {
                super(itemView);
                itemView.setOnClickListener(v -> {
                    final Intent intent = new Intent(itemView.getContext(), InviteContactsActivity.class);
                    startActivity(intent);
                });
            }
        }
    }
}
