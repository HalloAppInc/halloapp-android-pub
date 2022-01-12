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
    @IntDef({Usage.MISSED_VOICE_CALL, Usage.LOGGED_VOICE_CALL})
    public @interface Usage {
        int MISSED_VOICE_CALL = 0;
        int LOGGED_VOICE_CALL = 1;
        int MISSED_VIDEO_CALL = 2;
    }

    public @Usage int callUsage;
    public long callDuration;

    public CallMessage(long rowId, ChatId chatId, UserId senderUserId, String messageId, long timestamp, int usage, int state) {
        super(rowId, chatId, senderUserId, messageId, timestamp, Message.TYPE_CALL, usage, state, "", null, -1, null, -1, null, 0);

        this.callUsage = usage;
    }
}
