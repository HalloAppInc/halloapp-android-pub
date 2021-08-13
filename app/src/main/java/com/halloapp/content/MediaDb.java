package com.halloapp.content;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteStatement;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.FileStore;
import com.halloapp.content.tables.ArchiveTable;
import com.halloapp.content.tables.CommentsTable;
import com.halloapp.content.tables.MediaTable;
import com.halloapp.content.tables.PostsTable;
import com.halloapp.media.MediaUtils;

public class MediaDb {
    private final ContentDbHelper databaseHelper;
    private final FileStore fileStore;

    public MediaDb(@NonNull ContentDbHelper databaseHelper, @NonNull FileStore fileStore) {
        this.databaseHelper = databaseHelper;
        this.fileStore = fileStore;
    }

    @WorkerThread
    @Nullable Media getLatestMediaWithHash(@NonNull byte[] decSha256hash, @Media.BlobVersion int blobVersion) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final String rowQuerySql =
                "SELECT " + MediaTable._ID + " "
                        + "FROM " + MediaTable.TABLE_NAME + " "
                        + "WHERE " + MediaTable.COLUMN_DEC_SHA256_HASH + "=? AND " + MediaTable.COLUMN_BLOB_VERSION + "=? "
                        + "ORDER BY " + MediaTable._ID + " DESC "
                        + "LIMIT " + 1;
        final long rowId;
        try (final SQLiteStatement statement = db.compileStatement(rowQuerySql)) {
            statement.bindBlob(1, decSha256hash);
            statement.bindLong(2, blobVersion);
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
                        MediaTable.COLUMN_TRANSFERRED + "," +
                        MediaTable.COLUMN_BLOB_VERSION + "," +
                        MediaTable.COLUMN_CHUNK_SIZE + "," +
                        MediaTable.COLUMN_BLOB_SIZE + " " +
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
                        cursor.getInt(8),
                        cursor.getInt(9),
                        cursor.getInt(10),
                        cursor.getLong(11));
            }
        }
        return null;
    }

    @WorkerThread
    public void addMedia(@NonNull Post post) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        for (Media mediaItem : post.media) {
            addMediaItem(db, PostsTable.TABLE_NAME, post.rowId, mediaItem);
        }
    }

    @WorkerThread
    public void addArchiveMedia(@NonNull Post post) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        for (Media mediaItem : post.media) {
            addMediaItem(db, ArchiveTable.TABLE_NAME, post.rowId, mediaItem);
        }
    }

    @WorkerThread
    public void addMedia(@NonNull Comment comment) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        for (Media mediaItem : comment.media) {
            addMediaItem(db, CommentsTable.TABLE_NAME, comment.rowId, mediaItem);
        }
    }

    @WorkerThread
    private void addMediaItem(@NonNull SQLiteDatabase db, @NonNull String parentTable, long rowId, Media mediaItem) {
        final ContentValues mediaItemValues = new ContentValues();
        mediaItemValues.put(MediaTable.COLUMN_PARENT_TABLE, parentTable);
        mediaItemValues.put(MediaTable.COLUMN_PARENT_ROW_ID, rowId);
        mediaItemValues.put(MediaTable.COLUMN_TYPE, mediaItem.type);
        if (mediaItem.url != null) {
            mediaItemValues.put(MediaTable.COLUMN_URL, mediaItem.url);
        }
        if (mediaItem.file != null) {
            mediaItemValues.put(MediaTable.COLUMN_FILE, mediaItem.file.getName());
            if (mediaItem.width == 0 || mediaItem.height == 0) {
                final Size dimensions = MediaUtils.getDimensions(mediaItem.file, mediaItem.type);
                if (dimensions != null) {
                    mediaItem.width = dimensions.getWidth();
                    mediaItem.height = dimensions.getHeight();
                }
            }
        }
        if (mediaItem.encFile != null) {
            mediaItemValues.put(MediaTable.COLUMN_ENC_FILE, mediaItem.encFile.getName());
        }
        if (mediaItem.width > 0 && mediaItem.height > 0) {
            mediaItemValues.put(MediaTable.COLUMN_WIDTH, mediaItem.width);
            mediaItemValues.put(MediaTable.COLUMN_HEIGHT, mediaItem.height);
        }
        if (mediaItem.encKey != null) {
            mediaItemValues.put(MediaTable.COLUMN_ENC_KEY, mediaItem.encKey);
        }
        if (mediaItem.encSha256hash != null) {
            mediaItemValues.put(MediaTable.COLUMN_SHA256_HASH, mediaItem.encSha256hash);
        }
        if (mediaItem.decSha256hash != null) {
            mediaItemValues.put(MediaTable.COLUMN_DEC_SHA256_HASH, mediaItem.decSha256hash);
        }
        mediaItemValues.put(MediaTable.COLUMN_BLOB_VERSION, mediaItem.blobVersion);
        if (mediaItem.blobVersion == Media.BLOB_VERSION_CHUNKED) {
            mediaItemValues.put(MediaTable.COLUMN_CHUNK_SIZE, mediaItem.chunkSize);
            mediaItemValues.put(MediaTable.COLUMN_BLOB_SIZE, mediaItem.blobSize);
        }
        mediaItem.rowId = db.insertWithOnConflict(MediaTable.TABLE_NAME, null, mediaItemValues, SQLiteDatabase.CONFLICT_IGNORE);
    }
}
