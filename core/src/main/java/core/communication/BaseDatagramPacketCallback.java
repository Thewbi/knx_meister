package core.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;

import org.apache.logging.log4j.Logger;

import core.common.Converter;
import core.common.Utils;
import core.packets.KNXPacket;

public abstract class BaseDatagramPacketCallback implements DatagramPacketCallback {

	private Converter<byte[], KNXPacket> coreKNXPacketConverter;

	protected abstract Logger getLogger();

	@Override
	public void datagramPacket(final Connection connection, final DatagramSocket socket,
			final DatagramPacket datagramPacket, final String label) throws UnknownHostException, IOException {

		getLogger().trace(Utils.integerToStringNoPrefix(datagramPacket.getData()));

		final KNXPacket knxPacket = coreKNXPacketConverter.convert(datagramPacket.getData());

		getLogger().trace("<<< " + label + " " + knxPacket.getHeader().getServiceIdentifier().toString());

		knxPacket(connection, socket, datagramPacket, knxPacket, label);
	}

//	@Override
//	public void setCoreKNXPacketConverter(final Converter<byte[], KNXPacket> coreKNXPacketConverter) {
//		this.coreKNXPacketConverter = coreKNXPacketConverter;
//	}
}
