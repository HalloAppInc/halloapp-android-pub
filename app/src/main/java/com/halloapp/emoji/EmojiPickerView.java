package com.halloapp.emoji;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.halloapp.R;
import com.halloapp.util.ContextUtils;
import com.halloapp.util.Preconditions;

import java.util.ArrayList;

public class EmojiPickerView extends LinearLayoutCompat {

    private final EmojiManager emojiManager = EmojiManager.getInstance();
    private final RecentEmojiManager recentEmojiManager = RecentEmojiManager.getInstance();

    private RecyclerView footerRv;
    private ViewPager emojiViewPager;

    private EmojiCategoryAdapter emojiCategoryAdapter;
    private EmojiPagerAdapter emojiPagerAdapter;

    private EditText editText;
    private View backspaceView;

    private RecentEmojiPage recentEmojiPage;

    public EmojiPickerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public EmojiPickerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EmojiPickerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void bindEditText(EditText editText) {
        this.editText = editText;
    }

    private void insertEmoji(@NonNull Emoji emoji) {
        if (editText != null) {
            recentEmojiManager.onEmoji(emoji);
            final int start = editText.getSelectionStart();
            final int end = editText.getSelectionEnd();

            if (start < 0) {
                editText.append(emoji.getUnicode());
            } else {
                editText.getText().replace(Math.min(start, end), Math.max(start, end), emoji.getUnicode(), 0, emoji.getUnicode().length());
            }
        }
    }

    private void init(@NonNull Context context) {
        setOrientation(VERTICAL);
        inflate(context, R.layout.view_emoji_picker, this);

        backspaceView = findViewById(R.id.emoji_backspace);
        emojiViewPager = findViewById(R.id.emoji_view_pager);
        footerRv = findViewById(R.id.category_recycler);
        footerRv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

        emojiPagerAdapter = new EmojiPagerAdapter();
        emojiCategoryAdapter = new EmojiCategoryAdapter();
        emojiPagerAdapter.setOnEmojiSelectListener(this::insertEmoji);

        recentEmojiPage = new RecentEmojiPage(R.drawable.ic_recent_emoji, recentEmojiManager.getRecentEmojiList());

        LifecycleOwner owner = (LifecycleOwner) Preconditions.checkNotNull(ContextUtils.getActivity(context));
        emojiManager.getEmojiPickerLiveData().observe(owner, emojiData -> {
            ArrayList<EmojiPage> pages = new ArrayList<>();
            pages.add(recentEmojiPage);
            for (EmojiCategory category : emojiData.categories) {
                EmojiPage page = new EmojiPage(EmojiPage.getDrawable(category.name), category.emojis);
                pages.add(page);
            }
            emojiPagerAdapter.setEmojiPages(pages);
            emojiCategoryAdapter.setEmojiPages(pages);
        });

        recentEmojiManager.getRecentEmojiList().observe(owner, recents -> {
            int recentIndex = emojiPagerAdapter.getPageIndex(recentEmojiPage);
            if (recentIndex != emojiViewPager.getCurrentItem()) {
                emojiPagerAdapter.refresh(recentIndex);
            }
        });


        backspaceView.setOnClickListener(v -> {
            if (editText != null) {
                final KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                editText.dispatchKeyEvent(event);
            }
        });
        emojiCategoryAdapter.setOnSelectEmojiCategoryListener(index -> {
            emojiViewPager.setCurrentItem(index, true);
        });
        emojiViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                emojiCategoryAdapter.selectIndex(position);
                footerRv.smoothScrollToPosition(position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        emojiViewPager.setAdapter(emojiPagerAdapter);
        footerRv.setAdapter(emojiCategoryAdapter);
    }
}
