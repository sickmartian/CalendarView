package com.sickmartian.calendarviewsample;

import android.app.DatePickerDialog;
import android.content.Intent;
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

import com.sickmartian.calendarview.CalendarView;
import com.sickmartian.calendarview.MonthView;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    MonthView mMonthView;
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

        mMonthView = (MonthView) findViewById(R.id.calendar_view);
        mMonthView.setDate(mMonth, mYear);
        mMonthView.setCurrentDay(mDay);

        inputTestData();

        mMonthView.setDaySelectedListener(new CalendarView.DaySelectionListener() {
            @Override
            public void onTapEnded(CalendarView calendarView, CalendarView.DayMetadata day) {
                Toast.makeText(MainActivity.this, "onTapEnded " + Integer.toString(day.getDay()), Toast.LENGTH_SHORT).show();
                mMonthView.setSelectedDay(day.getDay());
            }

            @Override
            public void onLongClick(CalendarView calendarView, CalendarView.DayMetadata day) {
                Toast.makeText(MainActivity.this, "onLongClick " + Integer.toString(day.getDay()), Toast.LENGTH_SHORT).show();
                mMonthView.setSelectedDay(day.getDay());
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
                                // Again: Base 1 months
                                int proposedMonth = monthOfYear + 1;
                                if (proposedMonth != mMonth || mYear != year) {
                                    mYear = year;
                                    mMonth = proposedMonth;
                                    mMonthView.setDate(mMonth, mYear);
                                    // If the month or year changes, you need to input the data
                                    // again
                                    inputTestData();
                                }
                                mDay = dayOfMonth;
                                mMonthView.setCurrentDay(mDay);
                            }
                        },
                        mYear, mMonth - 1, mDay).show();
            }
        });

        RadioButton sunday = (RadioButton) findViewById(R.id.start_sunday);
        sunday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMonthView.setFirstDayOfTheWeek(MonthView.SUNDAY_SHIFT);
            }
        });

        RadioButton monday = (RadioButton) findViewById(R.id.start_monday);
        monday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMonthView.setFirstDayOfTheWeek(MonthView.MONDAY_SHIFT);
            }
        });

        RadioButton saturday = (RadioButton) findViewById(R.id.start_saturday);
        saturday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMonthView.setFirstDayOfTheWeek(MonthView.SATURDAY_SHIFT);
            }
        });

        Button addView1 = (Button) findViewById(R.id.add_content1);
        addView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View testView = getLayoutInflater().inflate(R.layout.test_view1, null);
                mMonthView.addViewToDay(mMonthView.getSelectedDay(),
                        testView);
            }
        });

        Button addView2 = (Button) findViewById(R.id.add_content2);
        addView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View testView = getLayoutInflater().inflate(R.layout.test_view2, null);
                mMonthView.addViewToDay(mMonthView.getSelectedDay(),
                        testView);
            }
        });

        Button delFirst = (Button) findViewById(R.id.remove_first);
        delFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<View> content = mMonthView.getDayContent(mMonthView.getSelectedDay());
                if (!(content != null && content.size() > 0)) return;
                content.remove(0);
                mMonthView.setDayContent(mMonthView.getSelectedDay(), content);
            }
        });

        Button delLast = (Button) findViewById(R.id.remove_last);
        delLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<View> content = mMonthView.getDayContent(mMonthView.getSelectedDay());
                if (!(content != null && content.size() > 0)) return;
                content.remove(content.size() - 1);
                mMonthView.setDayContent(mMonthView.getSelectedDay(), content);
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

    private void inputTestData() {
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

        addOutOfMonth();
    }

    private void addOutOfMonth() {
        // Out of month cells get placed, but will get discarded if the
        // start of the week changes
        View testView1;
        for (int i = 0; i < mMonthView.getFirstCellOfMonth(); i++) {
            testView1 = getLayoutInflater().inflate(R.layout.test_view2, null);
            mMonthView.addViewToCell(i, testView1);
        }
        for (int i = mMonthView.getLastCellOfMonth(); i < MonthView.DAYS_IN_GRID; i++) {
            testView1 = getLayoutInflater().inflate(R.layout.test_view2, null);
            mMonthView.addViewToCell(i, testView1);
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
        if (id == R.id.switch_activity) {
            Intent goToActivity = new Intent(getApplicationContext(), WeekActivity.class);
            startActivity(goToActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
