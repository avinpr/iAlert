package com.ialert.utilities;

import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.ford.syncV4.proxy.rpc.GPSData;

public class LocationHelper {

	public static GPSData getGPSData(Context context) {
		LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = lm.getProviders(true);

		/*
		 * Loop over the array backwards, and if you get an accurate location,
		 * then break out the loop
		 */
		Location l = null;

		for (int i = providers.size() - 1; i >= 0; i--) {
			l = lm.getLastKnownLocation(providers.get(i));
			if (l != null)
				break;
		}

		GPSData gpsData = new GPSData();
		if (l != null) {
			gpsData.setLatitudeDegrees(l.getLatitude());
			gpsData.setLongitudeDegrees(l.getLongitude());
		}
		return gpsData;
	}
}
