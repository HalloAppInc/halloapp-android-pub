package com.halloapp.content.tables;

import android.provider.BaseColumns;

public class FutureProofTable implements BaseColumns {

    private FutureProofTable() {
    }

    public static final String TABLE_NAME = "future_messages";

    public static final String INDEX_FUTURE_PROOF_KEY = "future_proof_key";

    public static final String COLUMN_PARENT_TABLE = "parent_table";
    public static final String COLUMN_PARENT_ROW_ID = "parent_row_id";

    public static final String COLUMN_CONTENT_BYTES = "proto_blob";
}
