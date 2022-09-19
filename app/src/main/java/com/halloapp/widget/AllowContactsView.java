package com.halloapp.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.halloapp.R;

public class AllowContactsView extends LinearLayoutCompat {

    private final TextView titleView;
    private final View requestPermissionsButton;

    public AllowContactsView(@NonNull Context context) {
        this(context, null);
    }

    public AllowContactsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllowContactsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        inflate(context, R.layout.view_allow_contacts, this);

        titleView = findViewById(R.id.title);
        requestPermissionsButton = findViewById(R.id.request_permissions);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AllowContactsView);
        String titleText = ta.getString(R.styleable.AllowContactsView_acvTitle);
        ta.recycle();
        titleView.setText(titleText);
    }

    public void setOnAllowClick(OnClickListener onAllowClick) {
        requestPermissionsButton.setOnClickListener(onAllowClick);
    }
}
