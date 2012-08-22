package com.ota.updater.two;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class UpdateSettings extends PreferenceActivity {
    @Override
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.update_settings);
    }
}
