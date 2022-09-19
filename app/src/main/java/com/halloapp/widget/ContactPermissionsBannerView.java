package com.halloapp.widget;

import android.Manifest;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.halloapp.R;
import com.halloapp.permissions.PermissionWatcher;

public class ContactPermissionsBannerView extends LinearLayout {

    private TextView bannerTextView;

    public ContactPermissionsBannerView(Context context) {
        this(context, null);
    }

    public ContactPermissionsBannerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContactPermissionsBannerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void bind(@NonNull PermissionWatcher permissionWatcher, LifecycleOwner lifecycleOwner) {
        permissionWatcher.getPermissionLiveData(Manifest.permission.READ_CONTACTS).observe(lifecycleOwner, enabled -> {
            int visibility = View.GONE;
            if (enabled == null || !enabled) {
                visibility = View.VISIBLE;
            }
            setVisibility(visibility);
        });
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        inflate(context, R.layout.view_contact_permissions_banner, this);

        bannerTextView = findViewById(R.id.contact_permissions_text);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ContactPermissionsBannerView);
        String bannerText = ta.getString(R.styleable.ContactPermissionsBannerView_cpbvText);
        ta.recycle();

        bannerTextView.setText(bannerText);
    }
}
