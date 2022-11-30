package com.halloapp.katchup;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

public class NewAppAvatarView extends androidx.appcompat.widget.AppCompatImageView {

    public NewAppAvatarView(Context context) {
        super(context);
        init();
    }

    public NewAppAvatarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NewAppAvatarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOutlineProvider(new JellybeanOutlineProvider());
        setClipToOutline(true);
    }
}
