package com.ialert.activity;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.ialert.R;
import com.ialert.utilities.GasStation;
import com.ialert.utilities.GasStationAdapter;
import com.ialert.utilities.GasStationHelper;

public class GasStationActivity extends Activity {

	@Override
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gas_stations);
		Bundle bundle = getIntent().getExtras();
		List<GasStation> gasStationList = bundle
				.getParcelableArrayList("gasStationList");
		ListView gasListView = (ListView) findViewById(R.id.gasStationList);
		gasListView.setAdapter(new GasStationAdapter(this, gasStationList));
		gasListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				GasStation gasStation = (GasStation) parent.getAdapter()
						.getItem(position);
				String gasStationAddress = getString(R.string.maps_url_template)
						.replaceAll("LATITUDE1",
								String.valueOf(GasStationHelper.mLatitude))
						.replaceAll("LONGITUDE1",
								String.valueOf(GasStationHelper.mLongitude))
						.replaceAll("LATITUDE2", gasStation.getLat())
						.replaceAll("LONGITUDE2", gasStation.getLon());
				Intent navIntent = new Intent(Intent.ACTION_VIEW, Uri
						.parse(gasStationAddress));
				startActivity(navIntent);
			}
		});
	}
}
