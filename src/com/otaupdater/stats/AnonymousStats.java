/*
 * Copyright (C) 2012 The CyanogenMod Project, OTA Update Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.otaupdater.stats;

import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.MenuItem;

import com.otaupdater.R;
import com.otaupdater.utils.Config;

@SuppressWarnings("deprecation")
public class AnonymousStats extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    private static final String VIEW_STATS = "pref_view_stats";
    protected static final String ANONYMOUS_OPT_IN = "pref_anonymous_opt_in";
    protected static final String ANONYMOUS_FIRST_BOOT = "pref_anonymous_first_boot";
    protected static final String ANONYMOUS_LAST_CHECKED = "pref_anonymous_checked_in";
    protected static final String ANONYMOUS_ALARM_SET = "pref_anonymous_alarm_set";
    protected static final String ANONYMOUS_REPORTED_VERSION = "pref_anonymous_reported_version";
    private CheckBoxPreference mEnableReporting;
    private Preference mViewStats;

    private Config cfg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar bar = getActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        cfg = Config.getInstance(getApplicationContext());

        addPreferencesFromResource(R.xml.annonymous_stats);

        mEnableReporting = (CheckBoxPreference) findPreference(ANONYMOUS_OPT_IN);
        mEnableReporting.setChecked(cfg.isStatsOptedIn());

        mViewStats = findPreference(VIEW_STATS);

        if (mEnableReporting.isChecked() && cfg.isStatsFirstRun()) {
            cfg.setStatsFirstRun(false);
            ReportingServiceManager.launchService(this);
        }

        Preference mId = findPreference("preview_id");
        mId.setSummary(Utilities.getUniqueID(getApplicationContext()));

        Preference mDevice = findPreference("preview_device");
        mDevice.setSummary(Utilities.getDevice());

        Preference mCountry = findPreference("preview_country");
        mCountry.setSummary(Utilities.getCountryCode(getApplicationContext()));

        Preference mCarrier = findPreference("preview_carrier");
        mCarrier.setSummary(Utilities.getCarrier(getApplicationContext()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mEnableReporting) {
            cfg.setStatsOptIn(mEnableReporting.isChecked());
        } else if (preference == mViewStats) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Config.STATS_VIEW_URL)));
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }
}
