package com.halloapp.autodownload;

import androidx.annotation.NonNull;

import java.util.Map;

public class DownloadableAsset{

    private final String key;
    private String checksum;
    private String filename;

    private static final String ASSET_CHECKSUM = "checksum";
    private static final String ASSET_FILENAME = "filename";

    public DownloadableAsset(@NonNull String key, @NonNull Map<String, String> defaultValueMap) {
        this.key = key;
        this.checksum = defaultValueMap.get(ASSET_CHECKSUM);
        this.filename = defaultValueMap.get(ASSET_FILENAME);

    }

    @NonNull
    public String getKey() {
        return key;
    }

    @NonNull
    public String getChecksum() { return checksum; }

    @NonNull
    public String getFilename() { return filename; }

    private void parse(@NonNull Map<String, String> valueMap) {
        this.checksum = valueMap.get(ASSET_CHECKSUM);
        this.filename = valueMap.get(ASSET_FILENAME);
    }

    @Override
    public String toString() {
        return "{key: " + getKey() + ", checksum: " + checksum + ", filename: " + filename + "}";
    }

}
