package core.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.api.pipeline.Pipeline;
import core.packets.ConnectionType;
import core.packets.HPAIStructure;
import core.packets.KNXPacket;

public class DefaultConnection implements Connection {

	private static final Logger LOG = LogManager.getLogger(DefaultConnection.class);

	private int id;

	/**
	 * The sequenceCounter is not a number used to order UDP packets received out of
	 * order. The sequenceCounter correlates several UDP packets to a unit of work.
	 *
	 * For example the Tunneling DEVICE_DESCRIPTION_READ_APCI unit of work consists
	 * of four packets all belonging to the same sequenceCounter value req+OK,
	 * ind+OK.
	 */
	private int sequenceCounter = -1;

	private DatagramSocket datagramSocket;

	private ConnectionType connectionType;

	private Pipeline<Object, Object> outputPipeline;

	private HPAIStructure controlEndpoint;

	private HPAIStructure dataEndpoint;

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

		// datagramSocket.getInetAddress().getHostAddress() +
		LOG.trace("Connection {} is sending packet over socketAddress {}", id, socketAddress);

		datagramSocket.send(datagramPacket);
	}

	@Override
	public void sendRequest(final KNXPacket knxPacket) throws IOException {

		sequenceCounter++;
		knxPacket.getConnectionHeader().setSequenceCounter(sequenceCounter);
		knxPacket.getConnectionHeader().setChannel((short) id);

		final InetSocketAddress inetSocketAddress = new InetSocketAddress(datagramSocket.getInetAddress(),
				datagramSocket.getPort());
		DatagramPacket datagramPacket;
		try {
			final Object[] objectArray = new Object[2];
			objectArray[0] = knxPacket;
			objectArray[1] = inetSocketAddress;

			datagramPacket = (DatagramPacket) outputPipeline.execute(objectArray);
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
			throw new IOException(e);
		}

		LOG.trace("Connection {} is sending packet over socketAddress {}", id, inetSocketAddress);

		datagramSocket.send(datagramPacket);
	}

	@Override
	public void sendResponse(final DatagramPacket datagramPacket, final InetAddress inetAddress, final int port)
			throws IOException {
		datagramSocket.send(datagramPacket);
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
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

	@Override
	public ConnectionType getConnectionType() {
		return connectionType;
	}

	@Override
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

	@Override
	public HPAIStructure getControlEndpoint() {
		return controlEndpoint;
	}

	@Override
	public void setControlEndpoint(final HPAIStructure controlEndpoint) {
		this.controlEndpoint = controlEndpoint;
	}

	@Override
	public HPAIStructure getDataEndpoint() {
		return dataEndpoint;
	}

	@Override
	public void setDataEndpoint(final HPAIStructure dataEndpoint) {
		this.dataEndpoint = dataEndpoint;
	}

	@Override
	public void sendData(final KNXPacket knxPacket) throws IOException {

		sequenceCounter++;
		knxPacket.getConnectionHeader().setSequenceCounter(sequenceCounter);
		knxPacket.getConnectionHeader().setChannel((short) id);

//		final InetSocketAddress inetSocketAddress = new InetSocketAddress(datagramSocket.getInetAddress(),
//				datagramSocket.getPort());

		final InetSocketAddress inetSocketAddress = new InetSocketAddress(getDataEndpoint().getIpAddressAsObject(),
				getDataEndpoint().getPort());

		DatagramPacket datagramPacket;
		try {
			final Object[] objectArray = new Object[2];
			objectArray[0] = knxPacket;
			objectArray[1] = inetSocketAddress;

			datagramPacket = (DatagramPacket) outputPipeline.execute(objectArray);
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
			throw new IOException(e);
		}

		LOG.trace("Connection {} is sending packet over socketAddress {}", id, inetSocketAddress);

		datagramSocket.send(datagramPacket);
	}

}
