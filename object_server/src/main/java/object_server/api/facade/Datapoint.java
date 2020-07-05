package object_server.api.facade;

import common.utils.Utils;

public class Datapoint {

	private int id;

	private int valueType;

	private int configFlags;

	private int dataPointType;

	public void fromBytes(final byte[] bytes, final int startIndex) {
		id = Utils.bytesToUnsignedShort(bytes[startIndex + 0], bytes[startIndex + 1], true);
		valueType = bytes[startIndex + 2];
		configFlags = bytes[startIndex + 3];
		dataPointType = bytes[startIndex + 4];
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
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

	@Override
	public String toString() {
		return "Datapoint [id=" + id + ", valueType=" + valueType + ", configFlags=" + configFlags + ", dataPointType="
				+ dataPointType + "]";
	}

}
