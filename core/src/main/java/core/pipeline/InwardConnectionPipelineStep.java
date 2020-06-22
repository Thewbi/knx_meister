package core.pipeline;

import java.net.DatagramSocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.api.pipeline.PipelineStep;
import core.communication.Connection;
import core.communication.ConnectionManager;
import core.packets.KNXPacket;

public class InwardConnectionPipelineStep implements PipelineStep<Object, Object> {

	private static final Logger LOG = LogManager.getLogger(InwardConnectionPipelineStep.class);

	private ConnectionManager connectionManager;

	@Override
	public Object execute(final Object dataAsObject) throws Exception {

		if (dataAsObject == null) {
			return null;
		}

		final Object[] data = (Object[]) dataAsObject;

		final DatagramSocket datagramSocket = (DatagramSocket) data[0];
		final KNXPacket knxPacket = (KNXPacket) data[1];

		Connection connection = null;

		// try to retrieve the connection via the connection header
		if (knxPacket.getConnectionHeader() != null) {
			connection = connectionManager.retrieveConnection(knxPacket.getConnectionHeader().getChannel());
			if (connection == null) {
				connection = connectionManager.createNewConnection(datagramSocket, knxPacket.getConnectionType());
			}
		}

		final int communicationChannelId = knxPacket.getCommunicationChannelId();

		// try to retrieve the connection via the communicationChannelId
		if (connection == null) {
			connection = connectionManager.retrieveConnection(knxPacket, datagramSocket);
			if (connection == null && communicationChannelId > 0) {
				connection = connectionManager.createNewConnection(datagramSocket, communicationChannelId,
						knxPacket.getConnectionType());
			}
		}

		if (connection == null) {
			LOG.warn("Connection with communicationChannelId = {} is not known! No response is sent!",
					communicationChannelId);
		} else {

			// set the counter so that a response including a valid sequence number can be
			// send as a response to a packet
			if (knxPacket.getConnectionHeader() != null) {
				connection.setReceiveSequenceCounter(knxPacket.getConnectionHeader().getSequenceCounter());
			}
			knxPacket.setConnection(connection);
		}

		return dataAsObject;
	}

	public void setConnectionManager(final ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

}
