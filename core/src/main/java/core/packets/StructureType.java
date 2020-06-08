package core.packets;

public enum StructureType {

	HPAI_CONTROL_ENDPOINT_UDP(0x01),

	HPAI_DATA_ENDPOINT_UDP(0x02),

	/**
	 * Data connection used to configure a KNXnet/IP device.
	 */
	DEVICE_MGMT_CONNECTION(0x03),

	/**
	 * Data connection used to forward KNX telegrams between two KNXnet/IP devices.
	 */
	TUNNELING_CONNECTION(0x04),

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
	OBJSVR_CONNECTION(0x08);

	private static final int HPAI_CONTROL_ENDPOINT_UDP_CODE = 0x01;

//	private static final int HPAI_DATA_ENDPOINT_UDP_CODE = 0x02;

	private static final int DEVICE_MGMT_CONNECTION_CODE = 0x03;

	private static final int TUNNELING_CONNECTION_CODE = 0x04;

	private static final int REMLOG_CONNECTION_CODE = 0x06;

	private static final int REMCONF_CONNECTION_CODE = 0x07;

	private static final int OBJSVR_CONNECTION_CODE = 0x08;

	private final int id;

	StructureType(final int id) {
		this.id = id;
	}

	public static StructureType fromInt(final int id) {

		switch (id) {
		case 0x00:
		case HPAI_CONTROL_ENDPOINT_UDP_CODE:
			return HPAI_CONTROL_ENDPOINT_UDP;

		case DEVICE_MGMT_CONNECTION_CODE:
			return DEVICE_MGMT_CONNECTION;

		case TUNNELING_CONNECTION_CODE:
			return TUNNELING_CONNECTION;

		case REMLOG_CONNECTION_CODE:
			return REMLOG_CONNECTION;

		case REMCONF_CONNECTION_CODE:
			return REMCONF_CONNECTION;

		case OBJSVR_CONNECTION_CODE:
			return OBJSVR_CONNECTION;

		default:
			throw new RuntimeException("Unkown id " + id);
		}
	}

	public int getValue() {
		return id;
	}

}
