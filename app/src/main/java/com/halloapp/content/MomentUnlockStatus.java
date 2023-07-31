package com.halloapp.content;

import com.halloapp.BuildConfig;
import com.halloapp.util.Preconditions;

public class MomentUnlockStatus {
    public String unlockingMomentId;
    public @Post.TransferredState int transferred = Post.TRANSFERRED_NO;

    public boolean isUnlocking() {
        return unlockingMomentId != null && transferred == Post.TRANSFERRED_NO;
    }

    public boolean isUnlocked() {
        Preconditions.checkState(BuildConfig.IS_KATCHUP, "HalloApp does not require unlocking");
        return unlockingMomentId != null && transferred == Post.TRANSFERRED_YES;
    }
}
