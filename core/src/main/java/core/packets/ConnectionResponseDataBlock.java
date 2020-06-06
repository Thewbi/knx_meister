package core.packets;

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

	public byte[] getBytes() {

		final byte[] bytes = new byte[length];
		bytes[0] = (byte) length;
		bytes[1] = (byte) connectionType.getValue();
		bytes[2] = (byte) deviceAddress;
		bytes[3] = (byte) (deviceAddress >> 8 & 0xFF);

		return bytes;
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
