package core.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;

import core.packets.KNXPacket;

public interface Connection {

	DatagramSocket getDatagramSocket();

	// TODO: change to KNXPacket and insert a output pipeline that converts a
	// DatagramPacket to a KNX packet
	void sendResponse(DatagramPacket datagramPacket) throws IOException;

	void sendResponse(KNXPacket knxPacket, SocketAddress socketAddress) throws IOException;

	void sendResponse(DatagramPacket datagramPacket, InetAddress inetAddress, int port) throws IOException;

}
