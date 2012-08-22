package com.ota.updater.two;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class AddFavorite extends PreferenceActivity {
    @Override
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.accounts_screen);
    }

    @SuppressWarnings("deprecation")
    public void addPref(String... params) {
        String tweakName = params[0];
        Preference pref = new Preference(this);
        pref.setTitle(tweakName);
        pref.setIcon(R.drawable.ic_download_default);
        ((PreferenceScreen) findPreference("accounts_screen")).addPreference(pref);
    }
}
