package com.halloapp.emoji;

import androidx.annotation.NonNull;

import com.halloapp.util.logs.Log;

import java.util.LinkedList;
import java.util.List;

public class EmojiWithVariants extends Emoji {

    private List<Emoji> variants;

    private int index;

    private Emoji emoji;

    public EmojiWithVariants(String unicode) {
        super(unicode);
        variants = new LinkedList<>();
        variants.add(new Emoji(unicode));

        this.emoji = variants.get(0);
    }

    @Override
    public String getUnicode() {
        return emoji.getUnicode();
    }

    public void addVariant(String unicode) {
        variants.add(new Emoji(unicode));
    }

    public int getIndex() {
        return index;
    }

    public void switchToVariant(@NonNull Emoji variant) {
        int i = variants.indexOf(variant);
        if (i != -1) {
            setVariantIndex(i);
        }
    }

    public List<Emoji> getVariants() {
        return variants;
    }

    public String getBaseUnicode() {
        return super.getUnicode();
    }

    public void setVariantIndex(int index) {
        if (this.index != index) {
            this.index = index;
            if (index < 0 || index >= variants.size()) {
                Log.e("invalid emoji variant index=" + index + " for emoji=" + super.getUnicode());
                this.index = 0;
            }
            this.emoji = variants.get(this.index);
        }
    }

}
