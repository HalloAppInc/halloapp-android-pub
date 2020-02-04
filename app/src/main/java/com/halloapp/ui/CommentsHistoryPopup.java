package com.halloapp.ui;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.halloapp.contacts.UserId;
import com.halloapp.posts.Comment;
import com.halloapp.posts.PostThumbnailLoader;
import com.halloapp.ui.home.HomeViewModel;
import com.halloapp.util.ListFormatter;
import com.halloapp.util.Log;
import com.halloapp.util.TimeUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommentsHistoryPopup {

    private final PopupWindow popupWindow;
    private final View anchorView;
    private final CommentsAdapter adapter = new CommentsAdapter();
    private final PostThumbnailLoader postThumbnailLoader;
    private final RecyclerView listView;
    private final View emptyView;

    private OnItemClickListener clickListener;
    private int maxHeight;

    public interface OnItemClickListener {
        void onItemClicked(@NonNull HomeViewModel.CommentsGroup commentsGroup);
    }

    public CommentsHistoryPopup(Context context, PostThumbnailLoader postThumbnailLoader, View anchorView) {

        this.anchorView = anchorView;
        this.postThumbnailLoader = postThumbnailLoader;

        popupWindow = new PopupWindow(context);
        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        popupWindow.setFocusable(true);
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(null);
        popupWindow.setAnimationStyle(R.style.comments_history_animation);

        final FrameLayout contentView = new FrameLayout(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST));
            }
        };

        LayoutInflater.from(context).inflate(R.layout.comments_history, contentView, true);

        listView = contentView.findViewById(android.R.id.list);
        emptyView = contentView.findViewById(android.R.id.empty);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter);

        popupWindow.setContentView(contentView);
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setCommentHistory(@Nullable HomeViewModel.CommentsHistory commentsHistory) {
        adapter.setCommentsHistory(commentsHistory);
        if (commentsHistory == null || commentsHistory.commentGroups.size() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
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
    }

    private class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

        private HomeViewModel.CommentsHistory commentsHistory;

        void setCommentsHistory(@Nullable HomeViewModel.CommentsHistory commentHistoryData) {
            this.commentsHistory = commentHistoryData;
            notifyDataSetChanged();
        }

        @Override
        public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_history_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(commentsHistory.commentGroups.get(position), commentsHistory.contacts);
        }

        @Override
        public int getItemCount() {
            return commentsHistory == null ? 0 : commentsHistory.commentGroups.size();
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
                timeView.setText(TimeUtils.formatTimeDiff(timeView.getContext(), System.currentTimeMillis() - timestamp));

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
