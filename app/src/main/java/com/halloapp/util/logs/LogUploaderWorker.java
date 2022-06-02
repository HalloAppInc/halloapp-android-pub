package com.halloapp.util.logs;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.Me;
import com.halloapp.util.RandomId;
import com.halloapp.util.ThreadUtils;
import com.halloapp.xmpp.Connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LogUploaderWorker extends Worker {

    private static final String LOG_UPLOAD_WORK_ID = "log-upload";

    private static final String WORKER_PARAM_ACK_ID = "ack-id";

    private final Me me = Me.getInstance();
    private final FileStore fileStore = FileStore.getInstance();
    private final LogManager logManager = LogManager.getInstance();

    public static void uploadLogs(@NonNull Context context) {
        uploadLogs(context, null);
    }

    public static void uploadLogs(@NonNull Context context, @Nullable String ackId) {
        final OneTimeWorkRequest.Builder workRequest = new OneTimeWorkRequest.Builder(LogUploaderWorker.class);
        if (ackId != null) {
            workRequest.setInputData(new Data.Builder().putString(WORKER_PARAM_ACK_ID, ackId).build());
        }
        WorkManager.getInstance(context).enqueueUniqueWork(LOG_UPLOAD_WORK_ID, ExistingWorkPolicy.KEEP, workRequest.build());
    }

    public LogUploaderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Uri endpoint;
        try {
            endpoint = getLogsEndpoint();
        } catch (UnsupportedEncodingException e) {
            return Result.failure();
        }
        if (endpoint == null) {
            return Result.failure();
        }
        File file = fileStore.getTmpFile(RandomId.create() + ".zip");
        logManager.zipLocalLogs(getApplicationContext(), file);
        try {
            int code = upload(file, endpoint.toString());
            Log.i("LogUploaderWorker/doWork got response code " + code);
        } catch (IOException e) {
            Log.e("LogUploaderWorker/doWork failed to upload logs", e);
            return Result.failure();
        }
        Data data = getInputData();
        String ackId = data.getString(WORKER_PARAM_ACK_ID);
        if (!TextUtils.isEmpty(ackId)) {
            Connection.getInstance().sendAck(ackId);
        }
        return Result.success();
    }

    @Nullable
    private Uri getLogsEndpoint() throws UnsupportedEncodingException {
        String uid;
        String phoneNumber;
        try {
             uid = URLEncoder.encode(me.getUser(), "UTF-8");
             phoneNumber = URLEncoder.encode(me.getPhone(), "UTF-8");
        } catch (NullPointerException e) {
            Log.w("Failed to get uid and phoneNumber for log upload; using placeholders", e);
            uid = "NOUID";
            phoneNumber = "NOPHONE";
        }
        String version = URLEncoder.encode("Android" + BuildConfig.VERSION_NAME, "UTF-8");

        if (TextUtils.isEmpty(uid) || TextUtils.isEmpty(phoneNumber)) {
            Log.e("LogUploaderWorker/getLogsEndpoint phone number or uid is null");
            return null;
        }

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("api.halloapp.net")
                .path("api/logs/device")
                .appendQueryParameter("uid", uid)
                .appendQueryParameter("phone", phoneNumber)
                .appendQueryParameter("version", version);
        return builder.build();
    }

    @WorkerThread
    public int upload(@NonNull File file, @NonNull String url) throws IOException {
        ThreadUtils.setSocketTag();

        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", Constants.USER_AGENT);
        connection.setRequestProperty("Content-Type", "application/zip");
        connection.setAllowUserInteraction(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setConnectTimeout(30_000);
        connection.setReadTimeout(30_000);

        OutputStream out = connection.getOutputStream();
        final InputStream in = new FileInputStream(file);
        final int bufferSize = 1024;
        final byte[] bytes = new byte[bufferSize];
        boolean cancelled = false;
        while (!cancelled) {
            final int count = in.read(bytes, 0, bufferSize);
            if (count == -1) {
                break;
            }
            out.write(bytes, 0, count);
            cancelled = isStopped();
        }
        out.close();
        in.close();

        return connection.getResponseCode();
    }
}
