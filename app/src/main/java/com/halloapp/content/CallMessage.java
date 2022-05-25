package com.halloapp.content;

import androidx.annotation.IntDef;

import com.halloapp.id.ChatId;
import com.halloapp.id.UserId;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CallMessage extends Message {

    @Override
    public boolean isLocalMessage() {
        return true;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Usage.MISSED_VOICE_CALL, Usage.LOGGED_VOICE_CALL, Usage.MISSED_VIDEO_CALL, Usage.LOGGED_VIDEO_CALL, Usage.UNANSWERED_VOICE_CALL, Usage.UNANSWERED_VIDEO_CALL})
    public @interface Usage {
        int MISSED_VOICE_CALL = 0;
        int LOGGED_VOICE_CALL = 1;
        int MISSED_VIDEO_CALL = 2;
        int LOGGED_VIDEO_CALL = 3;
        int UNANSWERED_VOICE_CALL = 4;
        int UNANSWERED_VIDEO_CALL = 5;
    }

    public @Usage int callUsage;
    public long callDuration;

    public CallMessage(long rowId, ChatId chatId, UserId senderUserId, String messageId, long timestamp, int usage, int state) {
        super(rowId, chatId, senderUserId, messageId, timestamp, Message.TYPE_CALL, usage, state, "", null, -1, null, -1, null, 0);

        this.callUsage = usage;
    }

    public boolean isMissedCall() {
        return callUsage == Usage.MISSED_VIDEO_CALL || callUsage == Usage.MISSED_VOICE_CALL;
    }
}
