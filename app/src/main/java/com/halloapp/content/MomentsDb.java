package com.halloapp.content;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import com.halloapp.content.tables.MomentsTable;
import com.halloapp.content.tables.PostsTable;
import com.halloapp.id.UserId;
import com.halloapp.util.logs.Log;

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
        cv.put(MomentsTable.COLUMN_SCREENSHOTTED, moment.screenshotted);
        cv.put(MomentsTable.COLUMN_SELFIE_MEDIA_INDEX, moment.selfieMediaIndex);
        cv.put(MomentsTable.COLUMN_LOCATION, moment.location);
        db.insertWithOnConflict(MomentsTable.TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void setMomentScreenshotted(@NonNull String postId, int screenshotted) {
        final ContentValues values = new ContentValues();
        values.put(MomentsTable.COLUMN_SCREENSHOTTED, screenshotted);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(MomentsTable.TABLE_NAME, values,
                    PostsTable.COLUMN_POST_ID + "=?",
                    new String [] {postId},
                    SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("ContentDb.setMomentScreenshotted: failed");
            throw ex;
        }
    }

    public void fillMoment(@NonNull MomentPost momentPost) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(MomentsTable.TABLE_NAME,
                new String[]{
                        MomentsTable._ID,
                        MomentsTable.COLUMN_POST_ID,
                        MomentsTable.COLUMN_UNLOCKED_USER_ID,
                        MomentsTable.COLUMN_SCREENSHOTTED,
                        MomentsTable.COLUMN_SELFIE_MEDIA_INDEX,
                        MomentsTable.COLUMN_LOCATION,
                },
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
                momentPost.screenshotted = cursor.getInt(3);
                momentPost.selfieMediaIndex = cursor.getInt(4);
                momentPost.location = cursor.getString(5);
            }
        }
    }
}
