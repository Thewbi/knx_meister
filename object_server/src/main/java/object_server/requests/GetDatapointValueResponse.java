package object_server.requests;

public class GetDatapointValueResponse extends BaseResponse {

	private int dataPointId;

	private int state;

	private int length;

	private byte[] value;

	private final int valueAmount = 1;

	public GetDatapointValueResponse() {
		setSubService(0x85);
	}

	@Override
	public int getMessageLength() {
		return RESPONSE_PRELUDE_LENGTH + (4 + length) * valueAmount;
	}

	@Override
	public byte[] getPayload() {

		final byte[] data = new byte[4 + length];
		int index = 0;

		// datapoint id (2 byte)
		data[index++] = (byte) (dataPointId >> 8);
		data[index++] = (byte) (dataPointId & 0xFF);

		// state
		data[index++] = (byte) state;

		// length
		data[index++] = (byte) length;

		// value
		System.arraycopy(value, 0, data, 4, value.length);

		return data;
	}

	@Override
	public void fromBytes(final byte[] bytes) {
		throw new RuntimeException("Not implemented yet!");
	}

	public int getDataPointId() {
		return dataPointId;
	}

	public void setDataPointId(final int dataPointId) {
		this.dataPointId = dataPointId;
	}

	public int getState() {
		return state;
	}

	public void setState(final int state) {
		this.state = state;
	}

	public int getLength() {
		return length;
	}

	public void setLength(final int length) {
		this.length = length;
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(final byte[] value) {
		this.value = value;
	}

}
