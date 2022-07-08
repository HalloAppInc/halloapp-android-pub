package com.halloapp.ui.posts;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.StringUtils;

public class TombstonePostViewHolder extends PostViewHolder {

    private final TextView tombstoneText;

    public TombstonePostViewHolder(@NonNull View itemView, @NonNull PostViewHolderParent parent) {
        super(itemView, parent);

        tombstoneText = itemView.findViewById(R.id.tombstone_text);

        CharSequence text = Html.fromHtml(tombstoneText.getContext().getString(R.string.post_tombstone_placeholder));
        text = StringUtils.replaceLink(tombstoneText.getContext(), text, "learn-more", () -> {
            IntentUtils.openOurWebsiteInBrowser(tombstoneText, Constants.WAITING_ON_MESSAGE_FAQ_SUFFIX);
        });
        tombstoneText.setText(text);
        tombstoneText.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
