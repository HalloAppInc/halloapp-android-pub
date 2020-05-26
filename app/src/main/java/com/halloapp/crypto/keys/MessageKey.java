package com.halloapp.crypto.keys;

import androidx.annotation.NonNull;

import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;

public class MessageKey {
    private final static int MESSAGE_KEY_BYTES = 80;
    private final static int INT_SIZE_BYTES = 4;

    private final int ephemeralKeyId;
    private final int previousChainLength;
    private final int currentChainIndex;
    private final byte[] messageKey;

    MessageKey(int ephemeralKeyId, int previousChainLength, int currentChainIndex, byte[] messageKey) throws InvalidKeyException {
        if (ephemeralKeyId < 0 || messageKey.length != MESSAGE_KEY_BYTES) {
            throw new InvalidKeyException();
        }
        Preconditions.checkArgument(previousChainLength >= 0);
        Preconditions.checkArgument(currentChainIndex >= 0);

        this.ephemeralKeyId = ephemeralKeyId;
        this.previousChainLength = previousChainLength;
        this.currentChainIndex = currentChainIndex;
        this.messageKey = messageKey;
    }

    public String encode() {
        return StringUtils.bytesToHexString(ByteBuffer.allocate(3 * INT_SIZE_BYTES + MESSAGE_KEY_BYTES).putInt(ephemeralKeyId).putInt(previousChainLength).putInt(currentChainIndex).put(messageKey).array());
    }

    public static MessageKey decode(String s) throws InvalidKeyException {
        ByteBuffer byteBuffer = ByteBuffer.wrap(StringUtils.bytesFromHexString(s));
        int ephemeralKeyId = byteBuffer.getInt();
        int previousChainLength = byteBuffer.getInt();
        int currentChainIndex = byteBuffer.getInt();

        byte[] messageKey = new byte[MESSAGE_KEY_BYTES];
        byteBuffer.get(messageKey);

        return new MessageKey(ephemeralKeyId, previousChainLength, currentChainIndex, messageKey);
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
