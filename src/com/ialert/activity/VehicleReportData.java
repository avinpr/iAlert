package com.ialert.activity;

import com.ford.syncV4.proxy.rpc.AirbagStatus;
import com.ford.syncV4.proxy.rpc.DeviceStatus;
import com.ford.syncV4.proxy.rpc.GPSData;
import com.ford.syncV4.proxy.rpc.TireStatus;
import com.ford.syncV4.proxy.rpc.enums.ComponentVolumeStatus;

public class VehicleReportData {
	private GPSData gpsData;
	private TireStatus tireStatus;
	private Double fuelLevel;
	private ComponentVolumeStatus fuelStatus;
	private AirbagStatus airbagStatus;
	private Integer odometer;
	private String vin;
	private DeviceStatus deviceStatus;
	
	public String getVin() {
		return vin;
	}
	public void setVin(String vin) {
		this.vin = vin;
	}
	public GPSData getGpsData() {
		return gpsData;
	}
	public void setGpsData(GPSData gpsData) {
		this.gpsData = gpsData;
	}
	public TireStatus getTireStatus() {
		return tireStatus;
	}
	public void setTireStatus(TireStatus tireStatus) {
		this.tireStatus = tireStatus;
	}
	public Double getFuelLevel() {
		return fuelLevel;
	}
	public void setFuelLevel(Double fuelLevel) {
		this.fuelLevel = fuelLevel;
	}
	public ComponentVolumeStatus getFuelStatus() {
		return fuelStatus;
	}
	public void setFuelStatus(ComponentVolumeStatus fuelStatus) {
		this.fuelStatus = fuelStatus;
	}
	public AirbagStatus getAirbagStatus() {
		return airbagStatus;
	}
	public void setAirbagStatus(AirbagStatus airbagStatus) {
		this.airbagStatus = airbagStatus;
	}
	public Integer getOdometer() {
		return odometer;
	}
	public void setOdometer(Integer odometer) {
		this.odometer = odometer;
	}
	public DeviceStatus getDeviceStatus() {
		return deviceStatus;
	}
	public void setDeviceStatus(DeviceStatus deviceStatus) {
		this.deviceStatus = deviceStatus;
	}
}
