package core.communication.controller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.communication.Connection;
import core.packets.ConnectionHeader;
import core.packets.KNXPacket;
import core.packets.ServiceIdentifier;

public class DeviceManagementController extends BaseController {

	private static final Logger LOG = LogManager.getLogger(DeviceManagementController.class);

	/**
	 * ctor
	 *
	 * @throws SocketException
	 * @throws UnknownHostException
	 */
	public DeviceManagementController() throws SocketException, UnknownHostException {
		super();
	}

	@Override
	public void knxPacket(final Connection connection, final DatagramSocket socket3671,
			final DatagramPacket datagramPacket, final KNXPacket knxPacket, final String label) throws IOException {

		switch (knxPacket.getHeader().getServiceIdentifier()) {

		case DEVICE_CONFIGURATION_REQUEST:
			final short propertyKey = knxPacket.getCemiPropReadRequest().getPropertyId();
			final short propertyValue = getDevice().getProperties().get(propertyKey);

			final byte[] responseData = new byte[2];
			responseData[0] = (byte) ((propertyValue >> 8) & 0xFF);
			responseData[1] = (byte) (propertyValue & 0xFF);

			// send the current configuration value back to the sender
			final KNXPacket deviceConfigurationRequestAnswer = new KNXPacket(knxPacket);
			// change the message code from 0xfc to 0xfb
			deviceConfigurationRequestAnswer.getCemiPropReadRequest().setMessageCode((short) 0xfb);
			deviceConfigurationRequestAnswer.getCemiPropReadRequest().setResponseData(responseData);
			connection.sendResponse(deviceConfigurationRequestAnswer, datagramPacket.getSocketAddress());

			// send acknowledge
			final KNXPacket knxPacketAck = new KNXPacket();
			knxPacketAck.getHeader().setServiceIdentifier(ServiceIdentifier.DEVICE_CONFIGURATION_ACK);

			knxPacketAck.setConnectionHeader(new ConnectionHeader());
			knxPacketAck.getConnectionHeader().setChannel(knxPacket.getConnectionHeader().getChannel());
			knxPacketAck.getConnectionHeader().setSequenceCounter(knxPacket.getConnectionHeader().getSequenceCounter());
			// status OK
			knxPacketAck.getConnectionHeader().setReserved(0x00);
			connection.sendResponse(knxPacketAck, datagramPacket.getSocketAddress());
			break;

		default:
			getLogger().warn("Ignoring: " + knxPacket.getHeader().getServiceIdentifier().name());
			break;
		}

	}

	@Override
	public boolean accepts(final DatagramPacket datagramPacket) {
		return false;
	}

	@Override
	public boolean accepts(final KNXPacket knxPacket) {
		switch (knxPacket.getHeader().getServiceIdentifier()) {
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
