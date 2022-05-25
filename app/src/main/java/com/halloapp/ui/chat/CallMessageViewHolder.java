package com.halloapp.ui.chat;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.halloapp.R;
import com.halloapp.calling.CallManager;
import com.halloapp.content.CallMessage;
import com.halloapp.content.Message;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.CallType;
import com.halloapp.util.TimeFormatter;

public class CallMessageViewHolder extends MessageViewHolder {

    private static final float DISABLED_BUTTON_ALPHA = 0.5f;

    private TextView logTitleView;
    private TextView durationView;
    private TextView callActionTextView;
    private ImageView callActionIconView;

    private View callButton;
    private int callType;

    private Drawable videoDrawable;
    private Drawable voiceDrawable;

    private final CallManager callManager = CallManager.getInstance();

    CallMessageViewHolder(@NonNull View itemView, @NonNull MessageViewHolderParent parent) {
        super(itemView, parent);

        videoDrawable = ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_video);
        voiceDrawable = ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_call);

        logTitleView = itemView.findViewById(R.id.call_status);
        durationView = itemView.findViewById(R.id.call_duration);
        callActionTextView = itemView.findViewById(R.id.call_text);
        callActionIconView = itemView.findViewById(R.id.call_icon);
        callButton = itemView.findViewById(R.id.call_button);
        callButton.setOnClickListener(v -> {
            if (message != null) {
                callManager.startCallActivity(v.getContext(), (UserId) message.chatId, CallType.forNumber(callType));
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
                    logTitleView.setText(R.string.log_missed_voice_call);
                    logTitleView.requestLayout();
                    color = ContextCompat.getColor(callActionIconView.getContext(), R.color.color_primary);
                    callActionIconView.setImageTintList(ColorStateList.valueOf(color));
                    callActionIconView.setImageDrawable(voiceDrawable);
                    callActionTextView.setTextColor(color);
                    callActionTextView.setText(R.string.call_action_voice);
                    callType = CallType.AUDIO_VALUE;
                    break;
                case CallMessage.Usage.LOGGED_VOICE_CALL:
                    durationView.setVisibility(View.VISIBLE);
                    logTitleView.setText(R.string.log_voice_call);
                    color = ContextCompat.getColor(callActionIconView.getContext(), R.color.color_secondary);
                    callActionIconView.setImageTintList(ColorStateList.valueOf(color));
                    callActionIconView.setImageDrawable(voiceDrawable);
                    callActionTextView.setTextColor(color);
                    callActionTextView.setText(R.string.call_action_voice);
                    durationView.setText(TimeFormatter.formatCallDuration(callMessage.callDuration));
                    callType = CallType.AUDIO_VALUE;
                    break;
                case CallMessage.Usage.MISSED_VIDEO_CALL:
                    durationView.setVisibility(View.GONE);
                    logTitleView.setText(R.string.log_missed_video_call);
                    color = ContextCompat.getColor(callActionIconView.getContext(), R.color.color_primary);
                    callActionIconView.setImageTintList(ColorStateList.valueOf(color));
                    callActionIconView.setImageDrawable(videoDrawable);
                    callActionTextView.setTextColor(color);
                    callActionTextView.setText(R.string.call_action_video);
                    callType = CallType.VIDEO_VALUE;
                    break;
                case CallMessage.Usage.LOGGED_VIDEO_CALL:
                    durationView.setVisibility(View.VISIBLE);
                    logTitleView.setText(R.string.log_video_call);
                    color = ContextCompat.getColor(callActionIconView.getContext(), R.color.color_secondary);
                    callActionIconView.setImageTintList(ColorStateList.valueOf(color));
                    callActionIconView.setImageDrawable(videoDrawable);
                    callActionTextView.setTextColor(color);
                    callActionTextView.setText(R.string.call_action_video);
                    durationView.setText(TimeFormatter.formatCallDuration(callMessage.callDuration));
                    callType = CallType.VIDEO_VALUE;
                    break;
                case CallMessage.Usage.UNANSWERED_VOICE_CALL:
                    durationView.setVisibility(View.GONE);
                    logTitleView.setText(R.string.log_unanswered_voice_call);
                    color = ContextCompat.getColor(callActionIconView.getContext(), R.color.color_secondary);
                    callActionIconView.setImageTintList(ColorStateList.valueOf(color));
                    callActionIconView.setImageDrawable(voiceDrawable);
                    callActionTextView.setTextColor(color);
                    callActionTextView.setText(R.string.call_action_voice);
                    callType = CallType.AUDIO_VALUE;
                    break;
                case CallMessage.Usage.UNANSWERED_VIDEO_CALL:
                    durationView.setVisibility(View.GONE);
                    logTitleView.setText(R.string.log_unanswered_video_call);
                    color = ContextCompat.getColor(callActionIconView.getContext(), R.color.color_secondary);
                    callActionIconView.setImageTintList(ColorStateList.valueOf(color));
                    callActionIconView.setImageDrawable(videoDrawable);
                    callActionTextView.setTextColor(color);
                    callActionTextView.setText(R.string.call_action_video);
                    callType = CallType.VIDEO_VALUE;
                    break;
            }
        }
    }

}
