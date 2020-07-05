package object_server.requests;

import common.packets.ServiceIdentifier;

public abstract class BaseResponse extends BaseRequest {

	/**
	 * 16 = KNXHeader (6 byte) + Connection Header (4 Byte) +
	 * ObjectServer+MainService+SubService+StartItem+NumberOfItems (6 Byte)
	 */
	protected static final int RESPONSE_PRELUDE_LENGTH = 16;

	@Override
	public byte[] getBytes() {

		final byte[] data = new byte[getMessageLength()];

		int index = 0;

		knxHeader.setTotalLength(getMessageLength());
		knxHeader.setProtocolVersion((byte) OBJECT_SERVER_PROTOCOL_VERSION);
		knxHeader.setServiceIdentifier(ServiceIdentifier.OBJECT_SERVER_REQUEST);

		knxHeader.writeBytesIntoBuffer(data, index);
		index += knxHeader.getLength();

		knxConnectionHeader.writeBytesIntoBuffer(data, index);
		index += knxConnectionHeader.getLength();

		data[index++] = (byte) (mainService & 0xFF);
		data[index++] = (byte) (getSubService() & 0xFF);

		data[index++] = (byte) (((getStart()) >> 8) & 0xFF);
		data[index++] = (byte) (getStart() & 0xFF);

		data[index++] = (byte) (((getMaxAmount()) >> 8) & 0xFF);
		data[index++] = (byte) (getMaxAmount() & 0xFF);

		System.arraycopy(getPayload(), 0, data, RESPONSE_PRELUDE_LENGTH, getPayload().length);

		return data;
	}

	@Override
	public int getMessageLength() {
		return RESPONSE_PRELUDE_LENGTH + getPayload().length;
	}

}
