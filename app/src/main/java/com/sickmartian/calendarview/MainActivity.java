package com.sickmartian.calendarview;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    CalendarView mCalendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCalendarView = (CalendarView) findViewById(R.id.calendar_view);
        View testView1 = getLayoutInflater().inflate(R.layout.test_view1, null);
        mCalendarView.addViewToDayInMonth(1, testView1);
        testView1 = getLayoutInflater().inflate(R.layout.test_view1, null);
        mCalendarView.addViewToDayInMonth(2, testView1);
        testView1 = getLayoutInflater().inflate(R.layout.test_view2, null);
        mCalendarView.addViewToDayInMonth(3, testView1);
        testView1 = getLayoutInflater().inflate(R.layout.test_view2, null);
        mCalendarView.addViewToDayInMonth(4, testView1);
        testView1 = getLayoutInflater().inflate(R.layout.test_view1, null);
        mCalendarView.addViewToDayInMonth(30, testView1);

        // Invalid day
        testView1 = getLayoutInflater().inflate(R.layout.test_view1, null);
        mCalendarView.addViewToDayInMonth(31, testView1);

        // Bounds
        testView1 = getLayoutInflater().inflate(R.layout.test_view1, null);
        mCalendarView.addViewToCell(0, testView1);
        testView1 = getLayoutInflater().inflate(R.layout.test_view1, null);
        mCalendarView.addViewToCell(41, testView1);

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
