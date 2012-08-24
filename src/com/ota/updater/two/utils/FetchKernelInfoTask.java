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

package com.ota.updater.two.utils;

import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.ota.updater.two.R;

public class FetchKernelInfoTask extends AsyncTask<Void, Void, KernelInfo> {
    private KernelInfoListener callback = null;
    private Context context = null;
    private String error = null;

    public FetchKernelInfoTask(Context ctx) {
        this(ctx, null);
    }

    public FetchKernelInfoTask(Context ctx, KernelInfoListener callback) {
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
            error = context.getString(R.string.alert_unsupported_kernel_title);
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
            HttpGet get = new HttpGet(Config.PULL_URL + "?" + URLEncodedUtils.format(params, "UTF-8"));
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

    public static interface KernelInfoListener {
        void onStartLoading();
        void onLoaded(KernelInfo info);
        void onError(String err);
    }
}
