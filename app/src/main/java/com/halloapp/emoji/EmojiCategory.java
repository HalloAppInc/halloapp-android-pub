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
            String baseUnicode = variantList.get(0).getAsString();
            if (variantList.size() > 1) {
                EmojiWithVariants emojiWithVariants = new EmojiWithVariants(baseUnicode);
                for (int j = 1; j < variantList.size(); j++) {
                    emojiWithVariants.addVariant(variantList.get(j).getAsString());
                }
                emojis[i] = emojiWithVariants;
            } else {
                emojis[i] = new Emoji(baseUnicode);
            }
        }
    }

}
