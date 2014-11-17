package com.ialert.utilities;

import java.util.Iterator;
import java.util.Vector;

import com.ford.syncV4.proxy.rpc.AirbagStatus;
import com.ford.syncV4.proxy.rpc.DeviceStatus;
import com.ford.syncV4.proxy.rpc.enums.ComponentVolumeStatus;
import com.ford.syncV4.proxy.rpc.enums.DeviceLevelStatus;
import com.ford.syncV4.proxy.rpc.enums.VehicleDataEventStatus;
import com.ialert.activity.Constants;
import com.ialert.activity.VehicleReportData;

public class VehicleDataHelper {

	public static final String ALERT_STATUS = "AT FAULT";
	public static final String LOW_STATUS = "LOW";
	public static final String NORMAL_STATUS = "NORMAL";
	public static final String UNKNOWN = "UNKNOWN";
	public static final Double ACCEPTABLE_FUEL_LEVEL = 10.0;

	public static String GetAirbagStatus(VehicleReportData data) {
		if (data == null || data.getAirbagStatus() == null)
			return UNKNOWN;
		AirbagStatus status = data.getAirbagStatus();
		if (status.getDriverAirbagDeployed() == VehicleDataEventStatus.FAULT
				|| status.getDriverCurtainAirbagDeployed() == VehicleDataEventStatus.FAULT
				|| status.getDriverKneeAirbagDeployed() == VehicleDataEventStatus.FAULT
				|| status.getDriverSideAirbagDeployed() == VehicleDataEventStatus.FAULT
				|| status.getPassengerAirbagDeployed() == VehicleDataEventStatus.FAULT
				|| status.getPassengerCurtainAirbagDeployed() == VehicleDataEventStatus.FAULT
				|| status.getPassengerKneeAirbagDeployed() == VehicleDataEventStatus.FAULT
				|| status.getPassengerSideAirbagDeployed() == VehicleDataEventStatus.FAULT
				|| status.getDriverAirbagDeployed() == VehicleDataEventStatus.YES
				|| status.getDriverCurtainAirbagDeployed() == VehicleDataEventStatus.YES
				|| status.getDriverKneeAirbagDeployed() == VehicleDataEventStatus.YES
				|| status.getDriverSideAirbagDeployed() == VehicleDataEventStatus.YES
				|| status.getPassengerAirbagDeployed() == VehicleDataEventStatus.YES
				|| status.getPassengerCurtainAirbagDeployed() == VehicleDataEventStatus.YES
				|| status.getPassengerKneeAirbagDeployed() == VehicleDataEventStatus.YES
				|| status.getPassengerSideAirbagDeployed() == VehicleDataEventStatus.YES) {
			return ALERT_STATUS;
		} else {
			return NORMAL_STATUS;
		}
	}

	public static boolean HasBadAirbag(VehicleReportData data) {
		return GetAirbagStatus(data) == VehicleDataHelper.ALERT_STATUS;
	}

	public static boolean IsTirePressureLow(VehicleReportData data) {
		if (data == null || data.getTireStatus() == null)
			return false;

		Vector<TirePressure> lowTireStatuses = GetLowTirePressureStatuses(data);
		if (!lowTireStatuses.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	public static Vector<TirePressure> GetTirePressureStatuses(
			VehicleReportData data) {
		Vector<TirePressure> tireStatuses = new Vector<TirePressure>();

		TirePressure leftRearPressure = new TirePressure();
		leftRearPressure.setName(Constants.LEFT_REAR);
		leftRearPressure.setStatus(data.getTireStatus().getLeftRear()
				.getStatus());
		tireStatuses.add(leftRearPressure);

		TirePressure rightRearPressure = new TirePressure();
		rightRearPressure.setName(Constants.RIGHT_REAR);
		rightRearPressure.setStatus(data.getTireStatus().getRightRear()
				.getStatus());
		tireStatuses.add(rightRearPressure);

		TirePressure leftFrontPressure = new TirePressure();
		leftFrontPressure.setName(Constants.LEFT_FRONT);
		leftFrontPressure.setStatus(data.getTireStatus().getLeftFront()
				.getStatus());
		tireStatuses.add(leftFrontPressure);

		TirePressure rightFrontPressure = new TirePressure();
		rightFrontPressure.setName(Constants.RIGHT_FRONT);
		rightFrontPressure.setStatus(data.getTireStatus().getRightFront()
				.getStatus());
		tireStatuses.add(rightFrontPressure);

		return tireStatuses;
	}

	public static Vector<TirePressure> GetLowTirePressureStatuses(
			VehicleReportData data) {
		Vector<TirePressure> lowTireStatuses = new Vector<TirePressure>();
		Vector<TirePressure> tireStatuses = GetTirePressureStatuses(data);
		Iterator<TirePressure> iter = tireStatuses.iterator();
		while (iter.hasNext()) {
			TirePressure tirePressure = iter.next();
			if (tirePressure.getStatus().compareTo(ComponentVolumeStatus.ALERT) == 0
					|| tirePressure.getStatus().compareTo(
							ComponentVolumeStatus.LOW) == 0
					|| tirePressure.getStatus().compareTo(
							ComponentVolumeStatus.FAULT) == 0) {
				lowTireStatuses.add(tirePressure);
			}
		}
		return lowTireStatuses;
	}

	@SuppressWarnings("null")
	public static String GetFuelLevel(VehicleReportData data) {
		if (data == null || data.getFuelLevel() == null) {
			return UNKNOWN;
		} else {
			Double fuelLevel = data.getFuelLevel();
			if (fuelLevel <= ACCEPTABLE_FUEL_LEVEL)
				return LOW_STATUS;
			else
				return NORMAL_STATUS;
		}
	}

	public static boolean HasLowFuel(VehicleReportData data) {
		if (data == null || data.getFuelLevel() == null) {
			return false;
		} else {
			Double fuelLevel = data.getFuelLevel();
			if (fuelLevel < ACCEPTABLE_FUEL_LEVEL)
				return true;
			else
				return false;
		}
	}

	public static boolean HasLowFuelStatus(VehicleReportData data) {
		if (data == null || data.getFuelStatus() == null) {
			return false;
		} else {
			ComponentVolumeStatus fuelStatus = data.getFuelStatus();
			if (fuelStatus == ComponentVolumeStatus.ALERT
					|| fuelStatus == ComponentVolumeStatus.LOW) {
				return true;
			}
			return false;
		}
	}

	public static String GetOdometerReading(VehicleReportData data) {
		if (data == null || data.getOdometer() == null)
			return UNKNOWN;
		return data.getOdometer().toString();
	}

	public static String GetRightRearTirePressureStatus(VehicleReportData data) {
		if (data == null || data.getTireStatus() == null)
			return UNKNOWN;
		return data.getTireStatus().getRightRear().getStatus().name();
	}

	public static String GetRightFrontTirePressureStatus(VehicleReportData data) {
		if (data == null || data.getTireStatus() == null)
			return UNKNOWN;
		return data.getTireStatus().getRightFront().getStatus().name();
	}

	public static String GetLeftRearTirePressureStatus(VehicleReportData data) {
		if (data == null || data.getTireStatus() == null)
			return UNKNOWN;
		return data.getTireStatus().getLeftRear().getStatus().name();
	}

	public static String GetLeftFrontTirePressureStatus(VehicleReportData data) {
		if (data == null || data.getTireStatus() == null)
			return UNKNOWN;
		return data.getTireStatus().getLeftFront().getStatus().name();
	}

	public static String GetVin(VehicleReportData data) {
		if (data == null || data.getVin() == null || data.getVin().equals("")) {
			return UNKNOWN;
		}
		return data.getVin();
	}

	public static String GetBatteryStatus(VehicleReportData data) {
		if (data == null || data.getDeviceStatus() == null) {
			return UNKNOWN;
		}
		DeviceStatus deviceStatus = data.getDeviceStatus();
		if (deviceStatus.getBattLevelStatus() == null) {
			return UNKNOWN;
		} else {
			DeviceLevelStatus battStatus = deviceStatus.getBattLevelStatus();
			if (battStatus.compareTo(DeviceLevelStatus.ZERO_LEVEL_BARS) == 0
					|| battStatus.compareTo(DeviceLevelStatus.ONE_LEVEL_BARS) == 0) {
				return ALERT_STATUS;
			}
		}
		return NORMAL_STATUS;
	}

	public static boolean HasBadBatteryStatus(VehicleReportData data) {
		String batteryStatus = GetBatteryStatus(data);
		if (batteryStatus.equals(ALERT_STATUS)) {
			return true;
		}
		return false;
	}

	public static boolean HasAnyAlert(VehicleReportData data) {
		return IsTirePressureLow(data) || HasLowFuel(data)
				|| HasLowFuelStatus(data) || HasBadBatteryStatus(data)
				|| HasBadAirbag(data);
	}
}
