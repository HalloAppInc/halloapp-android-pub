package com.halloapp.content;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import com.halloapp.content.tables.MomentsTable;
import com.halloapp.id.UserId;

public class MomentsDb {

    private final ContentDbHelper databaseHelper;

    public MomentsDb(@NonNull ContentDbHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void saveMoment(@NonNull MomentPost moment) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        final ContentValues cv = new ContentValues();
        cv.put(MomentsTable.COLUMN_POST_ID, moment.id);
        cv.put(MomentsTable.COLUMN_UNLOCKED_USER_ID, moment.unlockedUserId == null ? null : moment.unlockedUserId.rawId());
        db.insertWithOnConflict(MomentsTable.TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void fillMoment(@NonNull MomentPost momentPost) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(MomentsTable.TABLE_NAME,
                new String[]{
                        MomentsTable._ID,
                        MomentsTable.COLUMN_POST_ID,
                        MomentsTable.COLUMN_UNLOCKED_USER_ID},
                MomentsTable.COLUMN_POST_ID + "=?",
                new String[] {
                        momentPost.id
                },
                null,
                null,
                MomentsTable._ID + " ASC")) {
            if (cursor.moveToNext()) {
                String unlockedId = cursor.getString(2);
                momentPost.unlockedUserId = (UserId) UserId.fromNullable(unlockedId);
            }
        }
    }
}
