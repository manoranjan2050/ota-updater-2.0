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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;
import android.os.StatFs;

import com.otaupdater.utils.DownloadFile.DownloadResult;

public class DownloadFile extends AsyncTask<Void, Integer, DownloadResult> {
    private DownloadListener callback = null;

    private int totalSize = 0;

    private String srcUrl;
    private File dest;

    public DownloadFile(String url, String dest) {
        this(url, dest, null);
    }

    public DownloadFile(String url, String dest, DownloadListener callback) {
        this(url, new File(dest), callback);
    }

    public DownloadFile(String url, File dest) {
        this(url, dest, null);
    }

    public DownloadFile(String url, File dest, DownloadListener callback) {
        this.srcUrl = url;
        this.dest = dest;
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        if (callback != null) callback.onStart();
    }

    @Override
    protected DownloadResult doInBackground(Void... params) {
        InputStream in = null;
        OutputStream out = null;
        try {
            File dir = dest.getParentFile();
            if (dir == null) return DownloadResult.MOUNT_NOT_AVAILABLE;
            dir.mkdirs();
            if (!dir.exists()) return DownloadResult.MOUNT_NOT_AVAILABLE;

            URLConnection conn = new URL(srcUrl).openConnection();

            final int fileLength = conn.getContentLength();
            publishProgress(0, fileLength);

            StatFs stat = new StatFs(dest.getAbsolutePath());
            long availSpace = ((long) stat.getAvailableBlocks()) * ((long) stat.getBlockSize());
            if (fileLength >= availSpace) {
                dest.delete();
                return DownloadResult.NOT_ENOUGH_SPACE;
            }

            in = new BufferedInputStream(conn.getInputStream());
            out = new FileOutputStream(dest);

            byte[] buf = new byte[4096];
            int totalRead = 0;
            int nRead = -1;
            while ((nRead = in.read(buf)) != -1) {
                if (this.isCancelled()) return DownloadResult.CANCELLED;
                out.write(buf, 0, nRead);
                totalRead += nRead;
                publishProgress(totalRead);
            }

            return DownloadResult.FINISHED;
        } catch (Exception e) {
        } finally {
            if (in != null) {
                try { in.close(); }
                catch (IOException e) { }
            }
            if (out != null) {
                try { out.flush(); out.close(); }
                catch (IOException e) { }
            }
        }
        return null;
    }

    @Override
    protected void onCancelled(DownloadResult result) {
        dest.delete();
        if (callback != null) callback.onFinish(DownloadResult.CANCELLED, null);
    }

    @Override
    protected void onPostExecute(DownloadResult result) {
        if (result != DownloadResult.FINISHED) dest.delete();
        if (callback != null) callback.onFinish(result, result == DownloadResult.FINISHED ? dest : null);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (values.length == 0) return;
        if (values.length == 1) {
            if (callback != null) callback.onProgress(values[0], ((double) values[0]) / ((double) totalSize));
        } else {
            totalSize = values[1];
            if (callback != null) {
                callback.onLengthReceived(totalSize);
                callback.onProgress(values[0], ((double) values[0]) / ((double) totalSize));
            }
        }
    }

    public static enum DownloadResult {
        FINISHED, CANCELLED, MOUNT_NOT_AVAILABLE, NOT_ENOUGH_SPACE
    }

    public static interface DownloadListener {
        void onStart();
        void onLengthReceived(int length);
        void onProgress(int bytes, double pct);
        void onFinish(DownloadResult result, File file);
    }
}
