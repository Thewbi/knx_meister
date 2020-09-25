package core.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;

import api.exception.CommunicationException;
import api.exception.SequenceCounterException;
import core.packets.KNXPacket;

public interface DatagramPacketCallback {

    void datagramPacket(Connection connection, DatagramSocket socket, DatagramPacket datagramPacket, String label)
            throws UnknownHostException, IOException;

    void knxPacket(Connection connection, DatagramSocket socket, DatagramPacket datagramPacket, KNXPacket knxPacket,
            String label) throws UnknownHostException, IOException, SequenceCounterException, CommunicationException;

    boolean accepts(DatagramPacket datagramPacket);

    boolean accepts(KNXPacket knxPacket);

}
