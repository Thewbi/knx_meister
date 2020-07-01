package core.communication.controller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import common.packets.KNXConnectionHeader;
import common.packets.ServiceIdentifier;
import common.utils.Utils;
import core.communication.Connection;
import core.packets.CemiPropReadRequest;
import core.packets.CemiTunnelRequest;
import core.packets.DeviceManagement;
import core.packets.KNXPacket;
import core.packets.PropertyId;

public class DeviceManagementController extends BaseController {

	private static final int AUTH_LEVEL = 0;

	private static final int APCI_AUTH_REQUEST = 0xd1;

	private static final int APCI_AUTH_RESPONSE = 0xD2;

	private static final int APCI_DEVICE_DESCR_READ = 0x00;

	private static final int APCI_DEVICE_DESCRIPTION_RESPONSE = 0x40;

	private static final int APCI_PROP_DESCR_READ = 0xD8;

	private static final int APCI_PROP_DESCR_RESPONSE = 0xD9;

	private static final Logger LOG = LogManager.getLogger(DeviceManagementController.class);

	/**
	 * ctor
	 *
	 * @throws SocketException
	 * @throws UnknownHostException
	 */
	public DeviceManagementController(final String localInetAddress) throws SocketException, UnknownHostException {
		super(localInetAddress);
	}

	@Override
	public void knxPacket(final Connection connection, final DatagramSocket socket3671,
			final DatagramPacket datagramPacket, final KNXPacket knxPacket, final String label) throws IOException {

		KNXPacket knxPacketAck = null;
		KNXPacket knxPacketIndication = null;
		byte[] responseData = null;

		switch (knxPacket.getHeader().getServiceIdentifier()) {

		// 0x0310
		case DEVICE_CONFIGURATION_REQUEST:

			short messageCode = 0;

			CemiTunnelRequest cemiTunnelRequest = knxPacket.getCemiTunnelRequest();
			if (cemiTunnelRequest != null) {
				messageCode = cemiTunnelRequest.getMessageCode();
			}

			final CemiPropReadRequest cemiPropReadRequest = knxPacket.getCemiPropReadRequest();
			if (cemiPropReadRequest != null) {
				messageCode = cemiPropReadRequest.getMessageCode();
			}

			switch (messageCode) {
			case DeviceManagement.M_PROP_READ_REQ_VALUE:

				// @formatter:off
				// this section is for
				// 59 3.902163 192.168.0.108 192.168.0.241 KNXnet/IP 59 ConfigReq #01:0 M_PropRead.req OT=0 P=83
				// 60 3.905799 192.168.0.241 192.168.0.108 KNXnet/IP 61 ConfigReq #01:0 M_PropRead.con OT=0 P=83 $07B0
				// 61 3.905799 192.168.0.241 192.168.0.108 KNXnet/IP 60 ConfigAck #01:0 OK
				// 62 3.906024 192.168.0.108 192.168.0.241 KNXnet/IP 52 ConfigAck #01:0 OK
				// 63 3.908150 192.168.0.108 192.168.0.241 KNXnet/IP 59 ConfigReq #01:1 M_PropRead.req OT=0 P=56
				// 64 3.911340 192.168.0.241 192.168.0.108 KNXnet/IP 61 ConfigReq #01:1 M_PropRead.con OT=0 P=56 $0037
				// 65 3.911340 192.168.0.241 192.168.0.108 KNXnet/IP 60 ConfigAck #01:1 OK
				// 66 3.911560 192.168.0.108 192.168.0.241 KNXnet/IP 52 ConfigAck #01:1 OK
				// @formatter:on

				final short propertyKey = cemiPropReadRequest.getPropertyId();

				// copy device property into response
				responseData = new byte[2];
				responseData[0] = 0;
				responseData[1] = 0;
				if (getDevice().getProperties().containsKey(propertyKey)) {
					final short propertyValue = getDevice().getProperties().get(propertyKey);
					responseData[0] = (byte) ((propertyValue >> 8) & 0xFF);
					responseData[1] = (byte) (propertyValue & 0xFF);
				}

				// send the current configuration value back to the sender
				final KNXPacket deviceConfigurationRequestAnswer = new KNXPacket(knxPacket);
				// change the message code from M_PROP_READ_REQ to M_PROP_READ_CON
				deviceConfigurationRequestAnswer.getCemiPropReadRequest()
						.setMessageCode((short) DeviceManagement.M_PROP_READ_CON.getValue());
				deviceConfigurationRequestAnswer.getCemiPropReadRequest().setResponseData(responseData);
				connection.sendResponse(deviceConfigurationRequestAnswer, datagramPacket.getSocketAddress());

				final KNXConnectionHeader connectionHeader = new KNXConnectionHeader();
				connectionHeader.setChannel(knxPacket.getConnectionHeader().getChannel());
				connectionHeader.setSequenceCounter(knxPacket.getConnectionHeader().getSequenceCounter());
				// status OK
				connectionHeader.setReserved(0x00);

				// send acknowledge
				knxPacketAck = new KNXPacket();
				knxPacketAck.getHeader().setServiceIdentifier(ServiceIdentifier.DEVICE_CONFIGURATION_ACK);
				knxPacketAck.setConnectionHeader(connectionHeader);

				connection.sendResponse(knxPacketAck, datagramPacket.getSocketAddress());

				break;

			case DeviceManagement.T_DATA_CONNECTED_REQ_VALUE:

				switch (knxPacket.getCemiTunnelRequest().getApci()) {

				case APCI_DEVICE_DESCR_READ:

					// @formatter:off
					// 67 3.917579 192.168.0.108 192.168.0.241 KNXnet/IP 63 ConfigReq #01:2 T_Data_Connected.req DevDescrRead
					// 68 3.920656 192.168.0.241 192.168.0.108 KNXnet/IP 60 ConfigAck #01:2 OK
					// 69 3.927963 192.168.0.241 192.168.0.108 KNXnet/IP 65 ConfigReq #01:2 T_Data_Connected.ind DevDescrResp $07B0
					// 70 3.928335 192.168.0.108 192.168.0.241 KNXnet/IP 52 ConfigAck #01:2 OK
					// @formatter:on

					// send acknowledge
					knxPacketAck = new KNXPacket(knxPacket);
					knxPacketAck.clearExceptHeaders();
					knxPacketAck.getHeader().setServiceIdentifier(ServiceIdentifier.DEVICE_CONFIGURATION_ACK);

					connection.sendResponse(knxPacketAck, datagramPacket.getSocketAddress());

					// copy device property into response
					responseData = new byte[2];
					responseData[0] = 0;
					responseData[1] = 0;
					if (getDevice().hasPropertyValue(PropertyId.PID_DEVICE_DESCRIPTOR)) {
						final short propertyValue = getDevice().getPropertyValue(PropertyId.PID_DEVICE_DESCRIPTOR);
						responseData[0] = (byte) ((propertyValue >> 8) & 0xFF);
						responseData[1] = (byte) (propertyValue & 0xFF);
					} else {
						throw new RuntimeException("Device has no PropertyId.PID_DEVICE_DESCRIPTOR value!");
					}

					// send indication
					knxPacketIndication = new KNXPacket(knxPacket);

					cemiTunnelRequest = knxPacketIndication.getCemiTunnelRequest();
					cemiTunnelRequest.setMessageCode((short) DeviceManagement.T_DATA_CONNECTED_IND.getValue());
					cemiTunnelRequest.setLength(3);
					// 0x40 = device description response
					cemiTunnelRequest.setApci(APCI_DEVICE_DESCRIPTION_RESPONSE);
					cemiTunnelRequest.setPayloadBytes(responseData);

					connection.sendResponse(knxPacketIndication, datagramPacket.getSocketAddress());

					break;

				case APCI_AUTH_REQUEST:

					// @formatter:off
					// 75 3.956929 192.168.0.108 192.168.0.241 KNXnet/IP 68 ConfigReq #01:4 T_Data_Connected.req AuthReq $FFFFFFFF
					// 76 3.959605 192.168.0.241 192.168.0.108 KNXnet/IP 60 ConfigAck #01:4 OK
					// 77 3.968437 192.168.0.241 192.168.0.108 KNXnet/IP 64 ConfigReq #01:4 T_Data_Connected.ind AuthResp L=0
					// 78 3.968867 192.168.0.108 192.168.0.241 KNXnet/IP 52 ConfigAck #01:4 OK
					// @formatter:on

					// send acknowledge
					knxPacketAck = new KNXPacket(knxPacket);
					knxPacketAck.clearExceptHeaders();
					knxPacketAck.getHeader().setServiceIdentifier(ServiceIdentifier.DEVICE_CONFIGURATION_ACK);

					connection.sendResponse(knxPacketAck, datagramPacket.getSocketAddress());

					// copy device property into response
					responseData = new byte[1];
					responseData[0] = AUTH_LEVEL;

					// send indication
					knxPacketIndication = new KNXPacket(knxPacket);

					cemiTunnelRequest = knxPacketIndication.getCemiTunnelRequest();
					cemiTunnelRequest.setMessageCode((short) DeviceManagement.T_DATA_CONNECTED_IND.getValue());
					cemiTunnelRequest.setLength(2);
					// 0x40 = device description response
					cemiTunnelRequest.setApci(APCI_AUTH_RESPONSE);
					cemiTunnelRequest.setPayloadBytes(responseData);

					connection.sendResponse(knxPacketIndication, datagramPacket.getSocketAddress());

					break;

				case APCI_PROP_DESCR_READ:

					// @formatter:off
					// 79 3.977310 192.168.0.108 192.168.0.241 KNXnet/IP 66 ConfigReq #01:5 T_Data_Connected.req PropDescrRead OX=2 P=23
					// 80 3.980471 192.168.0.241 192.168.0.108 KNXnet/IP 60 ConfigAck #01:5 OK
					// 81 3.987842 192.168.0.241 192.168.0.108 KNXnet/IP 70 ConfigReq #01:5 T_Data_Connected.ind PropDescrResp OX=2 P=23 PX=0 T=20 N=909 R=3 W=2
					// 82 3.988235 192.168.0.108 192.168.0.241 KNXnet/IP 52 ConfigAck #01:5 OK
					// @formatter:on

					// send acknowledge
					knxPacketAck = new KNXPacket(knxPacket);
					knxPacketAck.clearExceptHeaders();
					knxPacketAck.getHeader().setServiceIdentifier(ServiceIdentifier.DEVICE_CONFIGURATION_ACK);

					connection.sendResponse(knxPacketAck, datagramPacket.getSocketAddress());

					// copy device property into response
					responseData = new byte[1];
					responseData[0] = AUTH_LEVEL;

					// send indication
					knxPacketIndication = new KNXPacket(knxPacket);

					cemiTunnelRequest = knxPacketIndication.getCemiTunnelRequest();
					cemiTunnelRequest.setMessageCode((short) DeviceManagement.T_DATA_CONNECTED_IND.getValue());
					cemiTunnelRequest.setLength(2);
					// 0x40 = device description response
					cemiTunnelRequest.setApci(APCI_PROP_DESCR_RESPONSE);
					cemiTunnelRequest.setPayloadBytes(responseData);

					connection.sendResponse(knxPacketIndication, datagramPacket.getSocketAddress());

					break;

				default:
					throw new RuntimeException("Unknown APCI= " + knxPacket.getCemiTunnelRequest().getApci());
				}

				break;

			default:

				throw new RuntimeException(
						"UNKOWN message code " + messageCode + " (" + Utils.integerToString(messageCode) + ")");

			}
			break;

		default:
			getLogger().warn("Ignoring: " + knxPacket.getHeader().getServiceIdentifier().name());
			break;
		}
	}

	@Override
	public boolean accepts(final DatagramPacket datagramPacket) {
		return false;
	}

	@Override
	public boolean accepts(final KNXPacket knxPacket) {
		switch (knxPacket.getHeader().getServiceIdentifier()) {
		case DEVICE_CONFIGURATION_REQUEST:
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
