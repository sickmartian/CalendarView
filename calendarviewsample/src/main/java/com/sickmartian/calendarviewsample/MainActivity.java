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

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    MonthCalendarView mMonthCalendarView;
    private int mYear;
    private int mMonth;
    private int mDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Calendar cal = Calendar.getInstance();
        mYear = cal.get(Calendar.YEAR);
        // IMPORTANT: We use base 1 months. And you should really use Joda Time.
        mMonth = 10;//cal.get(Calendar.MONTH) + 1;
        mDay = 15;//cal.get(Calendar.DATE);

        mMonthCalendarView = (MonthCalendarView) findViewById(R.id.calendar_view);
        mMonthCalendarView.setDate(mMonth, mYear);
        mMonthCalendarView.setCurrentDay(mDay);

        View testView1 = getLayoutInflater().inflate(R.layout.test_view1, null);
        mMonthCalendarView.addViewToDayInMonth(1, testView1);
        testView1 = getLayoutInflater().inflate(R.layout.test_view1, null);
        mMonthCalendarView.addViewToDayInMonth(2, testView1);
        testView1 = getLayoutInflater().inflate(R.layout.test_view2, null);
        mMonthCalendarView.addViewToDayInMonth(2, testView1);
        testView1 = getLayoutInflater().inflate(R.layout.test_view2, null);
        mMonthCalendarView.addViewToDayInMonth(2, testView1);
        testView1 = getLayoutInflater().inflate(R.layout.test_view2, null);
        mMonthCalendarView.addViewToDayInMonth(3, testView1);
        testView1 = getLayoutInflater().inflate(R.layout.test_view2, null);
        mMonthCalendarView.addViewToDayInMonth(4, testView1);

        testView1 = getLayoutInflater().inflate(R.layout.test_view1, null);
        mMonthCalendarView.addViewToDayInMonth(30, testView1);
        testView1 = getLayoutInflater().inflate(R.layout.test_view1, null);
        mMonthCalendarView.addViewToDayInMonth(31, testView1);

        // Invalid day gets ignored
        testView1 = getLayoutInflater().inflate(R.layout.test_view1, null);
        mMonthCalendarView.addViewToDayInMonth(32, testView1);

        addOutOfMonth();

        mMonthCalendarView.setDaySelectedListener(new MonthCalendarView.DaySelectionListener() {
            @Override
            public void onTapEnded(MonthCalendarView monthCalendarView, int day) {
                Toast.makeText(MainActivity.this, "onTapEnded " + Integer.toString(day), Toast.LENGTH_SHORT).show();
                mMonthCalendarView.setSelectedDay(day);
            }

            @Override
            public void onLongClick(MonthCalendarView monthCalendarView, int day) {
                Toast.makeText(MainActivity.this, "onLongClick " + Integer.toString(day), Toast.LENGTH_SHORT).show();
                mMonthCalendarView.setSelectedDay(day);
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
                                mYear = year;
                                mMonth = monthOfYear + 1; // Again: Base 1 months
                                mDay = dayOfMonth;
                                mMonthCalendarView.setDate(mMonth, mYear);
                                mMonthCalendarView.setCurrentDay(mDay);
                            }
                        },
                        mYear, mMonth - 1, mDay).show();
            }
        });

        RadioButton sunday = (RadioButton) findViewById(R.id.start_sunday);
        sunday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMonthCalendarView.setFirstDayOfTheWeek(MonthCalendarView.SUNDAY_SHIFT);
            }
        });

        RadioButton monday = (RadioButton) findViewById(R.id.start_monday);
        monday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMonthCalendarView.setFirstDayOfTheWeek(MonthCalendarView.MONDAY_SHIFT);
            }
        });

        RadioButton saturday = (RadioButton) findViewById(R.id.start_saturday);
        saturday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMonthCalendarView.setFirstDayOfTheWeek(MonthCalendarView.SATURDAY_SHIFT);
            }
        });

        Button addView1 = (Button) findViewById(R.id.add_content1);
        addView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View testView = getLayoutInflater().inflate(R.layout.test_view1, null);
                mMonthCalendarView.addViewToDayInMonth(mMonthCalendarView.getSelectedDay(),
                        testView);
            }
        });

        Button addView2 = (Button) findViewById(R.id.add_content2);
        addView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View testView = getLayoutInflater().inflate(R.layout.test_view2, null);
                mMonthCalendarView.addViewToDayInMonth(mMonthCalendarView.getSelectedDay(),
                        testView);
            }
        });

        Button delFirst = (Button) findViewById(R.id.remove_first);
        delFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<View> content = mMonthCalendarView.getDayContent(mMonthCalendarView.getSelectedDay());
                if (!(content.size() > 0)) return;
                content.remove(0);
                mMonthCalendarView.setDayContent(mMonthCalendarView.getSelectedDay(), content);
            }
        });

        Button delLast = (Button) findViewById(R.id.remove_last);
        delLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<View> content = mMonthCalendarView.getDayContent(mMonthCalendarView.getSelectedDay());
                if (!(content.size() > 0)) return;
                content.remove(content.size() - 1);
                mMonthCalendarView.setDayContent(mMonthCalendarView.getSelectedDay(), content);
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

    private void addOutOfMonth() {
        // Out of month cells get placed, but will get discarded if the
        // start of the week changes
        View testView1;
        for (int i = 0; i < mMonthCalendarView.getFirstCellOfMonth(); i++) {
            testView1 = getLayoutInflater().inflate(R.layout.test_view2, null);
            mMonthCalendarView.addViewToCell(i, testView1);
        }
        for (int i = mMonthCalendarView.getLastCellOfMonth(); i < MonthCalendarView.DAYS_IN_GRID; i++) {
            testView1 = getLayoutInflater().inflate(R.layout.test_view2, null);
            mMonthCalendarView.addViewToCell(i, testView1);
        }
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
