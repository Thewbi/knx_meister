package core.api.device;

import java.util.Map;

import core.packets.DeviceStatus;

public interface Device {

	Map<Short, Short> getProperties();

	int getHostPhysicalAddress();

	void setHostPhysicalAddress(int hostPhysicalAddress);

	int getPhysicalAddress();

	void setPhysicalAddress(int physicalAddress);

	void setDeviceStatus(DeviceStatus deviceStatus);

	DeviceStatus getDeviceStatus();

}
