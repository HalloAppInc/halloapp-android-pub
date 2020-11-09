package com.halloapp.ui.chat;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Outline;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.id.UserId;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.ui.MediaPagerAdapter;
import com.halloapp.ui.PostContentActivity;
import com.halloapp.ui.groups.GroupParticipants;
import com.halloapp.util.ViewDataLoader;

class ReplyContainer {

    private final View containerView;
    private final TextView nameView;
    private final TextView textView;
    private final ImageView mediaThumbView;
    private final ImageView mediaIconView;
    private final MessageViewHolder.MessageViewHolderParent parent;

    private static final int REPLY_NAME_ALPHA = 0x9A;

    ReplyContainer(@NonNull View containerView, @NonNull MessageViewHolder.MessageViewHolderParent parent) {
        this.containerView = containerView;
        this.parent = parent;
        nameView = containerView.findViewById(R.id.reply_name);
        textView = containerView.findViewById(R.id.reply_text);
        mediaThumbView = containerView.findViewById(R.id.reply_media_thumb);
        mediaThumbView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), mediaThumbView.getContext().getResources().getDimension(R.dimen.reply_media_corner_radius));
            }
        });
        mediaThumbView.setClipToOutline(true);
        mediaIconView = containerView.findViewById(R.id.reply_media_icon);
    }

    void show() {
        containerView.setVisibility(View.VISIBLE);
    }

    void hide() {
        containerView.setVisibility(View.GONE);
    }

    void bindTo(@NonNull Message message) {
        containerView.setOnClickListener(v -> {
            if (message.replyMessageId != null) {
                 parent.scrollToOriginal(message);
            } else if (message.replyPostId != null) {
                final Intent intent = new Intent(containerView.getContext(), PostContentActivity.class);
                intent.putExtra(PostContentActivity.EXTRA_POST_SENDER_USER_ID, message.isIncoming() ? UserId.ME : message.chatId);
                intent.putExtra(PostContentActivity.EXTRA_POST_ID, message.replyPostId);
                intent.putExtra(PostContentActivity.EXTRA_POST_MEDIA_INDEX, message.replyPostMediaIndex);
                if (containerView.getContext() instanceof Activity) {
                    final ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) (containerView.getContext()), mediaThumbView, mediaThumbView.getTransitionName());
                    parent.startActivity(intent, options);
                } else {
                    parent.startActivity(intent);
                }
            }
        });
        if (message.replyPostId != null) {
            mediaThumbView.setTransitionName(MediaPagerAdapter.getTransitionName(message.replyPostId, message.replyPostMediaIndex));
        } else {
            mediaThumbView.setTransitionName(MediaPagerAdapter.getTransitionName(message.replyMessageId, message.replyMessageMediaIndex));
        }
        containerView.setBackgroundResource(R.drawable.reply_frame_background);
        containerView.setBackgroundTintList(ColorStateList.valueOf(GroupParticipants.getParticipantReplyBgColor(containerView.getContext(), message.replyMessageSenderId)));
        parent.getReplyLoader().load(containerView, message, new ViewDataLoader.Displayer<View, ReplyLoader.Result>() {
            @Override
            public void showResult(@NonNull View view, @Nullable ReplyLoader.Result result) {
                if (result != null) {
                    nameView.setTextColor(ColorUtils.setAlphaComponent(GroupParticipants.getParticipantNameColor(nameView.getContext(), message.replyMessageSenderId), REPLY_NAME_ALPHA));
                    nameView.setText(result.name);
                    textView.setTypeface(textView.getTypeface(), Typeface.NORMAL);
                    if (result.mentions != null && !result.mentions.isEmpty()) {
                        parent.getTextContentLoader().load(textView, result);
                    } else if (result.text == null && result.thumb == null) {
                        textView.setText(R.string.reply_original_not_found);
                        textView.setTypeface(textView.getTypeface(), Typeface.ITALIC);
                    } else {
                        textView.setText(result.text);
                    }
                    switch (result.mediaType) {
                        case Media.MEDIA_TYPE_IMAGE: {
                            mediaIconView.setVisibility(View.VISIBLE);
                            mediaIconView.setImageResource(R.drawable.ic_camera);
                            if (TextUtils.isEmpty(result.text)) {
                                textView.setText(R.string.photo);
                            }
                            textView.setMaxLines(1);
                            break;
                        }
                        case Media.MEDIA_TYPE_VIDEO: {
                            mediaIconView.setVisibility(View.VISIBLE);
                            mediaIconView.setImageResource(R.drawable.ic_video);
                            if (TextUtils.isEmpty(result.text)) {
                                textView.setText(R.string.video);
                            }
                            textView.setMaxLines(1);
                            break;
                        }
                        case Media.MEDIA_TYPE_UNKNOWN:
                        default: {
                            mediaIconView.setVisibility(View.GONE);
                            textView.setMaxLines(2);
                            break;
                        }
                    }
                    if (result.thumb != null) {
                        mediaThumbView.setVisibility(View.VISIBLE);
                        mediaThumbView.setImageBitmap(result.thumb);
                    } else {
                        mediaThumbView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void showLoading(@NonNull View view) {
                nameView.setText("");
                textView.setText("");
                mediaIconView.setVisibility(View.GONE);
                mediaThumbView.setImageBitmap(null);
            }
        });
    }
}
