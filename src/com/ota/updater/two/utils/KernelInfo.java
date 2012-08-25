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

import java.io.File;
import java.util.Date;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.ota.updater.two.R;

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

    public long fetchFile(Context ctx) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(ctx.getString(R.string.notif_download));
        request.setDescription(kernelName);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationUri(Uri.fromFile(new File(Config.KERNEL_DL_PATH_FILE, kernelName + "__" + version + ".zip")));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI); //TODO check config for allow 3g download setting
        request.setVisibleInDownloadsUi(true);

        DownloadManager manager = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
        return manager.enqueue(request);
    }
}
