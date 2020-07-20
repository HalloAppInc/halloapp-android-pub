package com.halloapp;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.ui.settings.SettingsActivity;
import com.halloapp.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LogProvider extends ContentProvider {
    private static final String AUTHORITY = "com.halloapp.LogProvider";
    private static final String LOG_FILE_NAME = "logcat.log";
    private static final String DEBUG_SUFFIX = " [DEBUG]";
    private static final int MATCH_CODE = 1;

    private static final Cursor cursor = new AbstractCursor() {
        private int getFileSize() {
            return logcatData.length;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public String[] getColumnNames() {
            return new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE};
        }

        @Override
        public String getString(int column) {
            if (column == 0) {
                return LOG_FILE_NAME;
            } else if (column == 1) {
                return Integer.toString(getFileSize());
            }
            return null;
        }

        @Override
        public short getShort(int column) {
            return 0;
        }

        @Override
        public int getInt(int column) {
            return 0;
        }

        @Override
        public long getLong(int column) {
            return 0;
        }

        @Override
        public float getFloat(int column) {
            return 0;
        }

        @Override
        public double getDouble(int column) {
            return 0;
        }

        @Override
        public boolean isNull(int column) {
            return false;
        }
    };

    private static byte[] logcatData;

    private UriMatcher uriMatcher;

    @Override
    public boolean onCreate() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "*", MATCH_CODE);
        return true;
    }

    @MainThread
    public static void openLogIntent(final Context context) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                fetchLogcat();
                return Me.getInstance(context).getUser();
            }

            @Override
            protected void onPostExecute(String user) {
                super.onPostExecute(user);

                final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {SettingsActivity.SUPPORT_EMAIL});
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, context.getString(R.string.email_logs_subject) + DEBUG_SUFFIX);
                intent.putExtra(android.content.Intent.EXTRA_TEXT, context.getString(R.string.email_logs_text, user) + DEBUG_SUFFIX);
                intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://" + LogProvider.AUTHORITY + "/" + LOG_FILE_NAME));
                context.startActivity(intent);
            }
        }.execute();
    }

    private static void fetchLogcat() {
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            InputStream inputStream = process.getInputStream();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }

            logcatData = baos.toByteArray();
        } catch (IOException e) {
            Log.e("Error with log transfer", e);
        }
    }

    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        switch (uriMatcher.match(uri)) {
            case MATCH_CODE:
                try {
                    // Create pipe so we don't need storage permissions; assumes logs won't be too big for memory, but should be okay for debug
                    ParcelFileDescriptor[] descriptors = ParcelFileDescriptor.createPipe();
                    ParcelFileDescriptor read = descriptors[0];
                    ParcelFileDescriptor write = descriptors[1];

                    OutputStream outputStream = new FileOutputStream(write.getFileDescriptor());
                    outputStream.write(logcatData);
                    outputStream.close();

                    return read;
                } catch (IOException e) {
                    Log.w("Error getting logs", e);
                    throw new FileNotFoundException("IOException getting logs");
                }
            default:
                Log.w("Unsupported uri: '" + uri + "'.");
                throw new FileNotFoundException("Unsupported uri: " + uri.toString());
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "text/plain";
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
