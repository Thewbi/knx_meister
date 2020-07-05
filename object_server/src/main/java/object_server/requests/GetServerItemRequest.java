package object_server.requests;

import common.utils.Utils;

public class GetServerItemRequest extends BaseRequest {

	private static final int MESSAGE_LENGTH = 16;

	public static final int GET_SERVER_ITEM_REQUEST_CODE = 0x01;

	public GetServerItemRequest() {
		setSubService(GET_SERVER_ITEM_REQUEST_CODE);
	}

	public void fromBytes(final byte[] bytes) {

		final int preludeLength = 12;

		// extract start, length
		final boolean bigEndian = true;
		setStart(Utils.bytesToUnsignedShort(bytes[preludeLength + 0], bytes[preludeLength + 1], bigEndian));
		setMaxAmount(Utils.bytesToUnsignedShort(bytes[preludeLength + 2], bytes[preludeLength + 3], bigEndian));
	}

	@Override
	public String toString() {

		final StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("GetServerItemRequest StartItem: ").append(getStart()).append(" MaxNumberOfItems: ")
				.append(getMaxAmount());

		return stringBuffer.toString();
	}

	@Override
	public int getMessageLength() {
		return MESSAGE_LENGTH;
	}

}
