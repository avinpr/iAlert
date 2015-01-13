package com.ialert.applink;

import java.util.Vector;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.ford.syncV4.exception.SyncException;
import com.ford.syncV4.exception.SyncExceptionCause;
import com.ford.syncV4.proxy.RPCResponse;
import com.ford.syncV4.proxy.SyncProxyALM;
import com.ford.syncV4.proxy.TTSChunkFactory;
import com.ford.syncV4.proxy.interfaces.IProxyListenerALM;
import com.ford.syncV4.proxy.rpc.AddCommand;
import com.ford.syncV4.proxy.rpc.AddCommandResponse;
import com.ford.syncV4.proxy.rpc.AddSubMenuResponse;
import com.ford.syncV4.proxy.rpc.AirbagStatus;
import com.ford.syncV4.proxy.rpc.AlertResponse;
import com.ford.syncV4.proxy.rpc.BeltStatus;
import com.ford.syncV4.proxy.rpc.BodyInformation;
import com.ford.syncV4.proxy.rpc.ChangeRegistrationResponse;
import com.ford.syncV4.proxy.rpc.Choice;
import com.ford.syncV4.proxy.rpc.CreateInteractionChoiceSet;
import com.ford.syncV4.proxy.rpc.CreateInteractionChoiceSetResponse;
import com.ford.syncV4.proxy.rpc.DeleteCommandResponse;
import com.ford.syncV4.proxy.rpc.DeleteFileResponse;
import com.ford.syncV4.proxy.rpc.DeleteInteractionChoiceSetResponse;
import com.ford.syncV4.proxy.rpc.DeleteSubMenuResponse;
import com.ford.syncV4.proxy.rpc.DeviceStatus;
import com.ford.syncV4.proxy.rpc.ECallInfo;
import com.ford.syncV4.proxy.rpc.EmergencyEvent;
import com.ford.syncV4.proxy.rpc.EncodedSyncPDataResponse;
import com.ford.syncV4.proxy.rpc.EndAudioPassThruResponse;
import com.ford.syncV4.proxy.rpc.GPSData;
import com.ford.syncV4.proxy.rpc.GenericResponse;
import com.ford.syncV4.proxy.rpc.GetDTCsResponse;
import com.ford.syncV4.proxy.rpc.GetVehicleDataResponse;
import com.ford.syncV4.proxy.rpc.HeadLampStatus;
import com.ford.syncV4.proxy.rpc.ListFilesResponse;
import com.ford.syncV4.proxy.rpc.MenuParams;
import com.ford.syncV4.proxy.rpc.MyKey;
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
import com.ford.syncV4.proxy.rpc.PerformInteraction;
import com.ford.syncV4.proxy.rpc.PerformInteractionResponse;
import com.ford.syncV4.proxy.rpc.PutFileResponse;
import com.ford.syncV4.proxy.rpc.ReadDIDResponse;
import com.ford.syncV4.proxy.rpc.ResetGlobalPropertiesResponse;
import com.ford.syncV4.proxy.rpc.ScrollableMessageResponse;
import com.ford.syncV4.proxy.rpc.SetAppIconResponse;
import com.ford.syncV4.proxy.rpc.SetDisplayLayoutResponse;
import com.ford.syncV4.proxy.rpc.SetGlobalPropertiesResponse;
import com.ford.syncV4.proxy.rpc.SetMediaClockTimerResponse;
import com.ford.syncV4.proxy.rpc.ShowResponse;
import com.ford.syncV4.proxy.rpc.SliderResponse;
import com.ford.syncV4.proxy.rpc.SoftButton;
import com.ford.syncV4.proxy.rpc.SpeakResponse;
import com.ford.syncV4.proxy.rpc.SubscribeButtonResponse;
import com.ford.syncV4.proxy.rpc.SubscribeVehicleDataResponse;
import com.ford.syncV4.proxy.rpc.SyncPDataResponse;
import com.ford.syncV4.proxy.rpc.TTSChunk;
import com.ford.syncV4.proxy.rpc.TireStatus;
import com.ford.syncV4.proxy.rpc.UnsubscribeButtonResponse;
import com.ford.syncV4.proxy.rpc.UnsubscribeVehicleDataResponse;
import com.ford.syncV4.proxy.rpc.VrHelpItem;
import com.ford.syncV4.proxy.rpc.enums.ButtonEventMode;
import com.ford.syncV4.proxy.rpc.enums.ButtonName;
import com.ford.syncV4.proxy.rpc.enums.ComponentVolumeStatus;
import com.ford.syncV4.proxy.rpc.enums.DriverDistractionState;
import com.ford.syncV4.proxy.rpc.enums.InteractionMode;
import com.ford.syncV4.proxy.rpc.enums.PRNDL;
import com.ford.syncV4.proxy.rpc.enums.Result;
import com.ford.syncV4.proxy.rpc.enums.SoftButtonType;
import com.ford.syncV4.proxy.rpc.enums.SpeechCapabilities;
import com.ford.syncV4.proxy.rpc.enums.SystemAction;
import com.ford.syncV4.proxy.rpc.enums.TextAlignment;
import com.ford.syncV4.proxy.rpc.enums.VehicleDataEventStatus;
import com.ford.syncV4.proxy.rpc.enums.WiperStatus;
import com.ford.syncV4.transport.TCPTransportConfig;
import com.ford.syncV4.util.DebugTool;
import com.ialert.R;
import com.ialert.activity.Constants;
import com.ialert.activity.DashboardActivity;
import com.ialert.activity.VehicleReportData;
import com.ialert.utilities.GasStationHelper;
import com.ialert.utilities.LocationHelper;
import com.ialert.utilities.VehicleDataHelper;

public class AppLinkService extends Service implements IProxyListenerALM {
	// variable used to increment correlation ID for every request sent to SYNC
	public int autoIncCorrId = 0;
	// variable to contain the current state of the service
	private static AppLinkService instance = null;
	// variable to access the BluetoothAdapter
	// variable to create and call functions of the SyncProxy
	private SyncProxyALM proxy = null;

	private Handler mHandler = new Handler();

	private MediaPlayer mMediaPlayer;

	private final int YES = 1000;
	private final int NO = 1001;

	private final int COMMAND_FIND_GAS_STATIONS = 10001;
	private final int COMMAND_FIND_SERVICE_STATIONS = 10002;

	private boolean RUN_IN_ALE = false;

	private int PORT = 12345;// Use this port number to connect to ALE

	// Use 10.0.2.2 to connect to local ALE from Android emulator
	// If connecting via Bluetooth, find out the IP address of the machine with
	// the ALE and enter it below.
	// Ensure that this machine can ping the device. Find the IP address of the
	// device from "Settings --> Wi-Fi --> <Selected Wi-Fi> --> IP address"
	private final String IP_ADDRESS_LOCALHOST = "192.168.254.10"; // "10.255.6.47";
	// //"127.0.0.1";
	private final String IP_ADDRESS_EMULATOR = "10.0.2.2";

	Vector<SoftButton> mainSoftButtons = new Vector<SoftButton>();
	Vector<ComponentVolumeStatus> tireStatuses = new Vector<ComponentVolumeStatus>();

	// Service shutdown timing constants
	private static final int CONNECTION_TIMEOUT = 100000;
	private static final int STOP_SERVICE_DELAY = 5000;

	private boolean lowFuelAlerted = false;

	private static final Double driverDistractionSpeed = 5.0; // mph

	private boolean pastFirstRun = false;
	private boolean initialVehicleDataReceived = false;
	private boolean showGasStationsOnDriverDistractionOff = false;

	/**
	 * Runnable that stops this service if there hasn't been a connection to
	 * SYNC within a reasonable amount of time since ACL_CONNECT.
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
	 * Runnable that stops this service on ACL_DISCONNECT after a short time
	 * delay. This is a workaround until some synchronization issues are fixed
	 * within the proxy.
	 */
	private Runnable mStopServiceRunnable = new Runnable() {
		@Override
		public void run() {
			// As long as the proxy is null or not connected to SYNC, stop the
			// service
			if (proxy == null || !proxy.getIsConnected()) {
				mHandler.removeCallbacks(mCheckConnectionRunnable);
				mHandler.removeCallbacks(mStopServiceRunnable);
				stopSelf();
			}
		}
	};

	/**************** Utility functions *****************/
	private String getStr(int resourceId) {
		if (AppLinkApplication.getCurrentActivity() != null) {
			return AppLinkApplication.getCurrentActivity().getBaseContext()
					.getString(resourceId);
		} else {
			return "";
		}
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
		// Remove any previous stop service runnables that could be from a
		// recent ACL Disconnect
		mHandler.removeCallbacks(mStopServiceRunnable);

		// Start the proxy when the service starts
		if (proxy != null) {
			disposeSyncProxy();
		}
		startProxy();

		// Queue the check connection runnable to stop the service if no
		// connection is made
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
	 * Queue's a runnable that stops the service after a small delay, unless the
	 * proxy manages to reconnects to SYNC.
	 */
	public void stopService() {
		mHandler.removeCallbacks(mStopServiceRunnable);
		mHandler.postDelayed(mStopServiceRunnable, STOP_SERVICE_DELAY);
	}

	public void startProxy() {
		if (proxy == null) {
			try {
				// Phone or Android Emulator running against ALE
				if ((isAppRunningInEmulator() && RUN_IN_ALE)
						|| (!isAppRunningInEmulator() && RUN_IN_ALE)) {
					proxy = new SyncProxyALM(this, getStr(R.string.app_name),
							false, String.valueOf(PORT),
							new TCPTransportConfig(PORT, getIP(), false));
				}
				// Phone running against TDK
				else if (!isAppRunningInEmulator()) {
					proxy = new SyncProxyALM(this, getStr(R.string.app_name),
							true, getStr(R.string.ford_app_id));
				} else {
					Toast.makeText(
							AppLinkApplication.getCurrentActivity()
									.getBaseContext(),
							"App is running in an emulator and not connecting to the ALE.\nThis doesn't make sense",
							Toast.LENGTH_LONG).show();
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
				// something goes wrong, & the proxy returns as null, stop
				// service, do not want a running service with a null proxy
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
			/*
			 * proxy.subscribeButton(ButtonName.SEEKLEFT, autoIncCorrId++);
			 * proxy.subscribeButton(ButtonName.SEEKRIGHT, autoIncCorrId++);
			 * proxy.subscribeButton(ButtonName.TUNEUP, autoIncCorrId++);
			 * proxy.subscribeButton(ButtonName.TUNEDOWN, autoIncCorrId++);
			 * proxy.subscribeButton(ButtonName.PRESET_1, autoIncCorrId++);
			 * proxy.subscribeButton(ButtonName.PRESET_2, autoIncCorrId++);
			 * proxy.subscribeButton(ButtonName.PRESET_3, autoIncCorrId++);
			 * proxy.subscribeButton(ButtonName.PRESET_4, autoIncCorrId++);
			 * proxy.subscribeButton(ButtonName.PRESET_5, autoIncCorrId++);
			 * proxy.subscribeButton(ButtonName.PRESET_6, autoIncCorrId++);
			 * proxy.subscribeButton(ButtonName.PRESET_7, autoIncCorrId++);
			 * proxy.subscribeButton(ButtonName.PRESET_8, autoIncCorrId++);
			 * proxy.subscribeButton(ButtonName.PRESET_9, autoIncCorrId++);
			 * proxy.subscribeButton(ButtonName.PRESET_0, autoIncCorrId++);
			 */
		} catch (SyncException e) {
		}
	}

	private void playAudio(int audioId, final String message) {
		mMediaPlayer = MediaPlayer.create(AppLinkApplication
				.getCurrentActivity().getBaseContext(), audioId);
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				try {
					proxy.speak(message, autoIncCorrId++);
				} catch (SyncException e) {
					e.printStackTrace();
				}
			}

		});
		mMediaPlayer.start();
	}

	private void playAudio() {
		mMediaPlayer = MediaPlayer.create(AppLinkApplication
				.getCurrentActivity().getBaseContext(), R.raw.jingle);
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				try {
					proxy.speak(getStr(R.string.welcome_message),
							autoIncCorrId++);
				} catch (SyncException e) {
					e.printStackTrace();
				}
			}

		});
		mMediaPlayer.start();
	}

	@Override
	public void onProxyClosed(String info, Exception e) {
		LockScreenManager.setHMILevelState(null);
		LockScreenManager.clearLockScreen();

		if ((((SyncException) e).getSyncExceptionCause() != SyncExceptionCause.SYNC_PROXY_CYCLED)) {
			if (((SyncException) e).getSyncExceptionCause() != SyncExceptionCause.BLUETOOTH_DISABLED) {
				Log.v(AppLinkApplication.TAG, "reset proxy in onproxy closed");
				reset();
			}
		}
	}

	private void callGetVehicleData() {
		try {
			proxy.getvehicledata(Constants.GPS_DATA, Constants.SPEED_DATA,
					Constants.RPM_DATA, Constants.FUEL_LEVEL_DATA,
					Constants.FUEL_LEVEL_STATE_DATA,
					Constants.INSTANT_FUEL_CONSUMPTION_DATA,
					Constants.EXTERNAL_TEMP_DATA, Constants.VIN_DATA,
					Constants.PRNDL_DATA, Constants.TIRE_PRESSURE_DATA,
					Constants.ODOMETER_DATA, Constants.BELT_STATUS_DATA,
					Constants.BODY_INFORMATION_DATA,
					Constants.DEVICE_STATUS_DATA,
					Constants.DRIVER_BRAKING_DATA, Constants.WIPER_STATUS_DATA,
					Constants.HEADLAMP_STATUS_DATA,
					Constants.ENGINE_TORQUE_DATA,
					Constants.ACC_PEDAL_POSITION_DATA,
					Constants.STEERING_WHEEL_ANGLE_DATA,
					Constants.ECALL_INFO_DATA, Constants.AIRBAG_STATUS_DATA,
					Constants.EMERGENCY_EVENT_DATA,
					Constants.CLUSTER_MODE_STATUS_DATA, Constants.MY_KEY_DATA,
					autoIncCorrId++);
		} catch (SyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onOnHMIStatus(OnHMIStatus notification) {
		/*
		 * switch(notification.getSystemContext()) { case SYSCTXT_MAIN: break;
		 * case SYSCTXT_VRSESSION: break; case SYSCTXT_MENU: break; default:
		 * return; } AudioStreamingState state =
		 * notification.getAudioStreamingState(); switch(state) { case AUDIBLE:
		 * // play audio if applicable break; case NOT_AUDIBLE: //
		 * pause/stop/mute audio if applicable break; default: return; }
		 */
		LockScreenManager.setHMILevelState(notification.getHmiLevel());
		// LockScreenManager.updateLockScreen();

		switch (notification.getHmiLevel()) {
		case HMI_FULL:
			Log.i(AppLinkApplication.TAG, "HMI_FULL");
			if (notification.getFirstRun()) {
				// setup app on SYNC
				// send welcome message if applicable
				try {
					playAudio();
					if (RUN_IN_ALE && !initialVehicleDataReceived) {
						callGetVehicleData();
						pastFirstRun = true;
					}
					proxy.show(getStr(R.string.welcome_screen_message_1),
							getStr(R.string.welcome_screen_message_2),
							TextAlignment.CENTERED, autoIncCorrId++);
				} catch (SyncException e) {
					DebugTool.logError("Failed to send Show", e);
				}
				// send addcommands
				// subscribe to buttons
				addCommands();
				subButtons();
			} else {
				try {
					if (!pastFirstRun) {
						pastFirstRun = true;
						proxy.show("Retrieving vehicle", "data",
								TextAlignment.CENTERED, autoIncCorrId++);
						callGetVehicleData();
					}
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

	private void addCommands() {
		AddCommand msg = new AddCommand();
		msg.setCmdID(COMMAND_FIND_GAS_STATIONS);

		String menuName = "Gas Stations";
		MenuParams menuParams = new MenuParams();
		menuParams.setMenuName(menuName);
		msg.setMenuParams(menuParams);

		// Build voice recognition commands
		Vector<String> vrCommands = new Vector<String>();
		vrCommands.add("Gas stations");
		vrCommands.add("Find gas stations");
		vrCommands.add("Gas station");
		msg.setVrCommands(vrCommands);

		// Set the correlation ID
		int correlationId = autoIncCorrId++;
		msg.setCorrelationID(correlationId);

		AddCommand msg2 = new AddCommand();
		msg2.setCmdID(COMMAND_FIND_SERVICE_STATIONS);

		String menuName2 = "Gas Stations";
		MenuParams menuParams2 = new MenuParams();
		menuParams2.setMenuName(menuName2);
		msg2.setMenuParams(menuParams2);

		// Build voice recognition commands
		Vector<String> vrCommands2 = new Vector<String>();
		vrCommands2.add("Service stations");
		vrCommands2.add("Find service stations");
		vrCommands2.add("Service station");
		msg2.setVrCommands(vrCommands2);

		// Set the correlation ID
		int correlationId2 = autoIncCorrId++;
		msg2.setCorrelationID(correlationId2);

		// Send to proxy
		try {
			proxy.sendRPCRequest(msg);
			proxy.sendRPCRequest(msg2);
		} catch (SyncException e) {
			Log.i(AppLinkApplication.TAG,
					"sync exception" + e.getMessage()
							+ e.getSyncExceptionCause());
			e.printStackTrace();
		}
	}

	@Override
	public void onOnCommand(OnCommand notification) {
		// Get identifier for the command
		int cmdID = notification.getCmdID();

		// Determine which command was selected
		if (cmdID == COMMAND_FIND_GAS_STATIONS) {
			showGasStationList();
		} else if (cmdID == COMMAND_FIND_SERVICE_STATIONS) {
			try {
				proxy.speak("You asked to find service stations", autoIncCorrId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void showGasStationList() {
		LockScreenManager
				.setDriverDistractionState(DriverDistractionState.DD_OFF);
		LockScreenManager.updateLockScreen();
		GasStationHelper.GetGasStations(getApplicationContext(),
				LocationHelper.getGPSData(getApplicationContext()), false);
	}

	private void callSubscribeVehicleData() {
		try {
			proxy.subscribevehicledata(Constants.GPS_DATA,
					Constants.SPEED_DATA, Constants.RPM_DATA,
					Constants.FUEL_LEVEL_DATA, Constants.FUEL_LEVEL_STATE_DATA,
					Constants.INSTANT_FUEL_CONSUMPTION_DATA,
					Constants.EXTERNAL_TEMP_DATA, Constants.PRNDL_DATA,
					Constants.TIRE_PRESSURE_DATA, Constants.ODOMETER_DATA,
					Constants.BELT_STATUS_DATA,
					Constants.BODY_INFORMATION_DATA,
					Constants.DEVICE_STATUS_DATA,
					Constants.DRIVER_BRAKING_DATA, Constants.WIPER_STATUS_DATA,
					Constants.WIPER_STATUS_DATA, Constants.ENGINE_TORQUE_DATA,
					Constants.ACC_PEDAL_POSITION_DATA,
					Constants.STEERING_WHEEL_ANGLE_DATA,
					Constants.ECALL_INFO_DATA, Constants.AIRBAG_STATUS_DATA,
					Constants.EMERGENCY_EVENT_DATA,
					Constants.CLUSTER_MODE_STATUS_DATA, Constants.MY_KEY_DATA,
					autoIncCorrId++);
		} catch (SyncException e) {
			e.printStackTrace();
		}
	}

	private void callUnSubscribeVehicleData() {
		try {
			proxy.unsubscribevehicledata(Constants.GPS_DATA,
					Constants.SPEED_DATA, Constants.RPM_DATA,
					Constants.FUEL_LEVEL_DATA, Constants.FUEL_LEVEL_STATE_DATA,
					Constants.INSTANT_FUEL_CONSUMPTION_DATA,
					Constants.EXTERNAL_TEMP_DATA, Constants.PRNDL_DATA,
					Constants.TIRE_PRESSURE_DATA, Constants.ODOMETER_DATA,
					Constants.BELT_STATUS_DATA,
					Constants.BODY_INFORMATION_DATA,
					Constants.DEVICE_STATUS_DATA,
					Constants.DRIVER_BRAKING_DATA, Constants.WIPER_STATUS_DATA,
					Constants.WIPER_STATUS_DATA, Constants.ENGINE_TORQUE_DATA,
					Constants.ACC_PEDAL_POSITION_DATA,
					Constants.STEERING_WHEEL_ANGLE_DATA,
					Constants.ECALL_INFO_DATA, Constants.AIRBAG_STATUS_DATA,
					Constants.EMERGENCY_EVENT_DATA,
					Constants.CLUSTER_MODE_STATUS_DATA, Constants.MY_KEY_DATA,
					autoIncCorrId++);
		} catch (SyncException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onOnDriverDistraction(OnDriverDistraction notification) {
		DriverDistractionState driverDistractionState = notification.getState();
		LockScreenManager.setDriverDistractionState(driverDistractionState);
		LockScreenManager.updateLockScreen();
		if (!pastFirstRun)
			return;
		try {
			if (driverDistractionState == DriverDistractionState.DD_OFF
					&& proxy != null) {
				callUnSubscribeVehicleData();
				proxy.show("iAlert", "Monitoring: OFF", TextAlignment.CENTERED,
						autoIncCorrId++);
				if (showGasStationsOnDriverDistractionOff) {
					GasStationHelper.GetGasStations(getApplicationContext(),
							LocationHelper.getGPSData(getApplicationContext()),
							false);
				}
			} else if (driverDistractionState == DriverDistractionState.DD_ON
					&& proxy != null) {
				callSubscribeVehicleData();
				proxy.show("iAlert", "Monitoring: ON", TextAlignment.CENTERED,
						autoIncCorrId++);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void onOnButtonEvent(OnButtonEvent notification) {
		if (notification.getButtonEventMode().equals(ButtonEventMode.BUTTONUP)) {
			switch (notification.getButtonName()) {
			case CUSTOM_BUTTON:
				switch (notification.getCustomButtonID()) {
				case YES:
					break;
				case NO:

					break;
				default:
				}
				break;
			default:
			}
		}
	}

	@Override
	public void onGetVehicleDataResponse(GetVehicleDataResponse response) {
		log(response);
		Double accPedalPosition = response.getAccPedalPosition();
		AirbagStatus airbagStatus = response.getAirbagStatus();
		BeltStatus beltStatus = response.getBeltStatus();
		BodyInformation bodyInformation = response.getBodyInformation();
		DeviceStatus deviceStatus = response.getDeviceStatus();
		VehicleDataEventStatus vehicleDataEventStatus = response
				.getDriverBraking();
		ECallInfo eCallInfo = response.getECallInfo();
		EmergencyEvent emergencyEvent = response.getEmergencyEvent();
		Double engineTorque = response.getEngineTorque();
		Double externalTemperature = response.getExternalTemperature();
		Double fuelLevel = response.getFuelLevel();
		ComponentVolumeStatus fuelStatus = response.getFuelLevel_State();
		GPSData gpsData = response.getGps();
		HeadLampStatus headLampStatus = response.getHeadLampStatus();
		Double instantFuelConsumption = response.getInstantFuelConsumption();
		MyKey myKey = response.getMyKey();
		Integer odometer = response.getOdometer();
		PRNDL prndl = response.getPrndl();
		Integer rpm = response.getRpm();
		Double speed = 0.0;
		try {
			speed = response.getSpeed();
		} catch (Exception ex) {
			Log.d(AppLinkApplication.TAG, "Exception occured: " + ex);
		}
		Double steeringWheelAngle = response.getSteeringWheelAngle();
		TireStatus tireStatus = response.getTirePressure();
		String vin = response.getVin();
		WiperStatus wiperStatus = response.getWiperStatus();

		VehicleReportData data = new VehicleReportData();
		data.setGpsData(gpsData);
		data.setTireStatus(tireStatus);
		data.setFuelLevel(fuelLevel);
		data.setFuelStatus(fuelStatus);
		data.setAirbagStatus(airbagStatus);
		data.setOdometer(odometer);
		data.setVin(vin);
		data.setDeviceStatus(deviceStatus);

		if (speed != null && speed > driverDistractionSpeed) {
			try {
				LockScreenManager
						.setDriverDistractionState(DriverDistractionState.DD_ON);
				LockScreenManager.updateLockScreen();
				proxy.show("iAlert", "Monitoring: ON", TextAlignment.CENTERED,
						autoIncCorrId++);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			DashboardActivity dashboardActivity = (DashboardActivity) AppLinkApplication
					.getCurrentActivity();
			dashboardActivity.DisplayVehicleData(data);
			try {
				proxy.show("iAlert", "Vehicle data received",
						TextAlignment.CENTERED, autoIncCorrId++);
			} catch (SyncException e) {
				e.printStackTrace();
			}
		}
		initialVehicleDataReceived = true;
	}

	@Override
	public void onSubscribeVehicleDataResponse(
			SubscribeVehicleDataResponse response) {
		log(response);
	}

	@Override
	public void onUnsubscribeVehicleDataResponse(
			UnsubscribeVehicleDataResponse response) {
		log(response);
	}

	@Override
	public void onOnVehicleData(OnVehicleData notification) {
		Double acceleration = notification.getAcceleration();
		Double acceleratorPedalPosition = notification.getAccPedalPosition();
		AirbagStatus airbagStatus = notification.getAirbagStatus();
		BeltStatus beltStatus = notification.getBeltStatus();
		BodyInformation bodyInformation = notification.getBodyInformation();
		DeviceStatus deviceStatus = notification.getDeviceStatus();
		VehicleDataEventStatus vehicleDataEventStatus = notification
				.getDriverBraking();
		ECallInfo eCallInfo = notification.getECallInfo();
		EmergencyEvent emergencyEvent = notification.getEmergencyEvent();
		Double engineTorque = notification.getEngineTorque();
		Double externalTemperature = notification.getExternalTemperature();
		Double fuelLevel = notification.getFuelLevel();
		ComponentVolumeStatus fuelStatus = notification.getFuelLevel_State();
		GPSData gpsData = notification.getGps();
		HeadLampStatus headLampStatus = notification.getHeadLampStatus();
		Double instantFuelConsumption = notification
				.getInstantFuelConsumption();
		MyKey myKey = notification.getMyKey();
		Integer odometer = notification.getOdometer();
		PRNDL prndl = notification.getPrndl();
		Integer rpm = notification.getRpm();
		Double speed = notification.getSpeed();
		Double steeringWheelAngle = notification.getSteeringWheelAngle();
		TireStatus tirePressure = notification.getTirePressure();
		String vin = notification.getVin();
		WiperStatus wiperStatus = notification.getWiperStatus();

		VehicleReportData data = new VehicleReportData();
		data.setGpsData(gpsData);
		data.setTireStatus(tirePressure);
		data.setFuelLevel(fuelLevel);
		data.setFuelStatus(fuelStatus);
		data.setAirbagStatus(airbagStatus);
		data.setOdometer(odometer);
		data.setVin(vin);

		if (VehicleDataHelper.HasLowFuel(data)
				|| VehicleDataHelper.HasLowFuelStatus(data)) {
			if (!lowFuelAlerted) {
				try {
					/*
					 * playAudio(R.raw.alarm,
					 * "The fuel level in the car is low!");
					 */
					createLowFuelChoiceSet();
					createLowFuelInteraction();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				lowFuelAlerted = true;
			}
		}
	}

	/******** CHOICE SETS & INTERACTIONS *****/
	public void createLowFuelInteraction() {
		/******
		 * Prerequisite CreateInteractionChoiceSet Occurs Prior to
		 * PerformInteraction
		 *******/
		// Build Request and send to proxy object:
		int corrId = 1000;
		PerformInteraction msg = new PerformInteraction();
		msg.setInteractionMode(InteractionMode.VR_ONLY);
		msg.setCorrelationID(corrId);
		msg.setInitialText("Find gas station?");
		msg.setInitialPrompt(getTTSChunksFromString("iAlert: We have detected that the vehicle has low fuel. Would you like to find the closest gas station?"));
		msg.setInteractionMode(InteractionMode.BOTH);

		Vector<Integer> choiceSetIDs = new Vector<Integer>();
		choiceSetIDs.add(101); // match the ID used in
								// CreateInteractionChoiceSet
		msg.setInteractionChoiceSetIDList(choiceSetIDs);

		msg.setHelpPrompt(getTTSChunksFromString("Help Prompt"));
		msg.setTimeoutPrompt(getTTSChunksFromString("Timeout Prompt"));
		msg.setTimeout(10000); // max 10000 milliseconds

		Vector<VrHelpItem> vrHelpItems = new Vector<VrHelpItem>();
		VrHelpItem item = new VrHelpItem();
		item.setText("Help Prompt");
		item.setPosition(1);

		vrHelpItems.add(item);
		msg.setVrHelp(vrHelpItems);

		try {
			proxy.sendRPCRequest(msg);
		} catch (SyncException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPerformInteractionResponse(PerformInteractionResponse response) {
		log(response);

		if (response.getCorrelationID() == 1000) {
			switch (response.getChoiceID()) {
			case 99:
				Log.d(AppLinkApplication.TAG, "Case 99");
				/*
				 * GasStationHelper.GetGasStations(getApplicationContext(),
				 * LocationHelper.getGPSData(getApplicationContext()));
				 */
				showGasStationsOnDriverDistractionOff = true;
				try {
					proxy.speak(
							"Please pull over when possible. Your phone will then display the closest gas stations around you.",
							autoIncCorrId++);
					proxy.show("Low fuel level", "Pull over when possible",
							TextAlignment.CENTERED, autoIncCorrId++);
				} catch (SyncException e) {
					Log.d(AppLinkApplication.TAG, "Sync exception", e);
					e.printStackTrace();
				}
				break;
			case 100:
				Log.d(AppLinkApplication.TAG, "Case 100");
				break;
			default:
				return;
			}
		}

	}

	public void createLowFuelChoiceSet() {
		int corrId = autoIncCorrId++;

		String message = "iAlert, would you like us to find the closest gas station?";

		Vector<Choice> commands = new Vector<Choice>();
		Choice one = new Choice();
		one.setChoiceID(99);
		one.setMenuName("Yes");
		Vector<String> vrCommands = new Vector<String>();
		vrCommands.add("Yes");
		vrCommands.add("Yeah");
		vrCommands.add("Yep");
		one.setVrCommands(vrCommands);
		commands.add(one);

		Choice two = new Choice();
		two.setChoiceID(100);
		two.setMenuName("No");
		Vector<String> vrCommands2 = new Vector<String>();
		vrCommands2.add("No");
		vrCommands2.add("Nopes");
		vrCommands2.add("Nah");
		two.setVrCommands(vrCommands2);
		commands.add(two);

		// Build Request and send to proxy object:
		CreateInteractionChoiceSet msg = new CreateInteractionChoiceSet();
		msg.setCorrelationID(corrId);
		int choiceSetID = 101;
		msg.setInteractionChoiceSetID(choiceSetID);
		msg.setChoiceSet(commands);

		try {
			proxy.speak(message, autoIncCorrId++);
			proxy.sendRPCRequest(msg);
		} catch (SyncException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCreateInteractionChoiceSetResponse(
			CreateInteractionChoiceSetResponse response) {
		log(response);
		boolean bSuccess = response.getSuccess();

		if (!bSuccess) {
			// handle error
		}

		// associate correlation ID with request
		Result theResult = response.getResultCode();

		if (theResult != Result.SUCCESS) {
			// handle error
		}
	}

	/***** END CHOICE SETS & INTERACTIONS *****/

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

	@Override
	public void onShowResponse(ShowResponse response) {
		log(response);
	}

	@Override
	public void onSpeakResponse(SpeakResponse response) {
		log(response);
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(AppLinkApplication.TAG, "OnBind has been called");
		return null;
	}

	private void log(RPCResponse response) {
		Log.i(AppLinkApplication.TAG,
				"Function name = " + response.getFunctionName());
		Log.i(AppLinkApplication.TAG,
				"Message Type = " + response.getMessageType());
		Log.i(AppLinkApplication.TAG, "Success = "
				+ response.getSuccess().toString());
		Log.i(AppLinkApplication.TAG, "Info = " + response.getInfo());
		Log.i(AppLinkApplication.TAG, "Result code = "
				+ response.getResultCode().name());
	}

	/******************* Unused handlers **********************/

	@Override
	public void onError(String info, Exception e) {
	}

	@Override
	public void onGenericResponse(GenericResponse response) {
		log(response);
	}

	@Override
	public void onAddCommandResponse(AddCommandResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onAddSubMenuResponse(AddSubMenuResponse response) {
		// TODO Auto-generated method stub
	}

	private Vector<TTSChunk> getTTSChunksFromString(String str) {
		Vector<TTSChunk> chunks = new Vector<TTSChunk>(1);
		chunks.add(TTSChunkFactory.createChunk(SpeechCapabilities.TEXT, str));
		return chunks;
	}

	@Override
	public void onAlertResponse(AlertResponse response) {
		log(response);
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
	public void onResetGlobalPropertiesResponse(
			ResetGlobalPropertiesResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSetGlobalPropertiesResponse(
			SetGlobalPropertiesResponse response) {
	}

	@Override
	public void onSetMediaClockTimerResponse(SetMediaClockTimerResponse response) {
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
	public void onReadDIDResponse(ReadDIDResponse response) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGetDTCsResponse(GetDTCsResponse response) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPerformAudioPassThruResponse(
			PerformAudioPassThruResponse response) {
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
		log(response);
	}

	@Override
	public void onSetDisplayLayoutResponse(SetDisplayLayoutResponse response) {

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
