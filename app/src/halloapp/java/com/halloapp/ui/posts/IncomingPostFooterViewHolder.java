package com.halloapp.ui.posts;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.content.Post;
import com.halloapp.emoji.ReactionPopupWindow;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.FlatCommentsActivity;
import com.halloapp.ui.ReactionListBottomSheetDialogFragment;
import com.halloapp.ui.chat.chat.ChatActivity;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.widget.ReactionsLayout;

public class IncomingPostFooterViewHolder extends PostFooterViewHolder {

    private final View postActionsSeparator;
    private final View commentsIndicator;
    private final View comment;
    private final View message;
    private final View messageAndReactions;
    private final View replyPrivately;
    private final View react;
    protected ReactionsLayout reactionsView;

    private ReactionPopupWindow reactionPopupWindow;

    private final boolean postReactionsEnabled = ServerProps.getInstance().getPostReactionsEnabled();

    public IncomingPostFooterViewHolder(@NonNull View itemView, @NonNull PostViewHolder.PostViewHolderParent parent) {
        super(itemView, parent);
        postActionsSeparator = itemView.findViewById(R.id.post_actions_separator);
        commentsIndicator = itemView.findViewById(R.id.comments_indicator);
        comment = itemView.findViewById(R.id.comment);
        message = itemView.findViewById(R.id.message);
        messageAndReactions = itemView.findViewById(R.id.message_and_reactions);
        replyPrivately = itemView.findViewById(R.id.reply_privately);
        react = itemView.findViewById(R.id.react);
        reactionsView = itemView.findViewById(R.id.reactions);

        messageAndReactions.setVisibility(postReactionsEnabled ? View.VISIBLE : View.GONE);
        message.setVisibility(postReactionsEnabled ? View.GONE : View.VISIBLE);

        comment.setOnClickListener(view -> {
            final Intent intent = FlatCommentsActivity.viewComments(itemView.getContext(), post.id, post.senderUserId);
            intent.putExtra(FlatCommentsActivity.EXTRA_SHOW_KEYBOARD, post.commentCount == 0);
            parent.startActivity(intent);
        });
        View.OnClickListener replyPrivatelyListener = v -> {
            final Intent intent = ChatActivity.open(itemView.getContext(), post.senderUserId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(ChatActivity.EXTRA_REPLY_POST_ID, post.id);
            intent.putExtra(ChatActivity.EXTRA_REPLY_POST_SENDER_ID, post.senderUserId);
            final Integer selPos = parent.getMediaPagerPositionMap().get(post.rowId);
            intent.putExtra(ChatActivity.EXTRA_REPLY_POST_MEDIA_INDEX, selPos == null ? 0 : selPos);
            parent.startActivity(intent);
        };
        message.setOnClickListener(replyPrivatelyListener);
        replyPrivately.setOnClickListener(replyPrivatelyListener);
        react.setOnClickListener(v -> {
            reactionPopupWindow = new ReactionPopupWindow(itemView.getContext(), post, () -> reactionPopupWindow.dismiss());
            reactionPopupWindow.show(v);
        });
        reactionsView.setOnClickListener(v -> parent.showDialogFragment(ReactionListBottomSheetDialogFragment.newInstance(post.id)));
    }

    @Override
    public void setCanInteract(boolean canInteract) {
        comment.setAlpha(canInteract ? 1f : DISABLED_OPACITY);
        comment.setEnabled(canInteract);
        message.setAlpha(canInteract ? 1f : DISABLED_OPACITY);
        message.setEnabled(canInteract);
    }

    @Override
    public void setPercentTransferred(int percent) {
        // TODO(jack)
    }

    @Override
    public void bindTo(@NonNull Post post) {
        super.bindTo(post);
        if (post.unseenCommentCount > 0) {
            commentsIndicator.setVisibility(View.VISIBLE);
            commentsIndicator.setBackgroundResource(R.drawable.new_comments_indicator);
        } else if (post.commentCount > 0) {
            commentsIndicator.setVisibility(View.VISIBLE);
            commentsIndicator.setBackgroundResource(R.drawable.old_comments_indicator);
        } else {
            commentsIndicator.setVisibility(View.INVISIBLE);
        }

        if (post.type == Post.TYPE_MOMENT
                || post.type == Post.TYPE_MOMENT_PSA
                || post.type == Post.TYPE_RETRACTED_MOMENT) {
            comment.setVisibility(View.INVISIBLE);
        } else {
            comment.setVisibility(View.VISIBLE);
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
                (postReactionsEnabled ? replyPrivately : message).setVisibility(result != null && result.addressBookName != null ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void showLoading(@NonNull TextView view) {
                (postReactionsEnabled ? replyPrivately : message).setVisibility(View.INVISIBLE);
            }
        });

        parent.getReactionLoader().load(reactionsView, post.id);
    }

    @Override
    public void reloadReactions() {
        mainHandler.post(() -> parent.getReactionLoader().load(reactionsView, post.id));
    }
}
