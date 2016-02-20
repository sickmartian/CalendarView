package com.sickmartian.calendarviewsample;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.Toast;

import com.sickmartian.calendarview.MonthCalendarView;
import com.sickmartian.calendarview.WeekView;

import java.util.ArrayList;
import java.util.Calendar;

public class WeekActivity extends AppCompatActivity {

    WeekView mWeekView;
    private int mYear;
    private int mCalendarWeek;
    private int mDay;
    private int mMonth;

    public void setStateByCalendar(Calendar cal) {
        mCalendarWeek = cal.get(Calendar.WEEK_OF_YEAR);
        mYear = cal.get(Calendar.YEAR);
        mMonth = cal.get(Calendar.MONTH) + 1;
        mDay = cal.get(Calendar.DATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Calendar cal = Calendar.getInstance();
        setStateByCalendar(cal);

        mWeekView = (WeekView) findViewById(R.id.calendar_view);
        mWeekView.setDate(mCalendarWeek, mYear);
        mWeekView.setCurrentDay(cal);

        inputTestData();

        mWeekView.setDaySelectedListener(new WeekView.DaySelectionListener() {
            @Override
            public void onTapEnded(WeekView monthCalendarView, WeekView.CellMetadata cellMetadata) {
                Toast.makeText(WeekActivity.this, "onTapEnded " + Integer.toString(cellMetadata.getDay()), Toast.LENGTH_SHORT).show();
                setDay(cellMetadata);
            }

            private void setDay(WeekView.CellMetadata cellMetadata) {
                Calendar selectedDay = Calendar.getInstance();
                selectedDay.set(Calendar.YEAR, cellMetadata.getYear());
                selectedDay.set(Calendar.MONTH, cellMetadata.getMonth());
                selectedDay.set(Calendar.DATE, cellMetadata.getDay());
                selectedDay.set(Calendar.HOUR_OF_DAY, 0);
                selectedDay.set(Calendar.MINUTE, 0);
                selectedDay.set(Calendar.SECOND, 0);
                selectedDay.set(Calendar.MILLISECOND, 0);

                mWeekView.setSelectedDay(selectedDay);
            }

            @Override
            public void onLongClick(WeekView monthCalendarView, WeekView.CellMetadata cellMetadata) {
                Toast.makeText(WeekActivity.this, "onLongClick " + Integer.toString(cellMetadata.getDay()), Toast.LENGTH_SHORT).show();
                setDay(cellMetadata);
            }
        });

        Button setDate = (Button) findViewById(R.id.set_date);
        setDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(WeekActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                Calendar selectedDay = Calendar.getInstance();
                                selectedDay.set(Calendar.YEAR, mYear);
                                selectedDay.set(Calendar.MONTH, monthOfYear);
                                selectedDay.set(Calendar.DATE, dayOfMonth);
                                selectedDay.set(Calendar.HOUR_OF_DAY, 0);
                                selectedDay.set(Calendar.MINUTE, 0);
                                selectedDay.set(Calendar.SECOND, 0);
                                selectedDay.set(Calendar.MILLISECOND, 0);

                                setStateByCalendar(selectedDay);

                                mWeekView.setDate(mCalendarWeek, mYear);
                                mWeekView.setCurrentDay(selectedDay);

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
                mWeekView.setFirstDayOfTheWeek(MonthCalendarView.SUNDAY_SHIFT);
                setCurrentByState();
            }
        });

        RadioButton monday = (RadioButton) findViewById(R.id.start_monday);
        monday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWeekView.setFirstDayOfTheWeek(MonthCalendarView.MONDAY_SHIFT);
                setCurrentByState();
            }
        });

        RadioButton saturday = (RadioButton) findViewById(R.id.start_saturday);
        saturday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWeekView.setFirstDayOfTheWeek(MonthCalendarView.SATURDAY_SHIFT);
                setCurrentByState();
            }
        });

        Button addView1 = (Button) findViewById(R.id.add_content1);
        addView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View testView = getLayoutInflater().inflate(R.layout.test_view1, null);
                mWeekView.addViewToCell(mWeekView.getSelectedCell(),
                        testView);
            }
        });

        Button addView2 = (Button) findViewById(R.id.add_content2);
        addView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View testView = getLayoutInflater().inflate(R.layout.test_view2, null);
                mWeekView.addViewToCell(mWeekView.getSelectedCell(),
                        testView);
            }
        });

        Button delFirst = (Button) findViewById(R.id.remove_first);
        delFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<View> content = mWeekView.getDayContent(mWeekView.getSelectedDay());
                if (!(content.size() > 0)) return;
                content.remove(0);
                mWeekView.setDayContent(mWeekView.getSelectedDay(), content);
            }
        });

        Button delLast = (Button) findViewById(R.id.remove_last);
        delLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<View> content = mWeekView.getDayContent(mWeekView.getSelectedDay());
                if (!(content.size() > 0)) return;
                content.remove(content.size() - 1);
                mWeekView.setDayContent(mWeekView.getSelectedDay(), content);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void setCurrentByState() {
        Calendar selectedDay = Calendar.getInstance();
        selectedDay.set(Calendar.YEAR, mYear);
        selectedDay.set(Calendar.MONTH, mMonth - 1);
        selectedDay.set(Calendar.DATE, mDay);
        selectedDay.set(Calendar.HOUR_OF_DAY, 0);
        selectedDay.set(Calendar.MINUTE, 0);
        selectedDay.set(Calendar.SECOND, 0);
        selectedDay.set(Calendar.MILLISECOND, 0);
        mWeekView.setCurrentDay(selectedDay);
    }

    private void inputTestData() {
        View testView1 = getLayoutInflater().inflate(R.layout.test_view1, null);
        mWeekView.addViewToCell(1, testView1);
        testView1 = getLayoutInflater().inflate(R.layout.test_view1, null);
        mWeekView.addViewToCell(2, testView1);
        testView1 = getLayoutInflater().inflate(R.layout.test_view2, null);
        mWeekView.addViewToCell(2, testView1);
        testView1 = getLayoutInflater().inflate(R.layout.test_view2, null);
        mWeekView.addViewToCell(2, testView1);
        testView1 = getLayoutInflater().inflate(R.layout.test_view2, null);
        mWeekView.addViewToCell(3, testView1);
        testView1 = getLayoutInflater().inflate(R.layout.test_view2, null);
        mWeekView.addViewToCell(4, testView1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
