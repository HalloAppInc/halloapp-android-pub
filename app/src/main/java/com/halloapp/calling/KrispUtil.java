package com.halloapp.calling;

import android.content.Context;

import com.google.common.io.ByteStreams;
import com.halloapp.AppContext;
import com.halloapp.util.logs.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileOutputStream;

public class KrispUtil {
    private static final String RESOURCE_DIR = "resources";
    private static final String WTS_FILENAME = "krisp.kw";
    private static final String RESOURCE_NAME = "krisp";
    private static final String RESOURCE_TYPE = "raw";

    private static String dirPath() {
        return AppContext.getInstance().get().getFilesDir() + File.separator + RESOURCE_DIR;
    }

    public static String filePath() {
        return dirPath() + File.separator + WTS_FILENAME;
    }

    public static void initializeResources() {
        final String filePath = filePath();
        try {
            if (new File(filePath).exists()) {
                Log.i("Krisp file path: " + filePath + " found.");
                return;
            }
            new File(dirPath()).mkdir();
            Context context = AppContext.getInstance().get();
            final int krispId = context.getResources().getIdentifier(RESOURCE_NAME, RESOURCE_TYPE, context.getPackageName());
            InputStream inStream = context.getResources().openRawResource(krispId);
            final byte data[] = ByteStreams.toByteArray(inStream);
            FileOutputStream out = new FileOutputStream(filePath);
            Log.i("Krisp Read: " + data.length + " bytes, wrote to: " + filePath);
            out.write(data);
            out.close();
        } catch(IOException e) {
            Log.e("Failed to read krisp weights file from: " + filePath, e);
        }
    }

    public static boolean isResourcePresent() {
        return new File(filePath()).exists();
    }
}
