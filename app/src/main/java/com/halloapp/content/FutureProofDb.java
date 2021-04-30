package com.halloapp.content;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.halloapp.content.tables.CommentsTable;
import com.halloapp.content.tables.FutureProofTable;
import com.halloapp.content.tables.MessagesTable;
import com.halloapp.content.tables.PostsTable;

public class FutureProofDb {

    private final ContentDbHelper databaseHelper;

    public FutureProofDb(@NonNull ContentDbHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void saveFutureProof(@NonNull FutureProofMessage message) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();

        final ContentValues futureProofValues = new ContentValues();
        futureProofValues.put(FutureProofTable.COLUMN_PARENT_TABLE, MessagesTable.TABLE_NAME);
        futureProofValues.put(FutureProofTable.COLUMN_PARENT_ROW_ID, message.rowId);
        futureProofValues.put(FutureProofTable.COLUMN_CONTENT_BYTES, message.getProtoBytes());

        db.insertWithOnConflict(FutureProofTable.TABLE_NAME, null, futureProofValues, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void saveFutureProof(@NonNull FutureProofPost post) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();

        final ContentValues futureProofValues = new ContentValues();
        futureProofValues.put(FutureProofTable.COLUMN_PARENT_TABLE, PostsTable.TABLE_NAME);
        futureProofValues.put(FutureProofTable.COLUMN_PARENT_ROW_ID, post.rowId);
        futureProofValues.put(FutureProofTable.COLUMN_CONTENT_BYTES, post.getProtoBytes());

        db.insertWithOnConflict(FutureProofTable.TABLE_NAME, null, futureProofValues, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void saveFutureProof(@NonNull FutureProofComment comment) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();

        final ContentValues futureProofValues = new ContentValues();
        futureProofValues.put(FutureProofTable.COLUMN_PARENT_TABLE, CommentsTable.TABLE_NAME);
        futureProofValues.put(FutureProofTable.COLUMN_PARENT_ROW_ID, comment.rowId);
        futureProofValues.put(FutureProofTable.COLUMN_CONTENT_BYTES, comment.getProtoBytes());

        db.insertWithOnConflict(FutureProofTable.TABLE_NAME, null, futureProofValues, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void fillFutureProof(@NonNull FutureProofPost post) {
        post.setProtoBytes(readBytes(PostsTable.TABLE_NAME, post.rowId));
    }

    public void fillFutureProof(@NonNull FutureProofComment comment) {
        comment.setProtoBytes(readBytes(CommentsTable.TABLE_NAME, comment.rowId));
    }

    private byte[] readBytes(String parentTable, long parentRowId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(FutureProofTable.TABLE_NAME,
                new String[]{
                        FutureProofTable._ID,
                        FutureProofTable.COLUMN_PARENT_TABLE,
                        FutureProofTable.COLUMN_PARENT_ROW_ID,
                        FutureProofTable.COLUMN_CONTENT_BYTES},
                FutureProofTable.COLUMN_PARENT_TABLE + "=? AND " + FutureProofTable.COLUMN_PARENT_ROW_ID + "=?",
                new String[] {
                        parentTable,
                        Long.toString(parentRowId)
                },
                null,
                null,
                null)) {
            while (cursor.moveToNext()) {
                return cursor.getBlob(3);
            }
        }
        return null;
    }

}
