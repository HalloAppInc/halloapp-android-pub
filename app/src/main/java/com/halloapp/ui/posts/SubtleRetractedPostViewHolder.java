package com.halloapp.ui.posts;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.ui.MediaPagerAdapter;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.util.Rtl;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.widget.SeenDetectorLayout;

public class SubtleRetractedPostViewHolder extends ViewHolderWithLifecycle {

    private Post post;
    private PostViewHolder.PostViewHolderParent parent;

    private TextView deletedTextView;
    private TextView timeView;

    public SubtleRetractedPostViewHolder(@NonNull View itemView, @NonNull PostViewHolder.PostViewHolderParent parent) {
        super(itemView);

        this.parent = parent;
        this.deletedTextView = itemView.findViewById(R.id.deleted_text);
        this.timeView = itemView.findViewById(R.id.time);
    }

    public void bindTo(Post post) {
        this.post = post;

        if (post.isOutgoing()) {
            // I deleted a post
            deletedTextView.setText(R.string.post_retracted_by_me);
        } else {
            parent.getContactLoader().load(deletedTextView, post.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
                @Override
                public void showResult(@NonNull TextView view, @Nullable Contact result) {
                    Context context = view.getContext();
                    view.setText(context.getString(R.string.post_retracted_with_name, result == null ? context.getString(R.string.unknown_contact) : result.getDisplayName()));
                }

                @Override
                public void showLoading(@NonNull TextView view) {
                    view.setText("");
                }
            });
        }
        TimeFormatter.setTimePostsFormat(timeView, post.timestamp);
        parent.getTimestampRefresher().scheduleTimestampRefresh(post.timestamp);

        final SeenDetectorLayout postContentLayout = itemView.findViewById(R.id.post_container);
        postContentLayout.setOnSeenListener(() -> {
            if (post.seen == Post.SEEN_NO && post.isIncoming()) {
                post.seen = Post.SEEN_YES_PENDING;
                ContentDb.getInstance().setIncomingPostSeen(post.senderUserId, post.id);
            }
        });
    }
}
