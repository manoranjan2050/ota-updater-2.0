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

public class AboutTab extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about);

        Preference about_vr_toolkit = findPreference("about_toolkit");
        about_vr_toolkit.setSummary(" Copyright (C) 2012 - VillainROM \n Fully open-source \n Tap to visit our website");
        about_vr_toolkit.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String url = "http://www.villainrom.co.uk";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                return true;
            }
        });

        PackageInfo pInfo = null;
        try {
            pInfo = TabDisplay.mContext.getPackageManager().getPackageInfo(TabDisplay.mContext.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = pInfo.versionName;

        Preference about_app_version = findPreference("about_app");
        about_app_version.setSummary(version);
        about_app_version.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                return true;
            }
        });

        Preference licence = findPreference("opensource_license");
        licence.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(TabDisplay.mContext, License.class);
                startActivity(i);
                return false;
            }
        });

        Preference stats = findPreference("toolkit_stats");
        stats.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(TabDisplay.mContext, AnonymousStats.class);
                startActivity(i);
                return false;
            }
        });

        Preference follow_jieehd = findPreference("follow_jieehd_pref");
        follow_jieehd.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                String url = "http://www.twitter.com/jordancraig94";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                return false;
            }
        });

        Preference follow_vr = findPreference("follow_vr_pref");
        follow_vr.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                String url = "http://www.twitter.com/VillainROM";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                return false;
            }
        });
    }
}
