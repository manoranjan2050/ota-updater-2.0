package com.ota.updater.two.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.widget.Toast;

import com.ota.updater.two.utils.ShellCommand.CommandResult;

public class Utils {
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
        }
        return "";
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
}
