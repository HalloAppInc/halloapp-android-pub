package com.halloapp.ui.posts;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.halloapp.R;
import com.halloapp.contacts.UserId;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.ui.CommentsActivity;
import com.halloapp.ui.PostSeenByActivity;
import com.halloapp.util.TimeFormatter;
import com.halloapp.widget.AvatarsLayout;

public class OutgoingPostViewHolder extends PostViewHolder {

    private static final int MAX_SEEN_BY_AVATARS = 3;

    private final View addCommentButton;
    private final View viewCommentsButton;
    private final View viewCommentsIndicator;
    private final AvatarsLayout seenIndicator;
    private final View seenButton;
    private final View firstCommentContent;
    private final ImageView firstCommentAvatar;
    private final TextView firstCommentName;
    private final TextView firstCommentText;
    private final TextView firstCommentTimestamp;
    private final ImageView myAvatarView;

    public OutgoingPostViewHolder(@NonNull View itemView, @NonNull PostViewHolderParent parent) {
        super(itemView, parent);

        addCommentButton = itemView.findViewById(R.id.add_comment);
        viewCommentsButton = itemView.findViewById(R.id.view_comments);
        viewCommentsIndicator = itemView.findViewById(R.id.comments_indicator);
        seenIndicator = itemView.findViewById(R.id.seen_indicator);
        seenButton = itemView.findViewById(R.id.seen_button);
        firstCommentContent = itemView.findViewById(R.id.comment_content);
        firstCommentAvatar = itemView.findViewById(R.id.comment_avatar);
        firstCommentName = itemView.findViewById(R.id.comment_name);
        firstCommentText = itemView.findViewById(R.id.comment_text);
        firstCommentTimestamp = itemView.findViewById(R.id.comment_time);
        myAvatarView = itemView.findViewById(R.id.my_avatar);

        seenIndicator.setAvatarLoader(parent.getAvatarLoader());

        final View.OnClickListener commentsClickListener = v -> {
            final Intent intent = new Intent(itemView.getContext(), CommentsActivity.class);
            intent.putExtra(CommentsActivity.EXTRA_POST_SENDER_USER_ID, post.senderUserId.rawId());
            intent.putExtra(CommentsActivity.EXTRA_POST_ID, post.id);
            intent.putExtra(CommentsActivity.EXTRA_SHOW_KEYBOARD, post.commentCount == 0);
            parent.startActivity(intent);
        };
        addCommentButton.setOnClickListener(commentsClickListener);
        viewCommentsButton.setOnClickListener(commentsClickListener);

        final View.OnClickListener seenClickListener = v -> {
            final Intent intent = new Intent(itemView.getContext(), PostSeenByActivity.class);
            intent.putExtra(PostSeenByActivity.EXTRA_POST_ID, post.id);
            parent.startActivity(intent);
        };
        seenIndicator.setOnClickListener(seenClickListener);
        seenButton.setOnClickListener(seenClickListener);

        itemView.findViewById(R.id.comment_reply).setOnClickListener(v -> {
            final Intent intent = new Intent(itemView.getContext(), CommentsActivity.class);
            intent.putExtra(CommentsActivity.EXTRA_POST_SENDER_USER_ID, post.senderUserId.rawId());
            intent.putExtra(CommentsActivity.EXTRA_POST_ID, post.id);
            intent.putExtra(CommentsActivity.EXTRA_REPLY_USER_ID, post.firstComment.commentSenderUserId.rawId());
            intent.putExtra(CommentsActivity.EXTRA_REPLY_COMMENT_ID, post.firstComment.commentId);
            intent.putExtra(CommentsActivity.EXTRA_SHOW_KEYBOARD, true);
            parent.startActivity(intent);
        });
    }

    @CallSuper
    public void bindTo(@NonNull Post post) {
        super.bindTo(post);

        parent.getAvatarLoader().load(myAvatarView, UserId.ME);

        if (post.commentCount == 0) {
            addCommentButton.setVisibility(View.VISIBLE);
            viewCommentsButton.setVisibility(View.GONE);
        } else {
            addCommentButton.setVisibility(View.GONE);
            viewCommentsButton.setVisibility(View.VISIBLE);
        }

        if (post.seenByCount > 0) {
            seenIndicator.setVisibility(View.VISIBLE);
            seenButton.setVisibility(View.GONE);
            seenIndicator.setContentDescription(itemView.getContext().getResources().getQuantityString(R.plurals.seen_by, post.seenByCount, post.seenByCount));
            seenIndicator.setAvatarCount(Math.min(post.seenByCount, MAX_SEEN_BY_AVATARS));
            if (post.seenByCount > MAX_SEEN_BY_AVATARS) {
                final ImageView imageView0 = (ImageView)seenIndicator.getChildAt(0);
                imageView0.setImageTintMode(PorterDuff.Mode.OVERLAY);
                imageView0.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(imageView0.getContext(), R.color.dim_facepile_avatar_0)));
                final ImageView imageView1 = (ImageView)seenIndicator.getChildAt(1);
                imageView1.setImageTintMode(PorterDuff.Mode.OVERLAY);
                imageView1.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(imageView0.getContext(), R.color.dim_facepile_avatar_0)));
            } else {
                if (seenIndicator.getChildCount() > 0) {
                    final ImageView imageView = (ImageView)seenIndicator.getChildAt(0);
                    imageView.setImageTintList(null);
                }
                if (seenIndicator.getChildCount() > 1) {
                    final ImageView imageView = (ImageView)seenIndicator.getChildAt(1);
                    imageView.setImageTintList(null);
                }
            }
            parent.getSeenByLoader().load(seenIndicator, post.id);
        } else {
            seenIndicator.setVisibility(View.GONE);
            seenButton.setVisibility(View.VISIBLE);
        }
        footerSpacing.setVisibility(View.GONE);

        viewCommentsIndicator.setVisibility(post.unseenCommentCount > 0 ? View.VISIBLE : View.GONE);

        final Comment firstComment = post.firstComment;
        if (firstComment != null) {
            firstCommentContent.setVisibility(View.VISIBLE);

            parent.getAvatarLoader().load(firstCommentAvatar, firstComment.commentSenderUserId);

            firstCommentText.setText(firstComment.text);

            if (firstComment.commentSenderUserId.isMe()) {
                firstCommentName.setText(firstCommentName.getContext().getString(R.string.me));
            } else {
                parent.getContactLoader().load(firstCommentName, firstComment.commentSenderUserId);
            }

            TimeFormatter.setTimeDiffText(firstCommentTimestamp, System.currentTimeMillis() - firstComment.timestamp);
            parent.getTimestampRefresher().scheduleTimestampRefresh(firstComment.timestamp);
        } else {
            firstCommentContent.setVisibility(View.GONE);
        }
    }

    private void onRetractPost() {
        final Context context = itemView.getContext();
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.retract_post_confirmation));
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.yes, (dialog, which) -> ContentDb.getInstance(context).retractPost(post));
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }
}

