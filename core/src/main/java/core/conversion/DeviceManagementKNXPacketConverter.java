package core.conversion;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.packets.CemiPropReadRequest;
import core.packets.ConnectionHeader;
import core.packets.Header;
import core.packets.KNXPacket;

public class DeviceManagementKNXPacketConverter extends BaseKNXPacketConverter {

	private static final Logger LOG = LogManager.getLogger(DeviceManagementKNXPacketConverter.class);

	@Override
	public void convert(final byte[] source, final KNXPacket knxPacket) {

		int index = 0;

		// header
		final Header header = knxPacket.getHeader();
		header.fromBytes(source, index);
		index += header.getLength();

		if (!accept(header)) {
			return;
		}

		// connection header
		ConnectionHeader connectionHeader = knxPacket.getConnectionHeader();
		if (connectionHeader == null) {
			connectionHeader = new ConnectionHeader();
			knxPacket.setConnectionHeader(connectionHeader);
		}
		connectionHeader.fromBytes(source, index);
		index += connectionHeader.getLength();

		switch (header.getServiceIdentifier()) {

		// Device Management Specification- 4.2.6 DEVICE_CONFIGURATION_REQUEST
		case DEVICE_CONFIGURATION_REQUEST:
//			// communication channel
//			knxPacket.setCommunicationChannelId(source[index++]);
//
//			// skip reserved byte
//			index++;

			final CemiPropReadRequest cemiPropReadRequest = new CemiPropReadRequest();
			cemiPropReadRequest.fromBytes(source, index);
			knxPacket.setCemiPropReadRequest(cemiPropReadRequest);
			break;

		default:
			throw new RuntimeException("Unknown type: " + header.getServiceIdentifier());
		}

	}

	@Override
	public boolean accept(final Header header) {
		switch (header.getServiceIdentifier()) {

		// Device Management Specification- 4.2.6 DEVICE_CONFIGURATION_REQUEST
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
