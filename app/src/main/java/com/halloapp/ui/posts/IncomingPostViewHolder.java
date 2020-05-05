package com.halloapp.ui.posts;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.halloapp.R;
import com.halloapp.content.Post;
import com.halloapp.ui.CommentsActivity;
import com.halloapp.ui.PostContentActivity;
import com.halloapp.ui.chat.ChatActivity;

public class IncomingPostViewHolder extends PostViewHolder {

    private final View commentsIndicator;

    public IncomingPostViewHolder(@NonNull View itemView, @NonNull PostViewHolderParent parent) {
        super(itemView, parent);

        commentsIndicator = itemView.findViewById(R.id.comments_indicator);

        itemView.findViewById(R.id.comment).setOnClickListener(view -> {
            final Intent intent = new Intent(itemView.getContext(), CommentsActivity.class);
            intent.putExtra(CommentsActivity.EXTRA_POST_SENDER_USER_ID, post.senderUserId.rawId());
            intent.putExtra(CommentsActivity.EXTRA_POST_ID, post.id);
            intent.putExtra(CommentsActivity.EXTRA_SHOW_KEYBOARD, post.commentCount == 0);
            parent.startActivity(intent);
        });
        itemView.findViewById(R.id.message).setOnClickListener(view -> {
            final Intent intent = new Intent(itemView.getContext(), ChatActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(ChatActivity.EXTRA_CHAT_ID, post.senderUserId.rawId());
            intent.putExtra(ChatActivity.EXTRA_REPLY_POST_ID, post.id);
            final Integer selPos = parent.getMediaPagerPositionMap().get(post.rowId);
            intent.putExtra(ChatActivity.EXTRA_REPLY_POST_MEDIA_INDEX, selPos == null ? 0 : selPos);
            parent.startActivity(intent);
        });
    }

    @CallSuper
    public void bindTo(@NonNull Post post) {
        super.bindTo(post);

        if (post.unseenCommentCount > 0) {
            commentsIndicator.setVisibility(View.VISIBLE);
            commentsIndicator.setBackgroundResource(R.drawable.new_comments_indicator);
        } else if (post.commentCount > 0) {
            commentsIndicator.setVisibility(View.VISIBLE);
            commentsIndicator.setBackgroundResource(R.drawable.old_comments_indicator);
        } else {
            commentsIndicator.setVisibility(View.GONE);
        }

        footerSpacing.setVisibility(TextUtils.isEmpty(post.text) ? View.GONE : View.VISIBLE);
    }
}

