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

		// retrieve the connection
//		final Connection connection = connectionManager.retrieveConnection(knxPacket, datagramSocket);

		Connection connection = null;

		// try to retrieve the connection via the connection header
		if (knxPacket.getConnectionHeader() != null) {
			connection = connectionManager.retrieveConnection(knxPacket.getConnectionHeader().getChannel());
		}

		// try to retrieve the connection via the
		if (connection == null) {
			connection = connectionManager.retrieveConnection(knxPacket, datagramSocket);
		}

		if (connection == null) {
			final int communicationChannelId = knxPacket.getCommunicationChannelId();
			LOG.warn("Connection with communicationChannelId = {} is not known! No response is sent!",
					communicationChannelId);
		} else {

			// set the counter so that a response including a valid sequence number can be
			// send as a response to a packet
			if (knxPacket.getConnectionHeader() != null) {
				connection.setSequenceCounter(knxPacket.getConnectionHeader().getSequenceCounter());
			}
			knxPacket.setConnection(connection);
		}

		return dataAsObject;
	}

	public void setConnectionManager(final ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

}
