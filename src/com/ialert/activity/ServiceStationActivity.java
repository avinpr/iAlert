package com.ialert.activity;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ialert.R;
import com.ialert.utilities.ServiceStation;
import com.ialert.utilities.ServiceStationAdapter;

public class ServiceStationActivity extends Activity {

	protected boolean mPopupShowing = false;
	protected PopupWindow popupWindow;
	protected ServiceStation serviceStation;

	private View.OnClickListener mCloseListener = new View.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			popupWindow.dismiss();
			mPopupShowing = false;
		}
	};

	@Override
	public void onBackPressed() {
		if (mPopupShowing) {
			popupWindow.dismiss();
			mPopupShowing = false;
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_service_stations);
		Bundle bundle = getIntent().getExtras();
		List<ServiceStation> serviceStationList = bundle
				.getParcelableArrayList("serviceStationList");
		ListView serviceStationListView = (ListView) findViewById(R.id.serviceStationList);
		final View mainView = (LinearLayout) findViewById(R.id.mainView);
		serviceStationListView.setAdapter(new ServiceStationAdapter(this,
				serviceStationList));
		serviceStationListView
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
								.getSystemService(LAYOUT_INFLATER_SERVICE);
						View popupView = layoutInflater.inflate(
								R.layout.service_station_popup, null);
						TextView name = (TextView) popupView
								.findViewById(R.id.popServiceCenterName);
						TextView phone = (TextView) popupView
								.findViewById(R.id.popServiceCenterPhone);
						TextView fax = (TextView) popupView
								.findViewById(R.id.popServiceCenterFax);
						TextView website = (TextView) popupView
								.findViewById(R.id.popServiceCenterWebsite);
						TextView address = (TextView) popupView
								.findViewById(R.id.popServiceCenterAddress);
						Button close = (Button) popupView
								.findViewById(R.id.popServiceCenterClose);
						Button navigate = (Button) popupView
								.findViewById(R.id.popServiceCenterNavigate);
						Button call = (Button) popupView
								.findViewById(R.id.popServiceCenterCall);
						Button viewWebsite = (Button) popupView
								.findViewById(R.id.popServiceCenterViewWebsite);
						Button makeAppointment = (Button) popupView
								.findViewById(R.id.popServiceCenterMakeAppointment);

						serviceStation = (ServiceStation) parent.getAdapter()
								.getItem(position);
						name.setText(serviceStation.getName());
						phone.setText(serviceStation.getPhone());
						fax.setText(serviceStation.getFax());
						website.setText(serviceStation.getUrl());
						String strAddress = serviceStation.getAddress() + ", "
								+ serviceStation.getCity() + ", "
								+ serviceStation.getRegion() + ", "
								+ serviceStation.getZip();
						address.setText(strAddress);
						popupWindow = new PopupWindow(popupView,
								LayoutParams.MATCH_PARENT,
								LayoutParams.MATCH_PARENT);
						close.setOnClickListener(mCloseListener);
						navigate.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View arg0) {
								String mapUrl = getString(
										R.string.maps_url_template)
										.replaceAll(
												"LATITUDE2",
												String.valueOf(serviceStation
														.getLat())).replaceAll(
												"LONGITUDE2",
												String.valueOf(serviceStation
														.getLon()));
								Intent navIntent = new Intent(
										Intent.ACTION_VIEW, Uri.parse(mapUrl));
								startActivity(navIntent);
							}
						});
						call.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View arg0) {
								Intent intent = new Intent(Intent.ACTION_CALL);
								intent.setData(Uri.parse("tel:"
										+ serviceStation.getPhone()));
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
								startActivity(intent);
							}
						});
						viewWebsite
								.setOnClickListener(new View.OnClickListener() {

									@Override
									public void onClick(View arg0) {
										Intent browserIntent = new Intent(
												Intent.ACTION_VIEW, Uri
														.parse(serviceStation
																.getUrl()));
										startActivity(browserIntent);
									}
								});
						makeAppointment
								.setOnClickListener(new View.OnClickListener() {

									@Override
									public void onClick(View arg0) {
										Intent browserIntent = new Intent(
												Intent.ACTION_VIEW,
												Uri.parse(serviceStation
														.getAppointmentUrl()));
										startActivity(browserIntent);
									}
								});

						popupWindow.showAtLocation(mainView, Gravity.CENTER,
								10, 10);
						mPopupShowing = true;
					}
				});
	}
}
