package object_server.requests;

import common.utils.Utils;

public class GetDatapointValueRequest extends BaseRequest {

	private static final int MESSAGE_LENGTH = 17;

	public static final int GET_DATAPOINT_VALUE_REQUEST_CODE = 0x05;

	private int filter;

	public GetDatapointValueRequest() {
		setSubService(GET_DATAPOINT_VALUE_REQUEST_CODE);
	}

	@Override
	public void fromBytes(final byte[] bytes) {

		final int preludeLength = 12;

		// start, length
		final boolean bigEndian = true;
		setStart(Utils.bytesToUnsignedShort(bytes[preludeLength + 0], bytes[preludeLength + 1], bigEndian));
		setMaxAmount(Utils.bytesToUnsignedShort(bytes[preludeLength + 2], bytes[preludeLength + 3], bigEndian));

		// filter
		filter = bytes[preludeLength + 4];
	}

	@Override
	public int getMessageLength() {
		return MESSAGE_LENGTH;
	}

	public int getFilter() {
		return filter;
	}

	public void setFilter(final int filter) {
		this.filter = filter;
	}

	@Override
	public String toString() {
		return "DatapointId=" + getStart() + " (" + Utils.integerToString(getStart()) + ") MaxAmount=" + getMaxAmount();
	}

}
