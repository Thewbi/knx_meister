package api.device;

public enum DeviceStatus {

	NORMAL_MODE(0x00),

	PROGRAMMING_MODE(0x01);

	private final int id;

	DeviceStatus(final int id) {
		this.id = id;
	}

	public static DeviceStatus fromInt(final int id) {

		switch (id) {
		case 0x00:
			return NORMAL_MODE;

		case 0x01:
			return PROGRAMMING_MODE;

		default:
			throw new RuntimeException("Unkown id " + id);
		}
	}

	public int getValue() {
		return id;
	}

}
