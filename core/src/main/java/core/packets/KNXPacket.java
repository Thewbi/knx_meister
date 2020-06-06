package core.packets;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections4.MapUtils;

public class KNXPacket {

	private final Header header = new Header();

	private final Map<StructureType, Structure> structureMap = new TreeMap<>();

	private final Map<DescriptionInformationBlockType, DescriptionInformationBlock> dibMap = new TreeMap<>();

	private int communicationChannelId = -1;

	private ConnectionStatus connectionStatus = ConnectionStatus.UNSET;

	private ConnectionResponseDataBlock connectionResponseDataBlock;

	public byte[] getBytes() {

		// HPAI structure
		byte[] hpaiControlEndpoingStructureBuffer = null;
		if (structureMap.containsKey(StructureType.HPAI_CONTROL_ENDPOINT_UDP)) {
			hpaiControlEndpoingStructureBuffer = structureMap.get(StructureType.HPAI_CONTROL_ENDPOINT_UDP).getBytes();
		}
		byte[] hpaiDataEndpoingStructureBuffer = null;
		if (structureMap.containsKey(StructureType.HPAI_DATA_ENDPOINT_UDP)) {
			hpaiDataEndpoingStructureBuffer = structureMap.get(StructureType.HPAI_CONTROL_ENDPOINT_UDP).getBytes();
		}

		byte[] crdBuffer = null;
		if (getConnectionResponseDataBlock() != null) {
			crdBuffer = getConnectionResponseDataBlock().getBytes();
		}

		// compute total length
		int totalLength = 0;
		totalLength += header.getLength();
		if (this.getCommunicationChannelId() >= 0x00) {
			totalLength += 1;
		}
		if (this.getConnectionStatus() != ConnectionStatus.UNSET) {
			totalLength += 1;
		}
		if (hpaiControlEndpoingStructureBuffer != null) {
			totalLength += hpaiControlEndpoingStructureBuffer.length;
		}
		if (hpaiDataEndpoingStructureBuffer != null) {
			totalLength += hpaiDataEndpoingStructureBuffer.length;
		}
		totalLength += crdBuffer == null ? 0 : crdBuffer.length;
		for (final DescriptionInformationBlock dib : dibMap.values()) {
			totalLength += dib.getLength();
		}

		// header length
		header.setTotalLength(totalLength);

		final byte[] payload = new byte[totalLength];

		int index = 0;

		// copy header into payload
		System.arraycopy(header.getBytes(), 0, payload, index, header.getLength());
		index += header.getLength();

		// connection response
		if (this.getCommunicationChannelId() >= 0x00) {
			payload[index++] = (byte) getCommunicationChannelId();
		}
		if (this.getConnectionStatus() != ConnectionStatus.UNSET) {
			payload[index++] = (byte) getConnectionStatus().getValue();
		}

		// copy HPAI into payload
		if (hpaiControlEndpoingStructureBuffer != null) {
			System.arraycopy(hpaiControlEndpoingStructureBuffer, 0, payload, index,
					hpaiControlEndpoingStructureBuffer.length);
			index += hpaiControlEndpoingStructureBuffer.length;
		}
		if (hpaiDataEndpoingStructureBuffer != null) {
			System.arraycopy(hpaiDataEndpoingStructureBuffer, 0, payload, index,
					hpaiDataEndpoingStructureBuffer.length);
			index += hpaiDataEndpoingStructureBuffer.length;
		}

		// copy CRD - connection response data
		if (crdBuffer != null) {
			System.arraycopy(crdBuffer, 0, payload, index, crdBuffer.length);
			index += crdBuffer.length;
		}

		// copy all DIB
		for (final DescriptionInformationBlock dib : dibMap.values()) {

			final byte[] dibBytes = dib.getBytes();

			System.arraycopy(dibBytes, 0, payload, index, dibBytes.length);
			index += dibBytes.length;
		}

		return payload;
	}

	@Override
	public String toString() {

		// KNX/IP Search Response, Control @ 192.168.0.241:3671, 1.1.8 "KNX IP BAOS 777"

		final StringBuilder stringBuilder = new StringBuilder();

		// header
		stringBuilder.append(header.toString()).append("\n").append("\n");

		// structure
		if (MapUtils.isNotEmpty(structureMap)) {
			for (final Structure structure : structureMap.values()) {
				stringBuilder.append(structure.toString()).append("\n").append("\n");
			}
		}

		// DIB
		if (MapUtils.isNotEmpty(dibMap)) {
			for (final DescriptionInformationBlock dib : dibMap.values()) {
				stringBuilder.append(dib.toString()).append("\n").append("\n");
			}
		}

		return stringBuilder.toString();
	}

	public Header getHeader() {
		return header;
	}

	public Map<DescriptionInformationBlockType, DescriptionInformationBlock> getDibMap() {
		return dibMap;
	}

	public Map<StructureType, Structure> getStructureMap() {
		return structureMap;
	}

	public ConnectionResponseDataBlock getConnectionResponseDataBlock() {
		return connectionResponseDataBlock;
	}

	public void setConnectionResponseDataBlock(final ConnectionResponseDataBlock connectionResponseDataBlock) {
		this.connectionResponseDataBlock = connectionResponseDataBlock;
	}

	public int getCommunicationChannelId() {
		return communicationChannelId;
	}

	public void setCommunicationChannelId(final int communicationChannelId) {
		this.communicationChannelId = communicationChannelId;
	}

	public ConnectionStatus getConnectionStatus() {
		return connectionStatus;
	}

	public void setConnectionStatus(final ConnectionStatus connectionStatus) {
		this.connectionStatus = connectionStatus;
	}

}
