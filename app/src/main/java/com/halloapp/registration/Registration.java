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
import com.halloapp.content.ContentDb;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.OneTimePreKey;
import com.halloapp.crypto.keys.PrivateEdECKey;
import com.halloapp.crypto.keys.PublicXECKey;
import com.halloapp.proto.clients.IdentityKey;
import com.halloapp.proto.clients.SignedPreKey;
import com.halloapp.util.FileUtils;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class Registration {

    private static final String HOST = "api.halloapp.net";

    private static Registration instance;

    public static Registration getInstance() {
        if (instance == null) {
            synchronized(Registration.class) {
                if (instance == null) {
                    instance = new Registration(Me.getInstance(), ContentDb.getInstance(), Connection.getInstance(), EncryptedKeyStore.getInstance());
                }
            }
        }
        return instance;
    }

    private final Me me;
    private final ContentDb contentDb;
    private final Connection connection;
    private final EncryptedKeyStore encryptedKeyStore;

    private Registration(@NonNull Me me, @NonNull ContentDb contentDb, @NonNull Connection connection, @NonNull EncryptedKeyStore encryptedKeyStore) {
        this.me = me;
        this.contentDb = contentDb;
        this.connection = connection;
        this.encryptedKeyStore = encryptedKeyStore;
    }

    @WorkerThread
    public @NonNull RegistrationRequestResult requestRegistration(@NonNull String phone, @Nullable String groupInviteToken) {
        Log.i("Registration.requestRegistration phone=" + phone);
        ThreadUtils.setSocketTag();

        InputStream inStream = null;
        HttpsURLConnection connection = null;
        try {
            final URL url = new URL("https://" + HOST + "/api/registration/request_sms");
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
            Log.i("Registration.requestRegistration result=" + result + " error=" + error + " phone=" + normalizedPhone);
            if (!"ok".equals(result)) {
                return new RegistrationRequestResult(RegistrationRequestResult.translateServerErrorCode(error));
            }
            if (TextUtils.isEmpty(phone)) {
                return new RegistrationRequestResult(RegistrationRequestResult.RESULT_FAILED_SERVER);
            }
            return new RegistrationRequestResult(phone);
        } catch (IOException e) {
            Log.e("Registration.requestRegistration", e);
            return new RegistrationRequestResult(RegistrationRequestResult.RESULT_FAILED_NETWORK);
        } catch (JSONException e) {
            Log.e("Registration.requestRegistration", e);
            return new RegistrationRequestResult(RegistrationRequestResult.RESULT_FAILED_SERVER);
        } finally {
            FileUtils.closeSilently(inStream);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public @NonNull RegistrationRequestResult registerPhoneNumber(@Nullable String name, @NonNull String phone, @Nullable String groupInviteToken) {
        if (name != null) {
            me.saveName(name);
        }
        return requestRegistration(phone, groupInviteToken);
    }

    @WorkerThread
    public @NonNull RegistrationVerificationResult verifyPhoneNumber(@NonNull String phone, @NonNull String code) {
        RegistrationVerificationResult verificationResult = verifyRegistrationNoise(phone, code, me.getName());

        if (verificationResult.result == RegistrationVerificationResult.RESULT_OK) {
            String uid = me.getUser();
            if (!Preconditions.checkNotNull(verificationResult.user).equals(uid)) {
                // New user, we should clear data
                contentDb.deleteDb();
            }
            me.saveRegistrationNoise(
                    Preconditions.checkNotNull(verificationResult.user),
                    Preconditions.checkNotNull(verificationResult.phone));
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
        @IntDef({RESULT_OK, RESULT_FAILED_SERVER, RESULT_FAILED_NETWORK, RESULT_FAILED_SERVER_SMS_FAIL, RESULT_FAILED_SERVER_CANNOT_ENROLL, RESULT_FAILED_SERVER_NO_FRIENDS, RESULT_FAILED_SERVER_NOT_INVITED})
        @interface Result {}
        public static final int RESULT_OK = 0;
        public static final int RESULT_FAILED_NETWORK = 1;
        public static final int RESULT_FAILED_SERVER = 2;
        public static final int RESULT_FAILED_SERVER_SMS_FAIL = 3; // Sending the SMS failed
        public static final int RESULT_FAILED_SERVER_CANNOT_ENROLL = 4; // Error during the enroll function. This one does not make much sense.
        public static final int RESULT_FAILED_SERVER_NO_FRIENDS = 5; // The Phone number is not in any existing users contacts. We don't let users create accounts if they are not going to have any friends. Note this error is not returned for 555 phone numbers.
        public static final int RESULT_FAILED_SERVER_NOT_INVITED = 6; // Phone number trying to register has not been invited using the in-app invites system.

        public final String phone;
        public final @Result int result;

        RegistrationRequestResult(@NonNull String phone) {
            this.phone = phone;
            this.result = RESULT_OK;
        }

        RegistrationRequestResult(@Result int result) {
            Preconditions.checkState(result != RESULT_OK);
            this.phone = null;
            this.result = result;
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
