package com.halloapp.content;

import androidx.annotation.NonNull;

import com.halloapp.id.UserId;

import java.util.List;

public class VoiceNotePost extends Post {

    public VoiceNotePost(long rowId, UserId senderUserId, String postId, long timestamp, int transferred, int seen) {
        super(rowId, senderUserId, postId, timestamp, transferred, seen, Post.TYPE_VOICE_NOTE, "");
    }

    @Override
    public boolean shouldSend() {
        return isOutgoing() && transferred == TRANSFERRED_NO;
    }

    @NonNull
    @Override
    public List<Media> getMedia() {
        if (media.isEmpty()) {
            return media;
        }
        return media.subList(1, media.size());
    }
}
