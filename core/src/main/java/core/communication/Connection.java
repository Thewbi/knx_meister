package core.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;

import api.exception.CommunicationException;
import api.exception.SequenceCounterException;
import core.packets.ConnectionType;
import core.packets.HPAIStructure;
import core.packets.KNXPacket;

public interface Connection {

    DatagramSocket getDatagramSocket();

    // TODO: change to KNXPacket and insert a output pipeline that converts a
    // DatagramPacket to a KNX packet
    void sendResponse(DatagramPacket datagramPacket) throws IOException;

    void sendRequest(KNXPacket knxPacket) throws IOException, CommunicationException;

    void sendResponse(KNXPacket knxPacket) throws IOException, SequenceCounterException;

    void sendResponse(KNXPacket knxPacket, SocketAddress socketAddress) throws IOException;

    void sendResponse(DatagramPacket datagramPacket, InetAddress inetAddress, int port) throws IOException;

    ConnectionType getConnectionType();

    void setConnectionType(ConnectionType connectionType);

    int getId();

    void setId(int id);

    HPAIStructure getControlEndpoint();

    void setControlEndpoint(HPAIStructure controlEndpoint);

    HPAIStructure getDataEndpoint();

    void setDataEndpoint(HPAIStructure dataEndpoint);

    void sendData(KNXPacket knxPacket) throws IOException, CommunicationException;

    int getSendSequenceCounter();

    void setSendSequenceCounter(int sendSequenceCounter);

    int getReceiveSequenceCounter();

    void setReceiveSequenceCounter(int receiveSequenceCounter);

    long getTimestampLastUsed();

    /**
     * Touch the connection so it is deemed alive and should not be purged
     * 
     * @param timestampLastUsed
     */
    void setTimestampLastUsed(long timestampLastUsed);

}
