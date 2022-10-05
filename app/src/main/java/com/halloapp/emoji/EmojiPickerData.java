package com.halloapp.emoji;

import androidx.annotation.WorkerThread;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.halloapp.AppContext;
import com.halloapp.FileStore;
import com.halloapp.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class EmojiPickerData {

    public final EmojiCategory[] categories;
    public final String fontHash;

    @WorkerThread
    public static EmojiPickerData parse() throws IOException {
        try (InputStream is = AppContext.getInstance().get().getResources().openRawResource(R.raw.emoji_picker)) {
            return parse(is);
        }
    }

    @WorkerThread
    public static EmojiPickerData parse(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            return parse(is);
        } catch (Exception e) {
            if (!(e instanceof IOException)) {
                throw new IOException(e);
            }
            throw e;
        }
    }

    @WorkerThread
    private static EmojiPickerData parse(InputStream is) throws IOException {
        List<EmojiCategory> categories = new ArrayList<>();
        String hash;
        try (Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            JsonElement parser = JsonParser.parseReader(reader);
            JsonObject root = parser.getAsJsonObject();
            hash = root.get("font_hash").getAsString();
            JsonObject pages = root.get("pages").getAsJsonObject();

            for (String category : pages.keySet()) {
                categories.add(new EmojiCategory(category, pages.get(category).getAsJsonArray()));
            }
        }

        return new EmojiPickerData(hash, categories);
    }

    public int getCategoryCount() {
        return categories.length;
    }

    private EmojiPickerData(String fontHash, List<EmojiCategory> categories) {
        this.categories = categories.toArray(new EmojiCategory[0]);
        this.fontHash = fontHash;
    }
}
