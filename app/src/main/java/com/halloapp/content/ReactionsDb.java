package com.halloapp.content;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.halloapp.content.tables.ReactionsTable;
import com.halloapp.id.UserId;
import com.halloapp.util.logs.Log;

import java.util.ArrayList;
import java.util.List;

public class ReactionsDb {

    private final ContentDbHelper databaseHelper;

    ReactionsDb(ContentDbHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    void addReaction(@NonNull Reaction reaction) {
        Log.i("ContentDb.addReaction " + reaction);
        long now = System.currentTimeMillis();
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            if ("".equals(reaction.reactionType) && !reaction.senderUserId.isMe()) {
                db.delete(ReactionsTable.TABLE_NAME,
                        ReactionsTable.COLUMN_CONTENT_ID + "=? AND " + ReactionsTable.COLUMN_SENDER_USER_ID + "=?",
                        new String[]{reaction.contentId, reaction.senderUserId.rawId()});
            } else {
                final ContentValues reactionValues = new ContentValues();
                reactionValues.put(ReactionsTable.COLUMN_REACTION_ID, reaction.reactionId);
                reactionValues.put(ReactionsTable.COLUMN_CONTENT_ID, reaction.contentId);
                reactionValues.put(ReactionsTable.COLUMN_SENDER_USER_ID, reaction.getSenderUserId().rawId());
                if (reaction.getReactionType() != null) {
                    reactionValues.put(ReactionsTable.COLUMN_REACTION_TYPE, reaction.getReactionType());
                }
                reactionValues.put(ReactionsTable.COLUMN_TIMESTAMP, now);
                reactionValues.put(ReactionsTable.COLUMN_SENT, false);
                db.insertWithOnConflict(ReactionsTable.TABLE_NAME, null, reactionValues, SQLiteDatabase.CONFLICT_REPLACE);
            }
        } finally {
            db.setTransactionSuccessful();
        }
        db.endTransaction();
    }

    Reaction getReaction(@NonNull String reactionId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (final Cursor cursor = db.query(ReactionsTable.TABLE_NAME,
                new String [] {
                        ReactionsTable.COLUMN_REACTION_ID,
                        ReactionsTable.COLUMN_CONTENT_ID,
                        ReactionsTable.COLUMN_SENDER_USER_ID,
                        ReactionsTable.COLUMN_REACTION_TYPE,
                        ReactionsTable.COLUMN_TIMESTAMP},
                ReactionsTable.COLUMN_REACTION_ID + "=?", new String[] {reactionId},
                null, null, null)) {
            if (cursor.moveToNext()) {
                return new Reaction(
                        cursor.getString(0),
                        cursor.getString(1),
                        new UserId(cursor.getString(2)),
                        cursor.getString(3),
                        cursor.getLong(4));
            }
        }
        return null;
    }

    List<Reaction> getReactions(@NonNull String contentId) {
        final List<Reaction> reactions = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (final Cursor cursor = db.query(ReactionsTable.TABLE_NAME,
                new String [] {
                        ReactionsTable.COLUMN_REACTION_ID,
                        ReactionsTable.COLUMN_CONTENT_ID,
                        ReactionsTable.COLUMN_SENDER_USER_ID,
                        ReactionsTable.COLUMN_REACTION_TYPE,
                        ReactionsTable.COLUMN_TIMESTAMP},
                ReactionsTable.COLUMN_CONTENT_ID + "=? AND " + ReactionsTable.COLUMN_REACTION_TYPE + "<>''", new String[] {contentId},
                null, null, null)) {
            while (cursor.moveToNext()) {
                Reaction reaction = new Reaction(
                        cursor.getString(0),
                        cursor.getString(1),
                        new UserId(cursor.getString(2)),
                        cursor.getString(3),
                        cursor.getLong(4));
                reactions.add(reaction);
            }
        }
        return reactions;
    }

    void markReactionSent(@NonNull Reaction reaction) {
        ContentValues values = new ContentValues();
        values.put(ReactionsTable.COLUMN_SENT, true);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.update(ReactionsTable.TABLE_NAME, values,
                    ReactionsTable.COLUMN_CONTENT_ID + "=? AND " + ReactionsTable.COLUMN_SENDER_USER_ID + "=?",
                    new String[]{reaction.contentId, reaction.getSenderUserId().rawId()});
        } finally {
            db.setTransactionSuccessful();
        }
        db.endTransaction();
    }

    List<Reaction> getPendingReactions() {
        final List<Reaction> reactions = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (final Cursor cursor = db.query(ReactionsTable.TABLE_NAME,
                new String [] {
                        ReactionsTable.COLUMN_REACTION_ID,
                        ReactionsTable.COLUMN_CONTENT_ID,
                        ReactionsTable.COLUMN_SENDER_USER_ID,
                        ReactionsTable.COLUMN_REACTION_TYPE,
                        ReactionsTable.COLUMN_TIMESTAMP},
                ReactionsTable.COLUMN_SENT + "=0 AND " + ReactionsTable.COLUMN_SENDER_USER_ID + "=''",
                null, null, null, null)) {
            while (cursor.moveToNext()) {
                Reaction reaction = new Reaction(
                        cursor.getString(0),
                        cursor.getString(1),
                        new UserId(cursor.getString(2)),
                        cursor.getString(3),
                        cursor.getLong(4));
                reactions.add(reaction);
            }
        }
        Log.i("ContentDb.getPendingReactions: reactions.size=" + reactions.size());
        return reactions;
    }
}
