package com.halloapp.posts;

import androidx.annotation.IntDef;

import com.halloapp.util.RandomId;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public class Media {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MEDIA_TYPE_UNKNOWN, MEDIA_TYPE_IMAGE, MEDIA_TYPE_VIDEO})
    public @interface MediaType {}
    public static final int MEDIA_TYPE_UNKNOWN = 0;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    public final long rowId;
    public final String id;
    public final @MediaType int type;
    public String url;
    public File file;
    public int width;
    public int height;
    public final byte [] encKey;
    public byte [] sha256hash;

    public boolean transferred;

    public static Media createFromFile(@MediaType int type, File file) {
        return new Media(0, RandomId.create(), type, null, file, null, null,0, 0, false);
    }

    public static Media createFromUrl(@MediaType int type, String url, byte [] encKey, byte [] sha256hash, int width, int height) {
        return new Media(0, RandomId.create(), type, url, null, encKey, sha256hash, width, height, false);
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

    public Media(long rowId, String id, @MediaType int type, String url, File file, byte[] encKey, byte [] sha256hash, int width, int height, boolean transferred) {
        this.rowId = rowId;
        this.id = id;
        this.type = type;
        this.url = url;
        this.file = file;
        this.encKey = encKey;
        this.sha256hash = sha256hash;
        this.width = width;
        this.height = height;
        this.transferred = transferred;
    }

    public void generateEncKey() {
        /*
        * TODO (ds): uncomment when other platforms implement media encryption
        *
        final SecureRandom random = new SecureRandom();
        encKey = new byte[32];
        random.nextBytes(encKey);
         */
    }

}
