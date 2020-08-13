package com.halloapp;

import android.text.format.DateUtils;

public class Constants {

    public static final String USER_AGENT = "HalloApp/Android" + BuildConfig.VERSION_NAME + (BuildConfig.DEBUG ? "D" : "");
    public static final long POSTS_EXPIRATION = 31 * DateUtils.DAY_IN_MILLIS;
    public static final long RETRACT_COMMENT_ALLOWED_TIME = DateUtils.HOUR_IN_MILLIS;
    public static final long MINIMUM_PROGRESS_DIALOG_TIME_MILLIS = 300;
    public static final int MAX_TEXT_LENGTH = 32000;
    public static final int MAX_IMAGE_DIMENSION = 1600;
    public static final int MAX_POST_MEDIA_ITEMS = 10;
    public static final float MAX_IMAGE_ASPECT_RATIO = 1.25f;
    public static final long MAX_VIDEO_DURATION = 60 * 1000L;
    public static final long MAX_VIDEO_BITRATE = 8000000L;
    public static final int MEDIA_POST_LINE_LIMIT = 3;
    public static final int TEXT_POST_LINE_LIMIT = 12;
    public static final int POST_LINE_LIMIT_TOLERANCE = 3;
    public static final int MAX_AVATAR_DIMENSION = 250;
    public static final int MAX_NAME_LENGTH = 25;
    public static final int MAX_GROUP_NAME_LENGTH = 25;
    public static final int MAX_GROUP_SIZE = 25;

    public static final int JPEG_QUALITY = 80;
    public static final int VIDEO_BITRATE = 2000000;
    public static final int VIDEO_RESOLUTION_H264 = 360;
    public static final int VIDEO_RESOLUTION_H265 = 480;
    public static final int AUDIO_BITRATE = 96000;

    public static final boolean GROUPS_ENABLED = true;
    public static final boolean ENCRYPTION_TURNED_ON = true;
    public static final boolean REREQUEST_SEND_ENABLED = true;
    public static final boolean INVITES_ENABLED = true;
    public static final int MAX_REREQUESTS_PER_MESSAGE = 1;
}
