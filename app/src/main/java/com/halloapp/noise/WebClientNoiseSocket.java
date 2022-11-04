package com.halloapp.noise;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.halloapp.Me;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.keys.PrivateEdECKey;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.proto.server.NoiseMessage;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.southernstorm.noise.protocol.CipherState;
import com.southernstorm.noise.protocol.CipherStatePair;
import com.southernstorm.noise.protocol.HandshakeState;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.ShortBufferException;

public class WebClientNoiseSocket {

    private static final String IK_PROTOCOL = "Noise_IK_25519_AESGCM_SHA256";
    private static final String KK_PROTOCOL = "Noise_KK_25519_AESGCM_SHA256";

    private static final int BUFFER_SIZE = 4096;
    private static final int NOISE_EXTRA_SIZE = 128; // Noise actually only needs 96 extra bytes, but using 128 just in case

    private final Me me;
    private HandshakeState handshakeState;

    private CipherState sendCrypto;
    private CipherState recvCrypto;

    private Connection connection;

    public WebClientNoiseSocket(@NonNull Me me, @NonNull Connection connection) {
        this.me = me;
        this.connection = connection;
    }

    @WorkerThread
    public void initialize(@NonNull byte[] connectionInfo, boolean useKK) throws NoiseException {
        byte[] noiseKey = me.getMyWebClientEd25519NoiseKey();
        if (noiseKey == null) {
            throw new NoiseException("Missing my web client key for noise authentication");
        }
        if (me.getWebClientStaticKey() == null) {
            throw new NoiseException("Missing web client static key for noise authentication");
        }
        initialize(noiseKey, connectionInfo, useKK);
    }

    public void initialize(@NonNull byte[] noiseKey, @NonNull byte[] connectionInfo, @NonNull boolean useKK) throws NoiseException {
        PublicEdECKey webClientStaticKey = me.getWebClientStaticKey();
        if (webClientStaticKey == null) {
            Log.e("WebClientNoiseSocket web client key doesn't exist. Make sure qrCode was successfully scanned/converted to key");
        }
        try {
            if (useKK) {
                performKKHandshake(connectionInfo, noiseKey, webClientStaticKey);
            } else {
                performIKHandshake(connectionInfo, noiseKey, webClientStaticKey);
            }
        } catch (IOException | NoSuchAlgorithmException | CryptoException | ShortBufferException e) {
            throw new NoiseException(e);
        }
    }

    private void performIKHandshake(@NonNull byte[] connectionInfo, byte[] localKeypair, PublicEdECKey webClientStaticKey) throws IOException, NoSuchAlgorithmException, CryptoException, ShortBufferException {
        handshakeState = new HandshakeState(IK_PROTOCOL, HandshakeState.INITIATOR);

        PrivateEdECKey priv = new PrivateEdECKey(Arrays.copyOfRange(localKeypair, 32, 96));
        byte[] convertedKey = CryptoUtils.convertPrivateEdToX(priv).getKeyMaterial();
        handshakeState.getLocalKeyPair().setPrivateKey(convertedKey, 0);
        handshakeState.getRemotePublicKey().setPublicKey(webClientStaticKey.getKeyMaterial(), 0);

        handshakeState.start();

        byte[] msgBuf = createMsgBuffer(connectionInfo.length);
        int msgALen = handshakeState.writeMessage(msgBuf, 0, connectionInfo, 0, connectionInfo.length);
        connection.sendMessageToWebClient(msgBuf, NoiseMessage.MessageType.IK_A, webClientStaticKey, msgALen);
    }

    private void performKKHandshake(@NonNull byte[] connectionInfo, byte[] localKeypair, PublicEdECKey webClientStaticKey) throws IOException, NoSuchAlgorithmException, CryptoException, ShortBufferException {
        handshakeState = new HandshakeState(KK_PROTOCOL, HandshakeState.INITIATOR);

        PrivateEdECKey priv = new PrivateEdECKey(Arrays.copyOfRange(localKeypair, 32, 96));
        byte[] convertedKey = CryptoUtils.convertPrivateEdToX(priv).getKeyMaterial();
        handshakeState.getLocalKeyPair().setPrivateKey(convertedKey, 0);
        handshakeState.getRemotePublicKey().setPublicKey(webClientStaticKey.getKeyMaterial(), 0);

        handshakeState.start();

        byte[] msgBuf = createMsgBuffer(connectionInfo.length);
        int msgALen = handshakeState.writeMessage(msgBuf, 0, connectionInfo, 0, connectionInfo.length);
        connection.sendMessageToWebClient(msgBuf, NoiseMessage.MessageType.KK_A, webClientStaticKey, msgALen);
    }

    public void finishHandshake(byte[] msgBContent) throws NoiseException, BadPaddingException, ShortBufferException {
        byte[] msgBuf = createMsgBuffer(BUFFER_SIZE);
        handshakeState.readMessage(msgBContent, 0, msgBContent.length, msgBuf,0);

        if (HandshakeState.SPLIT != handshakeState.getAction()) {
            throw new NoiseException("Noise handshake failed");
        }

        CipherStatePair crypto = handshakeState.split();

        sendCrypto = crypto.getSender();
        recvCrypto = crypto.getReceiver();
        Log.i("NoiseSocket handshake complete");
    }

    private byte[] createMsgBuffer(int initLength) {
        return new byte[Math.max(initLength + NOISE_EXTRA_SIZE, BUFFER_SIZE)];
    }
}
