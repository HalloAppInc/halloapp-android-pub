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
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Message;
import com.halloapp.content.Reaction;
import com.halloapp.util.BgWorkers;

import java.util.concurrent.Callable;

public class ReactionPopupWindow extends PopupWindow {

    private static final String EMOJI_CRY = "\uD83D\uDE25";
    private static final String EMOJI_ANGRY = "\uD83D\uDE20";
    private static final String EMOJI_SHOCKED = "\uD83D\uDE2E";
    private static final String EMOJI_LAUGH = "\uD83D\uDE02";
    private static final String EMOJI_THUMB = "\uD83D\uDC4D";
    private static final String EMOJI_HEART = "❤";

    public ReactionPopupWindow(Context context, ContentItem contentItem) {
        super(context);

        final View root;
        if (contentItem instanceof Message) {
            Message message = (Message) contentItem;
            if (message.isMeMessageSender()) {
                root = LayoutInflater.from(context).inflate(R.layout.message_item_outgoing_reaction_bubble, null, false);
            } else {
                root = LayoutInflater.from(context).inflate(R.layout.message_item_incoming_reaction_bubble, null, false);
            }
        } else {
            root = null;
        }

        setContentView(root);
        setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setOutsideTouchable(false);
        setFocusable(false);

        BgWorkers.getInstance().execute(() -> {
            for (Reaction reaction : ContentDb.getInstance().getReactions(contentItem.id)) {
                if (reaction.senderUserId.isMe()) {
                    root.post(() -> {
                        if (EMOJI_CRY.equals(reaction.reactionType)) {
                            root.findViewById(R.id.cry_emoji_shade).setVisibility(View.VISIBLE);
                        } else if (EMOJI_ANGRY.equals(reaction.reactionType)) {
                            root.findViewById(R.id.angry_emoji_shade).setVisibility(View.VISIBLE);
                        } else if (EMOJI_SHOCKED.equals(reaction.reactionType)) {
                            root.findViewById(R.id.shocked_emoji_shade).setVisibility(View.VISIBLE);
                        } else if (EMOJI_LAUGH.equals(reaction.reactionType)) {
                            root.findViewById(R.id.laugh_emoji_shade).setVisibility(View.VISIBLE);
                        } else if (EMOJI_THUMB.equals(reaction.reactionType)) {
                            root.findViewById(R.id.thumbs_up_emoji_shade).setVisibility(View.VISIBLE);
                        } else if (EMOJI_HEART.equals(reaction.reactionType)) {
                            root.findViewById(R.id.heart_emoji_shade).setVisibility(View.VISIBLE);
                        }
                    });
                    break;
                }
            }
        });
    }

    public void show(@NonNull View anchor, @NonNull Consumer<String> handleSelection) {
        View contentView = getContentView();
        contentView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED));
        showAsDropDown(anchor, (contentView.getPaddingRight() + contentView.getPaddingLeft() + anchor.getWidth() - contentView.getMeasuredWidth()) / 2 , -contentView.getMeasuredHeight() - anchor.getHeight() - 2);

        getContentView().findViewById(R.id.cry_emoji).setOnClickListener(v -> handleSelection.accept(EMOJI_CRY));
        getContentView().findViewById(R.id.angry_emoji).setOnClickListener(v -> handleSelection.accept(EMOJI_ANGRY));
        getContentView().findViewById(R.id.shocked_emoji).setOnClickListener(v -> handleSelection.accept(EMOJI_SHOCKED));
        getContentView().findViewById(R.id.laugh_emoji).setOnClickListener(v -> handleSelection.accept(EMOJI_LAUGH));
        getContentView().findViewById(R.id.thumbs_up_emoji).setOnClickListener(v -> handleSelection.accept(EMOJI_THUMB));
        getContentView().findViewById(R.id.heart_emoji).setOnClickListener(v -> handleSelection.accept(EMOJI_HEART));
        getContentView().findViewById(R.id.more_options).setOnClickListener(null); // TODO(jack): Allow selecting any emoji
    }
}
