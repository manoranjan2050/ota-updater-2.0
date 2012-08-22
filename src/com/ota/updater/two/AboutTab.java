package com.ota.updater.two;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

import com.ota.updater.two.stats.AnonymousStats;

public class AboutTab extends PreferenceFragment implements OnPreferenceClickListener {

    private Preference aboutOtaUpdater;
    private Preference license;
    private Preference stats;
    private Preference followJieeHD;
    private Preference followVr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about);

        aboutOtaUpdater = findPreference("about_toolkit");
        aboutOtaUpdater.setSummary(" Copyright (C) 2012 - VillainROM \n Fully open-source \n Tap to visit our website");
        aboutOtaUpdater.setOnPreferenceClickListener(this);

        PackageInfo pInfo = null;
        try {
            pInfo = TabDisplay.mContext.getPackageManager().getPackageInfo(TabDisplay.mContext.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = pInfo == null ? "" : pInfo.versionName;

        Preference aboutAppVersion = findPreference("about_app");
        aboutAppVersion.setSummary(version);

        license = findPreference("opensource_license");
        license.setOnPreferenceClickListener(this);

        Preference stats = findPreference("toolkit_stats");
        stats.setOnPreferenceClickListener(this);

        followJieeHD = findPreference("follow_jieehd_pref");
        followJieeHD.setOnPreferenceClickListener(this);

        followVr = findPreference("follow_vr_pref");
        followVr.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == aboutOtaUpdater) {
            String url = "http://www.villainrom.co.uk";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);

            return true;
        } else if (preference == license) {
            Intent i = new Intent(TabDisplay.mContext, License.class);
            startActivity(i);

            return true;
        } else if (preference == stats) {
            Intent i = new Intent(TabDisplay.mContext, AnonymousStats.class);
            startActivity(i);

            return true;
        } else if (preference == followJieeHD) {
            String url = "http://www.twitter.com/jordancraig94";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);

            return true;
        } else if (preference == followVr) {
            String url = "http://www.twitter.com/VillainROM";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);

            return true;
        }
        return false;
    }
}
