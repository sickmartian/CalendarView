package com.sickmartian.calendarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.View;
import android.view.ViewGroup;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by ***REMOVED*** on 2/20/2016.
 */
public abstract class CalendarView extends ViewGroup implements GestureDetector.OnGestureListener {
    public static final int DAYS_IN_WEEK = 7;
    protected String[] mWeekDays;

    @IntDef({SUNDAY_SHIFT, SATURDAY_SHIFT, MONDAY_SHIFT})
    public @interface PossibleWeekShift {}
    public static final int SUNDAY_SHIFT = 0;
    public static final int SATURDAY_SHIFT = 1;
    public static final int MONDAY_SHIFT = 6;
    protected int mFirstDayOfTheWeekShift = SUNDAY_SHIFT;

    public static class DayMetadata {
        int year;
        int month;
        int day;
        String dayString;

        public DayMetadata(int year, int month, int day) {
            this.year = year;
            this.month = month;
            setDay(day);
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

            DayMetadata metadata = (DayMetadata) o;

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
    final Paint mCurrentDayTextColor;
    final float dp1;
    final Rect mReusableTextBound = new Rect();
    final float mEndOfHeaderWithoutWeekday;
    final float mEndOfHeaderWithWeekday;
    final int mSingleLetterWidth;
    final int mSingleLetterHeight;
    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MonthView,
                0, 0);

        float dp4 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        dp1 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());

        try {
            // Text
            mTextSize = a.getDimension(R.styleable.MonthView_textSize,
                    getResources().getDimension(R.dimen.calendar_view_default_text_size));

            mCurrentDayTextColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            mCurrentDayTextColor.setColor(a.getColor(R.styleable.MonthView_currentDayTextColor, Color.WHITE));
            mCurrentDayTextColor.setTextSize(mTextSize);

            mActiveTextColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            mActiveTextColor.setColor(a.getColor(R.styleable.MonthView_activeTextColor, Color.BLACK));
            mActiveTextColor.setTextSize(mTextSize);

            mInactiveTextColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            mInactiveTextColor.setColor(a.getColor(R.styleable.MonthView_inactiveTextColor, Color.DKGRAY));
            mInactiveTextColor.setTextSize(mTextSize);

            // Cell background
            mSeparationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mSeparationPaint.setStyle(Paint.Style.STROKE);
            mSeparationPaint.setColor(a.getColor(R.styleable.MonthView_separatorColor, Color.LTGRAY));

            mActiveBackgroundColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            mActiveBackgroundColor.setStyle(Paint.Style.FILL);
            mActiveBackgroundColor.setColor(a.getColor(R.styleable.MonthView_activeBackgroundColor, Color.WHITE));

            mInactiveBackgroundColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            mInactiveBackgroundColor.setStyle(Paint.Style.FILL);
            mInactiveBackgroundColor.setColor(a.getColor(R.styleable.MonthView_inactiveBackgroundColor, Color.GRAY));

            mSelectedBackgroundColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            mSelectedBackgroundColor.setStyle(Paint.Style.FILL);
            mSelectedBackgroundColor.setColor(a.getColor(R.styleable.MonthView_selectedBackgroundColor, Color.YELLOW));

            // Decoration
            mCurrentDayDrawable = a.getDrawable(R.styleable.MonthView_currentDayDecorationDrawable);

            mDecorationSize = a.getDimension(R.styleable.MonthView_currentDayDecorationSize, 0);
            mBetweenSiblingsPadding = dp4;

            // Overflow
            mShowOverflow = a.getBoolean(R.styleable.MonthView_showOverflow, true);
            mOverflowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mOverflowPaint.setStyle(Paint.Style.FILL);
            mOverflowPaint.setColor(a.getColor(R.styleable.MonthView_overflowColor, Color.GREEN));
            mOverflowHeight = a.getDimension(R.styleable.MonthView_overflowHeight,
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

    // Utils for calendar
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

    // Common interface
    public abstract void setFirstDayOfTheWeek(int firstDayOfTheWeekShift);
    public int getFirstDayOfTheWeek() {
        return mFirstDayOfTheWeekShift;
    }

    public void setShowOverflow(boolean showOverflow) {
        if (showOverflow != mShowOverflow) {
            mShowOverflow = showOverflow;
            invalidate();
        }
    }
    public boolean isOverflowShown() {
        return mShowOverflow;
    }

    public abstract void removeAllContent();

    public abstract void setCurrentDay(Calendar currentDay);
    public abstract void setSelectedDay(Calendar selectedDay);

    public abstract DayMetadata getSelectedDay();
    public abstract int getSelectedCell();

    public abstract void addViewToDay(DayMetadata dayMetadata, View viewToAppend);
    public abstract void addViewToCell(int cellNumber, View viewToAppend);

    public abstract ArrayList<View> getDayContent(DayMetadata day);
    public abstract void setDayContent(DayMetadata day, ArrayList<View> newContent);

    public abstract ArrayList<View> getCellContent(int cellNumber);
    public abstract void setCellContent(int cellNumber, ArrayList<View> newContent);

    // Interaction
    GestureDetectorCompat mDetector;
    DaySelectionListener mDaySelectionListener;
    public interface DaySelectionListener {
        void onTapEnded(CalendarView calendarView, DayMetadata day);
        void onLongClick(CalendarView calendarView, DayMetadata day);
    }
    public void setDaySelectedListener(DaySelectionListener listener) {
        this.mDaySelectionListener = listener;
    }

    private void setupInteraction(Context context) {
        mDetector = new GestureDetectorCompat(context, this);
        mDetector.setIsLongpressEnabled(true);
    }

}
