package com.halloapp.posts;

import androidx.annotation.IntDef;

import com.halloapp.util.RandomId;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Media {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MEDIA_TYPE_UNKNOWN, MEDIA_TYPE_IMAGE})
    public @interface MediaType {}
    public static final int MEDIA_TYPE_UNKNOWN = 0;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 1;

    public final String id;
    public final @MediaType int type;
    public String url;
    public String file;
    public int width;
    public int height;

    public boolean transferred;

    public static final Media createFromFile(@MediaType int type, String file) {
        return new Media(RandomId.create(), type, null, file, 0, 0, false);
    }

    public static final Media createFromUrl(@MediaType int type, String url, int width, int height) {
        return new Media(RandomId.create(), type, url, null, width, height, false);
    }

    public Media(String id, @MediaType int type, String url, String file, int width, int height, boolean transferred) {
        this.id = id;
        this.type = type;
        this.url = url;
        this.file = file;
        this.width = width;
        this.height = height;
        this.transferred = transferred;
    }


}
