package core.devices;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import api.device.Device;
import api.device.DeviceStatus;
import api.packets.PropertyId;
import api.project.KNXGroupAddress;

public class DefaultDevice implements Device {

	private int hostPhysicalAddress;

	private int physicalAddress;

	private final Map<Short, Short> properties = new ConcurrentHashMap<>();

	private DeviceStatus deviceStatus = DeviceStatus.NORMAL_MODE;

	private final Map<String, KNXGroupAddress> deviceProperties = new HashMap<>();

	/**
	 * ctor
	 */
	public DefaultDevice() {
		properties.put((short) PropertyId.PID_IP_CAPABILITIES.getValue(), (short) 0x0037);
		properties.put((short) PropertyId.PID_DEVICE_DESCRIPTOR.getValue(), (short) 0x07B0);
//		properties.put((short) PropertyId.PID_ADDITIONAL_INDIVIDUAL_ADDRESSES.getValue(), (short) 0x07B0);
	}

	@Override
	public Map<Short, Short> getProperties() {
		return properties;
	}

	@Override
	public boolean hasPropertyValue(final PropertyId propertyId) {
		return properties.containsKey((short) propertyId.getValue());
	}

	@Override
	public short getPropertyValue(final PropertyId propertyId) {
		return properties.get((short) propertyId.getValue());
	}

	@Override
	public int getHostPhysicalAddress() {
		return hostPhysicalAddress;
	}

	@Override
	public void setHostPhysicalAddress(final int hostPhysicalAddress) {
		this.hostPhysicalAddress = hostPhysicalAddress;
	}

	@Override
	public DeviceStatus getDeviceStatus() {
		return deviceStatus;
	}

	@Override
	public void setDeviceStatus(final DeviceStatus deviceStatus) {
		this.deviceStatus = deviceStatus;
	}

	@Override
	public int getPhysicalAddress() {
		return physicalAddress;
	}

	@Override
	public void setPhysicalAddress(final int physicalAddress) {
		this.physicalAddress = physicalAddress;
	}

	public Map<String, KNXGroupAddress> getDeviceProperties() {
		return deviceProperties;
	}

//	@Override
//	public int getValue() {
//		return value;
//	}
//
//	@Override
//	public void setValue(final int value) {
//		this.value = value;
//	}

}
