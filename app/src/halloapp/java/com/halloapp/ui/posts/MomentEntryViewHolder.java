package com.halloapp.ui.posts;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.halloapp.R;
import com.halloapp.id.UserId;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.camera.CameraActivity;

import eightbitlab.com.blurview.BlurView;

public class MomentEntryViewHolder extends ViewHolderWithLifecycle {

    private ImageView imageView;
    private TextView lineOne;
    private ImageView momentCover;

    private ImageView avatarView;

    private Button unlockButton;

    private PostViewHolder.PostViewHolderParent parent;

    private TextView shareTextView;

    private boolean unlocked;

    private BlurView blurView;

    public MomentEntryViewHolder(@NonNull View itemView, @NonNull PostViewHolder.PostViewHolderParent parent) {
        super(itemView);

        this.parent = parent;

        lineOne = itemView.findViewById(R.id.line_one);

        imageView = itemView.findViewById(R.id.image);
        momentCover = itemView.findViewById(R.id.moment_cover);
        avatarView = itemView.findViewById(R.id.avatar);

        unlockButton = itemView.findViewById(R.id.unlock);
        shareTextView = itemView.findViewById(R.id.share_text);

        blurView = itemView.findViewById(R.id.blurView);
        blurView.setVisibility(View.GONE);
        momentCover.setVisibility(View.VISIBLE);

        unlockButton.setText(R.string.open_camera_action);
        unlockButton.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), CameraActivity.class);
            i.putExtra(CameraActivity.EXTRA_PURPOSE, CameraActivity.PURPOSE_MOMENT);
            v.getContext().startActivity(i);
        });
        shareTextView.setText(R.string.share_moment_label);

        parent.getAvatarLoader().load(avatarView, UserId.ME);
    }
}
