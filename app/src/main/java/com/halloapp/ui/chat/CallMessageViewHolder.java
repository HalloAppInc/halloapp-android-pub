package com.halloapp.ui.chat;

import android.content.res.ColorStateList;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.halloapp.R;
import com.halloapp.calling.CallManager;
import com.halloapp.content.CallMessage;
import com.halloapp.content.Message;
import com.halloapp.id.UserId;
import com.halloapp.ui.calling.CallActivity;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.TimeUtils;

public class CallMessageViewHolder extends MessageViewHolder {

    private static final float DISABLED_BUTTON_ALPHA = 0.5f;

    private TextView logTitleView;
    private TextView durationView;
    private TextView callActionTextView;
    private ImageView callActionIconView;

    private View callButton;

    private final CallManager callManager = CallManager.getInstance();

    CallMessageViewHolder(@NonNull View itemView, @NonNull MessageViewHolderParent parent) {
        super(itemView, parent);

        logTitleView = itemView.findViewById(R.id.call_status);
        durationView = itemView.findViewById(R.id.call_duration);
        callActionTextView = itemView.findViewById(R.id.call_text);
        callActionIconView = itemView.findViewById(R.id.call_icon);
        callButton = itemView.findViewById(R.id.call_button);
        callButton.setOnClickListener(v -> {
            if (message != null) {
                callManager.startCallActivity(v.getContext(), (UserId) message.chatId);
            }
        });
        callManager.getIsInCall().observe(this, (inCall) -> {
            callButton.setEnabled(!inCall);
            callButton.setAlpha(inCall ? DISABLED_BUTTON_ALPHA : 1f);
        });
    }

    @Override
    void bindTo(@NonNull Message message, int newMessageCountSeparator, @Nullable Message prevMessage, @Nullable Message nextMessage, boolean isLast) {
        super.bindTo(message, newMessageCountSeparator, prevMessage, nextMessage, isLast);

        if (message instanceof CallMessage) {
            CallMessage callMessage = (CallMessage) message;
            int color;
            switch (callMessage.callUsage) {
                case CallMessage.Usage.MISSED_VOICE_CALL:
                    durationView.setVisibility(View.GONE);
                    logTitleView.setText(R.string.log_missed_call);
                    color = ContextCompat.getColor(callActionIconView.getContext(), R.color.color_primary);
                    callActionIconView.setImageTintList(ColorStateList.valueOf(color));
                    callActionTextView.setTextColor(color);
                    break;
                case CallMessage.Usage.LOGGED_VOICE_CALL:
                    durationView.setVisibility(View.VISIBLE);
                    logTitleView.setText(R.string.log_voice_call);
                    color = ContextCompat.getColor(callActionIconView.getContext(), R.color.color_secondary);
                    callActionIconView.setImageTintList(ColorStateList.valueOf(color));
                    callActionTextView.setTextColor(color);
                    durationView.setText(TimeFormatter.formatCallDuration(callMessage.callDuration));
                    break;
            }
        }
    }

}
