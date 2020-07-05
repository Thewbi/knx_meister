package object_server.api.facade;

public enum ErrorCode {

	NO_ERROR(0x00),

	INTERNAL_ERROR(0x01),

	NO_ITEM_FOUND(0x02),

	BUFFER_IS_TOO_SMALL(0x03),

	ITEM_IS_NOT_WRITEABLE(0x04),

	SERVICE_IS_NOT_SUPPORTED(0x05),

	BAD_SERVICE_PARAMETER(0x06),

	WRONG_DATAPOINT_ID(0x07),

	BAD_DATAPOINT_COMMAND(0x08),

	BAD_LENGTH_OF_DATAPOINT_VALUE(0x09),

	MESSAGE_INCONSISTENT(0x10),

	OBJECT_SERVER_IS_BUSY(0x11),

	/** No Value */
	UNSET(0x99);

	private final int id;

	ErrorCode(final int id) {
		this.id = id;
	}

	public static ErrorCode fromInt(final int id) {

		switch (id) {
		case 0x00:
			return NO_ERROR;

		case 0x01:
			return INTERNAL_ERROR;

		case 0x02:
			return NO_ITEM_FOUND;

		case 0x03:
			return BUFFER_IS_TOO_SMALL;

		case 0x04:
			return ITEM_IS_NOT_WRITEABLE;

		case 0x05:
			return SERVICE_IS_NOT_SUPPORTED;

		case 0x06:
			return BAD_SERVICE_PARAMETER;

		case 0x07:
			return WRONG_DATAPOINT_ID;

		case 0x08:
			return BAD_DATAPOINT_COMMAND;

		case 0x09:
			return BAD_LENGTH_OF_DATAPOINT_VALUE;

		case 0x10:
			return MESSAGE_INCONSISTENT;

		case 0x11:
			return OBJECT_SERVER_IS_BUSY;

		default:
			throw new RuntimeException("Unkown id " + id);
		}
	}

	public int getValue() {
		return id;
	}

}
