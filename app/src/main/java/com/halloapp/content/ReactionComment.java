package com.halloapp.content;

import com.halloapp.id.UserId;

public class ReactionComment extends Comment {
    public final Reaction reaction;
    public ReactionComment(Reaction reaction, long rowId, String postId, UserId senderUserId, String commentId, String parentCommentId, long timestamp, int transferred, boolean seen, String text) {
        super(rowId, postId, senderUserId, commentId, parentCommentId, timestamp, transferred, seen, text);
        this.reaction = reaction;
    }
}
