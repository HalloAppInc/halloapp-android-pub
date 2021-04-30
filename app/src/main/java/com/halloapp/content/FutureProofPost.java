package com.halloapp.content;

import androidx.annotation.Nullable;

import com.halloapp.id.UserId;

public class FutureProofPost extends Post {

    private byte[] protoBytes;

    public FutureProofPost(long rowId, UserId senderUserId, String postId, long timestamp, int transferred, int seen, String text) {
        super(rowId, senderUserId, postId, timestamp, transferred, seen, Post.TYPE_FUTURE_PROOF, text);
    }

    public void setProtoBytes(@Nullable byte[] protoBytes) {
        this.protoBytes = protoBytes;
    }

    public byte[] getProtoBytes() {
        return protoBytes;
    }
}
