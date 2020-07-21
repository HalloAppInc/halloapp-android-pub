package com.halloapp.ui.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.halloapp.BuildConfig;
import com.halloapp.R;

public class SettingsFooterPreference extends Preference {

    public SettingsFooterPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SettingsFooterPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SettingsFooterPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingsFooterPreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView versionView = (TextView) holder.findViewById(R.id.version_number);
        holder.itemView.setClickable(false);
        versionView.setText(versionView.getResources().getString(R.string.settings_version_footer, BuildConfig.VERSION_NAME));
    }
}
