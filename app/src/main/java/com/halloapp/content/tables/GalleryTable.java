package com.halloapp.content.tables;

import android.provider.BaseColumns;

public class GalleryTable implements BaseColumns {

    private GalleryTable() { }

    public static final String TABLE_NAME = "gallery";

    public static final String INDEX_GALLERY_KEY = "gallery_key";

    public static final String COLUMN_GALLERY_ITEM_URI = "gallery_item_uri";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_TIME_TAKEN = "time_taken";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_SUGGESTION_ID = "suggestion_id";
    public static final String COLUMN_IS_SUGGESTED = "is_suggested";
}
