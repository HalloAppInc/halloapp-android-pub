package com.halloapp.content;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;

public class Media {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MEDIA_TYPE_UNKNOWN, MEDIA_TYPE_IMAGE, MEDIA_TYPE_VIDEO})
    public @interface MediaType {}
    public static final int MEDIA_TYPE_UNKNOWN = 0;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TRANSFERRED_UNKNOWN, TRANSFERRED_NO, TRANSFERRED_YES, TRANSFERRED_FAILURE, TRANSFERRED_RESUME})
    public @interface TransferredState {}
    public static final int TRANSFERRED_UNKNOWN = -1;
    public static final int TRANSFERRED_NO = 0;
    public static final int TRANSFERRED_YES = 1;
    public static final int TRANSFERRED_FAILURE = 2;
    public static final int TRANSFERRED_RESUME = 3;

    public long rowId;
    public final @MediaType int type;
    public String url;
    public File file;
    public File encFile;
    public int width;
    public int height;
    public byte [] encKey;
    public byte [] sha256hash;
    private boolean initialState = false;

    public @TransferredState int transferred;

    public static Media createFromFile(@MediaType int type, File file) {
        return new Media(0, type, null, file, generateEncKey(), null,0, 0, TRANSFERRED_NO);
    }

    public static Media createFromUrl(@MediaType int type, String url, byte [] encKey, byte [] sha256hash, int width, int height) {
        return new Media(0, type, url, null, encKey, sha256hash, width, height, TRANSFERRED_NO);
    }

    public static float getMaxAspectRatio(List<Media> media) {
        float maxAspectRatio = 0;
        for (Media mediaItem : media) {
            if (mediaItem.width != 0) {
                float ratio = 1f * mediaItem.height / mediaItem.width;
                if (ratio > maxAspectRatio) {
                    maxAspectRatio = ratio;
                }
            }
        }
        return maxAspectRatio;
    }

    public static @MediaType int getMediaType(String mime) {
        if (mime == null) {
            return MEDIA_TYPE_UNKNOWN;
        } if (mime.startsWith("image/")) {
            return MEDIA_TYPE_IMAGE;
        } else if (mime.startsWith("video/")) {
            return MEDIA_TYPE_VIDEO;
        } else {
            return MEDIA_TYPE_UNKNOWN;
        }
    }

    public static String getFileExt(@MediaType int type) {
        switch (type) {
            case MEDIA_TYPE_IMAGE: {
                return "jpg";
            }
            case MEDIA_TYPE_VIDEO: {
                return "mp4";
            }
            case MEDIA_TYPE_UNKNOWN:
            default: {
                return "";
            }
        }
    }

    public Media(long rowId, @MediaType int type, String url, File file, byte[] encKey, byte [] sha256hash, int width, int height, @TransferredState int transferred) {
        this.rowId = rowId;
        this.type = type;
        this.url = url;
        this.file = file;
        this.encKey = encKey;
        this.sha256hash = sha256hash;
        this.width = width;
        this.height = height;
        this.transferred = transferred;
    }

    private static byte [] generateEncKey() {
        final SecureRandom random = new SecureRandom();
        final byte [] encKey = new byte[32];
        random.nextBytes(encKey);
        return encKey;
    }

    public boolean isInInitialState() {
        return initialState;
    }

    public void initializeRetry(){
        initialState = true;
        transferred = TRANSFERRED_RESUME;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        final Media media = (Media) o;
        return rowId == media.rowId &&
                type == media.type &&
                Objects.equals(file, media.file) &&
                transferred == media.transferred;
    }

    @NonNull
    @Override
    public String toString() {
        return "Media {rowId:" + rowId + ", type:" + type + ", initialState:" + initialState + ", transferred:" + transferred + '}';
    }
}
