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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    private final String LOGTAG = "MainActivity";

    static final int SETTINGS_REQUEST = 1;

    private SharedPreferences mSharedPreferences;

    private Calendar mCalendar;

    private TextView mTvClockHours;
    private TextView mTvClockMinutes;
    private TextView mTvClockSubTime;

    private ListView mTimeList;
    private TimeListAdapter mTimeListAdapter;
    private Timer mTimer;

    private TimeStore mTimeStore;

    private int mDisplayHour;
    private int mDisplayMin;

    private ClockUpdateTask mTimerTask = null;

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

        //LogUtil.INSTANCE.d(LOGTAG, "onCreate()");

        int windowFlags = WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        getWindow().setFlags(windowFlags, windowFlags);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        mSharedPreferences = getApplicationContext().getSharedPreferences("GrokStopClock", Context.MODE_PRIVATE);

        mDisplayHour = 0;
        mDisplayMin = 0;

        setContentView(R.layout.activity_main);

        mCalendar = Calendar.getInstance();

        mTvClockHours = (TextView) findViewById(R.id.clock_hours);
        mTvClockMinutes = (TextView) findViewById(R.id.clock_minutes);
        mTvClockSubTime = (TextView) findViewById(R.id.clock_subtime);

        mTimeStore = new TimeStore(this);
        mTimeStore.init();

        mTimeList = (ListView) findViewById(R.id.time_list);
        View header = getLayoutInflater().inflate(R.layout.time_table_header, null);
        mTimeList.addHeaderView(header); // have to do this before setAdapter
        mTimeListAdapter = new TimeListAdapter(this, mTimeStore);
        mTimeList.setAdapter(mTimeListAdapter); // have to this after addHeaderView

        mTimer = new Timer("ClockTimer");

    }

    @Override
    protected void onResume() {
        super.onResume();
        rescheduleTimer(mSharedPreferences.getInt("TimeFormatId", 0));
        updateTime();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTimerTask.cancel();
    }

    private void rescheduleTimer(int timeFormatId) {
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        mTimerTask = new ClockUpdateTask();
        switch (timeFormatId) {
            case 1:
                mTimer.scheduleAtFixedRate(mTimerTask, 100, 200); // update every 200 milliseconds
                break;
            case 2:
                mTimer.scheduleAtFixedRate(mTimerTask, 100, 300); // update every 300 milliseconds
                break;
            case 0:
            default:
                mTimer.scheduleAtFixedRate(mTimerTask, 100, 1000);  // update once a second
        }
    }

    private void updateTime() {

        mCalendar.setTimeInMillis(System.currentTimeMillis());

        int hour = mCalendar.get(mCalendar.HOUR_OF_DAY);
        int min = mCalendar.get(mCalendar.MINUTE);
        int sec = mCalendar.get(mCalendar.SECOND);
        int milli = mCalendar.get(mCalendar.MILLISECOND);

        if (mSharedPreferences.getBoolean("Use12HourTime", false)) {
            hour = (hour % 12);
            if (hour == 0) hour = 12;
        }


        if (mDisplayHour != hour) {
            mDisplayHour = hour;
            mTvClockHours.setText(Integer.toString(mDisplayHour));
        }

        if (mDisplayMin != min) {
            mDisplayMin = min;
            mTvClockMinutes.setText(String.format("%02d", mDisplayMin));
        }


        int timeFormatId = mSharedPreferences.getInt("TimeFormatId", 0);
        switch (timeFormatId) {
            case 1:  // tenths of a second
                mTvClockSubTime.setText(String.format(":%02d.%d", sec, milli/100));
                break;

            case 2:  // hundredths of a minute
                mTvClockSubTime.setText(String.format(".%02d", ((sec * 1000) + milli) / 600));  // GC every 7 seconds (hour/min separate field up top)
                /*  GC every 3.5 seconds BAD BAD
                mTvClockSubTime.setText(String.format("%d:%02d.%02d", mDisplayHour, mDisplayMin, ((sec * 1000) + tenth) / 600));
                StringBuilder sb = new StringBuilder(10);
                DecimalFormat hf = new DecimalFormat("?0");
                DecimalFormat mf = new DecimalFormat("00");
                DecimalFormat hunf = new DecimalFormat("00");
                sb.append(hf.format(mDisplayHour)).append(':').append(mf.format(mDisplayMin)).append('.').append(hunf.format(((sec * 1000) + tenth) / 600));
                mTvClockSubTime.setText(sb.toString());
                */
                /*
                // GC every six seconds or so
                mTvClockSubTime.setText(String.format("%d:%02d.%02d", mDisplayHour, mDisplayMin, ((sec * 1000) + milli) / 600));
                */
                break;

            case 0:  // seconds only
            default:
                mTvClockSubTime.setText(String.format(":%02d", sec + ((milli >= 500) ? 1 : 0)));
                break;
        }

        // Force a gc to keep things smooth. Otherwise garbage builds up and the gc runs
        // every few seconds and we get a perceptible stutter in our time display.
        System.gc();
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

        TimeEntry te = new TimeEntry(Integer.toString(mTimeStore.getEntryCount() + 1),
                year, month, day, hour, min, sec, tenth);

        mTimeStore.addToList(te);
        mTimeListAdapter.notifyDataSetChanged();
    }

    public void onSettingsButton(View view) {
        Intent settingsActivity = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivityForResult(settingsActivity, SETTINGS_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SETTINGS_REQUEST) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    boolean doDelete = extras.getBoolean("DELETE_DATAFILE");
                    if (doDelete == true) {
                        mTimeStore.deleteAndReinit();
                    }
                }
            }
            mTimeListAdapter.notifyDataSetChanged();
        }
        rescheduleTimer(mSharedPreferences.getInt("TimeFormatId", 0));
    }

}
