package core.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.api.pipeline.Pipeline;
import core.packets.ConnectionType;
import core.packets.KNXPacket;

public class DefaultConnection implements Connection {

	private static final Logger LOG = LogManager.getLogger(DefaultConnection.class);

	private int id;

	private int sequenceCounter = -1;

	private DatagramSocket datagramSocket;

	private ConnectionType connectionType;

	private Pipeline<Object, Object> outputPipeline;

	@Override
	public void sendResponse(final DatagramPacket datagramPacket) throws IOException {
		datagramSocket.send(datagramPacket);
	}

	@Override
	public void sendResponse(final KNXPacket knxPacket, final SocketAddress socketAddress) throws IOException {
		DatagramPacket datagramPacket;
		try {
			final Object[] objectArray = new Object[2];
			objectArray[0] = knxPacket;
			objectArray[1] = socketAddress;

			datagramPacket = (DatagramPacket) outputPipeline.execute(objectArray);
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
			throw new IOException(e);
		}
		datagramSocket.send(datagramPacket);
	}

	@Override
	public void sendResponse(final DatagramPacket datagramPacket, final InetAddress inetAddress, final int port)
			throws IOException {
		datagramSocket.send(datagramPacket);
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	@Override
	public DatagramSocket getDatagramSocket() {
		return datagramSocket;
	}

	public void setDatagramSocket(final DatagramSocket socket) {
		this.datagramSocket = socket;
	}

	public ConnectionType getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(final ConnectionType connectionType) {
		this.connectionType = connectionType;
	}

	public void setOutputPipeline(final Pipeline<Object, Object> outputPipeline) {
		this.outputPipeline = outputPipeline;
	}

	@Override
	public int getSequenceCounter() {
		return sequenceCounter;
	}

	@Override
	public void setSequenceCounter(final int sequenceCounter) {
		this.sequenceCounter = sequenceCounter;
	}

}
