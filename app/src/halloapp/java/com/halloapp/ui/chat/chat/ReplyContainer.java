package com.halloapp.ui.chat.chat;

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

import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.ui.MediaPagerAdapter;
import com.halloapp.ui.PostContentActivity;
import com.halloapp.util.StringUtils;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;

class ReplyContainer {

    private final View containerView;
    private final TextView nameView;
    private final TextView textView;
    private final ImageView mediaThumbView;
    private final ImageView mediaIconView;
    private final MessageViewHolder.MessageViewHolderParent parent;

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
                final Intent intent = PostContentActivity.open(v.getContext(), message.replyPostId, message.replyPostMediaIndex);
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
        containerView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(containerView.getContext(),
                message.isOutgoing()
                        ? R.color.message_background_reply_outgoing
                        : R.color.message_background_reply_incoming)));
        parent.getReplyLoader().load(containerView, message, new ViewDataLoader.Displayer<View, ReplyLoader.Result>() {
            @Override
            public void showResult(@NonNull View view, @Nullable ReplyLoader.Result result) {
                boolean isPost = message.replyPostId != null;
                if (result != null) {
                    nameView.setTextColor(ContextCompat.getColor(nameView.getContext(), R.color.secondary_text));
                    nameView.setText(result.name);
                    textView.setTypeface(textView.getTypeface(), Typeface.NORMAL);
                    if (result.text == null && result.thumb == null) {
                        textView.setText(isPost ? R.string.reply_original_post_not_found : R.string.reply_original_not_found);
                        textView.setTypeface(textView.getTypeface(), Typeface.ITALIC);
                    } else {
                        parent.getTextContentLoader().load(textView, result);
                    }
                    boolean showThumbnailIfPresent = true;
                    switch (result.mediaType) {
                        case Media.MEDIA_TYPE_IMAGE: {
                            mediaIconView.setVisibility(View.VISIBLE);
                            mediaIconView.setImageResource(R.drawable.ic_camera);
                            if (TextUtils.isEmpty(result.text)) {
                                if (result.postType != null && (result.postType == Post.TYPE_MOMENT || result.postType == Post.TYPE_MOMENT_PSA || result.postType == Post.TYPE_RETRACTED_MOMENT)) {
                                    if (result.thumb == null || result.postType == Post.TYPE_RETRACTED_MOMENT || message.replyMessageSenderId == null) {
                                        mediaIconView.setVisibility(View.GONE);
                                        textView.setText(R.string.expired_moment);
                                        showThumbnailIfPresent = false;
                                    } else {
                                        textView.setText(R.string.moment);
                                    }
                                } else {
                                    textView.setText(R.string.photo);
                                }
                            }
                            textView.setMaxLines(1);
                            break;
                        }
                        case Media.MEDIA_TYPE_DOCUMENT: {
                            mediaIconView.setVisibility(View.VISIBLE);
                            mediaIconView.setImageResource(R.drawable.ic_document);
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
                        case Media.MEDIA_TYPE_AUDIO: {
                            mediaIconView.setVisibility(View.VISIBLE);
                            mediaIconView.setImageResource(R.drawable.ic_keyboard_voice);
                            Long duration = null;
                            if (result.text != null) {
                                try {
                                    duration = Long.parseLong(result.text);
                                } catch (NumberFormatException e) {
                                    Log.e("ReplyContainer invalid duration " + result.text);
                                }
                            }
                            if (duration != null) {
                                textView.setText(textView.getContext().getString(
                                        isPost ? R.string.audio_post_preview : R.string.voice_note_preview,
                                        StringUtils.formatVoiceNoteDuration(textView.getContext(), duration)));
                            } else {
                                textView.setText(isPost ? R.string.audio_post : R.string.voice_note);
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
                    if (result.thumb != null && showThumbnailIfPresent) {
                        mediaThumbView.setVisibility(View.VISIBLE);
                        mediaThumbView.setImageBitmap(result.thumb);
                    } else {
                        mediaThumbView.setVisibility(View.GONE);
                    }
                } else {
                    if (message.chatId instanceof UserId) {
                        parent.getContactLoader().load(nameView, (UserId) message.chatId);
                    }
                    textView.setText(isPost ? R.string.reply_original_post_not_found : R.string.reply_original_not_found);
                    textView.setTypeface(textView.getTypeface(), Typeface.ITALIC);
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
