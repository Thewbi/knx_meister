package core.packets;

public enum KNXLayer {

	TUNNEL_LINKLAYER(0x02);

	private final int id;

	KNXLayer(final int id) {
		this.id = id;
	}

	public static KNXLayer fromInt(final int id) {

		switch (id) {

		case 0x02:
			return TUNNEL_LINKLAYER;

		default:
			throw new RuntimeException("Unkown id " + id);
		}
	}

	public int getValue() {
		return id;
	}
}
