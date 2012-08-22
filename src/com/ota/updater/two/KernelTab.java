package com.ota.updater.two;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.ota.updater.two.utils.Utils;

@SuppressWarnings("deprecation")
public class KernelTab extends PreferenceFragment {
    public final static String URL = "http://dl.dropbox.com/u/44265003/update.json";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.kernel);

        final String kernelVersion = System.getProperty("os.version");

        Preference kernelVer = findPreference("kernel_version");
        kernelVer.setSummary(kernelVersion);

        Preference kernelCustom = findPreference("kernel_custom");

        if (kernelVersion.toLowerCase().contains("ninphetamin3")) {
            kernelCustom.setSummary("VillainROM supported kernel!\nClick here to check for updates.");
            kernelCustom.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new Read().execute();
                    return false;
                }
            });
        } else {
            kernelCustom.setSummary("Not a VillainROM supported kernel." + "\n" + "What does this mean?");
            kernelCustom.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog alertDialog = new AlertDialog.Builder(TabDisplay.mContext).create();
                    alertDialog.setTitle("Why is my kernel unsupported?");
                    alertDialog.setMessage("Unfortunately due to hosting and compatibility, not all kernels for your device can be supported in this application. While we would like to include as many as possible, it is not viable to include all custom kernels for a myriad of devices. Sorry!");
                    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //here you can add functions
                        }
                    });
                    alertDialog.show();
                    return false;
                }
            });
        }
    }

    public class Display {
        public String mKernel;
        public String mUrl;

        public Display (String rom, String downurl) {
            mKernel = rom;
            mUrl = downurl;
        }
    }

    public JSONObject getKernelV() throws ClientProtocolException, IOException, JSONException{
        HttpClient client = new DefaultHttpClient();
        StringBuilder url = new StringBuilder(URL);
        HttpGet get = new HttpGet(url.toString());
        HttpResponse r = client.execute(get);
        int status = r.getStatusLine().getStatusCode();
        if (status == 200) {
            HttpEntity e = r.getEntity();
            String data = EntityUtils.toString(e);
            JSONObject stream = new JSONObject(data);
            JSONObject tweaks = stream.getJSONObject("avail-tweaks");
            return tweaks;
        } else {
            return null;
        }
    }

    public class Read extends AsyncTask<String, Integer, Display> {
        @Override
        protected Display doInBackground(String... params) {
            final String device = params[0];
            try {
                JSONObject json = getKernelV().getJSONObject("device").getJSONArray(device).getJSONObject(0);
                String downurl = json.getString("url");
                String build = json.getString("rom");

                return new Display(downurl, build);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                Utils.toastWrapper(getActivity(), "A server issue occured, please try again.", Toast.LENGTH_LONG);
            } catch (IOException e) {
                e.printStackTrace();
                Utils.toastWrapper(getActivity(), "Error whilst reading content.", Toast.LENGTH_LONG);
            } catch (JSONException e) {
                e.printStackTrace();
                Utils.toastWrapper(getActivity(), "No content for your device.", Toast.LENGTH_LONG);
            }
            return null;
        }
    }
}
