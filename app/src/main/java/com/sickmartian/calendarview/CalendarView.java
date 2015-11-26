package com.sickmartian.calendarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by ***REMOVED*** on 11/24/2015.
 */
public class CalendarView extends ViewGroup {

    private final Paint mActiveTextColor;
    private final Paint mSeparationPaint;
    RectF[] mDayCells = new RectF[42];
    private float mTextSize;

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CalendarView,
                0, 0);

        try {
            mTextSize = a.getDimension(R.styleable.CalendarView_textSize,
                    getResources().getDimension(R.dimen.calendar_view_default_text_size));

            mActiveTextColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            mActiveTextColor.setColor(a.getColor(R.styleable.CalendarView_activeTextColor,
                    getResources().getColor(R.color.colorPrimary)));
            mActiveTextColor.setTextSize(mTextSize);

            mSeparationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mSeparationPaint.setStyle(Paint.Style.STROKE);
            mSeparationPaint.setColor(a.getColor(R.styleable.CalendarView_separatorColor,
                    getResources().getColor(R.color.colorPrimaryDark)));
        } finally {
            a.recycle();
        }

        setWillNotDraw(false);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    public void setMonth(int month, int year) {

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mBounds = new RectF(0, 0, w - getPaddingLeft() - getPaddingRight(),
                h - getPaddingBottom() - getPaddingTop());

        int COLS = 7;
        int ROWS = 6;
        float widthStep = w / COLS;
        float heightStep = h / ROWS;
        for (int col = 0; col < COLS; col++) {
            for (int row = 0; row < ROWS; row++) {
                mDayCells[row * COLS  + col] = new RectF(widthStep * col, heightStep * row,
                        widthStep * (col + 1), heightStep * (row + 1));
            }
        }

        ROWS = 5;
    }

    RectF mBounds;

    @Override
    protected int getSuggestedMinimumWidth() {
        return (int) mTextSize;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return (int) mTextSize;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSizeAndState((int) (mTextSize * 4), widthMeasureSpec, 0);
        int h = resolveSizeAndState((int) mTextSize, heightMeasureSpec, 0);

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (RectF cell : mDayCells) {
            canvas.drawRect(cell, mSeparationPaint);
        }
    }
}
