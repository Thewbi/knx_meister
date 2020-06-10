package core.packets;

import core.common.NetworkUtils;
import core.common.Utils;

/**
 * 7.5.4.2 Device information DIB
 */
public class DeviceInformationDIB extends DescriptionInformationBlock {

	/** 1 byte, KNXMedium */
	private KNXMedium medium;

	/**
	 * 1 byte, DeviceStatus The encoding of bit 0 “programming mode” shall be
	 * identical to bit 0 “progmode” in PID_PROGMODE as specified in [01].
	 */
	private DeviceStatus deviceStatus;

	private int individualAddress;

	/**
	 * The Project-Installation identifier shall solely be assigned by ETS and shall
	 * be used to uniquely identify KNXnet/IP devices in a project with more than
	 * one KNX installation, i.e. more than 15 Areas with 15 Lines, or in an
	 * environment with more than one KNX project
	 *
	 * Project number in (bit 15 - 4)
	 *
	 * Installation number (bit 3- 0)
	 */
	private int projectInstallationIdentifier;

	/**
	 * The KNXnet/IP device KNX Serial Number shall be the KNX Serial Number of the
	 * KNXnet/IP device. This information may be used to identify the device or set
	 * its Individual Address.
	 */
	private byte[] deviceSerialNumber = new byte[6];

	/**
	 * The KNXnet/IP device routing multicast address shall be the multicast address
	 * that shall be used by a KNXnet/IP Router for KNXnet/IP Routing. KNXnet/IP
	 * devices that do not implement KNXnet/IP Routing shall set this value to
	 * 0.0.0.0. This information may be used if KNXnet/IP Routing frames need to be
	 * sent to KNXnet/IP Routers that do not use the default KNXnet/IP Routing
	 * Multicast Address, which shall be equal to the KNXnet/IP System Setup
	 * Multicast Address.
	 */
	private byte[] deviceRoutingMulticastAddress = new byte[4];

	/**
	 * The KNXnet/IP device MAC address shall be the Ethernet MAC address of the
	 * KNXnet/IP device. This information may be used to identify the device on the
	 * Ethernet to a server allocating network resources, specifically the unicast
	 * IP address for the KNXnet/IP device.
	 */
	private byte[] deviceMacAddress = new byte[6];

	/**
	 * The Device Friendly Name may be any NULL (00h) terminated ISO 8859-1
	 * character string with a maximum length of 30 octets. This name may be used to
	 * identify the device to a user. Unused octets shall be filled with the NULL
	 * (00h) character.
	 */
	private byte[] deviceFriendlyName = new byte[30];

	public DeviceInformationDIB() {

	}

	public DeviceInformationDIB(final DeviceInformationDIB other) {
		setLength(other.getLength());
		medium = other.medium;
		deviceStatus = other.deviceStatus;
		individualAddress = other.individualAddress;
		projectInstallationIdentifier = other.projectInstallationIdentifier;
		deviceSerialNumber = other.deviceSerialNumber.clone();
		deviceRoutingMulticastAddress = other.deviceRoutingMulticastAddress.clone();
		deviceMacAddress = other.deviceMacAddress.clone();
		deviceFriendlyName = other.deviceFriendlyName.clone();
	}

	@Override
	public DeviceInformationDIB clone() {
		return new DeviceInformationDIB(this);
	}

	@Override
	public DescriptionInformationBlockType getType() {
		return DescriptionInformationBlockType.DEVICE_INFO;
	}

	@Override
	public void fromBytes(final byte[] source, int index) {

		medium = KNXMedium.fromInt(source[index++]);

		deviceStatus = DeviceStatus.fromInt(source[index++]);

		individualAddress = Utils.bytesToUnsignedShort(source[index++], source[index++], true);

		projectInstallationIdentifier = Utils.bytesToUnsignedShort(source[index++], source[index++], true);

		System.arraycopy(source, index, deviceSerialNumber, 0, deviceSerialNumber.length);
		index += deviceSerialNumber.length;

		System.arraycopy(source, index, deviceRoutingMulticastAddress, 0, deviceRoutingMulticastAddress.length);
		index += deviceRoutingMulticastAddress.length;

		System.arraycopy(source, index, deviceMacAddress, 0, deviceMacAddress.length);
		index += deviceMacAddress.length;

		System.arraycopy(source, index, deviceFriendlyName, 0, deviceFriendlyName.length);
		index += deviceFriendlyName.length;
	}

	@Override
	public byte[] getBytes() {

		setLength(54);

		final byte[] bytes = new byte[getLength()];

		int index = 0;
		bytes[index++] = (byte) getLength();
		bytes[index++] = (byte) getType().getValue();
		bytes[index++] = (byte) getMedium().getValue();
		bytes[index++] = (byte) getDeviceStatus().getValue();
		bytes[index++] = (byte) (individualAddress >> 8);
		bytes[index++] = (byte) (individualAddress & 0xFF);
		bytes[index++] = (byte) (projectInstallationIdentifier >> 8);
		bytes[index++] = (byte) (projectInstallationIdentifier & 0xFF);

		System.arraycopy(deviceSerialNumber, 0, bytes, index, deviceSerialNumber.length);
		index += deviceSerialNumber.length;

		System.arraycopy(deviceRoutingMulticastAddress, 0, bytes, index, deviceRoutingMulticastAddress.length);
		index += deviceRoutingMulticastAddress.length;

		System.arraycopy(deviceMacAddress, 0, bytes, index, deviceMacAddress.length);
		index += deviceMacAddress.length;

		System.arraycopy(deviceFriendlyName, 0, bytes, index, deviceFriendlyName.length);
		index += deviceFriendlyName.length;

		return bytes;
	}

	@Override
	public String toString() {

		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Structure Length: ").append(getLength()).append(" bytes").append("\n");
		stringBuilder.append("Description Type: ").append(getType().name()).append(" ")
				.append(Utils.integerToString(getType().getValue())).append("\n");
		stringBuilder.append("KNX Medium: ").append(getMedium().name()).append(" ")
				.append(Utils.integerToString(getMedium().getValue())).append("\n");
		stringBuilder.append("Device Status: ").append(deviceStatus.name()).append(" ")
				.append(Utils.integerToString(deviceStatus.getValue())).append("\n");
		stringBuilder.append("KNX Individual Address: ").append(Utils.integerToKNXAddress(individualAddress))
				.append(" ").append(Utils.integerToString(individualAddress)).append("\n");
		stringBuilder.append("Project Installation Identifier: ")
				.append(Utils.integerToString(projectInstallationIdentifier)).append("\n");
		stringBuilder.append("KNX Serial Number: ").append(Utils.integerToStringNoPrefix(deviceSerialNumber))
				.append("\n");
		stringBuilder.append("Multicast Address: ").append(NetworkUtils.printIPAddress(deviceRoutingMulticastAddress))
				.append("\n");
		stringBuilder.append("MAC Address: ").append(NetworkUtils.printMACAddress(deviceMacAddress)).append("\n");
		stringBuilder.append("Friendly Name: ").append(new String(deviceFriendlyName));

		return stringBuilder.toString();
	}

	public int getIndividualAddress() {
		return individualAddress;
	}

	public void setIndividualAddress(final int individualAddress) {
		this.individualAddress = individualAddress;
	}

	public int getProjectInstallationIdentifier() {
		return projectInstallationIdentifier;
	}

	public void setProjectInstallationIdentifier(final int projectInstallationIdentifier) {
		this.projectInstallationIdentifier = projectInstallationIdentifier;
	}

	public byte[] getDeviceSerialNumber() {
		return deviceSerialNumber;
	}

	public byte[] getDeviceRoutingMulticastAddress() {
		return deviceRoutingMulticastAddress;
	}

	public byte[] getDeviceMacAddress() {
		return deviceMacAddress;
	}

	public byte[] getDeviceFriendlyName() {
		return deviceFriendlyName;
	}

	public KNXMedium getMedium() {
		return medium;
	}

	public void setMedium(final KNXMedium medium) {
		this.medium = medium;
	}

	public DeviceStatus getDeviceStatus() {
		return deviceStatus;
	}

	public void setDeviceStatus(final DeviceStatus deviceStatus) {
		this.deviceStatus = deviceStatus;
	}

	public String getDeviceSerialNumberAsString() {
		return Utils.integerToStringNoPrefix(deviceSerialNumber);
	}

}
