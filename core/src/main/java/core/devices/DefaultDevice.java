package core.devices;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import core.api.device.Device;

public class DefaultDevice implements Device {

	private final Map<Short, Short> properties = new ConcurrentHashMap<>();

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

}
