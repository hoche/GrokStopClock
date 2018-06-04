package com.grok.grokclock;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

public class Preferences extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        //to show the custom preference
        Preference customPref = (Preference) findPreference("customPref");
        customPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(getBaseContext(), "Custom Preference has been clicked",

                        Toast.LENGTH_LONG).show();
                SharedPreferences customSharedPreference = getSharedPreferences(
                        "myCustomSharedPrefs", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor =

                        customSharedPreference.edit();
                editor.putString("myCustomPref", "Custom Preference has been clicked");
                editor.commit();
                return true;
            }
        });
    }
}
