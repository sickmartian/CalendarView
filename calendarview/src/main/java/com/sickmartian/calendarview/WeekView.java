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
import android.support.annotation.IntegerRes;
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
public class WeekView extends ViewGroup
        implements GestureDetector.OnGestureListener {

    private static final int INITIAL = -1;
    public static final int DAYS_IN_GRID = 7;
    public static final int DAYS_IN_WEEK = 7;
    private static final String SINGLE_DIGIT_DAY_WIDTH_TEMPLATE = "7";
    private static final String DOUBLE_DIGIT_DAY_WIDTH_TEMPLATE = "30";
    private static final String SPECIAL_DAY_THAT_NEEDS_WORKAROUND = "31";

    @IntDef({SUNDAY_SHIFT, SATURDAY_SHIFT, MONDAY_SHIFT})
    public @interface PossibleWeekShift {}
    public static final int SUNDAY_SHIFT = 0;
    public static final int SATURDAY_SHIFT = 1;
    public static final int MONDAY_SHIFT = 6;
    private int mFirstDayOfTheWeekShift = SUNDAY_SHIFT;

    // Attributes to draw
    final Paint mActiveTextColor;
    final Paint mSeparationPaint;
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
    int mCurrentCell;
    int mSelectedCell = INITIAL;
    int mYear;
    int mCalendarWeek;

    public static class CellMetadata {
        int year;
        int month;
        int day;
        String dayString;

        public CellMetadata(int year, int month, int day, String dayString) {
            this.year = year;
            this.month = month;
            this.day = day;
            this.dayString = dayString;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public int getMonth() {
            return month;
        }

        public void setMonth(int month) {
            this.month = month;
        }

        public int getDay() {
            return day;
        }

        public void setDay(int day) {
            this.day = day;
            this.dayString = Integer.toString(day);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CellMetadata metadata = (CellMetadata) o;

            if (year != metadata.year) return false;
            if (month != metadata.month) return false;
            if (day != metadata.day) return false;
            return dayString != null ? dayString.equals(metadata.dayString) : metadata.dayString == null;

        }

        @Override
        public int hashCode() {
            int result = year;
            result = 31 * result + month;
            result = 31 * result + day;
            result = 31 * result + (dayString != null ? dayString.hashCode() : 0);
            return result;
        }
    }

    // Things we calculate and use to draw
    RectF[] mDayCells = new RectF[DAYS_IN_GRID];
    CellMetadata[] mDayMetadata = new CellMetadata[DAYS_IN_GRID];
    ArrayList<Integer> mCellsWithOverflow;
    String[] mWeekDays;
    int mLastDayOfCW;
    int mFirstDayOfCW = INITIAL;
    float mEndOfHeaderWithoutWeekday;
    float mEndOfHeaderWithWeekday;
    int mSingleLetterWidth;
    int mSingleLetterHeight;
    float dp1;

    // Interaction
    GestureDetectorCompat mDetector;
    DaySelectionListener mDaySelectionListener;
    public interface DaySelectionListener {
        void onTapEnded(WeekView monthCalendarView, CellMetadata day);
        void onLongClick(WeekView monthCalendarView, CellMetadata day);
    }

    public WeekView(Context context, AttributeSet attrs) {
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

            // Cell background
            mSeparationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mSeparationPaint.setStyle(Paint.Style.STROKE);
            mSeparationPaint.setColor(a.getColor(R.styleable.MonthCalendarView_separatorColor, Color.LTGRAY));

            mActiveBackgroundColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            mActiveBackgroundColor.setStyle(Paint.Style.FILL);
            mActiveBackgroundColor.setColor(a.getColor(R.styleable.MonthCalendarView_activeBackgroundColor, Color.WHITE));

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

    public void setCurrentDay(Calendar currentDay) {
        if (currentDay == null && mCurrentCell != INITIAL) {
            mCurrentCell = INITIAL;
            invalidate();
            return;
        } else if (currentDay == null) {
            return;
        }

        // Only mark and invalidate if it corresponds to our cells
        int i = 0;
        for (CellMetadata metadata : mDayMetadata) {
            if (metadata.day == currentDay.get(Calendar.DATE) &&
                    metadata.month == currentDay.get(Calendar.MONTH) &&
                    metadata.year == currentDay.get(Calendar.YEAR)) {
                mCurrentCell = i;
                invalidate();
                return;
            }
            i++;
        }

        if (mCurrentCell != INITIAL) {
            mCurrentCell = INITIAL;
            invalidate();
        }
    }

    public void setSelectedDay(Calendar selectedDay) {
        if (selectedDay == null && mSelectedCell != INITIAL) {
            mSelectedCell = INITIAL;
            invalidate();
            return;
        } else if (selectedDay == null) {
            return;
        }

        // Only mark and invalidate if it corresponds to our cells
        int i = 0;
        for (CellMetadata metadata : mDayMetadata) {
            if (metadata.day == selectedDay.get(Calendar.DATE) &&
                    metadata.month == selectedDay.get(Calendar.MONTH) &&
                    metadata.year == selectedDay.get(Calendar.YEAR)) {
                mSelectedCell = i;
                invalidate();
                return;
            }
            i++;
        }

        if (mSelectedCell != INITIAL) {
            mSelectedCell = INITIAL;
            invalidate();
        }
    }

    private void setDateInternal(int calendarWeek, int year) {
        mYear = year;
        mCalendarWeek = calendarWeek;

        sharedSetDate();
    }

    private void sharedSetDate() {
        // Get first day of the CW by number
        int initialDayOfCW = mCalendarWeek * 7;

        Calendar cal = getUTCCalendar();
        makeCalendarBeginningOfDay(cal);
        cal.set(Calendar.YEAR, mYear);
        cal.set(Calendar.DAY_OF_YEAR, initialDayOfCW);

        int unadjustedDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int requestedInitialDayOfWeek = getCalendarDayForShift();

        int dayDifference = 0;
        if (unadjustedDayOfWeek != requestedInitialDayOfWeek) {
            if (unadjustedDayOfWeek > requestedInitialDayOfWeek) {
                dayDifference = requestedInitialDayOfWeek - unadjustedDayOfWeek;
            } else {
                dayDifference = unadjustedDayOfWeek - requestedInitialDayOfWeek;
            }
        }
        cal.add(Calendar.DATE, dayDifference);

        mLastDayOfCW = mFirstDayOfCW = cal.get(Calendar.DATE);
        for (int i = 0; i < DAYS_IN_GRID; i++) {
            mLastDayOfCW = cal.get(Calendar.DATE);
            mDayMetadata[i] = new CellMetadata(cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DATE),
                    Integer.toString(mLastDayOfCW)
            );
            cal.add(Calendar.DATE, 1);
        }

        invalidate();
    }

    public void setDate(int calendarWeek, int year) {
        mYear = year;
        mCalendarWeek = calendarWeek - 1;

        setSelectedDay(null);
        removeAllContent();
        sharedSetDate();
    }

    public CellMetadata getSelectedDay() {
        if (mSelectedCell == INITIAL) {
            return null;
        }
        return mDayMetadata[mSelectedCell];
    }

    public int getSelectedCell() {
        return mSelectedCell;
    }

    public int getFirstDayOfTheWeek() {
        return mFirstDayOfTheWeekShift;
    }

    public void setFirstDayOfTheWeek(int firstDayOfTheWeekShift) {
        if (mFirstDayOfTheWeekShift != firstDayOfTheWeekShift) {
            mFirstDayOfTheWeekShift = firstDayOfTheWeekShift;

            // Apply changes
            setupWeekDays(); // Reset weekday names
            setDateInternal(mCalendarWeek, mYear); // Reset cells - Invalidates the view

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
        return mFirstDayOfCW;
    }

    public int getLastCellOfMonth() {
        return mFirstDayOfCW + mLastDayOfCW;
    }

    public ArrayList<View> getDayContent(CellMetadata day) {
        int cell = 0;
        for (CellMetadata metadata : mDayMetadata) {
            if (day.equals(metadata)) {
                return getCellContent(cell);
            }
            cell++;
        }
        return null;
    }

    public void setDayContent(CellMetadata day, ArrayList<View> newContent) {
        int cell = 0;
        for (CellMetadata metadata : mDayMetadata) {
            if (day.equals(metadata)) {
                setCellContent(cell, newContent);
            }
            cell++;
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
        int ROWS = 1;
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
        int lastCellOfMonth = mFirstDayOfCW + mLastDayOfCW - 1;
        for (int i = 0; i < DAYS_IN_GRID; i++) {
            // Selected day has special background
            if (i == mSelectedCell) {
                drawBackgroundForCellInColor(canvas, i, mSelectedBackgroundColor);
            } else {
                drawBackgroundForCellInColor(canvas, i, mActiveBackgroundColor);
            }

            // Current day might have a decoration
            if (mCurrentCell == i && mCurrentDayDrawable != null) {

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

        }

        // Overflow
        if (mShowOverflow) {
            for (int cellWithOverflow : mCellsWithOverflow) {
                canvas.drawRect(mDayCells[cellWithOverflow].left, mDayCells[cellWithOverflow].bottom - mOverflowHeight,
                        mDayCells[cellWithOverflow].right, mDayCells[cellWithOverflow].bottom, mOverflowPaint);
            }
        }
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
        if (mDayMetadata == null || mDayMetadata.length == 0 || mDayMetadata[0] == null) return;

        // So the days align between each other inside the decoration, we use
        // the same number to calculate the length of the text inside the decoration
        String templateDayText;
        if (mDayMetadata[cellNumber].dayString.length() < 2) {
            templateDayText = SINGLE_DIGIT_DAY_WIDTH_TEMPLATE;
        } else if (mDayMetadata[cellNumber].dayString.equals(SPECIAL_DAY_THAT_NEEDS_WORKAROUND)) {
            templateDayText = mDayMetadata[cellNumber].dayString;
        } else {
            templateDayText = DOUBLE_DIGIT_DAY_WIDTH_TEMPLATE;
        }
        mCurrentDayTextColor.getTextBounds(templateDayText, 0, templateDayText.length(), mReusableTextBound);

        int decorationLeftOffset = 0;
        int decorationTopOffset = 0;
        if (mDecorationSize > 0) {
            decorationLeftOffset = (int) ((mDecorationSize - mReusableTextBound.width()) / 2);
            decorationTopOffset = (int) ((mDecorationSize - mReusableTextBound.height()) / 2);
        }

        canvas.drawText(mDayMetadata[cellNumber].dayString,
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

    public CellMetadata getCellFromLocation(float x, float y) {
        for (int i = 0; i < mDayCells.length; i++) {
            if (mDayCells[i].contains(x, y)) {
                return mDayMetadata[i];
            }
        }
        return null;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (mDaySelectionListener != null) {
            CellMetadata currentDay = getCellFromLocation(e.getX(), e.getY());
            if (currentDay != null) {
                mDaySelectionListener.onTapEnded(this, currentDay);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (mDaySelectionListener != null) {
            CellMetadata currentDay = getCellFromLocation(e.getX(), e.getY());
            if (currentDay != null) {
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
        myOwnState.mCalendarWeek = mCalendarWeek;
        myOwnState.mCurrentCell = mCurrentCell;
        myOwnState.mSelectedCell = mSelectedCell;

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

        setDateInternal(myOwnState.mCalendarWeek, myOwnState.mYear);
        mCurrentCell = myOwnState.mCurrentCell;
        mSelectedCell = myOwnState.mSelectedCell;
    }

    private static class MyOwnState extends BaseSavedState {
        int mCurrentCell;
        int mSelectedCell;
        int mYear;
        int mCalendarWeek;

        public MyOwnState(Parcelable superState) {
            super(superState);
        }

        public MyOwnState(Parcel in) {
            super(in);
            mYear = in.readInt();
            mCalendarWeek = in.readInt();
            mCurrentCell = in.readInt();
            mSelectedCell = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mYear);
            out.writeInt(mCalendarWeek);
            out.writeInt(mCurrentCell);
            out.writeInt(mSelectedCell);
        }

        public static final Creator<MyOwnState> CREATOR =
            new Creator<WeekView.MyOwnState>() {
                public WeekView.MyOwnState createFromParcel(Parcel in) {
                    return new WeekView.MyOwnState(in);
                }
                public WeekView.MyOwnState[] newArray(int size) {
                    return new WeekView.MyOwnState[size];
                }
            };
    }

    public int getCalendarDayForShift() {
        int dayForShift;
        switch (mFirstDayOfTheWeekShift) {
            case SUNDAY_SHIFT: {
                dayForShift = Calendar.SUNDAY;
                break;
            } case SATURDAY_SHIFT: {
                dayForShift = Calendar.SATURDAY;
                break;
            } default: {
                dayForShift = Calendar.MONDAY;
                break;
            }
        }
        return dayForShift;
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
