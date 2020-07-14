package com.halloapp.util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtils {


    public static byte[] createFileMD5(@NonNull File file) throws IOException {
        final InputStream fileInputStream =  new FileInputStream(file);
        final byte[] buffer = new byte[1024];
        final MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e("FileUtils: no MD5");
            throw new FileNotFoundException("no MD5");
        }
        int numRead;
        do {
            numRead = fileInputStream.read(buffer);
            if (numRead > 0) {
                messageDigest.update(buffer, 0, numRead);
            }
        }
        while (numRead != -1);
        fileInputStream.close();
        return messageDigest.digest();
    }

    public static String getFileMD5(@NonNull File file) throws IOException {
        final byte [] md5 = createFileMD5(file);
        final StringBuilder result = new StringBuilder();
        for (byte value : md5) {
            result.append(Integer.toString((value & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

    public static void uriToFile(@NonNull Context context, @NonNull Uri uri, @NonNull File file) {
        final ContentResolver cr = context.getContentResolver();
        if (cr != null) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = cr.openInputStream(uri);
                if (inputStream != null) {
                    outputStream = new FileOutputStream(file);
                    byte[] buffer = new byte[4096];
                    int n;
                    while ((n = inputStream.read(buffer, 0, buffer.length)) >= 0) {
                        outputStream.write(buffer, 0, n);
                    }
                }
            } catch (SecurityException | IOException e) {
                Log.e("Unable to open stream", e);
            } finally {
                closeSilently(inputStream);
                closeSilently(outputStream);
            }
        }
    }

    public static String inputStreamToString(@NonNull InputStream inputStream) throws IOException {
        final StringBuilder string = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
            int c;
            while ((c = reader.read()) != -1) {
                string.append((char) c);
            }
        }
        return string.toString();
    }

    public static void closeSilently(@Nullable Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Throwable ignore) {
                // Do nothing
            }
        }
    }

    public static void deleteRecursive(@NonNull File file) {
        if (file.isDirectory()) {
            final File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        if (!file.delete()) {
            Log.e("Unable to delete file " + file.getAbsolutePath());
        }
    }

    public static void copyFile(@NonNull File src, @NonNull File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }

    public static String generateTempMediaFileName(@NonNull Uri uri, @Nullable String suffix) {
        String baseName = Base64.encodeToString(uri.toString().getBytes(), Base64.URL_SAFE);
        return TextUtils.isEmpty(suffix) ? baseName : String.format("%s-%s", baseName, suffix);
    }
}
