package com.halloapp.ui.posts;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.halloapp.R;
import com.halloapp.content.Post;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ArchivedPostViewHolder extends PostViewHolder {

    private final TextView archiveDate;

    public ArchivedPostViewHolder(@NonNull View itemView, @NonNull PostViewHolderParent parent) {
        super(itemView, parent);
        archiveDate = itemView.findViewById(R.id.archive_date);
    }

    @CallSuper
    public void bindTo(@NonNull Post post) {
        super.bindTo(post);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        archiveDate.setText(String.format(itemView.getContext().getString(R.string.archived_on), dateFormat.format(new Date(post.archiveDate))));
    }
}
