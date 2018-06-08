/*
 * GrokClock MainActivity.java
 *
 * Display a clock, a stop button, and the last 10 times.
 * This mimics the functionality of a TimeWise clock.
 *
 * Copyright (C) 2011 Michel Hoche-Mong, hoche@grok.com
 *
 */
package com.grok.stopclock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.grok.stopclock.LogUtil;
import com.grok.stopclock.R;
import com.grok.stopclock.SettingsActivity;
import com.grok.stopclock.TimeEntry;
import com.grok.stopclock.TimeStore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    private final String LOGTAG = "MainActivity";

    private SharedPreferences mSharedPreferences;

    private Calendar mCalendar;
    private TextView mTvClockTime;
    private Timer mTimer;

    private TimeStore mTimeStore;

    final Handler h = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            updateTime();
            return false;
        }
    });

    class ClockUpdateTask extends TimerTask {
        @Override
        public void run() {
            h.sendEmptyMessage(0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = getApplicationContext();

        LogUtil.INSTANCE.d(LOGTAG, "onCreate()");

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mSharedPreferences = getApplicationContext().getSharedPreferences("GrokStopClock", Context.MODE_PRIVATE);

        setContentView(R.layout.activity_main);

        mCalendar = Calendar.getInstance();
        mTvClockTime = (TextView) findViewById(R.id.clock_time);

        mTimeStore = new TimeStore(this);
        mTimeStore.init();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mTimer = new Timer("ClockTimer");

        // update every 200 milliseconds
        // It'd be nice to update faster, but that's about as fast as the display can
        // redraw.
        mTimer.scheduleAtFixedRate(new ClockUpdateTask(), 100, 200);

        updateTime();
        redrawTimeList();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mTimer.cancel();
        mTimer = null;
    }

    protected void redrawTimeList() {

        TableLayout table = (TableLayout) findViewById(R.id.time_table);
        table.removeAllViews();

        ArrayList<TimeEntry> timeList = mTimeStore.getList();
        int entryCount = timeList.size();

        LayoutInflater inflater = getLayoutInflater();

        for (int i = 0; i < 9; i++) {
            TableRow row = (TableRow) inflater.inflate(R.layout.time_table_row, table, false);

            TextView entry_num = (TextView) row.findViewById(R.id.entry_number);
            TextView entry_time = (TextView) row.findViewById(R.id.entry_time);

            if (i == 0) {
                // title row
                row.setBackgroundColor(Color.GRAY);
                entry_num.setText("ID");
                entry_time.setText("Time");
            } else if (i > entryCount) {
                entry_num.setText("--");
                entry_time.setText("--:--:--");
            } else {
                TimeEntry te = timeList.get(entryCount - i);
                entry_num.setText(te.getId());
                entry_time.setText(te.getTime(TimeEntry.getTimeFormat(mSharedPreferences.getInt("TimeFormatId", 0))));
            }

            table.addView(row);
        }
    }

    private void updateTime() {

        mCalendar.setTimeInMillis(System.currentTimeMillis());

        int hour = mCalendar.get(mCalendar.HOUR_OF_DAY);
        int min = mCalendar.get(mCalendar.MINUTE);
        int sec = mCalendar.get(mCalendar.SECOND);
        int tenth = mCalendar.get(mCalendar.MILLISECOND) / 100;

        TimeEntry te = new TimeEntry("", 0, 0, 0, hour, min, sec, tenth);
        String displayTime = te.getTime(TimeEntry.getTimeFormat(mSharedPreferences.getInt("TimeFormatId", 0)));

        // Only change if necessary to reduce flicker
        // TODO: Investigate to see if it's worthwhile just updating the seconds
        String oldDisplayTime = mTvClockTime.getText().toString();
        if (!oldDisplayTime.equals(displayTime)) {
            mTvClockTime.setText(displayTime);
        }

    }

    /**
     * Called when the user touches the SaveTime button
     */
    public void onSaveTimeButton(View view) {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        // Do something in response to button click
        int year = mCalendar.get(mCalendar.YEAR);
        int month = mCalendar.get(mCalendar.MONTH);
        int day = mCalendar.get(mCalendar.DAY_OF_MONTH);
        int hour = mCalendar.get(mCalendar.HOUR_OF_DAY);
        int min = mCalendar.get(mCalendar.MINUTE);
        int sec = mCalendar.get(mCalendar.SECOND);
        int tenth = mCalendar.get(mCalendar.MILLISECOND) / 100;

        TimeEntry te = new TimeEntry(Integer.toString(mTimeStore.getEntryCount()),
                year, month, day, hour, min, sec, tenth);

        mTimeStore.addToList(te);
        redrawTimeList();
    }

    public void onSettingsButton(View view) {
        Intent settingsActivity = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(settingsActivity);
    }

}
