package com.ialert.utilities;

import android.content.ContentResolver;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

public class MyLocationListener implements LocationListener {

	private Context mContext;

	public MyLocationListener(Context context) {
		mContext = context;
	}

	public boolean IsGpsEnabled() {
		ContentResolver contentResolver = mContext.getContentResolver();
		return Settings.Secure.isLocationProviderEnabled(contentResolver,
				LocationManager.GPS_PROVIDER);
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

}
