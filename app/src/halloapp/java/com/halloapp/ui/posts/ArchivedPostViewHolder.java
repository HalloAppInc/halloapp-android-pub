package com.halloapp.ui.posts;

import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.halloapp.R;
import com.halloapp.content.Post;

public class ArchivedPostViewHolder extends PostViewHolder {
    private final TextView timeView;

    public ArchivedPostViewHolder(@NonNull View itemView, @NonNull PostViewHolderParent parent) {
        super(itemView, parent);
        timeView = itemView.findViewById(R.id.time);
    }

    @CallSuper
    public void bindTo(@NonNull Post post) {
        super.bindTo(post);

        String date = DateUtils.formatDateTime(itemView.getContext(), post.archiveDate, DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_YEAR);
        String format = String.format(itemView.getContext().getString(R.string.archived_on), date);
        timeView.append(" • " + format);
    }
}
