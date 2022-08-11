package com.halloapp.autodownload;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.halloapp.FileStore;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class ManifestHelper {

    private final FileStore fileStore;

    public ManifestHelper(FileStore fileStore) {
        this.fileStore = fileStore;
    }

    public List<DownloadableAsset> load() throws IOException{
        List<DownloadableAsset> assets = new ArrayList<>();
        FileInputStream inputStream = null;
        InputStreamReader reader = null;
        try{
            File file = fileStore.getDownloadableAssetsFile(DownloadableAssetManager.getManifestFilePath());
            if (!file.exists()) {
                Log.e("ManifestHelper/load manifest file doesn't exist");
                return assets;
            }
            inputStream = new FileInputStream(file);
            reader = new InputStreamReader(inputStream, "Utf-8");
            assets = parse(reader);
        } catch (IOException e) {
            Log.e("ManifestHelper/load fail to load manifest file", e);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (reader != null) {
                reader.close();
            }
            return assets;
        }
    }

    private List<DownloadableAsset> parse(Reader reader) {
        List<DownloadableAsset> assets = new ArrayList<>();
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonArray jsonObjects = parser.parse(reader).getAsJsonArray();
        for(JsonElement obj : jsonObjects ) {
            DownloadableAsset asset = gson.fromJson(obj, DownloadableAsset.class);
            assets.add(asset);
        }
        return assets;
    }

}
