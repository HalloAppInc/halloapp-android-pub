package com.halloapp.content;

import androidx.annotation.NonNull;

public class ExternalShareInfo {
    public final String shareId;
    public final String shareKey;

    public ExternalShareInfo(@NonNull String shareId, @NonNull String shareKey) {
        this.shareId = shareId;
        this.shareKey = shareKey;
    }
}
