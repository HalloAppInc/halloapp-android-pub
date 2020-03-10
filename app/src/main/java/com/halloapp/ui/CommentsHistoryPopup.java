package com.halloapp.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.drawable.BitmapDrawable;
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
import androidx.core.util.Preconditions;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.UserId;
import com.halloapp.posts.Comment;
import com.halloapp.posts.PostThumbnailLoader;
import com.halloapp.ui.home.HomeViewModel;
import com.halloapp.util.ListFormatter;
import com.halloapp.util.TimeFormatter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

public class CommentsHistoryPopup {

    private final Context context;
    private final PopupWindow popupWindow;
    private final View anchorView;
    private final CommentsAdapter adapter = new CommentsAdapter();
    private final PostThumbnailLoader postThumbnailLoader;
    private final RecyclerView listView;
    private final View emptyView;
    private final View titleView;

    private OnItemClickListener clickListener;
    private int maxHeight;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ContactsDb.Observer contactsObserver = new ContactsDb.Observer() {

        @Override
        public void onContactsChanged() {
            mainHandler.post(() -> handleContactsChanged());
        }

        @Override
        public void onContactsReset() {
        }
    };

    public interface OnItemClickListener {
        void onItemClicked(@NonNull HomeViewModel.CommentsGroup commentsGroup);
    }

    public CommentsHistoryPopup(Context context, PostThumbnailLoader postThumbnailLoader, View anchorView) {

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

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter);

        popupWindow.setContentView(contentView);

        ContactsDb.getInstance(context).addObserver(contactsObserver);
    }

    public void destroy() {
        ContactsDb.getInstance(context).removeObserver(contactsObserver);
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setCommentHistory(@Nullable HomeViewModel.CommentsHistory commentsHistory) {
        if (commentsHistory == null || commentsHistory.commentGroups.size() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            titleView.setVisibility(View.GONE);
            listView.setVisibility(View.GONE);
            adapter.reset();
        } else {
            emptyView.setVisibility(View.GONE);
            titleView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.VISIBLE);
            adapter.setComments(commentsHistory.commentGroups);
            adapter.setContacts(commentsHistory.contacts);
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
            new RefreshContactsTask(ContactsDb.getInstance(context), adapter).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void handleContactsChanged() {
        if (isShowing()) {
            new RefreshContactsTask(ContactsDb.getInstance(context), adapter).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            adapter.invalidateContacts();
        }
    }

    static class RefreshContactsTask extends AsyncTask<Void, Void, Map<UserId, Contact>> {

        final ContactsDb contactsDb;
        final Set<UserId> userIds;
        final WeakReference<CommentsAdapter> adapterRef;

        RefreshContactsTask(ContactsDb contactsDb, CommentsAdapter adapter) {
            this.contactsDb = contactsDb;
            this.userIds = new HashSet<>(adapter.contacts.keySet());
            this.adapterRef = new WeakReference<>(adapter);
        }

        @Override
        protected Map<UserId, Contact> doInBackground(Void... voids) {
            final Map<UserId, Contact> contacts = new HashMap<>();
            for (UserId userId : userIds) {
                final Contact contact = contactsDb.getContact(userId);
                if (contact != null) {
                    contacts.put(userId, contact);
                } else {
                    contacts.put(userId, new Contact(userId));
                }
            }
            return contacts;
        }

        @Override
        protected void onPostExecute(final Map<UserId, Contact> contacts) {
            final CommentsAdapter adapter = adapterRef.get();
            if (adapter != null) {
                adapter.setContacts(contacts);
            }
        }
    }

    private class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

        private List<HomeViewModel.CommentsGroup> commentGroups;
        private Map<UserId, Contact> contacts;
        private boolean contactsInvalidated;

        void setComments(@NonNull List<HomeViewModel.CommentsGroup> commentGroups) {
            this.commentGroups = commentGroups;
            notifyDataSetChanged();
        }

        void setContacts(@NonNull Map<UserId, Contact> contacts) {
            this.contacts = contacts;
            this.contactsInvalidated = false;
            notifyDataSetChanged();
        }

        void reset() {
            this.commentGroups = null;
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
            holder.bind(commentGroups.get(position), contacts);
        }

        @Override
        public int getItemCount() {
            return commentGroups == null ? 0 : commentGroups.size();
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

            void bind(HomeViewModel.CommentsGroup commentsGroup, Map<UserId, Contact> contacts) {

                postThumbnailLoader.load(thumbnailView, commentsGroup.postSenderUserId, commentsGroup.postId);

                unseenIndicatorView.setVisibility(commentsGroup.comments.get(0).seen ? View.GONE : View.VISIBLE);

                final List<String> names = new ArrayList<>();
                final Set<UserId> userIdSet = new HashSet<>();
                long timestamp = 0;
                for (Comment comment : commentsGroup.comments) {
                    if (userIdSet.contains(comment.commentSenderUserId)) {
                        continue;
                    }
                    userIdSet.add(comment.commentSenderUserId);
                    final Contact contact = contacts.get(comment.commentSenderUserId);
                    if (contact != null) {
                        names.add(contact.getDisplayName());
                    }
                    if (comment.timestamp > timestamp) {
                        timestamp = comment.timestamp;
                    }
                }
                timeView.setText(TimeFormatter.formatTimeDiff(timeView.getContext(), System.currentTimeMillis() - timestamp));

                if (commentsGroup.postSenderUserId.isMe()) {
                    infoView.setText(Html.fromHtml(ListFormatter.format(infoView.getContext(),
                            R.string.commented_on_your_post_1,
                            R.string.commented_on_your_post_2,
                            R.string.commented_on_your_post_3,
                            R.plurals.commented_on_your_post_4, names)));
                } else {
                    final Contact contact = Preconditions.checkNotNull(contacts.get(commentsGroup.postSenderUserId));
                    if (userIdSet.size() == 1 && userIdSet.iterator().next().equals(commentsGroup.postSenderUserId)) {
                        infoView.setText(Html.fromHtml(infoView.getContext().getResources().getString(R.string.commented_on_own_post, contact.getDisplayName())));
                    } else {
                        infoView.setText(Html.fromHtml(ListFormatter.format(infoView.getContext(),
                                R.string.commented_on_someones_post_1,
                                R.string.commented_on_someones_post_2,
                                R.string.commented_on_someones_post_3,
                                R.plurals.commented_on_someones_post_4, names, contact.getDisplayName())));
                    }
                }

                itemView.setOnClickListener(v -> {
                    if (clickListener != null) {
                        clickListener.onItemClicked(commentsGroup);
                    }
                });
            }
        }
    }
}
