package core.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;

import org.apache.logging.log4j.Logger;

public abstract class BaseDatagramPacketCallback implements DatagramPacketCallback {

	protected abstract Logger getLogger();

	@Override
	public void datagramPacket(final Connection connection, final DatagramSocket socket,
			final DatagramPacket datagramPacket, final String label) throws UnknownHostException, IOException {
		throw new RuntimeException("Not implemented!");
	}

}
