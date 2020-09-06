package api.device;

import java.util.Map;

import api.packets.PropertyId;
import api.project.KNXComObject;
import api.project.KNXGroupAddress;

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

    Map<String, KNXGroupAddress> getDeviceProperties();

    Map<String, KNXComObject> getComObjects();

//	Map<Integer, KNXComObject> getComObjectsByDatapointType();

    Map<Integer, KNXComObject> getComObjectsByDatapointId();

//	// DEBUG
//	void setValue(int value);
//
//	int getValue();

}
