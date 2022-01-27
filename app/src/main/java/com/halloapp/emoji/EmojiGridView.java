package com.halloapp.emoji;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;

public class EmojiGridView extends RecyclerView {

    private GridLayoutManager layoutManager;

    private int columnWidth;

    private OnEmojiSelectListener listener;

    private int columnCount = 8;

    public EmojiGridView(@NonNull Context context) {
        this(context, null);
    }

    public EmojiGridView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmojiGridView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = r - l;
        int columns = width / columnWidth;

        if (columns != columnCount) {
            layoutManager.setSpanCount(columns);
            columnCount = columns;
        }
        super.onLayout(changed, l, t, r, b);
    }

    public void setOnEmojiSelectionListener(OnEmojiSelectListener listener) {
        this.listener = listener;
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        columnWidth = getResources().getDimensionPixelSize(R.dimen.emoji_column_width);
        layoutManager = new GridLayoutManager(context, columnCount);

        setLayoutManager(layoutManager);
    }

    public void bindEmojis(EmojiCategory emojiCategory) {
        setAdapter(new EmojiAdapter(emojiCategory));
    }

    private class EmojiViewHolder extends RecyclerView.ViewHolder {

        private final TextView emojiView;

        private Emoji emoji;

        public EmojiViewHolder(@NonNull View itemView) {
            super(itemView);

            emojiView = itemView.findViewById(R.id.emoji);
            itemView.setOnClickListener(v -> {
                if (emoji != null && listener != null) {
                    listener.onEmojiSelected(emoji);
                }
            });
        }

        public void bind(Emoji emoji) {
            this.emoji = emoji;
            emojiView.setText(emoji.getUnicode());
        }
    }

    private class EmojiAdapter extends RecyclerView.Adapter<EmojiViewHolder> {

        private final Emoji[] emojis;

        public EmojiAdapter(EmojiCategory emojiCategory) {
            emojis = emojiCategory.emojis;
        }

        @NonNull
        @Override
        public EmojiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_emoji_picker_item, parent, false);

            return new EmojiViewHolder(layout);
        }

        @Override
        public void onBindViewHolder(@NonNull EmojiViewHolder holder, int position) {
            holder.bind(emojis[position]);
        }

        @Override
        public int getItemCount() {
            return emojis == null ? 0 : emojis.length;
        }
    }
}
