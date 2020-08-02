package core.communication.controller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import common.packets.ServiceIdentifier;
import core.communication.Connection;
import core.packets.ConnectionRequestInformation;
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
import core.packets.StructureType;
import core.packets.SuppSvcFamiliesDIB;

/**
 * This is the message handler for the core section of the KNX specification. It
 * handles all packets specified in the accepts() methods.
 *
 * Initially there has been one large handler for all packet types. This handler
 * got so big that it was split into individual controllers. There is one
 * controller for each subsection of the KNX specification.
 */
public class CoreController extends BaseController {

	private static final Logger LOG = LogManager.getLogger(CoreController.class);

	/**
	 * ctor
	 *
	 * @throws SocketException
	 * @throws UnknownHostException
	 */
	public CoreController(final String localInetAddress) throws SocketException, UnknownHostException {
		super(localInetAddress);
	}

	@Override
	public void knxPacket(final Connection connection, final DatagramSocket datagramSocket,
			final DatagramPacket datagramPacket, final KNXPacket knxPacket, final String label) throws IOException {

		HPAIStructure hpaiStructure = null;
		InetAddress inetAddress = null;
		int port = -1;
		DeviceInformationDIB deviceInformationDIB = null;
		Connection packetConnection = null;
		int communicationChannelId = 0;

		switch (knxPacket.getHeader().getServiceIdentifier()) {

		case SEARCH_REQUEST_EXT:
			getLogger().trace("<<<<<<<<<<<<<<< Ignoring " + ServiceIdentifier.SEARCH_REQUEST_EXT);
			break;

		case SEARCH_REQUEST:
			hpaiStructure = (HPAIStructure) knxPacket.getStructureMap().get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);
			inetAddress = InetAddress.getByAddress(hpaiStructure.getIpAddress());
			port = hpaiStructure.getPort() & 0xFFFF;

			final KNXPacket sendSearchResponseToAddress = sendSearchResponseToAddress(datagramSocket, inetAddress,
					port);

			connection.sendResponse(sendSearchResponseToAddress, new InetSocketAddress(inetAddress, port));
			break;

		case DESCRIPTION_REQUEST:
			hpaiStructure = (HPAIStructure) knxPacket.getStructureMap().get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);
			inetAddress = InetAddress.getByAddress(hpaiStructure.getIpAddress());
			port = hpaiStructure.getPort() & 0xFFFF;

			final KNXPacket sendDescriptionResponse = retrieveDescriptionResponse(datagramSocket, datagramPacket,
					inetAddress, port);

			connection.sendResponse(sendDescriptionResponse, datagramPacket.getSocketAddress());
			break;

		case DESCRIPTION_RESPONSE:
			deviceInformationDIB = (DeviceInformationDIB) knxPacket.getDibMap()
					.get(DescriptionInformationBlockType.DEVICE_INFO);
			hpaiStructure = getDeviceMap().get(deviceInformationDIB.getDeviceSerialNumberAsString());

			final KNXPacket sendConnectionRequest = retrieveConnectionRequest(datagramPacket, knxPacket,
					hpaiStructure.getIpAddressAsObject(), hpaiStructure.getPort());

			final InetSocketAddress inetSocketAddress = new InetSocketAddress(hpaiStructure.getIpAddressAsObject(),
					hpaiStructure.getPort());
			connection.sendResponse(sendConnectionRequest, inetSocketAddress);
			break;

		// 0x0205
		case CONNECT_REQUEST:
			// if the connect request contains a CRI Tunneling Connection, this connection
			// should be handled by the tunneling controller
			if (knxPacket.getStructureMap().containsKey(StructureType.TUNNELING_CONNECTION)) {
				break;
			}

			final Connection newConnection = getConnectionManager().createNewConnection(datagramSocket,
					knxPacket.getConnectionType());

			hpaiStructure = (HPAIStructure) knxPacket.getStructureMap().get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);
			final InetAddress controlInetAddress = InetAddress.getByAddress(hpaiStructure.getIpAddress());
			final int controlPort = hpaiStructure.getPort() & 0xFFFF;

			final KNXPacket sendConnectionResponse = retrieveConnectionResponse(knxPacket.getConnectionType());
			sendConnectionResponse.setCommunicationChannelId(newConnection.getId());

			newConnection.sendResponse(sendConnectionResponse, new InetSocketAddress(controlInetAddress, controlPort));

//			startThread(getClass().getName() + " CONNECTION_REQUEST", newConnection);
			break;

		case CONNECTIONSTATE_REQUEST:
			// make sure this is not a tunneling connection
			communicationChannelId = knxPacket.getCommunicationChannelId();
			packetConnection = getConnectionManager().retrieveConnection(communicationChannelId);
			if (packetConnection.getConnectionType() == ConnectionType.TUNNEL_CONNECTION) {
				return;
			}

			final KNXPacket sendConnectionStateResponse = sendConnectionStateResponse(datagramSocket, datagramPacket,
					knxPacket, inetAddress, port);

			final HPAIStructure controlEndpointHPAIStructure = (HPAIStructure) knxPacket.getStructureMap()
					.get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);

			final InetSocketAddress socketAddr = new InetSocketAddress(
					controlEndpointHPAIStructure.getIpAddressAsObject(), controlEndpointHPAIStructure.getPort());

			connection.sendResponse(sendConnectionStateResponse, socketAddr);
			break;

		case DISCONNECT_REQUEST:
			// make sure this is not a tunneling connection
			communicationChannelId = knxPacket.getCommunicationChannelId();
			packetConnection = getConnectionManager().retrieveConnection(communicationChannelId);
			if (packetConnection == null || packetConnection.getConnectionType() == ConnectionType.TUNNEL_CONNECTION) {
				return;
			}

			getConnectionManager().closeConnection(knxPacket.getCommunicationChannelId());

			final KNXPacket sendDisconnetResponse = sendDisconnetResponse(datagramSocket, datagramPacket, knxPacket,
					inetAddress, port);

			connection.sendResponse(sendDisconnetResponse, datagramPacket.getSocketAddress());
			break;

		default:
			getLogger().warn("Ignoring: " + knxPacket.getHeader().getServiceIdentifier().name());
			break;
		}
	}

	private KNXPacket sendSearchResponseToAddress(final DatagramSocket socket3671, final InetAddress inetAddress,
			final int port) throws IOException {
		return retrieveSearchResponseKNXPacket();
	}

	@SuppressWarnings("unused")
	private void sendSearchResponseToSender(final DatagramSocket socket, final DatagramPacket datagramPacket)
			throws IOException {

		final KNXPacket knxPacket = retrieveSearchResponseKNXPacket();

		final byte[] bytes = knxPacket.getBytes();

		final DatagramPacket outDatagramPacket = new DatagramPacket(bytes, bytes.length,
				datagramPacket.getSocketAddress());

		socket.send(outDatagramPacket);
	}

	/**
	 * Factory that creates a search response packet.
	 *
	 * @return
	 * @throws UnknownHostException
	 */
	private KNXPacket retrieveSearchResponseKNXPacket() throws UnknownHostException {

		final KNXPacket knxPacket = new KNXPacket();

		// header
		knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.SEARCH_RESPONSE);

		// HPAI structure
		final HPAIStructure hpaiStructure = new HPAIStructure();
		hpaiStructure.setIpAddress(InetAddress.getByName(getLocalInetAddress()).getAddress());
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

	/**
	 * Factory that creates a description response packet.
	 *
	 * @param socket
	 * @param datagramPacket
	 * @param inetAddress
	 * @param port
	 * @return
	 * @throws IOException
	 */
	private KNXPacket retrieveDescriptionResponse(final DatagramSocket socket, final DatagramPacket datagramPacket,
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

	private MfrDataDIB retrieveMfrDataDIB() {

		final MfrDataDIB mfrDataDIB = new MfrDataDIB();
		mfrDataDIB.setLength(8);
		mfrDataDIB.setManufacturerId(0x00c5);

		return mfrDataDIB;
	}

	/**
	 * Factory that creates a connection request packet.
	 *
	 * @param originalDatagramPacket
	 * @param originalKNXPacket
	 * @param inetAddress
	 * @param port
	 * @return
	 * @throws IOException
	 */
	private KNXPacket retrieveConnectionRequest(final DatagramPacket originalDatagramPacket,
			final KNXPacket originalKNXPacket, final InetAddress inetAddress, final int port) throws IOException {

		final HPAIStructure controlHPAIStructure = new HPAIStructure();
		controlHPAIStructure.setIpAddress(InetAddress.getByName(getLocalInetAddress()).getAddress());
		controlHPAIStructure.setPort((short) POINT_TO_POINT_CONTROL_PORT);

		final HPAIStructure dataHPAIStructure = new HPAIStructure();
		dataHPAIStructure.setIpAddress(InetAddress.getByName(getLocalInetAddress()).getAddress());
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

	/**
	 * device info DescriptionInformationBlock (DIB)
	 */
	private DeviceInformationDIB retrieveDeviceInformationDIB() {

		final DeviceInformationDIB deviceInformationDIB = new DeviceInformationDIB();
		deviceInformationDIB.setDeviceStatus(getDevice().getDeviceStatus());
		deviceInformationDIB.setIndividualAddress(getDevice().getHostPhysicalAddress());
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

	/**
	 * Does not accept datagram packets.
	 */
	@Override
	public boolean accepts(final DatagramPacket datagramPacket) {
		return false;
	}

	/**
	 * Accepts all of the KNX packets specified below.
	 */
	@Override
	public boolean accepts(final KNXPacket knxPacket) {
		switch (knxPacket.getHeader().getServiceIdentifier()) {
		case SEARCH_REQUEST_EXT:
		case SEARCH_REQUEST:
		case DESCRIPTION_REQUEST:
		case CONNECT_REQUEST:
		case CONNECTIONSTATE_REQUEST:
		case DISCONNECT_REQUEST:
			return true;

		default:
			return false;
		}
	}

	@Override
	protected Logger getLogger() {
		return LOG;
	}

}
