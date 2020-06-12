package core.packets;

public enum DeviceManagement {

	M_PROP_READ_CON(0xfb),

	M_PROP_READ_REQ(0xfc),

	T_DATA_CONNECTED_REQ(0x41),

	T_DATA_CONNECTED_IND(0x89);

	public static final int M_PROP_READ_CON_VALUE = 0xfb;

	public static final int M_PROP_READ_REQ_VALUE = 0xfc;

	public static final int T_DATA_CONNECTED_REQ_VALUE = 0x41;

	public static final int T_DATA_CONNECTED_IND_VALUE = 0x89;

	private final int id;

	DeviceManagement(final int id) {
		this.id = id;
	}

	public static DeviceManagement fromInt(final int id) {

		switch (id) {
		case 0xfb:
			return M_PROP_READ_CON;

		case 0xfc:
			return M_PROP_READ_REQ;

		case 0x41:
			return T_DATA_CONNECTED_REQ;

		case 0x89:
			return T_DATA_CONNECTED_IND;

		default:
			throw new RuntimeException("Unkown id " + id);
		}
	}

	public int getValue() {
		return id;
	}

}
