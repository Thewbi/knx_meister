package core.conversion;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.packets.CemiTunnelRequest;
import core.packets.ConnectionHeader;
import core.packets.Header;
import core.packets.KNXPacket;

public class TunnelKNXPacketConverter extends BaseKNXPacketConverter {

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

			/** AKA TUNNELLING_REQUEST */
			case TUNNEL_REQUEST:

			final CemiTunnelRequest cemiTunnelRequest = new CemiTunnelRequest();
			cemiTunnelRequest.fromBytes(source, index);
			index += cemiTunnelRequest.getLength();
			knxPacket.setCemiTunnelRequest(cemiTunnelRequest);
			break;

		case TUNNEL_RESPONSE:
			break;

		default:
			throw new RuntimeException("Unknown type: " + header.getServiceIdentifier());
		}
	}

	@Override
	public boolean accept(final Header header) {
		switch (header.getServiceIdentifier()) {
		case TUNNEL_REQUEST:
		case TUNNEL_RESPONSE:
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
