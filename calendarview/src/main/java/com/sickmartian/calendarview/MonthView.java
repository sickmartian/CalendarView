package com.sickmartian.calendarview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by ***REMOVED*** on 11/24/2015.
 */
public class MonthView extends CalendarView
        implements GestureDetector.OnGestureListener {

    private static final int INITIAL = -1;
    public static final int DAYS_IN_GRID = 42;
    private static final String SINGLE_DIGIT_DAY_WIDTH_TEMPLATE = "7";
    private static final String DOUBLE_DIGIT_DAY_WIDTH_TEMPLATE = "30";
    private static final String SPECIAL_DAY_THAT_NEEDS_WORKAROUND = "31";

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
    int mLastDayOfMonth;
    int mFirstCellOfMonth = INITIAL;

    public MonthView(Context context, AttributeSet attrs) {
        super(context, attrs);
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

    // Convenience methods to interact
    public void removeAllContent() {
        removeAllViews();

        mCellsWithOverflow = new ArrayList<>();
        mChildInDays = new ArrayList<>();
        for (int i = 0; i < DAYS_IN_GRID; i++) {
            mChildInDays.add(i, new ArrayList<View>());
        }
    }

    public void addViewToCell(int cellNumber, View viewToAppend) {
        if (cellNumber < 0 || cellNumber > DAYS_IN_GRID) return;

        addView(viewToAppend);

        ArrayList<View> dayArray = mChildInDays.get(cellNumber);
        dayArray.add(viewToAppend);
        mChildInDays.set(cellNumber, dayArray);

        invalidate();
    }

    public void setCurrentDay(Calendar currentDay) {
        // Only mark as current if it is this month
        if (currentDay != null &&
                currentDay.get(Calendar.YEAR) == mYear &&
                currentDay.get(Calendar.MONTH) == mMonth) {
            mCurrentDay = currentDay.get(Calendar.DATE);
            invalidate();
        } else if (currentDay == null || mCurrentDay != INITIAL) {
            // Only invalidate previous layout if we had a current day before
            mCurrentDay = INITIAL;
            invalidate();
        }
    }

    public void setCurrentDay(DayMetadata dayMetadata) {
        // Only mark as current if it is this month
        if (dayMetadata != null &&
                dayMetadata.getYear() == mYear &&
                dayMetadata.getMonth() == mMonth + 1) {
            mCurrentDay = dayMetadata.getDay();
            invalidate();
        } else if (dayMetadata == null || mCurrentDay != INITIAL) {
            // Only invalidate previous layout if we had a current day before
            mCurrentDay = INITIAL;
            invalidate();
        }
    }

    public void setCurrentDay(int dayOfThisMonth) {
        // Only mark as current if it is this month
        if (dayOfThisMonth <= mLastDayOfMonth && dayOfThisMonth > 0) {
            mCurrentDay = dayOfThisMonth;
            invalidate();
        } else if (mCurrentDay != INITIAL) {
            // Only invalidate previous layout if we had a current day before
            mCurrentDay = INITIAL;
            invalidate();
        }
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

    public void setSelectedDay(Calendar date) {
        // Only mark as selected if it is this month
        if (date != null &&
                date.get(Calendar.YEAR) == mYear &&
                date.get(Calendar.MONTH) == mMonth) {
            mSelectedDay = date.get(Calendar.DATE);
            invalidate();
        } else if (date == null || mSelectedDay != INITIAL) {
            //Unset if null or not of this month and we
            // have one selected
            mSelectedDay = INITIAL;
            invalidate();
        }
    }

    public void setSelectedDay(DayMetadata dayMetadata) {
        // Only mark as selected if it is this month
        if (dayMetadata != null &&
                dayMetadata.getYear() == mYear &&
                dayMetadata.getMonth() == mMonth + 1) {
            mSelectedDay = dayMetadata.getDay();
            invalidate();
        } else if (dayMetadata == null || mSelectedDay != INITIAL) {
            // Unset if null or not of this month and we
            // have one selected
            mSelectedDay = INITIAL;
            invalidate();
        }
    }

    public void setDate(int month, int year) {
        mYear = year;
        mMonth = month - 1;

        setSelectedDay(INITIAL);
        removeAllContent();
        sharedSetDate();
    }

    public DayMetadata getSelectedDay() {
        return new DayMetadata(mYear, mMonth + 1, mSelectedDay);
    }

    public int getSelectedCell() {
        if (mSelectedDay == INITIAL) {
            return INITIAL;
        }
        return mFirstCellOfMonth + mSelectedDay - 1;
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

    public int getFirstCellOfMonth() {
        return mFirstCellOfMonth;
    }

    public int getLastCellOfMonth() {
        return mFirstCellOfMonth + mLastDayOfMonth;
    }

    public ArrayList<View> getDayContent(DayMetadata dayMetadata) {
        if (dayMetadata != null) {
            if (dayMetadata.getMonth() == mMonth && dayMetadata.getYear() == mYear) {
                return getDayContent(dayMetadata.getDay());
            }
        }
        return null;
    }

    public void setDayContent(DayMetadata dayMetadata, ArrayList<View> newContent) {
        if (dayMetadata != null) {
            if (dayMetadata.getMonth() == mMonth && dayMetadata.getYear() == mYear) {
                setDayContent(dayMetadata.getDay(), newContent);
            }
        }
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

    public void addViewToDay(DayMetadata dayMetadata, View viewToAppend) {
        if (dayMetadata.getMonth() == mMonth && dayMetadata.getYear() == mYear) {
            addViewToDayInCurrentMonth(dayMetadata.getDay(), viewToAppend);
        }
    }

    public void addViewToDayInCurrentMonth(int dayInMonth, View newView) {
        if (dayInMonth < 0 || dayInMonth > mLastDayOfMonth) return;
        addViewToCell(dayInMonth + mFirstCellOfMonth - 1, newView);
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
        super.onLayout(changed, l, t, r, b);

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

        // So the days align between each other inside the decoration, we use
        // the same number to calculate the length of the text inside the decoration
        String templateDayText;
        if (mDayNumbers[cellNumber].length() < 2) {
            templateDayText = SINGLE_DIGIT_DAY_WIDTH_TEMPLATE;
        } else if (mDayNumbers[cellNumber].equals(SPECIAL_DAY_THAT_NEEDS_WORKAROUND)) {
            templateDayText = mDayNumbers[cellNumber];
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

        canvas.drawText(mDayNumbers[cellNumber],
                mDayCells[cellNumber].left + mBetweenSiblingsPadding + decorationLeftOffset,
                mDayCells[cellNumber].top + mBetweenSiblingsPadding + mReusableTextBound.height() + decorationTopOffset + topOffset,
                mCurrentDayTextColor);
    }

    // Interaction
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
                mDaySelectionListener.onTapEnded(this, new DayMetadata(mYear, mMonth + 1, currentDay));
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
                mDaySelectionListener.onLongClick(this, new DayMetadata(mYear, mMonth + 1, currentDay));
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


    // Other
    @Override
    protected String getLogTag() {
        return mYear + "-" + (mMonth + 1);
    }
}
