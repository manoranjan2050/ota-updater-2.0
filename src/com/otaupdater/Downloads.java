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

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.DownloadManager;
import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.otaupdater.utils.Config;

public class Downloads extends ListActivity implements ActionBar.OnNavigationListener {

    private ArrayList<String> fileList = new ArrayList<String>();
    private DownloadAdapter dmAdapter = null;
    private ArrayAdapter<String> fileAdapter = null;
    private int state = 0;

    private static final int REFRESH_DELAY = 1000;
    private final Handler REFRESH_HANDLER = new RefreshHandler(this);
    private static class RefreshHandler extends Handler {
        private WeakReference<Downloads> downloadsAct;

        public RefreshHandler(Downloads dls) {
            downloadsAct = new WeakReference<Downloads>(dls);
        }

        @Override
        public void handleMessage(Message msg) {
            downloadsAct.get().updateFileList();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String extState = Environment.getExternalStorageState();
        if (!extState.equals(Environment.MEDIA_MOUNTED) && !extState.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            Toast.makeText(this, extState.equals(Environment.MEDIA_SHARED) ? R.string.toast_nosd_shared : R.string.toast_nosd_error, Toast.LENGTH_LONG).show();
            finish();
        }

        setContentView(R.layout.downloads);

        final ActionBar bar = getActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowTitleEnabled(false);
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        bar.setListNavigationCallbacks(ArrayAdapter.createFromResource(this, R.array.download_types, android.R.layout.simple_spinner_dropdown_item), this);

        if (savedInstanceState != null) {
            state = savedInstanceState.getInt("state", state);
        }
        bar.setSelectedNavigationItem(state);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("state", getActionBar().getSelectedNavigationIndex());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFileList();
    }

    @Override
    protected void onPause() {
        REFRESH_HANDLER.removeCallbacksAndMessages(null);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (dmAdapter != null) {
            dmAdapter.getCursor().close();
            dmAdapter = null;
        }
        if (fileAdapter != null) {
            fileList.clear();
            fileAdapter = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        state = itemPosition;
        ((TextView) getListView().getEmptyView()).setText(
                getResources().getStringArray(R.array.download_types_empty)[itemPosition]);
        updateFileList();
        return true;
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

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String path;
        if (state < 2) {
            Cursor c = dmAdapter.getCursor();
            c.moveToPosition(position);
            path = c.getString(c.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_FILENAME));
        } else {
            path = fileList.get(position);
            path = (state == 2 ? Config.ROM_DL_PATH : Config.KERNEL_DL_PATH) + path;
        }
        Log.v(Config.LOG_TAG + "DL", "clicked on " + path);
        //TODO show install dialog if necessary
    }

    private void updateFileList() {
        if (state < 2) {
            DownloadManager.Query query = new DownloadManager.Query();
            if (state == 0) {
                query.setFilterByStatus(DownloadManager.STATUS_PAUSED | DownloadManager.STATUS_PENDING | DownloadManager.STATUS_RUNNING);
            } else {
                query.setFilterByStatus(DownloadManager.STATUS_FAILED | DownloadManager.STATUS_SUCCESSFUL);
            }

            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            Cursor c = dm.query(query);

            if (dmAdapter == null) {
                dmAdapter = new DownloadAdapter(this, c, 0);
                getListView().setAdapter(dmAdapter);

                if (fileAdapter != null) {
                    fileList.clear();
                    fileAdapter = null;
                }
            } else {
                dmAdapter.changeCursor(c);
            }

            REFRESH_HANDLER.sendMessageDelayed(REFRESH_HANDLER.obtainMessage(), REFRESH_DELAY);
        } else {
            File dir = state == 2 ? Config.ROM_DL_PATH_FILE : Config.KERNEL_DL_PATH_FILE;
            File[] files = dir.listFiles();
            fileList.clear();
            for (File file : files) {
                if (file.isDirectory()) continue;
                fileList.add(file.getName());
            }

            if (fileAdapter == null) {
                fileAdapter = new ArrayAdapter<String>(this, R.layout.download_file, R.id.filename, fileList);
                getListView().setAdapter(fileAdapter);

                if (dmAdapter != null) {
                    dmAdapter.getCursor().close();
                    dmAdapter = null;
                }
            } else {
                fileAdapter.notifyDataSetChanged();
            }
        }
    }

    protected static class DownloadAdapter extends CursorAdapter {
        private static final int SCALE_KBYTES = 1024;
        private static final int KBYTE_THRESH = 920; //0.9kb

        private static final int SCALE_MBYTES = 1048576;
        private static final int MBYTE_THRESH = 943718; //0.9mb

        private static final int SCALE_GBYTES = 1073741824;
        private static final int GBYTE_THRESH = 966367641; //0.9gb

        public DownloadAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public void bindView(View view, Context ctx, Cursor c) {
            ImageView icon = (ImageView) view.findViewById(R.id.download_icon);
            ProgressBar bar = (ProgressBar) view.findViewById(R.id.download_progress_bar);
            TextView titleView = (TextView) view.findViewById(R.id.download_title);
            TextView subtxtView = (TextView) view.findViewById(R.id.download_subtext);
            TextView bytesView = (TextView) view.findViewById(R.id.download_bytes_text);
            TextView pctView = (TextView) view.findViewById(R.id.download_pct_text);

            int status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
            int reason = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON));
            int downBytes = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            int totalBytes = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            String name = c.getString(c.getColumnIndexOrThrow(DownloadManager.COLUMN_DESCRIPTION));
            String fileName = c.getString(c.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_FILENAME));

            titleView.setText(name);
            if (fileName.contains("/ROM/")) {
                icon.setImageResource(R.drawable.zip);
            } else if (fileName.contains("/kernel/")) {
                icon.setImageResource(R.drawable.zip);
            } else {
                icon.setImageResource(R.drawable.zip);
            }

            switch (status) {
            case DownloadManager.STATUS_RUNNING:
                bar.setIndeterminate(false);
                bar.setVisibility(View.VISIBLE);
                bytesView.setVisibility(View.VISIBLE);
                pctView.setVisibility(View.VISIBLE);
                subtxtView.setVisibility(View.GONE);
                updateProgressViews(ctx, bytesView, pctView, bar, downBytes, totalBytes);
                break;
            case DownloadManager.STATUS_PAUSED:
                bar.setIndeterminate(false);
                bar.setVisibility((System.currentTimeMillis() / 1000) % 2 == 0 ? View.VISIBLE : View.INVISIBLE);
                bytesView.setVisibility(View.VISIBLE);
                pctView.setVisibility(View.VISIBLE);
                updateProgressViews(ctx, bytesView, pctView, bar, downBytes, totalBytes);

                int pauseReason = R.string.downloads_paused_unknown;
                switch (reason) {
                case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                    pauseReason = R.string.downloads_paused_wifi;
                    break;
                case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                    pauseReason = R.string.downloads_paused_network;
                    break;
                case DownloadManager.PAUSED_WAITING_TO_RETRY:
                    pauseReason = R.string.downloads_paused_retry;
                    break;
                }
                subtxtView.setText(pauseReason);
                subtxtView.setVisibility(View.VISIBLE);

                break;
            case DownloadManager.STATUS_SUCCESSFUL:
                bar.setVisibility(View.GONE);
                bytesView.setVisibility(View.GONE);
                pctView.setVisibility(View.GONE);
                subtxtView.setVisibility(View.GONE);
                break;
            case DownloadManager.STATUS_FAILED:
                bar.setVisibility(View.GONE);
                bytesView.setVisibility(View.GONE);
                pctView.setVisibility(View.GONE);

                int failReason = R.string.downloads_failed_unknown;
                switch (reason) {
                case DownloadManager.ERROR_CANNOT_RESUME:
                    failReason = R.string.downloads_failed_resume;
                    break;
                case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                    failReason = R.string.downloads_failed_mount;
                    break;
                case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                    failReason = R.string.downloads_failed_fileexists;
                    break;
                case DownloadManager.ERROR_HTTP_DATA_ERROR:
                case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                    failReason = R.string.downloads_failed_http;
                    break;
                case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                    failReason = R.string.downloads_failed_space;
                    break;
                }
                subtxtView.setText(failReason);
                subtxtView.setVisibility(View.VISIBLE);

                break;
            case DownloadManager.STATUS_PENDING:
                bar.setIndeterminate(true);
                bar.setVisibility(View.VISIBLE);
                bytesView.setVisibility(View.GONE);
                pctView.setVisibility(View.GONE);
                subtxtView.setVisibility(View.GONE);
                break;
            }
        }

        private void updateProgressViews(Context ctx, TextView bytesView, TextView pctView, ProgressBar bar, int done, int total) {
            int scaledDone = done;
            int scaledTotal = total;
            int bytesTxtRes = R.string.downloads_size_progress_b;
            if (total >= GBYTE_THRESH) {
                scaledDone /= SCALE_GBYTES;
                scaledTotal /= SCALE_GBYTES;
                bytesTxtRes = R.string.downloads_size_progress_gb;
            } else if (total >= MBYTE_THRESH) {
                scaledDone /= SCALE_MBYTES;
                scaledTotal /= SCALE_MBYTES;
                bytesTxtRes = R.string.downloads_size_progress_mb;
            } else if (total >= KBYTE_THRESH) {
                scaledDone /= SCALE_KBYTES;
                scaledTotal /= SCALE_KBYTES;
                bytesTxtRes = R.string.downloads_size_progress_kb;
            }

            bytesView.setText(ctx.getString(bytesTxtRes, scaledDone, scaledTotal));
            pctView.setText(ctx.getString(R.string.downloads_pct_progress, Math.round(100 * ((float) done) / total)));
            bar.setMax(total);
            bar.setProgress(done);
        }

        @Override
        public View newView(Context ctx, Cursor c, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(ctx);
            View view = inflater.inflate(R.layout.download_item, parent, false);
            bindView(view, ctx, c);
            return view;
        }
    }
}
