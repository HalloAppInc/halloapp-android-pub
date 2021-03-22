package com.halloapp.ui.posts;

import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.halloapp.R;
import com.halloapp.content.Post;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.CommentsActivity;
import com.halloapp.ui.ReactionsPopupWindow;
import com.halloapp.ui.chat.ChatActivity;

public class IncomingPostViewHolder extends PostViewHolder {

    private final View commentsIndicator;
    private final View message;

    public IncomingPostViewHolder(@NonNull View itemView, @NonNull PostViewHolderParent parent) {
        super(itemView, parent);

        commentsIndicator = itemView.findViewById(R.id.comments_indicator);
        message = itemView.findViewById(R.id.message);

        itemView.findViewById(R.id.comment).setOnClickListener(view -> {
            final Intent intent = new Intent(itemView.getContext(), CommentsActivity.class);
            intent.putExtra(CommentsActivity.EXTRA_POST_SENDER_USER_ID, post.senderUserId.rawId());
            intent.putExtra(CommentsActivity.EXTRA_POST_ID, post.id);
            intent.putExtra(CommentsActivity.EXTRA_SHOW_KEYBOARD, post.commentCount == 0);
            parent.startActivity(intent);
        });
        message.setOnClickListener(view -> {
            final Intent intent = new Intent(itemView.getContext(), ChatActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(ChatActivity.EXTRA_CHAT_ID, post.senderUserId);
            intent.putExtra(ChatActivity.EXTRA_REPLY_POST_ID, post.id);
            intent.putExtra(ChatActivity.EXTRA_REPLY_POST_SENDER_ID, post.senderUserId);
            final Integer selPos = parent.getMediaPagerPositionMap().get(post.rowId);
            intent.putExtra(ChatActivity.EXTRA_REPLY_POST_MEDIA_INDEX, selPos == null ? 0 : selPos);
            parent.startActivity(intent);
        });
    }

    private void resetEmoji() {
        if (message instanceof TextView) {
            ((TextView)message).getPaint().setColorFilter(null);
            message.setAlpha(1.0f);
            message.invalidate();
        }
    }

    @CallSuper
    public void bindTo(@NonNull Post post) {
        super.bindTo(post);
        resetEmoji();
        if (post.unseenCommentCount > 0) {
            commentsIndicator.setVisibility(View.VISIBLE);
            commentsIndicator.setBackgroundResource(R.drawable.new_comments_indicator);
        } else if (post.commentCount > 0) {
            commentsIndicator.setVisibility(View.VISIBLE);
            commentsIndicator.setBackgroundResource(R.drawable.old_comments_indicator);
        } else {
            commentsIndicator.setVisibility(View.INVISIBLE);
        }

        if (TextUtils.isEmpty(post.text)) {
            footerSpacing.setVisibility(View.GONE);
        } else {
            footerSpacing.setVisibility(View.VISIBLE);
        }
    }
}

