package com.grok.stopclock;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.grok.stopclock.R;

public class SettingsActivity extends Activity {

    private final String LOGTAG = "SettingsActivity";

    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_settings);

        mSharedPreferences = getApplicationContext().getSharedPreferences("GrokStopClock", Context.MODE_PRIVATE);

        // Initialize the time format spinner
        Spinner spinner = (Spinner)findViewById(R.id.time_format_spinner);
        int id = mSharedPreferences.getInt("TimeFormatId", 0);
        if (id >= 0 && id < spinner.getCount()) {
            spinner.setSelection(id);
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                mSharedPreferences.edit().putInt("TimeFormatId", (int)id).commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // do nothing
            }
        });

        // Initialize the hour format spinner
        spinner = (Spinner)findViewById(R.id.hours_format_spinner);
        id = mSharedPreferences.getInt("HourFormatId", 0);
        if (id >= 0 && id < spinner.getCount()) {
            spinner.setSelection(id);
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                mSharedPreferences.edit().putInt("HourFormatId", (int)id).commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // do nothing
            }
        });

        // Initialize the car number requirement checkbox
        CheckBox cb = (CheckBox)findViewById(R.id.require_car_number_cb);
    }

    public void onBackButton(View view) {
        finish();
    }
}
