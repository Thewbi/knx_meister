package core.devices;

import api.device.DeviceProperty;

public class DefaultDeviceProperty implements DeviceProperty {

	private int groupAddress;

	public int getGroupAddress() {
		return groupAddress;
	}

	public void setGroupAddress(final int groupAddress) {
		this.groupAddress = groupAddress;
	}

}
