package com.halloapp.content;

public class MomentUnlockStatus {
    public String unlockingMomentId;
    public @Post.TransferredState int transferred = Post.TRANSFERRED_NO;

    public boolean isUnlocking() {
        return unlockingMomentId != null && transferred == Post.TRANSFERRED_NO;
    }

    public boolean isUnlocked() {
        return unlockingMomentId != null && transferred == Post.TRANSFERRED_YES;
    }
}
