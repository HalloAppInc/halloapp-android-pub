
package com.halloapp.katchup.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.halloapp.R;
import com.halloapp.widget.ShareExternallyView;

import java.util.Locale;

public class KatchupShareExternallyView extends ShareExternallyView {
    public KatchupShareExternallyView(@NonNull Context context) {
        super(context);
    }

    public KatchupShareExternallyView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public KatchupShareExternallyView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setupVerticalLayout() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        setLayoutManager(layoutManager);
    }

    @Override
    protected ShareOptionAdapter setupShareOptionAdapter() {
        return new KatchupShareOptionAdapter();
    }

    protected class KatchupShareOptionAdapter extends ShareOptionAdapter {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_SHARE_VIA) {
                return new ShareViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.share_banner_default_item, parent, false));
            }
            return new KatchupShareTargetViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.share_banner_app_item, parent, false));
        }
    }

    protected class KatchupShareTargetViewHolder extends ShareTargetViewHolder {

        public KatchupShareTargetViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void bind(ShareTarget target) {
            this.target = target;
            icon.setImageDrawable(target.drawable);
            name.setText(target.name.toString().toLowerCase(Locale.getDefault()));
        }
    }
}
