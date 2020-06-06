package core.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.conversion.KNXPacketConverter;
import core.packets.KNXPacket;
import core.packets.Structure;
import core.packets.StructureType;

public class MulticastListenerThread implements Runnable {

	private static final String KNX_MULTICAST_IP = "224.0.23.12";

	private static final Logger LOG = LogManager.getLogger(MulticastListenerThread.class);

	private boolean running;

	private DatagramPacketCallback datagramPacketCallback;

	@Override
	public void run() {

		LOG.info("Start MulticastListener thread!");

		running = true;

		try {
			startMultiCastListener(datagramPacketCallback);
		} catch (final IOException e) {
			LOG.error(e.getMessage(), e);
		}

		LOG.info("Reader MulticastListener end!");
	}

	// 8.6.3.1 General
	// listen for UDP multicast on the standard KNX device routing multicast address
	// 224.0.23.12:3671
	private void startMultiCastListener(final DatagramPacketCallback datagramPacketCallback)
			throws IOException, SocketException, UnknownHostException {

		try (MulticastSocket multicastSocket = new MulticastSocket(3671)) {

			multicastSocket.setReuseAddress(true);

//			final InetSocketAddress inetSocketAddress = new InetSocketAddress("224.0.23.12", 3671);
//			socket.joinGroup(inetSocketAddress, NetworkInterface.getByName("eth5"));

			final InetAddress inetAddress = InetAddress.getByName(KNX_MULTICAST_IP);
			multicastSocket.joinGroup(inetAddress);

//				System.out.println("Multicast listener on " + KNX_MULTICAST_IP + " started.");
			LOG.info("Multicast listener on " + KNX_MULTICAST_IP + " started.");

			while (true) {

				final byte[] buf = new byte[1024];
				final DatagramPacket packet = new DatagramPacket(buf, buf.length);
				multicastSocket.receive(packet);

//					socket.send(packet);

//					System.out.println(packet);
//					System.out.println(Utils.integerToStringNoPrefix(packet.getData()));

				final KNXPacketConverter knxPacketConverter = new KNXPacketConverter();
				final KNXPacket knxPacket = knxPacketConverter.convert(packet.getData());

				final String prefix = "<<< Multicast: " + knxPacket.getHeader().getServiceIdentifier().name();

				LOG.trace(prefix);

				// filter all packets that have a 0.0.0.0 IP
				final Structure structure = knxPacket.getStructureMap().get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);

//					if (structure != null && structure instanceof HPAIStructure) {
				//
//						final HPAIStructure hpaiStructure = (HPAIStructure) structure;
				//
////						if (NetworkUtils.compareIp(hpaiStructure.getIpAddress(), 0, 0, 0, 0)) {
////							LOG.warn(prefix + " Ignoring packet because of address 0.0.0.0");
////							continue;
////						}
////						if (hpaiStructure.getPort() == 0) {
////							LOG.warn(prefix + " Ignoring packet because of port 0");
////							continue;
////						}
				//
//						if (NetworkUtils.compareIp(hpaiStructure.getIpAddress(), 0, 0, 0, 0)) {
////							LOG.warn("Rewriting IP 0.0.0.0 to 127.0.0.1");
////							hpaiStructure.setIpAddress(new byte[] { (byte) 127, 0, 0, 1 });
				//
//							LOG.warn("Rewriting IP 0.0.0.0 to 224.0.23.12");
//							hpaiStructure.setIpAddress(new byte[] { (byte) 224, 0, 23, 12 });
				//
////							LOG.warn("Rewriting IP 0.0.0.0 to 127.0.0.108");
////							hpaiStructure.setIpAddress(new byte[] { (byte) 127, 0, 0, (byte) 108 });
				//
//						}
				//
//						if (hpaiStructure.getPort() == 0) {
//							LOG.warn("Rewriting port 0 to 3671");
//							hpaiStructure.setPort((short) 3671);
//						}
				//
//					}

				datagramPacketCallback.knxPacket(multicastSocket, packet, knxPacket, "Multicast");
			}
		}
	}

	public DatagramPacketCallback getDatagramPacketCallback() {
		return datagramPacketCallback;
	}

	public void setDatagramPacketCallback(final DatagramPacketCallback datagramPacketCallback) {
		this.datagramPacketCallback = datagramPacketCallback;
	}

}
