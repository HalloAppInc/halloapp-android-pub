package com.halloapp.ui.posts;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.halloapp.R;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.StringUtils;

public class FutureProofPostViewHolder extends PostViewHolder {

    private final TextView futureProofMessage;

    public FutureProofPostViewHolder(@NonNull View itemView, @NonNull PostViewHolder.PostViewHolderParent parent) {
        super(itemView, parent);

        futureProofMessage = itemView.findViewById(R.id.future_proof_text);

        CharSequence text = Html.fromHtml(futureProofMessage.getContext().getString(R.string.post_upgrade_placeholder));
        text = StringUtils.replaceLink(futureProofMessage.getContext(), text, "update-app", () -> {
            IntentUtils.openPlayOrMarket(futureProofMessage);
        });
        futureProofMessage.setText(text);
        futureProofMessage.setMovementMethod(LinkMovementMethod.getInstance());
    }

}

