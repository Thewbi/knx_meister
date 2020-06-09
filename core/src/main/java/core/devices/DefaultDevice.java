package core.devices;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import core.api.device.Device;
import core.packets.DeviceStatus;

public class DefaultDevice implements Device {

	private int hostPhysicalAddress;

	private int physicalAddress;

	private final Map<Short, Short> properties = new ConcurrentHashMap<>();

	private DeviceStatus deviceStatus = DeviceStatus.NORMAL_MODE;

	public DefaultDevice() {
		properties.put((short) 56, (short) 0x0037);
		properties.put((short) 83, (short) 0x07B0);

		// key 83
//		responseData[0] = (byte) 0x07;
//		responseData[1] = (byte) 0xB0;

		// key 56
//		responseData[0] = (byte) 0x00;
//		responseData[1] = (byte) 0x37;
	}

	@Override
	public Map<Short, Short> getProperties() {
		return properties;
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

	public int getPhysicalAddress() {
		return physicalAddress;
	}

	public void setPhysicalAddress(final int physicalAddress) {
		this.physicalAddress = physicalAddress;
	}

}
