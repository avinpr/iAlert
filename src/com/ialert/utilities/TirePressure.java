package com.ialert.utilities;

import com.ford.syncV4.proxy.rpc.enums.ComponentVolumeStatus;

public class TirePressure {
	private String name;
	private ComponentVolumeStatus status;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ComponentVolumeStatus getStatus() {
		return status;
	}
	public void setStatus(ComponentVolumeStatus status) {
		this.status = status;
	}
}
