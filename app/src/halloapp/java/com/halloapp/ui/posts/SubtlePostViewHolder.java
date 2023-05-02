package com.halloapp.ui.posts;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.widget.SeenDetectorLayout;

public class SubtlePostViewHolder extends ViewHolderWithLifecycle {

    private final PostViewHolder.PostViewHolderParent parent;

    private final TextView textView;
    private final TextView timeView;

    private int theme;

    public SubtlePostViewHolder(@NonNull View itemView, @NonNull PostViewHolder.PostViewHolderParent parent) {
        super(itemView);

        this.parent = parent;
        this.textView = itemView.findViewById(R.id.deleted_text);
        this.timeView = itemView.findViewById(R.id.time);
    }

    public void bindTo(Post post, boolean firstPost) {
        if (timeView != null) {
            TimeFormatter.setTimePostsFormat(timeView, post.timestamp);
        }
        parent.getTimestampRefresher().scheduleTimestampRefresh(post.timestamp);

        if (firstPost) {
            itemView.setPadding(itemView.getPaddingLeft(), itemView.getPaddingBottom(), itemView.getPaddingRight(), itemView.getPaddingBottom());
        } else {
            itemView.setPadding(itemView.getPaddingLeft(), 0, itemView.getPaddingRight(), itemView.getPaddingBottom());
        }
        parent.getContactLoader().cancel(textView);
        if (post.type == Post.TYPE_USER || post.type == Post.TYPE_RETRACTED) {
            textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.secondary_text));
            textView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(textView.getContext(), theme == 0 ? R.color.deleted_post_bg : R.color.deleted_post_themed_bg)));
            if (post.isOutgoing()) {
                // I deleted a post
                textView.setText(R.string.post_retracted_by_me);
            } else {
                parent.getContactLoader().load(textView, post.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
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

            final SeenDetectorLayout postContentLayout = itemView.findViewById(R.id.post_container);
            postContentLayout.setOnSeenListener(() -> {
                if (post.shouldSendSeenReceipt()) {
                    post.seen = Post.SEEN_YES_PENDING;
                    ContentDb.getInstance().setIncomingPostSeen(post.senderUserId, post.id, post.getParentGroup());
                }
            });
        } else {
            textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.system_post_text));
            textView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(textView.getContext(), R.color.system_post_bg)));
            parent.getSystemMessageTextResolver().bindGroupSystemPostPreview(textView, post);
        }
    }

    public void applyTheme(int theme) {
        this.theme = theme;
    }
}
