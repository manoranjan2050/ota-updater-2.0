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

package com.otaupdater.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.otaupdater.DownloadReceiver;
import com.otaupdater.R;
import com.otaupdater.TabDisplay;

public class KernelInfo {
    public String kernelName;
    public String version;
    public String changelog;
    public String url;
    public String md5;
    public Date date;

    public KernelInfo(String romName, String version, String changelog, String downurl, String md5, Date date) {
        this.kernelName = romName;
        this.version = version;
        this.changelog = changelog;
        this.url = downurl;
        this.md5 = md5;
        this.date = date;
    }

    public static KernelInfo fromIntent(Intent i) {
        return new KernelInfo(
                i.getStringExtra("kernel_info_name"),
                i.getStringExtra("kernel_info_version"),
                i.getStringExtra("kernel_info_changelog"),
                i.getStringExtra("kernel_info_url"),
                i.getStringExtra("kernel_info_md5"),
                Utils.parseDate(i.getStringExtra("kernel_info_date")));
    }

    public void addToIntent(Intent i) {
        i.putExtra("kernel_info_name", kernelName);
        i.putExtra("kernel_info_version", version);
        i.putExtra("kernel_info_changelog", changelog);
        i.putExtra("kernel_info_url", url);
        i.putExtra("kernel_info_md5", md5);
        i.putExtra("kernel_info_date", Utils.formatDate(date));
    }

    public void showUpdateNotif(Context ctx) {
        Intent mainInent = new Intent(ctx, TabDisplay.class);
        mainInent.setAction(TabDisplay.KERNEL_NOTIF_ACTION);
        this.addToIntent(mainInent);
        PendingIntent mainPIntent = PendingIntent.getActivity(ctx, 0, mainInent, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent dlInent = new Intent(ctx, DownloadReceiver.class);
        dlInent.setAction(DownloadReceiver.DL_KERNEL_ACTION);
        this.addToIntent(dlInent);
        PendingIntent dlPIntent = PendingIntent.getBroadcast(ctx, 0, dlInent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx);
        builder.setContentIntent(mainPIntent);
        builder.setContentTitle(ctx.getString(R.string.notif_source));
        builder.setContentText(ctx.getString(R.string.notif_text_kernel));
        builder.setTicker(ctx.getString(R.string.notif_text_kernel));
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.drawable.updates);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(changelog));
        builder.setPriority(NotificationCompat.PRIORITY_LOW);
        builder.addAction(R.drawable.ic_download_default, ctx.getString(R.string.notif_download), dlPIntent);

        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(Config.KERNEL_NOTIF_ID, builder.build());
    }

    public static void clearUpdateNotif(Context ctx) {
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(Config.KERNEL_NOTIF_ID);
    }

    @TargetApi(11)
    public long fetchFile(Context ctx) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(ctx.getString(R.string.notif_downloading));
        request.setDescription(kernelName);
        request.setVisibleInDownloadsUi(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }

        request.setDestinationUri(Uri.fromFile(new File(Config.KERNEL_DL_PATH_FILE, getDownloadFileName())));

        int netTypes = DownloadManager.Request.NETWORK_WIFI;
        if (!Config.getInstance(ctx).getWifiOnlyDl()) netTypes |= DownloadManager.Request.NETWORK_MOBILE;
        request.setAllowedNetworkTypes(netTypes);

        DownloadManager manager = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
        return manager.enqueue(request);
    }

    public String getDownloadFileName() {
        return Utils.sanitizeName(kernelName + "__" + version + ".zip");
    }

    public void showUpdateDialog(final Context ctx) {
        AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
        alert.setTitle(R.string.alert_update_title);
        alert.setMessage(ctx.getString(R.string.alert_update_kernel_to, kernelName, version));

        alert.setPositiveButton(R.string.alert_download, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();

                final File file = new File(Config.KERNEL_DL_PATH_FILE, getDownloadFileName());
                if (file.exists()) {
                    Log.v("OTA::Download", "Found old zip, checking md5");

                    InputStream is = null;
                    try {
                        is = new FileInputStream(file);
                        MessageDigest digest = MessageDigest.getInstance("MD5");
                        byte[] data = new byte[4096];
                        int nRead = -1;
                        while ((nRead = is.read(data)) != -1) {
                            digest.update(data, 0, nRead);
                        }
                        String oldMd5 = Utils.byteArrToStr(digest.digest());
                        Log.v("OTA::Download", "old zip md5: " + oldMd5);
                        if (!md5.equalsIgnoreCase(oldMd5)) {
                            file.delete();
                        } else {
                            //TODO show flash dialog
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        file.delete();
                    } finally {
                        if (is != null) {
                            try { is.close(); }
                            catch (Exception e) { }
                        }
                    }
                }

                final long dlID = fetchFile(ctx);

                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setTitle(R.string.alert_downloading);
                builder.setMessage(ctx.getString(R.string.alert_downloading_changelog, changelog));
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.alert_hide, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        DownloadManager dm = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
                        dm.remove(dlID);
                    }
                });
            }
        });

        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.create().show();
    }

    public static void fetchInfo(Context ctx) {
        fetchInfo(ctx, null);
    }

    public static void fetchInfo(Context ctx, KernelInfoListener callback) {
        new FetchInfoTask(ctx, callback).execute();
    }

    protected static class FetchInfoTask extends AsyncTask<Void, Void, KernelInfo> {
        private KernelInfoListener callback = null;
        private Context context = null;
        private String error = null;

        public FetchInfoTask(Context ctx, KernelInfoListener callback) {
            this.context = ctx;
            this.callback = callback;
        }

        @Override
        public void onPreExecute() {
            if (callback != null) callback.onStartLoading();
        }

        @Override
        protected KernelInfo doInBackground(Void... notused) {
            if (!Utils.isKernelOtaEnabled()) {
                error = context.getString(R.string.kernel_unsupported_title);
                return null;
            }
            if (!Utils.dataAvailable(context)) {
                error = context.getString(R.string.alert_nodata_title);
                return null;
            }

            try {
                ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
                params.add(new BasicNameValuePair("device", android.os.Build.DEVICE.toLowerCase()));
                params.add(new BasicNameValuePair("kernel", Utils.getKernelOtaID()));

                HttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(Config.KERNEL_PULL_URL + "?" + URLEncodedUtils.format(params, "UTF-8"));
                HttpResponse r = client.execute(get);
                int status = r.getStatusLine().getStatusCode();
                HttpEntity e = r.getEntity();
                if (status == 200) {
                    String data = EntityUtils.toString(e);
                    JSONObject json = new JSONObject(data);

                    if (json.has("error")) {
                        Log.e(Config.LOG_TAG + "Fetch", json.getString("error"));
                        error = json.getString("error");
                        return null;
                    }

                    return new KernelInfo(
                            json.getString("kernel"),
                            json.getString("version"),
                            json.getString("changelog"),
                            json.getString("url"),
                            json.getString("md5"),
                            Utils.parseDate(json.getString("date")));
                } else {
                    if (e != null) e.consumeContent();
                    error = "Server responded with error " + status;
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                error = e.getMessage();
            }

            return null;
        }

        @Override
        public void onPostExecute(KernelInfo result) {
            if (callback != null) {
                if (result != null) callback.onLoaded(result);
                else callback.onError(error);
            }
        }
    }

    public static interface KernelInfoListener {
        void onStartLoading();
        void onLoaded(KernelInfo info);
        void onError(String err);
    }
}
