package core.packets;

public enum ServiceFamily {

	KNXNET_IP_CORE(0x02),

	KNXNET_DEVICE_MGMT(0x03),

	KNXNET_IP_TUNNELLING(0x04),

	KNXNET_IP_ROUTING(0x05),

	KNXNET_OBJSVR_CONNECTION(0x08);

	private final int id;

	ServiceFamily(final int id) {
		this.id = id;
	}

	public static ServiceFamily fromInt(final int id) {

		switch (id) {
		case 0x02:
			return KNXNET_IP_CORE;

		case 0x03:
			return KNXNET_DEVICE_MGMT;

		case 0x04:
			return KNXNET_IP_TUNNELLING;

		case 0x05:
			return KNXNET_IP_ROUTING;

		case 0x08:
			return KNXNET_OBJSVR_CONNECTION;

		default:
			throw new RuntimeException("Unkown id " + id);
		}
	}

	public int getValue() {
		return id;
	}
}
