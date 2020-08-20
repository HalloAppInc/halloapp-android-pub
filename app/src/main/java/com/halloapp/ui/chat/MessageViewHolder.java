package com.halloapp.ui.chat;

import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.R;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Message;
import com.halloapp.media.UploadMediaTask;
import com.halloapp.ui.ContentViewHolderParent;
import com.halloapp.ui.MediaPagerAdapter;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.util.Rtl;
import com.halloapp.util.StringUtils;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.TimeUtils;
import com.halloapp.widget.LimitingTextView;
import com.halloapp.xmpp.Connection;

import me.relex.circleindicator.CircleIndicator3;

public class MessageViewHolder extends ViewHolderWithLifecycle {

    private final View contentView;
    private final ImageView statusView;
    private final TextView dateView;
    private final TextView timestampView;
    private final TextView newMessagesSeparator;
    private final TextView systemMessage;
    private final LimitingTextView textView;
    private final ViewPager2 mediaPagerView;
    private final CircleIndicator3 mediaPagerIndicator;
    private final MediaPagerAdapter mediaPagerAdapter;
    private @Nullable ReplyContainer replyContainer;
    private final MessageViewHolderParent parent;
    private final Connection connection;
    private final FileStore fileStore;
    private final ContentDb contentDb;
    private Message message;

    abstract static class MessageViewHolderParent implements MediaPagerAdapter.MediaPagerAdapterParent, ContentViewHolderParent {
        abstract void onItemLongClicked(String text, long messageRowId);
        abstract long getSelectedMessageRowId();
        abstract ReplyLoader getReplyLoader();
        abstract void unblockContactFromTap();
    }

    public static @DrawableRes int getStatusImageResource(@Message.State int state) {
        switch (state) {
            case Message.STATE_OUTGOING_SENT:
                return R.drawable.ic_messaging_sent;
            case Message.STATE_OUTGOING_DELIVERED:
                return R.drawable.ic_messaging_delivered;
            case Message.STATE_OUTGOING_SEEN:
                return R.drawable.ic_messaging_seen;
            case Message.STATE_INITIAL:
            default:
                return R.drawable.ic_messaging_clock;
        }
    }

    MessageViewHolder(@NonNull View itemView, @NonNull MessageViewHolderParent parent) {
        super(itemView);

        this.parent = parent;

        this.connection = Connection.getInstance();
        this.fileStore = FileStore.getInstance(itemView.getContext());
        this.contentDb = ContentDb.getInstance(itemView.getContext());

        contentView = itemView.findViewById(R.id.content);
        statusView = itemView.findViewById(R.id.status);
        dateView = itemView.findViewById(R.id.date);
        timestampView = itemView.findViewById(R.id.timestamp);
        newMessagesSeparator = itemView.findViewById(R.id.new_messages);
        textView = itemView.findViewById(R.id.text);
        mediaPagerView = itemView.findViewById(R.id.media_pager);
        mediaPagerIndicator = itemView.findViewById(R.id.media_pager_indicator);
        systemMessage = itemView.findViewById(R.id.system_message);

        if (textView != null) {
            textView.setOnLongClickListener(v -> {
                if (parent.getSelectedMessageRowId() == -1) {
                    parent.onItemLongClicked(textView.getText().toString(), message.rowId);
                }
                return true;
            });
        }

        if (contentView != null) {
            contentView.setOnLongClickListener(v -> {
                if (parent.getSelectedMessageRowId() == -1 && textView != null) {
                    parent.onItemLongClicked(textView.getText().toString(), message.rowId);
                }
                return true;
            });
        }

        if (systemMessage != null) {
            systemMessage.setOnClickListener(v -> {
                if (message.usage == Message.USAGE_BLOCK) {
                    parent.unblockContactFromTap();
                }
            });
        }

        if (statusView != null) {
            statusView.setOnClickListener(v -> {
                if (message != null && message.isTransferFailed()) {
                    UploadMediaTask.restartUpload(message, fileStore, contentDb, connection);
                }
            });
        }

        if (mediaPagerView != null) {
            mediaPagerAdapter = new MediaPagerAdapter(parent, itemView.getContext().getResources().getDimension(R.dimen.message_media_radius));
            mediaPagerView.setAdapter(mediaPagerAdapter);
            mediaPagerView.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
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

    private static @DrawableRes int getMessageBackground(@NonNull Message message, boolean mergeWithNext, boolean mergeWithPrev) {
        boolean outgoing = message.isOutgoing();
        // Disable corner changing for now till we can polish them later
        /*if (mergeWithNext && mergeWithPrev) {
            return outgoing ? R.drawable.message_background_outgoing_mid : R.drawable.message_background_incoming_mid;
        }
        if (mergeWithNext) {
            return outgoing ? R.drawable.message_background_outgoing_start : R.drawable.message_background_incoming_start;
        }
        if (mergeWithPrev) {
            return outgoing ? R.drawable.message_background_outgoing_end : R.drawable.message_background_incoming_end;
        }*/
        return outgoing ? R.drawable.message_background_outgoing : R.drawable.message_background_incoming;
    }

    private static boolean shouldMergeBubbles(@Nullable Message msg1, @Nullable Message msg2) {
        if (msg1 == null || msg2 == null) {
            return false;
        }
        return msg1.isOutgoing() == msg2.isOutgoing();
    }

    private void updateBubbleMerging(boolean mergeWithPrev, boolean mergeWithNext) {
        if (contentView == null) {
            return;
        }
        contentView.setBackgroundResource(getMessageBackground(message, mergeWithNext, mergeWithPrev));
        View contentParent = (contentView.getParent() instanceof View) ? (View) contentView.getParent() : null;
        if (contentParent != null) {
            contentParent.setPadding(
                    contentParent.getPaddingLeft(),
                    contentParent.getResources().getDimensionPixelSize(mergeWithPrev ? R.dimen.message_vertical_separator_sequence : R.dimen.message_vertical_separator),
                    contentParent.getPaddingRight(),
                    contentParent.getPaddingBottom());
        }
    }

    void bindTo(@NonNull Message message, int newMessageCountSeparator, @Nullable Message prevMessage, @Nullable Message nextMessage) {
        this.message = message;

        boolean mergeWithPrev = shouldMergeBubbles(message, prevMessage);
        boolean mergeWithNext = shouldMergeBubbles(message, nextMessage);
        updateBubbleMerging(mergeWithPrev, mergeWithNext);

        if (parent.getSelectedMessageRowId() == message.rowId) {
            itemView.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.color_secondary_40_alpha));
        } else {
            itemView.setBackgroundColor(0);
        }

        if (statusView != null) {
            if (message.isTransferFailed()) {
                statusView.setImageResource(R.drawable.ic_error);
                statusView.setColorFilter(itemView.getContext().getResources().getColor(R.color.design_default_color_error));
            } else {
                statusView.setImageResource(getStatusImageResource(message.state));
                statusView.setColorFilter(0);
            }
        }

        if (!message.media.isEmpty() && mediaPagerAdapter != null) {
            mediaPagerAdapter.setMedia(message.media);
            if (message.media.size() > 1) {
                mediaPagerIndicator.setVisibility(View.VISIBLE);
                mediaPagerIndicator.setViewPager(mediaPagerView);
            } else {
                mediaPagerIndicator.setVisibility(View.GONE);
            }
            final Integer selPos = parent.getMediaPagerPositionMap().get(message.rowId);
            mediaPagerView.setCurrentItem(selPos == null ? (Rtl.isRtl(mediaPagerView.getContext()) ? message.media.size() - 1 : 0) : selPos, false);
        }

        if (systemMessage != null) {
            if (message.usage == Message.USAGE_BLOCK) {
                systemMessage.setText(R.string.block_notification);
            } else if (message.usage == Message.USAGE_UNBLOCK) {
                systemMessage.setText(R.string.unblock_notification);
            }
        }

        if (textView != null) {
            final Integer textLimit = parent.getTextLimits().get(message.rowId);
            textView.setLineLimit(textLimit != null ? textLimit :
                    (message.media.isEmpty() ? Constants.TEXT_POST_LINE_LIMIT : Constants.MEDIA_POST_LINE_LIMIT));
            textView.setLineLimitTolerance(textLimit != null ? Constants.POST_LINE_LIMIT_TOLERANCE : 0);
            textView.setOnReadMoreListener((view, limit) -> {
                parent.getTextLimits().put(message.rowId, limit);
                return false;
            });
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textView.getResources().getDimension(StringUtils.isFewEmoji(message.text) ? R.dimen.message_text_size_few_emoji : R.dimen.message_text_size));
            textView.setText(message.text);

            if (TextUtils.isEmpty(message.text)) {
                textView.setVisibility(View.GONE);
            } else {
                textView.setVisibility(View.VISIBLE);
                textView.setTextColor(ContextCompat.getColor(textView.getContext(), message.isIncoming() ? R.color.message_text_incoming : R.color.message_text_outgoing));
            }
        }

        if (timestampView != null) {
            timestampView.setText(TimeFormatter.formatMessageTime(timestampView.getContext(), message.timestamp));
        }

        if (dateView != null) {
            if (prevMessage == null || !TimeUtils.isSameDay(message.timestamp, prevMessage.timestamp)) {
                dateView.setVisibility(View.VISIBLE);
                dateView.setText(TimeFormatter.formatMessageSeparatorDate(dateView.getContext(), message.timestamp));
            } else {
                dateView.setVisibility(View.GONE);
            }
        }

        if (newMessagesSeparator !=null) {
            if (newMessageCountSeparator > 0) {
                newMessagesSeparator.setVisibility(View.VISIBLE);
                newMessagesSeparator.setText(newMessagesSeparator.getContext().getResources().getQuantityString(R.plurals.new_messages_separator, newMessageCountSeparator, newMessageCountSeparator));
            } else {
                newMessagesSeparator.setVisibility(View.GONE);
            }
        }

        if (contentView !=null) {
            if (message.replyPostId != null) {
                if (replyContainer == null) {
                    final ViewGroup replyContainerView = itemView.findViewById(R.id.reply_container);
                    if (replyContainerView != null) {
                        replyContainer = new ReplyContainer(LayoutInflater.from(replyContainerView.getContext()).inflate(R.layout.message_item_reply_content, replyContainerView), parent);
                        replyContainer.bindTo(message);
                        replyContainer.show();
                        contentView.setMinimumWidth(itemView.getResources().getDimensionPixelSize(R.dimen.reply_min_width));
                    }
                } else {
                    replyContainer.bindTo(message);
                    replyContainer.show();
                    contentView.setMinimumWidth(itemView.getResources().getDimensionPixelSize(R.dimen.reply_min_width));
                }
            } else if (replyContainer != null) {
                replyContainer.hide();
                contentView.setMinimumWidth(0);
            }
        }
    }
}
