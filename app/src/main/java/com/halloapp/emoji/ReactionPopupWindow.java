package com.halloapp.emoji;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.core.util.Consumer;

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

import java.util.List;
import java.util.concurrent.Callable;

public class ReactionPopupWindow extends PopupWindow {

    private static final String EMOJI_THUMB = "\uD83D\uDC4D";
    private static final String EMOJI_HEART = "❤";
    private static final String EMOJI_CLAP = "\uD83D\uDC4F";
    private static final String EMOJI_FOLDED_HANDS = "\uD83D\uDE4F";
    private static final String EMOJI_CRY = "\uD83D\uDE25";
    private static final String EMOJI_SHOCKED = "\uD83D\uDE2E";
    private static final String EMOJI_LAUGH = "\uD83D\uDE02";

    private final BgWorkers bgWorkers = BgWorkers.getInstance();

    private final ContentItem contentItem;

    public ReactionPopupWindow(Context context, ContentItem contentItem) {
        super(context);

        this.contentItem = contentItem;

        final View root;
        Boolean outbound;
        if (contentItem instanceof Message) {
            Message message = (Message) contentItem;
            outbound = message.isMeMessageSender();
        } else if (contentItem instanceof Comment) {
            Comment comment = (Comment) contentItem;
            outbound = comment.isOutgoing();
        } else if (contentItem instanceof Post) {
            Post post = (Post) contentItem;
            outbound = post.isOutgoing();
        } else {
            outbound = null;
        }

        if (outbound) {
            root = LayoutInflater.from(context).inflate(R.layout.message_item_outgoing_reaction_bubble, null, false);
        } else {
            root = LayoutInflater.from(context).inflate(R.layout.message_item_incoming_reaction_bubble, null, false);
        }

        setContentView(root);
        setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setOutsideTouchable(false);
        setFocusable(false);

        BgWorkers.getInstance().execute(() -> {
            for (Reaction reaction : ContentDb.getInstance().getReactions(contentItem.id)) {
                if (reaction.senderUserId.isMe()) {
                    root.post(() -> {
                        if (EMOJI_THUMB.equals(reaction.reactionType)) {
                            root.findViewById(R.id.thumbs_up_emoji_shade).setVisibility(View.VISIBLE);
                        } else if (EMOJI_HEART.equals(reaction.reactionType)) {
                            root.findViewById(R.id.heart_emoji_shade).setVisibility(View.VISIBLE);
                        } else if (EMOJI_CLAP.equals(reaction.reactionType)) {
                            root.findViewById(R.id.clap_emoji_shade).setVisibility(View.VISIBLE);
                        } else if (EMOJI_FOLDED_HANDS.equals(reaction.reactionType)) {
                            root.findViewById(R.id.folded_hands_emoji_shade).setVisibility(View.VISIBLE);
                        } else if (EMOJI_CRY.equals(reaction.reactionType)) {
                            root.findViewById(R.id.cry_emoji_shade).setVisibility(View.VISIBLE);
                        } else if (EMOJI_SHOCKED.equals(reaction.reactionType)) {
                            root.findViewById(R.id.shocked_emoji_shade).setVisibility(View.VISIBLE);
                        } else if (EMOJI_LAUGH.equals(reaction.reactionType)) {
                            root.findViewById(R.id.laugh_emoji_shade).setVisibility(View.VISIBLE);
                        }
                    });
                    break;
                }
            }
        });
    }

    public void show(@NonNull View anchor, @NonNull Runnable callback) {
        View contentView = getContentView();
        contentView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED));
        showAsDropDown(anchor, (contentView.getPaddingRight() + contentView.getPaddingLeft() + anchor.getWidth() - contentView.getMeasuredWidth()) / 2 , -contentView.getMeasuredHeight() - anchor.getHeight() - 2);

        getContentView().findViewById(R.id.thumbs_up_emoji).setOnClickListener(v -> handleSelection(EMOJI_THUMB, callback));
        getContentView().findViewById(R.id.heart_emoji).setOnClickListener(v -> handleSelection(EMOJI_HEART, callback));
        getContentView().findViewById(R.id.clap_emoji).setOnClickListener(v -> handleSelection(EMOJI_CLAP, callback));
        getContentView().findViewById(R.id.folded_hands_emoji).setOnClickListener(v -> handleSelection(EMOJI_FOLDED_HANDS, callback));
        getContentView().findViewById(R.id.cry_emoji).setOnClickListener(v -> handleSelection(EMOJI_CRY, callback));
        getContentView().findViewById(R.id.shocked_emoji).setOnClickListener(v -> handleSelection(EMOJI_SHOCKED, callback));
        getContentView().findViewById(R.id.laugh_emoji).setOnClickListener(v -> handleSelection(EMOJI_LAUGH, callback));
        getContentView().findViewById(R.id.more_options).setOnClickListener(null); // TODO(jack): Allow selecting any emoji
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
