package core.packets;

public enum ConnectionType {

	/** Data connection used to configure a KNXnet/IP device. */
	DEVICE_MGMT_CONNECTION(0x03),

	/**
	 * Data connection used to forward KNX telegrams between two KNXnet/IP devices.
	 */
	TUNNEL_CONNECTION(0x04),

	/**
	 * Data connection used for configuration and data transfer with a remote
	 * logging server.
	 */
	REMLOG_CONNECTION(0x06),

	/**
	 * Data connection used for data transfer with a remote configuration server.
	 */
	REMCONF_CONNECTION(0x07),

	/**
	 * Data connection used for configuration and data transfer with an Object
	 * Server in a KNXnet/IP device.
	 */
	OBJSVR_CONNECTION(0x08),

	E_TUNNEL_CONNECTION(0x29),

	/** Used for multicast which requires no connection id id */
	UNKNOWN(0x9999);

	private final int id;

	ConnectionType(final int id) {
		this.id = id;
	}

	public static ConnectionType fromInt(final int id) {

		switch (id) {
		case 0x03:
			return DEVICE_MGMT_CONNECTION;

		case 0x04:
			return TUNNEL_CONNECTION;

		case 0x06:
			return REMLOG_CONNECTION;

		case 0x07:
			return REMCONF_CONNECTION;

		case 0x08:
			return OBJSVR_CONNECTION;

		case 0x29:
			return UNKNOWN;

		case 0x9999:
			return E_TUNNEL_CONNECTION;

		default:
			throw new RuntimeException("Unkown id " + id);
		}
	}

	public int getValue() {
		return id;
	}

}
