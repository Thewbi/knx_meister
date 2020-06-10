package core.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;

import org.apache.logging.log4j.Logger;

public abstract class BaseDatagramPacketCallback implements DatagramPacketCallback {

//	private Converter<byte[], KNXPacket> coreKNXPacketConverter;

	protected abstract Logger getLogger();

	@Override
	public void datagramPacket(final Connection connection, final DatagramSocket socket,
			final DatagramPacket datagramPacket, final String label) throws UnknownHostException, IOException {
		throw new RuntimeException("Not implemented!");
	}

//
//	@Override
//	public void datagramPacket(final Connection connection, final DatagramSocket socket,
//			final DatagramPacket datagramPacket, final String label) throws UnknownHostException, IOException {
//
//		getLogger().trace(Utils.integerToStringNoPrefix(datagramPacket.getData()));
//
//		final KNXPacket knxPacket = coreKNXPacketConverter.convert(datagramPacket.getData());
//
//		getLogger().trace("<<< " + label + " " + knxPacket.getHeader().getServiceIdentifier().toString());
//
//		knxPacket(connection, socket, datagramPacket, knxPacket, label);
//	}
}
