package com.sickmartian.calendarviewsample;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.sickmartian.calendarview.MonthCalendarView;

public class MainActivity extends AppCompatActivity {

    MonthCalendarView mMonthCalendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mMonthCalendarView = (MonthCalendarView) findViewById(R.id.calendar_view);
        mMonthCalendarView.setDate(11, 2015);
        mMonthCalendarView.setCurrentDay(28);

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

        // Invalid day
        testView1 = getLayoutInflater().inflate(R.layout.test_view1, null);
        mMonthCalendarView.addViewToDayInMonth(31, testView1);

        // Bounds
        testView1 = getLayoutInflater().inflate(R.layout.test_view1, null);
        mMonthCalendarView.addViewToCell(0, testView1);
        testView1 = getLayoutInflater().inflate(R.layout.test_view1, null);
        mMonthCalendarView.addViewToCell(41, testView1);

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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
