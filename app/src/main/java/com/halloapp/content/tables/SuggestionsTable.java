package com.halloapp.content.tables;

import android.provider.BaseColumns;

public class SuggestionsTable implements BaseColumns {

    private SuggestionsTable() { }

    public static final String TABLE_NAME = "suggestions";

    public static final String INDEX_SUGGESTION_KEY = "suggestions_key";

    public static final String COLUMN_SUGGESTION_ID = "suggestion_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_SIZE = "size";
    public static final String COLUMN_LOCATION_NAME = "location_name";
    public static final String COLUMN_LOCATION_ADDRESS = "location_address";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_IS_SCORED = "is_processed";
}
