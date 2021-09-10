package com.halloapp.content;

import com.halloapp.id.UserId;

public class VoiceNoteComment extends Comment {

    public VoiceNoteComment(
            long rowId,
            String postId,
            UserId senderUserId,
            String commentId,
            String parentCommentId,
            long timestamp,
            @TransferredState int transferred,
            boolean seen,
            String text) {
        super(rowId, postId, senderUserId, commentId, parentCommentId, timestamp, transferred, seen, text);

        this.type = Comment.TYPE_VOICE_NOTE;
    }
}
