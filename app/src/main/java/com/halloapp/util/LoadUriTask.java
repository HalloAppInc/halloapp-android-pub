package com.halloapp.util;

import android.app.Application;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import java.io.File;

public class LoadUriTask extends AsyncTask<Void, Void, File> {

    private final Uri uri;
    private final File file;
    protected final Application context;

    public LoadUriTask(final @NonNull Application context, final @NonNull Uri uri, final @NonNull File file) {
        this.uri = uri;
        this.file = file;
        this.context = context;
    }

    @Override
    protected File doInBackground(Void... voids) {
        FileUtils.uriToFile(context, uri, file);
        return file;
    }

    @Override
    protected void onPostExecute(final File file) {
    }
}