package common.packets;

import common.utils.Utils;

public class KNXHeader {

	private byte length = 6;

	private byte protocolVersion = 0x10;

	private ServiceIdentifier serviceIdentifier = ServiceIdentifier.UNKNOWN;

	private int totalLength;

	public KNXHeader() {

	}

	public KNXHeader(final KNXHeader header) {
		length = header.length;
		protocolVersion = header.protocolVersion;
		serviceIdentifier = header.serviceIdentifier;
		totalLength = header.totalLength;
	}

	public byte[] getBytes() {

		final byte[] bytes = new byte[6];
		bytes[0] = length;
		bytes[1] = protocolVersion;

		final int serviceIdentifierAsInt = serviceIdentifier.getValue();
		bytes[2] = (byte) ((serviceIdentifierAsInt >> 8) & 0xFF);
		bytes[3] = (byte) (serviceIdentifierAsInt & 0xFF);

		bytes[4] = (byte) ((totalLength >> 8) & 0xFF);
		bytes[5] = (byte) (totalLength & 0xFF);

		return bytes;
	}

	public void writeBytesIntoBuffer(final byte[] buffer, final int offset) {

		int index = 0;

		buffer[offset + index] = length;
		index++;
		buffer[offset + index] = protocolVersion;
		index++;

		final int serviceIdentifierAsInt = serviceIdentifier.getValue();
		buffer[offset + index] = (byte) ((serviceIdentifierAsInt >> 8) & 0xFF);
		index++;
		buffer[offset + index] = (byte) (serviceIdentifierAsInt & 0xFF);
		index++;

		buffer[offset + index] = (byte) ((totalLength >> 8) & 0xFF);
		index++;
		buffer[offset + index] = (byte) (totalLength & 0xFF);
		index++;
	}

	public void fromBytes(final byte[] data, final int startIndex) {

		length = data[startIndex];
		protocolVersion = data[startIndex + 1];

		// service identifier
		final int serviceIdentifierId = Utils.bytesToUnsignedShort(data[startIndex + 2], data[startIndex + 3], true);
		serviceIdentifier = ServiceIdentifier.fromInt(serviceIdentifierId);

		setTotalLength(Utils.bytesToUnsignedShort(data[startIndex + 4], data[startIndex + 5], true));
	}

	@Override
	public String toString() {

		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Header Length: ").append(getLength()).append(" bytes").append("\n");
		stringBuilder.append("Protocol version: ").append(Utils.integerToString(getProtocolVersion())).append("\n");
		stringBuilder.append("Service Identifier: ").append(getServiceIdentifier().name()).append(" (")
				.append(Utils.integerToString(getServiceIdentifier().getValue())).append(")").append("\n");
		stringBuilder.append("Total Length: ").append(getTotalLength()).append(" bytes");

		return stringBuilder.toString();
	}

	public byte getLength() {
		return length;
	}

	public void setLength(final byte length) {
		this.length = length;
	}

	public byte getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(final byte protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public ServiceIdentifier getServiceIdentifier() {
		return serviceIdentifier;
	}

	public void setServiceIdentifier(final ServiceIdentifier serviceIdentifier) {
		this.serviceIdentifier = serviceIdentifier;
	}

	public int getTotalLength() {
		return totalLength;
	}

	public void setTotalLength(final int totalLength) {
		this.totalLength = totalLength;
	}

}
