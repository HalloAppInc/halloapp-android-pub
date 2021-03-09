package com.halloapp.noise;

import android.text.format.DateUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.goterl.lazycode.lazysodium.interfaces.Sign;
import com.halloapp.Me;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.keys.PrivateEdECKey;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.proto.server.AuthRequest;
import com.halloapp.proto.server.CertMessage;
import com.halloapp.proto.server.NoiseMessage;
import com.halloapp.proto.server.Packet;
import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.Log;
import com.southernstorm.noise.protocol.CipherState;
import com.southernstorm.noise.protocol.CipherStatePair;
import com.southernstorm.noise.protocol.HandshakeState;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.ShortBufferException;

public class HANoiseSocket extends Socket {

    private static final String ROOT_PUB_CERT = "1dcd81dc096613759b186e93f354fff0a2f1e79390b8502a90bc461e08f98077";
    private static final String XX_PROTOCOL = "Noise_XX_25519_AESGCM_SHA256";
    private static final String IK_PROTOCOL = "Noise_IK_25519_AESGCM_SHA256";

    /** First byte of packet size reserved for future use */
    private static final int PACKET_SIZE_MASK = 0xFFFFFF;

    private static final int PUBLIC_KEY_SIZE = 32;
    private static final int BUFFER_SIZE = 4096;
    private static final int CONNECT_TIMEOUT = 20_000;
    private static final int READ_TIMEOUT = 3 * (int)DateUtils.MINUTE_IN_MILLIS;

    private final Me me;

    private HandshakeState handshakeState;

    private final ByteArrayOutputStream readerOutputStream = new ByteArrayOutputStream();

    private CipherState sendCrypto;
    private CipherState recvCrypto;

    public HANoiseSocket(@NonNull Me me, @NonNull InetAddress address, final int port) throws IOException {
        this.me = me;
        connect(new InetSocketAddress(address, port), CONNECT_TIMEOUT);
        setSoTimeout(READ_TIMEOUT);
    }

    @WorkerThread
    public void authenticate(@NonNull AuthRequest authRequest) throws IOException, NoiseException {
        byte[] noiseKey = me.getMyEd25519NoiseKey();
        if (noiseKey == null) {
           throw new NoiseException("Missing registered key for noise authentication");
        }
        PublicEdECKey serverStaticKey = me.getServerStaticKey();
        if (serverStaticKey == null) {
            Log.i("NoiseSocket/authenticate no saved server static key, doing XX handshake");
            try {
                performXXHandshake(authRequest, noiseKey);
                me.setServerStaticKey(getServerStaticKey());
            } catch (NoSuchAlgorithmException | ShortBufferException | BadPaddingException | CryptoException e) {
                throw new NoiseException(e);
            }
        } else {
            Log.i("NoiseSocket/authenticate trying IK handshake");
            try {
                performIKHandshake(authRequest, noiseKey, serverStaticKey.getKeyMaterial());
            } catch (NoSuchAlgorithmException | ShortBufferException | BadPaddingException | CryptoException e) {
                throw new NoiseException(e);
            }
        }
    }

    private void sendHAHandshakeSignature() throws IOException {
        OutputStream out = getOutputStream();
        out.write('H');
        out.write('A');
        out.write('0');
        out.write('0');
    }

    private void performIKHandshake(@NonNull AuthRequest authRequest, byte[] localKeypair, byte[] remoteStaticKey) throws NoSuchAlgorithmException, IOException, ShortBufferException, BadPaddingException, CryptoException, NoiseException {
        sendHAHandshakeSignature();

        handshakeState = new HandshakeState(IK_PROTOCOL, HandshakeState.INITIATOR);

        PrivateEdECKey priv = new PrivateEdECKey(Arrays.copyOfRange(localKeypair, 32, 96));
        byte[] convertedKey = CryptoUtils.convertPrivateEdToX(priv).getKeyMaterial();
        handshakeState.getLocalKeyPair().setPrivateKey(convertedKey, 0);
        handshakeState.getRemotePublicKey().setPublicKey(remoteStaticKey, 0);

        handshakeState.start();

        byte[] clientConfig = authRequest.toByteArray();

        byte[] msgBuf = new byte[BUFFER_SIZE];
        int msgALen = handshakeState.writeMessage(msgBuf, 0, clientConfig, 0, clientConfig.length);
        writeNoiseMessage(NoiseMessage.MessageType.IK_A, msgBuf, msgALen);

        NoiseMessage msgB = readNextNoiseMessage();
        byte[] msgBContent = msgB.getContent().toByteArray();

        boolean fallback = false;
        if (NoiseMessage.MessageType.XX_FALLBACK_A.equals(msgB.getMessageType())) {
            Log.i("NoiseSocket/ikHandshake falling back to XX");
            fallback = true;
            handshakeState.fallback();
            handshakeState.start();

            int certLen = handshakeState.readMessage(msgBContent, 0, msgBContent.length, msgBuf, 0);
            boolean correctCert = verifyCertificate(msgBuf, certLen);
            if (!correctCert) {
                throw new NoiseException("Certificate received does not match!");
            }
            int msgCLen = handshakeState.writeMessage(msgBuf, 0, clientConfig,0, clientConfig.length);
            writeNoiseMessage(NoiseMessage.MessageType.XX_FALLBACK_B, msgBuf, msgCLen);
        } else if(NoiseMessage.MessageType.IK_B.equals(msgB.getMessageType())) {
            handshakeState.readMessage(msgBContent, 0, msgBContent.length, msgBuf, 0);
        } else {
            throw new NoiseException("Unexpected Noise protocol message received, expecting XX_FALLBACK_B or IK_B");
        }

        finishHandshake();

        if (fallback) {
            me.setServerStaticKey(getServerStaticKey());
            // TODO: clarkc remove after we figure out fallback issue
            Log.e("NoiseSocket/xx_fallback triggered. Old: " + Base64.encodeToString(remoteStaticKey, Base64.NO_WRAP) + " New: " + Base64.encodeToString(getServerStaticKey(), Base64.NO_WRAP));
        }
    }

    private void performXXHandshake(@NonNull AuthRequest authRequest, byte[] localKeypair) throws NoSuchAlgorithmException, IOException, ShortBufferException, BadPaddingException, CryptoException, NoiseException {
        sendHAHandshakeSignature();

        handshakeState = new HandshakeState(XX_PROTOCOL, HandshakeState.INITIATOR);

        PrivateEdECKey priv = new PrivateEdECKey(Arrays.copyOfRange(localKeypair, 32, 96));
        handshakeState.getLocalKeyPair().setPrivateKey(CryptoUtils.convertPrivateEdToX(priv).getKeyMaterial(), 0);

        handshakeState.start();

        byte[] msgBuf = new byte[BUFFER_SIZE];
        int msgALen = handshakeState.writeMessage(msgBuf, 0, null, 0, 0);
        writeNoiseMessage(NoiseMessage.MessageType.XX_A, msgBuf, msgALen);

        NoiseMessage msgB = readNextNoiseMessage();
        if (!msgB.getMessageType().equals(NoiseMessage.MessageType.XX_B)) {
            throw new NoiseException("Unexpected noise protocol message received, expecting XX_B");
        }
        int msgBPayloadLen = handshakeState.readMessage(msgB.getContent().toByteArray(), 0, msgB.getContent().size(), msgBuf, 0);

        boolean correctCert = verifyCertificate(msgBuf, msgBPayloadLen);
        if (!correctCert) {
            throw new NoiseException("Certificate received does not match!");
        }

        byte[] clientConfig = authRequest.toByteArray();

        int msgCLen = handshakeState.writeMessage(msgBuf, 0, clientConfig, 0, clientConfig.length);
        writeNoiseMessage(NoiseMessage.MessageType.XX_C, msgBuf, msgCLen);

        finishHandshake();
    }

    private void finishHandshake() throws NoiseException {
        if (HandshakeState.SPLIT != handshakeState.getAction()) {
            throw new NoiseException("Noise handshake failed");
        }

        CipherStatePair crypto = handshakeState.split();

        sendCrypto = crypto.getSender();
        recvCrypto = crypto.getReceiver();
        Log.i("NoiseSocket handshake complete");
    }

    public byte[] readPacket() throws IOException, ShortBufferException, BadPaddingException, NoiseException {
        if (recvCrypto == null) {
            throw new NoiseException("You have to authenticate first");
        }
        byte[] packetBytes = readNextMessage();
        if (packetBytes == null) {
            throw new IOException("No packet returned, disconnected");
        }
        byte[] decryptedBytes = new byte[packetBytes.length];
        int decryptedLen = recvCrypto.decryptWithAd(null, packetBytes, 0, decryptedBytes, 0, packetBytes.length);
        return Arrays.copyOf(decryptedBytes, decryptedLen);
    }

    public void writePacket(@NonNull Packet packet) throws ShortBufferException, IOException, NoiseException {
        if (sendCrypto == null) {
            throw new NoiseException("You have to authenticate first");
        }
        byte[] packetBytes = packet.toByteArray();
        byte[] encryptedBytes = new byte[packetBytes.length + sendCrypto.getMACLength()];
        int encryptedLen = sendCrypto.encryptWithAd(null, packetBytes, 0, encryptedBytes, 0, packetBytes.length);
        writeMessage(encryptedBytes, 0, encryptedLen);
    }

    private boolean verifyCertificate(byte[] certificate, int certLen) {
        byte[] remotePubKey = getRemotePublicKey();
        byte[] rootSigPubKey = StringUtils.bytesFromHexString(ROOT_PUB_CERT);

        byte[] msg = new byte[certLen - Sign.BYTES];
        boolean correctSignature = CryptoUtils.verifyOpen(msg, certificate, certLen, rootSigPubKey);
        if (!correctSignature) {
            return false;
        }
        try {
            CertMessage certMessage = CertMessage.parseFrom(msg);
            return Arrays.equals(certMessage.getServerKey().toByteArray(), remotePubKey);
        } catch (InvalidProtocolBufferException e) {
            return false;
        }
    }

    private byte[] getRemotePublicKey() {
        byte[] remotePubKey = new byte[PUBLIC_KEY_SIZE];
        handshakeState.getRemotePublicKey().getPublicKey(remotePubKey, 0);
        return remotePubKey;
    }

    private void writeMessage(byte[] packetBytes, int offset, int len) throws IOException {
        byte[] msgALenBytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(len).array();
        OutputStream out = getOutputStream();
        out.write(msgALenBytes);
        out.write(packetBytes, offset, len);
    }

    private void writeNoiseMessage(NoiseMessage.MessageType type, byte[] content, int len) throws IOException {
        NoiseMessage msg = NoiseMessage.newBuilder().setContent(ByteString.copyFrom(content, 0, len)).setMessageType(type).build();
        byte[] msgBytes = msg.toByteArray();
        writeMessage(msgBytes, 0, msgBytes.length);
    }

    private NoiseMessage readNextNoiseMessage() throws IOException {
        byte[] msg = readNextMessage();
        if (msg == null) {
            throw new IOException("Stream closed");
        }
        return NoiseMessage.parseFrom(msg);
    }

    private byte[] readNextMessage() throws IOException {
        InputStream in = getInputStream();
        byte[] recvBuffer = new byte[BUFFER_SIZE];
        while (readerOutputStream.size() < 4) {
            int read = in.read(recvBuffer);
            if (read <= 0) {
                Log.e("readNextMessage stream closed " + read);
                return null;
            }
            readerOutputStream.write(recvBuffer, 0, read);
        }
        byte[] recvBytes = readerOutputStream.toByteArray();
        ByteBuffer wrapped = ByteBuffer.wrap(recvBytes); // big-endian by default
        int packetSize = wrapped.getInt() & PACKET_SIZE_MASK;;
        readerOutputStream.reset();
        if (recvBytes.length > 4) {
            readerOutputStream.write(recvBytes, 4, recvBytes.length - 4);
        }
        while (readerOutputStream.size() < packetSize) {
            int read = in.read(recvBuffer);
            if (read <= 0) {
                Log.e("readNextMessage stream closed " + read);
                return null;
            }
            readerOutputStream.write(recvBuffer, 0, read);
        }
        recvBytes = readerOutputStream.toByteArray();
        readerOutputStream.reset();
        if (packetSize < recvBytes.length) {
            readerOutputStream.write(recvBytes, packetSize, recvBytes.length - packetSize);
        }
        return Arrays.copyOfRange(recvBytes, 0, packetSize);
    }

    @Nullable
    public byte[] getServerStaticKey() {
        if (handshakeState == null || handshakeState.getRemotePublicKey() == null) {
            return null;
        }
        return getRemotePublicKey();
    }
}
