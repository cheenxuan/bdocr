/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package me.xuan.bdocr.ui.camera;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import me.xuan.bdocr.R;


public class OCRCameraLayout extends FrameLayout {

    public static int ORIENTATION_PORTRAIT = 0;
    public static int ORIENTATION_HORIZONTAL = 1;

    private int orientation = ORIENTATION_PORTRAIT;
    private View contentView;
    private View centerView;
    private View leftDownView;
    private View rightUpView;
    private View idCardExamView;
    private View idCardBackExamView;
    private View bankCardExamView;
    private View bankCardHintView;

    private int contentViewId;
    private int centerViewId;
    private int leftDownViewId;
    private int rightUpViewId;
    private int idCardExamViewId;
    private int idCardBackExamViewId;
    private int bankCardExamViewId;
    private int bankCardHintViewId;

    public void setOrientation(int orientation) {
        if (this.orientation == orientation) {
            return;
        }
        this.orientation = orientation;
        requestLayout();
    }

    public OCRCameraLayout(Context context) {
        super(context);
    }

    public OCRCameraLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttrs(attrs);
    }

    public OCRCameraLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parseAttrs(attrs);
    }

    {
        setWillNotDraw(false);
    }

    private void parseAttrs(AttributeSet attrs) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.OCRCameraLayout,
                0, 0);
        try {
            contentViewId = a.getResourceId(R.styleable.OCRCameraLayout_contentView, -1);
            centerViewId = a.getResourceId(R.styleable.OCRCameraLayout_centerView, -1);
            leftDownViewId = a.getResourceId(R.styleable.OCRCameraLayout_leftDownView, -1);
            rightUpViewId = a.getResourceId(R.styleable.OCRCameraLayout_rightUpView, -1);
            idCardExamViewId = a.getResourceId(R.styleable.OCRCameraLayout_idCardExamView, -1);
            idCardBackExamViewId = a.getResourceId(R.styleable.OCRCameraLayout_idCardBackExamView, -1);
            bankCardExamViewId = a.getResourceId(R.styleable.OCRCameraLayout_bankCardExamView, -1);
            bankCardHintViewId = a.getResourceId(R.styleable.OCRCameraLayout_bankCardHintView, -1);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        contentView = findViewById(contentViewId);
        if (centerViewId != -1) {
            centerView = findViewById(centerViewId);
        }
        leftDownView = findViewById(leftDownViewId);
        rightUpView = findViewById(rightUpViewId);
        idCardExamView = findViewById(idCardExamViewId);
        idCardBackExamView = findViewById(idCardBackExamViewId);
        bankCardExamView = findViewById(bankCardExamViewId);
        bankCardHintView = findViewById(bankCardHintViewId);
    }

    private Rect backgroundRect = new Rect();
    private Paint paint = new Paint();

    {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(83, 0, 0, 0));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = getWidth();
        int height = getHeight();
        int left;
        int top;

        ViewGroup.MarginLayoutParams leftDownViewLayoutParams = (MarginLayoutParams) leftDownView.getLayoutParams();
        ViewGroup.MarginLayoutParams rightUpViewLayoutParams = (MarginLayoutParams) rightUpView.getLayoutParams();
        if (r < b) {
            int contentHeight = width * 4 / 3;
            int heightLeft = height - contentHeight;
            contentView.layout(l, t, r, contentHeight);

            backgroundRect.left = 0;
            backgroundRect.top = contentHeight;
            backgroundRect.right = width;
            backgroundRect.bottom = height;


            if (idCardExamView != null && idCardExamView.getVisibility() == View.VISIBLE) {
                int examHeight = idCardExamView.getMeasuredHeight();
                contentHeight -= applyUnit(TypedValue.COMPLEX_UNIT_DIP, 66f);
                idCardExamView.layout(l, contentHeight, r, contentHeight + examHeight);
                contentHeight += examHeight;

                heightLeft = height - contentHeight;
            }

            if (idCardBackExamView != null && idCardBackExamView.getVisibility() == View.VISIBLE) {
                int examHeight = idCardBackExamView.getMeasuredHeight();
                contentHeight -= applyUnit(TypedValue.COMPLEX_UNIT_DIP, 66f);
                idCardBackExamView.layout(l, contentHeight, r, contentHeight + examHeight);
                contentHeight += examHeight;

                heightLeft = height - contentHeight;
            }

            if (bankCardExamView != null && bankCardExamView.getVisibility() == View.VISIBLE) {
                int examHeight = bankCardExamView.getMeasuredHeight();
                contentHeight -= applyUnit(TypedValue.COMPLEX_UNIT_DIP, 66f);
                bankCardExamView.layout(l, contentHeight, r, contentHeight + examHeight);
                contentHeight += examHeight;

                heightLeft = height - contentHeight;
            }

            if (bankCardHintView != null && bankCardHintView.getVisibility() == View.VISIBLE) {
                int hintTop = contentHeight - applyUnit(TypedValue.COMPLEX_UNIT_DIP, 60f);
                int hintWidth = bankCardHintView.getMeasuredWidth();
                int hintHeight = bankCardHintView.getMeasuredHeight();
                int hintLeft = (width - hintWidth) / 2;
                int hintRight = hintLeft + hintWidth;
                int hintBottom = hintTop + hintHeight;
                bankCardHintView.layout(hintLeft, hintTop, hintRight, hintBottom);
            }

            // layout centerView;
            if (centerView != null) {
                left = (width - centerView.getMeasuredWidth()) / 2;
                top = contentHeight + (heightLeft - centerView.getMeasuredHeight()) / 2;
                centerView
                        .layout(left, top, left + centerView.getMeasuredWidth(), top + centerView.getMeasuredHeight());
            }
            // layout leftDownView
            left = leftDownViewLayoutParams.leftMargin;
            top = contentHeight + (heightLeft - leftDownView.getMeasuredHeight()) / 2;
            leftDownView
                    .layout(left, top, left + leftDownView.getMeasuredWidth(), top + leftDownView.getMeasuredHeight());
            // layout rightUpView
            left = width - rightUpView.getMeasuredWidth() - rightUpViewLayoutParams.rightMargin;
            top = contentHeight + (heightLeft - rightUpView.getMeasuredHeight()) / 2;
            rightUpView.layout(left, top, left + rightUpView.getMeasuredWidth(), top + rightUpView.getMeasuredHeight());
        } else {
            int contentWidth = height * 4 / 3;
            int widthLeft = width - contentWidth;
            contentView.layout(l, t, contentWidth, height);

            backgroundRect.left = contentWidth;
            backgroundRect.top = 0;
            backgroundRect.right = width;
            backgroundRect.bottom = height;

            // layout centerView
            if (centerView != null) {
                left = contentWidth + (widthLeft - centerView.getMeasuredWidth()) / 2;
                top = (height - centerView.getMeasuredHeight()) / 2;
                centerView
                        .layout(left, top, left + centerView.getMeasuredWidth(), top + centerView.getMeasuredHeight());
            }
            // layout leftDownView
            left = contentWidth + (widthLeft - leftDownView.getMeasuredWidth()) / 2;
            top = height - leftDownView.getMeasuredHeight() - leftDownViewLayoutParams.bottomMargin;
            leftDownView
                    .layout(left, top, left + leftDownView.getMeasuredWidth(), top + leftDownView.getMeasuredHeight());
            // layout rightUpView
            left = contentWidth + (widthLeft - rightUpView.getMeasuredWidth()) / 2;

            top = rightUpViewLayoutParams.topMargin;
            rightUpView.layout(left, top, left + rightUpView.getMeasuredWidth(), top + rightUpView.getMeasuredHeight());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(backgroundRect, paint);
    }

    private int applyUnit(int unit, float value) {
        return (int) TypedValue.applyDimension(unit, value, getResources().getDisplayMetrics());
    }
}
