package core.api.device;

import java.util.Map;

import core.packets.DeviceStatus;
import core.packets.PropertyId;

public interface Device {

	Map<Short, Short> getProperties();

	int getHostPhysicalAddress();

	void setHostPhysicalAddress(int hostPhysicalAddress);

	int getPhysicalAddress();

	void setPhysicalAddress(int physicalAddress);

	void setDeviceStatus(DeviceStatus deviceStatus);

	DeviceStatus getDeviceStatus();

	boolean hasPropertyValue(PropertyId propertyId);

	short getPropertyValue(final PropertyId propertyId);

	// DEBUG
	void setValue(int value);

	int getValue();

}
