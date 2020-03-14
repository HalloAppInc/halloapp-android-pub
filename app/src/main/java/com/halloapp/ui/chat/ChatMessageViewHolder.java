package com.halloapp.ui.chat;

import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.posts.ChatMessage;
import com.halloapp.posts.Media;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.MediaPagerAdapter;
import com.halloapp.ui.ContentViewHolderParent;
import com.halloapp.widget.LimitingTextView;
import com.halloapp.widget.MediaViewPager;

import me.relex.circleindicator.CircleIndicator;

class ChatMessageViewHolder extends ViewHolderWithLifecycle {

    private final LimitingTextView textView;
    private final MediaViewPager mediaPagerView;
    private final CircleIndicator mediaPagerIndicator;
    private final MediaPagerAdapter mediaPagerAdapter;

    private final ChatMessageViewHolderParent parent;
    private ChatMessage message;

    abstract static class ChatMessageViewHolderParent implements MediaPagerAdapter.MediaPagerAdapterParent, ContentViewHolderParent {

    }

    ChatMessageViewHolder(final @NonNull View itemView, @NonNull ChatMessageViewHolderParent parent) {
        super(itemView);

        this.parent = parent;

        textView = itemView.findViewById(R.id.text);
        mediaPagerView = itemView.findViewById(R.id.media_pager);
        mediaPagerIndicator = itemView.findViewById(R.id.media_pager_indicator);

        if (mediaPagerView != null) {
            mediaPagerAdapter = new MediaPagerAdapter(parent);
            mediaPagerView.setAdapter(mediaPagerAdapter);
            mediaPagerView.setPageMargin(itemView.getContext().getResources().getDimensionPixelSize(R.dimen.media_pager_margin));
            mediaPagerView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    if (message == null) {
                        return;
                    }
                    if (position == 0) {
                        parent.getMediaPagerPositionMap().remove(message.rowId);
                    } else {
                        parent.getMediaPagerPositionMap().put(message.rowId, position);
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        } else {
            mediaPagerAdapter = null;
        }
    }

    void bindTo(final @NonNull ChatMessage message) {
        this.message = message;
        //textView.setText(message.timestamp + " " + message.rowId + " " + message.text);
        if (message.media.isEmpty()) {
            textView.setLineLimit(Constants.TEXT_POST_LINE_LIMIT);
        } else {
            mediaPagerView.setMaxAspectRatio(Math.min(Constants.MAX_IMAGE_ASPECT_RATIO, Media.getMaxAspectRatio(message.media)));
            mediaPagerAdapter.setMedia(message.media);
            if (message.media.size() > 1) {
                mediaPagerIndicator.setVisibility(View.VISIBLE);
                mediaPagerIndicator.setViewPager(mediaPagerView);
            } else {
                mediaPagerIndicator.setVisibility(View.GONE);
            }
            final Integer selPos = parent.getMediaPagerPositionMap().get(message.rowId);
            mediaPagerView.setCurrentItem(selPos == null ? 0 : selPos, false);
            textView.setLineLimit(Constants.MEDIA_POST_LINE_LIMIT);
        }
        textView.setLineStep(0);
        textView.setText(message.text);

        if (TextUtils.isEmpty(message.text)) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
        }
    }
}
