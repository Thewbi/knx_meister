package core.communication.controller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import core.api.device.Device;
import core.communication.BaseDatagramPacketCallback;
import core.communication.Connection;
import core.communication.ConnectionManager;
import core.data.sending.DataSender;
import core.packets.ConnectionResponseDataBlock;
import core.packets.ConnectionStatus;
import core.packets.ConnectionType;
import core.packets.HPAIStructure;
import core.packets.KNXPacket;
import core.packets.ServiceIdentifier;
import core.packets.StructureType;

public abstract class BaseController extends BaseDatagramPacketCallback {

	public static final short INDICATION_PRIMITIVE = 0x29;

	public static final short CONFIRM_PRIMITIVE = 0x2e;

	public static final short REQUEST_PRIMITIVE = 0x11;

	/** ??? what is the correct value???????? */
	public static final short RESPONSE_PRIMITIVE = 0xFF;

	public static final int KNX_PORT_DEFAULT = 3671;

	public static final int POINT_TO_POINT_PORT = KNX_PORT_DEFAULT;

	public static final int POINT_TO_POINT_CONTROL_PORT = KNX_PORT_DEFAULT;

	public static final int POINT_TO_POINT_DATA_PORT = KNX_PORT_DEFAULT;

	private final String localInetAddress;

	private final Map<String, HPAIStructure> deviceMap = new HashMap<>();

	private ConnectionManager connectionManager;

	private Device device;

	private DataSender dataSender;

	public BaseController(final String localInetAddress) throws SocketException, UnknownHostException {
		this.localInetAddress = localInetAddress;
	}

	/**
	 * 7.8.2 CONNECT_RESPONSE Example: 8.8.6 CONNECT_RESPONSE
	 *
	 * @param connectionType
	 *
	 * @throws IOException
	 */
	protected KNXPacket retrieveConnectionResponse(final ConnectionType connectionType) throws IOException {

		final KNXPacket knxPacket = new KNXPacket();

		// header
		knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.CONNECT_RESPONSE);

		knxPacket.setConnectionStatus(ConnectionStatus.E_NO_ERROR);

		final boolean addHPAIStructure = true;
		if (addHPAIStructure) {

			// HPAI structure
			final HPAIStructure hpaiStructure = new HPAIStructure();
			hpaiStructure.setIpAddress(InetAddress.getByName(getLocalInetAddress()).getAddress());
			hpaiStructure.setPort((short) POINT_TO_POINT_CONTROL_PORT);
			knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, hpaiStructure);
		}

		final boolean addconnectionResponseDataBlock = true;
		if (addconnectionResponseDataBlock) {

			// CRD - Connection Response Data Block
			final ConnectionResponseDataBlock connectionResponseDataBlock = new ConnectionResponseDataBlock();
			connectionResponseDataBlock.setConnectionType(connectionType);
			if (connectionType != ConnectionType.DEVICE_MGMT_CONNECTION) {
				connectionResponseDataBlock.setDeviceAddress(getDevice().getHostPhysicalAddress());
			}

			knxPacket.setConnectionResponseDataBlock(connectionResponseDataBlock);
		}

		return knxPacket;
	}

	protected KNXPacket sendConnectionStateResponse(final DatagramSocket socket, final DatagramPacket datagramPacket,
			final KNXPacket originalKNXPacket, final InetAddress inetAddress, final int port) throws IOException {

		final KNXPacket knxPacket = new KNXPacket();

		// header
		knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.CONNECTIONSTATE_RESPONSE);
		knxPacket.setCommunicationChannelId(originalKNXPacket.getCommunicationChannelId());
		knxPacket.setConnectionStatus(ConnectionStatus.E_NO_ERROR);

		return knxPacket;
	}

	protected KNXPacket sendDisconnetResponse(final DatagramSocket socket, final DatagramPacket datagramPacket,
			final KNXPacket originalKNXPacket, final InetAddress inetAddress, final int port) throws IOException {

		final KNXPacket knxPacket = new KNXPacket();

		// header
		knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.DISCONNECT_RESPONSE);

		knxPacket.setCommunicationChannelId(originalKNXPacket.getCommunicationChannelId());
		knxPacket.setConnectionStatus(ConnectionStatus.E_NO_ERROR);

		return knxPacket;
	}

	protected void startThread(final String label, final Connection connection) {

		getLogger().info(label + " Starting Thread");

		final Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					// in order to be compatible with the ETS5 Bus-Monitor, the tunnel requests can
					// only be send
					// to the ETS5 Bus-Monitor after the Bus-Monitor did ask for the ConnectionState
					// and that
					// request was answered with the answer OK.
					//
					// The sequence is:
					// 1. The Bus-Monitor establishes a tunneling connection with the device.
					// 2. The device returns the ID of the tunneling connection.
					// 3. The Bus-Monitor requests the ConnectionState of the tunneling connection
					// using the ID from step 2.
					// 4. The device answers with OK (the tunneling connection is in an OK state).
					// 5. The device now can use the tunneling connection to send data to the
					// Bus-Monitor in the form
					// of tunneling requests
					//
					// If the thread does not sleep but sends a tunneling request immediately, the
					// Bus-Monitor receives the tunneling request before it has performed the
					// Connection State check. If any requests arrives before the connection state
					// check, the Bus-Monitor will disconnect the tunneling connection immediately.
					//
					// An alternative would be to start this thread only after the communication
					// partner
					// has send a connection state request but some partners do never send a
					// communication state request
					getLogger().info(label + " Sleeping 5000 ...");
					Thread.sleep(5000);
				} catch (final InterruptedException e) {
					getLogger().error(e.getMessage(), e);
				}

				while (true) {

					getLogger().info(label + " Sending data ...");

					dataSender.send(connection);

					try {
						Thread.sleep(10000);
					} catch (final InterruptedException e) {
						getLogger().error(e.getMessage(), e);
						return;
					}
				}
			}
		});
		thread.start();
	}

	public void setConnectionManager(final ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(final Device device) {
		this.device = device;
	}

	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	public Map<String, HPAIStructure> getDeviceMap() {
		return deviceMap;
	}

	public String getLocalInetAddress() {
		return localInetAddress;
	}

	public DataSender getDataSender() {
		return dataSender;
	}

	public void setDataSender(final DataSender dataSender) {
		this.dataSender = dataSender;
	}

}
