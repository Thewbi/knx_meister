package core.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.common.Utils;

public class PointToPointReaderThread implements Runnable {

	private static final Logger LOG = LogManager.getLogger(PointToPointReaderThread.class);

	private boolean running;

	private DatagramPacketCallback datagramPacketCallback;

	private final String ipAddress;

	private final int bindPort;

	/**
	 * ctor
	 *
	 * @param bindPort
	 */
	public PointToPointReaderThread(final String ipAddress, final int bindPort) {
		this.ipAddress = ipAddress;
		this.bindPort = bindPort;
	}

	@Override
	public void run() {

		LOG.info("Start reader thread!");

		running = true;

		try {
			runPointToPointListener();
		} catch (final IOException e) {
			LOG.error(e.getMessage(), e);
		}

		LOG.info("Reader thread end!");
	}

	private void runPointToPointListener() throws IOException, SocketException, UnknownHostException {

		try (DatagramSocket datagramSocket = new DatagramSocket(null)) {

			final InetSocketAddress address = new InetSocketAddress(ipAddress, bindPort);
			datagramSocket.bind(address);

			LOG.info("PointToPoint listener on " + ipAddress + " started.");

			while (running) {

				final byte[] buffer = new byte[1024];
				final DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);

				LOG.info("Reader thread listening ...");

				datagramSocket.receive(datagramPacket);

				LOG.info("Reader thread received. {}", Utils.retrieveCurrentTimeAsString());

				// TODO: put in connection instead of null
				datagramPacketCallback.datagramPacket(null, datagramSocket, datagramPacket, "ReaderThread");
			}
		}
	}

	public void setDatagramPacketCallback(final DatagramPacketCallback datagramPacketCallback) {
		this.datagramPacketCallback = datagramPacketCallback;
	}

}
