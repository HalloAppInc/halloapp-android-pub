package com.halloapp.content;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteStatement;
import android.text.format.DateUtils;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.FileStore;
import com.halloapp.UrlPreview;
import com.halloapp.content.tables.ArchiveTable;
import com.halloapp.content.tables.CommentsTable;
import com.halloapp.content.tables.MediaTable;
import com.halloapp.content.tables.PostsTable;
import com.halloapp.content.tables.UrlPreviewsTable;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
    public List<Media> getAllMedia() {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();

        List<Media> media = new ArrayList<>();
        final String uploadQuerySql =
                "SELECT " +
                        MediaTable._ID + "," +
                        MediaTable.COLUMN_TYPE + "," +
                        MediaTable.COLUMN_FILE + " " +
                        "FROM " + MediaTable.TABLE_NAME;
        try (final Cursor cursor = db.rawQuery(uploadQuerySql, new String[]{})) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                int type = cursor.getInt((1));

                media.add(
                        new Media(
                                id,
                                type,
                                null,
                                fileStore.getMediaFile(cursor.getString(2)), null, null, null, 0, 0, Media.TRANSFERRED_NO, Media.BLOB_VERSION_DEFAULT, 0, 0));
            }
        }
        return media;
    }

    @WorkerThread
    public void cleanup() {
        List<Media> allMedia = getAllMedia();
        HashSet<String> mediaPaths = new HashSet<>();
        for (Media media : allMedia) {
            if (media.file != null) {
                mediaPaths.add(media.file.getAbsolutePath());
            }
        }
        File mediaDir = fileStore.getMediaDir();
        cleanupLeakedMediaFiles(mediaDir, mediaPaths);
    }

    private static void cleanupLeakedMediaFiles(File file, HashSet<String> mediaPaths) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                return;
            }
            for (int i = 0; i < files.length; i++) {
                cleanupLeakedMediaFiles(files[i], mediaPaths);
            }
        } else if (!mediaPaths.contains(file.getAbsolutePath())) {
            long ageMs = System.currentTimeMillis() - file.lastModified();
            boolean oldEnough = ageMs > DateUtils.DAY_IN_MILLIS;
            if (oldEnough) {
                boolean deleted = file.delete();
                if (deleted) {
                    Log.w("MediaDb/cleanupLeakedMediaFiles deleted orphaned media file: " + file.getName());
                } else {
                    Log.w("MediaDb/cleanupLeakedMediaFiles failed to delete orphaned media file: " + file.getName());
                }
            }
        }
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
    public void addMedia(@NonNull UrlPreview urlPreview) {
        if (urlPreview.imageMedia == null) {
            return;
        }
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        addMediaItem(db, UrlPreviewsTable.TABLE_NAME, urlPreview.rowId, urlPreview.imageMedia);
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
