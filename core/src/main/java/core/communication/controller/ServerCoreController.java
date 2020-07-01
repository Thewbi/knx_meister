package core.communication.controller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import common.packets.ServiceIdentifier;
import common.utils.NetworkUtils;
import core.communication.Connection;
import core.packets.ConnectionRequestInformation;
import core.packets.DescriptionInformationBlockType;
import core.packets.DeviceInformationDIB;
import core.packets.HPAIStructure;
import core.packets.KNXLayer;
import core.packets.KNXPacket;
import core.packets.StructureType;

/**
 * Sends out messages to clients. Implements the server part of the KNX Core
 * protocol.
 */
public class ServerCoreController extends BaseController {

	private static final Logger LOG = LogManager.getLogger(ServerCoreController.class);

	/**
	 * ctor
	 *
	 * @throws SocketException
	 * @throws UnknownHostException
	 */
	public ServerCoreController(final String localInetAddress) throws SocketException, UnknownHostException {
		super(localInetAddress);
	}

	@Override
	public void knxPacket(final Connection connection, final DatagramSocket socket3671,
			final DatagramPacket datagramPacket, final KNXPacket knxPacket, final String label) throws IOException {

		HPAIStructure hpaiStructure = null;
		InetAddress inetAddress = null;
		int port = -1;

		DeviceInformationDIB deviceInformationDIB = null;

		switch (knxPacket.getHeader().getServiceIdentifier()) {

		case SEARCH_RESPONSE:
			hpaiStructure = (HPAIStructure) knxPacket.getStructureMap().get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);
			inetAddress = InetAddress.getByAddress(hpaiStructure.getIpAddress());
			port = hpaiStructure.getPort() & 0xFFFF;

			deviceInformationDIB = (DeviceInformationDIB) knxPacket.getDibMap()
					.get(DescriptionInformationBlockType.DEVICE_INFO);
			getDeviceMap().put(deviceInformationDIB.getDeviceSerialNumberAsString(), hpaiStructure);

			// send a description request to the device that answered the search request
			final KNXPacket sendDescriptionRequest = sendDescriptionRequest(inetAddress, port);
			connection.sendResponse(sendDescriptionRequest, new InetSocketAddress(inetAddress, port));
			break;

//		case DESCRIPTION_RESPONSE:
//
//			LOG.trace(knxPacket);
//
//			hpaiStructure = (HPAIStructure) knxPacket.getStructureMap().get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);
//			inetAddress = InetAddress.getByAddress(hpaiStructure.getIpAddress());
//			port = hpaiStructure.getPort() & 0xFFFF;
//
//			// send a connection request
//			final KNXPacket tunnelingConnectionRequest = sendConnectionRequest(knxPacket);
//
//			connection.sendResponse(tunnelingConnectionRequest, new InetSocketAddress(inetAddress, port));
//
//			// send a tunneling connection state request
//			break;

		case DESCRIPTION_RESPONSE:
			deviceInformationDIB = (DeviceInformationDIB) knxPacket.getDibMap()
					.get(DescriptionInformationBlockType.DEVICE_INFO);
			hpaiStructure = getDeviceMap().get(deviceInformationDIB.getDeviceSerialNumberAsString());

			final KNXPacket sendConnectionRequest = sendConnectionRequest(datagramPacket, knxPacket,
					hpaiStructure.getIpAddressAsObject(), hpaiStructure.getPort());

			final InetSocketAddress inetSocketAddress = new InetSocketAddress(hpaiStructure.getIpAddressAsObject(),
					hpaiStructure.getPort());
			connection.sendResponse(sendConnectionRequest, inetSocketAddress);
			break;

		// 0x0206
		case CONNECT_RESPONSE:
			LOG.info("Connected");
			LOG.info("Connected! Tunnel connection id is: " + knxPacket.getCommunicationChannelId() + " Sequence "
					+ knxPacket.getConnectionStatus());

			if (knxPacket.getCommunicationChannelId() > 1) {
				startThread(getClass().getName() + " CONNECT_RESPONSE", knxPacket.getConnection());
			}
			break;

		default:
			LOG.warn("Ignoring: " + knxPacket.getHeader().getServiceIdentifier().name());
			break;
		}
	}

//	/**
//	 * Specification: <br />
//	 * <br />
//	 * KNX Standard Core <br />
//	 * <br />
//	 * 7.8 Connection Management <br />
//	 * <br />
//	 * 7.8.1 CONNECT_REQUEST<br />
//	 * <br />
//	 *
//	 * @param inputKnxPacket
//	 * @return
//	 * @throws UnknownHostException
//	 */
//	private KNXPacket sendConnectionRequest(final KNXPacket inputKnxPacket) throws UnknownHostException {
//
//		final HPAIStructure controlEndpointHpaiStructure = new HPAIStructure();
//		controlEndpointHpaiStructure.setIpAddress(InetAddress.getByName(getLocalInetAddress()).getAddress());
//		controlEndpointHpaiStructure.setPort((short) POINT_TO_POINT_CONTROL_PORT);
//
//		final HPAIStructure dataEndpointHpaiStructure = new HPAIStructure();
//		dataEndpointHpaiStructure.setIpAddress(InetAddress.getByName(getLocalInetAddress()).getAddress());
//		dataEndpointHpaiStructure.setPort((short) POINT_TO_POINT_CONTROL_PORT);
//
////		final ConnectionHeader connectionHeader = new ConnectionHeader();
////		connectionHeader.setChannel(inputKnxPacket.getConnectionHeader().getChannel());
////		connectionHeader.setSequenceCounter(inputKnxPacket.getConnectionHeader().getSequenceCounter());
////		// status OK
////		connectionHeader.setReserved(0x00);
//
//		final KNXPacket knxPacket = new KNXPacket();
//		knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.CONNECT_REQUEST);
//		knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, controlEndpointHpaiStructure);
//		knxPacket.getStructureMap().put(StructureType.HPAI_DATA_ENDPOINT_UDP, dataEndpointHpaiStructure);
////		knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, hpaiStructure);
//
//		return knxPacket;
//	}

	/**
	 * Specification: <br />
	 * <br />
	 * KNX Standard Core <br />
	 * <br />
	 * 7.8 Connection Management <br />
	 * <br />
	 * 7.8.1 CONNECT_REQUEST<br />
	 * <br />
	 *
	 * @param inputKnxPacket
	 * @return
	 * @throws UnknownHostException
	 */
	private KNXPacket sendConnectionRequest(final DatagramPacket originalDatagramPacket,
			final KNXPacket originalKNXPacket, final InetAddress inetAddress, final int port) throws IOException {

		final HPAIStructure controlHPAIStructure = new HPAIStructure();
		controlHPAIStructure.setIpAddress(InetAddress.getByName(getLocalInetAddress()).getAddress());
		controlHPAIStructure.setPort((short) POINT_TO_POINT_CONTROL_PORT);

		final HPAIStructure dataHPAIStructure = new HPAIStructure();
		dataHPAIStructure.setIpAddress(InetAddress.getByName(getLocalInetAddress()).getAddress());
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
	}

	private KNXPacket sendDescriptionRequest(final InetAddress inetAddress, final int port) throws IOException {

		final HPAIStructure hpaiStructure = new HPAIStructure();
		hpaiStructure.setIpAddress(InetAddress.getByName(getLocalInetAddress()).getAddress());
		hpaiStructure.setPort((short) POINT_TO_POINT_CONTROL_PORT);

		final KNXPacket knxPacket = new KNXPacket();
		knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.DESCRIPTION_REQUEST);
		knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, hpaiStructure);

		return knxPacket;
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
		hpaiStructure.setIpAddress(InetAddress.getByName(getLocalInetAddress()).getAddress());
		hpaiStructure.setPort((short) POINT_TO_POINT_CONTROL_PORT);
		knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, hpaiStructure);

		final byte[] bytes = knxPacket.getBytes();

		final String group = "224.0.23.12";
		final int port = 3671;
		final int ttl = 4;

		final DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, InetAddress.getByName(group),
				port);

		final NetworkInterface networkInterface = NetworkUtils
				.findInterfaceByIP(InetAddress.getByName(getLocalInetAddress()).getAddress());

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
	}

	public void sendTunnelConnectionRequest(final MulticastSocket multicastSocket) throws UnknownHostException {

		LOG.info("sendTunnelConnectionRequest() MulticastSocket = " + multicastSocket);

		final KNXPacket knxPacket = new KNXPacket();

		// header
		knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.CONNECT_REQUEST);

		// HPAI - as a IP address, specify the IP address of the NIC you want to receive
		// the response on
		HPAIStructure hpaiStructure = new HPAIStructure();
		hpaiStructure.setIpAddress(InetAddress.getByName(getLocalInetAddress()).getAddress());
		hpaiStructure.setPort((short) POINT_TO_POINT_CONTROL_PORT);
		knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, hpaiStructure);

		hpaiStructure = new HPAIStructure();
		hpaiStructure.setIpAddress(InetAddress.getByName(getLocalInetAddress()).getAddress());
		hpaiStructure.setPort((short) POINT_TO_POINT_CONTROL_PORT);
		knxPacket.getStructureMap().put(StructureType.HPAI_DATA_ENDPOINT_UDP, hpaiStructure);

		final ConnectionRequestInformation connectionRequestInformation = new ConnectionRequestInformation();
		connectionRequestInformation.setStructureType(StructureType.TUNNELING_CONNECTION);
		connectionRequestInformation.setKnxLayer(0x02);
		connectionRequestInformation.setReserved(0x00);
		knxPacket.getStructureMap().put(StructureType.TUNNELING_CONNECTION, connectionRequestInformation);

		final InetSocketAddress bindInetSocketAddress = new InetSocketAddress(getLocalInetAddress(), 3671);
		final InetSocketAddress destInetSocketAddress = new InetSocketAddress("192.168.0.24", 3671);
		final InetAddress destInetAddress = InetAddress.getByName("192.168.0.24");

		final DatagramSocket datagramSocket = null;
		try {
//			datagramSocket = new DatagramSocket(bindInetSocketAddress);

			final byte[] bytes = knxPacket.getBytes();

//			final DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length);
			final DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, destInetSocketAddress);
//			datagramPacket.setAddress(destInetAddress);

			multicastSocket.send(datagramPacket);
//			datagramSocket.send(datagramPacket);

		} catch (final SocketException e) {
			LOG.error(e.getMessage(), e);
		} catch (final IOException e) {
			LOG.error(e.getMessage(), e);
		} finally {
//			if (datagramSocket != null) {
//				datagramSocket.close();
//				datagramSocket = null;
//			}
		}
	}

	@Override
	public boolean accepts(final DatagramPacket datagramPacket) {
		return false;
	}

	@Override
	public boolean accepts(final KNXPacket knxPacket) {
		switch (knxPacket.getHeader().getServiceIdentifier()) {
		case SEARCH_RESPONSE:
		case DESCRIPTION_RESPONSE:
		case CONNECT_RESPONSE:
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
