package com.halloapp.ui.posts;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.content.Post;
import com.halloapp.ui.FlatCommentsActivity;
import com.halloapp.ui.chat.ChatActivity;
import com.halloapp.util.ViewDataLoader;

public class IncomingPostViewHolder extends PostViewHolder {

    private final View postActionsSeparator;
    private final View commentsIndicator;
    private final View message;

    public IncomingPostViewHolder(@NonNull View itemView, @NonNull PostViewHolderParent parent) {
        super(itemView, parent);

        postActionsSeparator = itemView.findViewById(R.id.post_actions_separator);
        commentsIndicator = itemView.findViewById(R.id.comments_indicator);
        message = itemView.findViewById(R.id.message);

        itemView.findViewById(R.id.comment).setOnClickListener(view -> {
            final Intent intent = FlatCommentsActivity.viewComments(itemView.getContext(), post.id, post.senderUserId);
            intent.putExtra(FlatCommentsActivity.EXTRA_SHOW_KEYBOARD, post.commentCount == 0);
            parent.startActivity(intent);
        });
        message.setOnClickListener(view -> {
            final Intent intent = ChatActivity.open(itemView.getContext(), post.senderUserId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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

        if (TextUtils.isEmpty(post.text) || post.urlPreview != null) {
            postActionsSeparator.setVisibility(View.GONE);
            footerSpacing.setVisibility(View.GONE);
        } else {
            postActionsSeparator.setVisibility(View.VISIBLE);
            footerSpacing.setVisibility(View.VISIBLE);
        }

        parent.getContactLoader().load(message.findViewById(R.id.message_text), post.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
            @Override
            public void showResult(@NonNull TextView view, @Nullable Contact result) {
                message.setVisibility(result != null && result.addressBookName != null ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void showLoading(@NonNull TextView view) {
                message.setVisibility(View.INVISIBLE);
            }
        });
    }
}

