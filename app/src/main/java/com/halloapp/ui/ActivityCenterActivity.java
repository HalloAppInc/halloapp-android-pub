package com.halloapp.ui;

import android.content.Intent;
import android.graphics.Outline;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.content.PostThumbnailLoader;
import com.halloapp.id.UserId;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.contacts.ContactHashInfoBottomSheetDialogFragment;
import com.halloapp.ui.home.HomeViewModel;
import com.halloapp.ui.invites.InviteContactsActivity;
import com.halloapp.util.DialogFragmentUtils;
import com.halloapp.util.ListFormatter;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;
import com.halloapp.util.TimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ActivityCenterActivity extends HalloActivity {

    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();

    private SocialEventsAdapter adapter = new SocialEventsAdapter();
    private PostThumbnailLoader postThumbnailLoader;
    private RecyclerView listView;
    private View emptyView;

    private ActivityCenterViewModel viewModel;

    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClicked(@NonNull ActivityCenterViewModel.SocialActionEvent commentsGroup);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.activity);

        setContentView(R.layout.activity_activity_center);


        listView = findViewById(android.R.id.list);
        emptyView = findViewById(android.R.id.empty);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ActivityCenterViewModel.class);

        viewModel.socialHistory.getLiveData().observe(this, this::setSocialHistory);
        viewModel.contacts.getLiveData().observe(this, c -> {
            if (c != null) {
                adapter.setContacts(c);
            }
        });

        postThumbnailLoader = new PostThumbnailLoader(this, getResources().getDimensionPixelSize(R.dimen.comment_history_thumbnail_size));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(getResources().getDimension(R.dimen.action_bar_elevation));
        }

        clickListener = commentsGroup -> {
            final ActivityCenterViewModel.SocialHistory commentHistoryData = viewModel.socialHistory.getLiveData().getValue();
            if (commentHistoryData != null) {
                final Intent intent = new Intent(this, CommentsActivity.class);
                intent.putExtra(CommentsActivity.EXTRA_POST_SENDER_USER_ID, commentsGroup.postSenderUserId.rawId());
                intent.putExtra(CommentsActivity.EXTRA_POST_ID, commentsGroup.postId);
                intent.putExtra(CommentsActivity.EXTRA_SHOW_KEYBOARD, false);
                startActivity(intent);
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_center_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.mark_all_read);
        SpannableString ss = new SpannableString(getString(R.string.mark_all_read));
        ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getBaseContext(), R.color.color_secondary)), 0, ss.length(), 0);
        menuItem.setTitle(ss);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.mark_all_read) {
            viewModel.markAllRead();
        }
        return false;
    }

    public void setSocialHistory(@Nullable ActivityCenterViewModel.SocialHistory socialHistory) {
        if (socialHistory == null || socialHistory.socialActionEvent.size() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            adapter.reset();
        } else {
            emptyView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            adapter.setEvents(socialHistory.socialActionEvent);
            adapter.setContacts(socialHistory.contacts);
        }
    }

    private void onInvitesNotificationClicked() {
        startActivity(new Intent(this, InviteContactsActivity.class));
        viewModel.markInvitesNotificationSeen();
    }

    private class SocialEventsAdapter extends RecyclerView.Adapter<SocialEventsAdapter.ViewHolder> {

        private List<ActivityCenterViewModel.SocialActionEvent> socialEvents;
        private Map<UserId, Contact> contacts;
        private boolean contactsInvalidated;

        void setEvents(@NonNull List<ActivityCenterViewModel.SocialActionEvent> socialEvents) {
            this.socialEvents = socialEvents;
            notifyDataSetChanged();
        }

        void setContacts(@NonNull Map<UserId, Contact> contacts) {
            this.contacts = contacts;
            this.contactsInvalidated = false;
            notifyDataSetChanged();
        }

        void reset() {
            this.socialEvents = null;
            this.contacts = null;
            this.contactsInvalidated = false;
            notifyDataSetChanged();
        }

        void invalidateContacts() {
            this.contactsInvalidated = true;
        }

        @Override
        public @NonNull SocialEventsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new SocialEventsAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_history_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull SocialEventsAdapter.ViewHolder holder, int position) {
            holder.bind(socialEvents.get(position), contacts);
        }

        @Override
        public int getItemCount() {
            return socialEvents == null ? 0 : socialEvents.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {

            final TextView infoView;
            final TextView timeView;
            final ImageView thumbnailView;
            final View unseenIndicatorView;
            final ImageView avatarView;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                infoView = itemView.findViewById(R.id.info);
                timeView = itemView.findViewById(R.id.time);
                thumbnailView = itemView.findViewById(R.id.thumbnail);
                unseenIndicatorView = itemView.findViewById(R.id.unseen_indicator);
                avatarView = itemView.findViewById(R.id.avatar);
                thumbnailView.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), itemView.getContext().getResources().getDimension(R.dimen.comment_media_list_corner_radius));
                    }
                });
                thumbnailView.setClipToOutline(true);
            }

            void bind(ActivityCenterViewModel.SocialActionEvent socialEvent, Map<UserId, Contact> contacts) {

                if (socialEvent.postId != null) {
                    postThumbnailLoader.load(thumbnailView, socialEvent.postSenderUserId, socialEvent.postId);
                }

                unseenIndicatorView.setVisibility(socialEvent.seen ? View.INVISIBLE : View.VISIBLE);
                itemView.setBackgroundColor(socialEvent.seen ? 0 : ContextCompat.getColor(itemView.getContext(), R.color.color_secondary_10_alpha));

                final List<String> names = new ArrayList<>();
                final Set<UserId> userIdSet = new HashSet<>();
                long timestamp = socialEvent.timestamp;
                for (UserId userId : socialEvent.involvedUsers) {
                    if (userIdSet.contains(userId)) {
                        continue;
                    }
                    userIdSet.add(userId);
                    final Contact contact = contacts.get(userId);
                    if (contact != null) {
                        names.add(contact.getDisplayName());
                    }
                }
                TimeFormatter.setTimePostsFormat(timeView, timestamp);

                if (socialEvent.action == ActivityCenterViewModel.SocialActionEvent.Action.TYPE_COMMENT) {
                    if (socialEvent.postSenderUserId.isMe()) {
                        infoView.setText(Html.fromHtml(ListFormatter.format(infoView.getContext(),
                                R.string.commented_on_your_post_1,
                                R.string.commented_on_your_post_2,
                                R.string.commented_on_your_post_3,
                                R.plurals.commented_on_your_post_4, names)));
                    } else {
                        final Contact contact = Preconditions.checkNotNull(contacts.get(socialEvent.postSenderUserId));
                        if (userIdSet.size() == 1 && userIdSet.iterator().next().equals(socialEvent.postSenderUserId)) {
                            infoView.setText(Html.fromHtml(infoView.getContext().getResources().getString(R.string.commented_on_own_post, contact.getDisplayName())));
                        } else {
                            infoView.setText(Html.fromHtml(ListFormatter.format(infoView.getContext(),
                                    R.string.commented_on_someones_post_1,
                                    R.string.commented_on_someones_post_2,
                                    R.string.commented_on_someones_post_3,
                                    R.plurals.commented_on_someones_post_4, names, contact.getDisplayName())));
                        }
                    }
                } else if (socialEvent.action == ActivityCenterViewModel.SocialActionEvent.Action.TYPE_MENTION_IN_POST) {
                    final Contact contact = Preconditions.checkNotNull(contacts.get(socialEvent.postSenderUserId));
                    infoView.setText(Html.fromHtml(infoView.getContext().getString(R.string.mentioned_you_in_their_post, contact.getDisplayName())));
                } else if (socialEvent.action == ActivityCenterViewModel.SocialActionEvent.Action.TYPE_MENTION_IN_COMMENT) {
                    if (socialEvent.postSenderUserId.isMe()) {
                        infoView.setText(Html.fromHtml(infoView.getContext().getString(R.string.mentioned_you_in_comment_on_your_post, names.get(0))));
                    } else {
                        final Contact contact = Preconditions.checkNotNull(contacts.get(socialEvent.postSenderUserId));
                        if (socialEvent.involvedUsers.get(0).equals(socialEvent.postSenderUserId)) {
                            infoView.setText(Html.fromHtml(infoView.getContext().getString(R.string.mentioned_you_in_comment_on_own_post, contact.getDisplayName())));
                        } else {
                            infoView.setText(Html.fromHtml(infoView.getContext().getString(R.string.mentioned_you_in_comment_on_someones_post, names.get(0), contact.getDisplayName())));
                        }
                    }
                } else if (socialEvent.action == ActivityCenterViewModel.SocialActionEvent.Action.TYPE_WELCOME) {
                    CharSequence text = Html.fromHtml(infoView.getContext().getResources().getString(R.string.welcome_notification));
                    text = StringUtils.replaceLink(infoView.getContext(), text, "invite-friend", ActivityCenterActivity.this::onInvitesNotificationClicked);
                    infoView.setText(text);
                    infoView.setMovementMethod(LinkMovementMethod.getInstance());
                }

                UserId actorUserId = socialEvent.involvedUsers.size() > 0 ? socialEvent.involvedUsers.get(0) : socialEvent.postSenderUserId;
                avatarLoader.load(avatarView, actorUserId);

                itemView.setOnClickListener(v -> {
                    if (socialEvent.action == ActivityCenterViewModel.SocialActionEvent.Action.TYPE_WELCOME) {
                        onInvitesNotificationClicked();
                        return;
                    }
                    if (clickListener != null) {
                        clickListener.onItemClicked(socialEvent);
                    }
                });
            }
        }
    }
}
