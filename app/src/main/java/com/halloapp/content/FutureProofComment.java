package com.halloapp.content;

import com.halloapp.id.UserId;

public class FutureProofComment extends Comment {

    private byte[] protoBytes;

    public FutureProofComment(long rowId, String postId, UserId senderUserId, String commentId, String parentCommentId, long timestamp, boolean transferred, boolean seen, String text) {
        super(rowId, postId, senderUserId, commentId, parentCommentId, timestamp, transferred, seen, text);

        this.type = Comment.TYPE_FUTURE_PROOF;
    }

    public void setProtoBytes(byte[] protoBytes) {
        this.protoBytes = protoBytes;
    }

    public byte[] getProtoBytes() {
        return protoBytes;
    }
}
