package com.halloapp.katchup;

import com.amplitude.android.Amplitude;
import com.amplitude.android.Configuration;
import com.amplitude.core.events.EventOptions;
import com.amplitude.core.events.Identify;
import com.google.android.gms.common.util.Hex;
import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.MainActivity;
import com.halloapp.content.KatchupPost;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.server.MomentInfo;
import com.halloapp.proto.server.UsernameResponse;
import com.halloapp.proto.server.VerifyOtpResponse;
import com.halloapp.util.logs.Log;

import android.Manifest;
import android.app.Application;
import android.content.Context;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import pub.devrel.easypermissions.EasyPermissions;

public class Analytics {

    final static public String CONTACT_NOTICE_NOTIFICATION = "contact_notice";
    final static public String DAILY_MOMENT_NOTIFICATION = "daily_moment";
    final static public String FEEDPOST_NOTIFICATION = "feedpost";
    final static public String FOLLOWER_NOTICE_NOTIFICATION = "follower_notice";
    final static public String SCREENSHOT_NOTIFICATION = "screenshot";

    private static Analytics instance;
    private Amplitude debugInstance;
    private Amplitude hashedUidsInstance;
    private Amplitude realUidsInstance;
    private Amplitude[] amplitudeInstances;

    private String prevScreen = "";
    private boolean notificationsEnabled;

    public static Analytics getInstance() {
        if (instance == null) {
            synchronized (Analytics.class) {
                if (instance == null) {
                    instance = new Analytics();
                }
            }
        }
        return instance;
    }

    private Analytics() {}

    public void init(Application application) {
        Context context = application.getApplicationContext();
        if (BuildConfig.DEBUG) {
            debugInstance = new Amplitude(new Configuration("279d791071ab6d93eba1e53ebd7abc4a", context));
            amplitudeInstances = new Amplitude[]{debugInstance};
            // TODO(josh): remove when Amplitude stuff is done
            debugInstance.getConfiguration().setFlushQueueSize(1);

        } else {
            hashedUidsInstance = new Amplitude(new Configuration("33aef835b533bb5780ce8df9c35abda0", context));
            realUidsInstance = new Amplitude(new Configuration("f9e407ab7aac116cc9fafc78fcdb3d54", context));
            amplitudeInstances = new Amplitude[]{hashedUidsInstance, realUidsInstance};
        }
//        amplitude.getConfiguration().setServerUrl("https://amplitude.halloapp.net");

        initUserProperties(context);
    }

    // TODO(josh): this is public so we can update these after registration
    // we should instead update props directly from a katchup version of the Prop class
    public void initUserProperties(Context context) {
        setUserProperty("clientVersion", Constants.USER_AGENT);
        setUserProperty("contactsPermissionEnabled", EasyPermissions.hasPermissions(context, Manifest.permission.READ_CONTACTS));
        setUserProperty("locationPermissionEnabled", EasyPermissions.hasPermissions(context, Manifest.permission.ACCESS_FINE_LOCATION) || EasyPermissions.hasPermissions(context, Manifest.permission.ACCESS_COARSE_LOCATION));
        setUserProperty("notificationPermissionEnabled", NotificationManagerCompat.from(context).areNotificationsEnabled());

        Map<String, ?> props = ServerProps.getInstance().getProps();
        for (String key : props.keySet()) {
            if (!key.startsWith("props:")) {
                setUserProperty("serverProperties.".concat(key), props.get(key));
            }
        }
    }

    public void setUid(String uid) {
        if (uid == null) return;

        if (debugInstance != null) {
            debugInstance.setUserId(uid);
        }

        if (realUidsInstance != null) {
            realUidsInstance.setUserId(uid);
        }

        if (hashedUidsInstance != null) {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                byte[] hash = messageDigest.digest(uid.getBytes(StandardCharsets.UTF_8));
                String hex = Hex.bytesToStringLowercase(Arrays.copyOfRange(hash, 0, 16));
                hashedUidsInstance.setUserId(hex);
            } catch (NoSuchAlgorithmException e) {
                Log.e("Failed to hash uid", e);
            }
        }
    }

    public void setUserProperty(String prop, Object value) {
        if (prop.equals("notificationPermissionEnabled")) {
            notificationsEnabled = (boolean) value;
        }
        for (Amplitude amplitudeInstance : amplitudeInstances) {
            amplitudeInstance.identify(new Identify().set(prop, value));
        }
    }

    private void track(String event) {
        for (Amplitude amplitudeInstance : amplitudeInstances) {
            amplitudeInstance.track(event);
        }
    }

    private void track(String event, Map<String, Object> properties) {
        for (Amplitude amplitudeInstance : amplitudeInstances) {
            amplitudeInstance.track(event, properties);
        }
    }

    private void track(String event, Map<String, Object> properties, EventOptions eventOptions) {
        for (Amplitude amplitudeInstance : amplitudeInstances) {
            amplitudeInstance.track(event, properties, eventOptions);
        }
    }

    private String getContentTypeString(MomentInfo.ContentType contentType) {
        if (MomentInfo.ContentType.IMAGE.equals(contentType)) {
            return "image";
        } else if (MomentInfo.ContentType.VIDEO.equals(contentType)) {
            return "video";
        } else if (MomentInfo.ContentType.TEXT.equals(contentType)) {
            return "text";
        } else if (MomentInfo.ContentType.ALBUM_IMAGE.equals(contentType)) {
            return "album_image";
        } else {
            return "";
        }
    }

    // EVENTS

    public void logOnboardingStart() {
        track("onboardingStart");
    }

    public void logOnboardingEnableContacts(boolean enabled) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("success", enabled);
        track("onboardingEnableContacts", properties);
    }

    public void logOnboardingEnableLocation(boolean enabled) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("success", enabled);
        track("onboardingEnableLocation", properties);
    }

    public void logOnboardingEnteredPhone() {
        track("onboardingEnteredPhone");
    }

    public void logOnboardingEnteredOtp(boolean success, VerifyOtpResponse.Reason reason) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("success", success);
        if (reason != null) {
            String strReason;
            if (VerifyOtpResponse.Reason.UNKNOWN_REASON.equals(reason)) {
                strReason = "unknown_reason";
            } else if (VerifyOtpResponse.Reason.INVALID_PHONE_NUMBER.equals(reason)) {
                strReason = "invalid_phone_number";
            } else if (VerifyOtpResponse.Reason.INVALID_CLIENT_VERSION.equals(reason)) {
                strReason = "invalid_client_version";
            } else if (VerifyOtpResponse.Reason.WRONG_SMS_CODE.equals(reason)) {
                strReason = "wrong_sms_code";
            } else if (VerifyOtpResponse.Reason.MISSING_PHONE.equals(reason)) {
                strReason = "missing_phone";
            } else if (VerifyOtpResponse.Reason.MISSING_CODE.equals(reason)) {
                strReason = "missing_code";
            } else if (VerifyOtpResponse.Reason.MISSING_NAME.equals(reason)) {
                strReason = "missing_name";
            } else if (VerifyOtpResponse.Reason.INVALID_NAME.equals(reason)) {
                strReason = "invalid_name";
            } else if (VerifyOtpResponse.Reason.MISSING_IDENTITY_KEY.equals(reason)) {
                strReason = "missing_identity_key";
            } else if (VerifyOtpResponse.Reason.MISSING_SIGNED_KEY.equals(reason)) {
                strReason = "missing_signed_key";
            } else if (VerifyOtpResponse.Reason.MISSING_ONE_TIME_KEYS.equals(reason)) {
                strReason = "missing_one_time_keys";
            } else if (VerifyOtpResponse.Reason.BAD_BASE64_KEY.equals(reason)) {
                strReason = "bad_base64_key";
            } else if (VerifyOtpResponse.Reason.INVALID_ONE_TIME_KEYS.equals(reason)) {
                strReason = "invalid_one_time_keys";
            } else if (VerifyOtpResponse.Reason.TOO_FEW_ONE_TIME_KEYS.equals(reason)) {
                strReason = "too_few_one_time_keys";
            } else if (VerifyOtpResponse.Reason.TOO_MANY_ONE_TIME_KEYS.equals(reason)) {
                strReason = "too_many_one_time_keys";
            } else if (VerifyOtpResponse.Reason.TOO_BIG_IDENTITY_KEY.equals(reason)) {
                strReason = "too_big_identity_key";
            } else if (VerifyOtpResponse.Reason.TOO_BIG_SIGNED_KEY.equals(reason)) {
                strReason = "too_big_signed_key";
            } else if (VerifyOtpResponse.Reason.TOO_BIG_ONE_TIME_KEYS.equals(reason)) {
                strReason = "too_big_one_time_keys";
            } else if (VerifyOtpResponse.Reason.INVALID_S_ED_PUB.equals(reason)) {
                strReason = "invalid_s_ed_pub";
            } else if (VerifyOtpResponse.Reason.INVALID_SIGNED_PHRASE.equals(reason)) {
                strReason = "invalid_signed_phrase";
            } else if (VerifyOtpResponse.Reason.UNABLE_TO_OPEN_SIGNED_PHRASE.equals(reason)) {
                strReason = "unable_to_open_signed_phrase";
            } else if (VerifyOtpResponse.Reason.BAD_REQUEST.equals(reason)) {
                strReason = "bad_request";
            } else if (VerifyOtpResponse.Reason.INTERNAL_SERVER_ERROR.equals(reason)) {
                strReason = "internal_server_error";
            } else if (VerifyOtpResponse.Reason.INVALID_COUNTRY_CODE.equals(reason)) {
                strReason = "invalid_country_code";
            } else if (VerifyOtpResponse.Reason.INVALID_LENGTH.equals(reason)) {
                strReason = "invalid_length";
            } else if (VerifyOtpResponse.Reason.LINE_TYPE_VOIP.equals(reason)) {
                strReason = "line_type_voip";
            } else if (VerifyOtpResponse.Reason.LINE_TYPE_FIXED.equals(reason)) {
                strReason = "line_type_fixed";
            } else if (VerifyOtpResponse.Reason.LINE_TYPE_OTHER.equals(reason)) {
                strReason = "line_type_other";
            } else if (VerifyOtpResponse.Reason.UNRECOGNIZED.equals(reason)) {
                strReason = "unrecognized";
            } else {
                strReason = "";
            }
            properties.put("reason", strReason);
        }
        track("onboardingEnteredOTP", properties);
    }

    public void logOnboardingResendOTP() {
        track("onboardingResendOTP");
    }

    public void logOnboardingRequestOTPCall() {
        track("onboardingRequestOTPCall");
    }

    public void logOnboardingEnteredName() {
        track("onboardingEnteredName");
    }

    public void logOnboardingEnteredUsername(boolean success, int intReason) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("success", success);
        UsernameResponse.Reason reason = UsernameResponse.Reason.forNumber(intReason);
        if (reason != null) {
            String strReason;
            if (UsernameResponse.Reason.TOOSHORT.equals(reason)) {
                strReason = "tooshort";
            } else if (UsernameResponse.Reason.TOOLONG.equals(reason)) {
                strReason = "toolong";
            } else if (UsernameResponse.Reason.BADEXPR.equals(reason)) {
                strReason = "badexpr";
            } else if (UsernameResponse.Reason.NOTUNIQ.equals(reason)) {
                strReason = "notuniq";
            } else if (UsernameResponse.Reason.UNRECOGNIZED.equals(reason)) {
                strReason = "unrecognized";
            } else {
                strReason = "";
            }
            properties.put("reason", strReason);
        }
        track("onboardingEnteredUsername", properties);
    }

    public void logOnboardingSetAvatar(boolean success) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("success", success);
        track("onboardingSetAvatar", properties);
    }

    public void onboardingFollowScreen(int numFollowed) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("num_followed", numFollowed);
        track("onboardingFollowScreen", properties);
    }

    public void logOnboardingFinish() {
        track("onboardingFinish");
    }

    public void logAppForegrounded() {
        track("appForegrounded");
    }

    public void logAppBackgrounded() {
        track("appBackgrounded");
    }

    public void openScreen(String screen) {
        if (!screen.equals(prevScreen)) {
            // This avoids an edge case where a non-onboarding screen is loaded
            // in the background before or during onboarding
            if (!screen.startsWith("onboarding") && !MainActivity.registrationIsDone) {
                return;
            }
            prevScreen = screen;
            Map<String, Object> properties = new HashMap<>();
            properties.put("screen", screen);
            setUserProperty("lastScreen", screen);
            track("openScreen", properties);
        }
    }

    public void posted(Media media, MomentInfo.ContentType contentType, long notificationId, String prompt) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("type", getContentTypeString(contentType));
        properties.put("moment_notif_id", notificationId);
        properties.put("prompt", prompt);
        track("posted", properties);
    }

    public void deletedPost(MomentInfo.ContentType contentType, long notificationId) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("type", getContentTypeString(contentType));
        properties.put("moment_notif_id", notificationId);
        track("deletedPost", properties);
    }

    public void commented(Post parentPost, String type) {
        Map<String, Object> properties = new HashMap<>();
        KatchupPost kParentPost = (KatchupPost) parentPost;
        properties.put("post_type", getContentTypeString(kParentPost.contentType));
        properties.put("post_moment_notif_id", kParentPost.notificationId);
        properties.put("type", type);
        track("commented", properties);
    }

    public void followed(boolean success) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("success", success);
        track("followed", properties);
    }

    public void unfollowed(boolean success) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("success", success);
        track("unfollowed", properties);
    }

    public void removedFollower(boolean success) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("success", success);
        track("removedFollower", properties);
    }

    public void blocked(boolean success) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("success", success);
        track("blocked", properties);
    }

    public void unblocked(boolean success) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("success", success);
        track("unblocked", properties);
    }

    public void notificationReceived(String type, boolean shownToUser) {
        notificationReceived(type, shownToUser, null, null, null);
    }

    public void notificationReceived(String type, boolean shownToUser, @Nullable Long momentNotificationId, @Nullable String prompt, @Nullable String notificationMessage) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("notificationType", type);
        properties.put("shownToUser", shownToUser && notificationsEnabled);
        if (momentNotificationId != null) {
            properties.put("moment_notif_id", momentNotificationId);
        }
        if (prompt != null) {
            properties.put("prompt", prompt);
        }
        if (notificationMessage != null) {
            properties.put("moment_notif_msg", notificationMessage);
        }
        track("notificationReceived", properties);
    }

    public void notificationOpened(String type) {
        notificationOpened(type, null, null, null);
    }

    public void notificationOpened(String type, @Nullable Long momentNotificationId, @Nullable String prompt, @Nullable String notificationMessage) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("notificationType", type);
        if (momentNotificationId != null) {
            properties.put("moment_notif_id", momentNotificationId);
        }
        if (prompt != null) {
            properties.put("prompt", prompt);
        }
        if (notificationMessage != null) {
            properties.put("moment_notif_msg", notificationMessage);
        }
        track("notificationOpened", properties);
    }

    public void deletedAccount() {
        track("deletedAccount");
        for (Amplitude amplitudeInstance : amplitudeInstances) {
            amplitudeInstance.flush();
            amplitudeInstance.reset();
        }
        MainActivity.registrationIsDone = false;
    }

    public void tappedLockedPost() {
        track("tappedLockedPost");
    }

    public void tappedPostButtonFromEmptyState() {
        track("tappedPostButtonFromEmptyState");
    }

    public void tappedPostButtonFromFeaturedPosts() {
        track("tappedPostButtonFromFeaturedPosts");
    }

    public void seenPost(String postId, MomentInfo.ContentType contentType, long notifId, String feed_type) {
        // amplitude ignores subsequent events from the same device with the same insert_id value
        // https://www.docs.developers.amplitude.com/analytics/apis/http-v2-api/#event-deduplication
        // everything in the `properties` var gets stored in `event_properties` from above link
        // other properties must be sent using EventOptions API
        EventOptions eventOpts = new EventOptions();
        eventOpts.setInsertId(postId);
        Map<String, Object> properties = new HashMap<>();
        properties.put("feed_type", feed_type);
        properties.put("moment_notif_id", notifId);
        properties.put("post_type", getContentTypeString(contentType));
        track("seenPost", properties, eventOpts);
    }
}
