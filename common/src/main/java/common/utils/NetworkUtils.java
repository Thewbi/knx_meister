package common.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class NetworkUtils {

	private static final Logger LOG = LogManager.getLogger(NetworkUtils.class);

	public static final int OBJECT_SERVER_PROTOCOL_PORT = 12004;

	public static final String KNX_MULTICAST_IP = "224.0.23.12";

	private static final String ADAPTER_NAME = "eth5";

//	private static String hostAddress = null;
	private static String hostAddress = "192.168.2.1";
//	private static String hostAddress = "127.0.0.1";
//	private static String hostAddress = "192.168.0.108";
//	private static String hostAddress = "172.18.60.118";
//	private static String hostAddress = "0.0.0.0";

	/**
	 * ctor
	 */
	private NetworkUtils() {
		// no instances of this class
	}

	public static String retrieveLocalIP() throws UnknownHostException, SocketException {

		if (StringUtils.isBlank(hostAddress)) {

			try (final DatagramSocket socket = new DatagramSocket()) {

				socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
				hostAddress = socket.getLocalAddress().getHostAddress();
			}
		}

		LOG.info("Local hostname is: " + hostAddress);

		return hostAddress;
	}

	public static List<InetAddress> listAllBroadcastAddresses() throws SocketException {

		final List<InetAddress> broadcastList = new ArrayList<>();
		final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

		while (interfaces.hasMoreElements()) {

			final NetworkInterface networkInterface = interfaces.nextElement();

			// ignore loopback and disabled interfaces
			if (networkInterface.isLoopback() || !networkInterface.isUp()) {
				continue;
			}

			networkInterface.getInterfaceAddresses().stream().map(a -> a.getBroadcast()).filter(Objects::nonNull)
					.forEach(broadcastList::add);
		}

		return broadcastList;
	}

	public static void broadcast(final InetAddress destinationAddress, final int destinationPort, final byte[] payload)
			throws IOException, SocketTimeoutException {

		final DatagramSocket socket = new DatagramSocket();
		socket.setBroadcast(true);

		final DatagramPacket datagramPacket = new DatagramPacket(payload, payload.length, destinationAddress,
				destinationPort);
		socket.send(datagramPacket);

		socket.setSoTimeout(5000);

		final byte[] receiveBuffer = new byte[1024];

		final DatagramPacket incomingDatagramPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
		socket.receive(incomingDatagramPacket);

		socket.close();
	}

	public static void sendMulticast(final byte[] payload) throws IOException {

		final MulticastSocket socket = new MulticastSocket();
		socket.setReuseAddress(true);
		socket.setSoTimeout(5000);

		// ipconfig /all
		final Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		for (final NetworkInterface netint : Collections.list(nets)) {
			displayInterfaceInformation(netint);
		}
		socket.joinGroup(new InetSocketAddress(KNX_MULTICAST_IP, 3671), NetworkInterface.getByName(ADAPTER_NAME));

		final InetAddress groupInetAddress = InetAddress.getByName(KNX_MULTICAST_IP);
		final DatagramPacket packet = new DatagramPacket(payload, payload.length, groupInetAddress, 3671);
		socket.send(packet);

		socket.close();
	}

	public static void displayInterfaceInformation(final NetworkInterface netint) throws SocketException {

		LOG.info("Display name: %s\n", netint.getDisplayName());
		LOG.info("Name: %s\n", netint.getName());
		final Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
		for (final InetAddress inetAddress : Collections.list(inetAddresses)) {
			LOG.info("InetAddress: %s\n", inetAddress);
		}
		LOG.info("\n");
	}

	public static NetworkInterface findInterfaceByIP(final byte[] ip) throws SocketException {

		final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
		for (final NetworkInterface networkInterface : Collections.list(networkInterfaces)) {
			final Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
			while (inetAddresses.hasMoreElements()) {
				final InetAddress nextElement = inetAddresses.nextElement();

				if (nextElement instanceof Inet4Address) {

					final Inet4Address inet4Address = (Inet4Address) nextElement;

					if (Arrays.equals(inet4Address.getAddress(), ip)) {
						return networkInterface;
					}
				}
			}
		}

		return null;
	}

	public static boolean compareIp(final byte[] bytes, final int a, final int b, final int c, final int d) {

		if (bytes == null) {
			return false;
		}
		if (bytes[0] != a) {
			return false;
		}
		if (bytes[1] != b) {
			return false;
		}
		if (bytes[2] != c) {
			return false;
		}
		if (bytes[3] != d) {
			return false;
		}
		return true;
	}

	public static String printIPAddress(final byte[] ipAddress) {
		return (ipAddress[0] & 0xFF) + "." + (ipAddress[1] & 0xFF) + "." + (ipAddress[2] & 0xFF) + "."
				+ (ipAddress[3] & 0xFF);
	}

	public static Object printMACAddress(final byte[] deviceMacAddress) {

		final StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < 6; i++) {

			if (i > 0) {
				stringBuilder.append(":");
			}
			stringBuilder.append(String.format("%1$02X", deviceMacAddress[i] & 0xFF));
		}

		return stringBuilder.toString();
	}

	public static short toNetworkOrder(final short physicalAddress) {

		final byte byte0 = (byte) (physicalAddress & 0xFF);
		final byte byte1 = (byte) ((physicalAddress >> 8) & 0xFF);

		return (short) Utils.bytesToUnsignedShort(byte0, byte1, true);
	}

}
