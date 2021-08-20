package com.halloapp.ui.chat;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
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

import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Message;
import com.halloapp.id.GroupId;
import com.halloapp.media.UploadMediaTask;
import com.halloapp.media.VoiceNotePlayer;
import com.halloapp.ui.ContentViewHolderParent;
import com.halloapp.ui.MediaPagerAdapter;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.groups.GroupParticipants;
import com.halloapp.util.Rtl;
import com.halloapp.util.StringUtils;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.TimeUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.LimitingTextView;
import com.halloapp.widget.MessageTextLayout;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.Connection;

import me.relex.circleindicator.CircleIndicator3;

public class MessageViewHolder extends ViewHolderWithLifecycle {

    private final View contentView;
    private final ImageView statusView;
    private final TextView dateView;
    private final TextView timestampView;
    private final TextView decryptStatusView;
    private final TextView newMessagesSeparator;
    private final View e2eNoticeView;
    private final TextView nameView;
    private final TextView systemMessage;
    private final TextView tombstoneMessage;
    private final TextView futureProofMessage;
    private final LimitingTextView textView;
    private final MessageTextLayout messageTextLayout;
    private final ViewPager2 mediaPagerView;
    private final CircleIndicator3 mediaPagerIndicator;
    private final MediaPagerAdapter mediaPagerAdapter;
    private @Nullable ReplyContainer replyContainer;
    protected final MessageViewHolderParent parent;

    private final Me me;
    private final Connection connection;
    private final FileStore fileStore;
    private final ContentDb contentDb;
    private final ContactLoader contactLoader;
    private final DecryptStatLoader decryptStatLoader;

    protected Message message;

    abstract static class MessageViewHolderParent implements MediaPagerAdapter.MediaPagerAdapterParent, ContentViewHolderParent {
        abstract void onItemLongClicked(String text, @NonNull Message message);
        abstract long getSelectedMessageRowId();
        abstract long getHighlightedMessageRowId();
        abstract ReplyLoader getReplyLoader();
        abstract void unblockContactFromTap();
        abstract void setReplyMessageMediaIndex(long rowId, int pos);
        abstract void scrollToOriginal(Message replyingMessage);
        abstract void clearHighlight();
        abstract VoiceNotePlayer getVoiceNotePlayer();
    }

    public static @DrawableRes int getStatusImageResource(@Message.State int state) {
        switch (state) {
            case Message.STATE_OUTGOING_SENT:
                return R.drawable.ic_messaging_sent;
            case Message.STATE_OUTGOING_DELIVERED:
                return R.drawable.ic_messaging_delivered;
            case Message.STATE_OUTGOING_PLAYED:
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

        this.me = Me.getInstance();
        this.connection = Connection.getInstance();
        this.fileStore = FileStore.getInstance();
        this.contentDb = ContentDb.getInstance();
        this.contactLoader = new ContactLoader();
        this.decryptStatLoader = new DecryptStatLoader();

        contentView = itemView.findViewById(R.id.content);
        statusView = itemView.findViewById(R.id.status);
        dateView = itemView.findViewById(R.id.date);
        timestampView = itemView.findViewById(R.id.timestamp);
        decryptStatusView = itemView.findViewById(R.id.decrypt_status);
        newMessagesSeparator = itemView.findViewById(R.id.new_messages);
        e2eNoticeView = itemView.findViewById(R.id.e2e_notice);
        if (e2eNoticeView != null) {
            TextView e2eText = e2eNoticeView.findViewById(R.id.e2e_text);
            e2eText.setText(StringUtils.replaceBoldWithMedium(Html.fromHtml(itemView.getContext().getString(R.string.e2e_notice))));
        }
        nameView = itemView.findViewById(R.id.name);
        textView = itemView.findViewById(R.id.text);
        mediaPagerView = itemView.findViewById(R.id.media_pager);
        mediaPagerIndicator = itemView.findViewById(R.id.media_pager_indicator);
        systemMessage = itemView.findViewById(R.id.system_message);
        tombstoneMessage = itemView.findViewById(R.id.tombstone_text);
        futureProofMessage = itemView.findViewById(R.id.future_proof_text);
        messageTextLayout = itemView.findViewById(R.id.message_text_container);

        itemView.setOnLongClickListener(v -> {
            if (parent.getSelectedMessageRowId() == -1) {
                String text = textView == null ? null : textView.getText().toString();
                parent.onItemLongClicked(text, message);
                return true;
            }
            return false;
        });

        if (systemMessage != null) {
            systemMessage.setOnClickListener(v -> {
                if (message.usage == Message.USAGE_BLOCK) {
                    parent.unblockContactFromTap();
                }
            });
        }

        if (tombstoneMessage != null) {
            tombstoneMessage.setMovementMethod(LinkMovementMethod.getInstance());
        }

        if (futureProofMessage != null) {
            SpannableStringBuilder current= new SpannableStringBuilder(futureProofMessage.getText());
            URLSpan[] spans= current.getSpans(0, current.length(), URLSpan.class);

            int linkColor = ContextCompat.getColor(futureProofMessage.getContext(), R.color.color_link);

            for (URLSpan span : spans) {
                int start = current.getSpanStart(span);
                int end = current.getSpanEnd(span);
                current.removeSpan(span);

                ClickableSpan learnMoreSpan = new ClickableSpan() {
                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        ds.setUnderlineText(false);
                        ds.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                        ds.setColor(linkColor);
                    }

                    @Override
                    public void onClick(@NonNull View widget) {
                        try {
                            final Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID));
                            intent.setPackage("com.android.vending");
                            parent.startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Log.i("MessageViewHolder Play Store Not Installed", e);
                            SnackbarHelper.showWarning(futureProofMessage,  R.string.app_expiration_no_play_store);
                        }
                    }
                };
                current.setSpan(learnMoreSpan, start, end, 0);
            }
            futureProofMessage.setText(current);
            futureProofMessage.setMovementMethod(LinkMovementMethod.getInstance());
        }

        if (statusView != null) {
            statusView.setOnClickListener(v -> {
                if (message != null && message.isTransferFailed()) {
                    UploadMediaTask.restartUpload(message, fileStore, contentDb, connection);
                }
            });
        }

        if (mediaPagerView != null) {
            final int defaultMediaInset = mediaPagerView.getResources().getDimensionPixelSize(R.dimen.media_pager_child_padding);
            mediaPagerAdapter = new MediaPagerAdapter(parent, itemView.getContext().getResources().getDimension(R.dimen.message_bubble_corner_radius), 0);
            mediaPagerAdapter.setMediaInset(0, 0, 0, defaultMediaInset);
            mediaPagerAdapter.setOffscreenPlayerLimit(1);
            mediaPagerAdapter.setAllowSaving(true);
            mediaPagerView.setAdapter(mediaPagerAdapter);
            mediaPagerView.setOffscreenPageLimit(1);
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
                    mediaPagerAdapter.refreshPlayers(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        } else {
            mediaPagerAdapter = null;
        }
    }

    Message getMessage() {
        return message;
    }

    private static @DrawableRes int getMessageBackground(@NonNull Message message, boolean mergeWithNext, boolean mergeWithPrev) {
        boolean outgoing = message.isOutgoing();
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

    protected void fillView(@NonNull Message message, boolean changed) {

    }

    void bindTo(@NonNull Message message, int newMessageCountSeparator, @Nullable Message prevMessage, @Nullable Message nextMessage, boolean isLast) {
        boolean changed = !message.equals(this.message);
        this.message = message;

        boolean mergeWithPrev = shouldMergeBubbles(message, prevMessage);
        boolean mergeWithNext = shouldMergeBubbles(message, nextMessage);
        updateBubbleMerging(mergeWithPrev, mergeWithNext);

        if (parent.getSelectedMessageRowId() == message.rowId) {
            itemView.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.color_secondary_40_alpha));
        } else if (parent.getHighlightedMessageRowId() == message.rowId) {
            AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(itemView.getContext(), R.animator.message_highlight);
            set.setTarget(itemView);
            set.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    parent.clearHighlight();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    parent.clearHighlight();
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            set.start();
        } else {
            itemView.setBackgroundColor(0);
        }

        fillView(message, changed);

        if (statusView != null) {
            if (message.isTransferFailed()) {
                statusView.setImageResource(R.drawable.ic_error);
                statusView.setColorFilter(itemView.getContext().getResources().getColor(R.color.design_default_color_error));
            } else {
                statusView.setImageResource(getStatusImageResource(message.state));
                statusView.setColorFilter(0);
            }
        }

        final Integer selPos = parent.getMediaPagerPositionMap().get(message.rowId);
        if (!message.media.isEmpty() && mediaPagerAdapter != null) {
            mediaPagerAdapter.setMedia(message.media);
            if (!message.id.equals(mediaPagerAdapter.getContentId())) {
                mediaPagerAdapter.setChat(message.chatId);
                mediaPagerAdapter.setContentId(message.id);
                if (message.media.size() > 1) {
                    mediaPagerIndicator.setVisibility(View.VISIBLE);
                    mediaPagerIndicator.setViewPager(mediaPagerView);
                } else {
                    mediaPagerIndicator.setVisibility(View.GONE);
                }
                mediaPagerView.setTag(MediaPagerAdapter.getPagerTag(message.id));
                mediaPagerView.setCurrentItem(selPos == null ? (Rtl.isRtl(mediaPagerView.getContext()) ? message.media.size() - 1 : 0) : selPos, false);
                parent.setReplyMessageMediaIndex(message.rowId, selPos == null ? 0 : selPos);
            }
        }

        if (systemMessage != null) {
            contactLoader.cancel(systemMessage);
            parent.getSystemMessageTextResolver().bindSystemMessagePostPreview(systemMessage, message);
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
            boolean emojisOnly = StringUtils.isFewEmoji(message.text);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textView.getResources().getDimension(emojisOnly ? R.dimen.message_text_size_few_emoji : R.dimen.message_text_size));
            parent.getTextContentLoader().load(textView, message);

            if (messageTextLayout != null) {
                messageTextLayout.setForceSeparateLine(emojisOnly);
            }

            if (TextUtils.isEmpty(message.text)) {
                textView.setText(message.text);
                textView.setVisibility(View.GONE);
            } else {
                textView.setVisibility(View.VISIBLE);
                textView.setTextColor(ContextCompat.getColor(textView.getContext(), message.isIncoming() ? R.color.message_text_incoming : R.color.message_text_outgoing));
            }
        }

        if (timestampView != null) {
            timestampView.setText(TimeFormatter.formatMessageTime(timestampView.getContext(), message.timestamp));
        }

        if (decryptStatusView != null) {
            decryptStatLoader.load(decryptStatusView, message.id);
        }

        if (dateView != null) {
            if (prevMessage == null || !TimeUtils.isSameDay(message.timestamp, prevMessage.timestamp)) {
                dateView.setVisibility(View.VISIBLE);
                dateView.setText(TimeFormatter.formatMessageSeparatorDate(dateView.getContext(), message.timestamp));
            } else {
                dateView.setVisibility(View.GONE);
            }
        }

        if (newMessagesSeparator != null) {
            if (newMessageCountSeparator > 0) {
                newMessagesSeparator.setVisibility(View.VISIBLE);
                newMessagesSeparator.setText(newMessagesSeparator.getContext().getResources().getQuantityString(R.plurals.new_messages_separator, newMessageCountSeparator, newMessageCountSeparator));
            } else {
                newMessagesSeparator.setVisibility(View.GONE);
            }
        }

        if (e2eNoticeView != null) {
            if (!isLast) {
                e2eNoticeView.setVisibility(View.GONE);
            } else {
                e2eNoticeView.setVisibility(View.VISIBLE);
                e2eNoticeView.setOnClickListener(v -> {
                    Uri uri = Uri.parse(Constants.ENCRYPTED_CHAT_BLOG_URL);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    parent.startActivity(intent);
                });
            }
        }

        if (nameView != null) {
            if (message.isOutgoing()) {
                contactLoader.cancel(nameView);
                nameView.setVisibility(View.GONE);
            } else if ((prevMessage == null || !prevMessage.senderUserId.equals(message.senderUserId) || prevMessage.isLocalMessage()) && message.chatId instanceof GroupId) {
                contactLoader.load(nameView, message.senderUserId);
                int color = GroupParticipants.getParticipantNameColor(nameView.getContext(), message.senderUserId);
                nameView.setTextColor(color);
                nameView.setVisibility(View.VISIBLE);
            } else {
                nameView.setVisibility(View.GONE);
            }
        }

        if (contentView !=null) {
            if (message.replyPostId != null || message.replyMessageId != null) {
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
