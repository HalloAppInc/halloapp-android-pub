package com.halloapp.content.tables;

import android.provider.BaseColumns;

public final class UrlPreviewsTable implements BaseColumns {

    private UrlPreviewsTable() {
    }

    public static final String TABLE_NAME = "url_previews";

    public static final String INDEX_URL_PREVIEW_KEY = "url_previews_key";

    public static final String COLUMN_PARENT_TABLE = "url_previews_parent_table";
    public static final String COLUMN_PARENT_ROW_ID = "url_previews_parent_row_id";
    public static final String COLUMN_TITLE = "url_previews_title";
    public static final String COLUMN_URL = "url_previews_url";
}
