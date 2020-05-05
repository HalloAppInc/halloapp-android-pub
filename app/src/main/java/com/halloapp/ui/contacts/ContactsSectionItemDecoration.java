package com.halloapp.ui.contacts;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ContactsSectionItemDecoration extends RecyclerView.ItemDecoration {

    private final SectionCallback sectionCallback;

    private final float headerWidth;
    private final float headerHeight;
    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Rect tmpRect = new Rect();

    ContactsSectionItemDecoration(float headerWidth, float headerHeight, float textSize, int textColor, @NonNull SectionCallback sectionCallback) {
        this.headerWidth = headerWidth;
        this.headerHeight = headerHeight;
        this.sectionCallback = sectionCallback;
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        //outRect.left = (int)headerWidth;
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        CharSequence previousHeader = "";
        for (int i = 0; i < parent.getChildCount(); i++) {
            final View child = parent.getChildAt(i);
            final int position = parent.getChildAdapterPosition(child);

            final String title = sectionCallback.getSectionHeader(position);
            float verticalAdjustment = 0;
            if (i == 0) {
                for (int j = 1; j < parent.getChildCount(); j++) {
                    final String nextTitle = sectionCallback.getSectionHeader(position + j);
                    if (!nextTitle.equals(title)) {
                        int nextTitleOffset = parent.getChildAt(j).getTop();
                        if (nextTitleOffset > 0 && nextTitleOffset < headerHeight) {
                            verticalAdjustment = nextTitleOffset - headerHeight;
                        }
                    }
                }
            }
            if (!previousHeader.equals(title)) {
                textPaint.getTextBounds(title, 0, title.length(), tmpRect);
                final float x = child.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL ?
                        child.getRight() - headerWidth / 2 : headerWidth / 2;
                c.drawText(title, x, headerHeight + Math.max(0, child.getTop()) + verticalAdjustment - (headerHeight - tmpRect.height()) / 2, textPaint);
                previousHeader = title;
            }
        }
    }

    public interface SectionCallback {
        String getSectionHeader(int position);
    }
}
