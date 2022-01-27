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

public class EmojiCategoryAdapter extends RecyclerView.Adapter<EmojiCategoryAdapter.EmojiCategoryViewHolder> {

    private EmojiPickerData emojiPickerData;

    private int selectedIndex;

    public interface OnSelectEmojiCategoryListener {
        void onSelectCategory(int index);
    }

    private OnSelectEmojiCategoryListener listener;

    public EmojiCategoryAdapter() {
    }

    public void setEmojiPickerData(@Nullable EmojiPickerData emojiPickerData) {
        this.emojiPickerData = emojiPickerData;
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
        holder.bind(emojiPickerData.categories[position].name, position, position == selectedIndex);
    }

    @Override
    public int getItemCount() {
        return emojiPickerData == null ? 0 : emojiPickerData.getCategoryCount();
    }

    public void selectCategory(String category) {
        for (int i = 0; i < emojiPickerData.categories.length; i++) {
            if (category.equals(emojiPickerData.categories[i].name)) {
                if (i != selectedIndex) {
                    int oldIndex = selectedIndex;
                    selectedIndex = i;
                    notifyItemChanged(oldIndex);
                    notifyItemChanged(selectedIndex);
                }
            }
        }
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

        private String category;
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

        public void bind(String category, int index, boolean selected) {
            this.category = category;
            this.index = index;
            selectedBar.setVisibility(selected ? View.VISIBLE : View.GONE);
            iconView.setSelected(selected);
            @DrawableRes int iconRes = R.drawable.ic_emoji_smilies;
            switch (category) {
                case "people":
                    iconRes = R.drawable.ic_emoji_smilies;
                    break;
                case "nature":
                    iconRes = R.drawable.ic_emoji_nature;
                    break;
                case "foods":
                    iconRes = R.drawable.ic_emoji_food;
                    break;
                case "places":
                    iconRes = R.drawable.ic_emoji_places;
                    break;
                case "activity":
                    iconRes = R.drawable.ic_emoji_events;
                    break;
                case "objects":
                    iconRes = R.drawable.ic_emoji_objects;
                    break;
                case "symbols":
                    iconRes = R.drawable.ic_emoji_symbols;
                    break;
                case "flags":
                    iconRes = R.drawable.ic_emoji_flag;
                    break;
            }
            iconView.setImageResource(iconRes);
        }
    }
}
