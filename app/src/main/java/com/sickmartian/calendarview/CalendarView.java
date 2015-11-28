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
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by ***REMOVED*** on 11/24/2015.
 */
public class CalendarView extends ViewGroup
        implements GestureDetector.OnGestureListener {

    private static final int INITIAL = -1;
    final Paint mActiveTextColor;
    final Paint mSeparationPaint;
    final Paint mInactiveTextColor;
    final Paint mInactiveBackgroundColor;
    final Paint mActiveBackgroundColor;
    final Paint mSelectedBackgroundColor;
    final Drawable mCurrentDayDrawable;
    final float mDecorationSize;
    final float mBetweenSiblingsPadding;
    private final boolean mShowOverflow;
    private final Paint mOverflowPaint;
    private final float mOverflowHeight;
    RectF[] mDayCells = new RectF[DAYS_IN_GRID];
    String[] mDayNumbers = new String[DAYS_IN_GRID];
    float mTextSize;
    int mLastDayOfMonth;
    int mFirstCellOfMonth = INITIAL;
    private int mYear;
    private int mMonth;
    private int mCurrentDay;
    private Rect mReusableTextBound = new Rect();
    private Paint mCurrentDayTextColor;
    private String[] mWeekDays;
    private int mSingleLetterWidth;
    private int mSingleLetterHeight;
    ArrayList<ArrayList<View>> mChildInDays;
    ArrayList<Integer> mCellsWithOverflow;

    private float mEndOfHeaderWithoutWeekday;
    private float mEndOfHeaderWithWeekday;
    private int mSeletedDay = INITIAL;

    @IntDef({SUNDAY_SHIFT, SATURDAY_SHIFT, MONDAY_SHIFT})
    public @interface PossibleWeekShift {}
    public static final int SUNDAY_SHIFT = 0;
    public static final int SATURDAY_SHIFT = 1;
    public static final int MONDAY_SHIFT = 6;
    private int mFirstDayOfTheWeekShift = MONDAY_SHIFT;

    static final int DAYS_IN_GRID = 42;
    static final int DAYS_IN_WEEK = 7;

    GestureDetectorCompat mDetector;
    DaySelectionListener mDaySelectionListener;
    public interface DaySelectionListener {
        void onTapEnded(CalendarView calendarView, int day);
        void onLongClick(CalendarView calendarView, int day);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CalendarView,
                0, 0);

        float sp4 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 4, getResources().getDisplayMetrics());
        float dp4 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());

        try {
            // Text
            mTextSize = a.getDimension(R.styleable.CalendarView_textSize,
                    getResources().getDimension(R.dimen.calendar_view_default_text_size));

            mCurrentDayTextColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            mCurrentDayTextColor.setColor(a.getColor(R.styleable.CalendarView_currentDayTextColor, Color.WHITE));
            mCurrentDayTextColor.setTextSize(mTextSize);

            mActiveTextColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            mActiveTextColor.setColor(a.getColor(R.styleable.CalendarView_activeTextColor, Color.BLACK));
            mActiveTextColor.setTextSize(mTextSize);

            mInactiveTextColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            mInactiveTextColor.setColor(a.getColor(R.styleable.CalendarView_inactiveTextColor, Color.DKGRAY));
            mInactiveTextColor.setTextSize(mTextSize);

            // Cell background
            mSeparationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mSeparationPaint.setStyle(Paint.Style.STROKE);
            mSeparationPaint.setColor(a.getColor(R.styleable.CalendarView_separatorColor, Color.LTGRAY));

            mActiveBackgroundColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            mActiveBackgroundColor.setStyle(Paint.Style.FILL);
            mActiveBackgroundColor.setColor(a.getColor(R.styleable.CalendarView_activeBackgroundColor, Color.WHITE));

            mInactiveBackgroundColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            mInactiveBackgroundColor.setStyle(Paint.Style.FILL);
            mInactiveBackgroundColor.setColor(a.getColor(R.styleable.CalendarView_inactiveBackgroundColor, Color.GRAY));

            mSelectedBackgroundColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            mSelectedBackgroundColor.setStyle(Paint.Style.FILL);
            mSelectedBackgroundColor.setColor(a.getColor(R.styleable.CalendarView_selectedBackgroundColor, Color.YELLOW));

            // Decoration
            mCurrentDayDrawable = a.getDrawable(R.styleable.CalendarView_currentDayDecorationDrawable);

            mDecorationSize = a.getDimension(R.styleable.CalendarView_currentDayDecorationSize, sp4);
            mBetweenSiblingsPadding = dp4;

            // Overflow
            mShowOverflow = a.getBoolean(R.styleable.CalendarView_showOverflow, true);
            mOverflowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mOverflowPaint.setStyle(Paint.Style.FILL);
            mOverflowPaint.setColor(a.getColor(R.styleable.CalendarView_overflowColor, Color.GREEN));
            mOverflowHeight = a.getDimension(R.styleable.CalendarView_overflowHeight,
                    getResources().getDimension(R.dimen.calendar_view_default_overflow_height));
        } finally {
            a.recycle();
        }

        // Arrays in initial state so we can draw ourselves on the editor
        mCellsWithOverflow = new ArrayList<>();
        mChildInDays = new ArrayList<>();
        for (int i = 0; i < DAYS_IN_GRID; i++) {
            mChildInDays.add(i, new ArrayList<View>());
        }

        // Calculate a bunch of no-data dependent dimensions
        mActiveTextColor.getTextBounds("W", 0, 1, mReusableTextBound);
        mSingleLetterWidth = mReusableTextBound.width();
        mSingleLetterHeight = mReusableTextBound.height();
        if (mDecorationSize > 0) {
            mEndOfHeaderWithoutWeekday = mBetweenSiblingsPadding * 2+ mDecorationSize;
            mEndOfHeaderWithWeekday = mBetweenSiblingsPadding * 2 + mDecorationSize + mSingleLetterHeight;
        } else {
            mEndOfHeaderWithoutWeekday = mBetweenSiblingsPadding * 2 + mSingleLetterHeight;
            mEndOfHeaderWithWeekday = mBetweenSiblingsPadding * 3 + mSingleLetterHeight * 2;
        }

        // Interaction
        mDetector = new GestureDetectorCompat(context, this);
        mDetector.setIsLongpressEnabled(true);

        // We will draw ourselves, even if we are a ViewGroup
        setWillNotDraw(false);

        setupWeekDays();
    }

    // Convenience methods to interact
    public void addViewToCell(int cellNumber, View viewToAppend) {
        addView(viewToAppend);

        ArrayList<View> dayArray = mChildInDays.get(cellNumber);
        dayArray.add(viewToAppend);
        mChildInDays.set(cellNumber, dayArray);

        invalidate();
    }

    public void addViewToDayInMonth(int dayInMonth, View viewToAppend) {
        if (dayInMonth > mLastDayOfMonth) return;
        addViewToCell(dayInMonth + mFirstCellOfMonth - 1, viewToAppend);
    }

    public void setCurrentDay(Calendar date) {
        // Only mark as selected if it is this month
        if (date.get(Calendar.YEAR) == mYear &&
                date.get(Calendar.MONTH) == mMonth) {
            mCurrentDay = date.get(Calendar.DATE);
            invalidate();
        } else if (mCurrentDay != INITIAL) {
            // Only invalidate previous layout if we had a selected day before
            mCurrentDay = INITIAL;
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

    public void setSelectedDay(int newSelectedDay) {
        mSeletedDay = newSelectedDay;
        invalidate();
    }

    // View methods
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We have a fixed size, we can omit some child views if they don't fit later
        int w = resolveSizeAndState((int) (
                (mSingleLetterWidth + mBetweenSiblingsPadding ) // Single column min size
                        * 2 // For chars in days of the month
                        * DAYS_IN_WEEK),
                widthMeasureSpec, 0);
        int h = resolveSizeAndState((int) ((mBetweenSiblingsPadding * 4 + mSingleLetterHeight) * DAYS_IN_WEEK), heightMeasureSpec, 0);

        setMeasuredDimension(w, h);

        // Measure child layouts if we have
        if (mDayCells.length == 0 || mDayCells[0] == null) return;

        float alreadyUsedTop = mEndOfHeaderWithWeekday;
        for (int i = 0; i < DAYS_IN_GRID; i++) {
            if (i >= DAYS_IN_WEEK) {
                alreadyUsedTop = mEndOfHeaderWithoutWeekday;
            }

            ArrayList<View> childArrayForDay = mChildInDays.get(i);
            for (int j = 0; j < childArrayForDay.size(); j++) {
                View viewToPlace = childArrayForDay.get(j);
                if (viewToPlace.getVisibility() != GONE) {
                    int wSpec = MeasureSpec.makeMeasureSpec(Math.round(mDayCells[i].width()), MeasureSpec.EXACTLY);
                    int hSpec = MeasureSpec.makeMeasureSpec(Math.round(mDayCells[i].height() - alreadyUsedTop), MeasureSpec.AT_MOST);
                    viewToPlace.measure(wSpec, hSpec);
                }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mCellsWithOverflow.clear();
        float topOffset;
        for (int i = 0; i < DAYS_IN_GRID; i++) {
            ArrayList<View> childArrayForDay = mChildInDays.get(i);
            if (i >= 7) {
                topOffset = mEndOfHeaderWithoutWeekday;
            } else {
                topOffset = mEndOfHeaderWithWeekday;
            }

            int cellBottom = (int) (mDayCells[i].bottom - mOverflowHeight);
            for (int j = 0; j < childArrayForDay.size(); j++) {
                View viewToPlace = childArrayForDay.get(j);
                if (viewToPlace.getVisibility() != GONE) {

                    // If we overflow the cell, crop the view
                    int proposedItemBottom = (int) (mDayCells[i].top + topOffset + viewToPlace.getMeasuredHeight());
                    if (proposedItemBottom >= cellBottom) {
                        proposedItemBottom = cellBottom;
                    }

                    viewToPlace.layout(
                            (int) mDayCells[i].left,
                            (int) (mDayCells[i].top + topOffset),
                            (int) mDayCells[i].right,
                            proposedItemBottom
                    );

                    topOffset += viewToPlace.getMeasuredHeight();

                    // If we don't have more space below, stop drawing them
                    if (proposedItemBottom == cellBottom) {
                        mCellsWithOverflow.add(i);
                        break;
                    }
                }
            }
        }
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

        // Weekdays and day numbers
        int lastCellOfMonth = mFirstCellOfMonth + mLastDayOfMonth - 1;
        for (int i = 0; i < DAYS_IN_GRID; i++) {
            // Cell in month
            if (i >= mFirstCellOfMonth && i <= lastCellOfMonth) {
                int day =  i - mFirstCellOfMonth + 1;

                // Selected day has special background
                if (day == mSeletedDay) {
                    canvas.drawRect(mDayCells[i], mSelectedBackgroundColor);
                } else {
                    canvas.drawRect(mDayCells[i], mActiveBackgroundColor);
                }

                // Current day might have a decoration
                if (mCurrentDay == day && mCurrentDayDrawable != null) {

                    // Decoration
                    float topOffset = mBetweenSiblingsPadding;
                    if (i < 7) {
                        topOffset += mBetweenSiblingsPadding + mSingleLetterHeight;
                    }
                    mCurrentDayDrawable.setBounds(
                            (int) (mDayCells[i].left + mBetweenSiblingsPadding),
                            (int) (mDayCells[i].top + topOffset),
                            (int) (mDayCells[i].left + mBetweenSiblingsPadding + mDecorationSize),
                            (int) (mDayCells[i].top + mDecorationSize + topOffset));
                    mCurrentDayDrawable.draw(canvas);

                    drawDayTextsInCell(canvas, i, mCurrentDayTextColor, mActiveTextColor);
                } else {
                    drawDayTextsInCell(canvas, i, mActiveTextColor, mActiveTextColor);
                }

                // Cell not in month
            } else {
                canvas.drawRect(mDayCells[i], mInactiveBackgroundColor);
                drawDayTextsInCell(canvas, i, mInactiveTextColor, mInactiveTextColor);
            }
        }

        // Overflow
        if (mShowOverflow) {
            for (int cellWithOverflow : mCellsWithOverflow) {
                canvas.drawRect(mDayCells[cellWithOverflow].left, mDayCells[cellWithOverflow].bottom - mOverflowHeight,
                        mDayCells[cellWithOverflow].right, mDayCells[cellWithOverflow].bottom, mOverflowPaint);
            }
        }
    }

    private void drawDayTextsInCell(Canvas canvas,
                                    int cellNumber,
                                    Paint mCurrentDayTextColor,
                                    Paint mCurrentWeekDayTextColor) {
        float topOffset = 0;
        // Weekday
        if (cellNumber < 7) {
            mCurrentWeekDayTextColor.getTextBounds(mWeekDays[cellNumber], 0, mWeekDays[cellNumber].length(), mReusableTextBound);

            int decorationLeftOffset = 0;
            if (mDecorationSize > 0) {
                decorationLeftOffset = (int) ((mDecorationSize - mReusableTextBound.width()) / 2);
            }

            canvas.drawText(mWeekDays[cellNumber],
                    mDayCells[cellNumber].left + mBetweenSiblingsPadding + decorationLeftOffset,
                    mDayCells[cellNumber].top + mBetweenSiblingsPadding + mReusableTextBound.height(),
                    mCurrentWeekDayTextColor);
            topOffset = mBetweenSiblingsPadding + mReusableTextBound.height();
        }

        // Day number
        // Check we have something to draw first.
        if (mDayNumbers == null || mDayNumbers.length == 0 || mDayNumbers[0] == null) return;

        mCurrentDayTextColor.getTextBounds(mDayNumbers[cellNumber], 0, mDayNumbers[cellNumber].length(), mReusableTextBound);
        int decorationLeftOffset = 0;
        int decorationTopOffset = 0;
        if (mDecorationSize > 0) {
            decorationLeftOffset = (int) ((mDecorationSize - mReusableTextBound.width()) / 2);
            decorationTopOffset = (int) ((mDecorationSize - mReusableTextBound.height()) / 2);
        }

        canvas.drawText(mDayNumbers[cellNumber],
                mDayCells[cellNumber].left + mBetweenSiblingsPadding + decorationLeftOffset,
                mDayCells[cellNumber].top + mBetweenSiblingsPadding + mReusableTextBound.height() + decorationTopOffset + topOffset,
                mCurrentDayTextColor);
    }

    // Interaction
    public void setDaySelectedListener(DaySelectionListener listener) {
        this.mDaySelectionListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    public int getCellFromLocation(float x, float y) {
        for (int i = 0; i < mDayCells.length; i++) {
            if (mDayCells[i].contains(x, y)) {
                if (i >= mFirstCellOfMonth &&
                        i <= mFirstCellOfMonth + mLastDayOfMonth - 1) {
                    return i - mFirstCellOfMonth + 1;
                } else {
                    return INITIAL;
                }
            }
        }
        return INITIAL;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (mDaySelectionListener != null) {
            int currentDay = getCellFromLocation(e.getX(), e.getY());
            if (currentDay != INITIAL) {
                mDaySelectionListener.onTapEnded(this, currentDay);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (mDaySelectionListener != null) {
            int currentDay = getCellFromLocation(e.getX(), e.getY());
            if (currentDay != INITIAL) {
                mDaySelectionListener.onLongClick(this, currentDay);
            }
        }
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    // Utils for calendar
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
