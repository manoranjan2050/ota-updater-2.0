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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import com.otaupdater.utils.Config;
import com.otaupdater.utils.Utils;

public class ReportingServiceManager extends BroadcastReceiver {

    public static final long dMill = 86400000; // ms in 1 day - 24 * 60 * 60 * 1000;
    public static final long tFrame = 604800000; // ms in 1 week - 7 * dMill;

    @Override
    public void onReceive(Context ctx, Intent intent) {
        final Context context = ctx.getApplicationContext();

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            setAlarm(context);
        } else {
            launchService(context);
        }
    }

    protected static void setAlarm(Context ctx) {
        final Config cfg = Config.getInstance(ctx);

        cfg.setStatsAlarmSet(false);

        if (!cfg.isStatsOptedIn() || cfg.isStatsFirstRun()) return;

        long lastSynced = cfg.getStatsLastReport();
        if (lastSynced == 0) {
            return;
        }

        long timeLeft = (lastSynced + tFrame) - System.currentTimeMillis();
        Intent i = new Intent(ConnectivityManager.CONNECTIVITY_ACTION);
        i.setComponent(new ComponentName(ctx.getPackageName(), ReportingServiceManager.class.getName()));
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC, System.currentTimeMillis() + timeLeft, PendingIntent.getBroadcast(ctx, 0, i, 0));
        Log.d(ReportingService.TAG, "Next sync attempt in : " + timeLeft / dMill + " days");
        cfg.setStatsAlarmSet(true);
    }

    public static void launchService(Context ctx) {
        final Config cfg = Config.getInstance(ctx);
        if (Utils.dataAvailable(ctx)) {
            if (cfg.isStatsAlarmSet()) return;

            if (cfg.isStatsOptedIn()) {
                long lastSynced = cfg.getStatsLastReport();
                boolean shouldSync = false;
                if (lastSynced == 0) {
                    shouldSync = true;
                } else if (System.currentTimeMillis() - lastSynced >= tFrame) {
                    shouldSync = true;
                }

                if (shouldSync) {
                    Intent sIntent = new Intent();
                    sIntent.setComponent(new ComponentName(ctx.getPackageName(), ReportingService.class.getName()));
                    ctx.startService(sIntent);
                } else {
                    setAlarm(ctx);
                }
            }
        }
    }
}
