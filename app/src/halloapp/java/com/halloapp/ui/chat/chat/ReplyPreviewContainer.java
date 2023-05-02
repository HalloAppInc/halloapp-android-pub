package com.halloapp.ui.chat.chat;

import android.content.res.ColorStateList;
import android.graphics.Outline;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.halloapp.R;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.media.AudioDurationLoader;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.util.StringUtils;
import com.halloapp.util.ViewDataLoader;

import java.util.List;

public class ReplyPreviewContainer {

    private final View replyPreviewContainer;
    private final View replyContainer;
    private final TextView replyNameView;

    private ContactLoader contactLoader;
    private TextContentLoader textContentLoader;
    private AudioDurationLoader audioDurationLoader;
    private MediaThumbnailLoader mediaThumbnailLoader;

    private Runnable onDismissListener;

    public ReplyPreviewContainer(View replyContainer) {
        this.replyContainer = replyContainer;
        this.replyPreviewContainer = replyContainer.findViewById(R.id.reply_preview_container);
        this.replyNameView = replyContainer.findViewById(R.id.reply_name);
    }

    public void init(
            @NonNull ContactLoader contactLoader,
            @NonNull TextContentLoader textContentLoader,
            @NonNull AudioDurationLoader audioDurationLoader,
            @NonNull MediaThumbnailLoader mediaThumbnailLoader) {
        this.contactLoader = contactLoader;
        this.textContentLoader = textContentLoader;
        this.audioDurationLoader = audioDurationLoader;
        this.mediaThumbnailLoader = mediaThumbnailLoader;
    }

    public void setOnDismissListener(Runnable runnable) {
        this.onDismissListener = runnable;
    }

    public void bindPost(@Nullable Post post, int replyPostMediaIndex) {
        if (post != null) {
            updateReplyColors(post.senderUserId);
            replyContainer.setVisibility(View.VISIBLE);
            final TextView replyTextView = replyContainer.findViewById(R.id.reply_text);
            textContentLoader.load(replyTextView, post);
            audioDurationLoader.cancel(replyTextView);
            contactLoader.load(replyNameView, post.senderUserId);
            final ImageView replyMediaIconView = replyContainer.findViewById(R.id.reply_media_icon);
            final ImageView replyMediaThumbView = replyContainer.findViewById(R.id.reply_media_thumb);
            List<Media> postMedia = post.getMedia();
            if (replyPostMediaIndex >= 0 && replyPostMediaIndex < postMedia.size()) {
                replyMediaThumbView.setVisibility(View.VISIBLE);
                replyMediaThumbView.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), replyMediaThumbView.getResources().getDimension(R.dimen.comment_media_list_corner_radius));
                    }
                });
                replyMediaThumbView.setClipToOutline(true);
                final Media media = postMedia.get(replyPostMediaIndex);
                mediaThumbnailLoader.load(replyMediaThumbView, media);
                replyMediaIconView.setVisibility(View.VISIBLE);
                switch (media.type) {
                    case Media.MEDIA_TYPE_IMAGE: {
                        replyMediaIconView.setImageResource(R.drawable.ic_camera);

                        if (TextUtils.isEmpty(post.text)) {
                            if (post.type == Post.TYPE_MOMENT || post.type == Post.TYPE_RETRACTED_MOMENT || post.type == Post.TYPE_MOMENT_PSA) {
                                replyTextView.setText(R.string.moment);
                            } else {
                                replyTextView.setText(R.string.photo);
                            }
                        }
                        break;
                    }
                    case Media.MEDIA_TYPE_VIDEO: {
                        replyMediaIconView.setImageResource(R.drawable.ic_video);
                        if (TextUtils.isEmpty(post.text)) {
                            replyTextView.setText(R.string.video);
                        }
                        break;
                    }
                    case Media.MEDIA_TYPE_UNKNOWN:
                    default: {
                        replyMediaIconView.setImageResource(R.drawable.ic_media_collection);
                        break;
                    }
                }
            } else if (post.type == Post.TYPE_VOICE_NOTE) {
                replyMediaIconView.setVisibility(View.VISIBLE);
                replyMediaIconView.setImageResource(R.drawable.ic_keyboard_voice);
                audioDurationLoader.load(replyTextView, post.media.get(0).file, new ViewDataLoader.Displayer<TextView, Long>() {
                    @Override
                    public void showResult(@NonNull TextView view, @Nullable Long result) {
                        if (result != null) {
                            replyTextView.setText(
                                    view.getContext().getString(R.string.audio_post_preview,
                                            StringUtils.formatVoiceNoteDuration(view.getContext(), result)));
                        }
                    }

                    @Override
                    public void showLoading(@NonNull TextView view) {
                        replyTextView.setText(R.string.audio_post);
                    }
                });
            } else {
                replyMediaThumbView.setVisibility(View.GONE);
                replyMediaIconView.setVisibility(View.GONE);
            }
            replyContainer.findViewById(R.id.reply_close).setOnClickListener(v -> {
                onRemoveReply();
            });
        } else {
            hide();
        }
    }

    public void bindMessage(@Nullable Message message, int replyMessageMediaIndex) {
        if (message != null) {
            updateReplyColors(message.senderUserId);
            replyContainer.setVisibility(View.VISIBLE);
            contactLoader.load(replyNameView, message.senderUserId);
            TextView replyTextView = replyContainer.findViewById(R.id.reply_text);
            textContentLoader.load(replyTextView, message);
            final ImageView replyMediaIconView = replyContainer.findViewById(R.id.reply_media_icon);
            final ImageView replyMediaThumbView = replyContainer.findViewById(R.id.reply_media_thumb);
            if (message.type == Message.TYPE_VOICE_NOTE) {
                replyMessageMediaIndex = 0;
            }
            audioDurationLoader.cancel(replyTextView);
            if (replyMessageMediaIndex >= 0 && replyMessageMediaIndex < message.media.size()) {
                replyMediaThumbView.setVisibility(View.VISIBLE);
                replyMediaThumbView.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), replyMediaThumbView.getResources().getDimension(R.dimen.comment_media_list_corner_radius));
                    }
                });
                replyMediaThumbView.setClipToOutline(true);
                final Media media = message.media.get(replyMessageMediaIndex);
                mediaThumbnailLoader.load(replyMediaThumbView, media);
                replyMediaIconView.setVisibility(View.VISIBLE);
                switch (media.type) {
                    case Media.MEDIA_TYPE_IMAGE: {
                        replyMediaIconView.setImageResource(R.drawable.ic_camera);
                        if (TextUtils.isEmpty(message.text)) {
                            replyTextView.setText(R.string.photo);
                        }
                        break;
                    }
                    case Media.MEDIA_TYPE_VIDEO: {
                        replyMediaIconView.setImageResource(R.drawable.ic_video);
                        if (TextUtils.isEmpty(message.text)) {
                            replyTextView.setText(R.string.video);
                        }
                        break;
                    }
                    case Media.MEDIA_TYPE_AUDIO: {
                        replyMediaIconView.setImageResource(R.drawable.ic_keyboard_voice);
                        replyMediaThumbView.setVisibility(View.GONE);
                        final String voiceNote = replyTextView.getContext().getString(R.string.voice_note);
                        audioDurationLoader.load(replyTextView, message.media.get(0).file, new ViewDataLoader.Displayer<TextView, Long>() {
                            @Override
                            public void showResult(@NonNull TextView view, @Nullable Long result) {
                                if (result != null) {
                                    replyTextView.setText(view.getContext().getString(R.string.voice_note_preview, StringUtils.formatVoiceNoteDuration(view.getContext(), result)));
                                }
                            }

                            @Override
                            public void showLoading(@NonNull TextView view) {
                                replyTextView.setText(voiceNote);
                            }
                        });
                        break;
                    }
                    case Media.MEDIA_TYPE_DOCUMENT: {
                        replyMediaIconView.setImageResource(R.drawable.ic_document);
                        replyTextView.setText(message.text);
                        break;
                    }
                    case Media.MEDIA_TYPE_UNKNOWN:
                    default: {
                        replyMediaIconView.setImageResource(R.drawable.ic_media_collection);
                        replyTextView.setText(message.text);
                        replyMediaThumbView.setVisibility(View.GONE);
                        break;
                    }
                }
            } else {
                replyMediaThumbView.setVisibility(View.GONE);
                replyMediaIconView.setVisibility(View.GONE);
            }
            replyContainer.findViewById(R.id.reply_close).setOnClickListener(v -> onRemoveReply());
        } else {
            hide();
        }
    }

    public void hide() {
        replyContainer.setVisibility(View.GONE);
    }

    private void onRemoveReply() {
        if (onDismissListener != null) {
            onDismissListener.run();
        }
    }

    private void updateReplyColors(@NonNull UserId userId) {
        if (replyPreviewContainer != null) {
            replyPreviewContainer.setBackgroundResource(R.drawable.reply_frame_background);
            replyPreviewContainer.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(replyPreviewContainer.getContext(), R.color.message_background_reply_incoming)));
        }
    }
}
