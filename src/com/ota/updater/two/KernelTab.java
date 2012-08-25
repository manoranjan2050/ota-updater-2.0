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

import java.text.DateFormat;
import java.util.Date;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import com.ota.updater.two.utils.Config;
import com.ota.updater.two.utils.FetchKernelInfoTask;
import com.ota.updater.two.utils.FetchKernelInfoTask.KernelInfoListener;
import com.ota.updater.two.utils.KernelInfo;
import com.ota.updater.two.utils.Utils;

public class KernelTab extends PreferenceFragment {

    private boolean fetching = false;
    private Preference availUpdatePref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utils.isKernelOtaEnabled()) {
            addPreferencesFromResource(R.xml.kernel);

            String kernelVersion = Utils.getKernelOtaVersion();
            if (kernelVersion == null) kernelVersion = getString(R.string.main_kernel_version_unknown);
            Date kernelDate = Utils.getKernelOtaDate();
            if (kernelDate != null) {
                kernelVersion += " (" + DateFormat.getDateTimeInstance().format(kernelDate) + ")";
            }

            final Preference device = findPreference("device_view");
            device.setSummary(android.os.Build.DEVICE.toLowerCase());
            final Preference kernel = findPreference("kernel_view");
            kernel.setSummary(Utils.getKernelVersion());
            final Preference version = findPreference("version_view");
            version.setSummary(kernelVersion);
            final Preference build = findPreference("otaid_view");
            build.setSummary(Utils.getKernelOtaID());

            availUpdatePref = findPreference("avail_updates");
        } else {
            addPreferencesFromResource(R.xml.kernel_unsupported);

            final Preference device = findPreference("device_view");
            device.setSummary(android.os.Build.DEVICE.toLowerCase());
            final Preference kernel = findPreference("kernel_view");
            kernel.setSummary(Utils.getKernelVersion());
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == availUpdatePref) {
            if (!fetching) checkForKernelUpdates();
        }
        return false;
    }

    private void checkForKernelUpdates() {
        if (fetching) return;
        final Config cfg = Config.getInstance(getActivity().getApplicationContext());
        new FetchKernelInfoTask(getActivity(), new KernelInfoListener() {
            @Override
            public void onStartLoading() {
                fetching = true;
            }
            @Override
            public void onLoaded(KernelInfo info) {
                fetching = false;
                if (info == null) {
                    availUpdatePref.setSummary(getString(R.string.main_updates_error, "Unknown error"));
                    Toast.makeText(getActivity(), R.string.toast_fetch_error, Toast.LENGTH_SHORT).show();
                } else if (Utils.isKernelUpdate(info)) {
                    cfg.storeKernelUpdate(info);
                    if (cfg.getShowNotif()) {
                        Utils.showKernelUpdateNotif(getActivity(), info);
                    } else {
                        Log.v(Config.LOG_TAG + "KernelTab", "found kernel update, notif not shown");
                    }
                    //TODO show kernel update dialog
                } else {
                    cfg.clearStoredKernelUpdate();
                    availUpdatePref.setSummary(R.string.main_updates_none);
                    Toast.makeText(getActivity(), R.string.toast_no_updates, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onError(String error) {
                fetching = false;
                availUpdatePref.setSummary(getString(R.string.main_updates_error, error));
                Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }
}
