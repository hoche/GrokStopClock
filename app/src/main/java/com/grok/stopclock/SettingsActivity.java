package com.grok.stopclock;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.grok.stopclock.R;

public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        /*
        // to handle a custom preference
        Preference customPref = (Preference) findPreference("customPref");
        customPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(getBaseContext(), "Custom Preference has been clicked",
                        Toast.LENGTH_LONG).show();
                SharedPreferences customSharedPreference = getSharedPreferences(
                        "myCustomSharedPrefs", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = customSharedPreference.edit();
                editor.putString("myCustomPref", "Custom Preference has been clicked");
                editor.commit();
                return true;
            }
        });
        */
    }
}
