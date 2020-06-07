package core.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.common.Utils;
import core.conversion.KNXPacketConverter;
import core.packets.KNXPacket;

public abstract class BaseDatagramPacketCallback implements DatagramPacketCallback {

	private static final Logger LOG = LogManager.getLogger("BaseDatagramPacketCallback");

	private KNXPacketConverter knxPacketConverter;

	@Override
	public void datagramPacket(final Connection connection, final DatagramSocket socket,
			final DatagramPacket datagramPacket, final String label) throws UnknownHostException, IOException {

//		socket.send(datagramPacket);

		LOG.trace(Utils.integerToStringNoPrefix(datagramPacket.getData()));

		final KNXPacket knxPacket = knxPacketConverter.convert(datagramPacket.getData());

//		// filter all packets that have a 0.0.0.0 IP
//		final Structure structure = knxPacket.getStructureMap().get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);
//
//		if (structure != null && structure instanceof HPAIStructure) {
//
//			final HPAIStructure hpaiStructure = (HPAIStructure) structure;
////			if (NetworkUtils.compareIp(hpaiStructure.getIpAddress(), 0, 0, 0, 0)) {
////				return;
////			}
////			if (hpaiStructure.getPort() == 0) {
////				return;
////			}
//
//			if (NetworkUtils.compareIp(hpaiStructure.getIpAddress(), 0, 0, 0, 0)) {
//				LOG.warn("Rewriting IP 0000");
//				hpaiStructure.setIpAddress(new byte[] { (byte) 127, 0, 0, 1 });
//			}
//		}

//		System.out.println(knxPacket);

		LOG.info("<<< " + label + " " + knxPacket.getHeader().getServiceIdentifier().toString());

//		socket.send(datagramPacket);

		knxPacket(connection, socket, datagramPacket, knxPacket, label);
	}

	@Override
	public void setKnxPacketConverter(final KNXPacketConverter knxPacketConverter) {
		this.knxPacketConverter = knxPacketConverter;
	}
}
