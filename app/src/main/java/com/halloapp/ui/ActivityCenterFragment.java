package com.halloapp.ui;

import android.content.Intent;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.content.Comment;
import com.halloapp.content.PostThumbnailLoader;
import com.halloapp.id.UserId;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.contacts.FavoritesNuxBottomSheetDialogFragment;
import com.halloapp.ui.invites.InviteContactsActivity;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.util.DialogFragmentUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;
import com.halloapp.util.TimeFormatter;
import com.halloapp.widget.ActionBarShadowOnScrollListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ActivityCenterFragment extends HalloFragment implements MainNavFragment {

    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();

    private final SocialEventsAdapter adapter = new SocialEventsAdapter();
    private TextContentLoader textContentLoader;
    private PostThumbnailLoader postThumbnailLoader;
    private RecyclerView listView;
    private LinearLayoutManager layoutManager;
    private View emptyView;
    private MenuItem markAllReadMenuItem;

    private boolean hasUnseenItems = false;

    private ActivityCenterViewModel viewModel;

    private OnItemClickListener clickListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        textContentLoader = new TextContentLoader();
        postThumbnailLoader = new PostThumbnailLoader(requireContext(), getResources().getDimensionPixelSize(R.dimen.comment_history_thumbnail_size));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (textContentLoader != null) {
            textContentLoader.destroy();
            textContentLoader = null;
        }
        if (postThumbnailLoader != null) {
            postThumbnailLoader.destroy();
            postThumbnailLoader = null;
        }
    }

    public interface OnItemClickListener {
        void onItemClicked(@NonNull ActivityCenterViewModel.SocialActionEvent commentsGroup);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View root = inflater.inflate(R.layout.activity_activity_center, container, false);

        listView = root.findViewById(android.R.id.list);
        emptyView = root.findViewById(android.R.id.empty);

        layoutManager = new LinearLayoutManager(requireContext());
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter);

        viewModel = new ViewModelProvider(requireActivity()).get(ActivityCenterViewModel.class);

        viewModel.socialHistory.getLiveData().observe(getViewLifecycleOwner(), history -> {
            hasUnseenItems = history.unseenCount > 0;
            updateMenu();
            setSocialHistory(history);
        });

        clickListener = commentsGroup -> {
            final ActivityCenterViewModel.SocialHistory commentHistoryData = viewModel.socialHistory.getLiveData().getValue();
            if (commentHistoryData != null) {
                final Intent intent = FlatCommentsActivity.viewComments(requireContext(), commentsGroup.postId, commentsGroup.postSenderUserId);
                if (commentsGroup.involvedUsers.size() == 1 && commentsGroup.contentItem instanceof Comment) {
                    intent.putExtra(FlatCommentsActivity.EXTRA_NAVIGATE_TO_COMMENT_ID, commentsGroup.contentItem.id);
                }
                startActivity(intent);
            }
        };
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.addOnScrollListener(new ActionBarShadowOnScrollListener((AppCompatActivity) requireActivity()) {
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                final RecyclerView.LayoutManager layoutManager = Preconditions.checkNotNull(recyclerView.getLayoutManager());
                final View childView = layoutManager.getChildAt(0);
                if (childView != null && layoutManager.getPosition(childView) == 0) {
                    onScrollToTop();
                }
            }
        });
    }

    private void onScrollToTop() {
        viewModel.onScrollToTop();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.mark_all_read) {
            viewModel.markAllRead();
        }
        return false;
    }

    private void updateMenu() {
        if (markAllReadMenuItem != null) {
            markAllReadMenuItem.setEnabled(hasUnseenItems);
            Drawable drawable = markAllReadMenuItem.getIcon();
            if (drawable != null) {
                // If we don't mutate the drawable, then all drawable's with this id will have a color
                // filter applied to it.
                drawable.mutate();
                drawable.setColorFilter(ContextCompat.getColor(requireContext(), hasUnseenItems ? R.color.color_secondary : R.color.disabled_text), PorterDuff.Mode.SRC_IN);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.activity_center_menu, menu);
        markAllReadMenuItem = menu.findItem(R.id.mark_all_read);
        updateMenu();
        super.onCreateOptionsMenu(menu,inflater);
    }

    private class SocialEventsAdapter extends RecyclerView.Adapter<SocialEventsAdapter.ViewHolder> {

        private List<ActivityCenterViewModel.SocialActionEvent> socialEvents;
        private Map<UserId, Contact> contacts;

        void setEvents(@NonNull List<ActivityCenterViewModel.SocialActionEvent> socialEvents) {
            this.socialEvents = socialEvents;
            notifyDataSetChanged();
        }

        void setContacts(@NonNull Map<UserId, Contact> contacts) {
            this.contacts = contacts;
            notifyDataSetChanged();
        }

        void reset() {
            this.socialEvents = null;
            this.contacts = null;
            notifyDataSetChanged();
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
                postThumbnailLoader.cancel(thumbnailView);
                if (socialEvent.postId != null) {
                    postThumbnailLoader.load(thumbnailView, socialEvent.postSenderUserId, socialEvent.postId);
                }

                if (socialEvent.action == ActivityCenterViewModel.SocialActionEvent.Action.TYPE_WELCOME
                        || socialEvent.action == ActivityCenterViewModel.SocialActionEvent.Action.TYPE_FAVORITES_NUX) {
                    thumbnailView.setVisibility(View.GONE);
                } else {
                    thumbnailView.setVisibility(View.VISIBLE);
                }

                unseenIndicatorView.setVisibility(socialEvent.seen ? View.INVISIBLE : View.VISIBLE);
                itemView.setBackgroundColor(socialEvent.seen ? 0 : ContextCompat.getColor(itemView.getContext(), R.color.color_secondary_10_alpha));

                final List<String> names = new ArrayList<>();
                final Set<UserId> userIdSet = new HashSet<>();
                long timestamp = socialEvent.timestamp;
                Set<UserId> uniqueUsers = new LinkedHashSet<>(socialEvent.involvedUsers);
                for (UserId userId : uniqueUsers) {
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
                textContentLoader.cancel(infoView);
                if (socialEvent.action == ActivityCenterViewModel.SocialActionEvent.Action.TYPE_COMMENT) {
                    if (uniqueUsers.size() == 1) {
                        Comment comment = (Comment) socialEvent.contentItem;
                        @StringRes int commentString;
                        if (comment.parentCommentId == null) {
                            commentString = R.string.commented_on_with_preview;
                        } else {
                            commentString = R.string.replied_to_with_preview;
                        }
                        textContentLoader.load(infoView, comment, new TextContentLoader.TextDisplayer() {
                            @Override
                            public void showResult(TextView tv, CharSequence text) {
                                infoView.setText(Html.fromHtml(getResources().getString(commentString, names.get(0), text)));
                            }

                            @Override
                            public void showPreview(TextView tv, CharSequence text) {
                                infoView.setText(Html.fromHtml(getResources().getString(commentString, names.get(0), "")));
                            }
                        });
                    } else if (uniqueUsers.size() == 2){
                        if (socialEvent.postSenderUserId.isMe()) {
                            infoView.setText(Html.fromHtml(getResources().getString(R.string.two_unknown_contacts_commented_on_your_post, names.get(0), names.get(1))));
                        } else {
                            final Contact contact = Preconditions.checkNotNull(contacts.get(socialEvent.postSenderUserId));
                            infoView.setText(Html.fromHtml(getResources().getString(R.string.two_unknown_contacts_commented_on_someones_post, names.get(0), names.get(1), contact.getDisplayName())));
                        }
                    } else {
                        int commenters = uniqueUsers.size() - 1;
                        if (socialEvent.postSenderUserId.isMe()) {
                            infoView.setText(Html.fromHtml(getResources().getQuantityString(R.plurals.commented_on_your_post_grouped_with_others, commenters, names.get(0), commenters)));
                        } else {
                            final Contact contact = Preconditions.checkNotNull(contacts.get(socialEvent.postSenderUserId));
                            infoView.setText(Html.fromHtml(getResources().getQuantityString(R.plurals.commented_on_someones_post_grouped_with_others, commenters, names.get(0), commenters, contact.getDisplayName())));
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
                        Comment comment = (Comment) socialEvent.contentItem;
                        if (comment.senderUserId.equals(socialEvent.postSenderUserId)) {
                            infoView.setText(Html.fromHtml(infoView.getContext().getString(R.string.mentioned_you_in_comment_on_own_post, contact.getDisplayName())));
                        } else {
                            infoView.setText(Html.fromHtml(infoView.getContext().getString(R.string.mentioned_you_in_comment_on_someones_post, names.get(0), contact.getDisplayName())));
                        }
                    }
                } else if (socialEvent.action == ActivityCenterViewModel.SocialActionEvent.Action.TYPE_WELCOME) {
                    CharSequence text = Html.fromHtml(infoView.getContext().getResources().getString(R.string.welcome_notification));
                    text = StringUtils.replaceLink(infoView.getContext(), text, "invite-friend", ActivityCenterFragment.this::onInvitesNotificationClicked);
                    infoView.setText(text);
                    infoView.setMovementMethod(LinkMovementMethod.getInstance());
                } else if (socialEvent.action == ActivityCenterViewModel.SocialActionEvent.Action.TYPE_FAVORITES_NUX) {
                    CharSequence text = Html.fromHtml(infoView.getContext().getResources().getString(R.string.favorites_notification));
                    text = StringUtils.replaceLink(infoView.getContext(), text, "learn-more", ActivityCenterFragment.this::onFavoritesNotificationClicked);
                    infoView.setText(text);
                    infoView.setMovementMethod(LinkMovementMethod.getInstance());
                }

                if (socialEvent.action == ActivityCenterViewModel.SocialActionEvent.Action.TYPE_FAVORITES_NUX) {
                    avatarLoader.cancel(avatarView);
                    avatarView.setImageResource(R.drawable.favorites_icon_large);
                } else {
                    final UserId actorUserId = socialEvent.involvedUsers.isEmpty() ? socialEvent.postSenderUserId : socialEvent.involvedUsers.iterator().next();
                    avatarLoader.load(avatarView, actorUserId);
                }

                itemView.setOnClickListener(v -> {
                    if (socialEvent.action == ActivityCenterViewModel.SocialActionEvent.Action.TYPE_WELCOME) {
                        onInvitesNotificationClicked();
                        return;
                    } else if (socialEvent.action == ActivityCenterViewModel.SocialActionEvent.Action.TYPE_FAVORITES_NUX) {
                        onFavoritesNotificationClicked();
                        return;
                    }
                    if (clickListener != null) {
                        clickListener.onItemClicked(socialEvent);
                    }
                });
            }
        }
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
        startActivity(new Intent(requireContext(), InviteContactsActivity.class));
        viewModel.markInvitesNotificationSeen();
    }

    private void onFavoritesNotificationClicked() {
        DialogFragmentUtils.showDialogFragmentOnce(FavoritesNuxBottomSheetDialogFragment.newInstance(), getParentFragmentManager());
        viewModel.markFavoritesNotificationSeen();
    }

    @Override
    public void resetScrollPosition() {
        layoutManager.scrollToPosition(0);
    }
}
