package test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.junit.jupiter.api.Test;

public class MulticastTest {

	@Test
	public void testMulticast() throws IOException, InterruptedException {

		// Which port should we send to
		final int port = 3671;

		// Which address
		final String group = "224.0.23.12";

		// Create the socket but we don't bind it as we are only going to send data
		final MulticastSocket multicastSocket = new MulticastSocket();

		// Note that we don't have to join the multicast group if we are only
		// sending data and not receiving

		// Fill the buffer with some data
		final byte buf[] = new byte[10];
		for (int i = 0; i < buf.length; i++) {
			buf[i] = (byte) i;
		}

		// Create a DatagramPacket
		final DatagramPacket pack = new DatagramPacket(buf, buf.length, InetAddress.getByName(group), port);

		// Do a send.
		multicastSocket.send(pack);

		Thread.sleep(3000);

		// And when we have finished sending data close the socket
		multicastSocket.close();
	}
}
