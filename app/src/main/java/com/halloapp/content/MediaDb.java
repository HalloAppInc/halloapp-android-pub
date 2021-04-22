package com.halloapp.content;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.FileStore;
import com.halloapp.content.tables.MediaTable;

public class MediaDb {
    private final ContentDbHelper databaseHelper;
    private final FileStore fileStore;

    public MediaDb(@NonNull ContentDbHelper databaseHelper, @NonNull FileStore fileStore) {
        this.databaseHelper = databaseHelper;
        this.fileStore = fileStore;
    }

    @WorkerThread
    @Nullable Media getLatestMediaWithHash(@NonNull byte[] decSha256hash) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final String rowQuerySql =
                "SELECT " + MediaTable._ID + " "
                        + "FROM " + MediaTable.TABLE_NAME + " "
                        + "WHERE " + MediaTable.COLUMN_DEC_SHA256_HASH + "=? "
                        + "ORDER BY " + MediaTable._ID + " DESC "
                        + "LIMIT " + 1;
        final long rowId;
        try (final SQLiteStatement statement = db.compileStatement(rowQuerySql)) {
            statement.bindBlob(1, decSha256hash);
            try {
                rowId = statement.simpleQueryForLong();
            } catch (SQLiteDoneException e) {
                return null;
            }
        }

        final String uploadQuerySql =
                "SELECT " +
                        MediaTable.COLUMN_TYPE + "," +
                        MediaTable.COLUMN_URL + "," +
                        MediaTable.COLUMN_FILE + "," +
                        MediaTable.COLUMN_ENC_KEY + "," +
                        MediaTable.COLUMN_SHA256_HASH + "," +
                        MediaTable.COLUMN_DEC_SHA256_HASH + "," +
                        MediaTable.COLUMN_WIDTH + "," +
                        MediaTable.COLUMN_HEIGHT + "," +
                        MediaTable.COLUMN_TRANSFERRED + " " +
                        "FROM " + MediaTable.TABLE_NAME + " " +
                        "WHERE " + MediaTable._ID + "=?";
        try (final Cursor cursor = db.rawQuery(uploadQuerySql, new String[]{Long.toString(rowId)})) {
            if (cursor.moveToFirst()) {
                return new Media(
                        rowId,
                        cursor.getInt(0),
                        cursor.getString(1),
                        fileStore.getMediaFile(cursor.getString(2)),
                        cursor.getBlob(3),
                        cursor.getBlob(4),
                        cursor.getBlob(5),
                        cursor.getInt(6),
                        cursor.getInt(7),
                        cursor.getInt(8));
            }
        }
        return null;
    }
}
