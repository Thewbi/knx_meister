package object_server.requests;

public class GetDatapointDescriptionResponseEntry {

	private int datapointId;

	private int valueType;

	private int configFlags;

	private int dataPointType;

	public byte[] getBytes() {

		int index = 0;

		final byte[] bytes = new byte[5];

		bytes[index++] = (byte) (datapointId >> 8);
		bytes[index++] = (byte) (datapointId & 0xFF);

		bytes[index++] = (byte) (valueType & 0xFF);

		bytes[index++] = (byte) (configFlags & 0xFF);

		bytes[index++] = (byte) (dataPointType & 0xFF);

		return bytes;
	}

	public int getDatapointId() {
		return datapointId;
	}

	public void setDatapointId(final int datapointId) {
		this.datapointId = datapointId;
	}

	public int getValueType() {
		return valueType;
	}

	public void setValueType(final int valueType) {
		this.valueType = valueType;
	}

	public int getConfigFlags() {
		return configFlags;
	}

	public void setConfigFlags(final int configFlags) {
		this.configFlags = configFlags;
	}

	public int getDataPointType() {
		return dataPointType;
	}

	public void setDataPointType(final int dataPointType) {
		this.dataPointType = dataPointType;
	}

}
