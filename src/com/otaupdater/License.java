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

package com.otaupdater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class License extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar bar = getActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        BufferedReader in = null;
        StringBuilder data = null;
        try {
            data = new StringBuilder();
            char[] buf = new char[4096];
            int numRead;
            in = new BufferedReader(new InputStreamReader(getAssets().open("license.txt")));
            while ((numRead = in.read(buf)) >= 0) {
                data.append(buf, 0, numRead);
            }
        } catch (IOException e) {
            showErrorAndFinish();
            return;
        } finally {
            if (in != null) {
                try { in.close(); }
                catch (IOException e) { }
            }
        }

        if (TextUtils.isEmpty(data)) {
            showErrorAndFinish();
            return;
        }

        WebView webView = new WebView(this);

        // Begin the loading.  This will be done in a separate thread in WebView.
        webView.loadDataWithBaseURL(null, data.toString(), "text/plain", "utf-8", null);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                setContentView(view);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        }
        return false;
    }

    private void showErrorAndFinish() {
        Toast.makeText(this, R.string.toast_license_file_error, Toast.LENGTH_LONG).show();
        finish();
    }
}
