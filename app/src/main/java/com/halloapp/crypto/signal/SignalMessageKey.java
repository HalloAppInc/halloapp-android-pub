package com.halloapp.crypto.signal;

import androidx.annotation.NonNull;

import com.halloapp.crypto.CryptoException;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;

public class SignalMessageKey {
    private final static int MESSAGE_KEY_BYTES = 80;

    private final int ephemeralKeyId;
    private final int previousChainLength;
    private final int currentChainIndex;
    private final byte[] messageKey;

    public SignalMessageKey(int ephemeralKeyId, int previousChainLength, int currentChainIndex, @NonNull byte[] messageKey) throws CryptoException {
        if (ephemeralKeyId < 0) {
            Log.e("Invalid ephemeral key id " + ephemeralKeyId);
            throw new CryptoException("neg_ephemeral_key_id");
        } else if (messageKey.length != MESSAGE_KEY_BYTES) {
            throw new CryptoException("msg_key_wrong_size");
        }
        Preconditions.checkArgument(previousChainLength >= 0);
        Preconditions.checkArgument(currentChainIndex >= 0);

        this.ephemeralKeyId = ephemeralKeyId;
        this.previousChainLength = previousChainLength;
        this.currentChainIndex = currentChainIndex;
        this.messageKey = messageKey;
    }

    public byte[] getKeyMaterial() {
        return messageKey;
    }

    public int getEphemeralKeyId() {
        return ephemeralKeyId;
    }

    public int getPreviousChainLength() {
        return previousChainLength;
    }

    public int getCurrentChainIndex() {
        return currentChainIndex;
    }

    @NonNull
    @Override
    public String toString() {
        return "MessageKey [ekid=" + ephemeralKeyId + "; pcl=" + previousChainLength + "; cci=" + currentChainIndex + "]";
    }
}
