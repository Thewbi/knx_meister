package core.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReaderThread implements Runnable {

	private static final Logger LOG = LogManager.getLogger(ReaderThread.class);

	private boolean running;

	private DatagramPacketCallback datagramPacketCallback;

	@Override
	public void run() {

		LOG.info("Start reader thread!");

		running = true;

//		DatagramSocket datagramSocket = null;
		try {
			final DatagramSocket datagramSocket = new DatagramSocket(null);

//			final InetSocketAddress address = new InetSocketAddress("192.168.2.1", 65000);
//			final InetSocketAddress address = new InetSocketAddress("127.0.0.1", 65000);
			final InetSocketAddress address = new InetSocketAddress("127.0.0.1", Controller.POINT_TO_POINT_PORT);
//			final InetSocketAddress address = new InetSocketAddress("192.168.56.1", 65000);

			datagramSocket.bind(address);
//			datagramSocket.bind(null);

			while (running) {

				final byte[] buffer = new byte[1024];
				final DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);

				LOG.info("Reader thread listening ...");

				datagramSocket.receive(datagramPacket);

				LOG.info("Reader thread received.");

//				LOG.info(datagramPacket.getSocketAddress());
//				LOG.info(datagramPacket.getAddress());

//				final byte[] data = new String("hello").getBytes(StandardCharsets.US_ASCII);
//				final DatagramPacket outDatagramPacket = new DatagramPacket(data, data.length,
//						datagramPacket.getSocketAddress());
//				datagramSocket.send(outDatagramPacket);

//				datagramSocket.send(datagramPacket);

				datagramPacketCallback.datagramPacket(datagramSocket, datagramPacket, "ReaderThread");

//				new Thread(new Runnable() {
//
//					@Override
//					public void run() {
//
//						if (datagramPacketCallback != null) {
//							try {
//								Thread.sleep(300);
//								datagramPacketCallback.datagramPacket(datagramSocket, datagramPacket, "ReaderThread");
//							} catch (final UnknownHostException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							} catch (final IOException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							} catch (final InterruptedException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//						}
//					}
//				}).start();

			}

		} catch (final IOException e) {
			LOG.error(e.getMessage(), e);
		} finally {
//			if (datagramSocket != null) {
//				datagramSocket.close();
//			}
		}

		LOG.info("Reader thread end!");
	}

	public void setDatagramPacketCallback(final DatagramPacketCallback datagramPacketCallback) {
		this.datagramPacketCallback = datagramPacketCallback;
	}

}
