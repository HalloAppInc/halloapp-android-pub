package com.halloapp.content;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.halloapp.FileStore;
import com.halloapp.UrlPreview;
import com.halloapp.content.tables.ArchiveTable;
import com.halloapp.content.tables.CommentsTable;
import com.halloapp.content.tables.MediaTable;
import com.halloapp.content.tables.MessagesTable;
import com.halloapp.content.tables.PostsTable;
import com.halloapp.content.tables.SeenTable;
import com.halloapp.content.tables.UrlPreviewsTable;
import com.halloapp.util.Preconditions;

public class UrlPreviewsDb {
    private final MediaDb mediaDb;
    private final ContentDbHelper databaseHelper;

    public UrlPreviewsDb(@NonNull MediaDb mediaDb, @NonNull ContentDbHelper databaseHelper) {
        this.mediaDb = mediaDb;
        this.databaseHelper = databaseHelper;
    }

    public void addUrlPreview(@NonNull Message message) {
        if (message.urlPreview != null) {
            final SQLiteDatabase db = databaseHelper.getWritableDatabase();
            UrlPreview urlPreview = message.urlPreview;
            final ContentValues urlValues = new ContentValues();
            urlValues.put(UrlPreviewsTable.COLUMN_PARENT_TABLE, MessagesTable.TABLE_NAME);
            urlValues.put(UrlPreviewsTable.COLUMN_PARENT_ROW_ID, message.rowId);

            urlValues.put(UrlPreviewsTable.COLUMN_TITLE, urlPreview.title);
            urlValues.put(UrlPreviewsTable.COLUMN_URL, urlPreview.url);
            urlValues.put(UrlPreviewsTable.COLUMN_DESCRIPTION, urlPreview.description);
            urlPreview.rowId = db.insertWithOnConflict(UrlPreviewsTable.TABLE_NAME, null, urlValues, SQLiteDatabase.CONFLICT_IGNORE);
            if (urlPreview.imageMedia != null) {
                mediaDb.addMedia(urlPreview);
            }
        }
    }

    public void addUrlPreview(@NonNull Post post) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        if (post.urlPreview != null) {
            UrlPreview urlPreview = post.urlPreview;
            final ContentValues urlValues = new ContentValues();
            urlValues.put(UrlPreviewsTable.COLUMN_PARENT_TABLE, PostsTable.TABLE_NAME);
            urlValues.put(UrlPreviewsTable.COLUMN_PARENT_ROW_ID, post.rowId);

            urlValues.put(UrlPreviewsTable.COLUMN_TITLE, urlPreview.title);
            urlValues.put(UrlPreviewsTable.COLUMN_URL, urlPreview.url);
            urlValues.put(UrlPreviewsTable.COLUMN_DESCRIPTION, urlPreview.description);
            urlPreview.rowId = db.insertWithOnConflict(UrlPreviewsTable.TABLE_NAME, null, urlValues, SQLiteDatabase.CONFLICT_IGNORE);
            if (urlPreview.imageMedia != null) {
                mediaDb.addMedia(urlPreview);
            }
        }
    }

    public void addUrlPreview(@NonNull Comment comment) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        if (comment.urlPreview != null) {
            UrlPreview urlPreview = comment.urlPreview;
            final ContentValues urlValues = new ContentValues();
            urlValues.put(UrlPreviewsTable.COLUMN_PARENT_TABLE, CommentsTable.TABLE_NAME);
            urlValues.put(UrlPreviewsTable.COLUMN_PARENT_ROW_ID, comment.rowId);

            urlValues.put(UrlPreviewsTable.COLUMN_TITLE, urlPreview.title);
            urlValues.put(UrlPreviewsTable.COLUMN_URL, urlPreview.url);
            urlValues.put(UrlPreviewsTable.COLUMN_DESCRIPTION, urlPreview.description);
            urlPreview.rowId = db.insertWithOnConflict(UrlPreviewsTable.TABLE_NAME, null, urlValues, SQLiteDatabase.CONFLICT_IGNORE);
            if (urlPreview.imageMedia != null) {
                mediaDb.addMedia(urlPreview);
            }
        }
    }

    public void fillUrlPreview(@NonNull Comment comment) {
        comment.urlPreview = readUrlPreview(CommentsTable.TABLE_NAME, comment.rowId);
    }

    public void fillUrlPreview(@NonNull Post post) {
        if (post.isArchived) {
            post.urlPreview = (readUrlPreview(ArchiveTable.TABLE_NAME, post.rowId));
        } else {
            post.urlPreview = (readUrlPreview(PostsTable.TABLE_NAME, post.rowId));
        }
    }

    public void fillUrlPreview(@NonNull Message message) {
        message.urlPreview = (readUrlPreview(MessagesTable.TABLE_NAME, message.rowId));
    }

    private UrlPreview readUrlPreview(String parentTable, long parentRowId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String sql =
                "SELECT " +
                        UrlPreviewsTable.TABLE_NAME + "." + UrlPreviewsTable._ID + "," +
                        UrlPreviewsTable.TABLE_NAME + "." + UrlPreviewsTable.COLUMN_PARENT_TABLE + "," +
                        UrlPreviewsTable.TABLE_NAME + "." + UrlPreviewsTable.COLUMN_PARENT_ROW_ID + "," +
                        UrlPreviewsTable.TABLE_NAME + "." +  UrlPreviewsTable.COLUMN_TITLE + "," +
                        UrlPreviewsTable.TABLE_NAME + "." + UrlPreviewsTable.COLUMN_URL + "," +
                        UrlPreviewsTable.TABLE_NAME + "." + UrlPreviewsTable.COLUMN_DESCRIPTION + "," +
                        "m." + MediaTable._ID + "," +
                        "m." + MediaTable.COLUMN_TYPE + "," +
                        "m." + MediaTable.COLUMN_URL + "," +
                        "m." + MediaTable.COLUMN_FILE + "," +
                        "m." + MediaTable.COLUMN_ENC_KEY + "," +
                        "m." + MediaTable.COLUMN_ENC_FILE + "," +
                        "m." + MediaTable.COLUMN_SHA256_HASH + "," +
                        "m." + MediaTable.COLUMN_WIDTH + "," +
                        "m." + MediaTable.COLUMN_HEIGHT + "," +
                        "m." + MediaTable.COLUMN_TRANSFERRED + "," +
                        "m." + MediaTable.COLUMN_BLOB_VERSION + " " +
                        "FROM " + UrlPreviewsTable.TABLE_NAME + " " +
                        "LEFT JOIN (" +
                        "SELECT " +
                        MediaTable._ID + "," +
                        MediaTable.COLUMN_PARENT_TABLE + "," +
                        MediaTable.COLUMN_PARENT_ROW_ID + "," +
                        MediaTable.COLUMN_TYPE + "," +
                        MediaTable.COLUMN_URL + "," +
                        MediaTable.COLUMN_FILE + "," +
                        MediaTable.COLUMN_ENC_KEY + "," +
                        MediaTable.COLUMN_ENC_FILE + "," +
                        MediaTable.COLUMN_SHA256_HASH + "," +
                        MediaTable.COLUMN_WIDTH + "," +
                        MediaTable.COLUMN_HEIGHT + "," +
                        MediaTable.COLUMN_TRANSFERRED + "," +
                        MediaTable.COLUMN_BLOB_VERSION +
                        " FROM " + MediaTable.TABLE_NAME + " ORDER BY " + MediaTable._ID + " ASC) " +
                        "AS m ON " + UrlPreviewsTable.TABLE_NAME + "." + UrlPreviewsTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + UrlPreviewsTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
                        "WHERE " + UrlPreviewsTable.TABLE_NAME + "." + UrlPreviewsTable.COLUMN_PARENT_TABLE + "=? AND " + UrlPreviewsTable.TABLE_NAME + "." + UrlPreviewsTable.COLUMN_PARENT_ROW_ID + "=?";;
        try (final Cursor cursor = db.rawQuery(sql, new String[]{parentTable, Long.toString(parentRowId)})) {
            UrlPreview preview = null;
            while (cursor.moveToNext()) {
                long rowId = cursor.getLong(0);
                if (preview == null) {
                    String title = cursor.getString(3);
                    String url = cursor.getString(4);
                    preview = UrlPreview.create(rowId, title, url);
                    preview.description = cursor.getString(5);
                }
                if (!cursor.isNull(6)) {
                    Media media = new Media(
                            cursor.getLong(6),
                            cursor.getInt(7),
                            cursor.getString(8),
                            FileStore.getInstance().getMediaFile(cursor.getString(9)),
                            cursor.getBlob(10),
                            cursor.getBlob(12),
                            null,
                            cursor.getInt(13),
                            cursor.getInt(14),
                            cursor.getInt(15),
                            cursor.getInt(16),
                            0,
                            0);
                    media.encFile = FileStore.getInstance().getTmpFile(cursor.getString(11));
                    Preconditions.checkNotNull(preview).imageMedia = media;
                }
            }
            return preview;
        }
    }
}
