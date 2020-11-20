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
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Message;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.media.UploadMediaTask;
import com.halloapp.ui.ContentViewHolderParent;
import com.halloapp.ui.MediaPagerAdapter;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.groups.GroupParticipants;
import com.halloapp.util.ListFormatter;
import com.halloapp.util.Preconditions;
import com.halloapp.util.Rtl;
import com.halloapp.util.StringUtils;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.TimeUtils;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.LimitingTextView;
import com.halloapp.widget.MessageTextLayout;
import com.halloapp.xmpp.Connection;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class MessageViewHolder extends ViewHolderWithLifecycle {

    private final View contentView;
    private final ImageView statusView;
    private final TextView dateView;
    private final TextView timestampView;
    private final TextView newMessagesSeparator;
    private final TextView nameView;
    private final TextView systemMessage;
    private final LimitingTextView textView;
    private final MessageTextLayout messageTextLayout;
    private final ViewPager2 mediaPagerView;
    private final CircleIndicator3 mediaPagerIndicator;
    private final MediaPagerAdapter mediaPagerAdapter;
    private @Nullable ReplyContainer replyContainer;
    private final MessageViewHolderParent parent;

    private final Me me;
    private final Connection connection;
    private final FileStore fileStore;
    private final ContentDb contentDb;
    private final ContactLoader contactLoader;

    private Message message;

    abstract static class MessageViewHolderParent implements MediaPagerAdapter.MediaPagerAdapterParent, ContentViewHolderParent {
        abstract void onItemLongClicked(String text, long messageRowId);
        abstract long getSelectedMessageRowId();
        abstract ReplyLoader getReplyLoader();
        abstract void unblockContactFromTap();
        abstract void setReplyMessageMediaIndex(long rowId, int pos);
        abstract void scrollToOriginal(Message replyingMessage);
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

        this.me = Me.getInstance();
        this.connection = Connection.getInstance();
        this.fileStore = FileStore.getInstance();
        this.contentDb = ContentDb.getInstance();
        this.contactLoader = new ContactLoader();

        contentView = itemView.findViewById(R.id.content);
        statusView = itemView.findViewById(R.id.status);
        dateView = itemView.findViewById(R.id.date);
        timestampView = itemView.findViewById(R.id.timestamp);
        newMessagesSeparator = itemView.findViewById(R.id.new_messages);
        nameView = itemView.findViewById(R.id.name);
        textView = itemView.findViewById(R.id.text);
        mediaPagerView = itemView.findViewById(R.id.media_pager);
        mediaPagerIndicator = itemView.findViewById(R.id.media_pager_indicator);
        systemMessage = itemView.findViewById(R.id.system_message);
        messageTextLayout = itemView.findViewById(R.id.message_text_container);

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
            final int defaultMediaInset = mediaPagerView.getResources().getDimensionPixelSize(R.dimen.media_pager_child_padding);
            mediaPagerAdapter = new MediaPagerAdapter(parent, itemView.getContext().getResources().getDimension(R.dimen.message_bubble_corner_radius));
            mediaPagerAdapter.setMediaInset(0, 0, 0, defaultMediaInset);
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
            mediaPagerAdapter.setContentId(message.id);
            if (message.media.size() > 1) {
                mediaPagerIndicator.setVisibility(View.VISIBLE);
                mediaPagerIndicator.setViewPager(mediaPagerView);
            } else {
                mediaPagerIndicator.setVisibility(View.GONE);
            }
            final Integer selPos = parent.getMediaPagerPositionMap().get(message.rowId);
            mediaPagerView.setTag(MediaPagerAdapter.getPagerTag(message.id));
            mediaPagerView.setCurrentItem(selPos == null ? (Rtl.isRtl(mediaPagerView.getContext()) ? message.media.size() - 1 : 0) : selPos, false);
            parent.setReplyMessageMediaIndex(message.rowId, selPos == null ? 0 : selPos);
        }

        if (systemMessage != null) {
            switch (message.usage) {
                case Message.USAGE_BLOCK: {
                    contactLoader.cancel(systemMessage);
                    systemMessage.setText(R.string.block_notification);
                    break;
                }
                case Message.USAGE_UNBLOCK: {
                    contactLoader.cancel(systemMessage);
                    systemMessage.setText(R.string.unblock_notification);
                    break;
                }
                case Message.USAGE_CREATE_GROUP: {
                    systemMessageSingleUser(message, R.string.system_message_group_created);
                    break;
                }
                case Message.USAGE_ADD_MEMBERS: {
                    systemMessageAffectedList(message, R.string.system_message_members_added);
                    break;
                }
                case Message.USAGE_REMOVE_MEMBER: {
                    systemMessageAffectedList(message, R.string.system_message_members_removed);
                    break;
                }
                case Message.USAGE_MEMBER_LEFT: {
                    systemMessageSingleUser(message, R.string.system_message_member_left);
                    break;
                }
                case Message.USAGE_PROMOTE: {
                    systemMessageAffectedList(message, R.string.system_message_members_promoted);
                    break;
                }
                case Message.USAGE_DEMOTE: {
                    systemMessageAffectedList(message, R.string.system_message_members_demoted);
                    break;
                }
                case Message.USAGE_AUTO_PROMOTE: {
                    systemMessageSingleUser(message, R.string.system_message_member_auto_promoted);
                    break;
                }
                case Message.USAGE_NAME_CHANGE: {
                    if (message.senderUserId.rawId().equals(me.user.getValue())) {
                        systemMessage.setText(itemView.getContext().getString(R.string.system_message_group_name_changed, itemView.getContext().getString(R.string.you), message.text));
                    } else {
                        contactLoader.load(systemMessage, message.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
                            @Override
                            public void showResult(@NonNull TextView view, @Nullable Contact result) {
                                if (result != null) {
                                    systemMessage.setText(itemView.getContext().getString(R.string.system_message_group_name_changed, result.getDisplayName(), message.text));
                                }
                            }

                            @Override
                            public void showLoading(@NonNull TextView view) {
                                systemMessage.setText("");
                            }
                        });
                    }
                    break;
                }
                case Message.USAGE_AVATAR_CHANGE: {
                    systemMessageSingleUser(message, R.string.system_message_group_avatar_changed);
                    break;
                }
                case Message.USAGE_GROUP_DELETED: {
                    systemMessageSingleUser(message, R.string.system_message_group_deleted);
                    break;
                }
                case Message.USAGE_CHAT:
                default: {
                    Log.w("Unrecognized system message usage " + message.usage);
                }
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
            boolean emojisOnly = StringUtils.isFewEmoji(message.text);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textView.getResources().getDimension(emojisOnly ? R.dimen.message_text_size_few_emoji : R.dimen.message_text_size));
            parent.getTextContentLoader().load(textView, message);

            if (messageTextLayout != null) {
                messageTextLayout.setForceSeparateLine(emojisOnly);
            }

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

    private void systemMessageSingleUser(@NonNull Message message, @StringRes int string) {
        if (message.senderUserId.isMe() || message.senderUserId.rawId().equals(me.user.getValue())) {
            systemMessage.setText(itemView.getContext().getString(string, itemView.getContext().getString(R.string.you)));
        } else {
            contactLoader.load(systemMessage, message.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
                @Override
                public void showResult(@NonNull TextView view, @Nullable Contact result) {
                    if (result != null) {
                        systemMessage.setText(itemView.getContext().getString(string, result.getDisplayName()));
                    }
                }

                @Override
                public void showLoading(@NonNull TextView view) {
                    systemMessage.setText("");
                }
            });
        }
    }

    private void systemMessageAffectedList(@NonNull Message message, @StringRes int string) {
        String commaSeparatedMembers = message.text;
        if (commaSeparatedMembers == null) {
            Log.w("MessageViewHolder system message of type " + message.usage + " missing affected list " + message);
            return;
        }
        String[] parts = commaSeparatedMembers.split(",");
        List<UserId> userIds = new ArrayList<>();
        userIds.add(message.senderUserId);
        for (String part : parts) {
            userIds.add(new UserId(part));
        }
        contactLoader.loadMultiple(systemMessage, userIds, new ViewDataLoader.Displayer<TextView, List<Contact>>() {
            @Override
            public void showResult(@NonNull TextView view, @Nullable List<Contact> result) {
                if (result != null) {
                    Contact sender = result.get(0);
                    String senderName = Preconditions.checkNotNull(sender.userId).rawId().equals(me.user.getValue()) ? itemView.getContext().getString(R.string.me) : sender.getDisplayName();
                    List<String> names = new ArrayList<>();
                    for (int i=1; i<result.size(); i++) {
                        Contact contact = result.get(i);
                        names.add(Preconditions.checkNotNull(contact.userId).rawId().equals(me.user.getValue()) ? systemMessage.getResources().getString(R.string.me) : contact.getDisplayName());
                    }
                    String formatted = ListFormatter.format(itemView.getContext(), names);
                    systemMessage.setText(itemView.getContext().getString(string, senderName, formatted));
                }
            }

            @Override
            public void showLoading(@NonNull TextView view) {
                systemMessage.setText("");
            }
        });
    }
}
