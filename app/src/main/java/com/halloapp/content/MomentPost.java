package com.halloapp.content;

import com.halloapp.id.UserId;

public class MomentPost extends Post {

    public UserId unlockedUserId;

    public MomentPost(long rowId, UserId senderUserId, String postId, long timestamp, int transferred, int seen, String text) {
        super(rowId, senderUserId, postId, timestamp, transferred, seen, TYPE_MOMENT, text);
    }

    @Override
    public boolean shouldSend() {
        return isOutgoing() && transferred == TRANSFERRED_NO;
    }
}
