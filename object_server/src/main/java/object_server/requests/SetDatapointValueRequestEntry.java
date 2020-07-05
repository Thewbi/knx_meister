package object_server.requests;

public class SetDatapointValueRequestEntry {

	private int datapointId = 0;

	private int command = 0;

	private int length = 0;

	private byte[] valueAsBytes;

	public int getDatapointId() {
		return datapointId;
	}

	public void setDatapointId(final int datapointId) {
		this.datapointId = datapointId;
	}

	public int getCommand() {
		return command;
	}

	public void setCommand(final int command) {
		this.command = command;
	}

	public int getLength() {
		return length;
	}

	public void setLength(final int length) {
		this.length = length;
	}

	public byte[] getValueAsBytes() {
		return valueAsBytes;
	}

	public void setValueAsBytes(final byte[] valueAsBytes) {
		this.valueAsBytes = valueAsBytes;
	}

}
