package object_server.requests;

import common.utils.Utils;

public class GetDatapointDescriptionRequest extends BaseRequest {

	private static final int MESSAGE_LENGTH = 16;

	public static final int GET_DATAPOINT_DESCRIPTION_REQUEST_CODE = 0x03;

	public GetDatapointDescriptionRequest() {
		setSubService(GET_DATAPOINT_DESCRIPTION_REQUEST_CODE);
	}

	public void fromBytes(final byte[] bytes) {

		final int preludeLength = 12;

		// extract start, length
		final boolean bigEndian = true;
		setStart(Utils.bytesToUnsignedShort(bytes[preludeLength + 0], bytes[preludeLength + 1], bigEndian));
		setMaxAmount(Utils.bytesToUnsignedShort(bytes[preludeLength + 2], bytes[preludeLength + 3], bigEndian));
	}

	@Override
	public int getMessageLength() {
		return MESSAGE_LENGTH;
	}

	@Override
	public String toString() {
		return "DatapointId=" + getStart() + " (" + Utils.integerToString(getStart()) + ") MaxAmount=" + getMaxAmount();
	}

}
