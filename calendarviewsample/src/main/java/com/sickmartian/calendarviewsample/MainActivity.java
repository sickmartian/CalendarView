package com.sickmartian.calendarviewsample;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.Toast;

import com.sickmartian.calendarview.CalendarView;
import com.sickmartian.calendarview.MonthView;
import com.sickmartian.calendarview.WeekView;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final String DAY_PARAMETER = "day";
    private  static final String MONTH_PARAMETER = "month";
    private  static final String YEAR_PARAMETER = "year";

    CalendarView mCalendarView;

    private int mYear;
    private int mDay;
    private int mMonth;

    public void setStateByCalendar(Calendar cal) {
        mYear = cal.get(Calendar.YEAR);
        mMonth = cal.get(Calendar.MONTH) + 1; // We use base 1 months..
                                              // You should use joda time or a sane calendar really
        mDay = cal.get(Calendar.DATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            Calendar cal = Calendar.getInstance();
            setStateByCalendar(cal);
        } else {
            mDay = savedInstanceState.getInt(DAY_PARAMETER);
            mMonth = savedInstanceState.getInt(MONTH_PARAMETER);
            mYear = savedInstanceState.getInt(YEAR_PARAMETER);
        }

        // The two views can't have the same id, or the state won't be preserved
        // correctly and they will throw an exception
        mCalendarView = (CalendarView) findViewById(R.id.monthView);
        if (mCalendarView == null) {
            mCalendarView = (CalendarView) findViewById(R.id.weekView);
        }

        setDateByStateDependingOnView();
        mCalendarView.setCurrentDay(getCalendarForState());

        inputTestData();

        mCalendarView.setDaySelectedListener(new CalendarView.DaySelectionListener() {
            @Override
            public void onTapEnded(CalendarView calendarView, CalendarView.DayMetadata dayMetadata) {
                Toast.makeText(MainActivity.this, "onTapEnded " + Integer.toString(dayMetadata.getDay()), Toast.LENGTH_SHORT).show();
                mCalendarView.setSelectedDay(dayMetadata);
            }

            @Override
            public void onLongClick(CalendarView calendarView, CalendarView.DayMetadata dayMetadata) {
                Toast.makeText(MainActivity.this, "onLongClick " + Integer.toString(dayMetadata.getDay()), Toast.LENGTH_SHORT).show();
                mCalendarView.setSelectedDay(dayMetadata);
            }
        });

        Button setDate = (Button) findViewById(R.id.set_date);
        setDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                Calendar selectedDay = Calendar.getInstance();
                                selectedDay.set(Calendar.YEAR, year);
                                selectedDay.set(Calendar.MONTH, monthOfYear);
                                selectedDay.set(Calendar.DATE, dayOfMonth);
                                selectedDay.set(Calendar.HOUR_OF_DAY, 0);
                                selectedDay.set(Calendar.MINUTE, 0);
                                selectedDay.set(Calendar.SECOND, 0);
                                selectedDay.set(Calendar.MILLISECOND, 0);

                                setStateByCalendar(selectedDay);

                                setDateByStateDependingOnView();
                                mCalendarView.setCurrentDay(selectedDay);

                                inputTestData();
                            }
                        },
                        mYear, mMonth - 1, mDay).show();
            }
        });

        RadioButton sunday = (RadioButton) findViewById(R.id.start_sunday);
        sunday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCalendarView.setFirstDayOfTheWeek(MonthView.SUNDAY_SHIFT);
                mCalendarView.setCurrentDay(getCalendarForState());
            }
        });

        RadioButton monday = (RadioButton) findViewById(R.id.start_monday);
        monday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCalendarView.setFirstDayOfTheWeek(MonthView.MONDAY_SHIFT);
                mCalendarView.setCurrentDay(getCalendarForState());
            }
        });

        RadioButton saturday = (RadioButton) findViewById(R.id.start_saturday);
        saturday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCalendarView.setFirstDayOfTheWeek(MonthView.SATURDAY_SHIFT);
                mCalendarView.setCurrentDay(getCalendarForState());
            }
        });

        Button addView1 = (Button) findViewById(R.id.add_content1);
        addView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View testView = getLayoutInflater().inflate(R.layout.test_view1, null);
                mCalendarView.addViewToDay(mCalendarView.getSelectedDay(),
                        testView);
            }
        });

        Button addView2 = (Button) findViewById(R.id.add_content2);
        addView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View testView = getLayoutInflater().inflate(R.layout.test_view2, null);
                mCalendarView.addViewToCell(mCalendarView.getSelectedCell(),
                        testView);
            }
        });

        Button delFirst = (Button) findViewById(R.id.remove_first);
        delFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<View> content = mCalendarView.getDayContent(mCalendarView.getSelectedDay());
                if (!(content != null && content.size() > 0)) return;
                content.remove(0);
                mCalendarView.setDayContent(mCalendarView.getSelectedDay(), content);
            }
        });

        Button delLast = (Button) findViewById(R.id.remove_last);
        delLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<View> content = mCalendarView.getDayContent(mCalendarView.getSelectedDay());
                if (!(content != null && content.size() > 0)) return;
                content.remove(content.size() - 1);
                mCalendarView.setDayContent(mCalendarView.getSelectedDay(), content);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(DAY_PARAMETER, mDay);
        outState.putInt(MONTH_PARAMETER, mMonth);
        outState.putInt(YEAR_PARAMETER, mYear);
    }

    private Calendar getCalendarForState() {
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.setMinimalDaysInFirstWeek(1);
        newCalendar.setFirstDayOfWeek(Calendar.SUNDAY);
        newCalendar.set(Calendar.YEAR, mYear);
        newCalendar.set(Calendar.MONTH, mMonth - 1);
        newCalendar.set(Calendar.DATE, mDay);
        newCalendar.set(Calendar.HOUR_OF_DAY, 0);
        newCalendar.set(Calendar.MINUTE, 0);
        newCalendar.set(Calendar.SECOND, 0);
        newCalendar.set(Calendar.MILLISECOND, 0);
        return newCalendar;
    }

    private void setDateByStateDependingOnView() {
        // The window of data shown depends on the view,
        // so there isn't a shared interface for this
        if (mCalendarView instanceof MonthView) {
            ((MonthView)mCalendarView).setDate(mMonth, mYear);
        } else {
            ((WeekView)mCalendarView).setDate(new CalendarView.DayMetadata(mYear, mMonth, mDay));
        }
    }

    private void inputTestData() {
        if (mCalendarView instanceof WeekView) {
            WeekView weekView = (WeekView) mCalendarView;

            View testView1 = getLayoutInflater().inflate(R.layout.test_view1, null);
            weekView.addViewToCell(0, testView1);
            testView1 = getLayoutInflater().inflate(R.layout.test_view1, null);
            weekView.addViewToCell(1, testView1);
            testView1 = getLayoutInflater().inflate(R.layout.test_view2, null);
            weekView.addViewToCell(1, testView1);
            testView1 = getLayoutInflater().inflate(R.layout.test_view2, null);
            weekView.addViewToCell(1, testView1);
            testView1 = getLayoutInflater().inflate(R.layout.test_view2, null);
            weekView.addViewToCell(2, testView1);
            testView1 = getLayoutInflater().inflate(R.layout.test_view2, null);
            weekView.addViewToCell(3, testView1);
        } else {
            MonthView mMonthView = (MonthView) mCalendarView;

            View testView1 = getLayoutInflater().inflate(R.layout.test_view1, null);
            mMonthView.addViewToDayInCurrentMonth(1, testView1);
            testView1 = getLayoutInflater().inflate(R.layout.test_view1, null);
            mMonthView.addViewToDayInCurrentMonth(2, testView1);
            testView1 = getLayoutInflater().inflate(R.layout.test_view2, null);
            mMonthView.addViewToDayInCurrentMonth(2, testView1);
            testView1 = getLayoutInflater().inflate(R.layout.test_view2, null);
            mMonthView.addViewToDayInCurrentMonth(2, testView1);
            testView1 = getLayoutInflater().inflate(R.layout.test_view2, null);
            mMonthView.addViewToDayInCurrentMonth(3, testView1);
            testView1 = getLayoutInflater().inflate(R.layout.test_view2, null);
            mMonthView.addViewToDayInCurrentMonth(4, testView1);

            testView1 = getLayoutInflater().inflate(R.layout.test_view1, null);
            mMonthView.addViewToDayInCurrentMonth(30, testView1);
            testView1 = getLayoutInflater().inflate(R.layout.test_view1, null);
            mMonthView.addViewToDayInCurrentMonth(31, testView1);

            // Invalid day gets ignored
            testView1 = getLayoutInflater().inflate(R.layout.test_view1, null);
            mMonthView.addViewToDayInCurrentMonth(32, testView1);

            // Out of month cells get placed, but will get discarded if the
            // start of the week changes
            for (int i = 0; i < mMonthView.getFirstCellOfMonth(); i++) {
                testView1 = getLayoutInflater().inflate(R.layout.test_view2, null);
                mMonthView.addViewToCell(i, testView1);
            }
            for (int i = mMonthView.getLastCellOfMonth(); i < MonthView.DAYS_IN_GRID; i++) {
                testView1 = getLayoutInflater().inflate(R.layout.test_view2, null);
                mMonthView.addViewToCell(i, testView1);
            }
        }
    }
}
