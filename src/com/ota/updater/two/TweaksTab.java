package com.ota.updater.two;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.ota.updater.two.utils.Utils;

//TODO make backend, make it work, fix, fix, fix
//XXX do not use for now...

public class TweaksTab extends ListActivity {
    public static String URL = "http://dl.dropbox.com/u/44265003/tweaks.json";
    public static String device = Build.MODEL.toUpperCase();

    private List<String> listItems = new ArrayList<String>();
    private List<String> urlItems = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private Dialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ArrayAdapter<String>(this,R.layout.row, R.id.filename,listItems);
        setListAdapter(adapter);

        dialog = new Dialog(this);
        dialog.setTitle(getString(R.string.loading_dialog_title));
        dialog.setContentView(R.layout.spinner_dialog);
        //Spinner spin = (Spinner) findViewById(R.id.spinner);
        dialog.show();

        listItems.clear();

        new Read().execute(device);
    }

    public class Display {
        public HashMap<String, String> mContent;
        public String name;
        public String url;

        public Display (HashMap<String,String> tweaks_list) {
            mContent = tweaks_list;
            for (Map.Entry<String, String> entry : mContent.entrySet()) {
                name = entry.getKey();
                url = entry.getValue();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listItems.add(name);
                        urlItems.add(url);
                    }
                });
            }
        }
    }

    public JSONObject getTweak() throws ClientProtocolException, IOException, JSONException{
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

    public void setListener() {
        ListView listView = getListView();
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int pos, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TweaksTab.this);
                builder.setTitle("Options");
                builder.setItems(new String[] { "Download", "Install", "Favorite", "Share" }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which) {
                        case 0:
                            String url = urlItems.get(pos);
                            String zipName = listItems.get(pos);
                            new FetchFile(getApplicationContext()).execute(zipName, url);
                            break;
                        case 1:
                            break;
                        case 2:
                            //TODO: Fix broken favorites method
                            String tweakName = listItems.get(pos);
                            AddFavorite addFav = new AddFavorite();
                            addFav.addPref(tweakName);
                            break;
                        case 3:
                            String name = listItems.get(pos);
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.putExtra(Intent.EXTRA_TEXT, "I just downloaded " + name + " from VillainToolkit! Get it here - www.villainrom.co.uk");
                            shareIntent.setType("text/plain");
                            startActivity(shareIntent);
                            break;
                        }
                    }
                });

                builder.show();
                return false;
            }
        });
    }

    public class Read extends AsyncTask<String, Integer, Display> {
        @Override
        protected Display doInBackground(String... params) {
            final String device = params[0];
            try {
                JSONObject json = getTweak();
                HashMap<String, String> tweaksList = new HashMap<String,String>();
                JSONArray availTweaks = json.getJSONObject("device").getJSONArray(device);
                for (int i = 0; i < availTweaks.length(); i++) {
                    JSONObject row = availTweaks.getJSONObject(i);
                    String name = row.getString("tweak");
                    String url = row.getString("url");
                    tweaksList.put(name, url);
                }
                return new Display(tweaksList);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                Utils.toastWrapper(TweaksTab.this, "A server issue occured, please try again.", Toast.LENGTH_LONG);
            } catch (IOException e) {
                e.printStackTrace();
                Utils.toastWrapper(TweaksTab.this, "Error whilst reading content.", Toast.LENGTH_LONG);
            } catch (JSONException e) {
                e.printStackTrace();
                Utils.toastWrapper(TweaksTab.this, "No content for your device.", Toast.LENGTH_LONG);
            }

            return null;
        }

        @Override
        public void onPostExecute(final Display result) {
            dialog.dismiss();
            adapter.notifyDataSetChanged();
            setListener();
        }
    }
}
