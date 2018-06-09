package com.grok.stopclock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.grok.stopclock.R;

public class SettingsActivity extends Activity {

    private final String LOGTAG = "SettingsActivity";

    private SharedPreferences mSharedPreferences;
    private boolean mDeleteDataFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int windowFlags = WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        getWindow().setFlags(windowFlags, windowFlags);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

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
        boolean twelveHourTime = mSharedPreferences.getBoolean("Use12HourTime", false);
        spinner.setSelection(twelveHourTime ? 0 : 1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                mSharedPreferences.edit().putBoolean("Use12HourTime", (id == 0) ).commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // do nothing
            }
        });

        // Initialize the car number requirement checkbox
        CheckBox cb = (CheckBox)findViewById(R.id.require_car_number_cb);
        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSharedPreferences.edit().putBoolean("RequireCarNumber", ((CheckBox)v).isChecked()).commit();
                Toast.makeText(SettingsActivity.this, "This doesn't actually do anything yet.",
                        Toast.LENGTH_LONG).show();
            }
        });

        // Initialize the clear data button
        mDeleteDataFile = false;
        Button btn = (Button)findViewById(R.id.clear_datafile_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("Confirm")
                        .setMessage("Do you really want to clear the saved times?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mDeleteDataFile = true;
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });
    }

    public void onBackButton(View view) {
        Intent intent = new Intent();
        intent.putExtra("DELETE_DATAFILE", mDeleteDataFile);
        setResult(RESULT_OK, intent);
        finish();
    }
}
