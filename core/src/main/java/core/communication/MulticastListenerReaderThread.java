package core.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.api.pipeline.Pipeline;
import core.packets.KNXPacket;

public class MulticastListenerReaderThread implements Runnable {

	private static final String KNX_MULTICAST_IP = "224.0.23.12";

	private static final Logger LOG = LogManager.getLogger(MulticastListenerReaderThread.class);

	private boolean running;

	private DatagramPacketCallback datagramPacketCallback;

	private final int bindPort;

	private Pipeline<Object, Object> inputPipeline;

	private ConnectionManager connectionManager;

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
			runMultiCastListener(datagramPacketCallback);
		} catch (final IOException e) {
			LOG.error(e.getMessage(), e);
		}

		LOG.info("Reader MulticastListener end!");
	}

	// 8.6.3.1 General
	// listen for UDP multicast on the standard KNX device routing multicast address
	// 224.0.23.12:3671
	private void runMultiCastListener(final DatagramPacketCallback datagramPacketCallback)
			throws IOException, SocketException, UnknownHostException {

		try (MulticastSocket multicastSocket = new MulticastSocket(bindPort)) {

			multicastSocket.setReuseAddress(true);

			final InetAddress inetAddress = InetAddress.getByName(KNX_MULTICAST_IP);
			multicastSocket.joinGroup(inetAddress);

			LOG.info("Multicast listener on " + KNX_MULTICAST_IP + " started.");

			while (running) {

				final byte[] buf = new byte[1024];
				final DatagramPacket packet = new DatagramPacket(buf, buf.length);
				multicastSocket.receive(packet);

				// use the pipeline to convert the input from the socket to a KNXPacket that the
				// system can use
				KNXPacket knxPacket = null;
				try {
					knxPacket = (KNXPacket) inputPipeline.execute(packet);
				} catch (final Exception e) {
					LOG.error(e.getMessage(), e);
					throw new IOException(e);
				}

				if (knxPacket == null) {
					continue;
				}

				// retrieve the connection
				final Connection connection = connectionManager.retrieveConnection(knxPacket, multicastSocket);

				datagramPacketCallback.knxPacket(connection, multicastSocket, packet, knxPacket, "Multicast");
			}
		}
	}

	public DatagramPacketCallback getDatagramPacketCallback() {
		return datagramPacketCallback;
	}

	/**
	 * This callback receives the KNXPacket that went trough the input pipeline.
	 *
	 * @param datagramPacketCallback
	 */
	public void setDatagramPacketCallback(final DatagramPacketCallback datagramPacketCallback) {
		this.datagramPacketCallback = datagramPacketCallback;
	}

	public void setInputPipeline(final Pipeline<Object, Object> inputPipeline) {
		this.inputPipeline = inputPipeline;
	}

	public void setConnectionManager(final ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

}
