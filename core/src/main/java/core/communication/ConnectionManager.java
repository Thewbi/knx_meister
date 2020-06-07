package core.communication;

import java.net.DatagramSocket;

import core.api.pipeline.Pipeline;
import core.packets.ConnectionType;
import core.packets.KNXPacket;

public interface ConnectionManager {

	Connection retrieveConnection(KNXPacket knxPacket, DatagramSocket datagramSocket);

	Connection createNewConnection(DatagramSocket datagramSocket, ConnectionType connectionType);

	void closeConnection(int id);

	void setOutputPipeline(Pipeline<Object, Object> outputPipeline);

}
