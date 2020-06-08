package core.packets;

public abstract class Structure {

	private int length;

	private StructureType structureType;

	public abstract byte[] getPayloadBytes();

	public abstract void fromBytes(byte[] bytes, int startIndex);

	@Override
	public abstract Structure clone();

	public int getPayloadLength() {
		final byte[] bytes = getPayloadBytes();
		return bytes == null ? 0 : bytes.length;
	}

	public byte[] getBytes() {

		final int payloadLength = getPayloadLength();
		length = payloadLength;
		if (length > 252) {
			// extended length format comprises one marker byte 0xFF and two byte length
			// information
			length += 3;
		} else {
			// normal length is encoded in a single byte
			length++;
		}
		// one byte for the structure type
		length++;

		final byte[] bytes = new byte[length];
		int index = 0;

		// encode length
		if (length > 252) {
			bytes[index++] = (byte) 0xFF;
			bytes[index++] = (byte) ((length >> 8) & 0xFF);
			bytes[index++] = (byte) (length & 0xFF);
		} else {
			bytes[index++] = (byte) length;
		}

		// encode type
		bytes[index++] = (byte) getStructureType().getValue();

		// encode payload
		System.arraycopy(getPayloadBytes(), 0, bytes, index, payloadLength);

		return bytes;
	}

	@Override
	public String toString() {

		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("StructureType: ").append(structureType.name()).append("\n");
		stringBuilder.append("Structure Length: ").append(getLength()).append(" bytes");

		return stringBuilder.toString();
	}

	public int getLength() {
		return length;
	}

	public void setLength(final int length) {
		this.length = length;
	}

	public StructureType getStructureType() {
		return structureType;
	}

	public void setStructureType(final StructureType structureType) {
		this.structureType = structureType;
	}

}
