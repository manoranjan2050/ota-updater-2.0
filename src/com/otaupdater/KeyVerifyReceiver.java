package com.otaupdater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.otaupdater.utils.Config;

public class KeyVerifyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(Config.LOG_TAG + "KeyVerify", "got pro key response");

        if (intent.hasExtra("errorCode")) {
            int error = intent.getIntExtra("errorCode", 0);
            int errorResId;
            switch (error) {
            case 1:
                errorResId = R.string.prokey_verify_error_nodata;
                break;
            case 2:
                errorResId = R.string.prokey_verify_error_nomarket;
                break;
            default:
                errorResId = R.string.prokey_verify_error_other;
            }
            Log.w(Config.LOG_TAG + "KeyVerify", "key verification returned error " + error);
            Toast.makeText(context, errorResId, Toast.LENGTH_LONG).show();
            return;
        }

        if (!intent.hasExtra("licensed") || !intent.hasExtra("definitive")) {
            Log.e(Config.LOG_TAG + "KeyVerify", "invalid key verification response!");
            return;
        }

        boolean licensed = intent.getBooleanExtra("licensed", false);
        boolean definitive = intent.getBooleanExtra("definitive", true);

        final Config cfg = Config.getInstance(context.getApplicationContext());

        if (licensed) {
            if (definitive) {
                cfg.setKeyExpiry(-1);
                Toast.makeText(context, R.string.prokey_verified, Toast.LENGTH_LONG).show();
            } else {
                cfg.setKeyExpiry(System.currentTimeMillis() + intent.getLongExtra("retry_after", 0));
                Toast.makeText(context, R.string.prokey_noverify, Toast.LENGTH_LONG).show();
            }
        } else {
            cfg.setKeyExpiry(0);
            Toast.makeText(context, definitive ? R.string.prokey_invalid : R.string.prokey_noverify, Toast.LENGTH_LONG).show();
        }
    }
}
