package com.halloapp.ui.chat;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.util.Hex;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.halloapp.Me;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.crypto.keys.PublicXECKey;
import com.halloapp.id.UserId;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.logs.Log;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class KeyVerificationViewModel extends AndroidViewModel {

    private UserId userId;

    final ComputableLiveData<String> name;
    final ComputableLiveData<Bitmap> qrCode;
    final ComputableLiveData<List<String>> safetyNumber;

    private final Me me = Me.getInstance();
    private final EncryptedKeyStore encryptedKeyStore = EncryptedKeyStore.getInstance();

    public KeyVerificationViewModel(@NonNull Application application, @NonNull UserId userId) {
        super(application);

        this.userId = userId;

        name = new ComputableLiveData<String>() {
            @Override
            protected String compute() {
                return ContactsDb.getInstance().getContact(userId).getDisplayName();
            }
        };

        qrCode = new ComputableLiveData<Bitmap>() {
            @Override
            protected Bitmap compute() {
                PublicXECKey peerIdentityKey;
                try {
                    peerIdentityKey = CryptoUtils.convertPublicEdToX(encryptedKeyStore.getPeerPublicIdentityKey(userId));
                } catch (CryptoException e) {
                    Log.e("KeyVerification: Failed to fetch peer identity key", e);
                    return null;
                }

                PublicXECKey myIdentityKey;
                try {
                    myIdentityKey = CryptoUtils.convertPublicEdToX(encryptedKeyStore.getMyPublicEd25519IdentityKey());
                } catch (CryptoException e) {
                    Log.e("KeyVerification: Failed to convert my identity key to XEC format", e);
                    return null;
                }

                String version = bytesToString(new byte[] {0});
                String myUid = bytesToString(uidToBytes(me.getUser()));
                String peerUid = bytesToString(uidToBytes(userId.rawId()));
                String myIdentityKeyStr = bytesToString(myIdentityKey.getKeyMaterial());
                String peerIdentityKeyStr = bytesToString(peerIdentityKey.getKeyMaterial());

                StringBuilder sb = new StringBuilder();
                sb.append(version).append(myUid).append(peerUid).append(myIdentityKeyStr).append(peerIdentityKeyStr);

                try {
                    BitMatrix bm = new QRCodeWriter().encode(sb.toString(), BarcodeFormat.QR_CODE, 1000, 1000);

                    return bitMatrixToBitmap(bm);
                } catch (WriterException e) {
                    Log.e("KeyVerification failed to encode QR", e);
                }
                return null;
            }
        };

        safetyNumber = new ComputableLiveData<List<String>>() {
            @Override
            protected List<String> compute() {
                List<String> ret = new ArrayList<>();

                PublicXECKey peerIdentityKey;
                try {
                    peerIdentityKey = CryptoUtils.convertPublicEdToX(encryptedKeyStore.getPeerPublicIdentityKey(userId));
                } catch (CryptoException e) {
                    Log.e("KeyVerification: Failed to fetch peer identity key", e);
                    return null;
                }

                PublicXECKey myIdentityKey;
                try {
                    myIdentityKey = CryptoUtils.convertPublicEdToX(encryptedKeyStore.getMyPublicEd25519IdentityKey());
                } catch (CryptoException e) {
                    Log.e("KeyVerification: Failed to convert my identity key to XEC format", e);
                    return null;
                }

                List<String> a = getSafetyNumber(userId.rawId(), peerIdentityKey);
                List<String> b = getSafetyNumber(me.getUser(), myIdentityKey);
                if (ordered(a, b)) {
                    ret.addAll(a);
                    ret.addAll(b);
                } else {
                    ret.addAll(b);
                    ret.addAll(a);
                }

                return ret;
            }
        };
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

            byte[] bytes = input.getBytes(StandardCharsets.US_ASCII);
            for (int i=0; i<5200; i++) {
                bytes = messageDigest.digest(bytes);
                messageDigest.reset();
            }

            for (int i=0; i<6; i++) {
                byte[] sub = Arrays.copyOfRange(bytes, i * 5, (i+1) * 5);
                long l = ByteBuffer.wrap(CryptoUtils.concat(new byte[]{0,0,0}, sub)).getLong();
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
        byte[] ret = buffer.array();
        return ret;
    }

    private static String bytesToString(@NonNull byte[] a) {
        return new String(a, StandardCharsets.US_ASCII);
    }

    private static String uidFromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(bytes);
        buffer.flip();
        return Long.toString(buffer.getLong());
    }

    private static Bitmap bitMatrixToBitmap(@NonNull BitMatrix bitMatrix) {
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        int[] pixels = new int[width * height];
        for (int i=0; i<height; i++) {
            for (int j=0; j<width; j++) {
                pixels[i*height + j] = bitMatrix.get(j, i) ? Color.WHITE : Color.BLACK;
            }
        }

        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
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
