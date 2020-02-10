package com.halloapp;

public class Constants {

    public static final String USER_AGENT = "HalloApp/Android" + BuildConfig.VERSION_NAME + (BuildConfig.DEBUG ? "D" : "");
    public static final int MAX_IMAGE_DIMENSION = 1600;
    public static final int JPEG_QUALITY = 80;
    public static final int MAX_POST_MEDIA_ITEMS = 10;
    public static final int MAX_TEXT_LENGTH = 32000;
    public static final float MAX_IMAGE_ASPECT_RATIO = 1.25f;
    public static final long POSTS_EXPIRATION = 30 * 24 * 60 * 60 * 1000L;
}
