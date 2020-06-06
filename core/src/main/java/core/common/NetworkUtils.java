package core.common;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

public final class NetworkUtils {

	private static final String ADAPTER_NAME = "eth5";

	private NetworkUtils() {
		// no instances of this class
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

//	public static void multicast(final byte[] payload) throws IOException {
//		final MulticastSocket socket = new MulticastSocket(3671);
//		final InetAddress group = InetAddress.getByName("224.0.23.12");
//		socket.joinGroup(group);
//		socket.setSoTimeout(5000);
//		while (true) {
//			final DatagramPacket packet = new DatagramPacket(payload, payload.length);
//			socket.receive(packet);
//			final String received = new String(packet.getData(), 0, packet.getLength());
//			if ("end".equals(received)) {
//				break;
//			}
//		}
//		socket.leaveGroup(group);
//		socket.close();
//	}

	public static void sendMulticast(final byte[] payload) throws IOException {

//		final DatagramSocket socket = new DatagramSocket();

//		final MulticastSocket socket = new MulticastSocket(3671);
		final MulticastSocket socket = new MulticastSocket();
		socket.setReuseAddress(true);
		socket.setSoTimeout(5000);
//		socket.bind(new InetSocketAddress("192.168.2.1", 40000));

		// ipconfig /all
		final Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		for (final NetworkInterface netint : Collections.list(nets)) {
			displayInterfaceInformation(netint);
		}
		final NetworkInterface nic = NetworkInterface.getByName(ADAPTER_NAME);
//		System.out.println("eth0: " + nic + " MAC: " + Utils.integerToStringNoPrefix(nic.getHardwareAddress()));

		socket.joinGroup(new InetSocketAddress("224.0.23.12", 3671), NetworkInterface.getByName(ADAPTER_NAME));

//		socket.connect(address, 65000);
//		final int port = socket.getPort();
//		System.out.println(port);
//		socket.setReuseAddress(true);
//		socket.setSoTimeout(5000);
//		socket.joinGroup(groupInetAddress);

		final InetAddress groupInetAddress = InetAddress.getByName("224.0.23.12");
		final DatagramPacket packet = new DatagramPacket(payload, payload.length, groupInetAddress, 3671);
		socket.send(packet);

//		final byte[] receiveBuffer = new byte[1024];
//
//		final DatagramPacket incomingDatagramPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
//		socket.receive(incomingDatagramPacket);

		socket.close();
	}

	public static void displayInterfaceInformation(final NetworkInterface netint) throws SocketException {

		System.out.printf("Display name: %s\n", netint.getDisplayName());
		System.out.printf("Name: %s\n", netint.getName());
		final Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
		for (final InetAddress inetAddress : Collections.list(inetAddresses)) {
			System.out.printf("InetAddress: %s\n", inetAddress);
		}
		System.out.printf("\n");
	}

	public static NetworkInterface findInterfaceByIP(final byte[] ip) throws SocketException {

		final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
		for (final NetworkInterface networkInterface : Collections.list(networkInterfaces)) {
			final Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
			while (inetAddresses.hasMoreElements()) {
				final InetAddress nextElement = inetAddresses.nextElement();

				if (nextElement instanceof Inet4Address) {

					final Inet4Address inet4Address = (Inet4Address) nextElement;
//					System.out.println(inet4Address.getAddress());

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

}
