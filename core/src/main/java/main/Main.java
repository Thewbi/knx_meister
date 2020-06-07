package main;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.communication.Controller;
import core.communication.MulticastListenerThread;
import core.communication.ReaderThread;
import core.conversion.KNXPacketConverter;

public class Main {

	private static final Logger LOG = LogManager.getLogger("Main");

	public static void main(final String[] args) throws IOException {

		// https://github.com/apache/dubbo/issues/2423
		//
		// on a macbook, the JVM prioritizes IPv6 interfaces over
		// IPv4 interfaces. Force the JVM to use IPv4.
		System.setProperty("java.net.preferIPv4Stack", "true");

		final InetAddress inetAddress = InetAddress.getLocalHost();
		LOG.info("IP of my system is := " + inetAddress.getHostAddress());

//		pingGoogle();

		final Controller controller = knxController();

		// start a reader thread for a point to point connection on 192.168.2.1:65000
		final ReaderThread readerThread = new ReaderThread(Controller.POINT_TO_POINT_PORT);
		readerThread.setDatagramPacketCallback(controller);
		new Thread(readerThread).start();

//		final ReaderThread controlReaderThread = new ReaderThread(Controller.POINT_TO_POINT_CONTROL_PORT);
//		controlReaderThread.setDatagramPacketCallback(controller);
//		new Thread(controlReaderThread).start();
//
//		final ReaderThread dataReaderThread = new ReaderThread(Controller.POINT_TO_POINT_DATA_PORT);
//		dataReaderThread.setDatagramPacketCallback(controller);
//		new Thread(dataReaderThread).start();

		final MulticastListenerThread multicastListenerThread = new MulticastListenerThread();
		multicastListenerThread.setDatagramPacketCallback(controller);
		new Thread(multicastListenerThread).start();

//		controller.sendSearchRequest();
	}

	@SuppressWarnings("unused")
	private static void pingGoogle() throws IOException {

		final String address = InetAddress.getByName("www.google.com").getHostAddress();
		final InetAddress inetAddress = InetAddress.getByName(address);
//		System.out.println("Sending Ping Request to " + address);

		if (inetAddress.isReachable(50000)) {
			System.out.println("Host is reachable");
			LOG.info("Host is reachable");
		} else {
//			System.out.println("Host is not reachable");
			LOG.info("Host is not reachable");
		}
	}

	private static Controller knxController() throws IOException {

		final KNXPacketConverter knxPacketConverter = new KNXPacketConverter();

		final Controller controller = new Controller();
		controller.setKnxPacketConverter(knxPacketConverter);

		return controller;
	}
}
