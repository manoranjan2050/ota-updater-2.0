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

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.otaupdater.utils.Config;

public class ReportingService extends Service {
    protected static final String TAG = Config.LOG_TAG + "Stats";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "User has opted in -- reporting.");
        Thread thread = new Thread() {
            @Override
            public void run() {
                report();
            }
        };
        thread.start();

        return Service.START_REDELIVER_INTENT;
    }

    private void report() {
        final Context context = getApplicationContext();

        String deviceId = Utilities.getUniqueID(context);
        String deviceName = Utilities.getDevice();
        String deviceCountry = Utilities.getCountryCode(context);
        String deviceCarrier = Utilities.getCarrier(context);
        String deviceCarrierId = Utilities.getCarrierId(context);

        Log.d(TAG, "SERVICE: Device ID=" + deviceId);
        Log.d(TAG, "SERVICE: Device Name=" + deviceName);
        Log.d(TAG, "SERVICE: Country=" + deviceCountry);
        Log.d(TAG, "SERVICE: Carrier=" + deviceCarrier);
        Log.d(TAG, "SERVICE: Carrier ID=" + deviceCarrierId);

        try {
            List<NameValuePair> kv = new ArrayList<NameValuePair>(5);
            kv.add(new BasicNameValuePair("hash", deviceId));
            kv.add(new BasicNameValuePair("device", deviceName));
            kv.add(new BasicNameValuePair("country", deviceCountry));
            kv.add(new BasicNameValuePair("carrier", deviceCarrier));
            kv.add(new BasicNameValuePair("carrier_id", deviceCarrierId));

            HttpPost post = new HttpPost(Config.STATS_REPORT_URL);
            post.setEntity(new UrlEncodedFormEntity(kv));

            HttpClient httpc = new DefaultHttpClient();
            httpc.execute(post);

            Config.getInstance(context).setStatsLastReport(System.currentTimeMillis());
        } catch (Exception e) {
            Log.e(TAG, "Got Exception", e);
        }
        ReportingServiceManager.setAlarm(context);
        stopSelf();
    }
}
