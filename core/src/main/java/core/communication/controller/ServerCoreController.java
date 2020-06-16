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

import core.common.NetworkUtils;
import core.communication.Connection;
import core.packets.DescriptionInformationBlockType;
import core.packets.DeviceInformationDIB;
import core.packets.HPAIStructure;
import core.packets.KNXPacket;
import core.packets.ServiceIdentifier;
import core.packets.StructureType;

/**
 * Sends out messages to clients. Implements the server part of the KNX Core
 * protocol.
 *
 * @author U5353
 *
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

			final KNXPacket sendDescriptionRequest = sendDescriptionRequest(inetAddress, port);
			connection.sendResponse(sendDescriptionRequest, new InetSocketAddress(inetAddress, port));
			break;

		default:
			getLogger().warn("Ignoring: " + knxPacket.getHeader().getServiceIdentifier().name());
			break;
		}
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

		getLogger().info("sendSearchRequest() ...");

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

	@Override
	public boolean accepts(final DatagramPacket datagramPacket) {
		return false;
	}

	@Override
	public boolean accepts(final KNXPacket knxPacket) {
		switch (knxPacket.getHeader().getServiceIdentifier()) {
		case SEARCH_RESPONSE:
		case DESCRIPTION_RESPONSE:
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
