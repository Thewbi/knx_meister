package core.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.api.device.Device;
import core.common.NetworkUtils;
import core.packets.ConnectionHeader;
import core.packets.ConnectionRequestInformation;
import core.packets.ConnectionResponseDataBlock;
import core.packets.ConnectionStatus;
import core.packets.ConnectionType;
import core.packets.DescriptionInformationBlockType;
import core.packets.DeviceInformationDIB;
import core.packets.HPAIStructure;
import core.packets.KNXLayer;
import core.packets.KNXMedium;
import core.packets.KNXPacket;
import core.packets.MfrDataDIB;
import core.packets.ProtocolDescriptor;
import core.packets.ServiceFamily;
import core.packets.ServiceIdentifier;
import core.packets.Structure;
import core.packets.StructureType;
import core.packets.SuppSvcFamiliesDIB;

public class Controller extends BaseDatagramPacketCallback {

	// public static final int POINT_TO_POINT_PORT = 65000;
	public static final int POINT_TO_POINT_PORT = 3671;

//	public static final int POINT_TO_POINT_CONTROL_PORT = 34000;
	public static final int POINT_TO_POINT_CONTROL_PORT = 3671;

//	public static final int POINT_TO_POINT_DATA_PORT = 34001;
	public static final int POINT_TO_POINT_DATA_PORT = 3671;

//	private final String localInetAddress = "127.0.0.1";
//	private final String localInetAddress = "192.168.0.108";
	private final String localInetAddress;

	private int channelNumber = 0x01;

	private static final Logger LOG = LogManager.getLogger(Controller.class);

	private final Map<String, HPAIStructure> deviceMap = new HashMap<>();

	private ConnectionManager connectionManager;

	private Device device;

	public Controller() throws SocketException, UnknownHostException {
		localInetAddress = InetAddress.getLocalHost().getHostAddress();
	}

	@Override
	public void knxPacket(final Connection connection, final DatagramSocket socket3671,
			final DatagramPacket datagramPacket, final KNXPacket knxPacket, final String label) throws IOException {

		HPAIStructure hpaiStructure = null;
		InetAddress inetAddress = null;
		int port = -1;

		DeviceInformationDIB deviceInformationDIB = null;

		switch (knxPacket.getHeader().getServiceIdentifier()) {

		case SEARCH_REQUEST_EXT:
			LOG.trace("<<<<<<<<<<<<<<< Ignoring " + ServiceIdentifier.SEARCH_REQUEST_EXT);
			break;

		case SEARCH_REQUEST:
			hpaiStructure = (HPAIStructure) knxPacket.getStructureMap().get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);
			inetAddress = InetAddress.getByAddress(hpaiStructure.getIpAddress());
			port = hpaiStructure.getPort() & 0xFFFF;

			final KNXPacket sendSearchResponseToAddress = sendSearchResponseToAddress(socket3671, inetAddress, port);

			connection.sendResponse(sendSearchResponseToAddress, new InetSocketAddress(inetAddress, port));
			break;

		case SEARCH_RESPONSE:
			hpaiStructure = (HPAIStructure) knxPacket.getStructureMap().get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);
			inetAddress = InetAddress.getByAddress(hpaiStructure.getIpAddress());
			port = hpaiStructure.getPort() & 0xFFFF;

			deviceInformationDIB = (DeviceInformationDIB) knxPacket.getDibMap()
					.get(DescriptionInformationBlockType.DEVICE_INFO);
			deviceMap.put(deviceInformationDIB.getDeviceSerialNumberAsString(), hpaiStructure);

			final KNXPacket sendDescriptionRequest = sendDescriptionRequest(inetAddress, port);
			connection.sendResponse(sendDescriptionRequest, new InetSocketAddress(inetAddress, port));
			break;

		case DESCRIPTION_REQUEST:
			hpaiStructure = (HPAIStructure) knxPacket.getStructureMap().get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);
			inetAddress = InetAddress.getByAddress(hpaiStructure.getIpAddress());
			port = hpaiStructure.getPort() & 0xFFFF;

			final KNXPacket sendDescriptionResponse = sendDescriptionResponse(socket3671, datagramPacket, inetAddress,
					port);

			connection.sendResponse(sendDescriptionResponse, datagramPacket.getSocketAddress());
			break;

		case DESCRIPTION_RESPONSE:
			deviceInformationDIB = (DeviceInformationDIB) knxPacket.getDibMap()
					.get(DescriptionInformationBlockType.DEVICE_INFO);
			hpaiStructure = deviceMap.get(deviceInformationDIB.getDeviceSerialNumberAsString());

			final KNXPacket sendConnectionRequest = sendConnectionRequest(socket3671, datagramPacket, knxPacket,
					hpaiStructure.getIpAddressAsObject(), hpaiStructure.getPort());

			final InetSocketAddress inetSocketAddress = new InetSocketAddress(hpaiStructure.getIpAddressAsObject(),
					hpaiStructure.getPort());
			connection.sendResponse(sendConnectionRequest, inetSocketAddress);
			break;

		case CONNECT_REQUEST:

			final Structure tunnelingStructure = knxPacket.getStructureMap().get(StructureType.TUNNELING_CONNECTION);
			final Structure deviceManagementStructure = knxPacket.getStructureMap()
					.get(StructureType.DEVICE_MGMT_CONNECTION);

			if ((tunnelingStructure == null) && (deviceManagementStructure == null)) {
				throw new RuntimeException("Cannot retrieve a connection structure!");
			}

			ConnectionType connectionType = ConnectionType.TUNNEL_CONNECTION;
			if (deviceManagementStructure != null) {
				connectionType = ConnectionType.DEVICE_MGMT_CONNECTION;
			}

			final Connection newConnection = connectionManager.createNewConnection(socket3671, connectionType);

			hpaiStructure = (HPAIStructure) knxPacket.getStructureMap().get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);
			final InetAddress controlInetAddress = InetAddress.getByAddress(hpaiStructure.getIpAddress());
			final int controlPort = hpaiStructure.getPort() & 0xFFFF;

			final KNXPacket sendConnectionResponse = sendConnectionResponse(socket3671, datagramPacket,
					controlInetAddress, controlPort, connectionType);
			newConnection.sendResponse(sendConnectionResponse, new InetSocketAddress(controlInetAddress, controlPort));
			break;

		case CONNECTIONSTATE_REQUEST:
			final KNXPacket sendConnectionStateResponse = sendConnectionStateResponse(socket3671, datagramPacket,
					knxPacket, inetAddress, port);

			connection.sendResponse(sendConnectionStateResponse, datagramPacket.getSocketAddress());
			break;

		case DISCONNECT_REQUEST:
			connectionManager.closeConnection(knxPacket.getCommunicationChannelId());

			final KNXPacket sendDisconnetResponse = sendDisconnetResponse(socket3671, datagramPacket, knxPacket,
					inetAddress, port);

			connection.sendResponse(sendDisconnetResponse, datagramPacket.getSocketAddress());
			break;

		case TUNNEL_REQUEST:
//			final Connection tunnelConnection = connectionManager
//					.retrieveConnection(knxPacket.getConnectionHeader().getChannel());

			// send acknowledge
			final KNXPacket tunnelResponse = sendTunnelResponse(knxPacket, socket3671, datagramPacket);
//			tunnelConnection.sendResponse(tunnelResponse, datagramPacket.getSocketAddress());
			knxPacket.getConnection().sendResponse(tunnelResponse, datagramPacket.getSocketAddress());

			// Send a acknowledge
			final KNXPacket acknowledgeKNXPacket = new KNXPacket(knxPacket);

			final int sequenceCounter = knxPacket.getConnection().getSequenceCounter();
			// increment by 2
			acknowledgeKNXPacket.getConnectionHeader().setSequenceCounter(sequenceCounter + 2);
//			acknowledgeKNXPacket.getConnectionHeader().setSequenceCounter(sequenceCounter + 1);
			acknowledgeKNXPacket.getCemiTunnelRequest().setMessageCode(0x2e);
			acknowledgeKNXPacket.getCemiTunnelRequest().setSourceKNXAddress(device.getPhysicalAddress());
//			acknowledgeKNXPacket.getCemiTunnelRequest().setSourceKNXAddress(0x1112);
			acknowledgeKNXPacket.getCemiTunnelRequest().setDestKNXAddress(0);
			acknowledgeKNXPacket.getCemiTunnelRequest().setCtrl1(0x91);
			acknowledgeKNXPacket.getCemiTunnelRequest().setLength(1);
			acknowledgeKNXPacket.getCemiTunnelRequest().setApci(0x0100);

			knxPacket.getConnection().sendResponse(acknowledgeKNXPacket, datagramPacket.getSocketAddress());

			// TODO ask the application layer service if the packet makes send and if so
			// send a confirmation

//			final KNXPacket indicationKNXPacket = new KNXPacket();
//			indicationKNXPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_REQUEST);
//
//			indicationKNXPacket.setConnectionHeader(new ConnectionHeader());
//			indicationKNXPacket.getConnectionHeader().setChannel(knxPacket.getConnectionHeader().getChannel());
//			indicationKNXPacket.getConnectionHeader()
//					.setSequenceCounter(knxPacket.getConnectionHeader().getSequenceCounter() + 3);
//			// set reserved to 0x00
//			indicationKNXPacket.getConnectionHeader().setReserved(0x00);
//
//			final CemiTunnelRequest cemiTunnelRequest = new CemiTunnelRequest();
//			cemiTunnelRequest.setMessageCode(0x29); // ind (= response in network layer terms)
//			cemiTunnelRequest.setAdditionalInfoLength(0);
//			cemiTunnelRequest.setCtrl1(0xB0);
//			cemiTunnelRequest.setCtrl2(0xE0);
//			cemiTunnelRequest.setSourceKNXAddress(device.getPhysicalAddress());
//			cemiTunnelRequest.setDestKNXAddress(0);
//			cemiTunnelRequest.setLength(1);
//			cemiTunnelRequest.setApci(0x0140); // IndAddrResp
//			indicationKNXPacket.setCemiTunnelRequest(cemiTunnelRequest);
//			knxPacket.getConnection().sendResponse(indicationKNXPacket, datagramPacket.getSocketAddress());

			break;

		case DEVICE_CONFIGURATION_REQUEST:

			final short propertyKey = knxPacket.getCemiPropReadRequest().getPropertyId();
			final short propertyValue = device.getProperties().get(propertyKey);

			final byte[] responseData = new byte[2];
			responseData[0] = (byte) ((propertyValue >> 8) & 0xFF);
			responseData[1] = (byte) (propertyValue & 0xFF);

			// send the current configuration value back to the sender
			final KNXPacket deviceConfigurationRequestAnswer = new KNXPacket(knxPacket);
			// change the message code from 0xfc to 0xfb
			deviceConfigurationRequestAnswer.getCemiPropReadRequest().setMessageCode((short) 0xfb);
			deviceConfigurationRequestAnswer.getCemiPropReadRequest().setResponseData(responseData);
			connection.sendResponse(deviceConfigurationRequestAnswer, datagramPacket.getSocketAddress());

			// send acknowledge
			final KNXPacket knxPacketAck = new KNXPacket();
			knxPacketAck.getHeader().setServiceIdentifier(ServiceIdentifier.DEVICE_CONFIGURATION_ACK);

			knxPacketAck.setConnectionHeader(new ConnectionHeader());
			knxPacketAck.getConnectionHeader().setChannel(knxPacket.getConnectionHeader().getChannel());
			knxPacketAck.getConnectionHeader().setSequenceCounter(knxPacket.getConnectionHeader().getSequenceCounter());
			// status OK
			knxPacketAck.getConnectionHeader().setReserved(0x00);
			connection.sendResponse(knxPacketAck, datagramPacket.getSocketAddress());
			break;

		default:
			LOG.warn("Ignoring: " + knxPacket.getHeader().getServiceIdentifier().name());
			break;
		}
	}

	private KNXPacket sendTunnelResponse(final KNXPacket knxPacket, final DatagramSocket socket3671,
			final DatagramPacket datagramPacket) {

		final KNXPacket outKNXPacket = new KNXPacket();
		outKNXPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_RESPONSE);

		outKNXPacket.setConnectionHeader(new ConnectionHeader());
		outKNXPacket.getConnectionHeader().setChannel(knxPacket.getConnectionHeader().getChannel());
		outKNXPacket.getConnectionHeader().setSequenceCounter(knxPacket.getConnectionHeader().getSequenceCounter());
		// status OK
		outKNXPacket.getConnectionHeader().setReserved(0x00);

		return outKNXPacket;
	}

	private KNXPacket sendConnectionRequest(final DatagramSocket socket, final DatagramPacket originalDatagramPacket,
			final KNXPacket originalKNXPacket, final InetAddress inetAddress, final int port) throws IOException {

		final HPAIStructure controlHPAIStructure = new HPAIStructure();
		controlHPAIStructure.setIpAddress(InetAddress.getByName(localInetAddress).getAddress());
		controlHPAIStructure.setPort((short) POINT_TO_POINT_CONTROL_PORT);

		final HPAIStructure dataHPAIStructure = new HPAIStructure();
		dataHPAIStructure.setIpAddress(InetAddress.getByName(localInetAddress).getAddress());
		dataHPAIStructure.setPort((short) POINT_TO_POINT_DATA_PORT);

		final KNXPacket knxPacket = new KNXPacket();
		knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.CONNECT_REQUEST);
		knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, controlHPAIStructure);
		knxPacket.getStructureMap().put(StructureType.HPAI_DATA_ENDPOINT_UDP, dataHPAIStructure);

		final ConnectionRequestInformation connectionRequestInformation = new ConnectionRequestInformation();
		connectionRequestInformation.setStructureType(StructureType.TUNNELING_CONNECTION);
		connectionRequestInformation.setKnxLayer(KNXLayer.TUNNEL_LINKLAYER.getValue());
		knxPacket.getStructureMap().put(connectionRequestInformation.getStructureType(), connectionRequestInformation);

		return knxPacket;
	}

	private KNXPacket sendDisconnetResponse(final DatagramSocket socket, final DatagramPacket datagramPacket,
			final KNXPacket originalKNXPacket, final InetAddress inetAddress, final int port) throws IOException {

		final KNXPacket knxPacket = new KNXPacket();

		// header
		knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.DISCONNECT_RESPONSE);

		knxPacket.setCommunicationChannelId(originalKNXPacket.getCommunicationChannelId());
		knxPacket.setConnectionStatus(ConnectionStatus.E_NO_ERROR);

		return knxPacket;
	}

	private KNXPacket sendConnectionStateResponse(final DatagramSocket socket, final DatagramPacket datagramPacket,
			final KNXPacket originalKNXPacket, final InetAddress inetAddress, final int port) throws IOException {

		final KNXPacket knxPacket = new KNXPacket();

		// header
		knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.CONNECTIONSTATE_RESPONSE);

		knxPacket.setCommunicationChannelId(originalKNXPacket.getCommunicationChannelId());
		knxPacket.setConnectionStatus(ConnectionStatus.E_NO_ERROR);

		return knxPacket;
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
	private KNXPacket sendConnectionResponse(final DatagramSocket socket3671, final DatagramPacket datagramPacket,
			final InetAddress inetAddress, final int port, final ConnectionType connectionType) throws IOException {

		final KNXPacket knxPacket = new KNXPacket();

		// header
		knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.CONNECT_RESPONSE);

		knxPacket.setCommunicationChannelId(channelNumber++);
		knxPacket.setConnectionStatus(ConnectionStatus.E_NO_ERROR);

		final boolean addHPAIStructure = true;
		if (addHPAIStructure) {

			// HPAI structure
			final HPAIStructure hpaiStructure = new HPAIStructure();
			hpaiStructure.setIpAddress(InetAddress.getByName(localInetAddress).getAddress());
			hpaiStructure.setPort((short) POINT_TO_POINT_CONTROL_PORT);
			knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, hpaiStructure);
		}

		final boolean addconnectionResponseDataBlock = true;
		if (addconnectionResponseDataBlock) {
			// CRD - Connection Response Data Block
			final ConnectionResponseDataBlock connectionResponseDataBlock = new ConnectionResponseDataBlock();
			connectionResponseDataBlock.setConnectionType(connectionType);
			if (connectionType != ConnectionType.DEVICE_MGMT_CONNECTION) {
				connectionResponseDataBlock.setDeviceAddress(device.getHostPhysicalAddress());
			}

			knxPacket.setConnectionResponseDataBlock(connectionResponseDataBlock);
		}

		return knxPacket;
	}

	/**
	 * 7.6.1 SEARCH_REQUEST
	 *
	 * @throws IOException
	 */
	public void sendSearchRequest() throws IOException {

		LOG.info("sendSearchRequest() ...");

		final KNXPacket knxPacket = new KNXPacket();

		// header
		knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.SEARCH_REQUEST);

		// HPAI - as a IP address, specify the IP address of the NIC you want to receive
		// the response on
		final HPAIStructure hpaiStructure = new HPAIStructure();
		hpaiStructure.setIpAddress(InetAddress.getByName(localInetAddress).getAddress());
		hpaiStructure.setPort((short) POINT_TO_POINT_CONTROL_PORT);
		knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, hpaiStructure);

		final byte[] bytes = knxPacket.getBytes();

		final String group = "224.0.23.12";
		final int port = 3671;
		final int ttl = 4;

		final DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, InetAddress.getByName(group),
				port);

		final NetworkInterface networkInterface = NetworkUtils
				.findInterfaceByIP(InetAddress.getByName(localInetAddress).getAddress());

		// https://stackoverflow.com/questions/37812919/why-a-datagramsocket-does-not-send-over-the-network-with-multicast-address
		//
		// When sending unicast datagrams, the routing tables dictate which network
		// interface is used to send the packet. For multicast, you need to specify the
		// interface. You can do that with a MulticastSocket.
		final MulticastSocket multicastSocket = new MulticastSocket();
		multicastSocket.setNetworkInterface(networkInterface);
		multicastSocket.setTimeToLive(ttl);
		multicastSocket.send(datagramPacket);
		multicastSocket.close();
	}

	public void sendSearchResponseToSender(final DatagramSocket socket, final DatagramPacket datagramPacket)
			throws IOException {

		final KNXPacket knxPacket = retrieveSearchResponseKNXPacket();

		final byte[] bytes = knxPacket.getBytes();

		final DatagramPacket outDatagramPacket = new DatagramPacket(bytes, bytes.length,
				datagramPacket.getSocketAddress());

		socket.send(outDatagramPacket);
	}

	public KNXPacket sendSearchResponseToAddress(final DatagramSocket socket3671, final InetAddress inetAddress,
			final int port) throws IOException {

		final KNXPacket knxPacket = retrieveSearchResponseKNXPacket();

		return knxPacket;
	}

	private KNXPacket retrieveSearchResponseKNXPacket() throws UnknownHostException {
		final KNXPacket knxPacket = new KNXPacket();

		// header
		knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.SEARCH_RESPONSE);

		// HPAI structure
		final HPAIStructure hpaiStructure = new HPAIStructure();
		hpaiStructure.setIpAddress(InetAddress.getByName(localInetAddress).getAddress());
		hpaiStructure.setPort((short) POINT_TO_POINT_CONTROL_PORT);
		knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, hpaiStructure);

		// Device Information DIB
		final DeviceInformationDIB deviceInformationDIB = retrieveDeviceInformationDIB();
		knxPacket.getDibMap().put(deviceInformationDIB.getType(), deviceInformationDIB);

		// Supported Service Families DIB
		final SuppSvcFamiliesDIB suppSvcFamiliesDIB = retrieveServiceFamiliesDIB();
		knxPacket.getDibMap().put(suppSvcFamiliesDIB.getType(), suppSvcFamiliesDIB);

		// Mft DIB
		final MfrDataDIB mfrDataDIB = retrieveMfrDataDIB();
		knxPacket.getDibMap().put(mfrDataDIB.getType(), mfrDataDIB);

		return knxPacket;
	}

	public KNXPacket sendDescriptionRequest(final InetAddress inetAddress, final int port) throws IOException {

		final HPAIStructure hpaiStructure = new HPAIStructure();
		hpaiStructure.setIpAddress(InetAddress.getByName(localInetAddress).getAddress());
		hpaiStructure.setPort((short) POINT_TO_POINT_CONTROL_PORT);

		final KNXPacket knxPacket = new KNXPacket();
		knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.DESCRIPTION_REQUEST);
		knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, hpaiStructure);

		return knxPacket;
	}

	private KNXPacket sendDescriptionResponse(final DatagramSocket socket, final DatagramPacket datagramPacket,
			final InetAddress inetAddress, final int port) throws IOException {

		final KNXPacket knxPacket = new KNXPacket();
		knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.DESCRIPTION_RESPONSE);

		final DeviceInformationDIB deviceInformationDIB = retrieveDeviceInformationDIB();
		knxPacket.getDibMap().put(deviceInformationDIB.getType(), deviceInformationDIB);

		// supported service families - SuppSvcFamilies DescriptionInformationBlock
		// (DIB)
		final SuppSvcFamiliesDIB suppSvcFamiliesDIB = retrieveServiceFamiliesDIB();
		knxPacket.getDibMap().put(suppSvcFamiliesDIB.getType(), suppSvcFamiliesDIB);

		return knxPacket;
	}

	/**
	 * device info DescriptionInformationBlock (DIB)
	 */
	private DeviceInformationDIB retrieveDeviceInformationDIB() {

		final DeviceInformationDIB deviceInformationDIB = new DeviceInformationDIB();
		deviceInformationDIB.setDeviceStatus(device.getDeviceStatus());
		deviceInformationDIB.setIndividualAddress(device.getHostPhysicalAddress());
		deviceInformationDIB.setMedium(KNXMedium.TP1);
		deviceInformationDIB.setProjectInstallationIdentifier(0);

		// serial number
//		System.arraycopy(new byte[] { (byte) 0x03, (byte) 0x03, (byte) 0x03, (byte) 0x03, (byte) 0x03, (byte) 0x03 }, 0,
//				deviceInformationDIB.getDeviceSerialNumber(), 0, deviceInformationDIB.getDeviceSerialNumber().length);
		System.arraycopy(new byte[] { (byte) 0x00, (byte) 0xC5, (byte) 0x01, (byte) 0x02, (byte) 0xD8, (byte) 0x4D }, 0,
				deviceInformationDIB.getDeviceSerialNumber(), 0, deviceInformationDIB.getDeviceSerialNumber().length);
		// real serial
//		System.arraycopy(new byte[] { (byte) 0x00, (byte) 0xC5, (byte) 0x01, (byte) 0x02, (byte) 0xD8, (byte) 0x4C }, 0,
//				deviceInformationDIB.getDeviceSerialNumber(), 0, deviceInformationDIB.getDeviceSerialNumber().length);

		// multicast address - 224.0.23.12
		System.arraycopy(new byte[] { (byte) 0xE0, (byte) 0x00, (byte) 0x17, (byte) 0x0C }, 0,
				deviceInformationDIB.getDeviceRoutingMulticastAddress(), 0,
				deviceInformationDIB.getDeviceRoutingMulticastAddress().length);

		// mac address
//		System.arraycopy(new byte[] { (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06 }, 0,
//				deviceInformationDIB.getDeviceMacAddress(), 0, deviceInformationDIB.getDeviceMacAddress().length);
//		System.arraycopy(new byte[] { (byte) 0x09, (byte) 0x09, (byte) 0x09, (byte) 0x09, (byte) 0x09, (byte) 0x09 }, 0,
//				deviceInformationDIB.getDeviceMacAddress(), 0, deviceInformationDIB.getDeviceMacAddress().length);
		// D0-C6-37-A1-2A-E8
//		System.arraycopy(new byte[] { (byte) 0xD0, (byte) 0xC6, (byte) 0x37, (byte) 0xA1, (byte) 0x2A, (byte) 0xE8 }, 0,
//				deviceInformationDIB.getDeviceMacAddress(), 0, deviceInformationDIB.getDeviceMacAddress().length);
		// 6c:40:08:97:d1:12
		System.arraycopy(new byte[] { (byte) 0x6C, (byte) 0x40, (byte) 0x08, (byte) 0x97, (byte) 0xD1, (byte) 0x12 }, 0,
				deviceInformationDIB.getDeviceMacAddress(), 0, deviceInformationDIB.getDeviceMacAddress().length);

		// friendly name
//		final String friendlyName = "test_object1";
		final String friendlyName = "KNX IP BAOS 777";
		final byte[] friendlyNameAsByteArray = friendlyName.getBytes(StandardCharsets.US_ASCII);
		System.arraycopy(friendlyNameAsByteArray, 0, deviceInformationDIB.getDeviceFriendlyName(), 0,
				friendlyNameAsByteArray.length);

		deviceInformationDIB.setLength(54);

		return deviceInformationDIB;
	}

	private SuppSvcFamiliesDIB retrieveServiceFamiliesDIB() {

		final SuppSvcFamiliesDIB suppSvcFamiliesDIB = new SuppSvcFamiliesDIB();
		suppSvcFamiliesDIB.setLength(8);

		ProtocolDescriptor protocoDescriptor = new ProtocolDescriptor();
		suppSvcFamiliesDIB.getProtocolDescriptors().add(protocoDescriptor);
		protocoDescriptor.setProtocol(ServiceFamily.KNXNET_IP_CORE.getValue());
		protocoDescriptor.setVersion(1);

		protocoDescriptor = new ProtocolDescriptor();
		suppSvcFamiliesDIB.getProtocolDescriptors().add(protocoDescriptor);
		protocoDescriptor.setProtocol(ServiceFamily.KNXNET_DEVICE_MGMT.getValue());
		protocoDescriptor.setVersion(2);

		protocoDescriptor = new ProtocolDescriptor();
		suppSvcFamiliesDIB.getProtocolDescriptors().add(protocoDescriptor);
		protocoDescriptor.setProtocol(ServiceFamily.KNXNET_IP_TUNNELLING.getValue());
		protocoDescriptor.setVersion(1);

		return suppSvcFamiliesDIB;
	}

	private MfrDataDIB retrieveMfrDataDIB() {

		final MfrDataDIB mfrDataDIB = new MfrDataDIB();
		mfrDataDIB.setLength(8);
		mfrDataDIB.setManufacturerId(0x00c5);

		return mfrDataDIB;
	}

	public void setConnectionManager(final ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	@Override
	protected Logger getLogger() {
		return LOG;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(final Device device) {
		this.device = device;
	}

}
