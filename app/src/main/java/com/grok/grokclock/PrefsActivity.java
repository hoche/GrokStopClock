/*
 * GrokClock PrefsActivity.java
 *
 * Show/Edit/Save preferences
 *
 * Copyright (C) 2011 Michel Hoche-Mong, hoche@grok.com
 *
 */

package com.grok.grokclock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import java.util.prefs.Preferences;

public class PrefsActivity extends Activity {

    EditText editText;
    Button bt;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        editText = (EditText) findViewById(R.id.editText1);
        bt = (Button) findViewById(R.id.button1);
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        String val = prefs.getString("pref", "some text");
        editText.setText(val);

        bt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("pref", editText.getText().toString());
                editor.commit();
            }
        });

        Button prefBtn = (Button) findViewById(R.id.prefButton);
        prefBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent settingsActivity = new Intent(getApplicationContext(), Preferences.class);
                startActivity(settingsActivity);
            }
        });

    }
}
