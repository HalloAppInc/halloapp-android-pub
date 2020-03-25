package com.halloapp.content;

import androidx.annotation.NonNull;

import com.halloapp.contacts.UserId;

public class Receipt {

    final UserId senderUserId;
    final String postId;

    public Receipt(@NonNull UserId senderUserId, @NonNull String postId) {
        this.senderUserId = senderUserId;
        this.postId = postId;
    }
}
