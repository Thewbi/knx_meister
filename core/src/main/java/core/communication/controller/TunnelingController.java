package core.communication.controller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.common.Utils;
import core.communication.Connection;
import core.packets.CemiTunnelRequest;
import core.packets.ConnectionHeader;
import core.packets.KNXPacket;
import core.packets.PropertyId;
import core.packets.ServiceIdentifier;

public class TunnelingController extends BaseController {

	private static final int DEVICE_DESCRIPTION_RESPONSE_APCI = 0x4340;

	private static final int INDICATION_PRIMITIVE = 0x29;

	private static final int IND_ADDR_RESPONSE_APCI = 0x0140;

	private static final int DEVICE_DESCRIPTION_READ_APCI = 0x4300;

	private static final int DEVICE_READ_APCI = 0x0100;

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
	public void knxPacket(final Connection connection, final DatagramSocket datagramSocket,
			final DatagramPacket datagramPacket, final KNXPacket knxPacket, final String label) throws IOException {

		switch (knxPacket.getHeader().getServiceIdentifier()) {

		case TUNNEL_REQUEST:

			final int apci = knxPacket.getCemiTunnelRequest().getApci();

			if (apci == 0) {

				// send acknowledge
				final KNXPacket tunnelResponse = sendTunnelResponse(knxPacket, datagramSocket, datagramPacket);
				knxPacket.getConnection().sendResponse(tunnelResponse, datagramPacket.getSocketAddress());

			} else {

				processTunnelingRequest(connection, datagramSocket, datagramPacket, knxPacket, label);

			}
			break;

		default:
			getLogger().warn("Ignoring: " + knxPacket.getHeader().getServiceIdentifier().name());
			break;
		}
	}

	private void processTunnelingRequest(final Connection connection, final DatagramSocket datagramSocket,
			final DatagramPacket datagramPacket, final KNXPacket knxPacket, final String label) throws IOException {

		KNXPacket tunnelResponse = null;
		KNXPacket acknowledgeKNXPacket = null;
		KNXPacket indicationKNXPacket = null;
		CemiTunnelRequest cemiTunnelRequest = null;
		int sequenceCounter = 0;

		switch (knxPacket.getCemiTunnelRequest().getApci()) {
		case DEVICE_DESCRIPTION_READ_APCI:
			// send acknowledge
			tunnelResponse = sendTunnelResponse(knxPacket, datagramSocket, datagramPacket);
			knxPacket.getConnection().sendResponse(tunnelResponse, datagramPacket.getSocketAddress());

			sequenceCounter = knxPacket.getConnection().getSequenceCounter();

			// send message acknowledge, send answer message
			acknowledgeKNXPacket = new KNXPacket(knxPacket);
			acknowledgeKNXPacket.getConnectionHeader().setSequenceCounter(sequenceCounter + 1);
			acknowledgeKNXPacket.getCemiTunnelRequest().setMessageCode(0x2e);
			acknowledgeKNXPacket.getCemiTunnelRequest().setSourceKNXAddress(getDevice().getPhysicalAddress());
			acknowledgeKNXPacket.getCemiTunnelRequest()
					.setDestKNXAddress(knxPacket.getCemiTunnelRequest().getDestKNXAddress());
			acknowledgeKNXPacket.getCemiTunnelRequest().setCtrl1(0x90);
			acknowledgeKNXPacket.getCemiTunnelRequest().setLength(1);
			// <------- FIX for different application services!
			acknowledgeKNXPacket.getCemiTunnelRequest().setApci(DEVICE_DESCRIPTION_READ_APCI);
			knxPacket.getConnection().sendResponse(acknowledgeKNXPacket, datagramPacket.getSocketAddress());

			final short deviceDescriptor = getDevice().getProperties()
					.get((short) PropertyId.PID_DEVICE_DESCRIPTOR.getValue());
			final byte[] payload = Utils.shortToByteArray(deviceDescriptor);

			// send the answer, that the server wanted
			indicationKNXPacket = new KNXPacket();
			indicationKNXPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_REQUEST);

			indicationKNXPacket.setConnectionHeader(new ConnectionHeader());
			indicationKNXPacket.getConnectionHeader().setChannel(knxPacket.getConnectionHeader().getChannel());
			indicationKNXPacket.getConnectionHeader().setSequenceCounter(sequenceCounter + 1);
			// set reserved to 0x00
			indicationKNXPacket.getConnectionHeader().setReserved(0x00);

			cemiTunnelRequest = new CemiTunnelRequest();
			// 0x29 == ind (the application layer sends response, the network layer converts
			// response to indication!)
			cemiTunnelRequest.setMessageCode(INDICATION_PRIMITIVE);
			cemiTunnelRequest.setAdditionalInfoLength(0);
			cemiTunnelRequest.setCtrl1(0x90);
			cemiTunnelRequest.setCtrl2(0x60);
			cemiTunnelRequest.setSourceKNXAddress(knxPacket.getCemiTunnelRequest().getDestKNXAddress());
			cemiTunnelRequest.setDestKNXAddress(getDevice().getPhysicalAddress());
			cemiTunnelRequest.setLength(1 + payload.length);
			cemiTunnelRequest.setApci(DEVICE_DESCRIPTION_RESPONSE_APCI);
			cemiTunnelRequest.setPayloadBytes(payload);

			indicationKNXPacket.setCemiTunnelRequest(cemiTunnelRequest);
			knxPacket.getConnection().sendResponse(indicationKNXPacket, datagramPacket.getSocketAddress());
			break;

		case DEVICE_READ_APCI:
			// send acknowledge
			tunnelResponse = sendTunnelResponse(knxPacket, datagramSocket, datagramPacket);
			knxPacket.getConnection().sendResponse(tunnelResponse, datagramPacket.getSocketAddress());

			sequenceCounter = knxPacket.getConnection().getSequenceCounter();

			// send message acknowledge, send answer message
			acknowledgeKNXPacket = new KNXPacket(knxPacket);
			acknowledgeKNXPacket.getConnectionHeader().setSequenceCounter(sequenceCounter);
			acknowledgeKNXPacket.getCemiTunnelRequest().setMessageCode(0x2e);
			acknowledgeKNXPacket.getCemiTunnelRequest().setSourceKNXAddress(getDevice().getPhysicalAddress());
			acknowledgeKNXPacket.getCemiTunnelRequest()
					.setDestKNXAddress(knxPacket.getCemiTunnelRequest().getSourceKNXAddress());
			acknowledgeKNXPacket.getCemiTunnelRequest().setCtrl1(0x91);
			acknowledgeKNXPacket.getCemiTunnelRequest().setLength(1);
			// <------- FIX for different application services!
			acknowledgeKNXPacket.getCemiTunnelRequest().setApci(DEVICE_READ_APCI);
			knxPacket.getConnection().sendResponse(acknowledgeKNXPacket, datagramPacket.getSocketAddress());

			// TODO ask the application layer service if the packet makes send and if so
			// send a confirmation

			indicationKNXPacket = new KNXPacket();
			indicationKNXPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_REQUEST);

			indicationKNXPacket.setConnectionHeader(new ConnectionHeader());
			indicationKNXPacket.getConnectionHeader().setChannel(knxPacket.getConnectionHeader().getChannel());
			indicationKNXPacket.getConnectionHeader()
					.setSequenceCounter(knxPacket.getConnectionHeader().getSequenceCounter() + 1);
			// set reserved to 0x00
			indicationKNXPacket.getConnectionHeader().setReserved(0x00);

			cemiTunnelRequest = new CemiTunnelRequest();
			// 0x29 == ind (the application layer sends response, the network layer converts
			// response to indication!)
			cemiTunnelRequest.setMessageCode(INDICATION_PRIMITIVE);
			cemiTunnelRequest.setAdditionalInfoLength(0);
			cemiTunnelRequest.setCtrl1(0xB0);
			cemiTunnelRequest.setCtrl2(0xE0);
			cemiTunnelRequest.setSourceKNXAddress(getDevice().getPhysicalAddress());
			cemiTunnelRequest.setDestKNXAddress(0);
			cemiTunnelRequest.setLength(1);
			cemiTunnelRequest.setApci(IND_ADDR_RESPONSE_APCI);

			indicationKNXPacket.setCemiTunnelRequest(cemiTunnelRequest);
			knxPacket.getConnection().sendResponse(indicationKNXPacket, datagramPacket.getSocketAddress());
			break;

		default:
			throw new RuntimeException("Unknown message APCI=" + knxPacket.getCemiTunnelRequest().getApci());
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
