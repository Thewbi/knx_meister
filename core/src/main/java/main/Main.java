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

//		pingGoogle();

		final Controller controller = knxController();

		// start a reader thread for a point to point connection on 192.168.2.1:65000
		final ReaderThread readerThread = new ReaderThread();
		readerThread.setDatagramPacketCallback(controller);

		new Thread(readerThread).start();

		final MulticastListenerThread multicastListenerThread = new MulticastListenerThread();
		multicastListenerThread.setDatagramPacketCallback(controller);
		new Thread(multicastListenerThread).start();

//		controller.sendSearchRequest();
	}

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
