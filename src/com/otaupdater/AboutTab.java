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

package com.otaupdater;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

import com.otaupdater.stats.AnonymousStats;
import com.otaupdater.utils.Config;

public class AboutTab extends PreferenceFragment implements OnPreferenceClickListener {

    private Preference aboutOtaUpdater;
    private Preference license;
    private Preference stats;
    private Preference followGPlus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about);

        aboutOtaUpdater = findPreference("about_toolkit");
        aboutOtaUpdater.setOnPreferenceClickListener(this);

        PackageInfo pInfo = null;
        try {
            Context ctx = getActivity().getApplicationContext();
            pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = pInfo == null ? getString(R.string.about_version_unknown) : pInfo.versionName;

        Preference aboutAppVersion = findPreference("about_app");
        aboutAppVersion.setSummary(version);

        license = findPreference("opensource_license");
        license.setOnPreferenceClickListener(this);

        Preference stats = findPreference("toolkit_stats");
        stats.setOnPreferenceClickListener(this);

        followGPlus = findPreference("follow_gplus_pref");
        followGPlus.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == aboutOtaUpdater) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(Config.WEB_HOME_URL));
            startActivity(i);

            return true;
        } else if (preference == license) {
            Intent i = new Intent(getActivity(), License.class);
            startActivity(i);

            return true;
        } else if (preference == stats) {
            Intent i = new Intent(getActivity(), AnonymousStats.class);
            startActivity(i);

            return true;
        } else if (preference == followGPlus) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(Config.GPLUS_URL));
            startActivity(i);

            return true;
        }
        return false;
    }
}
