package com.halloapp.emoji;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;

import java.util.List;

public class EmojiVariantPopupWindow extends PopupWindow {

    private final EmojiAdapter emojiAdapter;

    private OnEmojiSelectListener emojiSelectListener;

    public EmojiVariantPopupWindow(Context context) {
        super(context);

        View root = LayoutInflater.from(context).inflate(R.layout.popup_emoji_variant_chooser, null, false);
        setContentView(root);
        setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setOutsideTouchable(true);
        setFocusable(true);

        RecyclerView variantRecyclerView = root.findViewById(R.id.variant_rv);
        variantRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        emojiAdapter = new EmojiAdapter(null);
        variantRecyclerView.setAdapter(emojiAdapter);
    }

    public void setEmojiSelectListener(@Nullable OnEmojiSelectListener emojiSelectListener) {
        this.emojiSelectListener = emojiSelectListener;
    }

    public void show(@NonNull View anchor, @NonNull EmojiWithVariants emojiWithVariants) {
        View contentView = getContentView();
        emojiAdapter.setEmojis(emojiWithVariants.getVariants());
        contentView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec. UNSPECIFIED));
        showAsDropDown(anchor, (contentView.getPaddingRight() + contentView.getPaddingLeft() + anchor.getWidth() - contentView.getMeasuredWidth()) / 2 , -contentView.getMeasuredHeight() -anchor.getHeight());
    }

    private class EmojiViewHolder extends RecyclerView.ViewHolder {

        private final TextView emojiView;

        private Emoji emoji;

        public EmojiViewHolder(@NonNull View itemView) {
            super(itemView);

            emojiView = itemView.findViewById(R.id.emoji);
            itemView.setOnClickListener(v -> {
                if (emojiSelectListener != null) {
                    emojiSelectListener.onEmojiSelected(emoji);
                }
            });
        }

        public void bind(Emoji emoji) {
            this.emoji = emoji;
            emojiView.setText(emoji.getUnicode());
        }
    }

    private class EmojiAdapter extends RecyclerView.Adapter<EmojiViewHolder> {

        private List<Emoji> emojis;

        public EmojiAdapter(List<Emoji> emojis) {
            this.emojis = emojis;
        }

        public void setEmojis(List<Emoji> emojis) {
            this.emojis = emojis;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public EmojiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.emoji_variant_item, parent, false);

            return new EmojiViewHolder(layout);
        }

        @Override
        public void onBindViewHolder(@NonNull EmojiViewHolder holder, int position) {
            holder.bind(emojis.get(position));
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return emojis == null ? 0 : emojis.size();
        }
    }

}
