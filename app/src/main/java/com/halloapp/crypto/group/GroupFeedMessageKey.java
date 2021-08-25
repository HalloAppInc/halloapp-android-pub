package com.halloapp.crypto.group;

import androidx.annotation.NonNull;

import com.halloapp.crypto.CryptoException;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;

public class GroupFeedMessageKey {
    private final static int MESSAGE_KEY_BYTES = 80;

    private final int currentChainIndex;
    private final byte[] messageKey;

    public GroupFeedMessageKey(int currentChainIndex, @NonNull byte[] messageKey) throws CryptoException {
        if (messageKey.length != MESSAGE_KEY_BYTES) {
            throw new CryptoException("group_msg_key_wrong_size");
        }
        Preconditions.checkArgument(currentChainIndex >= 0);

        this.currentChainIndex = currentChainIndex;
        this.messageKey = messageKey;
    }

    public byte[] getKeyMaterial() {
        return messageKey;
    }

    public int getCurrentChainIndex() {
        return currentChainIndex;
    }

    @NonNull
    @Override
    public String toString() {
        return "MessageKey [cci=" + currentChainIndex + "]";
    }
}
