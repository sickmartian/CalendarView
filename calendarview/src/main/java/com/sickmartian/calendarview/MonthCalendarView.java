package com.sickmartian.calendarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
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
public class MonthCalendarView extends ViewGroup
        implements GestureDetector.OnGestureListener {

    private static final int INITIAL = -1;
    public static final int DAYS_IN_GRID = 42;
    public static final int DAYS_IN_WEEK = 7;

    @IntDef({SUNDAY_SHIFT, SATURDAY_SHIFT, MONDAY_SHIFT})
    public @interface PossibleWeekShift {}
    public static final int SUNDAY_SHIFT = 0;
    public static final int SATURDAY_SHIFT = 1;
    public static final int MONDAY_SHIFT = 6;
    private int mFirstDayOfTheWeekShift = SUNDAY_SHIFT;

    // Attributes to draw
    final Paint mActiveTextColor;
    final Paint mSeparationPaint;
    final Paint mInactiveTextColor;
    final Paint mInactiveBackgroundColor;
    final Paint mActiveBackgroundColor;
    final Paint mSelectedBackgroundColor;
    final Drawable mCurrentDayDrawable;
    final float mDecorationSize;
    final float mBetweenSiblingsPadding;
    boolean mShowOverflow;
    final Paint mOverflowPaint;
    final float mOverflowHeight;
    final float mTextSize;
    final Rect mReusableTextBound = new Rect();
    final Paint mCurrentDayTextColor;

    // User set state
    ArrayList<ArrayList<View>> mChildInDays;
    int mCurrentDay;
    int mSelectedDay = INITIAL;
    int mYear;
    int mMonth;

    // Things we calculate and use to draw
    RectF[] mDayCells = new RectF[DAYS_IN_GRID];
    String[] mDayNumbers = new String[DAYS_IN_GRID];
    ArrayList<Integer> mCellsWithOverflow;
    String[] mWeekDays;
    int mLastDayOfMonth;
    int mFirstCellOfMonth = INITIAL;
    float mEndOfHeaderWithoutWeekday;
    float mEndOfHeaderWithWeekday;
    int mSingleLetterWidth;
    int mSingleLetterHeight;
    float dp1;

    // Interaction
    GestureDetectorCompat mDetector;
    DaySelectionListener mDaySelectionListener;
    public interface DaySelectionListener {
        void onTapEnded(MonthCalendarView monthCalendarView, int day);
        void onLongClick(MonthCalendarView monthCalendarView, int day);
    }

    public MonthCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MonthCalendarView,
                0, 0);

        float dp4 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        dp1 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());

        try {
            // Text
            mTextSize = a.getDimension(R.styleable.MonthCalendarView_textSize,
                    getResources().getDimension(R.dimen.calendar_view_default_text_size));

            mCurrentDayTextColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            mCurrentDayTextColor.setColor(a.getColor(R.styleable.MonthCalendarView_currentDayTextColor, Color.WHITE));
            mCurrentDayTextColor.setTextSize(mTextSize);

            mActiveTextColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            mActiveTextColor.setColor(a.getColor(R.styleable.MonthCalendarView_activeTextColor, Color.BLACK));
            mActiveTextColor.setTextSize(mTextSize);

            mInactiveTextColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            mInactiveTextColor.setColor(a.getColor(R.styleable.MonthCalendarView_inactiveTextColor, Color.DKGRAY));
            mInactiveTextColor.setTextSize(mTextSize);

            // Cell background
            mSeparationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mSeparationPaint.setStyle(Paint.Style.STROKE);
            mSeparationPaint.setColor(a.getColor(R.styleable.MonthCalendarView_separatorColor, Color.LTGRAY));

            mActiveBackgroundColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            mActiveBackgroundColor.setStyle(Paint.Style.FILL);
            mActiveBackgroundColor.setColor(a.getColor(R.styleable.MonthCalendarView_activeBackgroundColor, Color.WHITE));

            mInactiveBackgroundColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            mInactiveBackgroundColor.setStyle(Paint.Style.FILL);
            mInactiveBackgroundColor.setColor(a.getColor(R.styleable.MonthCalendarView_inactiveBackgroundColor, Color.GRAY));

            mSelectedBackgroundColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            mSelectedBackgroundColor.setStyle(Paint.Style.FILL);
            mSelectedBackgroundColor.setColor(a.getColor(R.styleable.MonthCalendarView_selectedBackgroundColor, Color.YELLOW));

            // Decoration
            mCurrentDayDrawable = a.getDrawable(R.styleable.MonthCalendarView_currentDayDecorationDrawable);

            mDecorationSize = a.getDimension(R.styleable.MonthCalendarView_currentDayDecorationSize, 0);
            mBetweenSiblingsPadding = dp4;

            // Overflow
            mShowOverflow = a.getBoolean(R.styleable.MonthCalendarView_showOverflow, true);
            mOverflowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mOverflowPaint.setStyle(Paint.Style.FILL);
            mOverflowPaint.setColor(a.getColor(R.styleable.MonthCalendarView_overflowColor, Color.GREEN));
            mOverflowHeight = a.getDimension(R.styleable.MonthCalendarView_overflowHeight,
                    getResources().getDimension(R.dimen.calendar_view_default_overflow_height));
        } finally {
            a.recycle();
        }

        // Arrays in initial state so we can draw ourselves on the editor
        removeAllContent();

        // Calculate a bunch of no-data dependent dimensions
        mActiveTextColor.getTextBounds("W", 0, 1, mReusableTextBound);
        mSingleLetterWidth = mReusableTextBound.width();
        mSingleLetterHeight = mReusableTextBound.height();
        if (mDecorationSize > 0) {
            mEndOfHeaderWithoutWeekday = mBetweenSiblingsPadding * 2+ mDecorationSize;
            mEndOfHeaderWithWeekday = mBetweenSiblingsPadding * 3 + mDecorationSize + mSingleLetterHeight;
        } else {
            mEndOfHeaderWithoutWeekday = mBetweenSiblingsPadding * 2 + mSingleLetterHeight;
            mEndOfHeaderWithWeekday = mBetweenSiblingsPadding * 3 + mSingleLetterHeight * 2;
        }

        // Interaction
        setupInteraction(context);

        // We will draw ourselves, even if we are a ViewGroup
        setWillNotDraw(false);

        setupWeekDays();
    }

    public void removeAllContent() {
        removeAllViews();

        mCellsWithOverflow = new ArrayList<>();
        mChildInDays = new ArrayList<>();
        for (int i = 0; i < DAYS_IN_GRID; i++) {
            mChildInDays.add(i, new ArrayList<View>());
        }
    }

    private void setupInteraction(Context context) {
        mDetector = new GestureDetectorCompat(context, this);
        mDetector.setIsLongpressEnabled(true);
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
        if (dayInMonth < 0 || dayInMonth > mLastDayOfMonth) return;
        addViewToCell(dayInMonth + mFirstCellOfMonth - 1, viewToAppend);
    }

    public void setCurrentDay(int dayOfThisMonth) {
        // Only mark as selected if it is this month
        if (dayOfThisMonth <= mLastDayOfMonth && dayOfThisMonth > 0) {
            mCurrentDay = dayOfThisMonth;
            invalidate();
        } else if (mCurrentDay != INITIAL) {
            // Only invalidate previous layout if we had a selected day before
            mCurrentDay = INITIAL;
            invalidate();
        }
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

    private void setDateInternal(int month, int year) {
        mYear = year;
        mMonth = month;

        sharedSetDate();
    }

    private void sharedSetDate() {
        // Get first day of the week
        Calendar cal = getUTCCalendar();
        makeCalendarBeginningOfDay(cal);
        cal.set(mYear, mMonth, 1);
        int firstDayInWeekOfMonth = cal.get(Calendar.DAY_OF_WEEK) - 1;

        // Get last day of the week
        mLastDayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(mYear, mMonth, mLastDayOfMonth);
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

    public void setDate(int month, int year) {
        mYear = year;
        mMonth = month - 1;

        setSelectedDay(INITIAL);
        removeAllContent();
        sharedSetDate();
    }

    public void setSelectedDay(int newSelectedDay) {
        // Accept days in the month
        if (newSelectedDay <= mLastDayOfMonth && newSelectedDay > 0) {
            mSelectedDay = newSelectedDay;
            invalidate();
            // Or the initial to unset it
        } else if (newSelectedDay == INITIAL) {
            mSelectedDay = INITIAL;
            invalidate();
        }
    }

    public int getSelectedDay() {
        return mSelectedDay;
    }

    public int getFirstDayOfTheWeek() {
        return mFirstDayOfTheWeekShift;
    }

    public void setFirstDayOfTheWeek(int firstDayOfTheWeekShift) {
        if (mFirstDayOfTheWeekShift != firstDayOfTheWeekShift) {
            mFirstDayOfTheWeekShift = firstDayOfTheWeekShift;

            // Save pointer to previous data we might be able to save
            int previousFirstCellOfMonth = mFirstCellOfMonth;

            // Apply changes
            setupWeekDays(); // Reset weekday names
            setDateInternal(mMonth, mYear); // Reset cells - Invalidates the view

            // Save month's content (discard out of month data)
            ArrayList<ArrayList<View>> oldChilds = mChildInDays;
            mChildInDays = new ArrayList<>();

            // Remove out of bound views
            for (int i = 0; i < DAYS_IN_GRID; i++) {
                if (i < previousFirstCellOfMonth || i >= previousFirstCellOfMonth + mLastDayOfMonth) {
                    for (int j = 0; j < oldChilds.get(i).size(); j++) {
                        removeView(oldChilds.get(i).get(j));
                    }
                }
            }

            // Send in-month cells and add new out of bound cells
            for (int i = 0; i < mFirstCellOfMonth; i++) {
                mChildInDays.add(new ArrayList<View>());
            }
            for (int i = previousFirstCellOfMonth; i <= previousFirstCellOfMonth + mLastDayOfMonth; i++) {
                mChildInDays.add(oldChilds.get(i));
            }
            for (int i = mChildInDays.size(); i < DAYS_IN_GRID; i++) {
                mChildInDays.add(new ArrayList<View>());
            }

            requestLayout();
        }
    }

    public boolean isOverflowShown() {
        return mShowOverflow;
    }

    public void setShowOverflow(boolean showOverflow) {
        if (showOverflow != mShowOverflow) {
            mShowOverflow = showOverflow;
            invalidate();
        }
    }

    public int getFirstCellOfMonth() {
        return mFirstCellOfMonth;
    }

    public int getLastCellOfMonth() {
        return mFirstCellOfMonth + mLastDayOfMonth;
    }

    public ArrayList<View> getDayContent(int dayInMonth) {
        if (dayInMonth <= mLastDayOfMonth && dayInMonth > 0) {
            return getCellContent(mFirstCellOfMonth + dayInMonth - 1);
        }
        return null;
    }

    public void setDayContent(int dayInMonth, ArrayList<View> newContent) {
        if (dayInMonth <= mLastDayOfMonth && dayInMonth > 0) {
            setCellContent(mFirstCellOfMonth + dayInMonth - 1, newContent);
        }
    }

    public ArrayList<View> getCellContent(int cellNumber) {
        if (cellNumber < 0 || cellNumber > DAYS_IN_GRID) return null;

        return (ArrayList<View>) mChildInDays.get(cellNumber).clone();
    }

    public void setCellContent(int cellNumber, ArrayList<View> newContent) {
        if (cellNumber < 0 || cellNumber > DAYS_IN_GRID) return;

        // Add new views and remove discarded views
        ArrayList<View> oldContent = mChildInDays.get(cellNumber);
        for (View newView : newContent) {
            if (!(oldContent.contains(newView))) {
                addView(newView);
            }
        }
        for (View oldView : oldContent) {
            if (!(newContent.contains(oldView))) {
                removeView(oldView);
            }
        }

        // Set new content
        mChildInDays.set(cellNumber, newContent);
        requestLayout();
    }

    // View methods
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int firstRowExtraHeight = (int) (mSingleLetterHeight + mBetweenSiblingsPadding);

        int COLS = 7;
        int ROWS = 6;
        float widthStep = w / (float) COLS;
        float heightStep = ( h - firstRowExtraHeight ) / (float) ROWS;
        for (int col = 0; col < COLS; col++) {
            float lastBottom = INITIAL;
            for (int row = 0; row < ROWS; row++) {
                if (row == 0) {
                    lastBottom = (heightStep + firstRowExtraHeight);
                    mDayCells[row * COLS  + col] = new RectF(widthStep * col, heightStep * row,
                            widthStep * (col + 1), lastBottom);
                } else {
                    float newBottom = (lastBottom + heightStep);
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

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        // Weekdays and day numbers
        int lastCellOfMonth = mFirstCellOfMonth + mLastDayOfMonth - 1;
        for (int i = 0; i < DAYS_IN_GRID; i++) {
            // Cell in month
            if (i >= mFirstCellOfMonth && i <= lastCellOfMonth) {
                int day =  i - mFirstCellOfMonth + 1;

                // Selected day has special background
                if (day == mSelectedDay) {
                    drawBackgroundForCellInColor(canvas, i, mSelectedBackgroundColor);
                } else {
                    drawBackgroundForCellInColor(canvas, i, mActiveBackgroundColor);
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
                drawBackgroundForCellInColor(canvas, i, mInactiveBackgroundColor);
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

        // Separation lines
        canvas.drawLine(0, mDayCells[7].top, getWidth(), mDayCells[7].top, mSeparationPaint);
        canvas.drawLine(0, mDayCells[14].top, getWidth(), mDayCells[14].top, mSeparationPaint);
        canvas.drawLine(0, mDayCells[21].top, getWidth(), mDayCells[21].top, mSeparationPaint);
        canvas.drawLine(0, mDayCells[28].top, getWidth(), mDayCells[28].top, mSeparationPaint);
        canvas.drawLine(0, mDayCells[35].top, getWidth(), mDayCells[35].top, mSeparationPaint);
    }

    private void drawBackgroundForCellInColor(Canvas canvas, int cellNumber, Paint backgroundColor) {
        RectF backgroundRect = new RectF(
                mDayCells[cellNumber].left,
                mDayCells[cellNumber].top,
                mDayCells[cellNumber].right,
                mDayCells[cellNumber].bottom
        );
        canvas.drawRect(backgroundRect, backgroundColor);
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

    // Persistence
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        MyOwnState myOwnState = new MyOwnState(superState);
        myOwnState.mYear = mYear;
        myOwnState.mMonth = mMonth;
        myOwnState.mCurrentDay = mCurrentDay;
        myOwnState.mSelectedDay = mSelectedDay;

        return myOwnState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof MyOwnState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        MyOwnState myOwnState = (MyOwnState) state;
        super.onRestoreInstanceState(myOwnState.getSuperState());

        setDateInternal(myOwnState.mMonth, myOwnState.mYear);
        setCurrentDay(myOwnState.mCurrentDay);
        setSelectedDay(myOwnState.mSelectedDay);
    }

    private static class MyOwnState extends BaseSavedState {
        int mCurrentDay;
        int mSelectedDay;
        int mYear;
        int mMonth;

        public MyOwnState(Parcelable superState) {
            super(superState);
        }

        public MyOwnState(Parcel in) {
            super(in);
            mYear = in.readInt();
            mMonth = in.readInt();
            mCurrentDay = in.readInt();
            mSelectedDay = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mYear);
            out.writeInt(mMonth);
            out.writeInt(mCurrentDay);
            out.writeInt(mSelectedDay);
        }

        public static final Parcelable.Creator<MyOwnState> CREATOR =
            new Parcelable.Creator<MyOwnState>() {
                public MyOwnState createFromParcel(Parcel in) {
                    return new MyOwnState(in);
                }
                public MyOwnState[] newArray(int size) {
                    return new MyOwnState[size];
                }
            };
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
