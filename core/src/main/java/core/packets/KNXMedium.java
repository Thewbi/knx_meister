package core.packets;

public enum KNXMedium {

	RESERVED_1(0x01),

	TP1(0x02),

	PL110(0x04),

	RESERVED_3(0x08),

	RF(0x10),

	KNX_IP(0x20);

	private final int id;

	KNXMedium(final int id) {
		this.id = id;
	}

	public static KNXMedium fromInt(final int id) {

		switch (id) {
		case 0x01:
			return RESERVED_1;

		case 0x02:
			return TP1;

		case 0x04:
			return PL110;

		case 0x08:
			return RESERVED_3;

		case 0x10:
			return RF;

		case 0x20:
			return KNX_IP;

		default:
			throw new RuntimeException("Unkown id " + id);
		}
	}

	public int getValue() {
		return id;
	}

}
