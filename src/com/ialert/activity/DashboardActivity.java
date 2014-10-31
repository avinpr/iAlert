package com.ialert.activity;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.ialert.utilities.GasStationHelper;
import com.ialert.utilities.LocationHelper;
import com.ialert.utilities.ServiceCenterHelper;
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
	protected TextView mOdometerStatus;
	protected TextView mBatteryStatus;

	protected LinearLayout mTirePressureLayout;
	protected LinearLayout mFuelStatusLayout;
	protected LinearLayout mBatteryStatusLayout;

	private VehicleReportData mVehicleReportData;

	private void showLocationNotEnabledMessage(){
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
				if (mVehicleReportData != null) {
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
			builder.setMessage("Would you like to call the closest dealer?")
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
				if (mVehicleReportData != null) {
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
					"Would you like to navigate to the closest gas station?")
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
		mOdometerStatus = (TextView) findViewById(R.id.odometer_status);
		mBatteryStatus = (TextView) findViewById(R.id.battery_status);

		mTirePressureLayout = (LinearLayout) findViewById(R.id.tire_pressure_layout);
		mTirePressureLayout.setOnClickListener(mTirePressureOnClickListener);
		mFuelStatusLayout = (LinearLayout) findViewById(R.id.fuel_status_layout);
		mFuelStatusLayout.setOnClickListener(mFuelStatusOnClickListener);
		mBatteryStatusLayout = (LinearLayout) findViewById(R.id.battery_status_layout);
		mBatteryStatusLayout.setOnClickListener(mTirePressureOnClickListener);

		startSyncProxy();
		readAndSetFromSharedPrefs();
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
			startSyncProxy();
			String message = getString(R.string.menu_tdk_toast_message) + runInTdk;
			Toast.makeText(this, message,
					Toast.LENGTH_LONG).show();
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
		final String airbagStatus = VehicleDataHelper.GetAirbagStatus(data
				.getAirbagStatus());
		final String odometer = data.getOdometer().toString();
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mReportLastReceived.setText(lastReported);
						mRightRear.setText(data.getTireStatus().getRightRear()
								.getStatus().name());
						mLeftRear.setText(data.getTireStatus().getLeftRear()
								.getStatus().name());
						mRightFront.setText(data.getTireStatus()
								.getRightFront().getStatus().name());
						mLeftFront.setText(data.getTireStatus().getLeftFront()
								.getStatus().name());
						mFuelStatus.setText(data.getFuelStatus().name());
						mAirbagStatus.setText(airbagStatus);
						mOdometerStatus.setText(odometer);
						saveToSharedPrefs();
					}
				});
			}
		});
		t.start();
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
		editor.putString(Constants.ODOMETER, mOdometerStatus.getText()
				.toString());
		editor.putString(Constants.BATTERY, mBatteryStatus.getText().toString());
		editor.commit();
	}
}
