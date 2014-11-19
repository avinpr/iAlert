package com.ialert.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ford.syncV4.proxy.rpc.GPSData;
import com.ialert.R;
import com.ialert.applink.AppLinkActivity;
import com.ialert.applink.AppLinkApplication;
import com.ialert.applink.AppLinkService;
import com.ialert.utilities.GasStationHelper;
import com.ialert.utilities.LocationHelper;
import com.ialert.utilities.ServiceCenterHelper;
import com.ialert.utilities.TirePressure;
import com.ialert.utilities.VehicleDataHelper;

public class DashboardActivity extends AppLinkActivity {

	private final String mSharedPrefs = "com.ialert.diagnosticdata";
	private final String DEFAULT_STRING = "Unknown";

	protected TextView mReportLastReceived;
	protected TextView mRightRear;
	protected TextView mLeftRear;
	protected TextView mRightFront;
	protected TextView mLeftFront;
	protected TextView mFuelStatus;
	protected TextView mAirbagStatus;
	protected TextView mVinNumber;
	protected TextView mOdometerStatus;
	protected TextView mBatteryStatus;

	protected LinearLayout mTirePressureLayout;
	protected LinearLayout mFuelStatusLayout;
	protected LinearLayout mBatteryStatusLayout;
	protected LinearLayout mAirbagStatusLayout;

	private VehicleReportData mVehicleReportData;

	private void showLocationNotEnabledMessage() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				DashboardActivity.this);
		builder.setMessage(
				"Please check your network and GPS settings and try again later")
				.setPositiveButton("OK", null).show();
	}

	DialogInterface.OnClickListener tirePressureDialogClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				if (mVehicleReportData != null
						&& mVehicleReportData.getGpsData() != null) {
					ServiceCenterHelper.PlacePhoneCall(DashboardActivity.this,
							mVehicleReportData.getGpsData());
				} else {
					GPSData gpsData = LocationHelper
							.getGPSData(DashboardActivity.this);
					if (gpsData.getLatitudeDegrees() != null
							&& gpsData.getLatitudeDegrees() != 0.0) {
						ServiceCenterHelper.PlacePhoneCall(
								DashboardActivity.this, gpsData);
					} else {
						showLocationNotEnabledMessage();
					}
				}
				break;

			case DialogInterface.BUTTON_NEGATIVE:
				break;
			}
		}
	};

	protected View.OnClickListener mTirePressureOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					DashboardActivity.this);
			builder.setMessage(
					"Would you like us to find the closest Ford dealers?")
					.setPositiveButton("Yes", tirePressureDialogClickListener)
					.setNegativeButton("No", tirePressureDialogClickListener)
					.show();
		}
	};

	DialogInterface.OnClickListener fuelStatusDialogClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				if (mVehicleReportData != null
						&& mVehicleReportData.getGpsData() != null) {
					GasStationHelper.NavigateToClosestGasStation(
							DashboardActivity.this,
							mVehicleReportData.getGpsData());
				} else {
					GPSData gpsData = LocationHelper
							.getGPSData(DashboardActivity.this);
					if (gpsData.getLatitudeDegrees() != null
							&& gpsData.getLatitudeDegrees() != 0.0) {
						GasStationHelper.NavigateToClosestGasStation(
								DashboardActivity.this, gpsData);
					} else {
						showLocationNotEnabledMessage();
					}
				}
				break;

			case DialogInterface.BUTTON_NEGATIVE:
				break;
			}
		}
	};

	protected View.OnClickListener mFuelStatusOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					DashboardActivity.this);
			builder.setMessage(
					"Would you like us to find the closest gas stations?")
					.setPositiveButton("Yes", fuelStatusDialogClickListener)
					.setNegativeButton("No", fuelStatusDialogClickListener)
					.show();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dashboard);

		mReportLastReceived = (TextView) findViewById(R.id.report_last_received);
		mRightRear = (TextView) findViewById(R.id.right_rear);
		mLeftRear = (TextView) findViewById(R.id.left_rear);
		mRightFront = (TextView) findViewById(R.id.right_front);
		mLeftFront = (TextView) findViewById(R.id.left_front);
		mFuelStatus = (TextView) findViewById(R.id.fuel_status);
		mAirbagStatus = (TextView) findViewById(R.id.airbag_status);
		// mVinNumber = (TextView) findViewById(R.id.vin_number);

		mOdometerStatus = (TextView) findViewById(R.id.odometer_status);
		mBatteryStatus = (TextView) findViewById(R.id.battery_status);

		mTirePressureLayout = (LinearLayout) findViewById(R.id.tire_pressure_layout);
		mTirePressureLayout.setOnClickListener(mTirePressureOnClickListener);
		mFuelStatusLayout = (LinearLayout) findViewById(R.id.fuel_status_layout);
		mFuelStatusLayout.setOnClickListener(mFuelStatusOnClickListener);
		mBatteryStatusLayout = (LinearLayout) findViewById(R.id.battery_status_layout);
		mBatteryStatusLayout.setOnClickListener(mTirePressureOnClickListener);
		mAirbagStatusLayout = (LinearLayout) findViewById(R.id.airbag_status_layout);

		if (bluetoothAvailable()) {
			startSyncProxy();
		} else {
			promptBluetoothWarning();
		}
		readAndSetFromSharedPrefs();
	}

	private void promptBluetoothWarning() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Bluetooth not enabled");
		builder.setMessage(
				"iAlert requires the use of bluetooth. Would you like to enable bluetooth now?")
				.setPositiveButton("Yes", dialogClickListener)
				.setNegativeButton("No", dialogClickListener).show();
	}

	private DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				final Intent intent = new Intent(Intent.ACTION_MAIN, null);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				ComponentName cn = new ComponentName("com.android.settings",
						"com.android.settings.bluetooth.BluetoothSettings");
				intent.setComponent(cn);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				break;
			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		if (AppLinkService.getInstance() != null
				&& AppLinkService.getInstance().getProxy() == null
				&& bluetoothAvailable()) {
			startSyncProxy();
		}
	}

	private boolean bluetoothAvailable() {
		boolean btAvailable = false;
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter != null && btAdapter.isEnabled()
				&& !btAdapter.isDiscovering()) {
			btAvailable = true;
		}
		return btAvailable;
	}

	private void readAndSetFromSharedPrefs() {
		SharedPreferences sharedPrefs = this.getSharedPreferences(mSharedPrefs,
				Context.MODE_PRIVATE);
		if (sharedPrefs.contains(Constants.LAST_MODIFIED)) {
			mReportLastReceived.setText(sharedPrefs.getString(
					Constants.LAST_MODIFIED, DEFAULT_STRING));
			mRightRear.setText(sharedPrefs.getString(Constants.RIGHT_REAR,
					DEFAULT_STRING));
			mLeftRear.setText(sharedPrefs.getString(Constants.LEFT_REAR,
					DEFAULT_STRING));
			mRightFront.setText(sharedPrefs.getString(Constants.RIGHT_FRONT,
					DEFAULT_STRING));
			mLeftFront.setText(sharedPrefs.getString(Constants.LEFT_FRONT,
					DEFAULT_STRING));
			mFuelStatus.setText(sharedPrefs.getString(Constants.FUEL,
					DEFAULT_STRING));

			mAirbagStatus.setText(sharedPrefs.getString(Constants.AIRBAG,
					DEFAULT_STRING));

			/*
			 * mVinNumber.setText(sharedPrefs.getString(Constants.VIN,
			 * DEFAULT_STRING));
			 */
			mOdometerStatus.setText(sharedPrefs.getString(Constants.ODOMETER,
					DEFAULT_STRING));
			mBatteryStatus.setText(sharedPrefs.getString(Constants.BATTERY,
					DEFAULT_STRING));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@SuppressWarnings("static-access")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.reset:
			startSyncProxy();
			return true;
		case R.id.about:
			AppLinkApplication.getInstance().showAppVersion(this);
			return true;
		case R.id.tdk:
			boolean runInTdk = AppLinkApplication.getInstance().getRunInTdk();
			AppLinkApplication.getInstance().setRunInTdk(!runInTdk);
			String message = getString(R.string.menu_tdk_toast_message)
					+ !runInTdk;
			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
			return true;
		case R.id.history:
			startHistoryActivity();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void startHistoryActivity() {
		Intent intent = new Intent(this, HistoryActivity.class);
		startActivity(intent);
	}

	private void startSyncProxy() {
		AppLinkApplication.getInstance().endSyncProxyInstance();
		AppLinkApplication.getInstance().startSyncProxyService();
	}

	private String getCurrentDateAndTime() {
		final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		Date now = new Date();
		String currentDateAndTime = sdf.format(now);
		currentDateAndTime += ((now.getHours() > 12) ? " PM" : " AM");
		return currentDateAndTime;
	}

	public void DisplayVehicleData(final VehicleReportData data) {
		mVehicleReportData = data;
		final String lastReported = "Report last received on:"
				+ getCurrentDateAndTime();
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mReportLastReceived.setText(lastReported);
						mRightRear.setText(VehicleDataHelper
								.GetRightRearTirePressureStatus(data));
						mLeftRear.setText(VehicleDataHelper
								.GetLeftRearTirePressureStatus(data));
						mRightFront.setText(VehicleDataHelper
								.GetRightFrontTirePressureStatus(data));
						mLeftFront.setText(VehicleDataHelper
								.GetLeftFrontTirePressureStatus(data));
						boolean hasLowFuel = VehicleDataHelper.HasLowFuel(data);
						mFuelStatus.setText(VehicleDataHelper
								.GetFuelLevel(data));
						if (hasLowFuel) {
							mFuelStatus.setTextColor(Color.RED);
						}

						String airbagStatus = VehicleDataHelper
								.GetAirbagStatus(data);
						mAirbagStatus.setText(airbagStatus);
						if (airbagStatus == VehicleDataHelper.ALERT_STATUS) {
							mAirbagStatus.setTextColor(Color.RED);
						}

						String batteryStatus = VehicleDataHelper
								.GetBatteryStatus(data);
						mBatteryStatus.setText(batteryStatus);
						if (batteryStatus == VehicleDataHelper.ALERT_STATUS) {
							mBatteryStatus.setTextColor(Color.RED);
						}

						// mVinNumber.setText(VehicleDataHelper.GetVin(data));
						mOdometerStatus.setText(VehicleDataHelper
								.GetOdometerReading(data));
						saveToSharedPrefs();
					}
				});
			}
		});
		t.start();
		if (VehicleDataHelper.HasAnyAlert(data)) {
			// SaveToHistory();
		}
	}

	private void SaveToHistory() {
		SharedPreferences sharedPrefs = this.getSharedPreferences(mSharedPrefs,
				Context.MODE_PRIVATE);
		JSONObject json;
		if (!sharedPrefs.contains(Constants.HISTORY)) {
			json = new JSONObject();
			String currentDateString = getCurrentDateAndTime();
			try {
				json.put("dateTime", currentDateString);
				json.put("vin", mVehicleReportData.getVin());
				JSONObject alertsObj = new JSONObject();
				ArrayList alerts = new ArrayList();
				if (VehicleDataHelper.IsTirePressureLow(mVehicleReportData)) {
					JSONObject alertObj = new JSONObject();
					alertObj.put("type", Constants.HISTORY_LOW_TIRE_PRESSURE);
					alertObj.put("vin", mVehicleReportData.getVin());
					alertObj.put("dateTime", getCurrentDateAndTime());
					alertObj.put("name",
							Constants.HISTORY_LOW_TIRE_PRESSURE_NAME);
					ArrayList detailArray = new ArrayList();
					Vector<TirePressure> lowTirePressures = VehicleDataHelper
							.GetLowTirePressureStatuses(mVehicleReportData);
					Iterator<TirePressure> iter = lowTirePressures.iterator();
					while (iter.hasNext()) {
						TirePressure tirePressure = iter.next();
						JSONObject detail = new JSONObject();
						detail.put("position", tirePressure.getName());
						detail.put("status", tirePressure.getStatus().name());
						detailArray.add(detail);
					}
					alerts.add(detailArray);
				}
				// alertObj.put("name", value)
			} catch (Exception ex) {
				Log.d(AppLinkApplication.TAG, ex.toString());
			}
		}
	}

	private void saveToSharedPrefs() {
		SharedPreferences sharedPrefs = this.getSharedPreferences(mSharedPrefs,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.commit();
		editor.putString(Constants.LAST_MODIFIED, mReportLastReceived.getText()
				.toString());
		editor.putString(Constants.RIGHT_REAR, mRightRear.getText().toString());
		editor.putString(Constants.LEFT_REAR, mLeftRear.getText().toString());
		editor.putString(Constants.RIGHT_FRONT, mRightFront.getText()
				.toString());
		editor.putString(Constants.FUEL, mFuelStatus.getText().toString());
		editor.putString(Constants.AIRBAG, mAirbagStatus.getText().toString());
		// editor.putString(Constants.VIN, mVinNumber.getText().toString());
		editor.putString(Constants.ODOMETER, mOdometerStatus.getText()
				.toString());
		editor.putString(Constants.BATTERY, mBatteryStatus.getText().toString());
		editor.commit();
	}
}
