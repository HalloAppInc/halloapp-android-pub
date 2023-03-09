package com.halloapp.katchup;

import com.amplitude.android.Amplitude;
import com.amplitude.android.Configuration;
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
    private static Amplitude amplitude;

    private static final String API_KEY = BuildConfig.DEBUG ? "279d791071ab6d93eba1e53ebd7abc4a" : "33aef835b533bb5780ce8df9c35abda0";

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
        amplitude = new Amplitude(
                new Configuration(API_KEY, context)
        );
//        amplitude.getConfiguration().setServerUrl("https://amplitude.halloapp.net");
        // TODO(josh): remove when Amplitude stuff is done
        if (BuildConfig.DEBUG) {
            amplitude.getConfiguration().setFlushQueueSize(1);
        }

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
        if (uid == null) {
            return;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(uid.getBytes(StandardCharsets.UTF_8));
            String hex = Hex.bytesToStringLowercase(Arrays.copyOfRange(hash, 0, 16));
            amplitude.setUserId(hex);
        } catch (NoSuchAlgorithmException e) {
            Log.e("Failed to hash uid", e);
        }
    }

    public void setUserProperty(String prop, Object value) {
        if (prop.equals("notificationPermissionEnabled")) {
            notificationsEnabled = (boolean) value;
        }
        amplitude.identify(new Identify().set(prop, value));
    }

    private String getContentTypeString(MomentInfo.ContentType contentType) {
        if (MomentInfo.ContentType.IMAGE.equals(contentType)) {
            return "image";
        } else if (MomentInfo.ContentType.VIDEO.equals(contentType)) {
            return "video";
        } else if (MomentInfo.ContentType.TEXT.equals(contentType)) {
            return "text";
        } else {
            return "";
        }
    }

    // EVENTS

    public void logOnboardingStart() {
        amplitude.track("onboardingStart");
    }

    public void logOnboardingEnableContacts(boolean enabled) {
        Map<String, Boolean> properties = new HashMap<>();
        properties.put("success", enabled);
        amplitude.track("onboardingEnableContacts", properties);
    }

    public void logOnboardingEnableLocation(boolean enabled) {
        Map<String, Boolean> properties = new HashMap<>();
        properties.put("success", enabled);
        amplitude.track("onboardingEnableLocation", properties);
    }

    public void logOnboardingEnteredPhone() {
        amplitude.track("onboardingEnteredPhone");
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
        amplitude.track("onboardingEnteredOTP", properties);
    }

    public void logOnboardingResendOTP() {
        amplitude.track("onboardingResendOTP");
    }

    public void logOnboardingRequestOTPCall() {
        amplitude.track("onboardingRequestOTPCall");
    }

    public void logOnboardingEnteredName() {
        amplitude.track("onboardingEnteredName");
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
        amplitude.track("onboardingEnteredUsername", properties);
    }

    public void logOnboardingSetAvatar(boolean success) {
        Map<String, Boolean> properties = new HashMap<>();
        properties.put("success", success);
        amplitude.track("onboardingSetAvatar", properties);
    }

    public void onboardingFollowScreen(int numFollowed) {
        Map<String, Integer> properties = new HashMap<>();
        properties.put("num_followed", numFollowed);
        amplitude.track("onboardingFollowScreen", properties);
    }

    public void logOnboardingFinish() {
        amplitude.track("onboardingFinish");
    }

    public void logAppForegrounded() {
        amplitude.track("appForegrounded");
    }

    public void logAppBackgrounded() {
        amplitude.track("appBackgrounded");
    }

    public void openScreen(String screen) {
        if (!screen.equals(prevScreen)) {
            // This avoids an edge case where a non-onboarding screen is loaded
            // in the background before or during onboarding
            if (!screen.startsWith("onboarding") && !MainActivity.registrationIsDone) {
                return;
            }
            prevScreen = screen;
            Map<String, String> properties = new HashMap<>();
            properties.put("screen", screen);
            setUserProperty("lastScreen", screen);
            amplitude.track("openScreen", properties);
        }
    }

    public void posted(Media media, @SelfiePostComposerActivity.Type int composeType, long notificationId) {
        Map<String, Object> properties = new HashMap<>();
        String strType = "unknown";
        switch (media.type) {
            case Media.MEDIA_TYPE_IMAGE:
                strType = (composeType == SelfiePostComposerActivity.Type.LIVE_CAPTURE) ? "image" : "text";
                break;
            case Media.MEDIA_TYPE_VIDEO:
                strType = "video";
                break;
        }
        properties.put("type", strType);
        properties.put("moment_notif_id", notificationId);
        amplitude.track("posted", properties);
    }

    public void deletedPost(MomentInfo.ContentType contentType) {
        Map<String, String> properties = new HashMap<>();
        properties.put("type", getContentTypeString(contentType));
        amplitude.track("deletedPost", properties);
    }

    public void commented(Post parentPost, String type) {
        Map<String, Object> properties = new HashMap<>();
        KatchupPost kParentPost = (KatchupPost) parentPost;
        properties.put("post_type", getContentTypeString(kParentPost.contentType));
        properties.put("post_moment_notif_id", kParentPost.notificationId);
        properties.put("type", type);
        amplitude.track("commented", properties);
    }

    public void followed(boolean success) {
        Map<String, Boolean> properties = new HashMap<>();
        properties.put("success", success);
        amplitude.track("followed", properties);
    }

    public void unfollowed(boolean success) {
        Map<String, Boolean> properties = new HashMap<>();
        properties.put("success", success);
        amplitude.track("unfollowed", properties);
    }

    public void removedFollower(boolean success) {
        Map<String, Boolean> properties = new HashMap<>();
        properties.put("success", success);
        amplitude.track("removedFollower", properties);
    }

    public void blocked(boolean success) {
        Map<String, Boolean> properties = new HashMap<>();
        properties.put("success", success);
        amplitude.track("blocked", properties);
    }

    public void unblocked(boolean success) {
        Map<String, Boolean> properties = new HashMap<>();
        properties.put("success", success);
        amplitude.track("unblocked", properties);
    }

    public void notificationReceived(String type, boolean shownToUser) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("notificationType", type);
        properties.put("shownToUser", shownToUser && notificationsEnabled);
        amplitude.track("notificationReceived", properties);
    }

    public void notificationOpened(String type) {
        Map<String, String> properties = new HashMap<>();
        properties.put("notificationType", type);
        amplitude.track("notificationOpened", properties);
    }

    public void deletedAccount() {
        amplitude.track("deletedAccount");
        amplitude.flush();
        amplitude.reset();
        MainActivity.registrationIsDone = false;
    }

    public void tappedLockedPost() {
        amplitude.track("tappedLockedPost");
    }

    public void tappedPostButtonFromEmptyState() {
        amplitude.track("tappedPostButtonFromEmptyState");
    }

    public void tappedPostButtonFromFeaturedPosts() {
        amplitude.track("tappedPostButtonFromFeaturedPosts");
    }

    public void seenPost(String postId, MomentInfo.ContentType contentType, long notifId, String feed_type) {
        Map<String, Object> properties = new HashMap<>();
        // amplitude ignores subsequent events from the same device with the same insert_id value
        // https://www.docs.developers.amplitude.com/analytics/apis/http-v2-api/#event-deduplication
        properties.put("insert_id", postId);
        properties.put("feed_type", feed_type);
        properties.put("moment_notif_id", notifId);
        String strContentType;
        if (MomentInfo.ContentType.IMAGE.equals(contentType)) {
            strContentType = "image";
        } else if (MomentInfo.ContentType.VIDEO.equals(contentType)) {
            strContentType = "video";
        } else if (MomentInfo.ContentType.TEXT.equals(contentType)) {
            strContentType = "text";
        } else {
            strContentType = "";
        }
        properties.put("post_type", strContentType);
        amplitude.track("seenPost", properties);
    }
}
