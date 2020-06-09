package core.communication;

import java.net.DatagramSocket;

import core.api.pipeline.Pipeline;
import core.packets.ConnectionType;
import core.packets.KNXPacket;

public interface ConnectionManager {

	/**
	 * Connection via the knxPacket.getCommunicationChannelId() property.
	 */
	Connection retrieveConnection(KNXPacket knxPacket, DatagramSocket datagramSocket);

	Connection retrieveConnection(int communicationChannelId);

	Connection createNewConnection(DatagramSocket datagramSocket, ConnectionType connectionType);

	void closeConnection(int id);

	void setOutputPipeline(Pipeline<Object, Object> outputPipeline);

}
