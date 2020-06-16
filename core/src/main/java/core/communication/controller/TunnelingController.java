package core.communication.controller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.common.Utils;
import core.communication.Connection;
import core.data.sending.DataSender;
import core.packets.CemiTunnelRequest;
import core.packets.ConnectionHeader;
import core.packets.ConnectionType;
import core.packets.HPAIStructure;
import core.packets.KNXPacket;
import core.packets.PropertyId;
import core.packets.ServiceIdentifier;
import core.packets.StructureType;

public class TunnelingController extends BaseController {

	private static final int HOPS = 0x60;

	private static final int PRIO_SYSTEM = 0xB0;

	private static final int DEVICE_DESCRIPTION_READ_APCI = 0x4300;

	private static final int DEVICE_READ_APCI = 0x0100;

	private static final Logger LOG = LogManager.getLogger(TunnelingController.class);

	private KNXPacket indicationKNXPacket;

	private KNXPacket acknowledgeKNXPacket;

	private DataSender dataSender;

	/**
	 * ctor
	 *
	 * @throws SocketException
	 * @throws UnknownHostException
	 */
	public TunnelingController(final String localInetAddress) throws SocketException, UnknownHostException {
		super(localInetAddress);
	}

	@Override
	public void knxPacket(final Connection connection, final DatagramSocket datagramSocket,
			final DatagramPacket datagramPacket, final KNXPacket knxPacket, final String label) throws IOException {

		HPAIStructure hpaiStructure = null;
		final InetAddress inetAddress = null;
		final int port = -1;
		Connection packetConnection = null;
		int communicationChannelId = 0;
		HPAIStructure controlEndpointHPAIStructure = null;
		HPAIStructure dataEndpointHPAIStructure = null;

		switch (knxPacket.getHeader().getServiceIdentifier()) {

		case CONNECT_REQUEST:
			// if the connect request contains a CRI Tunneling Connection, this connection
			// should be handled by the tunneling controller
			if (!knxPacket.getStructureMap().containsKey(StructureType.TUNNELING_CONNECTION)) {
				break;
			}
			final ConnectionType connectionType = knxPacket.getConnectionType();
			final Connection newConnection = getConnectionManager().createNewConnection(datagramSocket, connectionType);

			controlEndpointHPAIStructure = (HPAIStructure) knxPacket.getStructureMap()
					.get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);
			newConnection.setControlEndpoint(controlEndpointHPAIStructure);

			dataEndpointHPAIStructure = (HPAIStructure) knxPacket.getStructureMap()
					.get(StructureType.HPAI_DATA_ENDPOINT_UDP);
			newConnection.setDataEndpoint(dataEndpointHPAIStructure);

			hpaiStructure = (HPAIStructure) knxPacket.getStructureMap().get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);
			final InetAddress controlInetAddress = InetAddress.getByAddress(hpaiStructure.getIpAddress());
			final int controlPort = hpaiStructure.getPort() & 0xFFFF;

			final KNXPacket sendConnectionResponse = sendConnectionResponse(datagramSocket, datagramPacket,
					controlInetAddress, controlPort, knxPacket.getConnectionType());
			sendConnectionResponse.setCommunicationChannelId(newConnection.getId());
			newConnection.sendResponse(sendConnectionResponse, new InetSocketAddress(controlInetAddress, controlPort));

			startThread(newConnection);
			break;

		case CONNECTIONSTATE_REQUEST:
			// make sure this is a tunneling connection
			communicationChannelId = knxPacket.getCommunicationChannelId();
			packetConnection = getConnectionManager().retrieveConnection(communicationChannelId);
			if (packetConnection.getConnectionType() != ConnectionType.TUNNEL_CONNECTION) {
				return;
			}

			LOG.info("CONNECTIONSTATE_REQUEST connection: " + communicationChannelId);

			final KNXPacket sendConnectionStateResponse = sendConnectionStateResponse(datagramSocket, datagramPacket,
					knxPacket, inetAddress, port);

			controlEndpointHPAIStructure = (HPAIStructure) knxPacket.getStructureMap()
					.get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);

			final InetSocketAddress socketAddr = new InetSocketAddress(
					controlEndpointHPAIStructure.getIpAddressAsObject(), controlEndpointHPAIStructure.getPort());

			connection.sendResponse(sendConnectionStateResponse, socketAddr);
			break;

		case DISCONNECT_REQUEST:
			// make sure this is a tunneling connection
			communicationChannelId = knxPacket.getCommunicationChannelId();
			packetConnection = getConnectionManager().retrieveConnection(communicationChannelId);
			if (packetConnection == null || packetConnection.getConnectionType() != ConnectionType.TUNNEL_CONNECTION) {
				return;
			}

			getConnectionManager().closeConnection(knxPacket.getCommunicationChannelId());

			final KNXPacket sendDisconnetResponse = sendDisconnetResponse(datagramSocket, datagramPacket, knxPacket,
					inetAddress, port);

			connection.sendResponse(sendDisconnetResponse, datagramPacket.getSocketAddress());
			break;

		// 0x0420
		case TUNNEL_REQUEST:

			final int tpci = knxPacket.getCemiTunnelRequest().getTpci();
			final int apci = knxPacket.getCemiTunnelRequest().getApci();

			if (tpci == 0 && apci == 0) {

				// send acknowledge
				final KNXPacket tunnelResponse = sendTunnelResponse(knxPacket, datagramSocket, datagramPacket);
				knxPacket.getConnection().sendResponse(tunnelResponse, datagramPacket.getSocketAddress());

			} else {

				processTunnelingRequest(connection, datagramSocket, datagramPacket, knxPacket, label);

			}
			break;

		// 0x0421
		case TUNNEL_RESPONSE:
			if (acknowledgeKNXPacket != null) {
				knxPacket.getConnection().sendResponse(acknowledgeKNXPacket, datagramPacket.getSocketAddress());
				acknowledgeKNXPacket = null;
			}
			if (indicationKNXPacket != null) {
				knxPacket.getConnection().sendResponse(indicationKNXPacket, datagramPacket.getSocketAddress());
				indicationKNXPacket = null;
			}
			break;

		default:
			getLogger().warn("Ignoring: " + knxPacket.getHeader().getServiceIdentifier().name());
			break;
		}
	}

	private void startThread(final Connection connection) {

		final Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					// in order to be compatible with the ETS5 Bus-Monitor, the tunnel requests can
					// only be send
					// to the ETS5 Bus-Monitor after the Bus-Monitor did ask for the ConnectionState
					// and that
					// request was answered with the answer OK.
					//
					// The sequence is:
					// 1. The Bus-Monitor establishes a tunneling connection with the device.
					// 2. The device returns the ID of the tunneling connection.
					// 3. The Bus-Monitor requests the ConnectionState of the tunneling connection
					// using the ID from step 2.
					// 4. The device answers with OK (the tunneling connection is in an OK state).
					// 5. The device now can use the tunneling connection to send data to the
					// Bus-Monitor in the form
					// of tunneling requests
					//
					// If the thread does not sleep but sends a tunneling request immediately, the
					// Bus-Monitor receives the tunneling request before it has performed the
					// Connection State check. If any requests arrives before the connection state
					// check, the Bus-Monitor will disconnect the tunneling connection immediately.
					LOG.info("Sleeping 5000 ...");
					Thread.sleep(5000);
				} catch (final InterruptedException e) {
					LOG.error(e.getMessage(), e);
				}

				while (true) {

					LOG.info("Sending data ...");

					dataSender.send(connection);

					try {
						Thread.sleep(2000);
					} catch (final InterruptedException e) {
						LOG.error(e.getMessage(), e);
						return;
					}
				}
			}
		});
		thread.start();
	}

	private void processTunnelingRequest(final Connection connection, final DatagramSocket datagramSocket,
			final DatagramPacket datagramPacket, final KNXPacket knxPacket, final String label) throws IOException {

		KNXPacket tunnelResponse = null;
		KNXPacket confirmKNXPacket = null;

		int sequenceCounter = 0;

		switch (knxPacket.getCemiTunnelRequest().getTpci()) {
		case 0x43:
			// send acknowledge
			tunnelResponse = sendTunnelResponse(knxPacket, datagramSocket, datagramPacket);
			knxPacket.getConnection().sendResponse(tunnelResponse, datagramPacket.getSocketAddress());

			// all four packets req+OK, ind+OK belong to the same sequence counter value
			sequenceCounter = knxPacket.getConnection().getSequenceCounter();

			// send message acknowledge, send answer message, send confirm
			confirmKNXPacket = new KNXPacket(knxPacket);
			confirmKNXPacket.getConnectionHeader().setSequenceCounter(sequenceCounter);
			confirmKNXPacket.getCemiTunnelRequest().setMessageCode(CONFIRM_PRIMITIVE);
			confirmKNXPacket.getCemiTunnelRequest().setSourceKNXAddress(getDevice().getPhysicalAddress());
			confirmKNXPacket.getCemiTunnelRequest()
					.setDestKNXAddress(knxPacket.getCemiTunnelRequest().getDestKNXAddress());
			confirmKNXPacket.getCemiTunnelRequest().setCtrl1(0x90);
			confirmKNXPacket.getCemiTunnelRequest().setLength(1);
			// <------- FIX for different application services!
			confirmKNXPacket.getCemiTunnelRequest().setApci(DEVICE_DESCRIPTION_READ_APCI);
			knxPacket.getConnection().sendResponse(confirmKNXPacket, datagramPacket.getSocketAddress());

			indicationKNXPacket = retrieveDeviceDescriptionReadAPCIIndicationPacket(knxPacket, sequenceCounter + 1);

			acknowledgeKNXPacket = retrieveDeviceDescriptionReadAPCIAcknowledgePacket(knxPacket, sequenceCounter);

			break;

		case 0x01:
			// send acknowledge
			tunnelResponse = sendTunnelResponse(knxPacket, datagramSocket, datagramPacket);
			knxPacket.getConnection().sendResponse(tunnelResponse, datagramPacket.getSocketAddress());

			sequenceCounter = knxPacket.getConnection().getSequenceCounter();

			// send message acknowledge, send answer message
			confirmKNXPacket = new KNXPacket(knxPacket);
			confirmKNXPacket.getConnectionHeader().setSequenceCounter(sequenceCounter);
			confirmKNXPacket.getCemiTunnelRequest().setMessageCode(CONFIRM_PRIMITIVE);
			confirmKNXPacket.getCemiTunnelRequest().setSourceKNXAddress(getDevice().getPhysicalAddress());
			confirmKNXPacket.getCemiTunnelRequest()
					.setDestKNXAddress(knxPacket.getCemiTunnelRequest().getSourceKNXAddress());
			confirmKNXPacket.getCemiTunnelRequest().setCtrl1(0x91);
			confirmKNXPacket.getCemiTunnelRequest().setLength(1);
			// <------- FIX for different application services!
			confirmKNXPacket.getCemiTunnelRequest().setApci(DEVICE_READ_APCI);
			knxPacket.getConnection().sendResponse(confirmKNXPacket, datagramPacket.getSocketAddress());

			indicationKNXPacket = retrieveDeviceReadAPCIIndicationPacket(knxPacket);
			break;

		// TUNNELING CONNECTION REQUEST
		case 0x80:
			// TUNNELING DISCONNECT REQUEST
		case 0x81:
			// send acknowledge
			final KNXPacket resKNXPacket = new KNXPacket(knxPacket);
			resKNXPacket.setCemiPropReadRequest(null);
			resKNXPacket.setCemiTunnelRequest(null);
			resKNXPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_RESPONSE);
			knxPacket.getConnection().sendResponse(resKNXPacket, datagramPacket.getSocketAddress());
			break;

		default:
			throw new RuntimeException("Unknown message TPCI=" + knxPacket.getCemiTunnelRequest().getTpci() + " APCI="
					+ knxPacket.getCemiTunnelRequest().getApci());
		}
	}

	private KNXPacket retrieveDeviceReadAPCIIndicationPacket(final KNXPacket knxPacket) {

		final CemiTunnelRequest cemiTunnelRequest = new CemiTunnelRequest();
		// 0x29 == ind (the application layer sends response, the network layer converts
		// response to indication!)
		cemiTunnelRequest.setMessageCode(INDICATION_PRIMITIVE);
		cemiTunnelRequest.setAdditionalInfoLength(0);
		cemiTunnelRequest.setCtrl1(0xB0);
		cemiTunnelRequest.setCtrl2(0xE0);
		cemiTunnelRequest.setSourceKNXAddress(getDevice().getPhysicalAddress());
		cemiTunnelRequest.setDestKNXAddress(0);
		cemiTunnelRequest.setTpci(0x01);
		cemiTunnelRequest.setApci(0x40);

		final ConnectionHeader connectionHeader = new ConnectionHeader();
		connectionHeader.setChannel(knxPacket.getConnectionHeader().getChannel());
		connectionHeader.setSequenceCounter(knxPacket.getConnectionHeader().getSequenceCounter() + 1);
		connectionHeader.setReserved(0x00);

		final KNXPacket indicationKNXPacket = new KNXPacket();
		indicationKNXPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_REQUEST);
		indicationKNXPacket.setConnectionHeader(connectionHeader);
		indicationKNXPacket.setCemiTunnelRequest(cemiTunnelRequest);

		return indicationKNXPacket;
	}

	private KNXPacket retrieveDeviceDescriptionReadAPCIAcknowledgePacket(final KNXPacket knxPacket,
			final int sequenceCounter) {

		final CemiTunnelRequest cemiTunnelRequest = new CemiTunnelRequest();
		// 0x29 == ind (the application layer sends response, the network layer converts
		// response to indication!)
		cemiTunnelRequest.setMessageCode(REQUEST_PRIMITIVE);
		cemiTunnelRequest.setAdditionalInfoLength(0);
		cemiTunnelRequest.setCtrl1(PRIO_SYSTEM);
		cemiTunnelRequest.setCtrl2(HOPS);
		cemiTunnelRequest.setSourceKNXAddress(knxPacket.getCemiTunnelRequest().getDestKNXAddress());
		cemiTunnelRequest.setDestKNXAddress(getDevice().getPhysicalAddress());
		cemiTunnelRequest.setTpci(0xc2);

		final ConnectionHeader connectionHeader = new ConnectionHeader();
		connectionHeader.setChannel(knxPacket.getConnectionHeader().getChannel());
		connectionHeader.setSequenceCounter(sequenceCounter + 1);
		connectionHeader.setReserved(0x00);

		// send the answer, that the server wanted
		final KNXPacket acknowledgeKNXPacket = new KNXPacket();
		acknowledgeKNXPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_REQUEST);
		acknowledgeKNXPacket.setConnectionHeader(connectionHeader);
		acknowledgeKNXPacket.setCemiTunnelRequest(cemiTunnelRequest);

		return acknowledgeKNXPacket;
	}

	private KNXPacket retrieveDeviceDescriptionReadAPCIIndicationPacket(final KNXPacket knxPacket,
			final int sequenceCounter) {

		final short deviceDescriptor = getDevice().getProperties()
				.get((short) PropertyId.PID_DEVICE_DESCRIPTOR.getValue());
		final byte[] payload = Utils.shortToByteArray(deviceDescriptor);

		final CemiTunnelRequest cemiTunnelRequest = new CemiTunnelRequest();
		// 0x29 == ind (the application layer sends response, the network layer converts
		// response to indication!)
		cemiTunnelRequest.setMessageCode(INDICATION_PRIMITIVE);
		cemiTunnelRequest.setAdditionalInfoLength(0);
		cemiTunnelRequest.setCtrl1(0x90);
		cemiTunnelRequest.setCtrl2(0x60);
		cemiTunnelRequest.setSourceKNXAddress(knxPacket.getCemiTunnelRequest().getDestKNXAddress());
		cemiTunnelRequest.setDestKNXAddress(getDevice().getPhysicalAddress());
		cemiTunnelRequest.setTpci(0x43);
		cemiTunnelRequest.setApci(0x40);
		cemiTunnelRequest.setPayloadBytes(payload);

		// send the answer, that the server wanted
		final KNXPacket indicationKNXPacket = new KNXPacket();
		indicationKNXPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_REQUEST);
		indicationKNXPacket.setConnectionHeader(new ConnectionHeader());
		indicationKNXPacket.getConnectionHeader().setChannel(knxPacket.getConnectionHeader().getChannel());
		indicationKNXPacket.getConnectionHeader().setSequenceCounter(sequenceCounter + 1);
		indicationKNXPacket.getConnectionHeader().setReserved(0x00);
		indicationKNXPacket.setCemiTunnelRequest(cemiTunnelRequest);

		return indicationKNXPacket;
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
		case CONNECT_REQUEST:
		case CONNECTIONSTATE_REQUEST:
		case DISCONNECT_REQUEST:
			return true;

		default:
			return false;
		}
	}

	@Override
	protected Logger getLogger() {
		return LOG;
	}

	public DataSender getDataSender() {
		return dataSender;
	}

	public void setDataSender(final DataSender dataSender) {
		this.dataSender = dataSender;
	}

}
