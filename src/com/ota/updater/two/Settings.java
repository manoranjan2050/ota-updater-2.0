/*
 * Copyright (C) 2012 OTA Update Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may only use this file in compliance with the license and provided you are not associated with or are in co-operation anyone by the name 'X Vanderpoel'.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ota.updater.two;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import com.ota.updater.two.utils.Config;

public class Settings extends PreferenceActivity {

    private Config cfg;

    private CheckBoxPreference notifPref;
    private CheckBoxPreference wifidlPref;
    private Preference resetWarnPref;
    private Preference prokeyPref;
    private Preference donatePref;

    @Override
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        cfg = Config.getInstance(getApplicationContext());

        notifPref = (CheckBoxPreference) findPreference("notif_pref");
        notifPref.setChecked(cfg.getShowNotif());

        wifidlPref = (CheckBoxPreference) findPreference("wifidl_pref");
        wifidlPref.setChecked(cfg.getWifiOnlyDl());

        resetWarnPref = findPreference("resetwarn_pref");
        prokeyPref = findPreference("prokey_pref");
        donatePref = findPreference("donate_pref");
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == notifPref) {
            cfg.setShowNotif(notifPref.isChecked());
            return true;
        } else if (preference == wifidlPref) {
            cfg.setWifiOnlyDl(wifidlPref.isChecked());
            return true;
        } else if (preference == resetWarnPref) {
            cfg.setIgnoredDataWarn(false);
            cfg.setIgnoredUnsupportedWarn(false);
        } else if (preference == prokeyPref) {
            //TODO in-app billing
        } else if (preference == donatePref) {
            //TODO paypal donate
        }
        return false;
    }

}
