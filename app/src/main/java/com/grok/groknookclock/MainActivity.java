package com.grok.groknookclock;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

enum eSubTimeType {
    SECONDS_ONLY,
    SECONDS_AND_TENTHS,
    HUNDREDTHS_OF_MINUTE
}

public class MainActivity extends Activity {

    private static final String LOGTAG = "MainActivity";

    private Calendar mCalendar;
    TextView mTvClockTime;
    eSubTimeType mSubTimeType;
    Timer mTimer;

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

        setContentView(R.layout.activity_main);

        mCalendar = Calendar.getInstance();
        mTvClockTime = (TextView) findViewById(R.id.clock_time);

        mSubTimeType = eSubTimeType.SECONDS_ONLY;

        mTimer = new Timer("ClockTimer");

        // update every 200 milliseconds
        // It'd be nice to update faster, but that's about as fast as the display can
        // redraw.
        mTimer.scheduleAtFixedRate(new ClockUpdateTask(), 100, 200);

        redrawTimeLayout();
        updateTime();
    }

    protected void redrawTimeLayout()
    {
        TableLayout table = (TableLayout)findViewById(R.id.time_table);
        table.removeAllViews();

        LayoutInflater inflater = getLayoutInflater();

        for(int i = 0; i < 9; i++) {
            TableRow row = (TableRow)inflater.inflate(R.layout.time_table_row, table, false);

            TextView entry_num = (TextView)row.findViewById(R.id.entry_number);
            TextView entry_time = (TextView)row.findViewById(R.id.entry_time);

            if (i == 0) {
                // title row
                row.setBackgroundColor(Color.GRAY);
                entry_num.setText("ID");
                entry_time.setText("Time");
            } else {
                entry_num.setText("" + i);
                entry_time.setText("00:00:0" + i); // set the text for the header
            }
            // other customizations to the row

            table.addView(row);
        }
    }

    private void updateTime() {

        //LogUtil.INSTANCE.d(LOGTAG, "updateTime()");

        mCalendar.setTimeInMillis(System.currentTimeMillis());

        int hours = mCalendar.get(mCalendar.HOUR_OF_DAY);
        int mins = mCalendar.get(mCalendar.MINUTE);
        int secs = mCalendar.get(mCalendar.SECOND);
        int millis = mCalendar.get(mCalendar.MILLISECOND);

        String displayTime = "";

        switch (mSubTimeType) {
            case SECONDS_ONLY: {
                DecimalFormat hf = new DecimalFormat("#0");
                DecimalFormat df = new DecimalFormat("00");
                displayTime = hf.format(hours) + ":" +
                        df.format(mins) + ":" +
                        df.format(secs);
            }
            break;

            case SECONDS_AND_TENTHS: {
                DecimalFormat hf = new DecimalFormat("#0");
                DecimalFormat df = new DecimalFormat("00");
                DecimalFormat sf = new DecimalFormat("0");
                displayTime = hf.format(hours) + ":" +
                        df.format(mins) + ":" +
                        df.format(secs) + "." +
                        sf.format(millis / 100);
            }
            break;

            case HUNDREDTHS_OF_MINUTE: {
                DecimalFormat hf = new DecimalFormat("#0");
                DecimalFormat df = new DecimalFormat("00");
                displayTime = hf.format(hours) + ":" +
                        df.format(mins) + ":" +
                        df.format(((secs * 1000) + millis)/600);
            }
            break;
        }

        // Only change if necessary to reduce flicker
        // Investigate to see if it's worthwhile just updating the seconds
        String oldDisplayTime = mTvClockTime.getText().toString();
        if (!oldDisplayTime.equals(displayTime)) {
            mTvClockTime.setText(displayTime);
        }

    }
}
