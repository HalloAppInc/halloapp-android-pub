package com.halloapp;

import android.text.format.DateUtils;

public class Constants {

    public static final String FULL_VERSION = BuildConfig.VERSION_NAME + (BuildConfig.DEBUG ? "D" : "");
    public static final String USER_AGENT = (BuildConfig.IS_KATCHUP ? "Katchup" : "HalloApp") + "/Android" + FULL_VERSION;
    public static final String URL_PREVIEW_USER_AGENT = "WhatsApp/2";

    public static final String PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID;

    public static final String[] WEBSITE_TRANSLATIONS = {"ar", "de", "es", "nl"};
    public static final String WEBSITE_BASE_URL = "https://www.halloapp.com/";
    public static final String FAQ_SUFFIX = "help";
    public static final String PRIVACY_POLICY_SUFFIX = "privacy";
    public static final String TERMS_OF_SERVICE_SUFFIX = "terms";
    public static final String ENCRYPTED_CHAT_BLOG_SUFFIX = "blog/encrypted-chat";
    public static final String WAITING_ON_MESSAGE_FAQ_SUFFIX = "help/#waiting-for-this-message";
    public static final String CONTACT_PERMISSIONS_LEARN_MORE_SUFFIX = "blog/why-address-book";
    public static final String DOWNLOAD_LINK_URL = "https://halloapp.com/dl";
    public static final String GROUP_INVITE_BASE_URL = "https://halloapp.com/invite/?g=";

    public static final String KATCHUP_WEBSITE_BASE_URL = "https://katchup.com/w/";
    public static final String KATCHUP_PRIVACY_NOTICE_LINK = "https://katchup.com/w/privacy/";
    public static final String KATCHUP_TERMS_LINK = "https://katchup.com/w/terms/";

    public static final String HUAWEI_APP_ID = "106829255";

    public static final String SUPPORT_EMAIL_LOCAL_PART = "android-support";
    public static final String SUPPORT_EMAIL_DOMAIN = "halloapp.com";
    public static final String SUPPORT_EMAIL = SUPPORT_EMAIL_LOCAL_PART + "@" + SUPPORT_EMAIL_DOMAIN;

    public static final String PACKAGE_INSTAGRAM = "com.instagram.android";
    public static final String PACKAGE_WHATSAPP = "com.whatsapp";
    public static final String PACKAGE_SNAPCHAT = "com.snapchat.android";

    public static final long POSTS_EXPIRATION = 31 * DateUtils.DAY_IN_MILLIS;
    public static final long DEFAULT_GROUP_EXPIRATION_TIME = 86400 * 30;
    public static final long MOMENT_EXPIRATION = DateUtils.DAY_IN_MILLIS;
    public static final long NEVER_EXPIRE_BUG_WORKAROUND_TIMESTAMP = 1977609600000L;
    public static final long SHARE_OLD_POST_LIMIT = 7 * DateUtils.DAY_IN_MILLIS;
    public static final long RETRACT_COMMENT_ALLOWED_TIME = DateUtils.HOUR_IN_MILLIS;
    public static final long MINIMUM_PROGRESS_DIALOG_TIME_MILLIS = 300;
    public static final long PUSH_TOKEN_RESYNC_TIME = DateUtils.DAY_IN_MILLIS;
    public static final int MAX_TEXT_LENGTH = 32000;
    public static final int MAX_IMAGE_DIMENSION = 1600;
    public static final float MAX_IMAGE_ASPECT_RATIO = 1.25f;
    public static final int MEDIA_POST_LINE_LIMIT = 3;
    public static final int TEXT_POST_LINE_LIMIT = 12;
    public static final int POST_LINE_LIMIT_TOLERANCE = 3;
    public static final int MAX_AVATAR_DIMENSION = 250;
    public static final int MAX_LARGE_AVATAR_DIMENSION = 1000;
    public static final int MAX_EXTERNAL_SHARE_THUMB_DIMENSION = 800;
    public static final int MAX_NAME_LENGTH = 25;
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 25;
    public static final int MAX_GROUP_NAME_LENGTH = 25;
    public static final int MAX_GROUP_DESCRIPTION_LENGTH = 500;
    public static final int SEND_LOGS_BUTTON_DELAY_MS = 15000;
    public static final int MINIMUM_AUDIO_NOTE_DURATION_MS = 1000;
    public static final int CALL_RINGING_TIMEOUT_MS = 60000;
    public static final int CALL_ICE_RESTART_TIMEOUT_MS = 3000;
    public static final int CALL_NO_CONNECTION_TIMEOUT_MS = 30_000;
    public static final boolean REACTIONS_ENABLED = false;

    public static final int EXTERNAL_SHARE_VIDEO_WIDTH = 720;
    public static final int EXTERNAL_SHARE_VIDEO_HEIGHT = 1280;
    public static final int EXTERNAL_SHARE_MAX_VIDEO_DURATION_MS = 20_000;
    public static final int EXTERNAL_SHARE_IMAGE_VIDEO_DURATION_MS = 10_000;

    public static final int EXTERNAL_SHARE_FOOTER_COLOR = 0xFFFED3D3;
    public static final float EXTERNAL_SHARE_FOOTER_TEXT_SIZE = 24f;

    public static final float EXTERNAL_SHARE_SELFIE_POS_X = 0.95f;
    public static final float EXTERNAL_SHARE_SELFIE_POS_Y = 0.03f;

    public static final int BUILD_EXPIRES_SOON_THRESHOLD_DAYS = 7;
    public static final int SECONDS_PER_DAY = 60 * 60 * 24;

    public static final int KEYBOARD_SHOW_DELAY = 300;
    public static final long COMMENTS_SCROLL_DELAY = 400L;

    public static final int JPEG_QUALITY = 80;
    public static int VIDEO_BITRATE_OVERRIDE = 0;
    public static int VIDEO_RESOLUTION_H264 = 360;
    public static int AUDIO_BITRATE = 96000;
    public static final long DOCUMENT_SIZE_LIMIT = 128L * 1024L * 1024L * 1024L; // 128 mb

    public static final int MAX_REREQUESTS_PER_MESSAGE = 5;

    public static final int DEFAULT_STREAMING_UPLOAD_CHUNK_SIZE = 64 * 1024;
    public static final int DEFAULT_STREAMING_INITIAL_DOWNLOAD_SIZE = 5 * 1024 * 1024;

    public static final String[] BANNED_INVITE_SUGGEST_TOKENS = new String[] {
            "hospital",
            "spam",
            "taxi"
    };

    public static final String CRYPTO_SUCCESS_EMOJI = "\u2714\uFE0E";
    public static final String CRYPTO_FAILURE_EMOJI = "\uD83D\uDCA5";

    public static final float PROFILE_PHOTO_OVAL_HEIGHT_RATIO = 0.75f; // assume this is always < 1
    public static final int PROFILE_PHOTO_OVAL_DEG = -12;

    // It appears some devices have not implemented the character classes like \p{IsAlphabetic} as
    // specified in the Android pattern docs here: https://developer.android.com/reference/java/util/regex/Pattern
    // This may be because not all of the character classes that are specified are listed in a table,
    // so it would be easy to miss some.
    public static final String USERNAME_CHARACTERS_REGEX = "^[a-zA-Z0-9_.]+$";
}
