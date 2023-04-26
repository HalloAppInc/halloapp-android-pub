package com.halloapp.katchup;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.R;
import com.halloapp.util.StringUtils;

public class GeotagPopupWindow extends PopupWindow {
    public GeotagPopupWindow(@NonNull Context context, boolean isMe, @Nullable String name, @NonNull String geotag, @NonNull Runnable removeRunnable) {
        super(context);

        final View root = LayoutInflater.from(context).inflate(R.layout.geotag_popup_window, null, false);

        TextView text = root.findViewById(R.id.geotag_text);
        text.setMovementMethod(LinkMovementMethod.getInstance());
        CharSequence content = isMe
                ? StringUtils.replaceLink(Html.fromHtml(context.getString(R.string.geotag_explanation_me, geotag)), "remove", Color.BLACK, Typeface.create("sans-serif", Typeface.BOLD), removeRunnable)
                : context.getString(R.string.geotag_expanation_other, name, geotag);
        text.setText(content);

        setContentView(root);

        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setOutsideTouchable(true);
        setFocusable(false);
    }

    public void show(@NonNull View anchor) {
        View contentView = getContentView();
        contentView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        showAsDropDown(anchor, (-contentView.getMeasuredWidth() + anchor.getMeasuredWidth()) / 2, 0);
    }
}
