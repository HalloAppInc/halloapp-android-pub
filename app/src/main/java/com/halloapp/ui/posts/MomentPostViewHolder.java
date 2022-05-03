package com.halloapp.ui.posts;

import android.content.Intent;
import android.graphics.Outline;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.content.MomentManager;
import com.halloapp.content.Post;
import com.halloapp.ui.BlurManager;
import com.halloapp.ui.MomentComposerActivity;
import com.halloapp.ui.MomentViewerActivity;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.camera.CameraActivity;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.TimeUtils;
import com.halloapp.util.ViewDataLoader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class MomentPostViewHolder extends ViewHolderWithLifecycle {

    private ImageView imageView;
    private TextView lineOne;
    private TextView lineTwo;
    private ImageView momentCover;

    private ImageView avatarView;

    private Button unlockButton;

    private PostViewHolder.PostViewHolderParent parent;

    private TextView shareTextView;

    private Post post;

    private boolean unlocked;

    private BlurView blurView;

    private final SimpleDateFormat dayFormatter;

    public MomentPostViewHolder(@NonNull View itemView, @NonNull PostViewHolder.PostViewHolderParent parent) {
        super(itemView);

        this.parent = parent;

        dayFormatter = new SimpleDateFormat("EEEE", Locale.getDefault());

        lineOne = itemView.findViewById(R.id.line_one);
        lineTwo = itemView.findViewById(R.id.line_two);

        imageView = itemView.findViewById(R.id.image);
        momentCover = itemView.findViewById(R.id.momentCover);
        avatarView = itemView.findViewById(R.id.avatar);

        unlockButton = itemView.findViewById(R.id.unlock);
        shareTextView = itemView.findViewById(R.id.share_text);

        LinearLayout blurContent = itemView.findViewById(R.id.blur_content);
        blurView = itemView.findViewById(R.id.blurView);
        BlurManager.getInstance().setupMomentBlur(blurView, blurContent);

        unlockButton.setOnClickListener(v -> {
            if (post != null) {
                if (unlocked) {
                    v.getContext().startActivity(MomentViewerActivity.viewMoment(v.getContext(), post.id));
                } else {
                    Intent i = new Intent(v.getContext(), CameraActivity.class);
                    i.putExtra(CameraActivity.EXTRA_PURPOSE, CameraActivity.PURPOSE_MOMENT);
                    i.putExtra(CameraActivity.EXTRA_TARGET_MOMENT, post.id);
                    v.getContext().startActivity(i);
                }
            }
        });
    }

    public void bindTo(Post post) {
        this.post = post;
        MomentManager.getInstance().isUnlockedLiveData().observe(this, isUnlocked -> {
            unlocked = isUnlocked;
            unlockButton.setText(isUnlocked ? R.string.view_action : R.string.unlock_action);
        });
        parent.getMediaThumbnailLoader().load(imageView, post.media.get(0));
        parent.getAvatarLoader().load(avatarView, post.senderUserId);
        if (post.isIncoming()) {
            parent.getContactLoader().load(shareTextView, post.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
                @Override
                public void showResult(@NonNull TextView view, @Nullable Contact result) {
                    if (result != null) {
                        view.setText(view.getContext().getString(R.string.instant_post_from, result.getDisplayName()));
                    }
                }

                @Override
                public void showLoading(@NonNull TextView view) {
                    view.setText("");
                }
            });
        } else {
            parent.getContactLoader().cancel(shareTextView);
            shareTextView.setText(R.string.instant_post_you);
        }
        lineTwo.setText(TimeFormatter.formatMessageTime(lineOne.getContext(), post.timestamp));
        lineOne.setText(dayFormatter.format(new Date(post.timestamp)));
    }
}
