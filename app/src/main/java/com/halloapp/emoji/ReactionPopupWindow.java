package com.halloapp.emoji;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.halloapp.R;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.content.Reaction;
import com.halloapp.id.UserId;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.RandomId;
import com.halloapp.widget.ReactionBubbleLinearLayout;

import java.util.List;

public class ReactionPopupWindow extends PopupWindow {

    private static final String EMOJI_THUMB = "\uD83D\uDC4D";
    private static final String EMOJI_HEART = "❤";
    private static final String EMOJI_CLAP = "\uD83D\uDC4F";
    private static final String EMOJI_FOLDED_HANDS = "\uD83D\uDE4F";
    private static final String EMOJI_CRY = "\uD83D\uDE25";
    private static final String EMOJI_SHOCKED = "\uD83D\uDE2E";
    private static final String EMOJI_LAUGH = "\uD83D\uDE02";
    private static final String EMOJI_FIRE = "\uD83D\uDD25";
    private static final String EMOJI_ENRAGED = "\uD83D\uDE21";

    private static final String[] POST_EMOJI = {
            EMOJI_HEART,
            EMOJI_CLAP,
            EMOJI_FIRE,
            EMOJI_ENRAGED,
            EMOJI_CRY,
            EMOJI_SHOCKED,
            EMOJI_LAUGH
    };

    private static final String[] MESSAGE_AND_COMMENT_EMOJI = {
            EMOJI_THUMB,
            EMOJI_HEART,
            EMOJI_CLAP,
            EMOJI_FOLDED_HANDS,
            EMOJI_CRY,
            EMOJI_SHOCKED,
            EMOJI_LAUGH
    };

    private final BgWorkers bgWorkers = BgWorkers.getInstance();

    private final ContentItem contentItem;

    public ReactionPopupWindow(@NonNull Context context, @NonNull ContentItem contentItem, @NonNull Runnable callback) {
        super(context);

        this.contentItem = contentItem;

        final View root;
        Boolean outbound;
        String[] emojiOptions;
        if (contentItem instanceof Message) {
            Message message = (Message) contentItem;
            outbound = message.isMeMessageSender();
            emojiOptions = MESSAGE_AND_COMMENT_EMOJI;
        } else if (contentItem instanceof Comment) {
            Comment comment = (Comment) contentItem;
            outbound = comment.isOutgoing();
            emojiOptions = MESSAGE_AND_COMMENT_EMOJI;
        } else if (contentItem instanceof Post) {
            Post post = (Post) contentItem;
            outbound = post.isOutgoing();
            emojiOptions = POST_EMOJI;
        } else {
            outbound = null;
            emojiOptions = null;
        }

        if (outbound) {
            root = LayoutInflater.from(context).inflate(R.layout.message_item_outgoing_reaction_bubble, null, false);
        } else {
            root = LayoutInflater.from(context).inflate(R.layout.message_item_incoming_reaction_bubble, null, false);
        }

        setContentView(root);
        final ReactionBubbleLinearLayout reactionBubbleLinearLayout = root.findViewById(R.id.reaction_bubble);

        for (String reaction : emojiOptions) {
            View reactionView = LayoutInflater.from(context).inflate(R.layout.reaction_bubble_item, reactionBubbleLinearLayout, false);
            reactionView.findViewById(R.id.emoji_shade).setTag(reaction);
            TextView textView = reactionView.findViewById(R.id.emoji_text);
            textView.setText(reaction);
            textView.setOnClickListener(v -> handleSelection(reaction, callback));
            reactionBubbleLinearLayout.addView(reactionView);
        }

        setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setOutsideTouchable(false);
        setFocusable(false);

        BgWorkers.getInstance().execute(() -> {
            for (Reaction reaction : ContentDb.getInstance().getReactions(contentItem.id)) {
                if (reaction.senderUserId.isMe()) {
                    root.post(() -> root.findViewWithTag(reaction.reactionType).setVisibility(View.VISIBLE));
                    break;
                }
            }
        });
    }

    public void show(@NonNull View anchor) {
        View contentView = getContentView();
        contentView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED));
        showAsDropDown(anchor, (contentView.getPaddingRight() + contentView.getPaddingLeft() + anchor.getWidth() - contentView.getMeasuredWidth()) / 2 , -contentView.getMeasuredHeight() - anchor.getHeight() - 2);
    }

    private void handleSelection(String reactionType, Runnable callback) {
        ContentDb contentDb = ContentDb.getInstance();
        bgWorkers.execute(() -> {
            Reaction newReaction = new Reaction(RandomId.create(), contentItem.id, UserId.ME, reactionType, System.currentTimeMillis());
            List<Reaction> reactionsList = contentDb.getReactions(contentItem.id);
            if (reactionsList == null || reactionsList.isEmpty()) {
                contentDb.addReaction(newReaction, contentItem, null, null);
            } else {
                boolean isRetract = false;
                for (Reaction oldReaction : reactionsList) {
                    if (oldReaction.getSenderUserId().equals(newReaction.getSenderUserId())) {
                        isRetract = oldReaction.getReactionType().equals(reactionType);
                        break;
                    }
                }
                if (isRetract) {
                    contentDb.retractReaction(newReaction, contentItem);
                } else {
                    contentDb.addReaction(newReaction, contentItem, null, null);
                }
            }
        });
        callback.run();
    }
}
