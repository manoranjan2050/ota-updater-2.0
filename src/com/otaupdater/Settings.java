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

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.MenuItem;

import com.otaupdater.stats.AnonymousStats;
import com.otaupdater.utils.Config;
import com.otaupdater.utils.Utils;

public class Settings extends PreferenceActivity {

    private Config cfg;

    private CheckBoxPreference notifPref;
    private CheckBoxPreference wifidlPref;
    private Preference resetWarnPref;
    private Preference statsPref;
    private Preference prokeyPref;
    private Preference donatePref;

    @Override
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar bar = getActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        cfg = Config.getInstance(getApplicationContext());
        if (Utils.haveProKey(getApplicationContext()) && !cfg.hasValidProKey() &&
                (!cfg.isProKeyTemporary() || cfg.getKeyExpires() < System.currentTimeMillis())) {
            Utils.verifyProKey(getApplicationContext());
        }

        addPreferencesFromResource(R.xml.settings);


        notifPref = (CheckBoxPreference) findPreference("notif_pref");
        notifPref.setChecked(cfg.getShowNotif());

        wifidlPref = (CheckBoxPreference) findPreference("wifidl_pref");
        wifidlPref.setChecked(cfg.getWifiOnlyDl());

        prokeyPref = findPreference("prokey_pref");
        if (Utils.haveProKey(getApplicationContext())) {
            if (cfg.hasValidProKey()) {
                prokeyPref.setSummary(R.string.settings_prokey_summary_pro);
            } else if (cfg.isVerifyingProKey()) {
                prokeyPref.setSummary(R.string.settings_prokey_summary_verifying);
            } else {
                prokeyPref.setSummary(R.string.settings_prokey_summary_verify);
            }
        } else if (cfg.hasValidProKey()) {
            prokeyPref.setSummary(R.string.settings_prokey_summary_redeemed);
        } else if (!Utils.marketAvailable(getApplicationContext())) {
            prokeyPref.setSummary(R.string.settings_prokey_summary_nomarket);
        }

        resetWarnPref = findPreference("resetwarn_pref");
        statsPref = findPreference("stats_pref");
        donatePref = findPreference("donate_pref");
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
    @SuppressWarnings("deprecation")
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == notifPref) {
            cfg.setShowNotif(notifPref.isChecked());
        } else if (preference == wifidlPref) {
            cfg.setWifiOnlyDl(wifidlPref.isChecked());
        } else if (preference == resetWarnPref) {
            cfg.setIgnoredDataWarn(false);
            cfg.setIgnoredUnsupportedWarn(false);
        } else if (preference == statsPref) {
            Intent i = new Intent(this, AnonymousStats.class);
            startActivity(i);
        } else if (preference == prokeyPref) {
            if (Utils.haveProKey(getApplicationContext())) {
                if (cfg.hasValidProKey()) {
                    AlertDialog.Builder dlg = new AlertDialog.Builder(this);
                    dlg.setMessage(R.string.prokey_thanks);
                    dlg.setNeutralButton(R.string.alert_close, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dlg.create().show();
                } else {
                    Utils.verifyProKey(getApplicationContext());
                }
            } else if (cfg.hasValidProKey()) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(this);
                dlg.setMessage(R.string.prokey_redeemed_thanks);
                dlg.setNeutralButton(R.string.alert_close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dlg.create().show();
            } else {
                AlertDialog.Builder dlg = new AlertDialog.Builder(this);
                dlg.setTitle(R.string.settings_prokey_title);

                final boolean market = Utils.marketAvailable(this);
                dlg.setItems(market ? R.array.prokey_ops : R.array.prokey_ops_nomarket, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        which -= market ? 1 : 0;
                        switch (which) {
                        case -1:
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + Config.KEY_PACKAGE)));
                            break;
                        case 0:
                            //TODO code redeem
                            break;
                        case 1:
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Config.PP_DONATE_URL)));
                            break;
                        }
                    }
                });

                dlg.create().show();
            }
        } else if (preference == donatePref) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Config.PP_DONATE_URL)));
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        return true;
    }
}
