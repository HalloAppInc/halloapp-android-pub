package com.halloapp.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.halloapp.R;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Mention;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.ui.chat.ChatActivity;
import com.halloapp.util.RandomId;
import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.Log;

import java.util.List;

public class ReactionsPopupWindow extends PopupWindow {

    private final ContentDb contentDb = ContentDb.getInstance();

    private Post post;
    private int mediaIndex;

    private Context context;

    public ReactionsPopupWindow(Context context) {
        super(context);

        this.context = context;

        View root = LayoutInflater.from(context).inflate(R.layout.reactions_bar, null, false);

        setContentView(root);
        setBackgroundDrawable(new ColorDrawable(
                android.graphics.Color.TRANSPARENT));
        setOutsideTouchable(true);
        setFocusable(true);

        View sad = root.findViewById(R.id.sad);
        View perfect = root.findViewById(R.id.perfect);
        View heart = root.findViewById(R.id.heart);
        View surprise = root.findViewById(R.id.surprise);
        View laughing = root.findViewById(R.id.laughing);

        View.OnClickListener reactionClickListener = view -> {
          if (view instanceof TextView) {
              TextView tv = (TextView) view;
              sendMessage(tv.getText().toString());
          }
        };

        sad.setOnClickListener(reactionClickListener);
        perfect.setOnClickListener(reactionClickListener);
        heart.setOnClickListener(reactionClickListener);
        surprise.setOnClickListener(reactionClickListener);
        laughing.setOnClickListener(reactionClickListener);
    }

    public void show(@NonNull View anchor, @NonNull Post post, Integer mediaIndex) {
        View contentView = getContentView();
        this.post = post;
        this.mediaIndex = mediaIndex == null ? 0 : mediaIndex;
        contentView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED));
        showAsDropDown(anchor, contentView.getPaddingRight() + contentView.getPaddingLeft() + anchor.getWidth() - contentView.getMeasuredWidth() , -contentView.getMeasuredHeight() -anchor.getHeight());
    }

    private void sendMessage(String messageText) {
        if (TextUtils.isEmpty(messageText)) {
            return;
        }
        final Message message = new Message(0,
                post.senderUserId,
                UserId.ME,
                RandomId.create(),
                System.currentTimeMillis(),
                Message.TYPE_CHAT,
                Message.USAGE_CHAT,
                Message.STATE_INITIAL,
                messageText,
                post.id,
                mediaIndex,
                null,
                0,
                post.senderUserId,
                0);
        message.addToStorage(contentDb);
        final Intent intent = new Intent(context, ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(ChatActivity.EXTRA_CHAT_ID, post.senderUserId);
        intent.putExtra(ChatActivity.EXTRA_OPEN_KEYBOARD, true);
        context.startActivity(intent);
        dismiss();
    }
}
