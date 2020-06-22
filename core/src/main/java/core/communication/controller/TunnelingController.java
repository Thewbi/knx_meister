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

import core.common.NetworkUtils;
import core.common.Utils;
import core.communication.Connection;
import core.packets.CemiTunnelRequest;
import core.packets.ConnectionHeader;
import core.packets.ConnectionType;
import core.packets.HPAIStructure;
import core.packets.KNXPacket;
import core.packets.PropertyId;
import core.packets.ServiceIdentifier;
import core.packets.StructureType;

public class TunnelingController extends BaseController {

	private static final int DEVICE_DESCRIPTION_READ_TCPI = 0x43;

	private static final int GROUP_VALUE_WRITE_OR_WRITE = 0x00;

	private static final int HOPS = 0x60;

	private static final int PRIO_SYSTEM = 0xB0;

	private static final int DEVICE_DESCRIPTION_READ_APCI = 0x4300;

	private static final int DEVICE_READ_APCI = 0x0100;

	private static final Logger LOG = LogManager.getLogger(TunnelingController.class);

	private KNXPacket indicationKNXPacket;

	private KNXPacket acknowledgeKNXPacket;

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

		// 0x0205
		case CONNECT_REQUEST:
			// if the connect request contains a CRI Tunneling Connection, this connection
			// should be handled by the tunneling controller
			if (!knxPacket.getStructureMap().containsKey(StructureType.TUNNELING_CONNECTION)) {
				break;
			}

			final ConnectionType connectionType = knxPacket.getConnectionType();

			// create the new connection
			final Connection newConnection = getConnectionManager().createNewConnection(datagramSocket, connectionType);

			controlEndpointHPAIStructure = (HPAIStructure) knxPacket.getStructureMap()
					.get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);
			newConnection.setControlEndpoint(controlEndpointHPAIStructure);

			dataEndpointHPAIStructure = (HPAIStructure) knxPacket.getStructureMap()
					.get(StructureType.HPAI_DATA_ENDPOINT_UDP);
			newConnection.setDataEndpoint(dataEndpointHPAIStructure);

			// construct the acknowledge
			hpaiStructure = (HPAIStructure) knxPacket.getStructureMap().get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);
			final InetAddress controlInetAddress = InetAddress.getByAddress(hpaiStructure.getIpAddress());
			final int controlPort = hpaiStructure.getPort() & 0xFFFF;

			final KNXPacket sendConnectionResponse = retrieveConnectionResponse(knxPacket.getConnectionType());
			sendConnectionResponse.setCommunicationChannelId(newConnection.getId());

			// send the acknowledge
			newConnection.sendResponse(sendConnectionResponse, new InetSocketAddress(controlInetAddress, controlPort));

//			sendTunnelRequestConnect(newConnection);

			startThread(getClass().getName() + " CONNECT_REQUEST", newConnection);
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

//			startThread(packetConnection);
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

//			final int tpci = knxPacket.getCemiTunnelRequest().getTpci();
//			final int apci = knxPacket.getCemiTunnelRequest().getApci();

//			if (tpci == 0 && apci == 0) {
//
//				// send acknowledge
//				final KNXPacket tunnelResponse = sendTunnelResponse(knxPacket, datagramSocket, datagramPacket);
//				knxPacket.getConnection().sendResponse(tunnelResponse, datagramPacket.getSocketAddress());
//
//			} else {
//
//				processTunnelingRequest(connection, datagramSocket, datagramPacket, knxPacket, label);
//
//			}
			processTunnelingRequest(connection, datagramSocket, datagramPacket, knxPacket, label);
			break;

		// 0x0421
		case TUNNEL_RESPONSE:
//			if (acknowledgeKNXPacket != null) {
//				knxPacket.getConnection().sendResponse(acknowledgeKNXPacket, datagramPacket.getSocketAddress());
//				acknowledgeKNXPacket = null;
//			}
//			if (indicationKNXPacket != null) {
//				knxPacket.getConnection().sendResponse(indicationKNXPacket, datagramPacket.getSocketAddress());
//				indicationKNXPacket = null;
//			}
			if (knxPacket.getConnectionHeader() != null && knxPacket.getConnectionHeader().getReserved() == 0x21) {

				final String msg = "E_CONNECTION_ID: 0x21, // - The KNXnet/IP server device could not find an active data connection with the given ID";
//				throw new RuntimeException(msg);

				LOG.error(msg);
			}
			break;

		default:
			getLogger().warn("Ignoring: " + knxPacket.getHeader().getServiceIdentifier().name());
			break;
		}
	}

	public void sendTunnelRequestConnect(final Connection connection) {

		LOG.info("sendTunnelRequestConnect() ...");

		final ConnectionHeader connectionHeader = new ConnectionHeader();
//		connectionHeader.setChannel((short) connection.getId());
//		connectionHeader.setSequenceCounter(connection.getSequenceCounter() + 2);

		final CemiTunnelRequest cemiTunnelRequest = new CemiTunnelRequest();
		cemiTunnelRequest.setMessageCode(BaseController.REQUEST_PRIMITIVE);
		cemiTunnelRequest.setAdditionalInfoLength(0);
		cemiTunnelRequest.setCtrl1(0xB0);
		cemiTunnelRequest.setCtrl2(0x60);
//		cemiTunnelRequest.setSourceKNXAddress(getDevice().getPhysicalAddress());
		cemiTunnelRequest.setSourceKNXAddress(0x11FF);
		cemiTunnelRequest.setDestKNXAddress(Utils.knxAddressToInteger("1.1.1"));
		cemiTunnelRequest.setLength(0);
		// tunnel connect request
		cemiTunnelRequest.setTpci(0x80);
//		cemiTunnelRequest.setApci(value == 0 ? 0x80 : 0x81);
//		cemiTunnelRequest.setPayloadBytes(dataSerializer.serializeToBytes(value));

		final KNXPacket knxPacket = new KNXPacket();
		knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_REQUEST);
		knxPacket.setConnectionHeader(connectionHeader);
		knxPacket.setCemiTunnelRequest(cemiTunnelRequest);

		try {
			connection.sendData(knxPacket);
		} catch (final IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private void processTunnelingRequest(final Connection connection, final DatagramSocket datagramSocket,
			final DatagramPacket datagramPacket, final KNXPacket knxPacket, final String label) throws IOException {

		KNXPacket tunnelResponse = null;
		KNXPacket confirmKNXPacket = null;

//		int sequenceCounter = 0;

		switch (knxPacket.getCemiTunnelRequest().getTpci()) {

		case GROUP_VALUE_WRITE_OR_WRITE:

			if (knxPacket.getCemiTunnelRequest().getApci() == 0) {

				LOG.info("GroupValue read");

				//
				// ANSWER an TunnelAcknowledge+OK
				//

				final KNXPacket ackKnxPacket = new KNXPacket(knxPacket);
				ackKnxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_RESPONSE);
				ackKnxPacket.setCemiTunnelRequest(null);
				knxPacket.getConnection().sendResponse(ackKnxPacket, datagramPacket.getSocketAddress());

				//
				// SEND TunnelRequest+Indication
				//

				final KNXPacket response = new KNXPacket(knxPacket);

//				response.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_RESPONSE);
				response.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_REQUEST);

				response.getCemiTunnelRequest().setMessageCode(BaseController.INDICATION_PRIMITIVE);
//				response.getCemiTunnelRequest().setMessageCode(BaseController.CONFIRM_PRIMITIVE);
				response.getCemiTunnelRequest().setAdditionalInfoLength(0);
				response.getCemiTunnelRequest().setLength(1);
//				response.getCemiTunnelRequest().setLength(0);
				// set GroupValueResponse and value
				// bitmask 01000001 (lower 6 bit are the value, upper two are the type, 01 is
				// response)
				// response.getCemiTunnelRequest().setApci(0x40 | ((byte) getDevice().getValue()
				// & 0xFF));
				response.getCemiTunnelRequest().setTpci(0x00);
				response.getCemiTunnelRequest().setApci(0x00);
//				response.getCemiTunnelRequest().setSourceKNXAddress(0x11FF);
//				response.getCemiTunnelRequest().setSourceKNXAddress(0x110B);
//				response.getCemiTunnelRequest().setDestKNXAddress(Utils.knxAddressToInteger("0/3/4"));
				// increment sequence
//				response.getConnectionHeader().setSequenceCounter(response.getConnectionHeader().getSequenceCounter());
				knxPacket.getConnection().sendResponse(response, datagramPacket.getSocketAddress());

				//
				// SEND TunnelRequest+Indication GroupValueResp
				//

				final ConnectionHeader connectionHeader = new ConnectionHeader();

				final CemiTunnelRequest cemiTunnelRequest = new CemiTunnelRequest();
				cemiTunnelRequest.setMessageCode(BaseController.INDICATION_PRIMITIVE);
				cemiTunnelRequest.setAdditionalInfoLength(0);
				cemiTunnelRequest.setCtrl1(0xB4);
				cemiTunnelRequest.setCtrl2(0xE0);
				cemiTunnelRequest
						.setSourceKNXAddress(NetworkUtils.toNetworkOrder((short) getDevice().getPhysicalAddress()));
				cemiTunnelRequest.setDestKNXAddress(Utils.knxAddressToInteger("0/3/4"));
				cemiTunnelRequest.setLength(1);
//				cemiTunnelRequest.setPayloadBytes(payloadBytes);
				cemiTunnelRequest.setTpci(0x00);
				// response bit + value
				cemiTunnelRequest.setApci(0x40 | ((byte) getDevice().getValue() & 0xFF));
//				cemiTunnelRequest.setApci(0x00);

				final KNXPacket requestIndication = new KNXPacket();
				requestIndication.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_REQUEST);
				requestIndication.setConnectionHeader(connectionHeader);
				requestIndication.setCemiTunnelRequest(cemiTunnelRequest);

				knxPacket.getConnection().sendData(requestIndication);

			} else {

				// TODO: write a data extractor that knows the datatype of the groupaddress and
				// can correctly retrieve the data send inside the KNXPacket Tunneling Request

				// for a switch with a byte datatype, the value is encoded into the APCI byte
				// for some reason.
				// It is encoded into the lower six bit = 0x3F.
				final int value = ((byte) knxPacket.getCemiTunnelRequest().getApci().intValue()) & 0x3F;
				LOG.info("GROUP_VALUE_WRITE From External Client: Value: " + value);

				LOG.info("GroupValue write. Value = " + value);

				// set value into the device
				getDevice().setValue(value);

				// send an acknowledge
				final KNXPacket ackKnxPacket = new KNXPacket(knxPacket);
				ackKnxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_RESPONSE);
				ackKnxPacket.setCemiTunnelRequest(null);

				knxPacket.getConnection().sendResponse(ackKnxPacket, datagramPacket.getSocketAddress());

				// Send confirm
				final KNXPacket response = new KNXPacket(knxPacket);

//				response.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_RESPONSE);
				response.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_REQUEST);

				response.getCemiTunnelRequest().setMessageCode(BaseController.INDICATION_PRIMITIVE);
				response.getCemiTunnelRequest().setAdditionalInfoLength(0);
				response.getCemiTunnelRequest().setLength(1);
				// set GroupValueResponse and value
				// bitmask 01000001 (lower 6 bit are the value, upper two are the type, 01 is
				// response)
//				response.getCemiTunnelRequest().setApci(0x41);
//				response.getCemiTunnelRequest().setSourceKNXAddress(0x11FF);
				response.getCemiTunnelRequest().setSourceKNXAddress(0x110B);
				response.getCemiTunnelRequest().setDestKNXAddress(Utils.knxAddressToInteger("0/3/4"));
				response.getCemiTunnelRequest().setApci(0x80 | ((byte) (value & 0xFF)));

				// increment sequence
				response.getConnectionHeader().setSequenceCounter(response.getConnectionHeader().getSequenceCounter());

				knxPacket.getConnection().sendResponse(response, datagramPacket.getSocketAddress());

//				startThread("TunnelingController", knxPacket.getConnection());
			}

			break;

		case DEVICE_DESCRIPTION_READ_TCPI:
			// send acknowledge
			tunnelResponse = sendTunnelResponse(knxPacket, datagramSocket, datagramPacket);
			knxPacket.getConnection().sendResponse(tunnelResponse, datagramPacket.getSocketAddress());

			// all four packets req+OK, ind+OK belong to the same sequence counter value
//			sequenceCounter = knxPacket.getConnection().getSequenceCounter();

			// send message acknowledge, send answer message, send confirm
			confirmKNXPacket = new KNXPacket(knxPacket);
//			confirmKNXPacket.getConnectionHeader().setSequenceCounter(sequenceCounter);
			confirmKNXPacket.getCemiTunnelRequest().setMessageCode(CONFIRM_PRIMITIVE);
			confirmKNXPacket.getCemiTunnelRequest().setSourceKNXAddress(getDevice().getPhysicalAddress());
			confirmKNXPacket.getCemiTunnelRequest()
					.setDestKNXAddress(knxPacket.getCemiTunnelRequest().getDestKNXAddress());
			confirmKNXPacket.getCemiTunnelRequest().setCtrl1(0x90);
			confirmKNXPacket.getCemiTunnelRequest().setLength(1);
			// <------- FIX for different application services!
			confirmKNXPacket.getCemiTunnelRequest().setApci(DEVICE_DESCRIPTION_READ_APCI);

			knxPacket.getConnection().sendResponse(confirmKNXPacket, datagramPacket.getSocketAddress());

//			// prepare packets that are sent when the communication partner sends a tunnel
//			// response
//			indicationKNXPacket = retrieveDeviceDescriptionReadAPCIIndicationPacket(knxPacket, sequenceCounter + 1);
//			acknowledgeKNXPacket = retrieveDeviceDescriptionReadAPCIAcknowledgePacket(knxPacket, sequenceCounter);
			break;

		case 0x01:
			// send acknowledge
			tunnelResponse = sendTunnelResponse(knxPacket, datagramSocket, datagramPacket);
			knxPacket.getConnection().sendResponse(tunnelResponse, datagramPacket.getSocketAddress());

//			sequenceCounter = knxPacket.getConnection().getSequenceCounter();

			// send message acknowledge, send answer message
			confirmKNXPacket = new KNXPacket(knxPacket);
//			confirmKNXPacket.getConnectionHeader().setSequenceCounter(sequenceCounter);
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
			LOG.info("Sending Tunnel connection response ...");

			// ANSWER tunnel acknowledge
			final KNXPacket acknowledgeKNXPacket = new KNXPacket(knxPacket);
			acknowledgeKNXPacket.setCemiPropReadRequest(null);
			acknowledgeKNXPacket.setCemiTunnelRequest(null);
			acknowledgeKNXPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_RESPONSE);
			knxPacket.getConnection().sendResponse(acknowledgeKNXPacket, datagramPacket.getSocketAddress());

			// SEND TunnelRequest DataIndication connect
			final ConnectionHeader connectionHeader = new ConnectionHeader();

//			final CemiTunnelRequest cemiTunnelRequest = new CemiTunnelRequest();
//			cemiTunnelRequest.setMessageCode(BaseController.INDICATION_PRIMITIVE);
//			cemiTunnelRequest.setAdditionalInfoLength(0);
//			cemiTunnelRequest.setCtrl1(0xB4);
//			cemiTunnelRequest.setCtrl2(0xE0);
////			cemiTunnelRequest.setSourceKNXAddress(getDevice().getPhysicalAddress());
////			cemiTunnelRequest.setDestKNXAddress(Utils.knxAddressToInteger("0/3/4"));
////			cemiTunnelRequest.setLength(1);
////			cemiTunnelRequest.setPayloadBytes(payloadBytes);
////			cemiTunnelRequest.setTpci(0x00);
////			cemiTunnelRequest.setApci(0x40 | ((byte) getDevice().getValue() & 0xFF));

			indicationKNXPacket = new KNXPacket(knxPacket);
			indicationKNXPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_REQUEST);
//			indicationKNXPacket.setConnectionHeader(connectionHeader);
//			indicationKNXPacket.setCemiTunnelRequest(cemiTunnelRequest);
			indicationKNXPacket.getCemiTunnelRequest().setMessageCode(BaseController.INDICATION_PRIMITIVE);
			knxPacket.getConnection().sendData(indicationKNXPacket);

//			startThread(connection);
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

	/**
	 * Learned from connecting ETS5 to the BAOS and sniffing the packets using
	 * wireshark.
	 *
	 * @param knxPacket
	 * @param sequenceCounter
	 * @return
	 */
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

	/**
	 * Create a packet that transfers the device's DEVICE_DESCRIPTOR property as a
	 * payload.
	 *
	 * Learned from connecting ETS5 to the BAOS and sniffing the packets using
	 * wireshark.
	 *
	 * @param knxPacket
	 * @param sequenceCounter
	 * @return
	 */
	private KNXPacket retrieveDeviceDescriptionReadAPCIIndicationPacket(final KNXPacket knxPacket,
			final int sequenceCounter) {

		// retrieve the device descriptor property and put it into the packet as payload
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
			// TUNNELRESPONSE == TUNNEL_ACKNOWLEDGE == 0x0421
		case TUNNEL_RESPONSE:
		case CONNECT_REQUEST:
//		case CONNECT_RESPONSE:
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

}
