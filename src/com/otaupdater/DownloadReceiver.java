package com.otaupdater;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DownloadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;
        if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            //do nothing?
        } else if (action.equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
            //TODO show flash dialog if necessary, otherwise go to Downloads
        }
    }
}
