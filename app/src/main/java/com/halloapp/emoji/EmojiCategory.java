package com.halloapp.emoji;

import com.google.gson.JsonArray;

public class EmojiCategory {
    public final String name;
    public final Emoji[] emojis;

    public EmojiCategory(String name, JsonArray jsonArray) {
        this.name = name;
        emojis = new Emoji[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonArray variantList = jsonArray.get(i).getAsJsonArray();
            //TODO: handle variants
            emojis[i] = new Emoji(variantList.get(0).getAsString());
        }
    }

}
