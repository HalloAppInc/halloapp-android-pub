package com.halloapp.ui.posts;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.halloapp.R;
import com.halloapp.content.Post;

public class RetractedPostViewHolder extends PostViewHolder {

    private final TextView retractedPlaceholder;

    public RetractedPostViewHolder(@NonNull View itemView, @NonNull PostViewHolderParent parent) {
        super(itemView, parent);

        retractedPlaceholder = itemView.findViewById(R.id.retracted_placeholder);
    }

    @Override
    public void bindTo(@NonNull Post post) {
        super.bindTo(post);

        retractedPlaceholder.setText(post.type == Post.TYPE_RETRACTED_MOMENT ? R.string.moment_expired_placeholder : R.string.post_retracted_placeholder);
    }
}
