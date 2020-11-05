package com.halloapp.xmpp;

import com.halloapp.id.UserId;

public class WhisperKeysMessage {

    public final Integer count;
    public final UserId userId;

    public WhisperKeysMessage(int count) {
        this.count = count;
        this.userId = null;
    }

    public WhisperKeysMessage(UserId userId) {
        this.count = null;
        this.userId = userId;
    }
}
