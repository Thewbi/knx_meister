package core.packets;

/**
 * ConnectionRequestInformation (CRI)
 *
 * 7.8.1 CONNECT_REQUEST
 */
public class ConnectionRequestInformation extends Structure {

	private int knxLayer;

	private int reserved;

	public ConnectionRequestInformation() {

	}

	public ConnectionRequestInformation(final ConnectionRequestInformation other) {
		setLength(other.getLength());
		setStructureType(other.getStructureType());
		knxLayer = other.knxLayer;
		reserved = other.reserved;
	}

	@Override
	public Structure clone() {
		return new ConnectionRequestInformation(this);
	}

	@Override
	public byte[] getPayloadBytes() {
		final byte[] payload = new byte[2];
		payload[0] = (byte) knxLayer;
		payload[1] = 0x00;

		return payload;
	}

	@Override
	public void fromBytes(final byte[] bytes, final int startIndex) {
		knxLayer = bytes[startIndex] & 0xFF;
		reserved = bytes[startIndex + 1] & 0xFF;
	}

	public int getKnxLayer() {
		return knxLayer;
	}

	public void setKnxLayer(final int knxLayer) {
		this.knxLayer = knxLayer;
	}

	public int getReserved() {
		return reserved;
	}

	public void setReserved(final int reserved) {
		this.reserved = reserved;
	}

}
