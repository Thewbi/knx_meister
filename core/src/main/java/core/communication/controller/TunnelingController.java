package core.communication.controller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.communication.Connection;
import core.packets.CemiTunnelRequest;
import core.packets.ConnectionHeader;
import core.packets.KNXPacket;
import core.packets.ServiceIdentifier;

public class TunnelingController extends BaseController {

	private static final Logger LOG = LogManager.getLogger(TunnelingController.class);

	/**
	 * ctor
	 *
	 * @throws SocketException
	 * @throws UnknownHostException
	 */
	public TunnelingController() throws SocketException, UnknownHostException {
		super();
	}

	@Override
	public void knxPacket(final Connection connection, final DatagramSocket socket3671,
			final DatagramPacket datagramPacket, final KNXPacket knxPacket, final String label) throws IOException {

		switch (knxPacket.getHeader().getServiceIdentifier()) {

		case TUNNEL_REQUEST:
			if (knxPacket.getCemiTunnelRequest().getApci() == 0x4300) {
				throw new RuntimeException("Unknown message");
			}

			// send acknowledge
			final KNXPacket tunnelResponse = sendTunnelResponse(knxPacket, socket3671, datagramPacket);
			knxPacket.getConnection().sendResponse(tunnelResponse, datagramPacket.getSocketAddress());

			// Send a acknowledge
			final KNXPacket acknowledgeKNXPacket = new KNXPacket(knxPacket);

			final int sequenceCounter = knxPacket.getConnection().getSequenceCounter();
			acknowledgeKNXPacket.getConnectionHeader().setSequenceCounter(sequenceCounter);
//			acknowledgeKNXPacket.getConnectionHeader().setSequenceCounter(sequenceCounter + 2);
//			acknowledgeKNXPacket.getConnectionHeader().setSequenceCounter(sequenceCounter + 1);
			acknowledgeKNXPacket.getCemiTunnelRequest().setMessageCode(0x2e);
			acknowledgeKNXPacket.getCemiTunnelRequest().setSourceKNXAddress(getDevice().getPhysicalAddress());
//			acknowledgeKNXPacket.getCemiTunnelRequest().setSourceKNXAddress(0x1112);
			acknowledgeKNXPacket.getCemiTunnelRequest().setDestKNXAddress(0);
			acknowledgeKNXPacket.getCemiTunnelRequest().setCtrl1(0x91);
			acknowledgeKNXPacket.getCemiTunnelRequest().setLength(1);
			acknowledgeKNXPacket.getCemiTunnelRequest().setApci(0x0100); // <------- FIX for different application
																			// services!

			knxPacket.getConnection().sendResponse(acknowledgeKNXPacket, datagramPacket.getSocketAddress());

			// TODO ask the application layer service if the packet makes send and if so
			// send a confirmation

			final KNXPacket indicationKNXPacket = new KNXPacket();
			indicationKNXPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_REQUEST);

			indicationKNXPacket.setConnectionHeader(new ConnectionHeader());
			indicationKNXPacket.getConnectionHeader().setChannel(knxPacket.getConnectionHeader().getChannel());
			indicationKNXPacket.getConnectionHeader()
					.setSequenceCounter(knxPacket.getConnectionHeader().getSequenceCounter() + 1);
			// set reserved to 0x00
			indicationKNXPacket.getConnectionHeader().setReserved(0x00);

			final CemiTunnelRequest cemiTunnelRequest = new CemiTunnelRequest();
			// 0x29 == ind (the application layer sends response, the network layer converts
			// response to indication!)
			cemiTunnelRequest.setMessageCode(0x29);
			cemiTunnelRequest.setAdditionalInfoLength(0);
			cemiTunnelRequest.setCtrl1(0xB0);
			cemiTunnelRequest.setCtrl2(0xE0);
			cemiTunnelRequest.setSourceKNXAddress(getDevice().getPhysicalAddress());
			cemiTunnelRequest.setDestKNXAddress(0);
			cemiTunnelRequest.setLength(1);
			cemiTunnelRequest.setApci(0x0140); // IndAddrResp
			indicationKNXPacket.setCemiTunnelRequest(cemiTunnelRequest);
			knxPacket.getConnection().sendResponse(indicationKNXPacket, datagramPacket.getSocketAddress());
			break;

		default:
			getLogger().warn("Ignoring: " + knxPacket.getHeader().getServiceIdentifier().name());
			break;
		}

	}

	private KNXPacket sendTunnelResponse(final KNXPacket knxPacket, final DatagramSocket socket3671,
			final DatagramPacket datagramPacket) {

		final KNXPacket outKNXPacket = new KNXPacket();
		outKNXPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_RESPONSE);

		outKNXPacket.setConnectionHeader(new ConnectionHeader());
		outKNXPacket.getConnectionHeader().setChannel(knxPacket.getConnectionHeader().getChannel());
		outKNXPacket.getConnectionHeader().setSequenceCounter(knxPacket.getConnectionHeader().getSequenceCounter());
		// status OK
		outKNXPacket.getConnectionHeader().setReserved(0x00);

		return outKNXPacket;
	}

	@Override
	public boolean accepts(final DatagramPacket datagramPacket) {
		return false;
	}

	@Override
	public boolean accepts(final KNXPacket knxPacket) {
		switch (knxPacket.getHeader().getServiceIdentifier()) {
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
