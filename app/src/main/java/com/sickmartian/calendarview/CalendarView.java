package com.sickmartian.calendarview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by ***REMOVED*** on 11/24/2015.
 */
public class CalendarView extends ViewGroup {

    private final Paint mTextPaint;
    private final Paint mSeparationPaint;
    int[] mDayNumbers;
    float[] mColumnOffsets = new float[7];
    private float mTextWidth = 12;
    private float[] mRowOffsets = new float[5];

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(getResources().getColor(R.color.colorAccent));
        mTextPaint.setTextSize(mTextWidth);

        mSeparationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSeparationPaint.setStyle(Paint.Style.FILL);
        mSeparationPaint.setColor(getResources().getColor(R.color.colorPrimary));

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

        float seventh = w / 7;
        mColumnOffsets[0] = 0;
        mColumnOffsets[1] = seventh;
        mColumnOffsets[2] = seventh * 2;
        mColumnOffsets[3] = seventh * 3;
        mColumnOffsets[4] = seventh * 4;
        mColumnOffsets[5] = seventh * 5;
        mColumnOffsets[6] = seventh * 6;

        float fifths = h / 5;
        mRowOffsets[0] = 0;
        mRowOffsets[1] = fifths;
        mRowOffsets[2] = fifths * 2;
        mRowOffsets[3] = fifths * 3;
        mRowOffsets[4] = fifths * 4;
    }

    RectF mBounds;

    @Override
    protected int getSuggestedMinimumWidth() {
        return (int) mTextWidth;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return (int) mTextWidth;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSizeAndState((int) (mTextWidth * 4), widthMeasureSpec, 0);
        int h = resolveSizeAndState((int) mTextWidth, heightMeasureSpec, 0);

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText("HOLA", 0, mTextWidth, mTextPaint);

        int col = 0;
        for (float colOffset : mColumnOffsets) {
            if (col > 0) {
                canvas.drawLine(colOffset, 0, colOffset, mBounds.height(), mSeparationPaint);
            }

            for (float rowOffset : mRowOffsets) {
                canvas.drawText("1", colOffset, rowOffset + mTextWidth, mTextPaint);
            }

            col++;
        }
    }
}
