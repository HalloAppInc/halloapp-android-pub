package com.halloapp.crypto.home;

import androidx.annotation.NonNull;

import com.halloapp.crypto.CryptoException;
import com.halloapp.util.Preconditions;

public class HomeFeedPostMessageKey {
    private final static int MESSAGE_KEY_BYTES = 80;

    private final int currentChainIndex;
    private final byte[] messageKey;

    public HomeFeedPostMessageKey(int currentChainIndex, @NonNull byte[] messageKey) throws CryptoException {
        if (messageKey.length != MESSAGE_KEY_BYTES) {
            throw new CryptoException("home_msg_key_wrong_size");
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
