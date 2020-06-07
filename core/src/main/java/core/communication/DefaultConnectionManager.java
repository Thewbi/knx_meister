package core.communication;

import java.net.DatagramSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.api.pipeline.Pipeline;
import core.packets.ConnectionType;
import core.packets.KNXPacket;

public class DefaultConnectionManager implements ConnectionManager {

	private static final Logger LOG = LogManager.getLogger(DefaultConnectionManager.class);

	private final Map<Integer, Connection> connectionMap = new ConcurrentHashMap<>();

	private final AtomicInteger connectionIdAtomicInteger = new AtomicInteger();

	private Pipeline<Object, Object> outputPipeline;

	@Override
	public Connection retrieveConnection(final KNXPacket knxPacket, final DatagramSocket datagramSocket) {

		final int communicationChannelId = knxPacket.getCommunicationChannelId();

		if (communicationChannelId > 0) {
			return connectionMap.get(communicationChannelId);
		}

		if (connectionMap.containsKey(0)) {
			return connectionMap.get(0);
		}

		return createNewConnection(datagramSocket, connectionIdAtomicInteger.getAndIncrement(), ConnectionType.UNKNOWN);
	}

	@Override
	public Connection createNewConnection(final DatagramSocket datagramSocket, final ConnectionType connectionType) {
		return createNewConnection(datagramSocket, connectionIdAtomicInteger.getAndIncrement(), connectionType);
	}

	private Connection createNewConnection(final DatagramSocket datagramSocket, final int id,
			final ConnectionType connectionType) {

		LOG.info("Creating new connnection with id {}", id);

		final DefaultConnection connection = new DefaultConnection();
		connection.setId(id);
		connection.setDatagramSocket(datagramSocket);
		connection.setConnectionType(connectionType);
		connection.setOutputPipeline(outputPipeline);

		connectionMap.put(id, connection);

		return connection;
	}

	@Override
	public void closeConnection(final int id) {

		LOG.info("Removing connnection with id {}", id);

		if (!connectionMap.containsKey(id)) {
			return;
		}
		connectionMap.remove(id);
	}

	@Override
	public void setOutputPipeline(final Pipeline<Object, Object> outputPipeline) {
		this.outputPipeline = outputPipeline;
	}

}
