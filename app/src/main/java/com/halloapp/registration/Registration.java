package com.halloapp.registration;

import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.google.protobuf.ByteString;
import com.halloapp.AppContext;
import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.Notifications;
import com.halloapp.Preferences;
import com.halloapp.content.ContentDb;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.OneTimePreKey;
import com.halloapp.crypto.keys.PrivateEdECKey;
import com.halloapp.crypto.keys.PublicXECKey;
import com.halloapp.noise.HANoiseSocket;
import com.halloapp.noise.NoiseException;
import com.halloapp.proto.clients.SignedPreKey;
import com.halloapp.proto.server.HashcashRequest;
import com.halloapp.proto.server.HashcashResponse;
import com.halloapp.proto.server.IdentityKey;
import com.halloapp.proto.server.OtpRequest;
import com.halloapp.proto.server.OtpResponse;
import com.halloapp.proto.server.RegisterRequest;
import com.halloapp.proto.server.RegisterResponse;
import com.halloapp.proto.server.VerifyOtpRequest;
import com.halloapp.proto.server.VerifyOtpResponse;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.LanguageUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.ThreadUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.util.logs.LogUploaderWorker;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.SocketConnector;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.BadPaddingException;
import javax.crypto.ShortBufferException;

public class Registration {

    private static final String NOISE_HOST = "s.halloapp.net";
    private static final String DEBUG_NOISE_HOST = "s-test.halloapp.net";
    private static final int NOISE_PORT = 5208;
    private static final int RETRY_DEFAULT_WAIT_TIME_SECONDS = 15;
    private static final int HASHCASH_BACKOFF_START_MS = 100;
    private static final int HASHCASH_REQUEST_MAX_RETRIES = 5;

    private static Registration instance;

    public static Registration getInstance() {
        if (instance == null) {
            synchronized(Registration.class) {
                if (instance == null) {
                    instance = new Registration(Me.getInstance(), ContentDb.getInstance(), Connection.getInstance(), Preferences.getInstance(), EncryptedKeyStore.getInstance());
                }
            }
        }
        return instance;
    }

    private final Me me;
    private final ContentDb contentDb;
    private final Connection connection;
    private final Preferences preferences;
    private final EncryptedKeyStore encryptedKeyStore;
    private boolean isPhoneNeeded;
    private HashcashResult hashcashResult;

    private Registration(@NonNull Me me, @NonNull ContentDb contentDb, @NonNull Connection connection, @NonNull Preferences preferences, @NonNull EncryptedKeyStore encryptedKeyStore) {
        this.me = me;
        this.contentDb = contentDb;
        this.connection = connection;
        this.preferences = preferences;
        this.encryptedKeyStore = encryptedKeyStore;
    }

    public boolean getIsPhoneNeeded() {
        return isPhoneNeeded;
    }

    @WorkerThread
    public HashcashResult getHashcashSolution() {
        String hashcashChallenge = requestHashcashChallenge();
        return getHashcashSolution(hashcashChallenge, HASHCASH_BACKOFF_START_MS, 0);
    }

    @WorkerThread
    public HashcashResult getHashcashSolution(String hashcashChallenge) {
        return getHashcashSolution(hashcashChallenge, HASHCASH_BACKOFF_START_MS, 0);
    }


    @WorkerThread
    public HashcashResult getHashcashSolution(String hashcashChallenge, long backoffMs, int retryCount) {
        if (hashcashChallenge == null) {
            if (retryCount > HASHCASH_REQUEST_MAX_RETRIES) {
                Log.w("Registration/getHashcashSolution reached max retries");
                return new HashcashResult(HashcashResult.RESULT_FAILED_GET_CHALLENGE);
            } else {
                try {
                    Log.i("Registration/getHashcashSolution sleeping for " + backoffMs + " before rerequesting challenge");
                    Thread.sleep(backoffMs);
                } catch (InterruptedException e) {
                    Log.w("Registration/getHashcashSolution interrupted while sleeping");
                }
                hashcashChallenge = requestHashcashChallenge();
                return getHashcashSolution(hashcashChallenge, backoffMs * 2, retryCount + 1);
            }
        }
        Log.d("Hashcash: got challenge " + hashcashChallenge);
        long startMs = System.currentTimeMillis();
        String[] parts = hashcashChallenge.split(":");
        if (parts.length <= 0) {
            Log.e("Hashcash: could not parse challenge string");
            return new HashcashResult(HashcashResult.RESULT_FAILED_PARSE);
        }
        if (!"H".equals(parts[0])) {
            Log.w("Hashcash: got unrecognized tag " + parts[0]);
            return new HashcashResult(HashcashResult.RESULT_FAILED_UNRECOGNIZED_TAG);
        }
        if (parts.length != 6) {
            Log.e("Hashcash: got unexpected segment count " + parts.length);
            return new HashcashResult(HashcashResult.RESULT_FAILED_INVALID_SEGMENT_COUNT);
        }

        int difficulty;
        try {
            difficulty = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            Log.e("Hashcash: got invalid difficulty " + parts[1], e);
            return new HashcashResult(HashcashResult.RESULT_FAILED_INVALID_DIFFICULTY);
        }
        if (difficulty < 0) {
            Log.e("Hashcash: got invalid difficulty " + difficulty);
            return new HashcashResult(HashcashResult.RESULT_FAILED_INVALID_DIFFICULTY);
        }

        long expiresIn;
        try {
            expiresIn = Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
            Log.e("Hashcash: got invalid expiration " + parts[2], e);
            return new HashcashResult(HashcashResult.RESULT_FAILED_INVALID_EXPIRATION);
        }
        if (expiresIn <= 0) {
            Log.e("Hashcash: got invalid expiration " + expiresIn);
            return new HashcashResult(HashcashResult.RESULT_FAILED_INVALID_EXPIRATION);
        }

        // subject and nonce ignored

        if (!"SHA-256".equals(parts[5])) {
            Log.e("Hashcash: got unexpected hash algo " + parts[5]);
            return new HashcashResult(HashcashResult.RESULT_FAILED_UNEXPECTED_HASH_ALGO);
        }

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            Log.e("Hashcash: failed to create digest", e);
            return new HashcashResult(HashcashResult.RESULT_FAILED_NO_DIGEST);
        }

        byte[] raw = new byte[32];
        AtomicInteger len = new AtomicInteger(0);
        long count = 0;
        while (true) {
            if (System.currentTimeMillis() - startMs > expiresIn * 1000L) {
                Log.w("Hashcash: original challenge has expired, restarting");
                return getHashcashSolution();
            }
            byte[] trimmed = Arrays.copyOfRange(raw, 0, len.get());
            String attempt = hashcashChallenge + ":" + Base64.encodeToString(trimmed, Base64.NO_WRAP);
            digest.reset();
            byte[] hash = digest.digest(attempt.getBytes(StandardCharsets.US_ASCII));

            int leadingZeros = countLeadingZeros(hash);
            if (leadingZeros >= difficulty) {
                long timeTakenMs = System.currentTimeMillis() - startMs;
                Log.d("Hashcash: found solution " + attempt + " with len " + leadingZeros + " in " + timeTakenMs + "ms");
                return new HashcashResult(attempt, timeTakenMs);
            } else {
                incrementArray(raw, len);
            }
            count++;
            if (count % 1000 == 0) {
                Log.i("Hashcash: attempted " + count + " solutions");
            }
        }
    }

    private void incrementArray(@NonNull byte[] arr, @NonNull AtomicInteger len) {
        int index = len.get();
        while (index > 0) {
            if (arr[index - 1] != Byte.MAX_VALUE) {
                arr[index - 1]++;
                for (int i = index; i<len.get(); i++) {
                    arr[i] = Byte.MIN_VALUE;
                }
                break;
            }
            index--;
        }
        if (index <= 0) {
            int newLen = len.incrementAndGet();
            for (int i=0; i<newLen; i++) {
                arr[i] = Byte.MIN_VALUE;
            }
        }
    }

    private int countLeadingZeros(@NonNull byte[] hash) {
        int leadingZeros = 0;
        boolean end = false;
        for (int i=0; i<hash.length && !end; i++) {
            byte b = hash[i];

            // Big-endian, highest-order first
            for (int j=7; j>=0 && !end; j--) {
                int masked = b & (1 << j);
                if (masked != 0) {
                    end = true;
                } else {
                    leadingZeros++;
                }
            }
        }
        return leadingZeros;
    }

    @WorkerThread
    public @Nullable String requestHashcashChallenge() {
        final String host = preferences.getUseDebugHost() ? DEBUG_NOISE_HOST : NOISE_HOST;

        byte[] noiseKey = me.getMyRegEd25519NoiseKey();
        if (noiseKey == null) {
            noiseKey = CryptoUtils.generateEd25519KeyPair();
            me.saveNoiseRegKey(noiseKey);
        }
        HANoiseSocket noiseSocket = null;
        try {
            SocketConnector socketConnector = new SocketConnector(BgWorkers.getInstance().getExecutor());
            noiseSocket = socketConnector.connect(me, host, NOISE_PORT);
            noiseSocket.initialize(noiseKey, RegisterRequest.newBuilder()
                    .setHashcashRequest(HashcashRequest.newBuilder().build())
                    .build().toByteArray());

            RegisterResponse packet = RegisterResponse.parseFrom(noiseSocket.readPacket());
            if (!packet.hasHashcashResponse()) {
                Log.e("Registration/requestHashcashChallenge no hashcash response received");
                return null;
            }
            HashcashResponse response = packet.getHashcashResponse();
            isPhoneNeeded = !response.getIsPhoneNotNeeded();

            return response.getHashcashChallenge();
        } catch (IOException | NoiseException | BadPaddingException | ShortBufferException e) {
            Log.e("Registration.requestHashcashChallenge error", e);
            return null;
        } finally {
            if (noiseSocket != null && !noiseSocket.isClosed()) {
                try {
                    noiseSocket.close();
                } catch (IOException e) {
                    Log.w("Registration/Failed to close socket", e);
                }
            }
        }
    }

    @WorkerThread
    public @NonNull RegistrationRequestResult requestRegistration(@NonNull String phone, @Nullable String groupInviteToken, @Nullable String campaignId, @Nullable HashcashResult hashcashSolution) {
        return requestRegistrationTypeViaNoise(phone, groupInviteToken,  campaignId, false, hashcashSolution);
    }

    @WorkerThread
    private @NonNull RegistrationRequestResult requestRegistrationTypeViaNoise(@NonNull String phone, @Nullable String groupInviteToken, @Nullable String campaignId, boolean phoneCall, @Nullable HashcashResult hashcashResult) {
        final String host = preferences.getUseDebugHost() ? DEBUG_NOISE_HOST : NOISE_HOST;
        OtpRequest.Builder otpRequestBuilder = OtpRequest.newBuilder();
        otpRequestBuilder.setPhone(phone);
        otpRequestBuilder.setLangId(LanguageUtils.getLocaleIdentifier());
        otpRequestBuilder.setMethod(phoneCall ? OtpRequest.Method.VOICE_CALL : OtpRequest.Method.SMS);
        otpRequestBuilder.setUserAgent(Constants.USER_AGENT);
        if (hashcashResult != null) {
            if (hashcashResult.fullSolution != null) {
                otpRequestBuilder.setHashcashSolution(hashcashResult.fullSolution);
                Log.i("Registration/requestRegistrationTypeViaNoise using solution " + hashcashResult.fullSolution);
            } else {
                Log.w("Registration/requestRegistrationTypeViaNoise no hashcash solution provided");
            }
            otpRequestBuilder.setHashcashSolutionTimeTakenMs(hashcashResult.timeTakenMs);
        } else {
            Log.w("Registration/requestRegistrationTypeViaNoise no hashcash solution provided");
        }
        if (groupInviteToken != null) {
            otpRequestBuilder.setGroupInviteToken(groupInviteToken);
        }
        if (campaignId != null) {
            otpRequestBuilder.setCampaignId(campaignId);
        }
        byte[] noiseKey = me.getMyRegEd25519NoiseKey();
        if (noiseKey == null) {
            noiseKey = CryptoUtils.generateEd25519KeyPair();
            me.saveNoiseRegKey(noiseKey);
        }
        HANoiseSocket noiseSocket = null;
        try {
            SocketConnector socketConnector = new SocketConnector(BgWorkers.getInstance().getExecutor());
            noiseSocket = socketConnector.connect(me, host, NOISE_PORT);
            noiseSocket.initialize(noiseKey, RegisterRequest.newBuilder()
                    .setOtpRequest(otpRequestBuilder)
                    .build().toByteArray());

            RegisterResponse packet = RegisterResponse.parseFrom(noiseSocket.readPacket());
            if (!packet.hasOtpResponse()) {
                Log.e("Registration/requestRegistrationTypeViaNoise no otp response received");
                return new RegistrationRequestResult(RegistrationRequestResult.RESULT_FAILED_SERVER, RETRY_DEFAULT_WAIT_TIME_SECONDS);
            }
            OtpResponse response = packet.getOtpResponse();
            final OtpResponse.Result result = response.getResult();
            final String normalizedPhone = response.getPhone();
            final OtpResponse.Reason error = response.getReason();
            final int retryTime = (int) response.getRetryAfterSecs();
            Log.i("Registration.requestRegistration result=" + result + " error=" + error + " phone=" + normalizedPhone);
            if (OtpResponse.Reason.INVALID_HASHCASH_NONCE.equals(error) || OtpResponse.Reason.WRONG_HASHCASH_SOLUTION.equals(error)) {
                Log.i("Hashcash failure; uploading logs");
                LogUploaderWorker.uploadLogs(AppContext.getInstance().get());
            }
            if (!OtpResponse.Result.SUCCESS.equals(result)) {
                return new RegistrationRequestResult(phone, RegistrationRequestResult.translateServerErrorCode(error), retryTime);
            }
            if (TextUtils.isEmpty(phone)) {
                return new RegistrationRequestResult(RegistrationRequestResult.RESULT_FAILED_SERVER, retryTime);
            }
            return new RegistrationRequestResult(phone, retryTime);
        } catch (IOException e) {
            Log.e("Registration.requestRegistration network failed", e);
            return new RegistrationRequestResult(RegistrationRequestResult.RESULT_FAILED_NETWORK, 0);
        } catch (NoiseException | BadPaddingException | ShortBufferException e) {
            Log.e("Registration.requestRegistration noise failed", e);
            return new RegistrationRequestResult(RegistrationRequestResult.RESULT_FAILED_SERVER, RETRY_DEFAULT_WAIT_TIME_SECONDS);
        } finally {
            if (noiseSocket != null && !noiseSocket.isClosed()) {
                try {
                    noiseSocket.close();
                } catch (IOException e) {
                    Log.w("Registration/Failed to close socket", e);
                }
            }
        }
    }

    public @NonNull RegistrationRequestResult requestRegistrationViaVoiceCall(@NonNull String phone, @Nullable String groupInviteToken, @Nullable String campaignId, @Nullable HashcashResult hashcashSolution) {
        return requestRegistrationTypeViaNoise(phone, groupInviteToken, campaignId, true, hashcashSolution);
    }

    public @NonNull RegistrationRequestResult registerPhoneNumber(@Nullable String name, @NonNull String phone, @Nullable String groupInviteToken, @Nullable String campaignId, @Nullable HashcashResult hashcashSolution) {
        if (name != null) {
            me.saveName(name);
        }
        me.savePhone(phone);
        return requestRegistration(phone, groupInviteToken, campaignId, hashcashSolution);
    }

    @WorkerThread
    public @Nullable RegistrationVerificationResult verifyWithoutPhoneNumber(@Nullable HashcashResult hashcashResult) {
        if (hashcashResult == null) {
            return null;
        }
        RegistrationVerificationResult verificationResult = verifyRegistrationViaNoise(null, null, null, null, me.getName(), hashcashResult);
        processRegistrationVerificationResult(verificationResult);
        return verificationResult;
    }

    @WorkerThread
    public @NonNull RegistrationVerificationResult verifyPhoneNumber(@NonNull String phone, @NonNull String code, @Nullable String campaignId, @Nullable String groupInviteToken) {
        RegistrationVerificationResult verificationResult = verifyRegistrationViaNoise(phone, code, campaignId, groupInviteToken, me.getName(), null);
        processRegistrationVerificationResult(verificationResult);
        return verificationResult;
    }

    @WorkerThread
    private void processRegistrationVerificationResult(@NonNull RegistrationVerificationResult verificationResult) {
        if (verificationResult.result == RegistrationVerificationResult.RESULT_OK) {
            if (!BuildConfig.IS_KATCHUP) {
                Notifications.getInstance(AppContext.getInstance().get()).clearFinishRegistrationNotification();
                preferences.setUnfinishedRegistrationNotifyDelayInDaysTimeOne(1);
                preferences.setUnfinishedRegistrationNotifyDelayInDaysTimeTwo(1);
                preferences.setPrevUnfinishedRegistrationNotificationTimeInMillis(0);
            }

            String uid = me.getUser();
            if (!Preconditions.checkNotNull(verificationResult.user).equals(uid)) {
                // New user, we should clear data
                contentDb.deleteDb();
            }

            if (!isPhoneNeeded) {
                me.saveRegistrationNoise(Preconditions.checkNotNull(verificationResult.user));
            } else {
                me.saveRegistrationNoise(
                        Preconditions.checkNotNull(verificationResult.user),
                        Preconditions.checkNotNull(verificationResult.phone));
            }

            preferences.setInitialRegistrationTime(System.currentTimeMillis());
            connection.connect();
        }
    }

    @WorkerThread
    private @NonNull RegistrationVerificationResult verifyRegistrationViaNoise(@Nullable String phone, @Nullable String code, @Nullable String campaignId, @Nullable String groupInviteToken, @Nullable String name, @Nullable HashcashResult hashcashResult) {
        ThreadUtils.setSocketTag();
        if (!encryptedKeyStore.clientPrivateKeysSet()) {
            encryptedKeyStore.edit().generateClientPrivateKeys().apply();
            Log.critical("Updated identity key to " + Base64.encodeToString(encryptedKeyStore.getMyPublicEd25519IdentityKey().getKeyMaterial(), Base64.NO_WRAP));
        }
        IdentityKey identityKeyProto = IdentityKey.newBuilder()
                .setPublicKey(ByteString.copyFrom(encryptedKeyStore.getMyPublicEd25519IdentityKey().getKeyMaterial()))
                .build();
        PublicXECKey signedPreKey = encryptedKeyStore.getMyPublicSignedPreKey();
        byte[] signature = CryptoUtils.verifyDetached(signedPreKey.getKeyMaterial(), encryptedKeyStore.getMyPrivateEd25519IdentityKey());
        SignedPreKey signedPreKeyProto = SignedPreKey.newBuilder()
                .setPublicKey(ByteString.copyFrom(signedPreKey.getKeyMaterial()))
                .setSignature(ByteString.copyFrom(signature))
                // TODO(jack): ID
                .build();
        List<ByteString> oneTimePreKeys = new ArrayList<>();
        for (OneTimePreKey otpk : encryptedKeyStore.getNewBatchOfOneTimePreKeys()) {
            com.halloapp.proto.clients.OneTimePreKey toAdd = com.halloapp.proto.clients.OneTimePreKey.newBuilder()
                    .setId(otpk.id)
                    .setPublicKey(ByteString.copyFrom(otpk.publicXECKey.getKeyMaterial()))
                    .build();
            oneTimePreKeys.add(toAdd.toByteString());
        }

        Log.i("Registration.verifyRegistration phone=" + phone + " code=" + code);

        final String host = preferences.getUseDebugHost() ? DEBUG_NOISE_HOST : NOISE_HOST;
        VerifyOtpRequest.Builder verifyOtpRequestBuilder = VerifyOtpRequest.newBuilder();
        if (isPhoneNeeded) {
            verifyOtpRequestBuilder.setPhone(phone);
            verifyOtpRequestBuilder.setCode(code);
        } else if (hashcashResult != null) {
            if (hashcashResult.fullSolution != null) {
                verifyOtpRequestBuilder.setHashcashSolution(hashcashResult.fullSolution);
                Log.i("Registration/verifyRegistrationViaNoise using solution " + hashcashResult.fullSolution);
            } else {
                Log.w("Registration/verifyRegistrationViaNoise no hashcash solution provided");
            }
            verifyOtpRequestBuilder.setHashcashSolutionTimeTakenMs(hashcashResult.timeTakenMs);
        } else {
            Log.w("Registration/verifyRegistrationViaNoise no hashcash solution provided");
        }
        if (name != null) {
            verifyOtpRequestBuilder.setName(name);
        }
        verifyOtpRequestBuilder.setIdentityKey(identityKeyProto.toByteString());
        verifyOtpRequestBuilder.setSignedKey(signedPreKeyProto.toByteString());
        verifyOtpRequestBuilder.addAllOneTimeKeys(oneTimePreKeys);
        verifyOtpRequestBuilder.setUserAgent(Constants.USER_AGENT);
        if (groupInviteToken != null) {
            verifyOtpRequestBuilder.setGroupInviteToken(groupInviteToken);
        }
        if (campaignId != null) {
            verifyOtpRequestBuilder.setCampaignId(campaignId);
        }

        byte[] keypair = CryptoUtils.generateEd25519KeyPair();
        byte[] pub = Arrays.copyOfRange(keypair, 0, 32);
        byte[] priv = Arrays.copyOfRange(keypair, 32, 96);
        verifyOtpRequestBuilder.setStaticKey(ByteString.copyFrom(pub));
        byte[] sign = CryptoUtils.sign("HALLO".getBytes(), new PrivateEdECKey(priv));
        verifyOtpRequestBuilder.setSignedPhrase(ByteString.copyFrom(sign));
        HANoiseSocket noiseSocket = null;
        try {
            SocketConnector socketConnector = new SocketConnector(BgWorkers.getInstance().getExecutor());
            noiseSocket = socketConnector.connect(me, host, NOISE_PORT);
            byte[] noiseKey = me.getMyRegEd25519NoiseKey();
            if (noiseKey == null) {
                noiseKey = CryptoUtils.generateEd25519KeyPair();
                me.saveNoiseRegKey(noiseKey);
            }
            noiseSocket.initialize(noiseKey, RegisterRequest.newBuilder().setVerifyRequest(verifyOtpRequestBuilder).build().toByteArray());

            RegisterResponse packet = RegisterResponse.parseFrom(noiseSocket.readPacket());
            final VerifyOtpResponse response = packet.getVerifyResponse();
            VerifyOtpResponse.Result result = response.getResult();
            VerifyOtpResponse.Reason reason = response.getReason();

            final String uid = Long.toString(response.getUid());
            if (!VerifyOtpResponse.Result.SUCCESS.equals(result) || (TextUtils.isEmpty(phone) && isPhoneNeeded) || TextUtils.isEmpty(uid)) {
                Log.w("Registration.verifyRegistration server rejected verification");
                return new RegistrationVerificationResult(RegistrationVerificationResult.RESULT_FAILED_SERVER, reason);
            }
            me.saveNoiseKey(keypair);
            final boolean nameSet = !TextUtils.isEmpty(response.getName());
            final boolean usernameSet = !TextUtils.isEmpty(response.getUsername());
            if (nameSet) {
                me.saveName(response.getName());
            }
            if (usernameSet) {
                me.saveUsername(response.getUsername());
            }
            if (BuildConfig.IS_KATCHUP && nameSet && usernameSet) {
                preferences.setProfileSetup(true);
            }
            Log.i("Registration.verifyRegistration success with uid " + uid);
            return new RegistrationVerificationResult(uid, null, phone);
        } catch (IOException e) {
            Log.e("Registration.verifyRegistration network failed", e);
            return new RegistrationVerificationResult(RegistrationVerificationResult.RESULT_FAILED_NETWORK, null);
        } catch (BadPaddingException | NoiseException | ShortBufferException e) {
            Log.e("Registration.verifyRegistration noise failed", e);
            return new RegistrationVerificationResult(RegistrationVerificationResult.RESULT_FAILED_SERVER, null);
        } finally {
            if (noiseSocket != null && !noiseSocket.isClosed()) {
                try {
                    noiseSocket.close();
                } catch (IOException e) {
                    Log.w("Registration/Failed to close socket", e);
                }
            }
        }
    }

    public static class HashcashResult {
        @Retention(RetentionPolicy.SOURCE)
        @IntDef({RESULT_OK, RESULT_FAILED_GET_CHALLENGE, RESULT_FAILED_PARSE,
                RESULT_FAILED_UNRECOGNIZED_TAG, RESULT_FAILED_INVALID_SEGMENT_COUNT,
                RESULT_FAILED_INVALID_DIFFICULTY, RESULT_FAILED_INVALID_EXPIRATION,
                RESULT_FAILED_UNEXPECTED_HASH_ALGO, RESULT_FAILED_NO_DIGEST})
        @interface Result {}
        public static final int RESULT_OK = 0;
        public static final int RESULT_FAILED_GET_CHALLENGE = 1;
        public static final int RESULT_FAILED_PARSE = 2;
        public static final int RESULT_FAILED_UNRECOGNIZED_TAG = 3;
        public static final int RESULT_FAILED_INVALID_SEGMENT_COUNT = 4;
        public static final int RESULT_FAILED_INVALID_DIFFICULTY = 5;
        public static final int RESULT_FAILED_INVALID_EXPIRATION = 6;
        public static final int RESULT_FAILED_UNEXPECTED_HASH_ALGO = 7;
        public static final int RESULT_FAILED_NO_DIGEST = 8;

        public final String fullSolution;
        public final @Result int result;
        public final long timeTakenMs;

        HashcashResult(@NonNull String fullSolution, long timeTakenMs) {
            this.fullSolution = fullSolution;
            this.result = RESULT_OK;
            this.timeTakenMs = timeTakenMs;
        }

        HashcashResult(@Result int result) {
            Preconditions.checkState(result != RESULT_OK);
            this.fullSolution = null;
            this.result = result;
            this.timeTakenMs = -1;
        }
    }

    public static class RegistrationRequestResult {

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({RESULT_OK, RESULT_FAILED_SERVER, RESULT_FAILED_NETWORK, RESULT_FAILED_SERVER_SMS_FAIL,
                RESULT_FAILED_SERVER_CANNOT_ENROLL, RESULT_FAILED_SERVER_NO_FRIENDS,
                RESULT_FAILED_SERVER_NOT_INVITED, RESULT_FAILED_CLIENT_EXPIRED,
                RESULT_FAILED_RETRIED_TOO_SOON, RESULT_FAILED_INVALID_PHONE_NUMBER,
                RESULT_FAILED_HASHCASH})
        @interface Result {}
        public static final int RESULT_OK = 0;
        public static final int RESULT_FAILED_NETWORK = 1;
        public static final int RESULT_FAILED_SERVER = 2;
        public static final int RESULT_FAILED_SERVER_SMS_FAIL = 3; // Sending the SMS failed
        public static final int RESULT_FAILED_SERVER_CANNOT_ENROLL = 4; // Error during the enroll function. This one does not make much sense.
        public static final int RESULT_FAILED_SERVER_NO_FRIENDS = 5; // The Phone number is not in any existing users contacts. We don't let users create accounts if they are not going to have any friends. Note this error is not returned for 555 phone numbers.
        public static final int RESULT_FAILED_SERVER_NOT_INVITED = 6; // Phone number trying to register has not been invited using the in-app invites system.
        public static final int RESULT_FAILED_CLIENT_EXPIRED = 7;
        public static final int RESULT_FAILED_RETRIED_TOO_SOON = 8;
        public static final int RESULT_FAILED_INVALID_PHONE_NUMBER = 9;
        public static final int RESULT_FAILED_HASHCASH = 10;

        public final String phone;
        public final @Result int result;
        public final int retryWaitTimeSeconds;

        RegistrationRequestResult(@NonNull String phone, int retryWaitTimeSeconds) {
            this.phone = phone;
            this.result = RESULT_OK;
            this.retryWaitTimeSeconds = retryWaitTimeSeconds;
        }

        RegistrationRequestResult(@Nullable String phone, @Result int result, int retryWaitTimeSeconds) {
            Preconditions.checkState(result != RESULT_OK);
            this.phone = phone;
            this.result = result;
            this.retryWaitTimeSeconds = retryWaitTimeSeconds;
        }

        RegistrationRequestResult(@Result int result, int retryWaitTimeSeconds) {
            this(null, result, retryWaitTimeSeconds);
        }

        static @Result int translateServerErrorCode(OtpResponse.Reason reason) {
            if (OtpResponse.Reason.OTP_FAIL.equals(reason)) {
                return RESULT_FAILED_SERVER_SMS_FAIL;
            } else if (OtpResponse.Reason.NOT_INVITED.equals(reason)) {
                return RESULT_FAILED_SERVER_NOT_INVITED;
            } else if (OtpResponse.Reason.INVALID_CLIENT_VERSION.equals(reason)) {
                return RESULT_FAILED_CLIENT_EXPIRED;
            } else if (OtpResponse.Reason.RETRIED_TOO_SOON.equals(reason)) {
                return RESULT_FAILED_RETRIED_TOO_SOON;
            } else if (OtpResponse.Reason.INVALID_PHONE_NUMBER.equals(reason)) {
                return RESULT_FAILED_INVALID_PHONE_NUMBER;
            } else if (OtpResponse.Reason.INVALID_HASHCASH_NONCE.equals(reason)) {
                return RESULT_FAILED_HASHCASH;
            } else if (OtpResponse.Reason.WRONG_HASHCASH_SOLUTION.equals(reason)) {
                return RESULT_FAILED_HASHCASH;
            }
            return RESULT_FAILED_SERVER;
        }
    }

    public static class RegistrationVerificationResult {

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({RESULT_OK, RESULT_FAILED_SERVER, RESULT_FAILED_NETWORK})
        @interface Result {}
        public static final int RESULT_OK = 0;
        public static final int RESULT_FAILED_SERVER = 1;
        public static final int RESULT_FAILED_NETWORK = 2;

        public final String user;
        public final String password;
        public final String phone;
        public final @Result int result;
        public final VerifyOtpResponse.Reason reason;

        RegistrationVerificationResult(@NonNull String user, @Nullable String password, @Nullable String phone) {
            this.user = user;
            this.password = password;
            this.phone = phone;
            this.result = RESULT_OK;
            this.reason = null;
        }

        RegistrationVerificationResult(@Result int result, VerifyOtpResponse.Reason reason) {
            Preconditions.checkState(result != RESULT_OK);
            this.user = null;
            this.password = null;
            this.phone = null;
            this.result = result;
            this.reason = reason;
        }
    }
}
