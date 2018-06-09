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
    private TextView mTvClockMinutesSeparator;
    private TextView mTvClockSubTime;

    private ListView mTimeList;
    private TimeListAdapter mTimeListAdapter;
    private Timer mTimer;

    private TimeStore mTimeStore;

    private int mDisplayFormatId;
    private int mDisplayHour;
    private int mDisplayMin;

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
        mDisplayFormatId = mSharedPreferences.getInt("TimeFormatId", 0);

        mDisplayHour = 0;
        mDisplayMin = 0;

        setContentView(R.layout.activity_main);

        mCalendar = Calendar.getInstance();

        mTvClockHours = (TextView) findViewById(R.id.clock_hours);
        mTvClockMinutes = (TextView) findViewById(R.id.clock_minutes);
        mTvClockMinutesSeparator = (TextView) findViewById(R.id.clock_minutes_separator);
        mTvClockSubTime = (TextView) findViewById(R.id.clock_subtime);

        mTimeStore = new TimeStore(this);
        mTimeStore.init();

        mTimeList = (ListView) findViewById(R.id.time_list);
        View header = getLayoutInflater().inflate(R.layout.time_table_header, null);
        mTimeList.addHeaderView(header); // have to do this before setAdapter
        mTimeListAdapter = new TimeListAdapter(this, mTimeStore);
        mTimeList.setAdapter(mTimeListAdapter); // have to this after addHeaderView
    }

    @Override
    protected void onResume() {
        super.onResume();

        // update every 100 milliseconds
        mTimer = new Timer("ClockTimer");
        mTimer.scheduleAtFixedRate(new ClockUpdateTask(), 100, 100);

        updateTime();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mTimer.cancel();
        mTimer = null;
    }

    private void updateTime() {

        mCalendar.setTimeInMillis(System.currentTimeMillis());

        int hour = mCalendar.get(mCalendar.HOUR_OF_DAY);
        int min = mCalendar.get(mCalendar.MINUTE);
        int sec = mCalendar.get(mCalendar.SECOND);
        int tenth = mCalendar.get(mCalendar.MILLISECOND) / 100;

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
        String newTime;
        switch (timeFormatId) {
            case 1:  // tenths of a second
                if (mDisplayFormatId != timeFormatId) {
                    mTvClockMinutesSeparator.setText(":");
                }
                newTime = String.format("%02d.%d", sec, tenth);
                break;

            case 2:  // hundredths of a minute
                if (mDisplayFormatId != timeFormatId) {
                    mTvClockMinutesSeparator.setText(".");
                }
                newTime = String.format("%02d", ((sec * 1000) + tenth)/600);
                break;

            case 0:  // seconds only
            default:
                if (mDisplayFormatId != timeFormatId) {
                    mTvClockMinutesSeparator.setText(":");
                }
                newTime = String.format("%02d", sec + ((tenth >= 5) ? 1 : 0) );
        }

        if (mTvClockSubTime.getText().toString() != newTime) {
            mTvClockSubTime.setText(newTime);
        }
        mDisplayFormatId = timeFormatId;
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
    }

}
