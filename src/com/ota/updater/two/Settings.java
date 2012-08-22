package com.ota.updater.two;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity implements OnPreferenceClickListener {

    private Preference updatePref;

    @Override
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        updatePref = findPreference("update_settings_pref");
        updatePref.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == updatePref) {
            Intent i = new Intent();
            i.setClass(getApplicationContext(), UpdateSettings.class);
            startActivity(i);

            return true;
        }
        return false;
    }
}
