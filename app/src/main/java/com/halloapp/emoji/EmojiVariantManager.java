package com.halloapp.emoji;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.halloapp.Preferences;
import com.halloapp.util.logs.Log;

import java.util.HashMap;

public class EmojiVariantManager {

    private static EmojiVariantManager instance;

    public static EmojiVariantManager getInstance() {
        if (instance == null) {
            synchronized (EmojiVariantManager.class) {
                if (instance == null) {
                    instance = new EmojiVariantManager(Preferences.getInstance());
                }
            }
        }
        return instance;
    }

    private final Preferences preferences;

    private VariantMapping variantMapping;

    private EmojiVariantManager(Preferences preferences) {
        this.preferences = preferences;
    }

    public void init() {
        variantMapping = loadEmojiVariants();
    }

    public int getVariantIndex(@NonNull String baseEmojiUnicode) {
        Integer index = null;
        if (variantMapping.containsKey(baseEmojiUnicode)) {
            index = variantMapping.get(baseEmojiUnicode);
        }
        return index == null ? 0 : index;
    }

    public void setVariantIndex(@NonNull String baseEmoji, int variantIndex) {
        variantMapping.put(baseEmoji, variantIndex);
        saveVariants();
    }

    public void updateVariant(@NonNull EmojiWithVariants emoji) {
        variantMapping.put(emoji.getBaseUnicode(), emoji.getIndex());
        saveVariants();
    }

    @NonNull
    private VariantMapping loadEmojiVariants() {
        Gson gson = new Gson();
        VariantMapping va = gson.fromJson(preferences.getEmojiVariants(), VariantMapping.class);
        if (va == null) {
            va = new VariantMapping();
        }
        return va;
    }

    private void saveVariants() {
        Gson gson = new Gson();
        String json = gson.toJson(variantMapping);
        Log.i("EmojiVariantManager/saveEmojiVariants");
        preferences.setEmojiVariants(json);
    }

    private static class VariantMapping extends HashMap<String, Integer> {
        public VariantMapping() {}
    }
}
