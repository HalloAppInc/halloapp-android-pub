package com.halloapp.content;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.content.tables.GalleryTable;
import com.halloapp.content.tables.SuggestionsTable;
import com.halloapp.Suggestion;
import com.halloapp.ui.mediapicker.GalleryItem;
import com.halloapp.util.logs.Log;

import java.util.ArrayList;
import java.util.List;

public class GalleryDb {

    private final ContentDbHelper databaseHelper;

    GalleryDb(ContentDbHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    void addGalleryItemUri(long uriId, int type, long date, long duration) {
        Log.i("GalleryDb.addGalleryItemUri " + uriId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final ContentValues galleryItemValues = new ContentValues();
            galleryItemValues.put(GalleryTable.COLUMN_GALLERY_ITEM_URI, uriId);
            galleryItemValues.put(GalleryTable.COLUMN_TYPE, type);
            galleryItemValues.put(GalleryTable.COLUMN_TIME_TAKEN, date);
            galleryItemValues.put(GalleryTable.COLUMN_DURATION, duration);
            db.insertWithOnConflict(GalleryTable.TABLE_NAME, null, galleryItemValues, SQLiteDatabase.CONFLICT_IGNORE);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    void addGalleryItem(@NonNull GalleryItem galleryItem, @Nullable String suggestionId) {
        Log.i("GalleryDb.addGalleryItem " + galleryItem.id);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final ContentValues galleryItemValues = new ContentValues();
            galleryItemValues.put(GalleryTable.COLUMN_GALLERY_ITEM_URI, galleryItem.id);
            galleryItemValues.put(GalleryTable.COLUMN_TYPE, galleryItem.type);
            galleryItemValues.put(GalleryTable.COLUMN_TIME_TAKEN, galleryItem.date);
            galleryItemValues.put(GalleryTable.COLUMN_DURATION, galleryItem.duration);
            galleryItemValues.put(GalleryTable.COLUMN_LATITUDE, galleryItem.latitude);
            galleryItemValues.put(GalleryTable.COLUMN_LONGITUDE, galleryItem.longitude);
            galleryItemValues.put(GalleryTable.COLUMN_SUGGESTION_ID, suggestionId);
            db.insertWithOnConflict(GalleryTable.TABLE_NAME, null, galleryItemValues, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    void addAllGalleryItems(@NonNull List<GalleryItem> galleryItems, @NonNull String suggestionId) {
        Log.i("GalleryDb.addAllGalleryItems adding " + galleryItems.size() + " to suggestion " + suggestionId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (GalleryItem galleryItem : galleryItems) {
                final ContentValues galleryItemValues = new ContentValues();
                galleryItemValues.put(GalleryTable.COLUMN_GALLERY_ITEM_URI, galleryItem.id);
                galleryItemValues.put(GalleryTable.COLUMN_TYPE, galleryItem.type);
                galleryItemValues.put(GalleryTable.COLUMN_TIME_TAKEN, galleryItem.date);
                galleryItemValues.put(GalleryTable.COLUMN_DURATION, galleryItem.duration);
                galleryItemValues.put(GalleryTable.COLUMN_LATITUDE, galleryItem.latitude);
                galleryItemValues.put(GalleryTable.COLUMN_LONGITUDE, galleryItem.longitude);
                galleryItemValues.put(GalleryTable.COLUMN_SUGGESTION_ID, suggestionId);
                db.insertWithOnConflict(GalleryTable.TABLE_NAME, null, galleryItemValues, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    void deleteGalleryItem(long uriId) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.delete(GalleryTable.TABLE_NAME, GalleryTable.COLUMN_GALLERY_ITEM_URI + "=?", new String[] {Long.toString(uriId)});
    }

    void deleteGalleryItemFromSuggestion(long uriId) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String suggestionId = null;
        int suggestionSize = 0;
        db.beginTransaction();
        try (final Cursor cursor = db.rawQuery(
                "SELECT " +
                        SuggestionsTable.TABLE_NAME + "." + SuggestionsTable.COLUMN_SUGGESTION_ID + ", " +
                        SuggestionsTable.TABLE_NAME + "." + SuggestionsTable.COLUMN_SIZE +
                        " FROM " + SuggestionsTable.TABLE_NAME +
                        " LEFT JOIN " + GalleryTable.TABLE_NAME + " ON " +
                        SuggestionsTable.TABLE_NAME + "." + SuggestionsTable.COLUMN_SUGGESTION_ID + "=" + GalleryTable.TABLE_NAME + "." + GalleryTable.COLUMN_SUGGESTION_ID +
                        " WHERE " + GalleryTable.TABLE_NAME + "." + GalleryTable.COLUMN_GALLERY_ITEM_URI + "=?", new String[] {Long.toString(uriId)})) {
            if (cursor.moveToNext()) {
                suggestionId = cursor.getString(0);
                suggestionSize = cursor.getInt(1);
            }
        }
        try {
            if (suggestionId != null) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(SuggestionsTable.COLUMN_SIZE, suggestionSize - 1);
                db.updateWithOnConflict(SuggestionsTable.TABLE_NAME, contentValues, SuggestionsTable.COLUMN_SUGGESTION_ID + "=?", new String[] {suggestionId}, SQLiteDatabase.CONFLICT_ABORT);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    void deleteGalleryItems(@NonNull List<GalleryItem> galleryItems) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (GalleryItem galleryItem : galleryItems) {
                db.delete(GalleryTable.TABLE_NAME, GalleryTable.COLUMN_GALLERY_ITEM_URI + "=?", new String[] {Long.toString(galleryItem.id)});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    ArrayList<GalleryItem> getPendingGalleryItems(long cutoffTime) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        ArrayList<GalleryItem> galleryItems = new ArrayList<>();
        try (final Cursor cursor = db.query(GalleryTable.TABLE_NAME,
                new String [] {
                        GalleryTable.COLUMN_GALLERY_ITEM_URI,
                        GalleryTable.COLUMN_TYPE,
                        GalleryTable.COLUMN_TIME_TAKEN,
                        GalleryTable.COLUMN_DURATION,
                        GalleryTable.COLUMN_LATITUDE,
                        GalleryTable.COLUMN_LONGITUDE,
                }, GalleryTable.COLUMN_SUGGESTION_ID + " IS NULL AND " + GalleryTable.COLUMN_GALLERY_ITEM_URI + " > ? AND " + GalleryTable.COLUMN_TIME_TAKEN + " > ?",
                new String[] {String.valueOf(-1), String.valueOf(cutoffTime)}, null, null, null)) {
            while (cursor.moveToNext()) {
                GalleryItem galleryItem = new GalleryItem(
                        cursor.getLong(0),
                        cursor.getInt(1),
                        cursor.getLong(2),
                        cursor.getLong(3),
                        cursor.getDouble(4),
                        cursor.getDouble(5),
                        null);
                galleryItems.add(galleryItem);
            }
        }
        return galleryItems;
    }

    void deleteAllGalleryItems() {
        Log.i("Deleting all galleryItems");
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(GalleryTable.TABLE_NAME, null, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
    
    void addAllSuggestions(@NonNull ArrayList<Suggestion> suggestions) {
        Log.i("GalleryDb.addAllSuggestions adding " + suggestions.size() + " suggestions");
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Suggestion suggestion : suggestions) {
                final ContentValues suggestionValues = new ContentValues();
                suggestionValues.put(SuggestionsTable.COLUMN_SUGGESTION_ID, suggestion.id);
                suggestionValues.put(SuggestionsTable.COLUMN_TIMESTAMP, suggestion.timestamp);
                suggestionValues.put(SuggestionsTable.COLUMN_SIZE, suggestion.size);
                suggestionValues.put(SuggestionsTable.COLUMN_LATITUDE, suggestion.latitude);
                suggestionValues.put(SuggestionsTable.COLUMN_LONGITUDE, suggestion.longitude);
                suggestionValues.put(SuggestionsTable.COLUMN_LOCATION_NAME, suggestion.locationName);
                suggestionValues.put(SuggestionsTable.COLUMN_LOCATION_ADDRESS, suggestion.locationAddress);
                suggestionValues.put(SuggestionsTable.COLUMN_IS_SCORED, suggestion.isScored);
                db.insertWithOnConflict(SuggestionsTable.TABLE_NAME, null, suggestionValues, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    void markSuggestedGalleryItems(@NonNull List<Long> suggestedGalleryItems, @NonNull String suggestionId) {
        ContentValues galleryItemValues = new ContentValues();
        galleryItemValues.put(GalleryTable.COLUMN_IS_SUGGESTED, true);
        ContentValues suggestionValues = new ContentValues();
        suggestionValues.put(SuggestionsTable.COLUMN_IS_SCORED, true);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Long galleryItemId : suggestedGalleryItems) {
                db.updateWithOnConflict(GalleryTable.TABLE_NAME, galleryItemValues,
                        GalleryTable.COLUMN_GALLERY_ITEM_URI + "=? ",
                        new String[] {Long.toString(galleryItemId)}, SQLiteDatabase.CONFLICT_ABORT);
            }
            db.updateWithOnConflict(SuggestionsTable.TABLE_NAME, suggestionValues,
                    SuggestionsTable.COLUMN_SUGGESTION_ID + "=? ",
                    new String[] {suggestionId}, SQLiteDatabase.CONFLICT_ABORT);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    Suggestion getSuggestion(@NonNull String suggestionId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(SuggestionsTable.TABLE_NAME,
                new String [] {
                        SuggestionsTable.COLUMN_SUGGESTION_ID,
                        SuggestionsTable.COLUMN_LATITUDE,
                        SuggestionsTable.COLUMN_LONGITUDE,
                        SuggestionsTable.COLUMN_LOCATION_NAME,
                        SuggestionsTable.COLUMN_LOCATION_ADDRESS,
                        SuggestionsTable.COLUMN_TIMESTAMP,
                        SuggestionsTable.COLUMN_SIZE,
                        SuggestionsTable.COLUMN_IS_SCORED
                }, SuggestionsTable.COLUMN_SUGGESTION_ID + "=? ", new String[] {suggestionId}, null, null, "1")) {
            if (cursor.moveToNext()) {
                return new Suggestion(
                        cursor.getString(0),
                        cursor.getDouble(1),
                        cursor.getDouble(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getLong(5),
                        cursor.getInt(6),
                        cursor.getInt(7) == 1);
            }
        }
        return null;
    }

    ArrayList<Suggestion> getAllSuggestions() {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        ArrayList<Suggestion> suggestions = new ArrayList<>();
        try (final Cursor cursor = db.query(SuggestionsTable.TABLE_NAME,
            new String [] {
                SuggestionsTable.COLUMN_SUGGESTION_ID,
                SuggestionsTable.COLUMN_LATITUDE,
                SuggestionsTable.COLUMN_LONGITUDE,
                SuggestionsTable.COLUMN_LOCATION_NAME,
                SuggestionsTable.COLUMN_LOCATION_ADDRESS,
                SuggestionsTable.COLUMN_TIMESTAMP,
                SuggestionsTable.COLUMN_SIZE,
                SuggestionsTable.COLUMN_IS_SCORED
            }, null , null, null, null, null)) {
            while (cursor.moveToNext()) {
                Suggestion suggestion = new Suggestion(
                    cursor.getString(0),
                    cursor.getDouble(1),
                    cursor.getDouble(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getLong(5),
                    cursor.getInt(6),
                    cursor.getInt(7) == 1);
                suggestions.add(suggestion);
            }
        }
        return suggestions;
    }

    void deleteAllSuggestions() {
        Log.i("Deleting all suggestions from GalleryItemsDb");
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(SuggestionsTable.TABLE_NAME, null, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    GalleryItem getThumbnailPhotoBySuggestion(@NonNull String suggestionId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(GalleryTable.TABLE_NAME,
                new String [] {
                    GalleryTable.COLUMN_GALLERY_ITEM_URI,
                    GalleryTable.COLUMN_TYPE,
                    GalleryTable.COLUMN_TIME_TAKEN,
                    GalleryTable.COLUMN_DURATION,
                    GalleryTable.COLUMN_LATITUDE,
                    GalleryTable.COLUMN_LONGITUDE,
                    GalleryTable.COLUMN_SUGGESTION_ID
                }, GalleryTable.COLUMN_SUGGESTION_ID + "=? ",
                new String[] {suggestionId}, null, null, null, "1")) {
            if (cursor.moveToNext()) {
                return new GalleryItem(
                    cursor.getLong(0),
                    cursor.getInt(1),
                    cursor.getLong(2),
                    cursor.getLong(3),
                    cursor.getFloat(4),
                    cursor.getFloat(5),
                    cursor.getString(6));
            }
        }
        return null;
    }

    List<Long> getSelectedGalleryItemIds(@NonNull String suggestionId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        ArrayList<Long> galleryItems = new ArrayList<>();
        try (final Cursor cursor = db.query(GalleryTable.TABLE_NAME,
                new String [] {GalleryTable.COLUMN_GALLERY_ITEM_URI},
                GalleryTable.COLUMN_IS_SUGGESTED + "=? AND " + GalleryTable.COLUMN_SUGGESTION_ID + "=? ",
                new String[] {String.valueOf(1), suggestionId}, null, null, null)) {
            while (cursor.moveToNext()) {
                galleryItems.add(cursor.getLong(0));
            }
        }
        return galleryItems;
    }

    List<GalleryItem> getGalleryItemsBySuggestion(@NonNull String suggestionId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        ArrayList<GalleryItem> galleryItems = new ArrayList<>();
        try (final Cursor cursor = db.query(GalleryTable.TABLE_NAME,
                new String [] {
                    GalleryTable.COLUMN_GALLERY_ITEM_URI,
                    GalleryTable.COLUMN_TYPE,
                    GalleryTable.COLUMN_TIME_TAKEN,
                    GalleryTable.COLUMN_DURATION,
                    GalleryTable.COLUMN_LATITUDE,
                    GalleryTable.COLUMN_LONGITUDE,
                    GalleryTable.COLUMN_SUGGESTION_ID
                }, GalleryTable.COLUMN_SUGGESTION_ID + "=? AND " + GalleryTable.COLUMN_GALLERY_ITEM_URI + " > ?",
                new String[] {suggestionId, String.valueOf(-1)}, null, null, GalleryTable.COLUMN_TIME_TAKEN + " DESC")) {
            while (cursor.moveToNext()) {
                GalleryItem galleryItem = new GalleryItem(
                    cursor.getLong(0),
                    cursor.getInt(1),
                    cursor.getLong(2),
                    cursor.getLong(3),
                    cursor.getDouble(4),
                    cursor.getDouble(5),
                    cursor.getString(6));
                galleryItems.add(galleryItem);
            }
        }
        return galleryItems;
    }
}
