package com.ialert.applink;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.ford.syncV4.exception.SyncException;
import com.ford.syncV4.exception.SyncExceptionCause;
import com.ford.syncV4.proxy.SyncProxyALM;
import com.ford.syncV4.proxy.interfaces.IProxyListenerALM;
import com.ford.syncV4.proxy.rpc.AddCommandResponse;
import com.ford.syncV4.proxy.rpc.AddSubMenuResponse;
import com.ford.syncV4.proxy.rpc.AlertResponse;
import com.ford.syncV4.proxy.rpc.ChangeRegistrationResponse;
import com.ford.syncV4.proxy.rpc.CreateInteractionChoiceSetResponse;
import com.ford.syncV4.proxy.rpc.DeleteCommandResponse;
import com.ford.syncV4.proxy.rpc.DeleteFileResponse;
import com.ford.syncV4.proxy.rpc.DeleteInteractionChoiceSetResponse;
import com.ford.syncV4.proxy.rpc.DeleteSubMenuResponse;
import com.ford.syncV4.proxy.rpc.EncodedSyncPDataResponse;
import com.ford.syncV4.proxy.rpc.EndAudioPassThruResponse;
import com.ford.syncV4.proxy.rpc.GenericResponse;
import com.ford.syncV4.proxy.rpc.GetDTCsResponse;
import com.ford.syncV4.proxy.rpc.GetVehicleDataResponse;
import com.ford.syncV4.proxy.rpc.ListFilesResponse;
import com.ford.syncV4.proxy.rpc.OnAudioPassThru;
import com.ford.syncV4.proxy.rpc.OnButtonEvent;
import com.ford.syncV4.proxy.rpc.OnButtonPress;
import com.ford.syncV4.proxy.rpc.OnCommand;
import com.ford.syncV4.proxy.rpc.OnDriverDistraction;
import com.ford.syncV4.proxy.rpc.OnEncodedSyncPData;
import com.ford.syncV4.proxy.rpc.OnHMIStatus;
import com.ford.syncV4.proxy.rpc.OnLanguageChange;
import com.ford.syncV4.proxy.rpc.OnPermissionsChange;
import com.ford.syncV4.proxy.rpc.OnSyncPData;
import com.ford.syncV4.proxy.rpc.OnTBTClientState;
import com.ford.syncV4.proxy.rpc.OnVehicleData;
import com.ford.syncV4.proxy.rpc.PerformAudioPassThruResponse;
import com.ford.syncV4.proxy.rpc.PerformInteractionResponse;
import com.ford.syncV4.proxy.rpc.PutFileResponse;
import com.ford.syncV4.proxy.rpc.ReadDIDResponse;
import com.ford.syncV4.proxy.rpc.ResetGlobalPropertiesResponse;
import com.ford.syncV4.proxy.rpc.ScrollableMessageResponse;
import com.ford.syncV4.proxy.rpc.SetAppIconResponse;
import com.ford.syncV4.proxy.rpc.SetDisplayLayoutResponse;
import com.ford.syncV4.proxy.rpc.SetGlobalPropertiesResponse;
import com.ford.syncV4.proxy.rpc.SetMediaClockTimerResponse;
import com.ford.syncV4.proxy.rpc.Show;
import com.ford.syncV4.proxy.rpc.ShowResponse;
import com.ford.syncV4.proxy.rpc.SingleTireStatus;
import com.ford.syncV4.proxy.rpc.SliderResponse;
import com.ford.syncV4.proxy.rpc.SoftButton;
import com.ford.syncV4.proxy.rpc.SpeakResponse;
import com.ford.syncV4.proxy.rpc.SubscribeButtonResponse;
import com.ford.syncV4.proxy.rpc.SubscribeVehicleDataResponse;
import com.ford.syncV4.proxy.rpc.SyncPDataResponse;
import com.ford.syncV4.proxy.rpc.UnsubscribeButtonResponse;
import com.ford.syncV4.proxy.rpc.UnsubscribeVehicleDataResponse;
import com.ford.syncV4.proxy.rpc.enums.ButtonEventMode;
import com.ford.syncV4.proxy.rpc.enums.ButtonName;
import com.ford.syncV4.proxy.rpc.enums.ComponentVolumeStatus;
import com.ford.syncV4.proxy.rpc.enums.SoftButtonType;
import com.ford.syncV4.proxy.rpc.enums.SystemAction;
import com.ford.syncV4.proxy.rpc.enums.TextAlignment;
import com.ford.syncV4.transport.TCPTransportConfig;
import com.ford.syncV4.util.DebugTool;
import com.ialert.R;

public class AppLinkService extends Service implements IProxyListenerALM {
	// variable used to increment correlation ID for every request sent to SYNC
	public int autoIncCorrId = 0;
	// variable to contain the current state of the service
	private static AppLinkService instance = null;
	// variable to access the BluetoothAdapter
	private BluetoothAdapter mBtAdapter;
	// variable to create and call functions of the SyncProxy
	private SyncProxyALM proxy = null;
	
	private final int YES = 1000;
	private final int NO = 1001;

	private double mLatitude;
	private double mLongitude;
	
	private boolean RUN_IN_ALE = false;

	private int THREAD_SLEEP = 2000;

	private final String DEFAULT_ZIP_CODE = "44143";
	
	private int PORT = 12345;//Use this port number to connect to ALE
	
	// Use 10.0.2.2 to connect to local ALE from Android emulator
	// If connecting via Bluetooth, find out the IP address of the machine with
	// the ALE and enter it below.
	// Ensure that this machine can ping the device. Find the IP address of the
	// device from "Settings --> Wi-Fi --> <Selected Wi-Fi> --> IP address"
	private final String IP_ADDRESS_LOCALHOST = "192.168.254.19"; // "192.168.254.19";//"10.255.6.47";
	// //"127.0.0.1";
	private final String IP_ADDRESS_EMULATOR = "10.0.2.2";

	Vector<SoftButton> mainSoftButtons = new Vector<SoftButton>();
	Vector<ComponentVolumeStatus> tireStatuses = new Vector<ComponentVolumeStatus>();
	
	// Service shutdown timing constants
	private static final int CONNECTION_TIMEOUT = 60000;
	private static final int STOP_SERVICE_DELAY = 5000;
	
	/**
	 *  Runnable that stops this service if there hasn't been a connection to SYNC
	 *  within a reasonable amount of time since ACL_CONNECT.
	 */
	private Runnable mCheckConnectionRunnable = new Runnable() {
		@Override
		public void run() {
			Boolean stopService = true;
			// If the proxy has connected to SYNC, do NOT stop the service
			if (proxy != null && proxy.getIsConnected()) {
				stopService = false;
			}
			if (stopService) {
				mHandler.removeCallbacks(mCheckConnectionRunnable);
				mHandler.removeCallbacks(mStopServiceRunnable);
				stopSelf();
			}
		}
	};
	
	/**
	 * Runnable that stops this service on ACL_DISCONNECT after a short time delay.
	 * This is a workaround until some synchronization issues are fixed within the proxy.
	 */
	private Runnable mStopServiceRunnable = new Runnable() {
		@Override
		public void run() {
			// As long as the proxy is null or not connected to SYNC, stop the service
			if (proxy == null || !proxy.getIsConnected()) {
				mHandler.removeCallbacks(mCheckConnectionRunnable);
				mHandler.removeCallbacks(mStopServiceRunnable);
				stopSelf();
			}
		}
	};
	
	private Handler mHandler = new Handler();
	
	/**************** Utility functions *****************/
	private String getStr(int resourceId) {
		return AppLinkApplication.getCurrentActivity().getBaseContext()
				.getString(resourceId);
	}
	
	private boolean isAppRunningInEmulator() {
		String build = Build.BRAND;
		if (build.compareToIgnoreCase("generic") == 0)
			return true;
		return false;
	}

	private String getIP() {
		if (isAppRunningInEmulator()) {
			return IP_ADDRESS_EMULATOR;
		} else {
			return IP_ADDRESS_LOCALHOST;
		}
	}
	/************** End Utility Functions ****************/
	
	@SuppressWarnings("static-access")
	@Override
	public void onCreate() {
		super.onCreate();
		RUN_IN_ALE = !AppLinkApplication.getInstance().getRunInTdk();
		instance = this;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Remove any previous stop service runnables that could be from a recent ACL Disconnect
		mHandler.removeCallbacks(mStopServiceRunnable);

		// Start the proxy when the service starts
        if (intent != null) {
        	mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    		if (mBtAdapter != null) {
    			if (mBtAdapter.isEnabled()) {
    				startProxy();
    			} else {
        			startProxy();
        		}
    		} else {
    			startProxy();
    		}
		}
        
        // Queue the check connection runnable to stop the service if no connection is made
        mHandler.removeCallbacks(mCheckConnectionRunnable);
        mHandler.postDelayed(mCheckConnectionRunnable, CONNECTION_TIMEOUT);
        
        return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		disposeSyncProxy();
		LockScreenManager.clearLockScreen();
		instance = null;
		super.onDestroy();
	}
	
	public static AppLinkService getInstance() {
		return instance;
	}
		
	public SyncProxyALM getProxy() {
		return proxy;
	}
	
	/**
	 * Queue's a runnable that stops the service after a small delay,
	 * unless the proxy manages to reconnects to SYNC.
	 */
	public void stopService() {
        mHandler.removeCallbacks(mStopServiceRunnable);
        mHandler.postDelayed(mStopServiceRunnable, STOP_SERVICE_DELAY);
	}

	public void startProxy() {
		if (proxy == null) {
			try {
				//Phone or Android Emulator running against ALE
				if ((isAppRunningInEmulator() && RUN_IN_ALE) || (!isAppRunningInEmulator() && RUN_IN_ALE)) {
					proxy = new SyncProxyALM(this, getStr(R.string.app_name), false, String.valueOf(PORT), new TCPTransportConfig(PORT, getIP(), false));
				}
				//Phone running against TDK 
				else if(!isAppRunningInEmulator()){
					proxy = new SyncProxyALM(this, getStr(R.string.app_name),
							true, getStr(R.string.ford_app_id));
				} else {
					Toast.makeText(AppLinkApplication.getCurrentActivity().getBaseContext(), "App is running in an emulator and not connecting to the ALE.\nThis doesn't make sense", Toast.LENGTH_LONG).show();
				}
			} catch (SyncException e) {
				e.printStackTrace();
				// error creating proxy, returned proxy = null
				if (proxy == null) {
					stopSelf();
				}
			}
		}
	}
	
	public void disposeSyncProxy() {
		if (proxy != null) {
			try {
				proxy.dispose();
			} catch (SyncException e) {
				e.printStackTrace();
			}
			proxy = null;
			LockScreenManager.clearLockScreen();
		}
	}
	
	public void reset() {
		if (proxy != null) {
			try {
				proxy.resetProxy();
			} catch (SyncException e1) {
				e1.printStackTrace();
				//something goes wrong, & the proxy returns as null, stop the service.
				// do not want a running service with a null proxy
				if (proxy == null) {
					stopSelf();
				}
			}
		} else {
			startProxy();
		}
	}
	
	public void subButtons() {
		try {
	        proxy.subscribeButton(ButtonName.OK, autoIncCorrId++);
	        /*proxy.subscribeButton(ButtonName.SEEKLEFT, autoIncCorrId++);
			proxy.subscribeButton(ButtonName.SEEKRIGHT, autoIncCorrId++);
			proxy.subscribeButton(ButtonName.TUNEUP, autoIncCorrId++);
			proxy.subscribeButton(ButtonName.TUNEDOWN, autoIncCorrId++);
			proxy.subscribeButton(ButtonName.PRESET_1, autoIncCorrId++);
			proxy.subscribeButton(ButtonName.PRESET_2, autoIncCorrId++);
			proxy.subscribeButton(ButtonName.PRESET_3, autoIncCorrId++);
			proxy.subscribeButton(ButtonName.PRESET_4, autoIncCorrId++);
			proxy.subscribeButton(ButtonName.PRESET_5, autoIncCorrId++);
			proxy.subscribeButton(ButtonName.PRESET_6, autoIncCorrId++);
			proxy.subscribeButton(ButtonName.PRESET_7, autoIncCorrId++);
			proxy.subscribeButton(ButtonName.PRESET_8, autoIncCorrId++);
			proxy.subscribeButton(ButtonName.PRESET_9, autoIncCorrId++);
			proxy.subscribeButton(ButtonName.PRESET_0, autoIncCorrId++);*/
		} catch (SyncException e) {}
	}
   
	@Override
	public void onProxyClosed(String info, Exception e) {
		LockScreenManager.setHMILevelState(null);
		LockScreenManager.clearLockScreen();
		
		if ((((SyncException) e).getSyncExceptionCause() != SyncExceptionCause.SYNC_PROXY_CYCLED))
		{
			if (((SyncException) e).getSyncExceptionCause() != SyncExceptionCause.BLUETOOTH_DISABLED) 
			{
				Log.v(AppLinkApplication.TAG, "reset proxy in onproxy closed");
				reset();
			}
		}
	}
   
	@Override
	public void onOnHMIStatus(OnHMIStatus notification) {
		switch(notification.getSystemContext()) {
			case SYSCTXT_MAIN:
				break;
			case SYSCTXT_VRSESSION:
				break;
			case SYSCTXT_MENU:
				break;
			default:
				return;
		}

		switch(notification.getAudioStreamingState()) {
			case AUDIBLE:
				// play audio if applicable
				break;
			case NOT_AUDIBLE:
				// pause/stop/mute audio if applicable
				break;
			default:
				return;
		}

		LockScreenManager.setHMILevelState(notification.getHmiLevel());
		LockScreenManager.updateLockScreen();
		  
		switch(notification.getHmiLevel()) {
			case HMI_FULL:
				Log.i(AppLinkApplication.TAG, "HMI_FULL");
				if (notification.getFirstRun()) {
					// setup app on SYNC
					// send welcome message if applicable
					try {
						proxy.speak(getStr(R.string.welcome_message),
								autoIncCorrId++);
						proxy.show(getStr(R.string.welcome_screen_message_1),
								getStr(R.string.welcome_screen_message_2),
								TextAlignment.CENTERED, autoIncCorrId++);
					} catch (SyncException e) {
						DebugTool.logError("Failed to send Show", e);
					}
					// send addcommands
					// subscribe to buttons
					subButtons();
				}
				else {
					try {
						proxy.show("SyncProxy is", "Alive", TextAlignment.CENTERED, autoIncCorrId++);
					} catch (SyncException e) {
						DebugTool.logError("Failed to send Show", e);
					}
				}
				break;
			case HMI_LIMITED:
				Log.i(AppLinkApplication.TAG, "HMI_LIMITED");
				break;
			case HMI_BACKGROUND:
				Log.i(AppLinkApplication.TAG, "HMI_BACKGROUND");
				break;
			case HMI_NONE:
				Log.i(AppLinkApplication.TAG, "HMI_NONE");
				break;
			default:
				return;
		}
	}

	@Override
	public void onOnDriverDistraction(OnDriverDistraction notification) {
		LockScreenManager.setDriverDistractionState(notification.getState());
		LockScreenManager.updateLockScreen();
	}
	
	@Override
	public void onOnButtonEvent(OnButtonEvent notification) {
		if (notification.getButtonEventMode().equals(ButtonEventMode.BUTTONUP)) {
			switch (notification.getButtonName()) {
			case CUSTOM_BUTTON:
				switch (notification.getCustomButtonID()) {
				case YES:
					findDealerAndCall();
					break;
				case NO:
					promptThankYouMessage();
					break;
				default:
				}
				break;
			default:
				try {
					proxy.show("Getting", "tire status",
							TextAlignment.CENTERED, autoIncCorrId++);
					proxy.getvehicledata(true, false, false, false, false,
							false, false, false, false, true, false, false,
							false, false, false, false, false, false, false,
							false, false, false, false, false, false,
							autoIncCorrId++);
					Thread.sleep(THREAD_SLEEP);
				} catch (Exception e) {

				}
			}
		}
	}
	
	private void promptThankYouMessage() {
		try {
			proxy.speak("Thank you for using iAlert.", autoIncCorrId++);
			proxy.show("Thank you", "", TextAlignment.CENTERED, autoIncCorrId);
			Show msg = new Show();
			msg.setSoftButtons(new Vector<SoftButton>());
			proxy.sendRPCRequest(msg);
		} catch (Exception ex) {
		}
	}
	
	private void findDealerAndCall() {
		try {
			proxy.speak(getStr(R.string.low_tire_finding_dealer),
					autoIncCorrId++);
			String phoneNumber = getClosestFordDealerPhoneNumber();
			makePhonecall(phoneNumber);
		} catch (Exception e) {
		}
	}
	
	private void makePhonecall(String phoneNumber) {
		Intent intent = new Intent(Intent.ACTION_CALL);
		intent.setData(Uri.parse("tel:" + phoneNumber));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
		startActivity(intent);
	}

	private String getZipCode() {
		// geocoders not present in emulators, only implemented on devices
		Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
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

	private String getClosestFordDealerString() {
		HttpClient client = new DefaultHttpClient();
		String zipcode = getZipCode();
		if (zipcode == null)
			zipcode = DEFAULT_ZIP_CODE;
		String requestUrl = getStr(R.string.ford_dealer_url_template)
				.replaceAll("ZIP_CODE", zipcode);
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

	private String getClosestFordDealerPhoneNumber() {
		String dealerResponse = getClosestFordDealerString();
		String phoneNumber = null;
		try {
			JSONObject dealerResp = new JSONObject(dealerResponse);
			JSONObject response = dealerResp.getJSONObject("Response");
			JSONObject dealer = response.getJSONObject("Dealer");
			String strPhoneNumber = dealer.getString("Phone");
			phoneNumber = strPhoneNumber.replaceAll("\\D", "");
			String dealerName = dealer.getString("Name");
			proxy.show("Calling dealer", dealerName, TextAlignment.CENTERED,
					autoIncCorrId++);
			proxy.speak("Calling closest dealer named " + dealerName,
					autoIncCorrId++);
		} catch (Exception ex) {
		}
		return phoneNumber;
	}
	
	@Override
	public void onGetVehicleDataResponse(GetVehicleDataResponse response) {
		// 1. Get tire status of all tires.
		// 2. Store all tire statuses in vector
		// 3. Hand it off to other function to handle statuses
		SingleTireStatus leftFrontStatus = response.getTirePressure()
				.getLeftFront();
		SingleTireStatus leftRearStatus = response.getTirePressure()
				.getLeftRear();
		SingleTireStatus rightFrontStatus = response.getTirePressure()
				.getRightFront();
		SingleTireStatus rightRearStatus = response.getTirePressure()
				.getRightRear();

		tireStatuses.clear();
		tireStatuses.add(leftFrontStatus.getStatus());
		tireStatuses.add(leftRearStatus.getStatus());
		tireStatuses.add(rightFrontStatus.getStatus());
		tireStatuses.add(rightRearStatus.getStatus());

		handleTireStatuses(response);

		mLatitude = response.getGps().getLatitudeDegrees();
		mLongitude = response.getGps().getLongitudeDegrees();
	}
	
	private void handleTireStatuses(GetVehicleDataResponse response) {
		boolean lowPressureIndicator = true;
		Iterator<ComponentVolumeStatus> iter = tireStatuses.iterator();
		while (iter.hasNext()) {
			ComponentVolumeStatus status = iter.next();
			if (status.compareTo(ComponentVolumeStatus.ALERT) == 0
					|| status.compareTo(ComponentVolumeStatus.LOW) == 0
					|| status.compareTo(ComponentVolumeStatus.FAULT) == 0) {
				lowPressureIndicator = true;
			}
		}
		if (lowPressureIndicator) {
			handleLowTireStatus();
		} else {

		}
	}

	// send softbuttons in the show() command
	private void handleLowTireStatus() {
		setUpSoftbuttons();
		Show msg = new Show();
		msg.setCorrelationID(autoIncCorrId++);
		msg.setMainField1("Low Pressure.");
		msg.setMainField2("Call dealer?");
		msg.setMainField3("");
		msg.setMainField4("");
		msg.setSoftButtons(mainSoftButtons);

		try {
			proxy.speak(getStr(R.string.low_tire_speak_message_1),
					autoIncCorrId++);
			proxy.sendRPCRequest(msg);
		} catch (SyncException e) {

		}
	}

	public void setUpSoftbuttons() {
		SoftButton sb1 = new SoftButton();
		sb1.setSoftButtonID(YES);
		sb1.setText("Yes");
		sb1.setType(SoftButtonType.SBT_TEXT);
		sb1.setIsHighlighted(false);
		sb1.setSystemAction(SystemAction.DEFAULT_ACTION);

		SoftButton sb2 = new SoftButton();
		sb2.setSoftButtonID(NO);
		sb2.setText("No");
		sb2.setType(SoftButtonType.SBT_TEXT);
		sb2.setIsHighlighted(false);
		sb2.setSystemAction(SystemAction.DEFAULT_ACTION);

		Vector<SoftButton> currentSoftButtons = new Vector<SoftButton>();
		// choose the order softbuttons appear
		currentSoftButtons.add(sb1);
		currentSoftButtons.add(sb2);

		mainSoftButtons = currentSoftButtons;
	}

	/******************* Unused handlers **********************/
	
	@Override
	public void onError(String info, Exception e) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onGenericResponse(GenericResponse response) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onOnCommand(OnCommand notification) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onAddCommandResponse(AddCommandResponse response) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onAddSubMenuResponse(AddSubMenuResponse response) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onCreateInteractionChoiceSetResponse(
			CreateInteractionChoiceSetResponse response) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onAlertResponse(AlertResponse response) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onDeleteCommandResponse(DeleteCommandResponse response) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onDeleteInteractionChoiceSetResponse(
			DeleteInteractionChoiceSetResponse response) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onDeleteSubMenuResponse(DeleteSubMenuResponse response) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onPerformInteractionResponse(PerformInteractionResponse response) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onResetGlobalPropertiesResponse(
			ResetGlobalPropertiesResponse response) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onSetGlobalPropertiesResponse(SetGlobalPropertiesResponse response) {
	}
	
	@Override
	public void onSetMediaClockTimerResponse(SetMediaClockTimerResponse response) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onShowResponse(ShowResponse response) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onSpeakResponse(SpeakResponse response) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onOnButtonPress(OnButtonPress notification) {
	}
	
	@Override
	public void onSubscribeButtonResponse(SubscribeButtonResponse response) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onUnsubscribeButtonResponse(UnsubscribeButtonResponse response) {
		// TODO Auto-generated method stub	
	}
	
	@Override
	public void onOnPermissionsChange(OnPermissionsChange notification) {
		// TODO Auto-generated method stub	
	}
	
	@Override
	public void onOnTBTClientState(OnTBTClientState notification) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		  return null;
	}
	 
	
	@Override
	public void onSubscribeVehicleDataResponse(SubscribeVehicleDataResponse response) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onUnsubscribeVehicleDataResponse(
			UnsubscribeVehicleDataResponse response) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onReadDIDResponse(ReadDIDResponse response) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onGetDTCsResponse(GetDTCsResponse response) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onOnVehicleData(OnVehicleData notification) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onPerformAudioPassThruResponse(PerformAudioPassThruResponse response) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onEndAudioPassThruResponse(EndAudioPassThruResponse response) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onOnAudioPassThru(OnAudioPassThru notification) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onPutFileResponse(PutFileResponse response) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onDeleteFileResponse(DeleteFileResponse response) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onListFilesResponse(ListFilesResponse response) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onSetAppIconResponse(SetAppIconResponse response) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onScrollableMessageResponse(ScrollableMessageResponse response) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onChangeRegistrationResponse(ChangeRegistrationResponse response) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onSetDisplayLayoutResponse(SetDisplayLayoutResponse response) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onOnLanguageChange(OnLanguageChange notification) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onSliderResponse(SliderResponse response) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEncodedSyncPDataResponse(EncodedSyncPDataResponse response) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSyncPDataResponse(SyncPDataResponse response) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onOnEncodedSyncPData(OnEncodedSyncPData notification) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onOnSyncPData(OnSyncPData notification) {
		// TODO Auto-generated method stub
		
	}
}
