package com.halloapp.ui.chat.chat;

import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Bitmap;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.Me;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.crypto.CryptoByteUtils;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.crypto.keys.PublicXECKey;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.IdentityKey;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.Preconditions;
import com.halloapp.util.QrUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.WhisperKeysResponseIq;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class KeyVerificationViewModel extends AndroidViewModel {

    private final UserId userId;

    final ComputableLiveData<String> name;
    final ComputableLiveData<KeyVerificationData> keyVerificationData;
    final ComputableLiveData<Boolean> verifiedSwitchState;
    final MutableLiveData<Boolean> verificationResult = new MutableLiveData<>();

    private final Me me = Me.getInstance();
    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private final Connection connection = Connection.getInstance();
    private final EncryptedKeyStore encryptedKeyStore = EncryptedKeyStore.getInstance();

    public KeyVerificationViewModel(@NonNull Application application, @NonNull UserId userId) {
        super(application);

        this.userId = userId;

        name = new ComputableLiveData<String>() {
            @SuppressLint("RestrictedApi")
            @Override
            protected String compute() {
                return ContactsDb.getInstance().getContact(userId).getDisplayName();
            }
        };

        verifiedSwitchState = new ComputableLiveData<Boolean>() {
            @SuppressLint("RestrictedApi")
            @Override
            protected Boolean compute() {
                return encryptedKeyStore.getPeerVerified(userId);
            }
        };

        keyVerificationData = new ComputableLiveData<KeyVerificationData>() {
            @SuppressLint("RestrictedApi")
            @Override
            protected KeyVerificationData compute() {
                PublicXECKey peerIdentityKey;
                try {
                    peerIdentityKey = CryptoUtils.convertPublicEdToX(encryptedKeyStore.getPeerPublicIdentityKey(userId));
                } catch (CryptoException e) {
                    Log.e("KeyVerification: Failed to get peer identity key; trying to fetch from server", e);
                    try {
                        WhisperKeysResponseIq keysIq = connection.downloadKeys(userId).await();
                        IdentityKey identityKeyProto = IdentityKey.parseFrom(keysIq.identityKey);
                        byte[] identityKeyBytes = identityKeyProto.getPublicKey().toByteArray();
                        PublicEdECKey peerEdECIdentityKey = new PublicEdECKey(identityKeyBytes);

                        encryptedKeyStore.edit().setPeerPublicIdentityKey(userId, peerEdECIdentityKey).apply();

                        peerIdentityKey = CryptoUtils.convertPublicEdToX(peerEdECIdentityKey);
                    } catch (ObservableErrorException | InterruptedException | InvalidProtocolBufferException | CryptoException e2) {
                        Log.e("KeyVerification: Failed to fetch the key from the server", e2);
                        return null;
                    }
                }

                PublicXECKey myIdentityKey;
                try {
                    myIdentityKey = CryptoUtils.convertPublicEdToX(encryptedKeyStore.getMyPublicEd25519IdentityKey());
                } catch (CryptoException e) {
                    Log.e("KeyVerification: Failed to convert my identity key to XEC format", e);
                    return null;
                }

                List<String> safetyNumber = new ArrayList<>();

                List<String> a = getSafetyNumber(userId.rawId(), peerIdentityKey);
                List<String> b = getSafetyNumber(me.getUser(), myIdentityKey);
                if (ordered(a, b)) {
                    safetyNumber.addAll(a);
                    safetyNumber.addAll(b);
                } else {
                    safetyNumber.addAll(b);
                    safetyNumber.addAll(a);
                }

                byte[] contents = CryptoByteUtils.concat(new byte[] {0}, uidToBytes(me.getUser()), uidToBytes(userId.rawId()), myIdentityKey.getKeyMaterial(), peerIdentityKey.getKeyMaterial());
                String s = bytesToString(contents);
                Bitmap qrCode = QrUtils.encode(s);
                return new KeyVerificationData(qrCode, safetyNumber);
            }
        };
    }

    void markVerificationState(boolean verified) {
        bgWorkers.execute(() -> {
            encryptedKeyStore.edit().setPeerVerified(userId, verified).apply();
            verifiedSwitchState.invalidate();
        });
    }

    void verify(@NonNull String result) {
        bgWorkers.execute(() -> {
            byte[] bytes = stringToBytes(result);
            byte[] version = Arrays.copyOfRange(bytes, 0, 1);
            if (version[0] != 0) {
                Log.e("KeyVerification: Unrecognized key verification version!");
                return;
            }

            if (bytes.length != 81) {
                Log.e("KeyVerification: Incorrect payload size " + bytes.length);
                return;
            }

            byte[] peerUidRecv = Arrays.copyOfRange(bytes, 1, 9);
            byte[] myUidRecv = Arrays.copyOfRange(bytes, 9, 17);
            byte[] peerIdentityKeyRecv = Arrays.copyOfRange(bytes, 17, 49);
            byte[] myIdentityKeyRecv = Arrays.copyOfRange(bytes, 49, 81);

            String peerUidStr = uidFromBytes(peerUidRecv);
            String myUidStr = uidFromBytes(myUidRecv);

            if (!me.getUser().equals(myUidStr)) {
                Log.w("KeyVerification: My UID did not match");
                verificationResult.postValue(false);
                return;
            }

            if (!userId.rawId().equals(peerUidStr)) {
                Log.w("KeyVerification: Peer UID did not match");
                verificationResult.postValue(false);
                return;
            }

            PublicXECKey peerIdentityKey;
            try {
                peerIdentityKey = CryptoUtils.convertPublicEdToX(encryptedKeyStore.getPeerPublicIdentityKey(userId));
            } catch (CryptoException e) {
                Log.e("KeyVerification: Failed to fetch peer identity key", e);
                verificationResult.postValue(false);
                return;
            }

            PublicXECKey myIdentityKey;
            try {
                myIdentityKey = CryptoUtils.convertPublicEdToX(encryptedKeyStore.getMyPublicEd25519IdentityKey());
            } catch (CryptoException e) {
                Log.e("KeyVerification: Failed to convert my identity key to XEC format", e);
                verificationResult.postValue(false);
                return;
            }

            if (!Arrays.equals(myIdentityKeyRecv, myIdentityKey.getKeyMaterial())) {
                Log.e("KeyVerification: My identity key did not match");
                verificationResult.postValue(false);
                return;
            }

            if (!Arrays.equals(peerIdentityKeyRecv, peerIdentityKey.getKeyMaterial())) {
                Log.e("KeyVerification: Peer identity key did not match");
                verificationResult.postValue(false);
                return;
            }

            Log.i("KeyVerification: Verification success");
            verificationResult.postValue(true);
        });
    }

    private static boolean ordered(List<String> a, List<String> b) {
        for (int i=0; i<a.size(); i++) {
            String as = a.get(i);
            String bs = b.get(i);
            int cmp = as.compareTo(bs);
            if (cmp < 0) {
                return true;
            } else if (cmp > 0) {
                return false;
            }
        }
        Log.w("Strings for key verification match");
        return true;
    }

    private static List<String> getSafetyNumber(String userId, PublicXECKey identityKey) {
        List<String> ret = new ArrayList<>();

        String input = userId + ";" + Base64.encodeToString(identityKey.getKeyMaterial(), Base64.NO_WRAP);

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA512");

            byte[] bytes = stringToBytes(input);
            for (int i=0; i<5200; i++) {
                bytes = messageDigest.digest(bytes);
                messageDigest.reset();
            }

            for (int i=0; i<6; i++) {
                byte[] sub = Arrays.copyOfRange(bytes, i * 5, (i+1) * 5);
                long l = ByteBuffer.wrap(CryptoByteUtils.concat(new byte[]{0,0,0}, sub)).getLong();
                long m = l % 100_000;
                ret.add(String.format(Locale.US, "%05d", m));
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e("KeyVerification: Failed to get sha512 for safety number", e);
            return null;
        }

        return ret;
    }

    private static byte[] uidToBytes(@NonNull String uid) {
        long l = Long.parseLong(uid);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(l);
        return buffer.array();
    }

    private static String bytesToString(@NonNull byte[] a) {
        return new String(a, StandardCharsets.ISO_8859_1);
    }

    private static byte[] stringToBytes(@NonNull String s) {
        return s.getBytes(StandardCharsets.ISO_8859_1);
    }

    private static String uidFromBytes(@NonNull byte[] bytes) {
        Preconditions.checkArgument(bytes.length == 8);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(bytes);
        buffer.flip();
        return Long.toString(buffer.getLong());
    }

    public static final class KeyVerificationData {
        public final Bitmap qrCode;
        public final List<String> safetyNumber;

        public KeyVerificationData(Bitmap qrCode, List<String> safetyNumber) {
            this.qrCode = qrCode;
            this.safetyNumber = safetyNumber;
        }
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final UserId userId;

        Factory(@NonNull Application application, @NonNull UserId userId) {
            this.application = application;
            this.userId = userId;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(KeyVerificationViewModel.class)) {
                //noinspection unchecked
                return (T) new KeyVerificationViewModel(application, userId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
