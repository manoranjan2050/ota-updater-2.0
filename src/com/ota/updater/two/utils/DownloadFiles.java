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

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;

public class DownloadFiles extends AsyncTask<String, Integer, String> {
    private String path;

    public DownloadFiles(String path) {
        this.path = path;
    }

    @Override
    protected String doInBackground(String... sUrl) {
        InputStream in = null;
        OutputStream out = null;
        try {
            URL url = new URL(sUrl[0]);
            URLConnection connection = url.openConnection();
            connection.connect();
            // this will be useful so that you can show a typical 0-100% progress bar
            int fileLength = connection.getContentLength();

            // download the file
            in = new BufferedInputStream(url.openStream());
            out = new FileOutputStream(path);

            byte[] buf = new byte[1024];
            long total = 0;
            int nRead = -1;
            while ((nRead = in.read(buf)) != -1) {
                total += nRead;
                // publishing the progress....
                publishProgress((int) (total * 100 / fileLength));
                out.write(buf, 0, nRead);
            }
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
}
