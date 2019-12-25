package com.halloapp.util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LoadUriTask extends AsyncTask<Void, Void, File> {

    final Uri uri;
    final File file;
    protected Context context;

    public LoadUriTask(Context context, final @NonNull Uri uri, final @NonNull File file) {
        this.uri = uri;
        this.file = file;
        this.context = context.getApplicationContext();
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