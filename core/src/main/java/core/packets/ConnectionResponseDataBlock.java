package core.packets;

import core.common.Utils;

/**
 * 7.5.3 Connection Response Data Block (CRD)
 */
public class ConnectionResponseDataBlock {

	/**
	 * 1 Byte.
	 *
	 * length of this structure because it contains optional variable length
	 * components
	 */
	private int length = 4;

	/** 1 Byte */
	private ConnectionType connectionType;

	/** 8.8.6 CONNECT_RESPONSE example */
	private int deviceAddress;

	public ConnectionResponseDataBlock() {
	}

	public ConnectionResponseDataBlock(final ConnectionResponseDataBlock connectionResponseDataBlock) {
		length = connectionResponseDataBlock.length;
		connectionType = connectionResponseDataBlock.connectionType;
		deviceAddress = connectionResponseDataBlock.deviceAddress;
	}

	public byte[] getBytes() {

		length = deviceAddress > 0 ? 4 : 2;

		final byte[] bytes = new byte[length];
		bytes[0] = (byte) length;
		bytes[1] = (byte) connectionType.getValue();

		if (deviceAddress > 0) {
			bytes[2] = (byte) deviceAddress;
			bytes[3] = (byte) (deviceAddress >> 8 & 0xFF);
		}

		return bytes;
	}

	@Override
	public String toString() {

		final StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("Connection Type = ").append(connectionType.name()).append(" (")
				.append(connectionType.getValue()).append(")\n");
		stringBuilder.append("KNX Individual Address = ").append(Utils.integerToString(deviceAddress)).append("\n");

		return stringBuilder.toString();
	}

	public int getLength() {
		return length;
	}

	public void setLength(final int length) {
		this.length = length;
	}

	public ConnectionType getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(final ConnectionType connectionType) {
		this.connectionType = connectionType;
	}

	public int getDeviceAddress() {
		return deviceAddress;
	}

	public void setDeviceAddress(final int deviceAddress) {
		this.deviceAddress = deviceAddress;
	}

}
