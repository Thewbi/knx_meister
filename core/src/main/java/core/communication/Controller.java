package core.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.common.NetworkUtils;
import core.packets.ConnectionRequestInformation;
import core.packets.ConnectionResponseDataBlock;
import core.packets.ConnectionStatus;
import core.packets.ConnectionType;
import core.packets.DescriptionInformationBlockType;
import core.packets.DeviceInformationDIB;
import core.packets.DeviceStatus;
import core.packets.HPAIStructure;
import core.packets.KNXLayer;
import core.packets.KNXMedium;
import core.packets.KNXPacket;
import core.packets.MfrDataDIB;
import core.packets.ProtocolDescriptor;
import core.packets.ServiceFamily;
import core.packets.ServiceIdentifier;
import core.packets.Structure;
import core.packets.StructureType;
import core.packets.SuppSvcFamiliesDIB;

public class Controller extends BaseDatagramPacketCallback {

	private static final int DEVICE_ADDRESS = 0x0A11;

	// public static final int POINT_TO_POINT_PORT = 65000;
	public static final int POINT_TO_POINT_PORT = 3671;

//	public static final int POINT_TO_POINT_CONTROL_PORT = 34000;
	public static final int POINT_TO_POINT_CONTROL_PORT = 3671;

//	public static final int POINT_TO_POINT_DATA_PORT = 34001;
	public static final int POINT_TO_POINT_DATA_PORT = 3671;

//	private final String localInetAddress = "127.0.0.1";
//	private final String localInetAddress = "192.168.0.108";
	private final String localInetAddress;

	private int channelNumber = 0x01;

	private static final Logger LOG = LogManager.getLogger(Controller.class);

	private final Map<String, HPAIStructure> deviceMap = new HashMap<>();

	private ConnectionManager connectionManager;

	public Controller() throws SocketException, UnknownHostException {
		localInetAddress = InetAddress.getLocalHost().getHostAddress();
	}

	@Override
	public void knxPacket(final Connection connection, final DatagramSocket socket3671,
			final DatagramPacket datagramPacket, final KNXPacket knxPacket, final String label) throws IOException {

		HPAIStructure hpaiStructure = null;
		InetAddress inetAddress = null;
		int port = -1;

		DeviceInformationDIB deviceInformationDIB = null;

		switch (knxPacket.getHeader().getServiceIdentifier()) {

		case SEARCH_REQUEST_EXT:
			LOG.trace("<<<<<<<<<<<<<<< Ignoring " + ServiceIdentifier.SEARCH_REQUEST_EXT);
			break;

		case SEARCH_REQUEST:

//			LOG.trace("<<<<<<<<<<<<< SEARCH_REQUEST {}", Utils.retrieveCurrentTimeAsString());
//			LOG.trace("\n" + knxPacket.toString());

			hpaiStructure = (HPAIStructure) knxPacket.getStructureMap().get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);
			inetAddress = InetAddress.getByAddress(hpaiStructure.getIpAddress());
			port = hpaiStructure.getPort() & 0xFFFF;

			final KNXPacket sendSearchResponseToAddress = sendSearchResponseToAddress(socket3671, inetAddress, port);
//			sendSearchResponseToSender(socket, datagramPacket);

			connection.sendResponse(sendSearchResponseToAddress, new InetSocketAddress(inetAddress, port));
			break;

		case SEARCH_RESPONSE:
//			LOG.info("<<<<<<<<<<<<< SEARCH_RESPONSE {}", Utils.retrieveCurrentTimeAsString());
//			LOG.info("\n" + knxPacket.toString());

			hpaiStructure = (HPAIStructure) knxPacket.getStructureMap().get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);
			inetAddress = InetAddress.getByAddress(hpaiStructure.getIpAddress());
			port = hpaiStructure.getPort() & 0xFFFF;

			deviceInformationDIB = (DeviceInformationDIB) knxPacket.getDibMap()
					.get(DescriptionInformationBlockType.DEVICE_INFO);
			deviceMap.put(deviceInformationDIB.getDeviceSerialNumberAsString(), hpaiStructure);

			// TODO: use connection
			final KNXPacket sendDescriptionRequest = sendDescriptionRequest(inetAddress, port);
			connection.sendResponse(sendDescriptionRequest, new InetSocketAddress(inetAddress, port));
			break;

		case DESCRIPTION_REQUEST:
//			LOG.info("<<<<<<<<<<<<< DESCRIPTION_REQUEST {}", Utils.retrieveCurrentTimeAsString());
//			LOG.info("\n" + knxPacket.toString());

			hpaiStructure = (HPAIStructure) knxPacket.getStructureMap().get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);
			inetAddress = InetAddress.getByAddress(hpaiStructure.getIpAddress());
			port = hpaiStructure.getPort() & 0xFFFF;

			// TODO: use connection
			final KNXPacket sendDescriptionResponse = sendDescriptionResponse(socket3671, datagramPacket, inetAddress,
					port);

			connection.sendResponse(sendDescriptionResponse, datagramPacket.getSocketAddress());
//			connection.sendResponse(sendDescriptionResponse, new InetSocketAddress(inetAddress, port));
			break;

		case DESCRIPTION_RESPONSE:
//			LOG.info("<<<<<<<<<<<<< DESCRIPTION_RESPONSE {}", Utils.retrieveCurrentTimeAsString());
//			LOG.info("\n" + knxPacket.toString());

			deviceInformationDIB = (DeviceInformationDIB) knxPacket.getDibMap()
					.get(DescriptionInformationBlockType.DEVICE_INFO);
			hpaiStructure = deviceMap.get(deviceInformationDIB.getDeviceSerialNumberAsString());

			// TODO: use connection
			final KNXPacket sendConnectionRequest = sendConnectionRequest(socket3671, datagramPacket, knxPacket,
					hpaiStructure.getIpAddressAsObject(), hpaiStructure.getPort());

			final InetSocketAddress inetSocketAddress = new InetSocketAddress(hpaiStructure.getIpAddressAsObject(),
					hpaiStructure.getPort());
			connection.sendResponse(sendConnectionRequest, inetSocketAddress);
			break;

		case CONNECT_REQUEST:
//			LOG.info("<<<<<<<<<<<<< CONNECT_REQUEST {}", Utils.retrieveCurrentTimeAsString());
//			LOG.info("<<<<<<<<<<<<<\n" + knxPacket.toString());

//			LOG.info("<<<<<<<<<<<<< CONNECT_REQUEST from Address " + socket3671.getPort());
//			LOG.info("<<<<<<<<<<<<< CONNECT_REQUEST from Address " + socket3671.getLocalPort());
//			LOG.info("<<<<<<<<<<<<< CONNECT_REQUEST from Address " + socket3671.getLocalAddress());
//			LOG.info("<<<<<<<<<<<<< CONNECT_REQUEST from Address " + socket3671.getLocalSocketAddress());
//			LOG.info("<<<<<<<<<<<<< CONNECT_REQUEST from Address " + socket3671.getRemoteSocketAddress());

			final Structure tunnelingStructure = knxPacket.getStructureMap().get(StructureType.TUNNELING_CONNECTION);
			final Structure deviceManagementStructure = knxPacket.getStructureMap()
					.get(StructureType.DEVICE_MGMT_CONNECTION);

			if ((tunnelingStructure == null) && (deviceManagementStructure == null)) {
				throw new RuntimeException("Cannot retrieve a connection structure!");
			}

			ConnectionType connectionType = ConnectionType.TUNNEL_CONNECTION;
			if (deviceManagementStructure != null) {
				connectionType = ConnectionType.DEVICE_MGMT_CONNECTION;
			}

			final Connection newConnection = connectionManager.createNewConnection(socket3671, connectionType);

			hpaiStructure = (HPAIStructure) knxPacket.getStructureMap().get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);
			final InetAddress controlInetAddress = InetAddress.getByAddress(hpaiStructure.getIpAddress());
			final int controlPort = hpaiStructure.getPort() & 0xFFFF;

			// TODO: use connection
			final KNXPacket sendConnectionResponse = sendConnectionResponse(socket3671, datagramPacket,
					controlInetAddress, controlPort, connectionType);
			newConnection.sendResponse(sendConnectionResponse, new InetSocketAddress(controlInetAddress, controlPort));

//			hpaiStructure = (HPAIStructure) knxPacket.getStructureMap().get(StructureType.HPAI_DATA_ENDPOINT_UDP);
//			inetAddress = InetAddress.getByAddress(hpaiStructure.getIpAddress());
//			port = hpaiStructure.getPort() & 0xFFFF;
//			sendConnectionResponse(inetAddress, port);

			break;

		case CONNECTIONSTATE_REQUEST:
//			LOG.info("<<<<<<<<<<<<< CONNECTIONSTATE_REQUEST {}", Utils.retrieveCurrentTimeAsString());
//			LOG.info("\n" + knxPacket.toString());

			final KNXPacket sendConnectionStateResponse = sendConnectionStateResponse(socket3671, datagramPacket,
					knxPacket, inetAddress, port);

			connection.sendResponse(sendConnectionStateResponse, datagramPacket.getSocketAddress());
			break;

		case DISCONNECT_REQUEST:
//			LOG.info("<<<<<<<<<<<<< DISCONNECT_REQUEST {}", Utils.retrieveCurrentTimeAsString());
//			LOG.info("\n" + knxPacket.toString());

			connectionManager.closeConnection(knxPacket.getCommunicationChannelId());

			// TODO: use connection
			final KNXPacket sendDisconnetResponse = sendDisconnetResponse(socket3671, datagramPacket, knxPacket,
					inetAddress, port);

			connection.sendResponse(sendDisconnetResponse, datagramPacket.getSocketAddress());
//			connection.sendResponse(sendDisconnetResponse, new InetSocketAddress(inetAddress, port));
			break;

		case TUNNEL_REQUEST:
//			LOG.info("<<<<<<<<<<<<< TUNNEL_REQUEST {}", Utils.retrieveCurrentTimeAsString());
//			LOG.info("\n" + knxPacket.toString());

			throw new RuntimeException("Not implemented!");

		default:
//			LOG.info("<<<<<<<<<<<<< " + knxPacket.getHeader().getServiceIdentifier().name());
//			LOG.info("\n" + knxPacket.toString());

//			throw new RuntimeException("Unknown command!" + knxPacket.getHeader().getServiceIdentifier());
//			System.out.println("Ignoring: " + knxPacket.getHeader().getServiceIdentifier().name());
			LOG.warn("Ignoring: " + knxPacket.getHeader().getServiceIdentifier().name());
			break;
		}
	}

	private KNXPacket sendConnectionRequest(final DatagramSocket socket, final DatagramPacket originalDatagramPacket,
			final KNXPacket originalKNXPacket, final InetAddress inetAddress, final int port) throws IOException {

		final HPAIStructure controlHPAIStructure = new HPAIStructure();
//		controlHPAIStructure.setIpAddress(new byte[] { (byte) 192, (byte) 168, (byte) 2, (byte) 1 });
//		controlHPAIStructure.setIpAddress(new byte[] { (byte) 127, (byte) 0, (byte) 0, (byte) 1 });
		controlHPAIStructure.setIpAddress(InetAddress.getByName(localInetAddress).getAddress());
//		controlHPAIStructure.setPort((short) POINT_TO_POINT_PORT);
		controlHPAIStructure.setPort((short) POINT_TO_POINT_CONTROL_PORT);

		final HPAIStructure dataHPAIStructure = new HPAIStructure();
//		dataHPAIStructure.setIpAddress(new byte[] { (byte) 192, (byte) 168, (byte) 2, (byte) 1 });
//		dataHPAIStructure.setIpAddress(new byte[] { (byte) 127, (byte) 0, (byte) 0, (byte) 1 });
		dataHPAIStructure.setIpAddress(InetAddress.getByName(localInetAddress).getAddress());
//		dataHPAIStructure.setPort((short) POINT_TO_POINT_PORT);
		dataHPAIStructure.setPort((short) POINT_TO_POINT_DATA_PORT);

		final KNXPacket knxPacket = new KNXPacket();
		knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.CONNECT_REQUEST);
		knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, controlHPAIStructure);
		knxPacket.getStructureMap().put(StructureType.HPAI_DATA_ENDPOINT_UDP, dataHPAIStructure);

		final ConnectionRequestInformation connectionRequestInformation = new ConnectionRequestInformation();
		connectionRequestInformation.setStructureType(StructureType.TUNNELING_CONNECTION);
		connectionRequestInformation.setKnxLayer(KNXLayer.TUNNEL_LINKLAYER.getValue());
		knxPacket.getStructureMap().put(connectionRequestInformation.getStructureType(), connectionRequestInformation);

		return knxPacket;

//		final byte[] bytes = knxPacket.getBytes();
//
//		final DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, inetAddress, port);
//
////		final DatagramSocket socket = new DatagramSocket();
////		socket.send(datagramPacket);
////		socket.close();
//
////		LOG.info(">>>>>>>>>> SENDING {} {}", knxPacket.getHeader().getServiceIdentifier().toString(),
////				Utils.retrieveCurrentTimeAsString());
//
//		return datagramPacket;
	}

	private KNXPacket sendDisconnetResponse(final DatagramSocket socket, final DatagramPacket datagramPacket,
			final KNXPacket originalKNXPacket, final InetAddress inetAddress, final int port) throws IOException {

		final KNXPacket knxPacket = new KNXPacket();

		// header
		knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.DISCONNECT_RESPONSE);

		knxPacket.setCommunicationChannelId(originalKNXPacket.getCommunicationChannelId());
		knxPacket.setConnectionStatus(ConnectionStatus.E_NO_ERROR);

		return knxPacket;

//		final byte[] bytes = knxPacket.getBytes();
//
////		LOG.info(Utils.integerToStringNoPrefix(bytes));
//
////		final DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, inetAddress, port);
//		final DatagramPacket outDatagramPacket = new DatagramPacket(bytes, bytes.length,
//				datagramPacket.getSocketAddress());
//
////		socket.send(datagramPacket);
////		socket.close();
//
////		originalSocket.send(datagramPacket);
////		socket.send(outDatagramPacket);
//
////		LOG.info(">>>>>>>>>> " + knxPacket.getHeader().getServiceIdentifier().toString() + " to IP "
////				+ inetAddress.toString() + " Port: " + port);
////		LOG.info(">>>>>>>>>> SENDING {} {}", knxPacket.getHeader().getServiceIdentifier().toString(),
////				Utils.retrieveCurrentTimeAsString());
//
//		return outDatagramPacket;
	}

	private KNXPacket sendConnectionStateResponse(final DatagramSocket socket, final DatagramPacket datagramPacket,
			final KNXPacket originalKNXPacket, final InetAddress inetAddress, final int port) throws IOException {

		final KNXPacket knxPacket = new KNXPacket();

		// header
		knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.CONNECTIONSTATE_RESPONSE);

		knxPacket.setCommunicationChannelId(originalKNXPacket.getCommunicationChannelId());
		knxPacket.setConnectionStatus(ConnectionStatus.E_NO_ERROR);

//		LOG.trace(knxPacket);

//		final byte[] bytes = knxPacket.getBytes();

//		LOG.trace(Utils.integerToStringNoPrefix(bytes));

//		final DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, inetAddress, port);
//		final DatagramPacket outDatagramPacket = new DatagramPacket(bytes, bytes.length,
//				datagramPacket.getSocketAddress());

//		socket.send(datagramPacket);
//		socket.close();

//		originalSocket.send(datagramPacket);
//		socket.send(outDatagramPacket);

//		LOG.info(">>>>>>>>>> " + knxPacket.getHeader().getServiceIdentifier().toString() + " to IP "
//				+ inetAddress.toString() + " Port: " + port);
//		LOG.info(">>>>>>>>>> SENDING {} {}", knxPacket.getHeader().getServiceIdentifier().toString(),
//				Utils.retrieveCurrentTimeAsString());

		return knxPacket;
	}

	/**
	 * 7.8.2 CONNECT_RESPONSE Example: 8.8.6 CONNECT_RESPONSE
	 *
	 * @param socket3671     the DatagramSocket that the reader thread is bound to
	 *                       on port 3671 and which messages are received from.
	 * @param datagramPacket
	 *
	 * @param inetAddress    the IP address of the KNX Clients control endpoint
	 *                       (send in the control HPAI).
	 * @param port
	 * @throws IOException
	 */
	private KNXPacket sendConnectionResponse(final DatagramSocket socket3671, final DatagramPacket datagramPacket,
			final InetAddress inetAddress, final int port, final ConnectionType connectionType) throws IOException {

//		LOG.info(">>>>>>>>>> SENDING CONNECTION_RESPONSE ...");

//		final DatagramSocket socket = new DatagramSocket();
////		socket.connect(InetAddress.getByName("127.0.0.1"), 7777);
//		socket.connect(inetAddress, port);
//		final int receivePort = socket.getPort();
//		final int localPort = socket.getLocalPort();
//		final InetAddress localAddress = socket.getLocalAddress();

//		originalSocket.send(datagramPacket);

		final InetAddress localAddress = socket3671.getLocalAddress();
		final int localPort = socket3671.getLocalPort();

		final KNXPacket knxPacket = new KNXPacket();

		// header
		knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.CONNECT_RESPONSE);

		knxPacket.setCommunicationChannelId(channelNumber++);
		knxPacket.setConnectionStatus(ConnectionStatus.E_NO_ERROR);

		final boolean addHPAIStructure = true;
		if (addHPAIStructure) {

			// HPAI structure
			final HPAIStructure hpaiStructure = new HPAIStructure();
			// hpaiStructure.setIpAddress(new byte[] { (byte) 192, (byte) 168, (byte) 2,
			// (byte) 1 });
			// hpaiStructure.setIpAddress(new byte[] { (byte) 127, (byte) 0, (byte) 0,
			// (byte) 1 });
			hpaiStructure.setIpAddress(InetAddress.getByName(localInetAddress).getAddress());
			// hpaiStructure.setIpAddress(localAddress.getAddress());
//			hpaiStructure.setPort((short) POINT_TO_POINT_PORT);
			hpaiStructure.setPort((short) POINT_TO_POINT_CONTROL_PORT);
			// hpaiStructure.setPort((short) localPort);
			knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, hpaiStructure);
			// knxPacket.getStructureMap().put(StructureType.HPAI_DATA_ENDPOINT_UDP,
			// hpaiStructure);
		}

		final boolean addconnectionResponseDataBlock = true;
		if (addconnectionResponseDataBlock) {
			// CRD - Connection Response Data Block
			final ConnectionResponseDataBlock connectionResponseDataBlock = new ConnectionResponseDataBlock();

//			connectionResponseDataBlock.setConnectionType(ConnectionType.DEVICE_MGMT_CONNECTION);
//			connectionResponseDataBlock.setConnectionType(ConnectionType.TUNNEL_CONNECTION);
			connectionResponseDataBlock.setConnectionType(connectionType);

			if (connectionType != ConnectionType.DEVICE_MGMT_CONNECTION) {
				connectionResponseDataBlock.setDeviceAddress(DEVICE_ADDRESS);
			}
			// connectionResponseDataBlock.setLength(2);

			knxPacket.setConnectionResponseDataBlock(connectionResponseDataBlock);
		}

		return knxPacket;

//		final byte[] bytes = knxPacket.getBytes();
//
////		LOG.info("\n" + knxPacket.toString());
////		LOG.trace(Utils.integerToStringNoPrefix(bytes));
//
//		final boolean answerToControl = false;
//		if (answerToControl) {
//			// answer to control endpoint
//
//			DatagramSocket socket = null;
//			try {
//				socket = new DatagramSocket(3671);
////				socket = new DatagramSocket();
//
//				final DatagramPacket outDatagramPacketSpecificIP = new DatagramPacket(bytes, bytes.length, inetAddress,
//						port);
//				socket.send(outDatagramPacketSpecificIP);
//
////				outDatagramPacketSpecificIP = new DatagramPacket(bytes, bytes.length, inetAddress, port + 1);
////				socket.send(outDatagramPacketSpecificIP);
//			} catch (final java.net.SocketException e) {
//				e.printStackTrace();
//			} finally {
//				if (socket != null) {
//					socket.close();
//				}
//			}
//		}
//
//		final boolean answerToOriginal = true;
//		if (answerToOriginal) {
//			// answer to the original socket
//			final DatagramPacket outDatagramPacket = new DatagramPacket(bytes, bytes.length,
//					datagramPacket.getSocketAddress());
////			socket3671.send(outDatagramPacket);
//
////			LOG.info(
////					">>>>>>>>>> SENDING {} {}", knxPacket.getHeader().getServiceIdentifier().toString() + " to IP "
////							+ inetAddress.toString() + " Port: " + port + " done.",
////					Utils.retrieveCurrentTimeAsString());
//
//			return outDatagramPacket;
//		}
//
//		return null;
	}

	/**
	 * 7.6.1 SEARCH_REQUEST
	 *
	 * @throws IOException
	 */
	public void sendSearchRequest() throws IOException {

		LOG.info("sendSearchRequest() ...");

		final KNXPacket knxPacket = new KNXPacket();

		// header
		knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.SEARCH_REQUEST);

		// HPAI - as a IP address, specify the IP address of the NIC you want to receive
		// the response on
		final HPAIStructure hpaiStructure = new HPAIStructure();
//		hpaiStructure.setIpAddress(new byte[] { (byte) 127, (byte) 0, (byte) 0, (byte) 1 });
//		hpaiStructure.setIpAddress(new byte[] { (byte) 192, (byte) 168, (byte) 0, (byte) 108 });
		hpaiStructure.setIpAddress(InetAddress.getByName(localInetAddress).getAddress());
//		hpaiStructure.setPort((short) POINT_TO_POINT_PORT);
		hpaiStructure.setPort((short) POINT_TO_POINT_CONTROL_PORT);
		knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, hpaiStructure);

		final byte[] bytes = knxPacket.getBytes();

		final String group = "224.0.23.12";
		final int port = 3671;
		final int ttl = 4;

		final DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, InetAddress.getByName(group),
				port);

		// as a network interface for sending the multicast, use a NIC that is working
		// and configured
		// on your system
//		final NetworkInterface networkInterface = NetworkUtils
//				.findInterfaceByIP(new byte[] { (byte) 127, (byte) 0, (byte) 0, (byte) 1 });

		// TODO: this is a hard coded IP! FIX IT!
//		final NetworkInterface networkInterface = NetworkUtils
//				.findInterfaceByIP(new byte[] { (byte) 192, (byte) 168, (byte) 0, (byte) 108 });

		final NetworkInterface networkInterface = NetworkUtils
				.findInterfaceByIP(InetAddress.getByName(localInetAddress).getAddress());

		// https://stackoverflow.com/questions/37812919/why-a-datagramsocket-does-not-send-over-the-network-with-multicast-address
		//
		// When sending unicast datagrams, the routing tables dictate which network
		// interface is used to send the packet. For multicast, you need to specify the
		// interface. You can do that with a MulticastSocket.
		final MulticastSocket multicastSocket = new MulticastSocket();
		multicastSocket.setNetworkInterface(networkInterface);
		multicastSocket.setTimeToLive(ttl);
		multicastSocket.send(datagramPacket);
		multicastSocket.close();

//		LOG.info(">>>>>>>>>> SENDING {} {}", knxPacket.getHeader().getServiceIdentifier().toString(),
//				Utils.retrieveCurrentTimeAsString());

//		LOG.info("sendSearchRequest() done.");
	}

	public void sendSearchResponseToSender(final DatagramSocket socket, final DatagramPacket datagramPacket)
			throws IOException {

		final KNXPacket knxPacket = retrieveSearchResponseKNXPacket();

		final byte[] bytes = knxPacket.getBytes();

//		System.out.println(Utils.integerToStringNoPrefix(bytes));

		final DatagramPacket outDatagramPacket = new DatagramPacket(bytes, bytes.length,
				datagramPacket.getSocketAddress());

//		final DatagramSocket socket = new DatagramSocket();
		socket.send(outDatagramPacket);
//		socket.close();

//		LOG.info(">>>>>>>>>> SENDING {} {}", knxPacket.getHeader().getServiceIdentifier().toString(),
//				Utils.retrieveCurrentTimeAsString());

	}

	public KNXPacket sendSearchResponseToAddress(final DatagramSocket socket3671, final InetAddress inetAddress,
			final int port) throws IOException {

//		LOG.trace("sendSearchResponseToAddress to " + inetAddress.toString() + " " + port);

		final KNXPacket knxPacket = retrieveSearchResponseKNXPacket();
		return knxPacket;

//		final byte[] bytes = knxPacket.getBytes();

//		System.out.println(Utils.integerToStringNoPrefix(bytes));

//		final DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, inetAddress, port);

//		final NetworkInterface loopbackNetworkInterface = NetworkUtils
//				.findInterfaceByIP(new byte[] { (byte) 127, (byte) 0, (byte) 0, (byte) 1 });
//		final InetSocketAddress bindInetSocketAddress = new InetSocketAddress(
//				loopbackNetworkInterface.getInetAddresses().nextElement(), POINT_TO_POINT_PORT);

//		networkInterface.get

//		final InetSocketAddress bindInetSocketAddress = new InetSocketAddress(inetAddress, port);

//		DatagramSocket socket = null;
//		try {
//			socket = new DatagramSocket();
////			socket = new DatagramSocket(bindInetSocketAddress);
////		socket.connect(inetAddr);
//			socket.send(datagramPacket);
////			socket.close();
//		} catch (final java.net.SocketException e) {
//			e.printStackTrace();
//		} finally {
//			if (socket != null) {
//				socket.close();
//			}
//		}

//		socket3671.send(datagramPacket);

//		LOG.trace(">>>>>>>>>> SENDING {} {}", knxPacket.getHeader().getServiceIdentifier().toString(),
//				Utils.retrieveCurrentTimeAsString());

//		return datagramPacket;
	}

	private KNXPacket retrieveSearchResponseKNXPacket() throws UnknownHostException {
		final KNXPacket knxPacket = new KNXPacket();

		// header
		knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.SEARCH_RESPONSE);

		// HPAI structure
		final HPAIStructure hpaiStructure = new HPAIStructure();
//		hpaiStructure.setIpAddress(new byte[] { (byte) 192, (byte) 168, (byte) 2, (byte) 1 });
//		hpaiStructure.setIpAddress(new byte[] { (byte) 127, (byte) 0, (byte) 0, (byte) 1 });
		hpaiStructure.setIpAddress(InetAddress.getByName(localInetAddress).getAddress());
		hpaiStructure.setPort((short) POINT_TO_POINT_CONTROL_PORT);
		knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, hpaiStructure);

		// Device Information DIB
		final DeviceInformationDIB deviceInformationDIB = retrieveDeviceInformationDIB();
		knxPacket.getDibMap().put(deviceInformationDIB.getType(), deviceInformationDIB);

		// Supported Service Families DIB
		final SuppSvcFamiliesDIB suppSvcFamiliesDIB = retrieveServiceFamiliesDIB();
		knxPacket.getDibMap().put(suppSvcFamiliesDIB.getType(), suppSvcFamiliesDIB);

		// Mft DIB
		final MfrDataDIB mfrDataDIB = retrieveMfrDataDIB();
		knxPacket.getDibMap().put(mfrDataDIB.getType(), mfrDataDIB);
		return knxPacket;
	}

	public KNXPacket sendDescriptionRequest(final InetAddress inetAddress, final int port) throws IOException {

		final HPAIStructure hpaiStructure = new HPAIStructure();
//		hpaiStructure.setIpAddress(new byte[] { (byte) 192, (byte) 168, (byte) 2, (byte) 1 });
//		hpaiStructure.setIpAddress(new byte[] { (byte) 127, (byte) 0, (byte) 0, (byte) 1 });
		hpaiStructure.setIpAddress(InetAddress.getByName(localInetAddress).getAddress());
		hpaiStructure.setPort((short) POINT_TO_POINT_CONTROL_PORT);

		final KNXPacket knxPacket = new KNXPacket();
		knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.DESCRIPTION_REQUEST);
		knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, hpaiStructure);

		return knxPacket;

//		final byte[] bytes = knxPacket.getBytes();
//
//		final DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, inetAddress, port);
////
////		final DatagramSocket socket = new DatagramSocket();
////		socket.send(datagramPacket);
////		socket.close();
//
////		LOG.info(">>>>>>>>>> SENDING {} {}", knxPacket.getHeader().getServiceIdentifier().toString(),
////				Utils.retrieveCurrentTimeAsString());
//
//		return datagramPacket;
	}

	private KNXPacket sendDescriptionResponse(final DatagramSocket socket, final DatagramPacket datagramPacket,
			final InetAddress inetAddress, final int port) throws IOException {

		final KNXPacket knxPacket = new KNXPacket();
		knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.DESCRIPTION_RESPONSE);

//		final HPAIStructure hpaiStructure = new HPAIStructure();
////		hpaiStructure.setIpAddress(new byte[] { (byte) 192, (byte) 168, (byte) 2, (byte) 1 });
////		hpaiStructure.setIpAddress(new byte[] { (byte) 127, (byte) 0, (byte) 0, (byte) 1 });
//		hpaiStructure.setIpAddress(InetAddress.getByName(localInetAddress).getAddress());
//		hpaiStructure.setPort((short) POINT_TO_POINT_PORT);
//		knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, hpaiStructure);

		final DeviceInformationDIB deviceInformationDIB = retrieveDeviceInformationDIB();
		knxPacket.getDibMap().put(deviceInformationDIB.getType(), deviceInformationDIB);

		// supported service families - SuppSvcFamilies DescriptionInformationBlock
		// (DIB)
		final SuppSvcFamiliesDIB suppSvcFamiliesDIB = retrieveServiceFamiliesDIB();
		knxPacket.getDibMap().put(suppSvcFamiliesDIB.getType(), suppSvcFamiliesDIB);

		return knxPacket;

//		final byte[] bytes = knxPacket.getBytes();
//
////		final DatagramPacket outDatagramPacket = new DatagramPacket(bytes, bytes.length, inetAddress, port);
//		final DatagramPacket outDatagramPacket = new DatagramPacket(bytes, bytes.length,
//				datagramPacket.getSocketAddress());
//
////		final DatagramSocket socket = new DatagramSocket();
////		socket.send(outDatagramPacket);
////		socket.close();
//
////		LOG.info(">>>>>>>>>> SENDING {} {}", knxPacket.getHeader().getServiceIdentifier().toString(),
////				Utils.retrieveCurrentTimeAsString());
//
//		return outDatagramPacket;
	}

	/**
	 * device info DescriptionInformationBlock (DIB)
	 */
	private DeviceInformationDIB retrieveDeviceInformationDIB() {

		final DeviceInformationDIB deviceInformationDIB = new DeviceInformationDIB();

//		deviceInformationDIB.setDeviceStatus(DeviceStatus.PROGRAMMING_MODE);
		deviceInformationDIB.setDeviceStatus(DeviceStatus.NORMAL_MODE);

//		deviceInformationDIB.setIndividualAddress(0x1102);
//		deviceInformationDIB.setIndividualAddress(0x1103);
//		deviceInformationDIB.setIndividualAddress(0x0A11);
		deviceInformationDIB.setIndividualAddress(DEVICE_ADDRESS);

//		deviceInformationDIB.setMedium(KNXMedium.KNX_IP);
		deviceInformationDIB.setMedium(KNXMedium.TP1);

//		deviceInformationDIB.setProjectInstallationIdentifier(17);
		deviceInformationDIB.setProjectInstallationIdentifier(0);

//		SerialNumber=00C5:0102D84C
		// serial number
//		System.arraycopy(new byte[] { (byte) 0x03, (byte) 0x03, (byte) 0x03, (byte) 0x03, (byte) 0x03, (byte) 0x03 }, 0,
//				deviceInformationDIB.getDeviceSerialNumber(), 0, deviceInformationDIB.getDeviceSerialNumber().length);
		System.arraycopy(new byte[] { (byte) 0x00, (byte) 0xC5, (byte) 0x01, (byte) 0x02, (byte) 0xD8, (byte) 0x4D }, 0,
				deviceInformationDIB.getDeviceSerialNumber(), 0, deviceInformationDIB.getDeviceSerialNumber().length);
		// real serial
//		System.arraycopy(new byte[] { (byte) 0x00, (byte) 0xC5, (byte) 0x01, (byte) 0x02, (byte) 0xD8, (byte) 0x4C }, 0,
//				deviceInformationDIB.getDeviceSerialNumber(), 0, deviceInformationDIB.getDeviceSerialNumber().length);

		// multicast address - 224.0.23.12
		System.arraycopy(new byte[] { (byte) 0xE0, (byte) 0x00, (byte) 0x17, (byte) 0x0C }, 0,
				deviceInformationDIB.getDeviceRoutingMulticastAddress(), 0,
				deviceInformationDIB.getDeviceRoutingMulticastAddress().length);

		// mac address
//		System.arraycopy(new byte[] { (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06 }, 0,
//				deviceInformationDIB.getDeviceMacAddress(), 0, deviceInformationDIB.getDeviceMacAddress().length);
//		System.arraycopy(new byte[] { (byte) 0x09, (byte) 0x09, (byte) 0x09, (byte) 0x09, (byte) 0x09, (byte) 0x09 }, 0,
//				deviceInformationDIB.getDeviceMacAddress(), 0, deviceInformationDIB.getDeviceMacAddress().length);
		// D0-C6-37-A1-2A-E8
//		System.arraycopy(new byte[] { (byte) 0xD0, (byte) 0xC6, (byte) 0x37, (byte) 0xA1, (byte) 0x2A, (byte) 0xE8 }, 0,
//				deviceInformationDIB.getDeviceMacAddress(), 0, deviceInformationDIB.getDeviceMacAddress().length);
		// 6c:40:08:97:d1:12
		System.arraycopy(new byte[] { (byte) 0x6C, (byte) 0x40, (byte) 0x08, (byte) 0x97, (byte) 0xD1, (byte) 0x12 }, 0,
				deviceInformationDIB.getDeviceMacAddress(), 0, deviceInformationDIB.getDeviceMacAddress().length);

		// friendly name
//		final String friendlyName = "test_object1";
		final String friendlyName = "KNX IP BAOS 777";
		final byte[] friendlyNameAsByteArray = friendlyName.getBytes(StandardCharsets.US_ASCII);
		System.arraycopy(friendlyNameAsByteArray, 0, deviceInformationDIB.getDeviceFriendlyName(), 0,
				friendlyNameAsByteArray.length);

		deviceInformationDIB.setLength(54);

		return deviceInformationDIB;
	}

	private SuppSvcFamiliesDIB retrieveServiceFamiliesDIB() {

		final SuppSvcFamiliesDIB suppSvcFamiliesDIB = new SuppSvcFamiliesDIB();
		suppSvcFamiliesDIB.setLength(8);

		ProtocolDescriptor protocoDescriptor = new ProtocolDescriptor();
		suppSvcFamiliesDIB.getProtocolDescriptors().add(protocoDescriptor);
		protocoDescriptor.setProtocol(ServiceFamily.KNXNET_IP_CORE.getValue());
		protocoDescriptor.setVersion(1);

		protocoDescriptor = new ProtocolDescriptor();
		suppSvcFamiliesDIB.getProtocolDescriptors().add(protocoDescriptor);
		protocoDescriptor.setProtocol(ServiceFamily.KNXNET_DEVICE_MGMT.getValue());
		protocoDescriptor.setVersion(2);

		protocoDescriptor = new ProtocolDescriptor();
		suppSvcFamiliesDIB.getProtocolDescriptors().add(protocoDescriptor);
		protocoDescriptor.setProtocol(ServiceFamily.KNXNET_IP_TUNNELLING.getValue());
		protocoDescriptor.setVersion(1);

		return suppSvcFamiliesDIB;
	}

	private MfrDataDIB retrieveMfrDataDIB() {

		final MfrDataDIB mfrDataDIB = new MfrDataDIB();
		mfrDataDIB.setLength(8);
		mfrDataDIB.setManufacturerId(0x00c5);

		return mfrDataDIB;
	}

	public void setConnectionManager(final ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

}
