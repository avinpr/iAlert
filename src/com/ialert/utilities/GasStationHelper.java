package com.ialert.utilities;

import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.ford.syncV4.proxy.rpc.GPSData;
import com.ialert.R;
import com.ialert.activity.GasStationActivity;

public class GasStationHelper {

	protected static Context mContext;
	public static double mLatitude;
	public static double mLongitude;
	private static ProgressDialog progress;

	public static void NavigateToClosestGasStation(Context context,
			GPSData gpsData) {
		GetGasStations(context, gpsData);
		/*
		 * mContext = context; mLatitude = gpsData.getLatitudeDegrees();
		 * mLongitude = gpsData.getLongitudeDegrees(); Thread t = new Thread(new
		 * Runnable() {
		 * 
		 * @Override public void run() { startNavigationIntent(); } });
		 * t.start();
		 */
	}

	private static void startNavigationIntent() {
		String navigationUrl = getClosestGasStationAddress();
		Intent navIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse(navigationUrl));
		mContext.startActivity(navIntent);
	}

	private static String getGasStationsJson() {
		HttpClient client = new DefaultHttpClient();
		String requestUrl = mContext.getString(R.string.mygasfeed_url_template)
				.replaceAll("LATITUDE", String.valueOf(mLatitude))
				.replaceAll("LONGITUDE", String.valueOf(mLongitude));
		HttpGet request = new HttpGet(requestUrl);
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String response_str = null;
		try {
			response_str = client.execute(request, responseHandler);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response_str;
	}

	private static String getClosestGasStationAddress() {
		String gasStationResponse = getGasStationsJson();
		String gasStationAddress = null;
		try {
			JSONObject gasStationResp = new JSONObject(gasStationResponse);
			JSONArray gasStations = gasStationResp.getJSONArray("stations");
			JSONObject closestGasStation = gasStations.getJSONObject(0);
			String gasStationLat = closestGasStation.getString("lat");
			String gasStationLon = closestGasStation.getString("lng");
			gasStationAddress = mContext.getString(R.string.maps_url_template)
					.replaceAll("LATITUDE1", String.valueOf(mLatitude))
					.replaceAll("LONGITUDE1", String.valueOf(mLongitude))
					.replaceAll("LATITUDE2", gasStationLat)
					.replaceAll("LONGITUDE2", gasStationLon);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return gasStationAddress;
	}

	private static void findClosestGasStations() {
		ArrayList<GasStation> gasStationList = new ArrayList<GasStation>();
		String gasStationResponse = getGasStationsJson();
		try {
			JSONObject gasStationResp = new JSONObject(gasStationResponse);
			JSONArray gasStations = gasStationResp.getJSONArray("stations");
			for (int i = 0; i < gasStations.length(); i++) {
				JSONObject closestGasStation = gasStations.getJSONObject(i);
				GasStation gasStation = new GasStation();
				gasStation.setCountry(closestGasStation.getString("country"));
				gasStation.setZip(closestGasStation.getString("zip"));
				gasStation.setPrice(closestGasStation.getString("reg_price"));
				gasStation.setAddress(closestGasStation.getString("address"));
				gasStation.setLat(closestGasStation.getString("lat"));
				gasStation.setLon(closestGasStation.getString("lng"));
				gasStation.setStation(closestGasStation.getString("station"));
				gasStation.setRegion(closestGasStation.getString("region"));
				//gasStation.setCity(closestGasStation.getString("city"));
				gasStation.setDistance(closestGasStation.getString("distance"));
				gasStationList.add(gasStation);
			}
			if(progress != null){
				progress.dismiss();
				progress = null;
			}
			Intent intent = new Intent(mContext, GasStationActivity.class);
			intent.putExtra("gasStationList", gasStationList);
			mContext.startActivity(intent);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void GetGasStations(Context context, GPSData gpsData) {
		mContext = context;
		mLatitude = gpsData.getLatitudeDegrees();
		mLongitude = gpsData.getLongitudeDegrees();
		progress = ProgressDialog.show(mContext, "Loading",
			    "Finding gas stations", true);
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				findClosestGasStations();
			}
		});
		t.start();

	}
}
