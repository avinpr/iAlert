package com.ialert.utilities;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater mInflater = (LayoutInflater) mContext
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(R.layout.gas_station, null);
		}
		final TextView gasStationName = (TextView) convertView
				.findViewById(R.id.gasStationName);
		final TextView gasStationAddress = (TextView) convertView
				.findViewById(R.id.gasStationAddress);
		final TextView gasStationDistance = (TextView) convertView
				.findViewById(R.id.gasStationDistance);
		final TextView gasStationPrice = (TextView) convertView
				.findViewById(R.id.gasStationPrice);
		final ImageView gasStationIcon = (ImageView) convertView
				.findViewById(R.id.gasStationIcon);

		final GasStation gasStation = mGasStationList.get(position);
		gasStationName.setText(gasStation.getStation());
		gasStationAddress.setText(gasStation.getAddress());
		gasStationDistance.setText(gasStation.getDistance());
		gasStationPrice.setText("Price: $" + gasStation.getPrice());
		int intGasStationIcon = GetGasStationIcon(gasStation.getStation());

		// The Nexus S is crashing (while the Nexus 5 is not) when trying to
		// change icons.I'm about to do the dumbest thing in all of
		// programming!!!
		String model = android.os.Build.MODEL;
		if (model.equalsIgnoreCase("Nexus 5")) {
			if (intGasStationIcon != 0) {
				gasStationIcon.setImageResource(intGasStationIcon);
				notifyDataSetChanged();
			} else {
				gasStationIcon.setImageResource(R.drawable.fuel);
			}
		}
		return convertView;
	}

	private int GetGasStationIcon(String gasStationName) {
		int gasStationIcon = 0;
		if (gasStationName.equalsIgnoreCase("Xtramart"))
			gasStationIcon = R.drawable.xtramart;
		else if (gasStationName.equalsIgnoreCase("Sunoco"))
			gasStationIcon = R.drawable.sunoco;
		else if (gasStationName.equalsIgnoreCase("Shell"))
			gasStationIcon = R.drawable.shell;
		else if (gasStationName.equalsIgnoreCase("Citgo"))
			gasStationIcon = R.drawable.citgo;
		else if (gasStationName.equalsIgnoreCase("Marathon"))
			gasStationIcon = R.drawable.marathon;
		else if (gasStationName.equalsIgnoreCase("BP"))
			gasStationIcon = R.drawable.bp;
		return gasStationIcon;
	}

}
