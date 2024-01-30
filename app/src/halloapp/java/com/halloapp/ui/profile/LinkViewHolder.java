package com.halloapp.ui.profile;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.contacts.SocialLink;
import com.halloapp.util.IntentUtils;

public class LinkViewHolder extends RecyclerView.ViewHolder {

    private final TextView linkView;
    private final ImageView iconView;

    public LinkViewHolder(@NonNull View itemView) {
        super(itemView);
        linkView = itemView.findViewById(R.id.link);
        iconView = itemView.findViewById(R.id.icon);
    }

    public void bindTo(@NonNull SocialLink item) {
        switch (item.type) {
            case (SocialLink.Type.TIKTOK):
                iconView.setImageResource(R.drawable.ic_tiktok);
                break;
            case (SocialLink.Type.INSTAGRAM):
                iconView.setImageResource(R.drawable.ic_instagram);
                break;
            case (SocialLink.Type.X):
                iconView.setImageResource(R.drawable.ic_twitter_x);
                break;
            case (SocialLink.Type.YOUTUBE):
                iconView.setImageResource(R.drawable.ic_youtube);
                break;
            default:
                iconView.setImageResource(R.drawable.ic_link);
        }
        linkView.setText(item.text);
        linkView.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(item.text)) {
                String link = item.text.startsWith(item.getPrefix()) ? item.text : item.getPrefix() + item.text;
                IntentUtils.openUrlInBrowser(itemView, link);
            }
        });
    }
}
