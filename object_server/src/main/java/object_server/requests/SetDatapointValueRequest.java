package object_server.requests;

import common.utils.Utils;

public class SetDatapointValueRequest extends BaseRequest {

	public static final int SET_DATAPOINT_VALUE_REQUEST_CODE = 0x06;

	private int length;

	private final int valueAmount = 1;

	public SetDatapointValueRequest() {
		setSubService(SET_DATAPOINT_VALUE_REQUEST_CODE);
	}

	@Override
	public int getMessageLength() {
		return REQUEST_PRELUDE_LENGTH + (4 + length) * valueAmount;
	}

	@Override
	public void fromBytes(final byte[] bytes) {

		final int preludeLength = 12;

		// start, length
		final boolean bigEndian = true;
		setStart(Utils.bytesToUnsignedShort(bytes[preludeLength + 0], bytes[preludeLength + 1], bigEndian));
		setMaxAmount(Utils.bytesToUnsignedShort(bytes[preludeLength + 2], bytes[preludeLength + 3], bigEndian));

		int datapointId = 0;
		int command = 0;
		int length = 0;
		byte[] value;

		int index = preludeLength + 4;

		for (int i = 0; i < getMaxAmount(); i++) {

			// datapoint id
			datapointId = Utils.bytesToUnsignedShort(bytes[index++], bytes[index++], bigEndian);

			// command
			command = bytes[index++];

			// length
			length = bytes[index++];

			// value
			value = new byte[length];
			System.arraycopy(bytes, index, value, 0, length);

			getKnxProject();
		}

	}

}
