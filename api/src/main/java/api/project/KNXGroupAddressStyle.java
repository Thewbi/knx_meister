package api.project;

public enum KNXGroupAddressStyle {

	FREE,

	TWOLEVEL,

	THREELEVEL,

	UNKNOWN;

	public static KNXGroupAddressStyle fromString(final String attribute) {
		if (attribute.equalsIgnoreCase("Free")) {
			return FREE;
		} else if (attribute.equalsIgnoreCase("TwoLevel")) {
			return TWOLEVEL;
		} else if (attribute.equalsIgnoreCase("ThreeLevel")) {
			return THREELEVEL;
		}
		return UNKNOWN;
	}

}
