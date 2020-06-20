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
import core.communication.ConnectionManager;
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

	public BaseController(final String localInetAddress) throws SocketException, UnknownHostException {
		this.localInetAddress = localInetAddress;
	}

	/**
	 * 7.8.2 CONNECT_RESPONSE Example: 8.8.6 CONNECT_RESPONSE
	 *
	 * @param socket3671     the DatagramSocket that the reader thread is bound to
	 *                       on port 3671 and which messages are received from.
	 * @param datagramPacket
	 *
	 * @param inetAddress    the IP address of the KNX Clients control endpoint
	 *                       (send in the control HPAI).
	 * @param port
	 * @throws IOException
	 */
	protected KNXPacket sendConnectionResponse(final DatagramSocket socket3671, final DatagramPacket datagramPacket,
			final InetAddress inetAddress, final int port, final ConnectionType connectionType) throws IOException {

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

}
