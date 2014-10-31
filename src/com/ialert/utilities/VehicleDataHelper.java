package com.ialert.utilities;

import java.util.Iterator;
import java.util.Vector;

import com.ford.syncV4.proxy.rpc.AirbagStatus;
import com.ford.syncV4.proxy.rpc.enums.ComponentVolumeStatus;
import com.ford.syncV4.proxy.rpc.enums.VehicleDataEventStatus;
import com.ialert.activity.VehicleReportData;

public class VehicleDataHelper {

	private static final String ALERT_STATUS = "AT FAULT";
	private static final String NORMAL_STATUS = "NORMAL";

	public static String GetAirbagStatus(AirbagStatus status) {
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
}
