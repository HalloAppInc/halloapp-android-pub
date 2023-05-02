package com.halloapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.invites.InviteContactsActivity;
import com.halloapp.ui.settings.SettingsPrivacy;
import com.halloapp.util.DialogFragmentUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.TimeFormatter;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.privacy.PrivacyList;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_seen_by);

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
        avatarLoader = AvatarLoader.getInstance();

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
        if (post != null && (post.type == Post.TYPE_MOMENT || post.type == Post.TYPE_MOMENT_PSA)) {
            setTitle(R.string.moment_seen_by);
        } else {
            setTitle(R.string.seen_by);
        }

        adapter.setPost(post);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaThumbnailLoader.destroy();
    }

    interface ListItem {
        int getType();
    }

    static class ContactListItem implements ListItem {
        final @NonNull Contact contact;
        final long timestamp;
        final boolean screenshotted;
        final String reaction;

        ContactListItem(@NonNull Contact contact, long timestamp, boolean screenshotted, String reaction) {
            this.contact = contact;
            this.timestamp = timestamp;
            this.screenshotted = screenshotted;
            this.reaction = reaction;
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

        public final static int DISCLAIMER_CONTACTS = 0;
        public final static int DISCLAIMER_FAVORITES = 1;
        public final static int DISCLAIMER_MOMENTS = 2;

        private final int disclaimerType;
        private final long expirationTime;

        public DividerListItem(int disclaimerType, long expirationTime) {
            this.disclaimerType = disclaimerType;
            this.expirationTime = expirationTime;
        }

        public int getDisclaimerType() {
            return disclaimerType;
        }

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
        final boolean isMoment;

        EmptyListItem(boolean isMoment) {
            this.isMoment = isMoment;
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

        private Post post;

        void setSeenBy(List<PostSeenByViewModel.SeenByContact> seenByContacts) {
            this.seenByContacts = seenByContacts;
            createListItems();
            notifyDataSetChanged();
        }

        void setPost(@Nullable Post post) {
            this.post = post;
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
                    listItems.add(new ContactListItem(seenByContact.contact, seenByContact.timestamp, seenByContact.screenshotted, seenByContact.reaction));
                    seenByUserIds.add(seenByContact.contact.userId);
                    count++;
                }
                if (!expanded && count < seenByContacts.size()) {
                    listItems.add(new ExpandListItem());
                }
            } else {
                listItems.add(new EmptyListItem(post != null && (post.type == Post.TYPE_MOMENT || post.type == Post.TYPE_MOMENT_PSA)));
            }
            int disclaimerType = DividerListItem.DISCLAIMER_CONTACTS;
            if (post != null) {
                if (post.type == Post.TYPE_MOMENT || post.type == Post.TYPE_MOMENT_PSA) {
                    disclaimerType = DividerListItem.DISCLAIMER_MOMENTS;
                } else if (PrivacyList.Type.ONLY.equals(post.getAudienceType())) {
                    disclaimerType = DividerListItem.DISCLAIMER_FAVORITES;
                }
            }
            listItems.add(new DividerListItem(disclaimerType, post == null ? -1 : post.expirationTime));
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
            final View screenshottedView;
            final TextView reactionView;

            Contact contact;

            ContactViewHolder(@NonNull View itemView) {
                super(itemView);
                avatarView = itemView.findViewById(R.id.avatar);
                nameView = itemView.findViewById(R.id.name);
                timeView = itemView.findViewById(R.id.time);
                menuView = itemView.findViewById(R.id.menu);
                phoneView = itemView.findViewById(R.id.phone);
                screenshottedView = itemView.findViewById(R.id.screenshotted);
                reactionView = itemView.findViewById(R.id.reaction);

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
                timeView.setVisibility(View.INVISIBLE);
                if (item.timestamp == -1) {
                    itemView.setAlpha(0.6f);
                } else {
                    itemView.setAlpha(1.0f);
                }
                screenshottedView.setVisibility(item.screenshotted ? View.VISIBLE : View.GONE);
                if (TextUtils.isEmpty(item.reaction)) {
                    reactionView.setVisibility(View.GONE);
                } else {
                    reactionView.setVisibility(View.VISIBLE);
                    reactionView.setText(item.reaction);
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

            private final TextView disclaimerView;

            DividerViewHolder(@NonNull View itemView) {
                super(itemView);
                disclaimerView = itemView.findViewById(R.id.seen_by_disclaimer);
            }

            @Override
            void bindTo(DividerListItem listItem) {
                CharSequence expirationDuration = listItem.expirationTime <= 0 ? "" : TimeFormatter.formatExpirationDuration(disclaimerView.getContext(), (int) ((listItem.expirationTime - System.currentTimeMillis()) / 1000));
                switch (listItem.disclaimerType) {
                    case DividerListItem.DISCLAIMER_CONTACTS:
                        if (listItem.expirationTime == -1) {
                            disclaimerView.setText(R.string.seen_by_post_expiration_my_contacts);
                        } else if (listItem.expirationTime == Post.POST_EXPIRATION_NEVER) {
                            disclaimerView.setText(R.string.seen_by_post_expiration_permanent);
                        } else {
                            disclaimerView.setText(disclaimerView.getResources().getString(R.string.seen_by_post_expiration_my_contacts_with_duration, expirationDuration));
                        }
                        break;
                    case DividerListItem.DISCLAIMER_FAVORITES:
                        if (listItem.expirationTime <= 0) {
                            disclaimerView.setText(R.string.seen_by_post_expiration_favorites);
                        } else {
                            disclaimerView.setText(disclaimerView.getResources().getString(R.string.seen_by_post_expiration_favorites_with_duration, expirationDuration));
                        }
                        break;
                    case DividerListItem.DISCLAIMER_MOMENTS:
                        disclaimerView.setText(R.string.seen_by_post_expiration_moments);
                        break;
                }
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
                emptyText.setText(item.isMoment ? R.string.empty_moment_viewed_by : R.string.empty_viewed_by);
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
