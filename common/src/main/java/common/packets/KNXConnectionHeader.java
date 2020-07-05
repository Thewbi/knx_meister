package common.packets;

public class KNXConnectionHeader {

	private short length = 4;

	private short channel;

	private int sequenceCounter;

	private int reserved;

	public KNXConnectionHeader() {
	}

	public KNXConnectionHeader(final KNXConnectionHeader connectionHeader) {
		length = connectionHeader.length;
		channel = connectionHeader.channel;
		sequenceCounter = connectionHeader.sequenceCounter;
		reserved = connectionHeader.reserved;
	}

	public byte[] getBytes() {

		final byte[] bytes = new byte[4];
		bytes[0] = (byte) length;
		bytes[1] = (byte) channel;
		bytes[2] = (byte) sequenceCounter;
		bytes[3] = (byte) reserved;

		return bytes;
	}

	public void writeBytesIntoBuffer(final byte[] buffer, final int offset) {

		int index = 0;

		buffer[offset + index] = (byte) length;
		index++;

		buffer[offset + index] = (byte) channel;
		index++;

		buffer[offset + index] = (byte) sequenceCounter;
		index++;

		buffer[offset + index] = (byte) reserved;
		index++;
	}

	public void fromBytes(final byte[] source, final int startIndex) {

		// length
		length = (short) ((source[startIndex]) & 0xFF);

		// channel id
		channel = (short) ((source[startIndex + 1]) & 0xFF);

		// sequenceCounter
		sequenceCounter = (source[startIndex + 2]) & 0xFF;

		// reserved
		reserved = (source[startIndex + 3]) & 0xFF;
	}

	public short getLength() {
		return length;
	}

	public void setLength(final short length) {
		this.length = length;
	}

	public short getChannel() {
		return channel;
	}

	public void setChannel(final short channel) {
		this.channel = channel;
	}

	public int getSequenceCounter() {
		return sequenceCounter;
	}

	public void setSequenceCounter(final int sequenceCounter) {
		this.sequenceCounter = sequenceCounter;
	}

	public int getReserved() {
		return reserved;
	}

	public void setReserved(final int reserved) {
		this.reserved = reserved;
	}

}
