package com.ialert.utilities;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ialert.R;

public class GasStationAdapter extends BaseAdapter {

	private Context mContext;
	private List<GasStation> mGasStationList;

	public GasStationAdapter(Context context, List<GasStation> gasStationList) {
		mContext = context;
		mGasStationList = gasStationList;
	}

	@Override
	public int getCount() {
		return mGasStationList.size();
	}

	@Override
	public Object getItem(int position) {
		return mGasStationList.get(position);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater mInflater = (LayoutInflater) mContext
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(R.layout.gas_station, null);
		}
		TextView gasStationName = (TextView) convertView
				.findViewById(R.id.gasStationName);
		TextView gasStationAddress = (TextView) convertView
				.findViewById(R.id.gasStationAddress);
		TextView gasStationDistance = (TextView) convertView
				.findViewById(R.id.gasStationDistance);
		TextView gasStationPrice = (TextView) convertView
				.findViewById(R.id.gasStationPrice);

		GasStation gasStation = mGasStationList.get(position);
		gasStationName.setText(gasStation.getStation());
		gasStationAddress.setText(gasStation.getAddress());
		gasStationDistance.setText(gasStation.getDistance());
		gasStationPrice.setText("Price: $" + gasStation.getPrice());
		convertView.setTag(position);
		return convertView;
	}

}
