package com.halloapp.content;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.halloapp.UrlPreview;
import com.halloapp.content.tables.CallsTable;
import com.halloapp.content.tables.FutureProofTable;
import com.halloapp.content.tables.MessagesTable;
import com.halloapp.content.tables.UrlPreviewsTable;

public class CallsDb {

    private final ContentDbHelper databaseHelper;

    public CallsDb(@NonNull ContentDbHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void addCall(String id, Long duration) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        final ContentValues callValues = new ContentValues();
        callValues.put(CallsTable.COLUMN_CALL_ID, id);
        if (duration != null) {
            callValues.put(CallsTable.COLUMN_CALL_DURATION, duration);
        }
        db.insertWithOnConflict(CallsTable.TABLE_NAME, null, callValues, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void fillCallMessage(@NonNull CallMessage callMessage) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(CallsTable.TABLE_NAME,
                new String[]{
                        CallsTable._ID,
                        CallsTable.COLUMN_CALL_ID,
                        CallsTable.COLUMN_CALL_DURATION},
                CallsTable.COLUMN_CALL_ID + "=?",
                new String[] {
                        callMessage.id
                },
                null,
                null,
                null)) {
            if (cursor.moveToNext()) {
                callMessage.callDuration = cursor.getLong(2);
            }
        }
    }
}
