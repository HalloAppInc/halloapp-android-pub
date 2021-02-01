package com.halloapp.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.content.PostThumbnailLoader;
import com.halloapp.ui.home.HomeViewModel;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ListFormatter;
import com.halloapp.util.Preconditions;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.logs.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SocialHistoryPopup {

    private final Context context;
    private final PopupWindow popupWindow;
    private final View anchorView;
    private final SocialEventsAdapter adapter = new SocialEventsAdapter();
    private final PostThumbnailLoader postThumbnailLoader;
    private final RecyclerView listView;
    private final View emptyView;
    private final View titleView;
    private final View markAllReadView;
    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private final ContentDb contentDb = ContentDb.getInstance();

    private OnItemClickListener clickListener;
    private int maxHeight;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
        @Override
        public void onContactsChanged() {
            mainHandler.post(() -> handleContactsChanged());
        }
    };

    public interface OnItemClickListener {
        void onItemClicked(@NonNull HomeViewModel.SocialActionEvent commentsGroup);
    }

    public SocialHistoryPopup(Context context, PostThumbnailLoader postThumbnailLoader, View anchorView) {

        this.context = context;
        this.anchorView = anchorView;
        this.postThumbnailLoader = postThumbnailLoader;

        popupWindow = new PopupWindow(context);
        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        popupWindow.setFocusable(true);
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null)); // to workaround popup doesn't dismiss on api 21 devices
        popupWindow.setAnimationStyle(R.style.CommentsHistoryAnimation);

        final FrameLayout contentView = new FrameLayout(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST));
            }
        };

        LayoutInflater.from(context).inflate(R.layout.comments_history, contentView, true);

        listView = contentView.findViewById(android.R.id.list);
        emptyView = contentView.findViewById(android.R.id.empty);
        titleView = contentView.findViewById(android.R.id.title);
        markAllReadView = contentView.findViewById(R.id.mark_all_read);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter);

        markAllReadView.setOnClickListener(v -> {
            bgWorkers.execute(() -> {
                final HashSet<Comment> comments = new HashSet<>(contentDb.getIncomingCommentsHistory(-1));
                final List<Post> mentionedPosts = contentDb.getMentionedPosts(UserId.ME, -1);
                final List<Comment> mentionedComments = contentDb.getMentionedComments(UserId.ME, -1);

                comments.addAll(mentionedComments);

                for (Comment comment : comments) {
                    contentDb.setCommentSeen(comment.postId, comment.commentId, true);
                }

                for (Post post : mentionedPosts) {
                    contentDb.setIncomingPostSeen(post.senderUserId, post.id);
                }
            });
            dismiss();
        });

        popupWindow.setContentView(contentView);

        ContactsDb.getInstance().addObserver(contactsObserver);
    }

    public void destroy() {
        ContactsDb.getInstance().removeObserver(contactsObserver);
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setSocialHistory(@Nullable HomeViewModel.SocialHistory socialHistory) {
        if (socialHistory == null || socialHistory.socialActionEvent.size() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            titleView.setVisibility(View.GONE);
            listView.setVisibility(View.GONE);
            markAllReadView.setVisibility(View.GONE);
            adapter.reset();
        } else {
            emptyView.setVisibility(View.GONE);
            titleView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.VISIBLE);
            markAllReadView.setVisibility(View.VISIBLE);
            adapter.setEvents(socialHistory.socialActionEvent);
            adapter.setContacts(socialHistory.contacts);
        }
    }

    public void dismiss() {
        popupWindow.dismiss();
    }

    public boolean isShowing() {
        return popupWindow.isShowing();
    }

    public void show(int maxHeight) {
        this.maxHeight = maxHeight;

        popupWindow.showAsDropDown(anchorView);
        listView.scrollToPosition(0);

        final View rootView = popupWindow.getContentView().getRootView();
        final Context context = popupWindow.getContentView().getContext();
        final WindowManager wm = Preconditions.checkNotNull((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
        final WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) rootView.getLayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.3f;
        wm.updateViewLayout(rootView, layoutParams);

        if (adapter.contactsInvalidated) {
            new RefreshContactsTask(ContactsDb.getInstance(), adapter).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void handleContactsChanged() {
        if (isShowing()) {
            new RefreshContactsTask(ContactsDb.getInstance(), adapter).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            adapter.invalidateContacts();
        }
    }

    static class RefreshContactsTask extends AsyncTask<Void, Void, Map<UserId, Contact>> {

        final ContactsDb contactsDb;
        final Set<UserId> userIds;
        final WeakReference<SocialEventsAdapter> adapterRef;

        RefreshContactsTask(ContactsDb contactsDb, SocialEventsAdapter adapter) {
            this.contactsDb = contactsDb;
            this.userIds = new HashSet<>();
            if (adapter.contacts != null) {
                this.userIds.addAll(adapter.contacts.keySet());
            }
            this.adapterRef = new WeakReference<>(adapter);
        }

        @Override
        protected Map<UserId, Contact> doInBackground(Void... voids) {
            final Map<UserId, Contact> contacts = new HashMap<>();
            for (UserId userId : userIds) {
                contacts.put(userId, contactsDb.getContact(userId));
            }
            return contacts;
        }

        @Override
        protected void onPostExecute(final Map<UserId, Contact> contacts) {
            final SocialEventsAdapter adapter = adapterRef.get();
            if (adapter != null) {
                adapter.setContacts(contacts);
            }
        }
    }

    private class SocialEventsAdapter extends RecyclerView.Adapter<SocialEventsAdapter.ViewHolder> {

        private List<HomeViewModel.SocialActionEvent> socialEvents;
        private Map<UserId, Contact> contacts;
        private boolean contactsInvalidated;

        void setEvents(@NonNull List<HomeViewModel.SocialActionEvent> socialEvents) {
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
        public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_history_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                infoView = itemView.findViewById(R.id.info);
                timeView = itemView.findViewById(R.id.time);
                thumbnailView = itemView.findViewById(R.id.thumbnail);
                unseenIndicatorView = itemView.findViewById(R.id.unseen_indicator);
                thumbnailView.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), itemView.getContext().getResources().getDimension(R.dimen.comment_media_list_corner_radius));
                    }
                });
                thumbnailView.setClipToOutline(true);
            }

            void bind(HomeViewModel.SocialActionEvent socialEvent, Map<UserId, Contact> contacts) {

                postThumbnailLoader.load(thumbnailView, socialEvent.postSenderUserId, socialEvent.postId);

                unseenIndicatorView.setVisibility(socialEvent.seen ? View.GONE : View.VISIBLE);

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

                if (socialEvent.action == HomeViewModel.SocialActionEvent.Action.TYPE_COMMENT) {
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
                } else if (socialEvent.action == HomeViewModel.SocialActionEvent.Action.TYPE_MENTION_IN_POST) {
                    final Contact contact = Preconditions.checkNotNull(contacts.get(socialEvent.postSenderUserId));
                    infoView.setText(Html.fromHtml(infoView.getContext().getString(R.string.mentioned_you_in_their_post, contact.getDisplayName())));
                } else if (socialEvent.action == HomeViewModel.SocialActionEvent.Action.TYPE_MENTION_IN_COMMENT) {
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
                }

                itemView.setOnClickListener(v -> {
                    if (clickListener != null) {
                        clickListener.onItemClicked(socialEvent);
                    }
                });
            }
        }
    }
}
