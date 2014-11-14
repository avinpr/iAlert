package com.ialert.utilities;

import java.util.Iterator;
import java.util.Vector;

import com.ford.syncV4.proxy.rpc.AirbagStatus;
import com.ford.syncV4.proxy.rpc.enums.ComponentVolumeStatus;
import com.ford.syncV4.proxy.rpc.enums.VehicleDataEventStatus;
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
		if (status.getDriverAirbagDeployed().compareTo(
				VehicleDataEventStatus.FAULT) == 0
				|| status.getDriverCurtainAirbagDeployed().compareTo(
						VehicleDataEventStatus.FAULT) == 0
				|| status.getDriverKneeAirbagDeployed().compareTo(
						VehicleDataEventStatus.FAULT) == 0
				|| status.getDriverSideAirbagDeployed().compareTo(
						VehicleDataEventStatus.FAULT) == 0
				|| status.getPassengerAirbagDeployed().compareTo(
						VehicleDataEventStatus.FAULT) == 0
				|| status.getPassengerCurtainAirbagDeployed().compareTo(
						VehicleDataEventStatus.FAULT) == 0
				|| status.getPassengerKneeAirbagDeployed().compareTo(
						VehicleDataEventStatus.FAULT) == 0
				|| status.getPassengerSideAirbagDeployed().compareTo(
						VehicleDataEventStatus.FAULT) == 0) {
			return ALERT_STATUS;
		} else {
			return NORMAL_STATUS;
		}
	}

	public static Boolean IsTirePressureLow(VehicleReportData data) {
		if (data == null || data.getTireStatus() == null)
			return false;
		Vector<ComponentVolumeStatus> tireStatuses = new Vector<ComponentVolumeStatus>();
		tireStatuses.add(data.getTireStatus().getLeftRear().getStatus());
		tireStatuses.add(data.getTireStatus().getRightRear().getStatus());
		tireStatuses.add(data.getTireStatus().getRightRear().getStatus());
		tireStatuses.add(data.getTireStatus().getLeftRear().getStatus());

		boolean lowPressureIndicator = false;
		Iterator<ComponentVolumeStatus> iter = tireStatuses.iterator();
		while (iter.hasNext()) {
			ComponentVolumeStatus status = iter.next();
			if (status.compareTo(ComponentVolumeStatus.ALERT) == 0
					|| status.compareTo(ComponentVolumeStatus.LOW) == 0
					|| status.compareTo(ComponentVolumeStatus.FAULT) == 0) {
				lowPressureIndicator = true;
			}
		}
		return lowPressureIndicator;
	}

	@SuppressWarnings("null")
	public static String GetFuelStatus(VehicleReportData data) {
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
}
