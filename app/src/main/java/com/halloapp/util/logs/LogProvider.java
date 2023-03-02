package com.halloapp.util.logs;

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
import android.provider.Settings;
import android.text.format.DateFormat;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.goterl.lazysodium.interfaces.Sign;
import com.halloapp.AppContext;
import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;

public class LogProvider extends ContentProvider {
    public static final String LOG_ZIP_NAME = "logs.zip";

    private static final String AUTHORITY = BuildConfig.IS_KATCHUP ? "com.halloapp.katchup.util.logs.LogProvider" : "com.halloapp.util.logs.LogProvider";
    private static final String LOG_FILE_NAME = "logcat.log";
    private static final String DEBUG_SUFFIX = " [DEBUG]";
    private static final int MATCH_LOGCAT = 1;
    private static final int MATCH_CRASHLYTICS = 2;
    private static final int EMAIL_LOCAL_PART_MAX_LENGTH = 64;

    private final AppContext appContext = AppContext.getInstance();

    private static byte[] logcatData;

    private UriMatcher uriMatcher;

    @Override
    public boolean onCreate() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, LOG_FILE_NAME, MATCH_LOGCAT);
        uriMatcher.addURI(AUTHORITY, LOG_ZIP_NAME, MATCH_CRASHLYTICS);
        return true;
    }

    @MainThread
    public static LiveData<Intent> openLogIntent(final Context context) {
        return openLogIntent(context, null);
    }

    @MainThread
    public static LiveData<Intent> openLogIntent(final Context context, @Nullable String contentId) {
        if (BuildConfig.DEBUG) {
            return LogProvider.openDebugLogcatIntent(context, contentId);
        } else {
            return LogProvider.openEmailLogIntent(context, contentId);
        }
    }

    @MainThread
    private static LiveData<Intent> openEmailLogIntent(final Context context, @Nullable String contentId) {
        MutableLiveData<Intent> ret = new MutableLiveData<>();
        if ("true".equals(Settings.System.getString(context.getContentResolver(), "firebase.test.lab"))) {
            Log.i("Skipping log upload in Firebase test lab");
        } else {
            LogUploaderWorker.uploadLogs(context);
        }
        BgWorkers.getInstance().execute(() -> {
            byte[] fullRegNoiseKey = Me.getInstance().getMyRegEd25519NoiseKey();
            String regNoiseKey = fullRegNoiseKey == null ? null : StringUtils.bytesToHexString(Arrays.copyOfRange(fullRegNoiseKey, 0, Sign.ED25519_PUBLICKEYBYTES));
            File file = new File(context.getExternalCacheDir(), LogProvider.LOG_ZIP_NAME);
            LogManager.getInstance().zipLocalLogs(context, file);
            String user = Me.getInstance().getUser() + "-" + Me.getInstance().getPhone();
            String text = context.getString(R.string.email_logs_text, user, BuildConfig.VERSION_NAME)
                    + (contentId == null ? "" : "\ncontentId: " + contentId)
                    + "\nregNoiseKey: " + regNoiseKey;

            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("application/zip");
            intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {getSupportEmail()});
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, context.getString(BuildConfig.IS_KATCHUP ? R.string.katchup_email_logs_subject : R.string.email_logs_subject, BuildConfig.VERSION_NAME, getTimestamp()));
            intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://" + LogProvider.AUTHORITY + "/" + LOG_ZIP_NAME));
            ret.postValue(intent);
        });
        return ret;
    }

    @MainThread
    private static LiveData<Intent> openDebugLogcatIntent(final Context context, @Nullable String contentId) {
        MutableLiveData<Intent> ret = new MutableLiveData<>();
        BgWorkers.getInstance().execute(() -> {
            fetchLogcat();
            byte[] fullRegNoiseKey = Me.getInstance().getMyRegEd25519NoiseKey();
            String regNoiseKey = fullRegNoiseKey == null ? null : StringUtils.bytesToHexString(Arrays.copyOfRange(fullRegNoiseKey, 0, Sign.ED25519_PUBLICKEYBYTES));
            String user = Me.getInstance().getUser();
            String text = context.getString(R.string.email_logs_text, user, BuildConfig.VERSION_NAME) + DEBUG_SUFFIX
                    + (contentId == null ? "" : "\ncontentId: " + contentId)
                    + "\nregNoiseKey: " + regNoiseKey;

            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("plain/text");
            intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {getSupportEmail()});
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, context.getString(BuildConfig.IS_KATCHUP ? R.string.katchup_email_logs_subject : R.string.email_logs_subject, BuildConfig.VERSION_NAME, getTimestamp()) + DEBUG_SUFFIX);
            intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://" + LogProvider.AUTHORITY + "/" + LOG_FILE_NAME));
            ret.postValue(intent);
        });
        return ret;
    }

    public static String getSupportEmail() {
        String localPart = Constants.SUPPORT_EMAIL_LOCAL_PART
                + "+" + Me.getInstance().getUser()
                + "-" + Me.getInstance().getPhone()
                + "+v" + BuildConfig.VERSION_NAME;
        if (localPart.length() > EMAIL_LOCAL_PART_MAX_LENGTH) {
            Log.w("Support email local part " + localPart + " exceeded max length; using untagged email address");
            return Constants.SUPPORT_EMAIL;
        }
        return localPart + "@" + Constants.SUPPORT_EMAIL_DOMAIN;
    }

    public static CharSequence getTimestamp() {
        Context context = AppContext.getInstance().get();
        Date date = new Date();
        return DateFormat.getDateFormat(context).format(date) + " " + DateFormat.getTimeFormat(context).format(date);
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
            case MATCH_LOGCAT:
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
            case MATCH_CRASHLYTICS:
                return ParcelFileDescriptor.open(new File(appContext.get().getExternalCacheDir(), LOG_ZIP_NAME), ParcelFileDescriptor.MODE_READ_ONLY);
            default:
                Log.w("Unsupported uri: '" + uri + "'.");
                throw new FileNotFoundException("Unsupported uri: " + uri.toString());
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (uriMatcher.match(uri)) {
            case MATCH_CRASHLYTICS: {
                return "application/zip";
            }
            default: {
                return "text/plain";
            }
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (uriMatcher.match(uri)) {
            case MATCH_CRASHLYTICS: {
                if (getContext() == null) {
                    return null;
                }
                File zipFile = new File(getContext().getExternalCacheDir(), LOG_ZIP_NAME);
                return new AbstractCursor() {

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
                            return LOG_ZIP_NAME;
                        } else if (column == 1) {
                            return Long.toString(zipFile.length());
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
            }
            default: {
                return new AbstractCursor() {
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
            }
        }
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
