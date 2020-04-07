package com.halloapp.ui.chat;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.content.Chat;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.ui.ContentViewHolderParent;
import com.halloapp.ui.MediaPagerAdapter;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.util.Rtl;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.TimeUtils;
import com.halloapp.widget.LimitingTextView;
import com.halloapp.widget.MediaViewPager;

import me.relex.circleindicator.CircleIndicator;

class MessageViewHolder extends ViewHolderWithLifecycle {

    private final ImageView statusView;
    private final TextView dateView;
    private final TextView newMessagesSeparator;
    private final LimitingTextView textView;
    private final MediaViewPager mediaPagerView;
    private final CircleIndicator mediaPagerIndicator;
    private final MediaPagerAdapter mediaPagerAdapter;

    private final MessageViewHolderParent parent;
    private Message message;

    abstract static class MessageViewHolderParent implements MediaPagerAdapter.MediaPagerAdapterParent, ContentViewHolderParent {
    }

    MessageViewHolder(@NonNull View itemView, @NonNull MessageViewHolderParent parent) {
        super(itemView);

        this.parent = parent;

        statusView = itemView.findViewById(R.id.status);
        dateView = itemView.findViewById(R.id.date);
        newMessagesSeparator = itemView.findViewById(R.id.new_messages);
        textView = itemView.findViewById(R.id.text);
        mediaPagerView = itemView.findViewById(R.id.media_pager);
        mediaPagerIndicator = itemView.findViewById(R.id.media_pager_indicator);

        if (mediaPagerView != null) {
            mediaPagerAdapter = new MediaPagerAdapter(parent, itemView.getContext().getResources().getDimension(R.dimen.message_media_radius));
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

    void bindTo(@NonNull Message message, @Nullable Chat chat, @Nullable Message prevMessage) {
        this.message = message;

        if (statusView != null) {
            if (message.seen == Message.SEEN_YES) {
                statusView.setImageResource(R.drawable.ic_messaging_seen);
            } else if (message.transferred == Message.TRANSFERRED_DESTINATION) {
                statusView.setImageResource(R.drawable.ic_messaging_delivered);
            } else if (message.transferred == Message.TRANSFERRED_SERVER) {
                statusView.setImageResource(R.drawable.ic_messaging_sent);
            } else {
                statusView.setImageResource(R.drawable.ic_messaging_clock);
            }
        }

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
            mediaPagerView.setCurrentItem(selPos == null ? (Rtl.isRtl(mediaPagerView.getContext()) ? message.media.size() - 1 : 0) : selPos, false);
            textView.setLineLimit(Constants.MEDIA_POST_LINE_LIMIT);
        }
        textView.setLineStep(0);
        textView.setText(message.text);

        if (TextUtils.isEmpty(message.text)) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setTextColor(ContextCompat.getColor(textView.getContext(), message.isIncoming() ? R.color.message_text_incoming : R.color.message_text_outgoing));
        }

        if (prevMessage == null || !TimeUtils.isSameDay(message.timestamp, prevMessage.timestamp)) {
            dateView.setVisibility(View.VISIBLE);
            dateView.setText(TimeFormatter.formatMessageSeparatorDate(dateView.getContext(), message.timestamp));
        } else {
            dateView.setVisibility(View.GONE);
        }

        if (chat != null && chat.firstUnseenMessageRowId == message.rowId) {
            newMessagesSeparator.setVisibility(View.VISIBLE);
            newMessagesSeparator.setText(newMessagesSeparator.getContext().getResources().getQuantityString(R.plurals.new_messages_separator, chat.newMessageCount, chat.newMessageCount));
        } else {
            newMessagesSeparator.setVisibility(View.GONE);
        }
    }
}
