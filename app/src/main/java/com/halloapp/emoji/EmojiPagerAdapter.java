package com.halloapp.emoji;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import java.util.HashMap;
import java.util.List;

public class EmojiPagerAdapter extends PagerAdapter {

    private List<EmojiPage> emojiPages;

    private final HashMap<Integer, EmojiGridView> emojiGrids = new HashMap<>();

    public EmojiPagerAdapter() {
    }

    public void setEmojiPages(@Nullable List<EmojiPage> pages) {
        this.emojiPages = pages;
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
        return emojiPages == null ? 0 : emojiPages.size();
    }

    public int getPageIndex(@NonNull EmojiPage emojiPage) {
        for (int i = 0; i < emojiPages.size(); i++) {
            EmojiPage page = emojiPages.get(i);
            if (page == emojiPage) {
                return i;
            }
        }
        return -1;
    }

    public void refresh(int index) {
        if (emojiPages == null) {
            return;
        }
        EmojiGridView gridView = emojiGrids.get(index);
        if (gridView != null && gridView.getAdapter() != null) {
            gridView.refresh();
        }
    }



    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        EmojiGridView gridView = new EmojiGridView(container.getContext());
        gridView.setOnEmojiSelectionListener(proxyListener);
        container.addView(gridView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        gridView.bindEmojis(emojiPages.get(position));
        emojiGrids.put(position, gridView);
        return gridView;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, @NonNull Object view) {
        collection.removeView((View) view);
        emojiGrids.remove(position);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
