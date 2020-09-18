package com.halloapp.ui.chat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Outline;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;

import com.halloapp.R;
import com.halloapp.id.UserId;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.ui.MediaPagerAdapter;
import com.halloapp.ui.PostContentActivity;
import com.halloapp.util.Log;
import com.halloapp.util.ViewDataLoader;

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
            // TODO(jack): Scroll to message
            if (message.replyPostId == null) {
                return;
            }
            final Intent intent = new Intent(containerView.getContext(), PostContentActivity.class);
            intent.putExtra(PostContentActivity.EXTRA_POST_SENDER_USER_ID, message.isIncoming() ? UserId.ME : message.chatId);
            intent.putExtra(PostContentActivity.EXTRA_POST_ID, message.replyPostId);
            intent.putExtra(PostContentActivity.EXTRA_POST_MEDIA_INDEX, message.replyPostMediaIndex);
            if (containerView.getContext() instanceof Activity) {
                final ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity)(containerView.getContext()), mediaThumbView, mediaThumbView.getTransitionName());
                parent.startActivity(intent, options);
            } else {
                parent.startActivity(intent);
            }
        });
        if (message.replyPostId != null) {
            mediaThumbView.setTransitionName(MediaPagerAdapter.getTransitionName(message.replyPostId, message.replyPostMediaIndex));
        } else {
            mediaThumbView.setTransitionName(MediaPagerAdapter.getTransitionName(message.replyMessageId, message.replyMessageMediaIndex));
        }
        containerView.setBackgroundResource(message.isIncoming() ? R.drawable.reply_frame_incoming : R.drawable.reply_frame_outgoing);
        parent.getReplyLoader().load(containerView, message, new ViewDataLoader.Displayer<View, ReplyLoader.Result>() {
            @Override
            public void showResult(@NonNull View view, @Nullable ReplyLoader.Result result) {
                if (result != null) {
                    nameView.setText(result.name);
                    if (result.mentions != null) {
                        parent.getTextContentLoader().load(textView, result);
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
