package com.halloapp;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ApkHasher {

    private static ApkHasher instance;

    public static ApkHasher getInstance() {
        if (instance == null) {
            synchronized (ApkHasher.class) {
                if (instance == null) {
                    instance = new ApkHasher();
                }
            }
        }
        return instance;
    }

    private String hash = null;

    public void run(@NonNull Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            String apkPath = applicationInfo.sourceDir;
            File apkFile = new File(apkPath);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int c;
            InputStream is = new FileInputStream(apkFile);
            while ((c = is.read(buffer)) > 0) {
                digest.update(buffer, 0, c);
            }
            hash = StringUtils.bytesToHexString(digest.digest());
            Log.i("HalloApp digest: " + hash);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Could not find own package", e);
        } catch (IOException e) {
            Log.e("Could not open file", e);
        } catch (NoSuchAlgorithmException e) {
            Log.e("Digest unavailable", e);
        }
    }

    public String get() {
        return hash;
    }
}
