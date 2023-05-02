package com.halloapp.calling.calling;

import com.halloapp.AppContext;
import com.halloapp.util.logs.Log;
import java.io.File;

public class KrispUtil {
    private static final String RESOURCE_DIR = "resources";
    private static final String OLD_WTS_FILENAME = "krisp.kw";
    private static final String WTS_FILENAME = "krisp1.kw";

    private static String dirPath() {
        return AppContext.getInstance().get().getFilesDir() + File.separator + RESOURCE_DIR;
    }

    public static String filePath() {
        return dirPath() + File.separator + WTS_FILENAME;
    }

    private static String oldFilePath() { return dirPath() + File.separator + OLD_WTS_FILENAME; }

    public static void initializeResources() {
        final String filePath = filePath();
        final String oldFilePath = oldFilePath();
        File oldFile = new File(oldFilePath);
        if (oldFile.exists()) {
            Log.i("Krisp old file path: " + filePath + " found, deleting.");
            oldFile.delete();
        }

        File newFile = new File(filePath);
        if (newFile.exists()) {
            Log.i("Krisp file path: " + filePath + " found.");
            newFile.delete();
        }
    }
}
