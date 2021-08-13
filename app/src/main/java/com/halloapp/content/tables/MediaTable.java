package com.halloapp.content.tables;

import android.provider.BaseColumns;

public final class MediaTable implements BaseColumns {

    private MediaTable() { }

    public static final String TABLE_NAME = "media";

    public static final String INDEX_MEDIA_KEY = "media_key";
    public static final String INDEX_DEC_HASH_KEY = "dec_hash_key";

    public static final String COLUMN_PARENT_TABLE = "parent_table";
    public static final String COLUMN_PARENT_ROW_ID = "parent_row_id";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_TRANSFERRED = "transferred";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_FILE = "file";
    public static final String COLUMN_ENC_FILE = "enc_file";
    public static final String COLUMN_PATCH_URL = "patch_url";
    public static final String COLUMN_ENC_KEY = "enckey";
    public static final String COLUMN_SHA256_HASH = "sha256hash";
    public static final String COLUMN_WIDTH = "width";
    public static final String COLUMN_HEIGHT = "height";
    public static final String COLUMN_UPLOAD_PROGRESS = "upload_progress";
    public static final String COLUMN_RETRY_COUNT = "retry_count";
    public static final String COLUMN_DEC_SHA256_HASH = "dec_sha256_hash";
    public static final String COLUMN_BLOB_VERSION = "blob_version";
    public static final String COLUMN_CHUNK_SIZE = "chunk_size";
    public static final String COLUMN_BLOB_SIZE = "blob_size";
}

