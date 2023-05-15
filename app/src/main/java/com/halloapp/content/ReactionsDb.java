package com.halloapp.content;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.content.tables.CommentsTable;
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

    List<Reaction> getIncomingPostReactionsHistory(int limit) {
        final List<Reaction> reactions = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String where = ReactionsTable.COLUMN_REACTION_TYPE + "<>'' AND " + ReactionsTable.COLUMN_SENDER_USER_ID + "<>'' AND "
                + "EXISTS(SELECT post_id FROM posts WHERE posts.post_id=reactions.content_id AND posts.sender_user_id='')";

        try (final Cursor cursor = db.query(ReactionsTable.TABLE_NAME,
                new String [] {
                        ReactionsTable.COLUMN_REACTION_ID,
                        ReactionsTable.COLUMN_CONTENT_ID,
                        ReactionsTable.COLUMN_SENDER_USER_ID,
                        ReactionsTable.COLUMN_REACTION_TYPE,
                        ReactionsTable.COLUMN_TIMESTAMP,
                        ReactionsTable.COLUMN_SEEN},
                where,
                null,
                null, null, ReactionsTable.COLUMN_TIMESTAMP + " DESC", String.valueOf(limit))) {
            while (cursor.moveToNext()) {
                Reaction reaction = new Reaction(
                        cursor.getString(0),
                        cursor.getString(1),
                        new UserId(cursor.getString(2)),
                        cursor.getString(3),
                        cursor.getLong(4));
                reaction.seen = cursor.getInt(5) == 1;
                reactions.add(reaction);
            }
        }

        Log.i("ContentDb.getIncomingPostReactionsHistory: reactions.size=" + reactions.size());
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

    void markReactionsSeen(@NonNull String contentId, boolean seen) {
        ContentValues values = new ContentValues();
        values.put(ReactionsTable.COLUMN_SEEN, seen);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.update(ReactionsTable.TABLE_NAME, values,
                    ReactionsTable.COLUMN_CONTENT_ID + "=?",
                    new String[]{contentId});
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

    @WorkerThread
    @NonNull List<Reaction> getKatchupPostReactions(@NonNull String postId, int start, int count) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final List<Reaction> reactions = new ArrayList<>();
        final String sql =
                "SELECT " + ReactionsTable.TABLE_NAME + "." + ReactionsTable.COLUMN_REACTION_ID + ","
                        + ReactionsTable.TABLE_NAME + "." + ReactionsTable.COLUMN_CONTENT_ID + ","
                        + ReactionsTable.TABLE_NAME + "." + ReactionsTable.COLUMN_SENDER_USER_ID + ","
                        + ReactionsTable.TABLE_NAME + "." + ReactionsTable.COLUMN_REACTION_TYPE + ","
                        + ReactionsTable.TABLE_NAME + "." + ReactionsTable.COLUMN_TIMESTAMP
                        + " FROM " + ReactionsTable.TABLE_NAME
                        + " LEFT JOIN " + CommentsTable.TABLE_NAME
                        + " ON " + ReactionsTable.TABLE_NAME + "." + ReactionsTable.COLUMN_REACTION_ID + "=" + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_COMMENT_ID
                        + " WHERE " + ReactionsTable.TABLE_NAME + "." + ReactionsTable.COLUMN_CONTENT_ID + "=?"
                        + " AND " + ReactionsTable.TABLE_NAME + "." + ReactionsTable.COLUMN_REACTION_TYPE + "<>''"
                        + " AND (" + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_TYPE + "<>" + Comment.TYPE_RETRACTED
                        + " OR " + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_COMMENT_ID + " IS NULL)"
                        + " LIMIT " + count + " OFFSET " + start;

        try (final Cursor cursor = db.rawQuery(sql, new String[]{postId})) {
            while (cursor.moveToNext()) {
                final Reaction reaction = new Reaction(
                        cursor.getString(0),
                        cursor.getString(1),
                        new UserId(cursor.getString(2)),
                        cursor.getString(3),
                        cursor.getLong(4));
                reactions.add(reaction);
            }
        }
        Log.i("ReactionsDb.getLikeReactionsKatchup: start=" + start + " count=" + count + " reactions.size=" + reactions.size());
        return reactions;
    }

    @WorkerThread
    int getKatchupPostReactionsCount(@NonNull String postId, @Nullable String senderUserId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String sql =
                "SELECT COUNT(*) FROM " + ReactionsTable.TABLE_NAME
                        + " LEFT JOIN " + CommentsTable.TABLE_NAME
                        + " ON " + ReactionsTable.TABLE_NAME + "." + ReactionsTable.COLUMN_REACTION_ID + "=" + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_COMMENT_ID
                        + " WHERE " + ReactionsTable.TABLE_NAME + "." + ReactionsTable.COLUMN_CONTENT_ID + "=?"
                        + " AND " + ReactionsTable.TABLE_NAME + "." + ReactionsTable.COLUMN_REACTION_TYPE + "<>''"
                        + " AND (" + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_TYPE + "<>" + Comment.TYPE_RETRACTED
                        + " OR " + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_COMMENT_ID + " IS NULL)";
        final List<String> arguments = new ArrayList<>();
        arguments.add(postId);

        if (senderUserId != null) {
            sql += " AND " + ReactionsTable.TABLE_NAME + "." + ReactionsTable.COLUMN_SENDER_USER_ID + "=?";
            arguments.add(senderUserId);
        }

        int count = 0;
        try (final Cursor cursor = db.rawQuery(sql, arguments.toArray(new String[0]))) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
        }
        return count;
    }

    @WorkerThread
    @Nullable
    Reaction getMyKatchupPostReaction(@NonNull String postId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final String sql =
                "SELECT " + ReactionsTable.TABLE_NAME + "." + ReactionsTable.COLUMN_REACTION_ID + ", "
                        + ReactionsTable.TABLE_NAME + "." + ReactionsTable.COLUMN_CONTENT_ID + ", "
                        + ReactionsTable.TABLE_NAME + "." + ReactionsTable.COLUMN_SENDER_USER_ID + ", "
                        + ReactionsTable.TABLE_NAME + "." + ReactionsTable.COLUMN_REACTION_TYPE + ", "
                        + ReactionsTable.TABLE_NAME + "." + ReactionsTable.COLUMN_TIMESTAMP
                        + " FROM " + ReactionsTable.TABLE_NAME
                        + " LEFT JOIN " + CommentsTable.TABLE_NAME
                        + " ON " + ReactionsTable.TABLE_NAME + "." + ReactionsTable.COLUMN_REACTION_ID + "=" + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_COMMENT_ID
                        + " WHERE " + ReactionsTable.TABLE_NAME + "." + ReactionsTable.COLUMN_CONTENT_ID + "=?"
                        + " AND " + ReactionsTable.TABLE_NAME + "." + ReactionsTable.COLUMN_SENDER_USER_ID + "=?"
                        + " AND " + ReactionsTable.TABLE_NAME + "." + ReactionsTable.COLUMN_REACTION_TYPE + "<>''"
                        + " AND (" + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_TYPE + "<>" + Comment.TYPE_RETRACTED
                        + " OR " + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_COMMENT_ID + " IS NULL)";

        try (final Cursor cursor = db.rawQuery(sql, new String[]{postId, UserId.ME.rawId()})) {
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
}
