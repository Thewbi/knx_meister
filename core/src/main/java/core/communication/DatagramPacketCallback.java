package core.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;

import core.conversion.KNXPacketConverter;
import core.packets.KNXPacket;

public interface DatagramPacketCallback {

	void datagramPacket(DatagramSocket socket, DatagramPacket datagramPacket, String label)
			throws UnknownHostException, IOException;

	void knxPacket(DatagramSocket socket, final DatagramPacket datagramPacket, KNXPacket knxPacket, String label)
			throws UnknownHostException, IOException;

	void setKnxPacketConverter(KNXPacketConverter knxPacketConverter);

}
