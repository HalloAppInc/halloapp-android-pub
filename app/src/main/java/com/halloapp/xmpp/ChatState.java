package com.halloapp.xmpp;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.halloapp.id.ChatId;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ChatState {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Type.AVAILABLE, Type.TYPING})
    public @interface Type {
        int AVAILABLE = 0;
        int TYPING = 1;
    }

    public final @Type int type;
    public final ChatId chatId;

    public ChatState(@Type int type, @NonNull ChatId chatId) {
        this.type = type;
        this.chatId = chatId;
    }
}
