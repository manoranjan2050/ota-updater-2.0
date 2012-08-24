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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Scanner;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.view.View;
import android.widget.Toast;

import com.ota.updater.two.R;
import com.ota.updater.two.TabDisplay;
import com.ota.updater.two.utils.ShellCommand.CommandResult;

public class Utils {
    private static String cachedRomID = null;
    private static Date cachedOtaDate = null;
    private static String cachedOtaVer = null;
    private static String cachedOSSdPath = null;
    private static String cachedRcvrySdPath = null;

    public static String md5(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());

            return byteArrToStr(digest.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String md5(File f) {
        InputStream in = null;
        try {
            in = new FileInputStream(f);

            MessageDigest digest = MessageDigest.getInstance("MD5");

            byte[] buf = new byte[4096];
            int nRead = -1;
            while ((nRead = in.read(buf)) != -1) {
                digest.update(buf, 0, nRead);
            }

            return byteArrToStr(digest.digest());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try { in.close(); }
                catch (IOException e) { }
            }
        }
        return "";
    }

    public static String getVersion() {
        ShellCommand cmd = new ShellCommand();
        CommandResult modversion = cmd.sh.runWaitFor("getprop ro.modversion");
        if (modversion.stdout.length() != 0) return modversion.stdout;

        CommandResult cmversion = cmd.sh.runWaitFor("getprop ro.cm.version");
        if (cmversion.stdout.length() != 0) return cmversion.stdout;

        CommandResult aokpversion = cmd.sh.runWaitFor("getprop ro.aokp.version");
        if (aokpversion.stdout.length() != 0) return aokpversion.stdout;

        return Build.DISPLAY;
    }

    public static void toastWrapper(final Activity activity, final CharSequence text, final int duration) {
        activity.runOnUiThread(new Runnable() {
            @Override public void run() {
                Toast.makeText(activity, text, duration).show();
            }
        });
    }

    public static void toastWrapper(final Activity activity, final int resId, final int duration) {
        activity.runOnUiThread(new Runnable() {
            @Override public void run() {
                Toast.makeText(activity, resId, duration).show();
            }
        });
    }

    public static void toastWrapper(final View view, final CharSequence text, final int duration) {
        view.post(new Runnable() {
            @Override public void run() {
                Toast.makeText(view.getContext(), text, duration).show();
            }
        });
    }

    public static void toastWrapper(final View view, final int resId, final int duration) {
        view.post(new Runnable() {
            @Override public void run() {
                Toast.makeText(view.getContext(), resId, duration).show();
            }
        });
    }

    public static boolean marketAvailable(Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        try {
            pm.getPackageInfo("com.android.vending", 0);
        } catch (NameNotFoundException e) {
            return false;
        }
        return true;
    }

    public static boolean isROMSupported() {
        String romID = getRomID();
        return romID != null && romID.length() != 0;
    }

    public static String getRomID() {
        if (cachedRomID == null) {
            cachedRomID = getprop(Config.OTA_ID_PROP);
        }
        return cachedRomID;
    }

    public static String getOSSdPath() {
        if (cachedOSSdPath == null) {
            cachedOSSdPath = getprop(Config.OTA_SD_PATH_OS_PROP);
            if (cachedOSSdPath == null) return "sdcard";
        }
        return cachedOSSdPath;
    }

    public static String getRcvrySdPath() {
        if (cachedRcvrySdPath == null) {
            cachedRcvrySdPath = getprop(Config.OTA_SD_PATH_RECOVERY_PROP);
            if (cachedRcvrySdPath == null) return "sdcard";
        }
        return cachedRcvrySdPath;
    }

    public static Date getOtaDate() {
        if (cachedOtaDate == null) {
            String otaDateStr = getprop(Config.OTA_DATE_PROP);
            if (otaDateStr == null) return null;
            cachedOtaDate = parseDate(otaDateStr);
        }
        return cachedOtaDate;
    }

    public static String getOtaVersion() {
        if (cachedOtaVer == null) {
            cachedOtaVer = getprop(Config.OTA_VER_PROP);
        }
        return cachedOtaVer;
    }

    private static String getprop(String name) {
        ProcessBuilder pb = new ProcessBuilder("/system/bin/getprop", name);
        pb.redirectErrorStream(true);

        Process p = null;
        InputStream is = null;
        try {
            p = pb.start();
            is = p.getInputStream();
            String prop = new Scanner(is).next();
            if (prop.length() == 0) return null;
            return prop;
        } catch (NoSuchElementException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try { is.close(); }
                catch (Exception e) { }
            }
        }
        return null;
    }

    public static boolean dataAvailable(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }

    public static Date parseDate(String date) {
        if (date == null) return null;
        try {
            return new SimpleDateFormat("yyyyMMdd-kkmm").parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String formatDate(Date date) {
        if (date == null) return null;
        return new SimpleDateFormat("yyyyMMdd-kkmm").format(date);
    }

    public static boolean isUpdate(RomInfo info) {
        if (info == null) return false;
        if (info.version != null) {
            if (getOtaVersion() == null || !info.version.equalsIgnoreCase(getOtaVersion())) return true;
        }
        if (info.date != null) {
            if (getOtaDate() == null || info.date.after(getOtaDate())) return true;
        }
        return false;
    }

    public static void showUpdateNotif(Context ctx, RomInfo info) {
        Intent i = new Intent(ctx, TabDisplay.class);
        i.setAction(TabDisplay.NOTIF_ACTION);
        info.addToIntent(i);

        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification.Builder builder = new Notification.Builder(ctx);
        builder.setContentIntent(contentIntent);
        builder.setContentTitle(ctx.getString(R.string.notif_source));
        builder.setContentText(ctx.getString(R.string.notif_text_rom));
        builder.setTicker(ctx.getString(R.string.notif_text_rom));
        builder.setWhen(System.currentTimeMillis());
        //builder.setSmallIcon(R.drawable.updates);
        nm.notify(1, builder.getNotification());
    }

    private static final char[] HEX_DIGITS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    public static String byteArrToStr(byte[] bytes) {
        StringBuffer str = new StringBuffer();
        for (int q = 0; q < bytes.length; q++) {
            str.append(HEX_DIGITS[(0xF0 & bytes[q]) >>> 4]);
            str.append(HEX_DIGITS[0xF & bytes[q]]);
        }
        return str.toString();
    }
}
