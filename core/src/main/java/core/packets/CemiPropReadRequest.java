package core.packets;

import common.utils.Utils;

public class CemiPropReadRequest {

	private short messageCode;

	private int objectType;

	private short objectInstance;

	private short propertyId;

	private int range;

	private byte[] responseData;

	private int length = 7;

	public CemiPropReadRequest() {
	}

	public CemiPropReadRequest(final CemiPropReadRequest cemiPropReadRequest) {
		messageCode = cemiPropReadRequest.messageCode;
		objectType = cemiPropReadRequest.objectType;
		objectInstance = cemiPropReadRequest.objectInstance;
		propertyId = cemiPropReadRequest.propertyId;
		range = cemiPropReadRequest.range;
		if (responseData != null) {
			responseData = cemiPropReadRequest.responseData.clone();
		}
	}

	public void fromBytes(final byte[] source, final int startIndex) {

		// messageCode (1 byte)
		messageCode = (short) ((source[startIndex + 0]) & 0xFF);

		// object type (2 byte)
		objectType = Utils.bytesToUnsignedShort(source[startIndex + 1], source[startIndex + 2], true);

		// objectInstance (1 byte)
		objectInstance = (short) ((source[startIndex + 3]) & 0xFF);

		// propertyId (1 byte)
		propertyId = (short) ((source[startIndex + 4]) & 0xFF);

		// range (2 byte)
		range = Utils.bytesToUnsignedShort(source[startIndex + 5], source[startIndex + 6], true);

		// TODO if this is optional, make sure this method works correctly for packets
		// that do not have those bytes
		responseData = new byte[2];
		responseData[0] = source[startIndex + 7];
		responseData[1] = source[startIndex + 8];
	}

	public byte[] getBytes() {

		length = 7;

		// add length for optional response data
		if (responseData != null) {
			length += responseData.length;
		}

		int index = 0;

		final byte[] payload = new byte[length];

		payload[index++] = (byte) (messageCode & 0xFF);

		payload[index++] = (byte) (((objectType) >> 8) & 0xFF);
		payload[index++] = (byte) (objectType & 0xFF);

		payload[index++] = (byte) (objectInstance & 0xFF);

		payload[index++] = (byte) (propertyId & 0xFF);

		payload[index++] = (byte) (((range) >> 8) & 0xFF);
		payload[index++] = (byte) (range & 0xFF);

		if (responseData != null) {
			for (int i = 0; i < responseData.length; i++) {
				payload[index++] = responseData[i];
			}
		}

		return payload;
	}

	public short getMessageCode() {
		return messageCode;
	}

	public void setMessageCode(final short messageCode) {
		this.messageCode = messageCode;
	}

	public short getObjectInstance() {
		return objectInstance;
	}

	public void setObjectInstance(final short objectInstance) {
		this.objectInstance = objectInstance;
	}

	public short getPropertyId() {
		return propertyId;
	}

	public void setPropertyId(final short propertyId) {
		this.propertyId = propertyId;
	}

	public int getRange() {
		return range;
	}

	public void setRange(final int range) {
		this.range = range;
	}

	public int getObjectType() {
		return objectType;
	}

	public void setObjectType(final int objectType) {
		this.objectType = objectType;
	}

	public byte[] getResponseData() {
		return responseData;
	}

	public void setResponseData(final byte[] responseData) {
		this.responseData = responseData;
	}

	public int getLength() {
		return length;
	}

	public void setLength(final int length) {
		this.length = length;
	}

}
