package core.packets;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import common.utils.Utils;

public class DeviceInformationDIBTest {

	private static final Logger LOG = LogManager.getLogger(DeviceInformationDIBTest.class);

	@Test
	public void testGetBytes() {

		final DeviceInformationDIB deviceInformationDIB = retrieveDeviceInformationDIB();

		final byte[] bytes = deviceInformationDIB.getBytes();

		LOG.info(Utils.integerToStringNoPrefix(bytes));

		final byte[] hexStringToByteArray = Utils.hexStringToByteArray(
				"360102001101000000c50102d84ce000170c00246d01d80a4b4e582049502042414f5320373737000000000000000000000000000000");

		LOG.info(Utils.integerToStringNoPrefix(hexStringToByteArray));

		assertTrue(java.util.Objects.deepEquals(bytes, hexStringToByteArray));
	}

	/**
	 * device info DescriptionInformationBlock (DIB)
	 */
	private DeviceInformationDIB retrieveDeviceInformationDIB() {

		final DeviceInformationDIB deviceInformationDIB = new DeviceInformationDIB();
		deviceInformationDIB.setDeviceStatus(DeviceStatus.NORMAL_MODE);
		deviceInformationDIB.setIndividualAddress(0x1101);
		deviceInformationDIB.setMedium(KNXMedium.TP1);
		deviceInformationDIB.setProjectInstallationIdentifier(0);

		// serial number
		System.arraycopy(new byte[] { (byte) 0x00, (byte) 0xC5, (byte) 0x01, (byte) 0x02, (byte) 0xD8, (byte) 0x4C }, 0,
				deviceInformationDIB.getDeviceSerialNumber(), 0, deviceInformationDIB.getDeviceSerialNumber().length);

		// multicast address - 224.0.23.12
		System.arraycopy(new byte[] { (byte) 0xE0, (byte) 0x00, (byte) 0x17, (byte) 0x0C }, 0,
				deviceInformationDIB.getDeviceRoutingMulticastAddress(), 0,
				deviceInformationDIB.getDeviceRoutingMulticastAddress().length);

		// mac address
		System.arraycopy(new byte[] { (byte) 0x00, (byte) 0x24, (byte) 0x6D, (byte) 0x01, (byte) 0xD8, (byte) 0x0A }, 0,
				deviceInformationDIB.getDeviceMacAddress(), 0, deviceInformationDIB.getDeviceMacAddress().length);

		// friendly name
		final String friendlyName = "KNX IP BAOS 777";
		final byte[] friendlyNameAsByteArray = friendlyName.getBytes(StandardCharsets.US_ASCII);
		System.arraycopy(friendlyNameAsByteArray, 0, deviceInformationDIB.getDeviceFriendlyName(), 0,
				friendlyNameAsByteArray.length);

		deviceInformationDIB.setLength(54);

		return deviceInformationDIB;
	}

}
