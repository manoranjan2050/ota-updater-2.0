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

package com.ota.updater.two;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gcm.GCMRegistrar;
import com.ota.updater.two.utils.Config;
import com.ota.updater.two.utils.Utils;

public class TabDisplay extends FragmentActivity {
    public static final String ROM_NOTIF_ACTION = "com.updater.ota.two.action.ROM_NOTIF_ACTION";
    public static final String KERNEL_NOTIF_ACTION = "com.updater.ota.two.action.KERNEL_NOTIF_ACTION";

    private ViewPager mViewPager;
    private TabsAdapter mTabsAdapter;
    private Config cfg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context context = getApplicationContext();
        cfg = Config.getInstance(context);

        if (!Utils.isRomOtaEnabled() && !Utils.isKernelOtaEnabled()) {
            if (!cfg.getIgnoredUnsupportedWarn()) {
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle(R.string.alert_unsupported_title);
                alert.setMessage(R.string.alert_unsupported_message);
                alert.setCancelable(false);
                alert.setNegativeButton(R.string.alert_exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
                alert.setPositiveButton(R.string.alert_ignore, new DialogInterface.OnClickListener() {
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    				    cfg.setIgnoredUnsupportedWarn(true);
    					dialog.dismiss();
    				}
    			});
                alert.create().show();
            }

            if (Utils.marketAvailable(this)) {
                GCMRegistrar.checkDevice(context);
                GCMRegistrar.checkManifest(context);
                final String regId = GCMRegistrar.getRegistrationId(context);
                if (regId.length() != 0) {
                    GCMRegistrar.unregister(context);
                }
            }

        } else {
            if (Utils.marketAvailable(this)) {
                GCMRegistrar.checkDevice(context);
                GCMRegistrar.checkManifest(context);
                final String regId = GCMRegistrar.getRegistrationId(context);
                if (regId.length() != 0) {
                    if (cfg.upToDate()) {
                        Log.v(Config.LOG_TAG + "GCMRegister", "Already registered");
                    } else {
                        Log.v(Config.LOG_TAG + "GCMRegister", "Already registered, out-of-date, reregistering");
                        cfg.setValuesToCurrent();
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                Utils.updateGCMRegistration(context, regId);
                                return null;
                            }
                        }.execute();
                    }
                } else {
                    GCMRegistrar.register(context, Config.GCM_SENDER_ID);
                    Log.v(Config.LOG_TAG + "GCMRegister", "GCM registered");
                }
            } else {
                UpdateCheckReceiver.setAlarm(context);
            }
        }

        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.pager);
        setContentView(mViewPager);

        final ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_TITLE);
        bar.setTitle(R.string.app_name);

        mTabsAdapter = new TabsAdapter(this, mViewPager);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.main_about), AboutTab.class, null);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.main_rom), ROMTab.class, null);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.main_kernel), KernelTab.class, null);

        if (savedInstanceState != null) {
            bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
    }

    public static class TabsAdapter extends FragmentPagerAdapter
            implements ActionBar.TabListener, ViewPager.OnPageChangeListener {

        private Context ctx;
        private final ActionBar mActionBar;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        static final class TabInfo {
            private final Class<?> clss;
            private final Bundle args;

            TabInfo(Class<?> _class, Bundle _args) {
                clss = _class;
                args = _args;
            }
        }

        public TabsAdapter(Activity activity, ViewPager pager) {
            super(activity.getFragmentManager());
            ctx = activity;
            mActionBar = activity.getActionBar();
            mViewPager = pager;
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
            TabInfo info = new TabInfo(clss, args);
            tab.setTag(info);
            tab.setTabListener(this);
            mTabs.add(info);
            mActionBar.addTab(tab);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            return Fragment.instantiate(ctx, info.clss.getName(), info.args);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            mActionBar.setSelectedNavigationItem(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            Object tag = tab.getTag();
            for (int i=0; i<mTabs.size(); i++) {
                if (mTabs.get(i) == tag) {
                    mViewPager.setCurrentItem(i);
                }
            }
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
        case R.id.settings:
            i = new Intent(this, Settings.class);
            startActivity(i);
            return true;
        case R.id.downloads:
            i = new Intent(this, Downloads.class);
            startActivity(i);
            return true;
        case R.id.accounts:
            i = new Intent(this, AccountsScreen.class);
            startActivity(i);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
