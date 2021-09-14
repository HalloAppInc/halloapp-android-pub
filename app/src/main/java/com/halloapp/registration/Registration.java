package com.halloapp.registration;

import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.google.protobuf.ByteString;
import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.content.ContentDb;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.OneTimePreKey;
import com.halloapp.crypto.keys.PrivateEdECKey;
import com.halloapp.crypto.keys.PublicXECKey;
import com.halloapp.noise.HANoiseSocket;
import com.halloapp.noise.NoiseException;
import com.halloapp.proto.server.IdentityKey;
import com.halloapp.proto.clients.SignedPreKey;
import com.halloapp.proto.server.OtpRequest;
import com.halloapp.proto.server.OtpResponse;
import com.halloapp.proto.server.RegisterRequest;
import com.halloapp.proto.server.RegisterResponse;
import com.halloapp.proto.server.VerifyOtpRequest;
import com.halloapp.proto.server.VerifyOtpResponse;
import com.halloapp.util.FileUtils;
import com.halloapp.util.LanguageUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.ThreadUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.ShortBufferException;
import javax.net.ssl.HttpsURLConnection;

public class Registration {

    private static final String NOISE_HOST = "s.halloapp.net";
    private static final String DEBUG_NOISE_HOST = "s-test.halloapp.net";
    private static final int NOISE_PORT = 5208;
    private static final String HOST = "api.halloapp.net";
    private static final int RETRY_DEFAULT_WAIT_TIME_SECONDS = 15;

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

    private Registration(@NonNull Me me, @NonNull ContentDb contentDb, @NonNull Connection connection, @NonNull Preferences preferences, @NonNull EncryptedKeyStore encryptedKeyStore) {
        this.me = me;
        this.contentDb = contentDb;
        this.connection = connection;
        this.preferences = preferences;
        this.encryptedKeyStore = encryptedKeyStore;
    }

    @WorkerThread
    public @NonNull RegistrationRequestResult requestRegistration(@NonNull String phone, @Nullable String groupInviteToken) {
        return requestRegistrationType(phone, groupInviteToken,  false);
    }

    @WorkerThread
    private @NonNull RegistrationRequestResult requestRegistrationTypeViaNoise(@NonNull String phone, @Nullable String groupInviteToken, boolean phoneCall) {
        final String host = preferences.getUseDebugHost() ? DEBUG_NOISE_HOST : NOISE_HOST;
        OtpRequest.Builder otpRequestBuilder = OtpRequest.newBuilder();
        otpRequestBuilder.setPhone(phone);
        otpRequestBuilder.setLangId(LanguageUtils.getLocaleIdentifier());
        otpRequestBuilder.setMethod(phoneCall ? OtpRequest.Method.VOICE_CALL : OtpRequest.Method.SMS);
        otpRequestBuilder.setUserAgent(Constants.USER_AGENT);
        if (groupInviteToken != null) {
            otpRequestBuilder.setGroupInviteToken(groupInviteToken);
        }
        byte[] noiseKey = me.getMyRegEd25519NoiseKey();
        if (noiseKey == null) {
            noiseKey = CryptoUtils.generateEd25519KeyPair();
            me.saveNoiseRegKey(noiseKey);
        }
        HANoiseSocket noiseSocket = null;
        try {
            final InetAddress address = InetAddress.getByName(host);
            noiseSocket = new HANoiseSocket(me, address, NOISE_PORT);
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
            if (!OtpResponse.Result.SUCCESS.equals(result)) {
                return new RegistrationRequestResult(phone, RegistrationRequestResult.translateServerErrorCode(error), retryTime);
            }
            if (TextUtils.isEmpty(phone)) {
                return new RegistrationRequestResult(RegistrationRequestResult.RESULT_FAILED_SERVER, retryTime);
            }
            return new RegistrationRequestResult(phone, retryTime);
        } catch (IOException e) {
            Log.e("Registration.requestRegistration", e);
            return new RegistrationRequestResult(RegistrationRequestResult.RESULT_FAILED_NETWORK, 0);
        } catch (NoiseException | BadPaddingException | ShortBufferException e) {
            Log.e("Registration.requestRegistration", e);
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

    @WorkerThread
    private @NonNull RegistrationRequestResult requestRegistrationType(@NonNull String phone, @Nullable String groupInviteToken, boolean phoneCall) {
        if (Constants.USE_NOISE_FOR_REGISTRATION) {
            return requestRegistrationTypeViaNoise(phone, groupInviteToken, phoneCall);
        }
        Log.i("Registration.requestRegistration phone=" + phone);
        ThreadUtils.setSocketTag();

        InputStream inStream = null;
        HttpsURLConnection connection = null;
        try {
            final URL url = new URL("https://" + HOST + "/api/registration/request_otp");
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", Constants.USER_AGENT);
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);
            connection.setConnectTimeout(30_000);
            connection.setReadTimeout(30_000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            JSONObject requestJson = new JSONObject();
            requestJson.put("phone", phone);
            if (groupInviteToken != null) {
                requestJson.put("group_invite_token", groupInviteToken);
            }
            if (phoneCall) {
                requestJson.put("method", "voice_call");
            }
            requestJson.put("lang_id", LanguageUtils.getLocaleIdentifier());
            connection.getOutputStream().write(requestJson.toString().getBytes());

            final int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e("Registration.requestRegistration responseCode:" + responseCode);
            }
            inStream = responseCode < 400 ? connection.getInputStream() : connection.getErrorStream();
            final JSONObject responseJson = new JSONObject(FileUtils.inputStreamToString(inStream));
            final String result = responseJson.optString("result");
            final String normalizedPhone = responseJson.optString("phone");
            final String error = responseJson.optString("error");
            final String retryTimeStr = responseJson.optString("retry_after_secs");
            int retryTime = 0;
            try {
                retryTime = Integer.parseInt(retryTimeStr);
            } catch (NumberFormatException e) {
                Log.e("Registration/requestRegistration invalid retry time: " + retryTimeStr, e);
            }
            Log.i("Registration.requestRegistration result=" + result + " error=" + error + " phone=" + normalizedPhone);
            if (!"ok".equals(result)) {
                return new RegistrationRequestResult(phone, RegistrationRequestResult.translateServerErrorCode(error), retryTime);
            }
            if (TextUtils.isEmpty(phone)) {
                return new RegistrationRequestResult(RegistrationRequestResult.RESULT_FAILED_SERVER, retryTime);
            }
            return new RegistrationRequestResult(phone, retryTime);
        } catch (IOException e) {
            Log.e("Registration.requestRegistration", e);
            return new RegistrationRequestResult(RegistrationRequestResult.RESULT_FAILED_NETWORK, 0);
        } catch (JSONException e) {
            Log.e("Registration.requestRegistration", e);
            return new RegistrationRequestResult(RegistrationRequestResult.RESULT_FAILED_SERVER, RETRY_DEFAULT_WAIT_TIME_SECONDS);
        } finally {
            FileUtils.closeSilently(inStream);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public @NonNull RegistrationRequestResult requestRegistrationViaVoiceCall(@NonNull String phone, @Nullable String groupInviteToken) {
        return requestRegistrationType(phone, groupInviteToken, true);
    }

    public @NonNull RegistrationRequestResult registerPhoneNumber(@Nullable String name, @NonNull String phone, @Nullable String groupInviteToken) {
        if (name != null) {
            me.saveName(name);
        }
        return requestRegistration(phone, groupInviteToken);
    }

    @WorkerThread
    public @NonNull RegistrationVerificationResult verifyPhoneNumber(@NonNull String phone, @NonNull String code) {
        RegistrationVerificationResult verificationResult = Constants.USE_NOISE_FOR_REGISTRATION
                ? verifyRegistrationViaNoise(phone, code, me.getName())
                : verifyRegistrationNoise(phone, code, me.getName());

        if (verificationResult.result == RegistrationVerificationResult.RESULT_OK) {
            String uid = me.getUser();
            if (!Preconditions.checkNotNull(verificationResult.user).equals(uid)) {
                // New user, we should clear data
                contentDb.deleteDb();
            }
            me.saveRegistrationNoise(
                    Preconditions.checkNotNull(verificationResult.user),
                    Preconditions.checkNotNull(verificationResult.phone));
            preferences.setInitialRegistrationTime(System.currentTimeMillis());
            connection.connect();
        }
        return verificationResult;
    }

    @WorkerThread
    public RegistrationVerificationResult migrateRegistrationToNoise() {
        ThreadUtils.setSocketTag();

        InputStream inStream = null;
        HttpsURLConnection connection = null;

        final String uid = me.getUser();
        final String password = me.getPassword();
        try {
            final URL url = new URL("https://" + HOST + "/api/registration/update_key");
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", Constants.USER_AGENT);
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);
            connection.setConnectTimeout(30_000);
            connection.setReadTimeout(30_000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            JSONObject requestJson = new JSONObject();

            byte[] keypair = CryptoUtils.generateEd25519KeyPair();
            byte[] pub = Arrays.copyOfRange(keypair, 0, 32);
            byte[] priv = Arrays.copyOfRange(keypair, 32, 96);
            requestJson.put("s_ed_pub", Base64.encodeToString(pub, Base64.NO_WRAP));
            byte[] sign = CryptoUtils.sign("HALLO".getBytes(), new PrivateEdECKey(priv));
            requestJson.put("signed_phrase", Base64.encodeToString(sign, Base64.NO_WRAP));
            requestJson.put("uid", uid);
            requestJson.put("password", password);

            connection.getOutputStream().write(requestJson.toString().getBytes());

            final int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e("Registration.verifyRegistration responseCode:" + responseCode);
            }
            inStream = responseCode < 400 ? connection.getInputStream() : connection.getErrorStream();
            final JSONObject responseJson = new JSONObject(FileUtils.inputStreamToString(inStream));
            final String result = responseJson.optString("result");
            if (!"ok".equals(result)) {
                return new RegistrationVerificationResult(RegistrationVerificationResult.RESULT_FAILED_SERVER);
            }
            me.saveNoiseKey(keypair);
            return new RegistrationVerificationResult(uid, password, me.getPhone());
        } catch (IOException e) {
            Log.e("Registration.verifyRegistration", e);
            return new RegistrationVerificationResult(RegistrationVerificationResult.RESULT_FAILED_NETWORK);
        } catch (JSONException e) {
            Log.e("Registration.verifyRegistration", e);
            return new RegistrationVerificationResult(RegistrationVerificationResult.RESULT_FAILED_SERVER);
        } finally {
            FileUtils.closeSilently(inStream);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @WorkerThread
    private @NonNull RegistrationVerificationResult verifyRegistrationViaNoise(@NonNull String phone, @NonNull String code, @NonNull String name) {
        ThreadUtils.setSocketTag();
        encryptedKeyStore.generateClientPrivateKeys();
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
        verifyOtpRequestBuilder.setPhone(phone);
        verifyOtpRequestBuilder.setName(name);
        verifyOtpRequestBuilder.setCode(code);
        verifyOtpRequestBuilder.setIdentityKey(identityKeyProto.toByteString());
        verifyOtpRequestBuilder.setSignedKey(signedPreKeyProto.toByteString());
        verifyOtpRequestBuilder.addAllOneTimeKeys(oneTimePreKeys);
        verifyOtpRequestBuilder.setUserAgent(Constants.USER_AGENT);

        byte[] keypair = CryptoUtils.generateEd25519KeyPair();
        byte[] pub = Arrays.copyOfRange(keypair, 0, 32);
        byte[] priv = Arrays.copyOfRange(keypair, 32, 96);
        verifyOtpRequestBuilder.setStaticKey(ByteString.copyFrom(pub));
        byte[] sign = CryptoUtils.sign("HALLO".getBytes(), new PrivateEdECKey(priv));
        verifyOtpRequestBuilder.setSignedPhrase(ByteString.copyFrom(sign));
        HANoiseSocket noiseSocket = null;
        try {
            final InetAddress address = InetAddress.getByName(host);
            noiseSocket = new HANoiseSocket(me, address, NOISE_PORT);
            byte[] noiseKey = me.getMyRegEd25519NoiseKey();
            if (noiseKey == null) {
                noiseKey = CryptoUtils.generateEd25519KeyPair();
                me.saveNoiseRegKey(noiseKey);
            }
            noiseSocket.initialize(noiseKey, RegisterRequest.newBuilder().setVerifyRequest(verifyOtpRequestBuilder).build().toByteArray());

            RegisterResponse packet = RegisterResponse.parseFrom(noiseSocket.readPacket());
            final VerifyOtpResponse response = packet.getVerifyResponse();
            VerifyOtpResponse.Result result = response.getResult();

            final String uid = Long.toString(response.getUid());
            if (!VerifyOtpResponse.Result.SUCCESS.equals(result) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(uid)) {
                return new RegistrationVerificationResult(RegistrationVerificationResult.RESULT_FAILED_SERVER);
            }
            me.saveNoiseKey(keypair);
            return new RegistrationVerificationResult(uid, null, phone);
        } catch (IOException e) {
            Log.e("Registration.verifyRegistration", e);
            return new RegistrationVerificationResult(RegistrationVerificationResult.RESULT_FAILED_NETWORK);
        } catch (BadPaddingException | NoiseException | ShortBufferException e) {
            Log.e("Registration.verifyRegistration", e);
            return new RegistrationVerificationResult(RegistrationVerificationResult.RESULT_FAILED_SERVER);
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
    private @NonNull RegistrationVerificationResult verifyRegistrationNoise(@NonNull String phone, @NonNull String code, @NonNull String name) {
        ThreadUtils.setSocketTag();

        encryptedKeyStore.generateClientPrivateKeys();
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
        List<byte[]> oneTimePreKeys = new ArrayList<>();
        for (OneTimePreKey otpk : encryptedKeyStore.getNewBatchOfOneTimePreKeys()) {
            com.halloapp.proto.clients.OneTimePreKey toAdd = com.halloapp.proto.clients.OneTimePreKey.newBuilder()
                    .setId(otpk.id)
                    .setPublicKey(ByteString.copyFrom(otpk.publicXECKey.getKeyMaterial()))
                    .build();
            oneTimePreKeys.add(toAdd.toByteArray());
        }

        String identityKeyB64 = Base64.encodeToString(identityKeyProto.toByteArray(), Base64.NO_WRAP);
        String signedPreKeyB64 = Base64.encodeToString(signedPreKeyProto.toByteArray(), Base64.NO_WRAP);
        JSONArray jsonArray = new JSONArray();
        for (byte[] otpk : oneTimePreKeys) {
            jsonArray.put(Base64.encodeToString(otpk, Base64.NO_WRAP));
        }

        Log.i("Registration.verifyRegistration phone=" + phone + " code=" + code);
        InputStream inStream = null;
        HttpsURLConnection connection = null;
        try {
            final URL url = new URL("https://" + HOST + "/api/registration/register2");
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", Constants.USER_AGENT);
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);
            connection.setConnectTimeout(30_000);
            connection.setReadTimeout(30_000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            JSONObject requestJson = new JSONObject();
            requestJson.put("phone", phone);
            requestJson.put("code", code);
            requestJson.put("name", name);

            requestJson.put("identity_key", identityKeyB64);
            requestJson.put("signed_key", signedPreKeyB64);
            requestJson.put("one_time_keys", jsonArray);

            byte[] keypair = CryptoUtils.generateEd25519KeyPair();
            byte[] pub = Arrays.copyOfRange(keypair, 0, 32);
            byte[] priv = Arrays.copyOfRange(keypair, 32, 96);
            requestJson.put("s_ed_pub", Base64.encodeToString(pub, Base64.NO_WRAP));
            byte[] sign = CryptoUtils.sign("HALLO".getBytes(), new PrivateEdECKey(priv));
            requestJson.put("signed_phrase", Base64.encodeToString(sign, Base64.NO_WRAP));

            connection.getOutputStream().write(requestJson.toString().getBytes());

            final int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e("Registration.verifyRegistration responseCode:" + responseCode);
            }
            inStream = responseCode < 400 ? connection.getInputStream() : connection.getErrorStream();
            final JSONObject responseJson = new JSONObject(FileUtils.inputStreamToString(inStream));
            final String result = responseJson.optString("result");
            final String normalizedPhone = responseJson.optString("phone");
            final String uid = responseJson.optString("uid");
            final String error = responseJson.optString("error");
            if (!"ok".equals(result) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(uid)) {
                return new RegistrationVerificationResult(RegistrationVerificationResult.RESULT_FAILED_SERVER);
            }
            me.saveNoiseKey(keypair);
            return new RegistrationVerificationResult(uid, null, phone);
        } catch (IOException e) {
            Log.e("Registration.verifyRegistration", e);
            return new RegistrationVerificationResult(RegistrationVerificationResult.RESULT_FAILED_NETWORK);
        } catch (JSONException e) {
            Log.e("Registration.verifyRegistration", e);
            return new RegistrationVerificationResult(RegistrationVerificationResult.RESULT_FAILED_SERVER);
        } finally {
            FileUtils.closeSilently(inStream);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static class RegistrationRequestResult {

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({RESULT_OK, RESULT_FAILED_SERVER, RESULT_FAILED_NETWORK, RESULT_FAILED_SERVER_SMS_FAIL,
                RESULT_FAILED_SERVER_CANNOT_ENROLL, RESULT_FAILED_SERVER_NO_FRIENDS,
                RESULT_FAILED_SERVER_NOT_INVITED, RESULT_FAILED_CLIENT_EXPIRED,
                RESULT_FAILED_RETRIED_TOO_SOON, RESULT_FAILED_INVALID_PHONE_NUMBER})
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

        static @Result int translateServerErrorCode(String error) {
            if ("sms_fail".equals(error)) {
                return RESULT_FAILED_SERVER_SMS_FAIL;
            } else if ("cannot_enroll".equals(error)) {
                return RESULT_FAILED_SERVER_CANNOT_ENROLL;
            } else if ("no_friends".equals(error)) {
                return RESULT_FAILED_SERVER_NO_FRIENDS;
            } else if ("not_invited".equals(error)) {
                return RESULT_FAILED_SERVER_NOT_INVITED;
            } else if ("invalid_client_version".equals(error)) {
                return RESULT_FAILED_CLIENT_EXPIRED;
            } else if ("retried_too_soon".equals(error)) {
                return RESULT_FAILED_RETRIED_TOO_SOON;
            } else if ("invalid_phone_number".equals(error)) {
                return RESULT_FAILED_INVALID_PHONE_NUMBER;
            }
            return RESULT_FAILED_SERVER;
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

        RegistrationVerificationResult(@NonNull String user, @NonNull String password, @NonNull String phone) {
            this.user = user;
            this.password = password;
            this.phone = phone;
            this.result = RESULT_OK;
        }

        RegistrationVerificationResult(@Result int result) {
            Preconditions.checkState(result != RESULT_OK);
            this.user = null;
            this.password = null;
            this.phone = null;
            this.result = result;
        }
    }
}
