package com.halloapp.ui.posts;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;

import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.core.content.ContextCompat;

import com.halloapp.BuildConfig;
import com.halloapp.R;

import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;

public class FutureProofPostViewHolder extends IncomingPostViewHolder {

    private TextView futureProofMessage;

    public FutureProofPostViewHolder(@NonNull View itemView, @NonNull PostViewHolder.PostViewHolderParent parent) {
        super(itemView, parent);

        futureProofMessage = itemView.findViewById(R.id.future_proof_text);

        SpannableStringBuilder current= new SpannableStringBuilder(futureProofMessage.getText());
        URLSpan[] spans= current.getSpans(0, current.length(), URLSpan.class);

        int linkColor = ContextCompat.getColor(futureProofMessage.getContext(), R.color.color_link);

        for (URLSpan span : spans) {
            int start = current.getSpanStart(span);
            int end = current.getSpanEnd(span);
            current.removeSpan(span);

            ClickableSpan learnMoreSpan = new ClickableSpan() {
                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setUnderlineText(false);
                    ds.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                    ds.setColor(linkColor);
                }

                @Override
                public void onClick(@NonNull View widget) {
                    try {
                        final Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID));
                        intent.setPackage("com.android.vending");
                        parent.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Log.i("FutureProofPostViewHolder Play Store Not Installed", e);
                        SnackbarHelper.showWarning(futureProofMessage,  R.string.app_expiration_no_play_store);
                    }
                }
            };
            current.setSpan(learnMoreSpan, start, end, 0);
        }
        futureProofMessage.setText(current);
        futureProofMessage.setMovementMethod(LinkMovementMethod.getInstance());
    }

}

