package core.packets;

/**
 * 7.5.4 Description Information Block (DIB) - Table 1 - Description type codes
 *
 * Also called DescriptioType in the KNX specification
 */
public enum DescriptionInformationBlockType {

	/** Device information e.g. KNX medium. */
	DEVICE_INFO(0x01),

	/** Service families supported by the device. */
	SUPP_SVC_FAMILIES(0x02),

	/** IP configuration */
	IP_CONFIG(0x03),

	/** current configuration */
	IP_CUR_CONFIG(0x04),

	/** KNX addresses */
	KNX_ADDRESSES(0x05),

	/** DIB structure for further data defined by device manufacturer. */
	MFR_DATA(0xFE);

	private final int id;

	DescriptionInformationBlockType(final int id) {
		this.id = id;
	}

	public static DescriptionInformationBlockType fromInt(final int id) {
		switch (id) {
		case 0x01:
			return DEVICE_INFO;

		case 0x02:
			return SUPP_SVC_FAMILIES;

		case 0x03:
			return IP_CONFIG;

		case 0x04:
			return IP_CUR_CONFIG;

		case 0x05:
			return KNX_ADDRESSES;

		case 0xFE:
			return MFR_DATA;

		default:
			throw new RuntimeException("Unkown id " + id);
		}
	}

	public int getValue() {
		return id;
	}

}
