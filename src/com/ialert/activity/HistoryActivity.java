package com.ialert.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.view.*;

import com.ialert.R;

public class HistoryActivity extends Activity {

	protected ListView mHistoryListView;
	protected String[] values = new String[] {
			"10/01/2014: Low Tire Pressure - Right Front",
			"09/25/2014: Low Fuel", "07/12/2014: Airbag malfunction",
			"06/19/2014: Low Tire Pressure - Right Rear",
			"10/01/2014: Low Tire Pressure - Right Front",
			"09/25/2014: Low Fuel", "07/12/2014: Airbag malfunction",
			"06/19/2014: Low Tire Pressure - Right Rear",
			"10/01/2014: Low Tire Pressure - Right Front",
			"09/25/2014: Low Fuel", "07/12/2014: Airbag malfunction",
			"06/19/2014: Low Tire Pressure - Right Rear" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);

		mHistoryListView = (ListView) findViewById(R.id.history_list);
		populateList();
	}

	private void populateList() {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_2, android.R.id.text1, values) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);

				TextView textView = (TextView) view
						.findViewById(android.R.id.text1);

				textView.setTextColor(Color.WHITE);
				textView.setTextSize(13);

				return view;
			}
		};
		mHistoryListView.setAdapter(adapter);
	}
}
