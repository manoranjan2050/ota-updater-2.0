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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;
import com.ota.updater.two.utils.Config;
import com.ota.updater.two.utils.FetchKernelInfoTask;
import com.ota.updater.two.utils.FetchKernelInfoTask.KernelInfoListener;
import com.ota.updater.two.utils.FetchRomInfoTask;
import com.ota.updater.two.utils.FetchRomInfoTask.RomInfoListener;
import com.ota.updater.two.utils.KernelInfo;
import com.ota.updater.two.utils.RomInfo;
import com.ota.updater.two.utils.Utils;

public class UpdateCheckReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        final Context context = ctx.getApplicationContext();
        final Config cfg = Config.getInstance(context);

        if (cfg.hasStoredRomUpdate()) {
            RomInfo info = cfg.getStoredRomUpdate();
            if (Utils.isRomUpdate(info)) {
                if (cfg.getShowNotif()) {
                    Utils.showRomUpdateNotif(context, info);
                    Log.v(Config.LOG_TAG + "Receiver", "Found stored rom update");
                } else {
                    Log.v(Config.LOG_TAG + "Receiver", "Found stored rom update, notif not shown");
                }
            } else {
                Log.v(Config.LOG_TAG + "Receiver", "Found invalid stored rom update");
                cfg.clearStoredRomUpdate();
                Utils.clearRomUpdateNotif(context);
            }
        } else {
            Log.v(Config.LOG_TAG + "Receiver", "No stored rom update");
        }

        if (cfg.hasStoredKernelUpdate()) {
            KernelInfo info = cfg.getStoredKernelUpdate();
            if (Utils.isKernelUpdate(info)) {
                if (cfg.getShowNotif()) {
                    Utils.showKernelUpdateNotif(context, info);
                    Log.v(Config.LOG_TAG + "Receiver", "Found stored kernel update");
                } else {
                    Log.v(Config.LOG_TAG + "Receiver", "Found stored kernel update, notif not shown");
                }
            } else {
                Log.v(Config.LOG_TAG + "Receiver", "Found invalid stored kernel update");
                cfg.clearStoredKernelUpdate();
                Utils.clearKernelUpdateNotif(context);
            }
        } else {
            Log.v(Config.LOG_TAG + "Receiver", "No stored kernel update");
        }

        if (Utils.isRomOtaEnabled() || Utils.isKernelOtaEnabled()) {
            if (Utils.marketAvailable(context)) {
                Log.v(Config.LOG_TAG + "Receiver", "Found market, trying GCM");
                GCMRegistrar.checkDevice(context);
                GCMRegistrar.checkManifest(context);
                final String regId = GCMRegistrar.getRegistrationId(context);
                if (regId.length() != 0) {
                    if (cfg.upToDate()) {
                        Log.v(Config.LOG_TAG + "GCMRegister", "Already registered");
                    } else {
                        Log.v(Config.LOG_TAG + "GCMRegister", "Already registered, out-of-date");
                        cfg.setValuesToCurrent();
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                Utils.updateGCMRegistration(context, regId);
                                return null;
                            }
                        }.execute();
                    }
                } else {
                    GCMRegistrar.register(context, Config.GCM_SENDER_ID);
                    Log.v(Config.LOG_TAG + "GCMRegister", "GCM registered");
                }
            } else {
                Log.v(Config.LOG_TAG + "Receiver", "No market, using pull method");
                if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                    setAlarm(context);
                }

                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

                if (Utils.isRomOtaEnabled()) {
                    final WakeLock romWL = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, UpdateCheckReceiver.class.getName());
                    romWL.acquire();

                    new FetchRomInfoTask(context, new RomInfoListener() {
                        @Override
                        public void onStartLoading() { }
                        @Override
                        public void onLoaded(RomInfo info) {
                            if (Utils.isRomUpdate(info)) {
                                cfg.storeRomUpdate(info);
                                if (cfg.getShowNotif()) {
                                    Utils.showRomUpdateNotif(context, info);
                                } else {
                                    Log.v(Config.LOG_TAG + "Receiver", "found rom update, notif not shown");
                                }
                            } else {
                                cfg.clearStoredRomUpdate();
                                Utils.clearRomUpdateNotif(context);
                            }

                            romWL.release();
                        }
                        @Override
                        public void onError(String error) {
                            romWL.release();
                        }
                    }).execute();
                }

                if (Utils.isKernelOtaEnabled()) {
                    final WakeLock kernelWL = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, UpdateCheckReceiver.class.getName());
                    kernelWL.acquire();

                    new FetchKernelInfoTask(context, new KernelInfoListener() {
                        @Override
                        public void onStartLoading() { }
                        @Override
                        public void onLoaded(KernelInfo info) {
                            if (Utils.isKernelUpdate(info)) {
                                cfg.storeKernelUpdate(info);
                                if (cfg.getShowNotif()) {
                                    Utils.showKernelUpdateNotif(context, info);
                                } else {
                                    Log.v(Config.LOG_TAG + "Receiver", "found kernel update, notif not shown");
                                }
                            } else {
                                cfg.clearStoredKernelUpdate();
                                Utils.clearKernelUpdateNotif(context);
                            }

                            kernelWL.release();
                        }
                        @Override
                        public void onError(String error) {
                            kernelWL.release();
                        }
                    }).execute();
                }
            }
        } else {
            Log.w(Config.LOG_TAG + "Receiver", "Unsupported ROM");
        }
    }

    protected static void setAlarm(Context ctx) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ctx, UpdateCheckReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        am.cancel(pendingIntent);
        am.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }
}
