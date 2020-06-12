package core.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.api.pipeline.Pipeline;
import core.packets.KNXPacket;

public class MulticastListenerReaderThread implements Runnable, DatagramPacketCallback {

	private static final String KNX_MULTICAST_IP = "224.0.23.12";

	private static final Logger LOG = LogManager.getLogger(MulticastListenerReaderThread.class);

	private boolean running;

	/**
	 * One of these callbacks receives the KNXPacket that went trough the input
	 * pipeline.
	 *
	 * @param datagramPacketCallback
	 */
	private final List<DatagramPacketCallback> datagramPacketCallbacks = new ArrayList<>();

	private final int bindPort;

	private Pipeline<Object, Object> inputPipeline;

	/**
	 * ctor
	 *
	 * @param bindPort
	 */
	public MulticastListenerReaderThread(final int bindPort) {
		this.bindPort = bindPort;
	}

	@Override
	public void run() {

		LOG.info("Start MulticastListener thread!");

		running = true;

		try {
			runMultiCastListener(this);
		} catch (final IOException e) {
			LOG.error(e.getMessage(), e);
		}

		LOG.info("Reader MulticastListener end!");
	}

	/**
	 * 8.6.3.1 General listen for UDP multicast on the standard KNX device routing
	 * multicast address 224.0.23.12:3671
	 *
	 * @param datagramPacketCallback
	 * @throws IOException
	 * @throws SocketException
	 * @throws UnknownHostException
	 */
	private void runMultiCastListener(final DatagramPacketCallback datagramPacketCallback)
			throws IOException, SocketException, UnknownHostException {

		try (MulticastSocket multicastSocket = new MulticastSocket(bindPort)) {

			multicastSocket.setReuseAddress(true);

			final InetAddress inetAddress = InetAddress.getByName(KNX_MULTICAST_IP);
			multicastSocket.joinGroup(inetAddress);

			LOG.info("Multicast listener on " + KNX_MULTICAST_IP + " started.");

			while (running) {

				final byte[] buf = new byte[1024];
				final DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
				multicastSocket.receive(datagramPacket);

				// use the pipeline to convert the input from the socket to a KNXPacket that the
				// system can use
				KNXPacket knxPacket = null;
				try {

					Object[] data = new Object[2];
					data[0] = multicastSocket;
					data[1] = datagramPacket;

					data = (Object[]) inputPipeline.execute(data);
					if (data == null) {
						continue;
					}

					knxPacket = (KNXPacket) data[1];
					if (knxPacket == null) {
						continue;
					}
				} catch (final Exception e) {
					LOG.error(e.getMessage(), e);
					throw new IOException(e);
				}

				// retrieve the connection
				if (knxPacket.getConnection() == null) {
					final int communicationChannelId = knxPacket.getCommunicationChannelId();
					LOG.warn("Connection with communicationChannelId = {} is not known! No response is sent!",
							communicationChannelId);
				} else {
					datagramPacketCallback.knxPacket(knxPacket.getConnection(), multicastSocket, datagramPacket,
							knxPacket, "Multicast");
				}
			}
		}
	}

	public List<DatagramPacketCallback> getDatagramPacketCallbacks() {
		return datagramPacketCallbacks;
	}

	public void setInputPipeline(final Pipeline<Object, Object> inputPipeline) {
		this.inputPipeline = inputPipeline;
	}

	@Override
	public void datagramPacket(final Connection connection, final DatagramSocket socket,
			final DatagramPacket datagramPacket, final String label) throws UnknownHostException, IOException {

		if (CollectionUtils.isEmpty(datagramPacketCallbacks)) {
			throw new RuntimeException("No listeners registered! System is malconfigured!");
		}

		for (final DatagramPacketCallback datagramPacketCallback : datagramPacketCallbacks) {

			if (datagramPacketCallback.accepts(datagramPacket)) {
				datagramPacketCallback.datagramPacket(connection, socket, datagramPacket, label);
				return;
			}
		}

		throw new RuntimeException("No listener accepts the datagram packet" + datagramPacket);
	}

	@Override
	public void knxPacket(final Connection connection, final DatagramSocket socket, final DatagramPacket datagramPacket,
			final KNXPacket knxPacket, final String label) throws UnknownHostException, IOException {

		boolean packetAcceptedAtLeastOnce = false;

		if (CollectionUtils.isEmpty(datagramPacketCallbacks)) {
			throw new RuntimeException("No listeners registered! System is malconfigured!");
		}

		for (final DatagramPacketCallback datagramPacketCallback : datagramPacketCallbacks) {

			if (datagramPacketCallback.accepts(knxPacket)) {

				packetAcceptedAtLeastOnce = true;

				datagramPacketCallback.knxPacket(connection, socket, datagramPacket, knxPacket, label);

				// either only ask the first accepting controller or as all accepting
				// controllers
//				return;
			}
		}

		if (!packetAcceptedAtLeastOnce) {
			throw new RuntimeException("No listener accepts the KNX packet" + knxPacket);
		}
	}

	@Override
	public boolean accepts(final DatagramPacket datagramPacket) {
		return true;
	}

	@Override
	public boolean accepts(final KNXPacket knxPacket) {
		return true;
	}

}
