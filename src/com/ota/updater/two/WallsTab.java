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
import java.util.Arrays;

import android.app.Fragment;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;

import com.ota.updater.two.utils.ImageAdapter;

public class WallsTab extends Fragment implements OnItemSelectedListener {
	private int screenHeight;
	private int screenWidth;
	private String deviceRes;

    public int getShownIndex() {
        return getArguments().getInt("index", 0);
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//TODO something that checks orientation??
		DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

		deviceRes = Integer.toString(screenWidth) + "x" + Integer.toString(screenHeight);
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.walls, container, false);

		ArrayList<String> resValues = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.res_array)));
		if (!resValues.contains(deviceRes)) resValues.add(deviceRes);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_expandable_list_item_1, resValues);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        int resIdx = adapter.getPosition(deviceRes);

		Spinner spinner = (Spinner) v.findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
		spinner.setSelection(resIdx);
		spinner.setOnItemSelectedListener(this);

	    GridView gridview = (GridView) v.findViewById(R.id.gridview);
	    gridview.setAdapter(new ImageAdapter(getActivity()));
        return v;
    }

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
	}
}
