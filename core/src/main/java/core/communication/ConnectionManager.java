package core.communication;

import java.net.DatagramSocket;
import java.util.Optional;

import api.pipeline.Pipeline;
import core.packets.ConnectionType;
import core.packets.KNXPacket;

public interface ConnectionManager {

    /**
     * what is the maximum age a connection can have before a running purge check
     * removes it
     */
    final int CONNECTION_TIMEOUT = 1000 * 60 * 5;

    final int OUTPUT_PERIOD_IN_MILLIS = 5000;

    /** how frequently is the purge check executed */
    final int PURGE_PERIOD_IN_MILLIS = 1000 * 60;

    /**
     * Connection via the knxPacket.getCommunicationChannelId() property.
     */
    Connection retrieveConnection(KNXPacket knxPacket, DatagramSocket datagramSocket);

    Connection retrieveConnection(int communicationChannelId);

    Connection createNewConnection(DatagramSocket datagramSocket, ConnectionType connectionType);

    Connection createNewConnection(DatagramSocket datagramSocket, int id, ConnectionType connectionType);

    void closeConnection(int id);

    void setOutputPipeline(Pipeline<Object, Object> outputPipeline);

    /**
     * Select one of the currently active connections, which means it has the
     * following properties:
     * <ol>
     * <li />Alive (has been recently used)
     * <li />Is a tunneling connection (has the tunneling type)
     * </ol>
     * It is important to use a live connection because the communication partner
     * has not abandoned that connection yet and is actively listening to it. <br/>
     * <br/>
     * It is important to use a tunneling connection because data is only processed
     * by the connection partner if it is send over a tunneling connection. Data
     * send over a normal connection is just ignored on the receiver side.
     *
     * @return
     */
    Optional<Connection> getLiveTunnelingConnection();

}
