package com.halloapp.emoji;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;

import com.halloapp.R;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Message;

public class ReactionPopupWindow extends PopupWindow {

    public ReactionPopupWindow(Context context, ContentItem contentItem) {
        super(context);

        View root = null;
        if (contentItem instanceof Message) {
            Message message = (Message) contentItem;
            if (message.isMeMessageSender()) {
                root = LayoutInflater.from(context).inflate(R.layout.message_item_outgoing_reaction_bubble, null, false);
            } else {
                root = LayoutInflater.from(context).inflate(R.layout.message_item_incoming_reaction_bubble, null, false);
            }
        }

        setContentView(root);
        setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setOutsideTouchable(true);
        setFocusable(true);
    }

    public void show(@NonNull View anchor) {
        View contentView = getContentView();
        contentView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED));
        showAsDropDown(anchor, (contentView.getPaddingRight() + contentView.getPaddingLeft() + anchor.getWidth() - contentView.getMeasuredWidth()) / 2 , -contentView.getMeasuredHeight() - anchor.getHeight() - 2);
    }

}
