package core.packets;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections4.MapUtils;

import core.communication.Connection;

/**
 * <ol>
 * <li/>Make sure the new object has a copy constructor and a clone() method
 * <li/>Make sure the byte serialization of your new object works. getBytes()
 * method.
 * <li/>Update the copy constructor and clone() of the KNXPacket, so your new
 * object is copied
 * <li/>Update the CoreKNXPacketConverter and DeviceManagementKNXPacketConverter
 * so that your new object is parsed from the incoming byte array
 * <li/>Update the KNXPacket.toBytes() method so that the new object gets
 * serialized correctly
 * </ol>
 */
public class KNXPacket {

	private Header header = new Header();

	private ConnectionHeader connectionHeader;

	private final Map<StructureType, Structure> structureMap = new TreeMap<>();

	private final Map<DescriptionInformationBlockType, DescriptionInformationBlock> dibMap = new TreeMap<>();

	private int communicationChannelId = -1;

	private ConnectionStatus connectionStatus = ConnectionStatus.UNSET;

	private ConnectionResponseDataBlock connectionResponseDataBlock;

	private CemiPropReadRequest cemiPropReadRequest;

	private CemiTunnelRequest cemiTunnelRequest;

	private Connection connection;

	public KNXPacket() {

	}

	public KNXPacket(final KNXPacket knxPacket) {

		// header
		header = new Header(knxPacket.getHeader());

		// connection header
		if (knxPacket.connectionHeader != null) {
			connectionHeader = new ConnectionHeader(knxPacket.connectionHeader);
		}

		if (MapUtils.isNotEmpty(knxPacket.structureMap)) {

			for (final Structure structure : knxPacket.structureMap.values()) {

				final Structure clonedStructure = structure.clone();
				structureMap.put(clonedStructure.getStructureType(), clonedStructure);
			}
		}

		if (MapUtils.isNotEmpty(knxPacket.dibMap)) {

			for (final DescriptionInformationBlock descriptionInformationBlock : knxPacket.dibMap.values()) {

				final DescriptionInformationBlock clonedDescriptionInformationBlock = descriptionInformationBlock
						.clone();
				dibMap.put(clonedDescriptionInformationBlock.getType(), clonedDescriptionInformationBlock);
			}
		}

		communicationChannelId = knxPacket.communicationChannelId;
		connectionStatus = knxPacket.connectionStatus;

		if (knxPacket.getConnectionResponseDataBlock() != null) {
			connectionResponseDataBlock = new ConnectionResponseDataBlock(knxPacket.getConnectionResponseDataBlock());
		}
		if (knxPacket.getCemiPropReadRequest() != null) {
			cemiPropReadRequest = new CemiPropReadRequest(knxPacket.getCemiPropReadRequest());
		}
		if (knxPacket.getCemiTunnelRequest() != null) {
			cemiTunnelRequest = new CemiTunnelRequest(knxPacket.getCemiTunnelRequest());
		}
	}

	public byte[] getBytes() {

		// connectionHeader
		byte[] connectionHeaderBuffer = null;
		if (getConnectionHeader() != null) {
			connectionHeaderBuffer = getConnectionHeader().getBytes();
		}

		// HPAI structure
		byte[] hpaiControlEndpoingStructureBuffer = null;
		if (structureMap.containsKey(StructureType.HPAI_CONTROL_ENDPOINT_UDP)) {
			hpaiControlEndpoingStructureBuffer = structureMap.get(StructureType.HPAI_CONTROL_ENDPOINT_UDP).getBytes();
		}
		byte[] hpaiDataEndpoingStructureBuffer = null;
		if (structureMap.containsKey(StructureType.HPAI_DATA_ENDPOINT_UDP)) {
			hpaiDataEndpoingStructureBuffer = structureMap.get(StructureType.HPAI_CONTROL_ENDPOINT_UDP).getBytes();
		}

		// Connection Response Datablock
		byte[] crdBuffer = null;
		if (getConnectionResponseDataBlock() != null) {
			crdBuffer = getConnectionResponseDataBlock().getBytes();
		}

		// cemiPropReadRequest
		byte[] cemiPropReadRequestBuffer = null;
		if (getCemiPropReadRequest() != null) {
			cemiPropReadRequestBuffer = getCemiPropReadRequest().getBytes();
		}
		// cemiTunnelRequest
		byte[] cemiTunnelRequestBuffer = null;
		if (getCemiTunnelRequest() != null) {
			cemiTunnelRequestBuffer = getCemiTunnelRequest().getBytes();
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
		totalLength += cemiPropReadRequestBuffer == null ? 0 : cemiPropReadRequestBuffer.length;
		totalLength += cemiTunnelRequestBuffer == null ? 0 : cemiTunnelRequestBuffer.length;
		totalLength += connectionHeaderBuffer == null ? 0 : connectionHeaderBuffer.length;
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

		// copy connectionHeader into payload
		if (connectionHeaderBuffer != null) {
			System.arraycopy(connectionHeaderBuffer, 0, payload, index, connectionHeaderBuffer.length);
			index += connectionHeaderBuffer.length;
		}

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

		// copy cemi prop read
		if (cemiPropReadRequestBuffer != null) {
			System.arraycopy(cemiPropReadRequestBuffer, 0, payload, index, cemiPropReadRequestBuffer.length);
			index += cemiPropReadRequestBuffer.length;
		}
		// copy cemi tunnel request
		if (cemiTunnelRequestBuffer != null) {
			System.arraycopy(cemiTunnelRequestBuffer, 0, payload, index, cemiTunnelRequestBuffer.length);
			index += cemiTunnelRequestBuffer.length;
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

		// connection response
		if (this.getCommunicationChannelId() >= 0x00) {
			stringBuilder.append("ChannelId = " + getCommunicationChannelId()).append("\n");
		}
		if (this.getConnectionStatus() != ConnectionStatus.UNSET) {
			stringBuilder.append("ConnectionStatus = " + getConnectionStatus().getValue()).append("\n").append("\n");
		}

		// DIB
		if (MapUtils.isNotEmpty(dibMap)) {
			for (final DescriptionInformationBlock dib : dibMap.values()) {
				stringBuilder.append(dib.toString()).append("\n").append("\n");
			}
		}

		// Connection Response Datablock
		if (connectionResponseDataBlock != null) {
			stringBuilder.append(connectionResponseDataBlock.toString()).append("\n").append("\n");
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

	public CemiPropReadRequest getCemiPropReadRequest() {
		return cemiPropReadRequest;
	}

	public void setCemiPropReadRequest(final CemiPropReadRequest cemiPropReadRequest) {
		this.cemiPropReadRequest = cemiPropReadRequest;
	}

	public ConnectionHeader getConnectionHeader() {
		return connectionHeader;
	}

	public void setConnectionHeader(final ConnectionHeader connectionHeader) {
		this.connectionHeader = connectionHeader;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(final Connection connection) {
		this.connection = connection;
	}

	public CemiTunnelRequest getCemiTunnelRequest() {
		return cemiTunnelRequest;
	}

	public void setCemiTunnelRequest(final CemiTunnelRequest cemiTunnelRequest) {
		this.cemiTunnelRequest = cemiTunnelRequest;
	}

	public ConnectionType getConnectionType() {

		final Structure tunnelingStructure = getStructureMap().get(StructureType.TUNNELING_CONNECTION);
		final Structure deviceManagementStructure = getStructureMap().get(StructureType.DEVICE_MGMT_CONNECTION);

		if ((tunnelingStructure != null) || (cemiPropReadRequest != null) || (cemiTunnelRequest != null)) {
			return ConnectionType.TUNNEL_CONNECTION;
		}

		if (deviceManagementStructure != null) {
			return ConnectionType.DEVICE_MGMT_CONNECTION;
		}

//		throw new RuntimeException("Cannot retrieve a connection type!");

		return ConnectionType.UNKNOWN;
	}

}
