package core.conversion;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import common.packets.KNXConnectionHeader;
import common.packets.KNXHeader;
import core.packets.CemiPropReadRequest;
import core.packets.CemiTunnelRequest;
import core.packets.DeviceManagement;
import core.packets.KNXPacket;

public class DeviceManagementKNXPacketConverter extends BaseKNXPacketConverter {

	private static final Logger LOG = LogManager.getLogger(DeviceManagementKNXPacketConverter.class);

	@Override
	public void convert(final byte[] source, final KNXPacket knxPacket) {

		int index = 0;

		// header
		final KNXHeader header = knxPacket.getHeader();
		header.fromBytes(source, index);
		index += header.getLength();

		if (!accept(header)) {
			return;
		}

		// connection header
		KNXConnectionHeader connectionHeader = knxPacket.getConnectionHeader();
		if (connectionHeader == null) {
			connectionHeader = new KNXConnectionHeader();
			knxPacket.setConnectionHeader(connectionHeader);
		}
		connectionHeader.fromBytes(source, index);
		index += connectionHeader.getLength();

		switch (header.getServiceIdentifier()) {

		// Device Management Specification- 4.2.6 DEVICE_CONFIGURATION_REQUEST
		// 0x0310
		case DEVICE_CONFIGURATION_REQUEST:

			final int messageCode = (source[index] & 0xFF);

			switch (messageCode) {

			case DeviceManagement.M_PROP_READ_REQ_VALUE:
				final CemiPropReadRequest cemiPropReadRequest = new CemiPropReadRequest();
				cemiPropReadRequest.fromBytes(source, index);
				index += cemiPropReadRequest.getLength();
				knxPacket.setCemiPropReadRequest(cemiPropReadRequest);
				break;

			case DeviceManagement.T_DATA_CONNECTED_REQ_VALUE:
				final CemiTunnelRequest cemiTunnelRequest = new CemiTunnelRequest();
				cemiTunnelRequest.fromBytes(source, index);
				index += cemiTunnelRequest.getLength();
				knxPacket.setCemiTunnelRequest(cemiTunnelRequest);
				break;

			default:
				throw new RuntimeException("Unknown messageCode=" + messageCode);
			}

			break;

		default:
			throw new RuntimeException("Unknown type: " + header.getServiceIdentifier());
		}
	}

	@Override
	public boolean accept(final KNXHeader header) {
		switch (header.getServiceIdentifier()) {

		// Device Management Specification- 4.2.6 DEVICE_CONFIGURATION_REQUEST
		// 0x0310
		case DEVICE_CONFIGURATION_REQUEST:
			return true;

		default:
			return false;
		}
	}

	@Override
	protected Logger getLogger() {
		return LOG;
	}

}
