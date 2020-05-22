package com.halloapp.ui;

import android.graphics.Outline;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.UserId;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.CenterToast;
import com.halloapp.widget.LinearSpacingItemDecoration;
import com.halloapp.xmpp.Connection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PostSeenByActivity extends AppCompatActivity {

    public static final String EXTRA_POST_ID = "post_id";

    private final ContactsAdapter adapter = new ContactsAdapter();
    private PostSeenByViewModel viewModel;
    private MediaThumbnailLoader mediaThumbnailLoader;
    private AvatarLoader avatarLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("PostSeenByActivity.onCreate");
        setContentView(R.layout.activity_post_seen_by);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final RecyclerView seenByView = findViewById(R.id.seen_by_list);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        seenByView.setLayoutManager(layoutManager);
        seenByView.setAdapter(adapter);

        final String postId = Preconditions.checkNotNull(getIntent().getStringExtra(EXTRA_POST_ID));

        viewModel = new ViewModelProvider(this, new PostSeenByViewModel.Factory(getApplication(), postId)).get(PostSeenByViewModel.class);
        viewModel.seenByList.getLiveData().observe(this, adapter::setSeenBy);
        viewModel.friendsList.getLiveData().observe(this, adapter::setFriends);

        mediaThumbnailLoader = new MediaThumbnailLoader(this, 2 * getResources().getDimensionPixelSize(R.dimen.details_media_list_height));
        avatarLoader = AvatarLoader.getInstance(Connection.getInstance(), this);

        viewModel.post.getLiveData().observe(this, this::showPost);

        viewModel.postDeleted.observe(this, deleted -> {
            if (Boolean.TRUE.equals(deleted)) {
                finish();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("PostSeenByActivity.onDestroy");
        mediaThumbnailLoader.destroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.post_seen_by_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case R.id.retract: {
                onRetractPost();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void onRetractPost() {
        final Post post = viewModel.post.getLiveData().getValue();
        if (post != null) {
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.retract_post_confirmation))
                    .setCancelable(true)
                    .setPositiveButton(R.string.yes, (dialog, which) ->
                            ContentDb.getInstance(this).retractPost(post))
                    .setNegativeButton(R.string.no, null)
                    .show();
        }
    }

    private void showPost(@NonNull Post post) {
        final RecyclerView mediaGallery = findViewById(R.id.media);
        if (post.media.isEmpty()) {
            mediaGallery.setVisibility(View.GONE);
        } else {
            final LinearLayoutManager layoutManager = new LinearLayoutManager(mediaGallery.getContext(), RecyclerView.HORIZONTAL, false);
            mediaGallery.setLayoutManager(layoutManager);
            mediaGallery.addItemDecoration(new LinearSpacingItemDecoration(layoutManager, getResources().getDimensionPixelSize(R.dimen.details_media_list_spacing)));
            mediaGallery.setAdapter(new MediaAdapter(post.media));
        }

        final TextView textView = findViewById(R.id.text);
        if (TextUtils.isEmpty(post.text)) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textView.getContext().getResources().getDimension(
                    (post.text.length() < 180 && post.media.isEmpty()) ? R.dimen.post_text_size_large : R.dimen.post_text_size));
            textView.setText(post.text);
        }
    }

    private class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {

        final List<Media> media;

        MediaAdapter(@NonNull List<Media> media) {
            this.media = media;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), getResources().getDimension(R.dimen.details_media_list_corner_radius));
                }
            });
            imageView.setClipToOutline(true);
            return new ViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final ImageView imageView = (ImageView)holder.itemView;
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setAdjustViewBounds(true);
            mediaThumbnailLoader.load(imageView, media.get(position));
        }

        @Override
        public int getItemCount() {
            return media.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {

            ViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }

    interface ListItem {

        int getType();
    }

    static class ContactListItem implements ListItem {
        final @NonNull Contact contact;

        ContactListItem(@NonNull Contact contact) {
            this.contact = contact;
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

    static class EmptyListItem implements ListItem {
        final String text;

        EmptyListItem(String text) {
            this.text = text;
        }

        @Override
        public int getType() {
            return ContactsAdapter.VIEW_TYPE_EMPTY;
        }
    }

    private class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

        static final int VIEW_TYPE_HEADER = 0;
        static final int VIEW_TYPE_CONTACT = 1;
        static final int VIEW_TYPE_EMPTY = 2;

        private List<Contact> seenBy;
        private List<Contact> friends;

        private final List<ListItem> listItems = new ArrayList<>();

        void setSeenBy(List<Contact> seenBy) {
            this.seenBy = seenBy;
            createListItems();
            notifyDataSetChanged();
        }

        void setFriends(List<Contact> friends) {
            this.friends = friends;
            notifyDataSetChanged();
            createListItems();
        }

        private void createListItems() {
            listItems.clear();
            listItems.add(new HeaderListItem(getString(R.string.seen_by)));
            final Set<UserId> seenByUserIds = new HashSet<>();
            if (seenBy == null || seenBy.isEmpty()) {
                listItems.add(new EmptyListItem(getString(R.string.no_one_seen_your_post)));
            } else {
                for (Contact contact : seenBy) {
                    listItems.add(new ContactListItem(contact));
                    seenByUserIds.add(contact.userId);
                }
            }
            if (friends != null && !friends.isEmpty()) {
                boolean headerAdded = false;
                for (Contact contact : friends) {
                    if (!seenByUserIds.contains(contact.userId)) {
                        if (!headerAdded) {
                            listItems.add(new HeaderListItem(getString(R.string.other_friends)));
                            headerAdded = true;
                        }
                        listItems.add(new ContactListItem(contact));
                    }
                }
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
            final View menuView;

            Contact contact;

            ContactViewHolder(@NonNull View itemView) {
                super(itemView);
                avatarView = itemView.findViewById(R.id.avatar);
                nameView = itemView.findViewById(R.id.name);
                menuView = itemView.findViewById(R.id.menu);
                menuView.setOnClickListener(v -> {
                    final PopupMenu menu = new PopupMenu(menuView.getContext(), menuView);
                    getMenuInflater().inflate(R.menu.contact_menu, menu.getMenu());
                    menu.setOnMenuItemClickListener(item -> {
                        //noinspection SwitchStatementWithTooFewBranches
                        switch (item.getItemId()) {
                            case R.id.block: {
                                CenterToast.show(getBaseContext(), R.string.block); // TODO (ds): add contact blocking
                                return true;
                            }
                            default: {
                                return false;
                            }
                        }
                    });
                    menu.show();
                });
                menuView.setVisibility(View.INVISIBLE); // TODO (ds): uncomment when blocking is implemented
            }

            @Override
            void bindTo(@NonNull ContactListItem item) {
                contact = item.contact;
                avatarLoader.load(avatarView, Preconditions.checkNotNull(item.contact.userId));
                nameView.setText(contact.getDisplayName());
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

        private class EmptyViewHolder extends ViewHolder<EmptyListItem> {

            EmptyViewHolder(@NonNull View itemView) {
                super(itemView);
            }

            @Override
            void bindTo(@NonNull EmptyListItem item) {
                ((TextView)itemView).setText(item.text);
            }
        }
    }
}
