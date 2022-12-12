package com.halloapp.content;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.halloapp.content.tables.KatchupMomentsTable;
import com.halloapp.content.tables.MomentsTable;

public class KatchupMomentDb {

    private final ContentDbHelper databaseHelper;

    public KatchupMomentDb(@NonNull ContentDbHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void saveKatchupMoment(@NonNull KatchupPost moment) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        final ContentValues cv = new ContentValues();
        cv.put(KatchupMomentsTable.COLUMN_POST_ID, moment.id);
        cv.put(KatchupMomentsTable.COLUMN_LOCATION, moment.location);
        cv.put(KatchupMomentsTable.COLUMN_SELFIE_X, moment.selfieX);
        cv.put(KatchupMomentsTable.COLUMN_SELFIE_Y, moment.selfieY);
        cv.put(KatchupMomentsTable.COLUMN_NOTIFICATION_TIMESTAMP, moment.notificationTimestamp);
        db.insertWithOnConflict(KatchupMomentsTable.TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void fillKatchupMoment(@NonNull KatchupPost momentPost) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(KatchupMomentsTable.TABLE_NAME,
                new String[]{
                        KatchupMomentsTable._ID,
                        KatchupMomentsTable.COLUMN_POST_ID,
                        KatchupMomentsTable.COLUMN_LOCATION,
                        KatchupMomentsTable.COLUMN_SELFIE_X,
                        KatchupMomentsTable.COLUMN_SELFIE_Y,
                        KatchupMomentsTable.COLUMN_NOTIFICATION_TIMESTAMP
                },
                KatchupMomentsTable.COLUMN_POST_ID + "=?",
                new String[] {
                        momentPost.id
                },
                null,
                null,
                MomentsTable._ID + " ASC")) {
            if (cursor.moveToNext()) {
                momentPost.location = cursor.getString(2);
                momentPost.selfieX = cursor.getFloat(3);
                momentPost.selfieY = cursor.getFloat(4);
                momentPost.notificationTimestamp = cursor.getLong(5);
            }
        }
    }
}
