package com.ialert.utilities;

import java.text.DecimalFormat;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ialert.R;

public class ServiceStationAdapter extends BaseAdapter {

	private Context mContext;
	private List<ServiceStation> mServiceStationList;

	public ServiceStationAdapter(Context context,
			List<ServiceStation> serviceStationList) {
		mContext = context;
		mServiceStationList = serviceStationList;
	}

	@Override
	public int getCount() {
		return mServiceStationList.size();
	}

	@Override
	public Object getItem(int position) {
		return mServiceStationList.get(position);
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
			convertView = mInflater.inflate(R.layout.service_station, null);
		}
		TextView stationName = (TextView) convertView
				.findViewById(R.id.serviceStationName);
		TextView stationAddress = (TextView) convertView
				.findViewById(R.id.serviceStationAddress);
		TextView stationDistance = (TextView) convertView
				.findViewById(R.id.serviceStationDistance);
		TextView stationPhone = (TextView) convertView
				.findViewById(R.id.serviceStationPhone);

		ServiceStation serviceStation = mServiceStationList.get(position);
		stationName.setText(serviceStation.getName());
		stationAddress.setText(serviceStation.getAddress());
		String distanceInKm = serviceStation.getDistance();
		Double distanceInMiles = Float.valueOf(distanceInKm) * 0.621371;
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		stationDistance.setText(df.format(distanceInMiles) + " miles");
		stationPhone.setText(serviceStation.getPhone());
		convertView.setTag(position);
		return convertView;
	}

}
