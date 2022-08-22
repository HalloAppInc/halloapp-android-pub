package com.halloapp.emoji;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;

import com.halloapp.R;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Message;
import com.halloapp.content.Reaction;
import com.halloapp.util.BgWorkers;

public class ReactionPopupWindow extends PopupWindow {

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
        setOutsideTouchable(true);
        setFocusable(true);

        BgWorkers.getInstance().execute(() -> {
            for (Reaction reaction : ContentDb.getInstance().getReactions(contentItem.id)) {
                if (reaction.senderUserId.isMe()) {
                    root.post(() -> {
                        if ("\uD83D\uDE25".equals(reaction.reactionType)) {
                            root.findViewById(R.id.cry_emoji_shade).setVisibility(View.VISIBLE);
                        } else if ("\uD83D\uDE20".equals(reaction.reactionType)) {
                            root.findViewById(R.id.angry_emoji_shade).setVisibility(View.VISIBLE);
                        } else if ("\uD83D\uDE2E".equals(reaction.reactionType)) {
                            root.findViewById(R.id.shocked_emoji_shade).setVisibility(View.VISIBLE);
                        } else if ("\uD83D\uDE02".equals(reaction.reactionType)) {
                            root.findViewById(R.id.laugh_emoji_shade).setVisibility(View.VISIBLE);
                        } else if ("\uD83D\uDC4D".equals(reaction.reactionType)) {
                            root.findViewById(R.id.thumbs_up_emoji_shade).setVisibility(View.VISIBLE);
                        } else if ("❤".equals(reaction.reactionType)) {
                            root.findViewById(R.id.heart_emoji_shade).setVisibility(View.VISIBLE);
                        }
                    });
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

}
