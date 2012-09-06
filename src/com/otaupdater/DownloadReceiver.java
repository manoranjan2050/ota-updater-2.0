package com.otaupdater;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.otaupdater.utils.KernelInfo;
import com.otaupdater.utils.RomInfo;

public class DownloadReceiver extends BroadcastReceiver {
    public static final String DL_ROM_ACTION = "com.otaupdater.action.DL_ROM_ACTION";
    public static final String DL_KERNEL_ACTION = "com.otaupdater.action.DL_KERNEL_ACTION";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;
        if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            //do nothing?
        } else if (action.equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
            //TODO show flash dialog if necessary, otherwise go to Downloads
        } else if (action.equals(DL_ROM_ACTION)) {
            RomInfo.fromIntent(intent).fetchFile(context);
        } else if (action.equals(DL_KERNEL_ACTION)) {
            KernelInfo.fromIntent(intent).fetchFile(context);
        }
    }
}
