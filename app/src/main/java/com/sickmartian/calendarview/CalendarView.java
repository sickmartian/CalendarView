package com.sickmartian.calendarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by ***REMOVED*** on 11/24/2015.
 */
public class CalendarView extends ViewGroup {

    private static final int INITIAL = -1;
    final Paint mActiveTextColor;
    final Paint mSeparationPaint;
    final Paint mInactiveTextColor;
    final Paint mInactiveBackgroundColor;
    final Drawable mSelectedDayDrawable;
    final float mDecorationPadding;
    final float mBetweenSiblingsPadding;
    RectF[] mDayCells = new RectF[DAYS_IN_GRID];
    String[] mDayNumbers = new String[DAYS_IN_GRID];
    float mTextSize;
    int mLastDayOfMonth;
    int mFirstCellOfMonth = INITIAL;
    private int mYear;
    private int mMonth;
    private int mSelectedDay;
    private Rect mReusableTextBound = new Rect();
    private Paint mCurrentDayTextColor;
    private String[] mWeekDays;
    private int mSingleLetterWidth;
    private int mSingleLetterHeight;

    @IntDef({SUNDAY_SHIFT, SATURDAY_SHIFT, MONDAY_SHIFT})
    public @interface PossibleWeekShift {}
    public static final int SUNDAY_SHIFT = 0;
    public static final int SATURDAY_SHIFT = 1;
    public static final int MONDAY_SHIFT = 6;
    private int mFirstDayOfTheWeekShift = MONDAY_SHIFT;

    static final int DAYS_IN_GRID = 42;
    static final int DAYS_IN_WEEK = 7;

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CalendarView,
                0, 0);

        try {
            float sp4 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 4, getResources().getDisplayMetrics());

            mTextSize = a.getDimension(R.styleable.CalendarView_textSize,
                    getResources().getDimension(R.dimen.calendar_view_default_text_size));

            mCurrentDayTextColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            mCurrentDayTextColor.setColor(a.getColor(R.styleable.CalendarView_currentDayTextColor, Color.CYAN));
            mCurrentDayTextColor.setTextSize(mTextSize);

            mActiveTextColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            mActiveTextColor.setColor(a.getColor(R.styleable.CalendarView_activeTextColor, Color.BLACK));
            mActiveTextColor.setTextSize(mTextSize);

            mInactiveTextColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            mInactiveTextColor.setColor(a.getColor(R.styleable.CalendarView_inactiveTextColor, Color.DKGRAY));
            mInactiveTextColor.setTextSize(mTextSize);

            mSeparationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mSeparationPaint.setStyle(Paint.Style.STROKE);
            mSeparationPaint.setColor(a.getColor(R.styleable.CalendarView_separatorColor, Color.LTGRAY));

            mInactiveBackgroundColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            mInactiveBackgroundColor.setStyle(Paint.Style.FILL);
            mInactiveBackgroundColor.setColor(a.getColor(R.styleable.CalendarView_inactiveBackgroundColor, Color.GRAY));

            mSelectedDayDrawable = a.getDrawable(R.styleable.CalendarView_currentDayDecorationDrawable);

            mDecorationPadding = a.getDimension(R.styleable.CalendarView_currentDayDecorationPadding, sp4);
            mBetweenSiblingsPadding = sp4;

            mActiveTextColor.getTextBounds("W", 0, 1, mReusableTextBound);
            mSingleLetterWidth = mReusableTextBound.width();
            mSingleLetterHeight = mReusableTextBound.height();
        } finally {
            a.recycle();
        }

        setDate(11, 2015);
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(Calendar.DATE, 2);
        setSelectedDate(selectedDate);
        setupWeekDays();

        setWillNotDraw(false);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    public void setSelectedDate(Calendar date) {
        // Only mark as selected if it is this month
        if (date.get(Calendar.YEAR) == mYear &&
                date.get(Calendar.MONTH) == mMonth) {
            mSelectedDay = date.get(Calendar.DATE);
            invalidate();
        } else if (mSelectedDay != INITIAL) {
            // Only invalidate previous layout if we had a selected day before
            mSelectedDay = INITIAL;
            invalidate();
        }
    }

    public void setDate(int month, int year) {
        mYear = year;
        mMonth = month - 1;

        // Get first day of the week
        Calendar cal = getUTCCalendar();
        makeCalendarBeginningOfDay(cal);
        cal.set(year, mMonth, 1);
        int firstDayInWeekOfMonth = cal.get(Calendar.DAY_OF_WEEK) - 1;

        // Get last day of the week
        mLastDayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(year, mMonth, mLastDayOfMonth);
        firstDayInWeekOfMonth = ( firstDayInWeekOfMonth + mFirstDayOfTheWeekShift ) % 7;

        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.DATE, 1);
        int lastDayOfLastMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int day;
        mFirstCellOfMonth = INITIAL;
        for (int i = 0; i < DAYS_IN_GRID; i++) {
            if (i < firstDayInWeekOfMonth) {
                day = lastDayOfLastMonth - firstDayInWeekOfMonth + i + 1;
            } else if ( i < firstDayInWeekOfMonth + mLastDayOfMonth ){
                day = i - firstDayInWeekOfMonth + 1;
                if (mFirstCellOfMonth == INITIAL) {
                    mFirstCellOfMonth = i;
                }
            } else {
                day = i - firstDayInWeekOfMonth - mLastDayOfMonth + 1;
            }
            mDayNumbers[i] = Integer.toString(day);
        }

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mBounds = new RectF(0, 0, w - getPaddingLeft() - getPaddingRight(),
                h - getPaddingBottom() - getPaddingTop());

        int firstRowExtraHeight = (int) (mSingleLetterHeight + mBetweenSiblingsPadding);

        int COLS = 7;
        int ROWS = 6;
        float widthStep = w / COLS;
        float heightStep = ( h - firstRowExtraHeight ) / ROWS;
        for (int col = 0; col < COLS; col++) {
            int lastBottom = INITIAL;
            for (int row = 0; row < ROWS; row++) {
                if (row == 0) {
                    lastBottom = (int) (heightStep * (row + 1) + firstRowExtraHeight);
                    mDayCells[row * COLS  + col] = new RectF(widthStep * col, heightStep * row,
                            widthStep * (col + 1), lastBottom);
                } else {
                    int newBottom = (int) (lastBottom + heightStep);
                    mDayCells[row * COLS  + col] = new RectF(widthStep * col, lastBottom,
                            widthStep * (col + 1), newBottom);
                    lastBottom = newBottom;
                }
            }
        }
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

        // Separation lines go first
        canvas.drawLine(0, mDayCells[7].top, getWidth(), mDayCells[7].top, mSeparationPaint);
        canvas.drawLine(0, mDayCells[14].top, getWidth(), mDayCells[14].top, mSeparationPaint);
        canvas.drawLine(0, mDayCells[21].top, getWidth(), mDayCells[21].top, mSeparationPaint);
        canvas.drawLine(0, mDayCells[28].top, getWidth(), mDayCells[28].top, mSeparationPaint);
        canvas.drawLine(0, mDayCells[35].top, getWidth(), mDayCells[35].top, mSeparationPaint);

        int lastCellOfMonth = mFirstCellOfMonth + mLastDayOfMonth - 1;
        for (int i = 0; i < DAYS_IN_GRID; i++) {
            // Cell in month
            if (i >= mFirstCellOfMonth && i <= lastCellOfMonth) {
                int day =  i - mFirstCellOfMonth + 1;
                if (mSelectedDay == day && mSelectedDayDrawable != null) {

                    int maxWH = Math.max(mSingleLetterWidth, mSingleLetterHeight);
                    int l = (int) (mDayCells[i].left + mBetweenSiblingsPadding);
                    int r = (int) (l + maxWH + mDecorationPadding);
                    int t = (int) (mDayCells[i].top + mBetweenSiblingsPadding);
                    int b = (int) (t + maxWH + mDecorationPadding);
                    mSelectedDayDrawable.setBounds(l, t, r, b);
                    mSelectedDayDrawable.draw(canvas);

                    drawDayTextsInCell(canvas, i, mCurrentDayTextColor);
                } else {
                    drawDayTextsInCell(canvas, i, mActiveTextColor);
                }

                // Cell not in month
            } else {
                canvas.drawRect(mDayCells[i], mInactiveBackgroundColor);
                drawDayTextsInCell(canvas, i, mInactiveTextColor);
            }
        }
    }

    private void drawDayTextsInCell(Canvas canvas,
                                    int cellNumber,
                                    Paint mCurrentDayTextColor) {
        float topOffset = mTextSize + mBetweenSiblingsPadding;
        if (cellNumber < 7) {
            canvas.drawText(mWeekDays[cellNumber],
                    mDayCells[cellNumber].left,
                    mDayCells[cellNumber].top + topOffset,
                    mCurrentDayTextColor);
            topOffset += mTextSize + mBetweenSiblingsPadding;
        }
        float leftExtra = mSingleLetterWidth / 2;
        if (mDayNumbers[cellNumber].length() == 2) {
            leftExtra /= 2;
        }
        canvas.drawText(mDayNumbers[cellNumber],
                mDayCells[cellNumber].left + mBetweenSiblingsPadding + mDecorationPadding / 2 + leftExtra,
                mDayCells[cellNumber].top + mBetweenSiblingsPadding + mDecorationPadding / 2 + topOffset,
                mCurrentDayTextColor);
    }

    public static Calendar getUTCCalendar() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    }

    public static void makeCalendarBeginningOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    protected void setupWeekDays() {
        mWeekDays = new String[DAYS_IN_WEEK];
        String[] namesOfDays = new DateFormatSymbols().getShortWeekdays();
        for (int i = 0; i < DAYS_IN_WEEK; i++) {
            mWeekDays[i] = namesOfDays[1 + (7 - mFirstDayOfTheWeekShift + i) % 7]
                            .toUpperCase()
                            .substring(0, 1);
        }
    }
}
