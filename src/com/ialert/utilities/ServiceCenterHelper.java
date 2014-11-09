package com.ialert.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;

import com.ford.syncV4.proxy.rpc.GPSData;
import com.ialert.R;
import com.ialert.activity.ServiceStationActivity;

public class ServiceCenterHelper {

	protected static Context mContext;
	private static final String DEFAULT_ZIP_CODE = "90015";// zip code of LA
															// convention center
	private static double mLatitude;
	private static double mLongitude;

	public static void PlacePhoneCall(Context context, GPSData gpsData) {
		GetServiceStations(context, gpsData);
		/*
		 * mContext = context; mLatitude = gpsData.getLatitudeDegrees();
		 * mLongitude = gpsData.getLongitudeDegrees(); Thread t = new Thread(new
		 * Runnable() {
		 * 
		 * @Override public void run() { makePhonecall(); } }); t.start();
		 */

	}

	private static void makePhonecall() {
		String phoneNumber = getClosestFordDealerPhoneNumber();
		Intent intent = new Intent(Intent.ACTION_CALL);
		intent.setData(Uri.parse("tel:" + phoneNumber));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
		mContext.startActivity(intent);
	}

	private static String getZipCode() {
		// geocoders not present in emulators, only implemented on devices
		Geocoder geocoder = new Geocoder(mContext, Locale.ENGLISH);
		String zipCode = null;
		try {
			List<Address> addresses = geocoder.getFromLocation(mLatitude,
					mLongitude, 1);
			if (addresses != null && !addresses.isEmpty()) {
				Address address = addresses.get(0);
				zipCode = address.getPostalCode();
			}
		} catch (Exception e) {
		}
		return zipCode;
	}

	private static String getClosestFordDealersJson() {
		HttpClient client = new DefaultHttpClient();
		String zipcode = getZipCode();
		if (zipcode == null)
			zipcode = DEFAULT_ZIP_CODE;
		String requestUrl = mContext
				.getString(R.string.ford_dealer_url_template)
				.replaceAll("ZIP_CODE", zipcode).replaceAll("PAGE_SIZE", "30");
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

	private static String getClosestFordDealerPhoneNumber() {
		String dealerResponse = getClosestFordDealersJson();
		String phoneNumber = null;
		try {
			JSONObject dealerResp = new JSONObject(dealerResponse);
			JSONObject response = dealerResp.getJSONObject("Response");
			JSONObject dealer = response.getJSONObject("Dealer");
			String strPhoneNumber = dealer.getString("Phone");
			phoneNumber = strPhoneNumber.replaceAll("\\D", "");
		} catch (Exception ex) {
		}
		return phoneNumber;
	}

	private static void findClosestServiceStations() {
		ArrayList<ServiceStation> serviceStationList = new ArrayList<ServiceStation>();
		String serviceStationResponse = getClosestFordDealersJson();
		try {
			JSONObject dealerResp = new JSONObject(serviceStationResponse);
			JSONObject response = dealerResp.getJSONObject("Response");
			JSONArray serviceStations = response.getJSONArray("Dealer");
			for (int i = 0; i < serviceStations.length(); i++) {
				JSONObject serviceStationObj = serviceStations.getJSONObject(i);
				ServiceStation serviceStation = new ServiceStation();
				serviceStation.setName(serviceStationObj.getString("Name"));
				serviceStation.setLat(serviceStationObj.getString("Latitude"));
				serviceStation.setLon(serviceStationObj.getString("Longitude"));
				serviceStation.setUrl(serviceStationObj.getString("URL"));
				serviceStation.setPhone(serviceStationObj.getString("Phone"));
				serviceStation.setDistance(serviceStationObj
						.getString("Distance"));
				serviceStation.setEmail(serviceStationObj.getString("Email"));
				serviceStation.setFax(serviceStationObj.getString("Fax"));

				JSONObject addressObj = serviceStationObj
						.getJSONObject("Address");
				serviceStation.setAddress(addressObj.getString("Street1"));
				serviceStation.setCity(addressObj.getString("City"));
				serviceStation.setZip(addressObj.getString("PostalCode"));
				serviceStation.setCountry(addressObj.getString("Country"));
				serviceStation.setRegion(addressObj.getString("State"));				

				serviceStationList.add(serviceStation);
			}
			Intent intent = new Intent(mContext, ServiceStationActivity.class);
			intent.putExtra("serviceStationList", serviceStationList);
			mContext.startActivity(intent);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void GetServiceStations(Context context, GPSData gpsData) {
		mContext = context;
		mLatitude = gpsData.getLatitudeDegrees();
		mLongitude = gpsData.getLongitudeDegrees();
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				findClosestServiceStations();
			}
		});
		t.start();

	}

}
