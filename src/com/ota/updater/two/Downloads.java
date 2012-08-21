package com.ota.updater.two;

import com.ota.updater.two.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Downloads extends PreferenceActivity {
	
	@Override
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.downloads);
	}

}
