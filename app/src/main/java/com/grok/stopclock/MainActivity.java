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
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
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
import java.util.Formatter;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    private final String LOGTAG = "MainActivity";

    private SharedPreferences mSharedPreferences;

    private Calendar mCalendar;
    private TextView mTvClockTime;
    private ListView mTimeList;
    private TimeListAdapter mTimeListAdapter;
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

        LogUtil.INSTANCE.d(LOGTAG, "onCreate()");

        int windowFlags = WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        getWindow().setFlags(windowFlags, windowFlags);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        mSharedPreferences = getApplicationContext().getSharedPreferences("GrokStopClock", Context.MODE_PRIVATE);

        setContentView(R.layout.activity_main);

        mCalendar = Calendar.getInstance();
        mTvClockTime = (TextView) findViewById(R.id.clock_time);

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

        // update every 200 milliseconds
        // It'd be nice to update faster, but that's about as fast as the display can
        // redraw.
        mTimer = new Timer("ClockTimer");
        mTimer.scheduleAtFixedRate(new ClockUpdateTask(), 100, 200);

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

        // This is all temporary. It'd be nice to use a common time-formatter between this
        // and the list adapter, but since we're updating this one a lot we want to minimize
        // the amount that we have to redraw, so this one's optimized for that.
        StringBuilder sbuf = new StringBuilder();
        Formatter fmt = new Formatter(sbuf);
        switch (mSharedPreferences.getInt("TimeFormatId", 0)) {
            case 1:
                fmt.format("%d:%02d:%02d.%d", hour, min, sec, tenth);
                break;

            case 2:
                fmt.format("%d:%02d.%02d", hour, min, ((sec * 1000) + tenth)/600);
                break;

            case 0:
            default:
                fmt.format("%d:%02d:%02d", hour, min, sec + ((tenth >= 5) ? 1 : 0) );
        }
        String displayTime = sbuf.toString();

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
        mTimeListAdapter.notifyDataSetChanged();
    }

    public void onSettingsButton(View view) {
        Intent settingsActivity = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(settingsActivity);
    }

}
