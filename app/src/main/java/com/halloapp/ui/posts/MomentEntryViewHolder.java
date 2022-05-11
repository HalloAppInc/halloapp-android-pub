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
import com.halloapp.id.UserId;
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

public class MomentEntryViewHolder extends ViewHolderWithLifecycle {

    private ImageView imageView;
    private TextView lineOne;
    private TextView lineTwo;
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
        lineTwo = itemView.findViewById(R.id.line_two);

        imageView = itemView.findViewById(R.id.image);
        momentCover = itemView.findViewById(R.id.momentCover);
        avatarView = itemView.findViewById(R.id.avatar);

        unlockButton = itemView.findViewById(R.id.unlock);
        shareTextView = itemView.findViewById(R.id.share_text);

        blurView = itemView.findViewById(R.id.blurView);
        blurView.setVisibility(View.GONE);
        momentCover.setVisibility(View.VISIBLE);

        unlockButton.setText(R.string.start_action);
        unlockButton.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), CameraActivity.class);
            i.putExtra(CameraActivity.EXTRA_PURPOSE, CameraActivity.PURPOSE_MOMENT);
            v.getContext().startActivity(i);
        });
        shareTextView.setText(R.string.share_moment_label);

        parent.getAvatarLoader().load(avatarView, UserId.ME);
    }
}
