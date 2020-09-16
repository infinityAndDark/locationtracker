package com.demo.app.speedlocation.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class FrameLayout5v3Width extends FrameLayout {

    public FrameLayout5v3Width(Context context) {
        super(context);
    }

    public FrameLayout5v3Width(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FrameLayout5v3Width(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int newHeight = Math.round(width / (5f / 4f));
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY));

    }
}
