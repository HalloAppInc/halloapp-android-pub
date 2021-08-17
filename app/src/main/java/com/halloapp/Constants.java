package com.halloapp;

import android.text.format.DateUtils;

public class Constants {

    public static final String USER_AGENT = "HalloApp/Android" + BuildConfig.VERSION_NAME + (BuildConfig.DEBUG ? "D" : "");

    public static final String MAIN_WEBSITE_URL = "https://www.halloapp.com/";
    public static final String ABOUT_PAGE_URL = "https://www.halloapp.com/about";
    public static final String GERMAN_ABOUT_URL = "https://www.halloapp.com/de/about";
    public static final String FAQ_URL = "https://www.halloapp.com/help/";
    public static final String PRIVACY_POLICY_URL = "https://www.halloapp.com/privacy";
    public static final String TERMS_OF_SERVICE_URL = "https://www.halloapp.com/terms";
    public static final String GROUP_INVITE_BASE_URL = "https://halloapp.com/invite/?g=";
    public static final String ENCRYPTED_CHAT_BLOG_URL = "https://halloapp.com/blog/encrypted-chat";

    public static final String SUPPORT_EMAIL = "android-support@halloapp.com";
    public static final String SUPPORT_EMAIL_LOCAL_PART = "android-support";
    public static final String SUPPORT_EMAIL_DOMAIN = "halloapp.com";

    public static final long POSTS_EXPIRATION = 31 * DateUtils.DAY_IN_MILLIS;
    public static final long SHARE_OLD_POST_LIMIT = 7 * DateUtils.DAY_IN_MILLIS;
    public static final long RETRACT_COMMENT_ALLOWED_TIME = DateUtils.HOUR_IN_MILLIS;
    public static final long MINIMUM_PROGRESS_DIALOG_TIME_MILLIS = 300;
    public static final long PUSH_TOKEN_RESYNC_TIME = DateUtils.DAY_IN_MILLIS;
    public static final int MAX_TEXT_LENGTH = 32000;
    public static final int MAX_IMAGE_DIMENSION = 1600;
    public static final int MAX_POST_MEDIA_ITEMS = 10;
    public static final float MAX_IMAGE_ASPECT_RATIO = 1.25f;
    public static final int MEDIA_POST_LINE_LIMIT = 3;
    public static final int TEXT_POST_LINE_LIMIT = 12;
    public static final int POST_LINE_LIMIT_TOLERANCE = 3;
    public static final int MAX_AVATAR_DIMENSION = 250;
    public static final int MAX_NAME_LENGTH = 25;
    public static final int MAX_GROUP_NAME_LENGTH = 25;
    public static final int MAX_GROUP_DESCRIPTION_LENGTH = 500;
    public static final int SEND_LOGS_BUTTON_DELAY_MS = 15000;
    public static final int MINIMUM_AUDIO_NOTE_DURATION_MS = 1000;

    public static final int BUILD_EXPIRES_SOON_THRESHOLD_DAYS = 7;
    public static final int SECONDS_PER_DAY = 60 * 60 * 24;

    public static final int KEYBOARD_SHOW_DELAY = 300;

    public static final int JPEG_QUALITY = 80;
    public static int VIDEO_BITRATE = 2000000;
    public static int VIDEO_RESOLUTION_H264 = 360;
    public static int VIDEO_RESOLUTION_H265 = 480;
    public static int AUDIO_BITRATE = 96000;

    public static final boolean REREQUEST_SEND_ENABLED = true;
    public static final int MAX_REREQUESTS_PER_MESSAGE = 5;

    public static final boolean SEND_CONTAINER = BuildConfig.DEBUG;

    public static final boolean VOICE_NOTE_SENDING_ENABLED = BuildConfig.DEBUG;
}
