package com.halloapp.emoji;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

public class EmojiPagerAdapter extends PagerAdapter {

    private EmojiPickerData emojiPickerData;

    public EmojiPagerAdapter() {
    }

    public void setEmojiPickerData(@Nullable EmojiPickerData emojiPickerData) {
        this.emojiPickerData = emojiPickerData;
        notifyDataSetChanged();
    }

    private OnEmojiSelectListener listener;

    private final OnEmojiSelectListener proxyListener = emoji -> {
        if (listener != null) {
            listener.onEmojiSelected(emoji);
        }
    };

    public void setOnEmojiSelectListener(@Nullable OnEmojiSelectListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return emojiPickerData == null ? 0 : emojiPickerData.categories.length;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        EmojiGridView gridView = new EmojiGridView(container.getContext());
        gridView.setOnEmojiSelectionListener(proxyListener);
        container.addView(gridView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        gridView.bindEmojis(emojiPickerData.categories[position]);
        return gridView;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
