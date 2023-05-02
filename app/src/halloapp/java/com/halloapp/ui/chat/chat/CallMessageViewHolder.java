package com.halloapp.ui.chat.chat;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.halloapp.R;
import com.halloapp.calling.calling.CallManager;
import com.halloapp.contacts.Contact;
import com.halloapp.content.CallMessage;
import com.halloapp.content.Message;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.CallType;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.ViewDataLoader;

public class CallMessageViewHolder extends MessageViewHolder {

    private final TextView callTextView;
    private final ImageView callIconView;

    private int callType;

    private final Drawable videoDrawable;
    private final Drawable voiceDrawable;

    private final CallManager callManager = CallManager.getInstance();

    CallMessageViewHolder(@NonNull View itemView, @NonNull MessageViewHolderParent parent) {
        super(itemView, parent);

        videoDrawable = ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_video_call_button);
        voiceDrawable = ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_call);

        callIconView = itemView.findViewById(R.id.call_icon);
        callTextView = itemView.findViewById(R.id.call_text);

        contentView.setOnClickListener(v -> {
            if (message instanceof CallMessage) {
                CallMessage callMessage = (CallMessage) message;
                if (callMessage.isMissedCall()) {
                    callManager.startCallActivity(v.getContext(), (UserId) message.chatId, CallType.forNumber(callType));
                }
            }
        });
    }

    protected boolean shouldMergeBubbles(@Nullable Message msg1, @Nullable Message msg2) {
        return false;
    }

    protected @DrawableRes
    int getMessageBackground(@NonNull Message message) {
        return R.drawable.message_background_incoming;
    }

    @Override
    void bindTo(@NonNull Message message, int newMessageCountSeparator, @Nullable Message prevMessage, @Nullable Message nextMessage, boolean isLast) {
        super.bindTo(message, newMessageCountSeparator, prevMessage, nextMessage, isLast);

        if (message instanceof CallMessage) {
            parent.getContactLoader().cancel(callTextView);

            CallMessage callMessage = (CallMessage) message;
            int textColor = ContextCompat.getColor(callTextView.getContext(), R.color.secondary_text);
            Context context = callTextView.getContext();
            int iconColor = ContextCompat.getColor(context, R.color.secondary_text);
            switch (callMessage.callUsage) {
                case CallMessage.Usage.MISSED_VOICE_CALL:
                    textColor = ContextCompat.getColor(callTextView.getContext(), R.color.color_link);
                    callTextView.setText(R.string.call_log_missed_voice);
                    callType = CallType.AUDIO_VALUE;
                    iconColor = ContextCompat.getColor(context, R.color.call_decline);
                    break;
                case CallMessage.Usage.MISSED_VIDEO_CALL:
                    textColor = ContextCompat.getColor(callTextView.getContext(), R.color.color_link);
                    callTextView.setText(R.string.call_log_missed_video);
                    iconColor = ContextCompat.getColor(context, R.color.call_decline);
                    callType = CallType.VIDEO_VALUE;
                    break;
                case CallMessage.Usage.LOGGED_VIDEO_CALL:
                    callType = CallType.VIDEO_VALUE;
                    displayCallLog(callMessage);
                    break;
                case CallMessage.Usage.LOGGED_VOICE_CALL:
                    callType = CallType.AUDIO_VALUE;
                    displayCallLog(callMessage);
                    break;
                case CallMessage.Usage.UNANSWERED_VOICE_CALL:
                    callType = CallType.AUDIO_VALUE;
                    displayUnansweredCall(callMessage);
                    iconColor = ContextCompat.getColor(context, R.color.call_decline);
                    break;
                case CallMessage.Usage.UNANSWERED_VIDEO_CALL:
                    displayUnansweredCall(callMessage);
                    iconColor = ContextCompat.getColor(context, R.color.call_decline);
                    callType = CallType.VIDEO_VALUE;
                    break;
            }
            if (CallType.VIDEO_VALUE == callType) {
                callIconView.setImageDrawable(videoDrawable);
            } else {
                callIconView.setImageDrawable(voiceDrawable);
            }
            callTextView.setTextColor(textColor);
            callIconView.setImageTintList(ColorStateList.valueOf(iconColor));
        }
    }

    private void displayUnansweredCall(@NonNull CallMessage message) {
        parent.getContactLoader().load(callTextView, (UserId) message.chatId, new ViewDataLoader.Displayer<TextView, Contact>() {
            @Override
            public void showResult(@NonNull TextView view, @Nullable Contact result) {
                if (result != null) {
                    callTextView.setText(callTextView.getContext().getString(R.string.call_log_no_answer, result.getShortName()));
                }
            }

            @Override
            public void showLoading(@NonNull TextView view) {
                callTextView.setText("");
            }
        });
    }

    private void displayCallLog(@NonNull CallMessage message) {
        parent.getContactLoader().load(callTextView, (UserId) message.chatId, new ViewDataLoader.Displayer<TextView, Contact>() {
            @Override
            public void showResult(@NonNull TextView view, @Nullable Contact result) {
                if (result != null) {
                    if (message.isMeMessageSender()) {
                        callTextView.setText(callTextView.getContext().getString(R.string.call_log_with_duration_you_start, result.getShortName(), TimeFormatter.formatCallDuration(message.callDuration)));
                    } else {
                        callTextView.setText(callTextView.getContext().getString(R.string.call_log_with_duration_you_recv, result.getShortName(), TimeFormatter.formatCallDuration(message.callDuration)));
                    }
                }
            }

            @Override
            public void showLoading(@NonNull TextView view) {
                callTextView.setText("");
            }
        });
    }

}
