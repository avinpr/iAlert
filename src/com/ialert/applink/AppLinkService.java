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
import com.ford.syncV4.proxy.SyncProxyALM;
import com.ford.syncV4.proxy.TTSChunkFactory;
import com.ford.syncV4.proxy.interfaces.IProxyListenerALM;
import com.ford.syncV4.proxy.rpc.AddCommandResponse;
import com.ford.syncV4.proxy.rpc.AddSubMenuResponse;
import com.ford.syncV4.proxy.rpc.AirbagStatus;
import com.ford.syncV4.proxy.rpc.AlertResponse;
import com.ford.syncV4.proxy.rpc.ChangeRegistrationResponse;
import com.ford.syncV4.proxy.rpc.Choice;
import com.ford.syncV4.proxy.rpc.CreateInteractionChoiceSet;
import com.ford.syncV4.proxy.rpc.CreateInteractionChoiceSetResponse;
import com.ford.syncV4.proxy.rpc.DeleteCommandResponse;
import com.ford.syncV4.proxy.rpc.DeleteFileResponse;
import com.ford.syncV4.proxy.rpc.DeleteInteractionChoiceSetResponse;
import com.ford.syncV4.proxy.rpc.DeleteSubMenuResponse;
import com.ford.syncV4.proxy.rpc.EncodedSyncPDataResponse;
import com.ford.syncV4.proxy.rpc.EndAudioPassThruResponse;
import com.ford.syncV4.proxy.rpc.GPSData;
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
import com.ford.syncV4.proxy.rpc.VehicleDataResult;
import com.ford.syncV4.proxy.rpc.VrHelpItem;
import com.ford.syncV4.proxy.rpc.enums.ButtonEventMode;
import com.ford.syncV4.proxy.rpc.enums.ButtonName;
import com.ford.syncV4.proxy.rpc.enums.ComponentVolumeStatus;
import com.ford.syncV4.proxy.rpc.enums.InteractionMode;
import com.ford.syncV4.proxy.rpc.enums.Result;
import com.ford.syncV4.proxy.rpc.enums.SoftButtonType;
import com.ford.syncV4.proxy.rpc.enums.SpeechCapabilities;
import com.ford.syncV4.proxy.rpc.enums.SystemAction;
import com.ford.syncV4.proxy.rpc.enums.TextAlignment;
import com.ford.syncV4.transport.TCPTransportConfig;
import com.ford.syncV4.util.DebugTool;
import com.ialert.R;
import com.ialert.activity.DashboardActivity;
import com.ialert.activity.VehicleReportData;

public class AppLinkService extends Service implements IProxyListenerALM {
	// variable used to increment correlation ID for every request sent to SYNC
	public int autoIncCorrId = 0;
	// variable to contain the current state of the service
	private static AppLinkService instance = null;
	// variable to access the BluetoothAdapter
	// variable to create and call functions of the SyncProxy
	private SyncProxyALM proxy = null;

	private MediaPlayer mMediaPlayer;

	private final int YES = 1000;
	private final int NO = 1001;

	private boolean RUN_IN_ALE = false;

	private int PORT = 12345;// Use this port number to connect to ALE

	// Use 10.0.2.2 to connect to local ALE from Android emulator
	// If connecting via Bluetooth, find out the IP address of the machine with
	// the ALE and enter it below.
	// Ensure that this machine can ping the device. Find the IP address of the
	// device from "Settings --> Wi-Fi --> <Selected Wi-Fi> --> IP address"
	private final String IP_ADDRESS_LOCALHOST = "192.168.254.19"; // "10.255.6.47";
	// //"127.0.0.1";
	private final String IP_ADDRESS_EMULATOR = "10.0.2.2";

	Vector<SoftButton> mainSoftButtons = new Vector<SoftButton>();
	Vector<ComponentVolumeStatus> tireStatuses = new Vector<ComponentVolumeStatus>();

	// Service shutdown timing constants
	private static final int CONNECTION_TIMEOUT = 60000;
	private static final int STOP_SERVICE_DELAY = 5000;

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
		// Remove any previous stop service runnables that could be from a
		// recent ACL Disconnect
		mHandler.removeCallbacks(mStopServiceRunnable);

		// Start the proxy when the service starts
		if (intent != null) {
			startProxy();
		}

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

	public void sampleCreateChoiceSet() {
		int corrId = autoIncCorrId++;

		Vector<Choice> commands = new Vector<Choice>();
		Choice one = new Choice();
		one.setChoiceID(99);
		one.setMenuName("Choice One");
		Vector<String> vrCommands = new Vector<String>();
		vrCommands.add("Choice 1");
		vrCommands.add("Choice 1 Alias 1");
		vrCommands.add("Choice 1 Alias 2");
		one.setVrCommands(vrCommands);

		// Adding images is causing a failure when sending the rpc request.
		// Logs of ALE says "INVALID_DATA"

		// icon shown to the left on a choice set item
		/*
		 * Image image = new Image(); image.setImageType(ImageType.STATIC);
		 * image.setValue("0x89"); one.setImage(image);
		 */
		commands.add(one);

		Choice two = new Choice();
		two.setChoiceID(100);
		two.setMenuName("Choice Two");
		Vector<String> vrCommands2 = new Vector<String>();
		vrCommands2.add("Choice 2");
		vrCommands2.add("Choice 2 Alias 1");
		vrCommands2.add("Choice 2 Alias 2");
		two.setVrCommands(vrCommands2);

		/*
		 * Image image2 = new Image(); image2.setImageType(ImageType.STATIC);
		 * image2.setValue("0x89"); two.setImage(image2);
		 */
		commands.add(two);

		// Build Request and send to proxy object:
		CreateInteractionChoiceSet msg = new CreateInteractionChoiceSet();
		msg.setCorrelationID(corrId);
		int choiceSetID = 101;
		msg.setInteractionChoiceSetID(choiceSetID);
		msg.setChoiceSet(commands);

		try {
			proxy.sendRPCRequest(msg);
		} catch (SyncException e) {
			e.printStackTrace();
		}
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
					proxy.show(getStr(R.string.welcome_screen_message_1),
							getStr(R.string.welcome_screen_message_2),
							TextAlignment.CENTERED, autoIncCorrId++);
					proxy.getvehicledata(true, false, false, true, true, false,
							false, true, false, true, true, false, false,
							false, false, false, false, false, false, false,
							false, true, false, false, false, autoIncCorrId++);
					proxy.subscribevehicledata(true, false, false, true, true,
							true, false, false, true, true, true, false, false,
							false, false, false, false, false, false, false,
							true, false, false, false, autoIncCorrId++);
				} catch (SyncException e) {
					DebugTool.logError("Failed to send Show", e);
				}
				// send addcommands
				// subscribe to buttons
				subButtons();
			} else {
				try {
					// proxy.show("SyncProxy is", "Alive",
					// TextAlignment.CENTERED, autoIncCorrId++);
					// playAudio();
					/*
					 * proxy.speak(getStr(R.string.welcome_message),
					 * autoIncCorrId++);
					 */
					proxy.show(getStr(R.string.welcome_screen_message_1),
							getStr(R.string.welcome_screen_message_2),
							TextAlignment.CENTERED, autoIncCorrId++);
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
		GPSData gpsData = response.getGps();
		TireStatus tireStatus = response.getTirePressure();
		Double fuelLevel = response.getFuelLevel();
		ComponentVolumeStatus fuelStatus = response.getFuelLevel_State();
		AirbagStatus airbagStatus = response.getAirbagStatus();
		Integer odometer = response.getOdometer();

		VehicleReportData data = new VehicleReportData();
		data.setGpsData(gpsData);
		data.setTireStatus(tireStatus);
		data.setFuelLevel(fuelLevel);
		data.setFuelStatus(fuelStatus);
		data.setAirbagStatus(airbagStatus);
		data.setOdometer(odometer);

		DashboardActivity dashboardActivity = (DashboardActivity) AppLinkApplication
				.getCurrentActivity();
		dashboardActivity.DisplayVehicleData(data);
	}

	@Override
	public void onSubscribeVehicleDataResponse(
			SubscribeVehicleDataResponse response) {
		VehicleDataResult gpsData = response.getGps();
		// gpsData.
		/*
		 * TireStatus tireStatus = response.getTirePressure(); Double fuelLevel
		 * = response.getFuelLevel(); ComponentVolumeStatus fuelStatus =
		 * response.getFuelLevel_State(); AirbagStatus airbagStatus =
		 * response.getAirbagStatus(); Integer odometer =
		 * response.getOdometer();
		 * 
		 * VehicleReportData data = new VehicleReportData();
		 * data.setGpsData(gpsData); data.setTireStatus(tireStatus);
		 * data.setFuelLevel(fuelLevel); data.setFuelStatus(fuelStatus);
		 * data.setAirbagStatus(airbagStatus); data.setOdometer(odometer);
		 */
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
		boolean bSuccess = response.getSuccess();

		if (!bSuccess) {
			// handle error
		}

		// associate correlation ID with request
		Result theResult = response.getResultCode();

		if (theResult != Result.SUCCESS) {
			// handle error
		}
		samplePerformInteraction();
	}

	private Vector<TTSChunk> getTTSChunksFromString(String str) {
		Vector<TTSChunk> chunks = new Vector<TTSChunk>(1);
		chunks.add(TTSChunkFactory.createChunk(SpeechCapabilities.TEXT, str));
		return chunks;
	}

	public void samplePerformInteraction() {
		/******
		 * Prerequisite CreateInteractionChoiceSet Occurs Prior to
		 * PerformInteraction
		 *******/
		// Build Request and send to proxy object:
		int corrId = 1000;
		PerformInteraction msg = new PerformInteraction();
		msg.setInteractionMode(InteractionMode.VR_ONLY);
		msg.setCorrelationID(corrId);
		msg.setInitialText("My initial text");
		msg.setInitialPrompt(getTTSChunksFromString("Welcome welcome welcome"));
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

		/*
		 * Image image = new Image(); image.setValue("0x89");
		 * image.setImageType(ImageType.STATIC); item.setImage(image);
		 */

		vrHelpItems.add(item);
		msg.setVrHelp(vrHelpItems);

		try {
			proxy.sendRPCRequest(msg);
		} catch (SyncException e) {
			e.printStackTrace();
		}
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
		/*
		 * if (response.getCorrelationID() == 1000) { switch
		 * (response.getChoiceID()) { case 99: try {
		 * proxy.speak("You selected 99", autoIncCorrId++); } catch
		 * (SyncException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } break; case 100:
		 * 
		 * break; default: return; } }
		 */
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
