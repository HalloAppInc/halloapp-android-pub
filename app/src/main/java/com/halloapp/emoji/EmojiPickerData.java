package com.halloapp.emoji;

import androidx.annotation.WorkerThread;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.halloapp.AppContext;
import com.halloapp.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class EmojiPickerData {

    public final EmojiCategory[] categories;

    @WorkerThread
    public static EmojiPickerData parse() throws IOException {
        List<EmojiCategory> categories = new ArrayList<>();
        InputStream is = AppContext.getInstance().get().getResources().openRawResource(R.raw.emoji_picker);
        try (Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            JsonElement parser = JsonParser.parseReader(reader);
            JsonObject pages = parser.getAsJsonObject().get("pages").getAsJsonObject();

            for (String category : pages.keySet()) {
                categories.add(new EmojiCategory(category, pages.get(category).getAsJsonArray()));
            }
        }

        return new EmojiPickerData(categories);
    }

    public int getCategoryCount() {
        return categories.length;
    }

    private EmojiPickerData(List<EmojiCategory> categories) {
        this.categories = categories.toArray(new EmojiCategory[0]);
    }
}
