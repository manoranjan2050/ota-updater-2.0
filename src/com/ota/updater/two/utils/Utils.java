package com.ota.updater.two.utils;

import android.os.AsyncTask;
import android.os.Build;

import com.ota.updater.two.utils.ShellCommand.CommandResult;

public class Utils {
    public static final String LOGTAG = "VillainToolkit";

    public class Read extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            ShellCommand cmd = new ShellCommand();
            CommandResult modversion = cmd.sh.runWaitFor("getprop ro.modversion");
            if (modversion.stdout.length() != 0) return modversion.stdout;

            CommandResult cmversion = cmd.sh.runWaitFor("getprop ro.cm.version");
            if (cmversion.stdout.length() != 0) return cmversion.stdout;

            CommandResult aokpversion = cmd.sh.runWaitFor("getprop ro.aokp.version");
            if (aokpversion.stdout.length() != 0) return aokpversion.stdout;

            return Build.DISPLAY;
        }
    }
}
