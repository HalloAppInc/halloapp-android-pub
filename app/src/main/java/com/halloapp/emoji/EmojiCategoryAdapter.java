package com.halloapp.emoji;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;

import java.util.List;

public class EmojiCategoryAdapter extends RecyclerView.Adapter<EmojiCategoryAdapter.EmojiCategoryViewHolder> {

    private int selectedIndex;

    private List<EmojiPage> emojiPages;

    public interface OnSelectEmojiCategoryListener {
        void onSelectCategory(int index);
    }

    private OnSelectEmojiCategoryListener listener;

    public EmojiCategoryAdapter() {
    }

    public void setEmojiPages(@NonNull List<EmojiPage> pages) {
        this.emojiPages = pages;
        notifyDataSetChanged();
    }


    public void setOnSelectEmojiCategoryListener(@NonNull OnSelectEmojiCategoryListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public EmojiCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_emoji_category_item, parent, false);
        return new EmojiCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmojiCategoryViewHolder holder, int position) {
        holder.bind(emojiPages.get(position), position, position == selectedIndex);
    }

    @Override
    public int getItemCount() {
        return emojiPages == null ? 0 : emojiPages.size();
    }

    public void selectIndex(int index) {
        if (selectedIndex != index) {
            notifyItemChanged(selectedIndex);
            selectedIndex = index;
            notifyItemChanged(selectedIndex);
        }
    }

    public class EmojiCategoryViewHolder extends RecyclerView.ViewHolder {

        private final ImageView iconView;
        private final View selectedBar;

        private int index;

        public EmojiCategoryViewHolder(@NonNull View itemView) {
            super(itemView);

            selectedBar = itemView.findViewById(R.id.category_underline);
            iconView = itemView.findViewById(R.id.category_icon);

            itemView.setOnClickListener(v -> {
                int oldIndex = selectedIndex;
                selectedIndex = index;
                notifyItemChanged(oldIndex);
                notifyItemChanged(index);
                if (listener != null) {
                    listener.onSelectCategory(selectedIndex);
                }
            });
        }

        public void bind(EmojiPage emojiPage, int index, boolean selected) {
            this.index = index;
            selectedBar.setVisibility(selected ? View.VISIBLE : View.GONE);
            iconView.setSelected(selected);
            iconView.setImageResource(emojiPage.iconRes);
        }
    }
}
