package com.ota.updater.two;
import java.util.Arrays;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
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

	 int screenHeight;
	 int screenWidth;
	 private Context cx = TabDisplay.cx;
	 private String deviceWH;


    public int getShownIndex() {
        return getArguments().getInt("index", 0);
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Activity ac = getActivity();

		screenHeight = getScreenHeight(ac);
		screenWidth = getScreenWidth(ac);

		deviceWH = Integer.toString(screenWidth) + "x" + Integer.toString(screenHeight);
	}

    @SuppressWarnings("unchecked")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.walls, container, false);

		Spinner spinner = (Spinner) v.findViewById(R.id.spinner);
		String[] res_resources = getResources().getStringArray(R.array.res_array);
		String[] res_values = append(res_resources, deviceWH);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(cx, android.R.layout.simple_expandable_list_item_1, res_values);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		//Set device width & height as default in wallpaper search dropdown.
		ArrayAdapter<String> drop_adap = (ArrayAdapter<String>) spinner.getAdapter();
		int spinnerPosition = drop_adap.getPosition(deviceWH);
		spinner.setSelection(spinnerPosition);


		spinner.setOnItemSelectedListener(this);



	    GridView gridview = (GridView) v.findViewById(R.id.gridview);
	    gridview.setAdapter(new ImageAdapter(cx));
        return v;
    }

    static <T> T[] append(T[] arr, T element) {
        final int N = arr.length;
        arr = Arrays.copyOf(arr, N + 1);
        arr[N] = element;
        return arr;
    }

	public int getScreenHeight(Activity ac) {
		DisplayMetrics displaymetrics = new DisplayMetrics();
		ac.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int height = displaymetrics.heightPixels;

		return height;
	}

	public int getScreenWidth(Activity ac) {
		DisplayMetrics displaymetrics = new DisplayMetrics();
		ac.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int width = displaymetrics.widthPixels;

		return width;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}



}
