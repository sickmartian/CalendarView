package com.sickmartian.calendarview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by ***REMOVED*** on 11/24/2015.
 */
public class WeekView extends CalendarView
        implements GestureDetector.OnGestureListener {

    private static final int INITIAL = -1;
    public static final int DAYS_IN_GRID = 7;
    private static final String SINGLE_DIGIT_DAY_WIDTH_TEMPLATE = "7";
    private static final String DOUBLE_DIGIT_DAY_WIDTH_TEMPLATE = "30";
    private static final String SPECIAL_DAY_THAT_NEEDS_WORKAROUND = "31";

    // User set state
    ArrayList<ArrayList<View>> mChildInDays;
    int mCurrentCell;
    int mSelectedCell = INITIAL;
    DayMetadata mDay;

    // Things we calculate and use to draw
    RectF[] mDayCells = new RectF[DAYS_IN_GRID];
    DayMetadata[] mDayMetadata = new DayMetadata[DAYS_IN_GRID];
    ArrayList<Integer> mCellsWithOverflow;

    public WeekView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void setupInteraction(Context context) {
        mDetector = new GestureDetectorCompat(context, this);
        mDetector.setIsLongpressEnabled(true);
    }

    private void setDateInternal(DayMetadata dayMetadata) {
        mDay = dayMetadata;

        sharedSetDate();
    }

    private void sharedSetDate() {
        Calendar cal = getUTCCalendar();
        makeCalendarBeginningOfDay(cal);
        cal.set(Calendar.YEAR, mDay.getYear());
        cal.set(Calendar.MONTH, mDay.getMonth() - 1);
        cal.set(Calendar.DATE, mDay.getDay());

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

        int lastDay;
        for (int i = 0; i < DAYS_IN_GRID; i++) {
            lastDay = cal.get(Calendar.DATE);
            mDayMetadata[i] = new DayMetadata(cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    lastDay
            );
            cal.add(Calendar.DATE, 1);
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

    @Override
    public void addViewToDay(DayMetadata dayMetadata, View viewToAppend) {
        if (dayMetadata == null) return;

        int cell = 0;
        for (DayMetadata metadata : mDayMetadata) {
            if (dayMetadata.equals(metadata)) {
                addViewToCell(cell, viewToAppend);
                break;
            }
            cell++;
        }

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
        for (DayMetadata metadata : mDayMetadata) {
            if (metadata.day == currentDay.get(Calendar.DATE) &&
                    metadata.month == currentDay.get(Calendar.MONTH) + 1 &&
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

    public void setCurrentDay(DayMetadata currentDay) {
        if (currentDay == null && mCurrentCell != INITIAL) {
            mCurrentCell = INITIAL;
            invalidate();
            return;
        } else if (currentDay == null) {
            return;
        }

        // Only mark and invalidate if it corresponds to our cells
        int i = 0;
        for (DayMetadata metadata : mDayMetadata) {
            if (metadata.equals(currentDay)) {
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
        for (DayMetadata metadata : mDayMetadata) {
            if (metadata.day == selectedDay.get(Calendar.DATE) &&
                    metadata.month == selectedDay.get(Calendar.MONTH) + 1 &&
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

    public void setSelectedDay(DayMetadata selectedDay) {
        if (selectedDay == null && mSelectedCell != INITIAL) {
            mSelectedCell = INITIAL;
            invalidate();
            return;
        } else if (selectedDay == null) {
            return;
        }

        // Only mark and invalidate if it corresponds to our cells
        int i = 0;
        for (DayMetadata metadata : mDayMetadata) {
            if (metadata.equals(selectedDay)) {
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

    public void setDate(DayMetadata dayMetadata) {
        mDay = dayMetadata;

        setSelectedDay((DayMetadata) null);
        removeAllContent();
        sharedSetDate();
    }

    public DayMetadata getSelectedDay() {
        if (mSelectedCell == INITIAL) {
            return null;
        }
        return mDayMetadata[mSelectedCell];
    }

    public int getSelectedCell() {
        return mSelectedCell;
    }

    public void setFirstDayOfTheWeek(int firstDayOfTheWeekShift) {
        if (mFirstDayOfTheWeekShift != firstDayOfTheWeekShift) {
            mFirstDayOfTheWeekShift = firstDayOfTheWeekShift;

            // Apply changes
            mWeekDays = getWeekdaysForShift(mFirstDayOfTheWeekShift); // Reset weekday names
            setDateInternal(mDay); // Reset cells - Invalidates the view

            requestLayout();
        }
    }

    public ArrayList<View> getDayContent(DayMetadata day) {
        if (day != null) {
            int cell = 0;
            for (DayMetadata metadata : mDayMetadata) {
                if (day.equals(metadata)) {
                    return getCellContent(cell);
                }
                cell++;
            }
        }
        return null;
    }

    public void setDayContent(DayMetadata day, ArrayList<View> newContent) {
        if (day != null) {
            int cell = 0;
            for (DayMetadata metadata : mDayMetadata) {
                if (day.equals(metadata)) {
                    setCellContent(cell, newContent);
                }
                cell++;
            }
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

    public DayMetadata getFirstDay() {
        if (mDayMetadata == null) return null;

        return mDayMetadata[0];
    }

    public DayMetadata getLastDay() {
        if (mDayMetadata == null) return null;

        return mDayMetadata[mDayMetadata.length - 1];
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
            mCurrentWeekDayTextColor.getTextBounds("S", 0, 1, mReusableTextBound);

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

    public DayMetadata getCellFromLocation(float x, float y) {
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
            DayMetadata currentDay = getCellFromLocation(e.getX(), e.getY());
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
            DayMetadata currentDay = getCellFromLocation(e.getX(), e.getY());
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
        myOwnState.mDay = mDay;
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

        setDateInternal(myOwnState.mDay);
        mCurrentCell = myOwnState.mCurrentCell;
        mSelectedCell = myOwnState.mSelectedCell;
    }

    private static class MyOwnState extends BaseSavedState {
        int mCurrentCell;
        int mSelectedCell;
        DayMetadata mDay;

        public MyOwnState(Parcelable superState) {
            super(superState);
        }

        public MyOwnState(Parcel in) {
            super(in);
            mDay.setDay(in.readInt());
            mDay.setMonth(in.readInt());
            mDay.setYear(in.readInt());
            mCurrentCell = in.readInt();
            mSelectedCell = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mDay.getDay());
            out.writeInt(mDay.getMonth());
            out.writeInt(mDay.getYear());
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

    // Other
    @Override
    protected String getLogTag() {
        return mDay.getYear() + "-" + mDay.getMonth() + "-" + mDay.getDay();
    }

}
