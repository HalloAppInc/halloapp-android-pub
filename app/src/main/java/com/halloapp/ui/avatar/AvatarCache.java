package com.halloapp.ui.avatar;

import android.graphics.Bitmap;

public class AvatarCache {

    private static AvatarCache instance;

    public static AvatarCache getInstance() {
        if (instance == null) {
            synchronized (AvatarCache.class) {
                if (instance == null) {
                    instance = new AvatarCache();
                }
            }
        }
        return instance;
    }

    // TODO(jack): Cache not yet implemented, all checks are misses
    public Bitmap getAvatarFor(String hash) {
        return null;
    }
}
