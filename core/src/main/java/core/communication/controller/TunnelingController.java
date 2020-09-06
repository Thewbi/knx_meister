package core.communication.controller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.device.Device;
import api.packets.PropertyId;
import api.project.KNXComObject;
import api.project.KNXGroupAddress;
import common.packets.KNXConnectionHeader;
import common.packets.ServiceIdentifier;
import common.utils.NetworkUtils;
import common.utils.Utils;
import core.communication.Connection;
import core.data.sending.DataSender;
import core.packets.CemiTunnelRequest;
import core.packets.ConnectionType;
import core.packets.HPAIStructure;
import core.packets.KNXPacket;
import core.packets.StructureType;

public class TunnelingController extends BaseController {

    private static final int TUNNELING_DISCONNECT_REQUEST = 0x81;

    private static final int TUNNELING_CONNECTION_REQUEST = 0x80;

    private static final int DEVICE_DESCRIPTION_READ_TCPI = 0x03;
    // I do not know why there is a prefix 4 in there?!?
    private static final int DEVICE_DESCRIPTION_READ_TCPI_EXT = 0x43;

    private static final int GROUP_VALUE_WRITE = 0x00;

    private static final int HOPS = 0x60;

    private static final int PRIO_SYSTEM = 0xB0;

    private static final int DEVICE_DESCRIPTION_READ_APCI = 0x4300;

    private static final int DEVICE_READ_APCI = 0x0100;

    private static final Logger LOG = LogManager.getLogger(TunnelingController.class);

    private KNXPacket indicationKNXPacket;

//	private Thread dataSenderThread;

//    private DataSenderRunnable dataSenderRunnable;

//	/**
//	 * ctor
//	 *
//	 * @throws SocketException
//	 * @throws UnknownHostException
//	 */
//	public TunnelingController(final String localInetAddress) throws SocketException, UnknownHostException {
//		super(localInetAddress);
//	}

    @Override
    public void knxPacket(final Connection connection, final DatagramSocket datagramSocket,
            final DatagramPacket datagramPacket, final KNXPacket knxPacket, final String label) throws IOException {

        HPAIStructure hpaiStructure = null;
        final InetAddress inetAddress = null;
        final int port = -1;
        Connection packetConnection = null;
        int communicationChannelId = 0;
        HPAIStructure controlEndpointHPAIStructure = null;
        HPAIStructure dataEndpointHPAIStructure = null;

        LOG.trace("Tunneling controller '{}'", knxPacket.getHeader().getServiceIdentifier().name());

        switch (knxPacket.getHeader().getServiceIdentifier()) {

        // 0x0205
        case CONNECT_REQUEST:
            // if the connect request does not contain a CRI Tunneling Connection, this
            // connection should be handled by the core controller
            if (!knxPacket.getStructureMap().containsKey(StructureType.TUNNELING_CONNECTION)) {
                break;
            }

            final ConnectionType connectionType = knxPacket.getConnectionType();

            // create the new connection
            final Connection newConnection = getConnectionManager().createNewConnection(datagramSocket, connectionType);

            controlEndpointHPAIStructure = (HPAIStructure) knxPacket.getStructureMap()
                    .get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);
            newConnection.setControlEndpoint(controlEndpointHPAIStructure);

            dataEndpointHPAIStructure = (HPAIStructure) knxPacket.getStructureMap()
                    .get(StructureType.HPAI_DATA_ENDPOINT_UDP);
            newConnection.setDataEndpoint(dataEndpointHPAIStructure);

            // construct the acknowledge
            hpaiStructure = (HPAIStructure) knxPacket.getStructureMap().get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);
            final InetAddress controlInetAddress = InetAddress.getByAddress(hpaiStructure.getIpAddress());
            final int controlPort = hpaiStructure.getPort() & 0xFFFF;

            final KNXPacket sendConnectionResponse = retrieveConnectionResponse(knxPacket.getConnectionType());
            sendConnectionResponse.setCommunicationChannelId(newConnection.getId());

            // send the acknowledge
            newConnection.sendResponse(sendConnectionResponse, new InetSocketAddress(controlInetAddress, controlPort));

//            LOG.info("Tunneling controller starts data sender for connection " + newConnection.getId());
//			dataSenderThread = startThread(getClass().getName() + " CONNECT_REQUEST", newConnection);
//            dataSenderRunnable = startThread(getClass().getName() + " CONNECT_REQUEST", newConnection);

//			if (knxPacket.getCommunicationChannelId() >= 1) {
//				if (dataSenderThread == null) {
//					LOG.info("Tunneling controller starts data sender!");
//					dataSenderThread = startThread(getClass().getName() + " CONNECT_REQUEST", newConnection);
//				}
//			}
            break;

        case CONNECTIONSTATE_REQUEST:
            // make sure this is a tunneling connection
            communicationChannelId = knxPacket.getCommunicationChannelId();
            packetConnection = getConnectionManager().retrieveConnection(communicationChannelId);
            if (packetConnection.getConnectionType() != ConnectionType.TUNNEL_CONNECTION) {
                return;
            }

            LOG.info("CONNECTIONSTATE_REQUEST connection: " + communicationChannelId);

            final KNXPacket sendConnectionStateResponse = sendConnectionStateResponse(datagramSocket, datagramPacket,
                    knxPacket, inetAddress, port);

            controlEndpointHPAIStructure = (HPAIStructure) knxPacket.getStructureMap()
                    .get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);

            final InetSocketAddress socketAddr = new InetSocketAddress(
                    controlEndpointHPAIStructure.getIpAddressAsObject(), controlEndpointHPAIStructure.getPort());

            connection.sendResponse(sendConnectionStateResponse, socketAddr);
            break;

        case DISCONNECT_REQUEST:
//            if (dataSenderRunnable != null) {
////				throw new RuntimeException("Thread has to be stopped!");
////				dataSenderRunnable.stop();
//            }

            // make sure this is a tunneling connection
            communicationChannelId = knxPacket.getCommunicationChannelId();
            packetConnection = getConnectionManager().retrieveConnection(communicationChannelId);
            if (packetConnection == null || packetConnection.getConnectionType() != ConnectionType.TUNNEL_CONNECTION) {
                return;
            }

            getConnectionManager().closeConnection(knxPacket.getCommunicationChannelId());

            final KNXPacket sendDisconnetResponse = sendDisconnetResponse(datagramSocket, datagramPacket, knxPacket,
                    inetAddress, port);

            connection.sendResponse(sendDisconnetResponse, datagramPacket.getSocketAddress());
            break;

        // 0x0420
        case TUNNEL_REQUEST:
            processTunnelingRequest(connection, datagramSocket, datagramPacket, knxPacket, label);
            break;

        // 0x0421
        case TUNNEL_RESPONSE:
            if (knxPacket.getConnectionHeader() != null && knxPacket.getConnectionHeader().getReserved() == 0x21) {
                final String msg = "E_CONNECTION_ID: 0x21 - The KNXnet/IP server device could not find an active data connection with the given ID";
                LOG.error(msg);
            }
            break;

        default:
            getLogger().warn("Ignoring: " + knxPacket.getHeader().getServiceIdentifier().name());
            break;
        }
    }

    public void sendTunnelRequestConnect(final Connection connection) {

        LOG.info("sendTunnelRequestConnect() ...");

        final KNXConnectionHeader connectionHeader = new KNXConnectionHeader();
//		connectionHeader.setChannel((short) connection.getId());
//		connectionHeader.setSequenceCounter(connection.getSequenceCounter() + 2);

        final CemiTunnelRequest cemiTunnelRequest = new CemiTunnelRequest();
        cemiTunnelRequest.setMessageCode(BaseController.REQUEST_PRIMITIVE);
        cemiTunnelRequest.setAdditionalInfoLength(0);
        cemiTunnelRequest.setCtrl1(0xB0);
        cemiTunnelRequest.setCtrl2(0x60);
//		cemiTunnelRequest.setSourceKNXAddress(getDevice().getPhysicalAddress());
        cemiTunnelRequest.setSourceKNXAddress(0x11FF);
        cemiTunnelRequest.setDestKNXAddress(Utils.knxAddressToInteger("1.1.1"));
        cemiTunnelRequest.setLength(0);
        // tunnel connect request
        cemiTunnelRequest.setTpci(0x80);
//		cemiTunnelRequest.setApci(value == 0 ? 0x80 : 0x81);
//		cemiTunnelRequest.setPayloadBytes(dataSerializer.serializeToBytes(value));

        final KNXPacket knxPacket = new KNXPacket();
        knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_REQUEST);
        knxPacket.setConnectionHeader(connectionHeader);
        knxPacket.setCemiTunnelRequest(cemiTunnelRequest);

        try {
            connection.sendData(knxPacket);
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void processTunnelingRequest(final Connection connection, final DatagramSocket datagramSocket,
            final DatagramPacket datagramPacket, final KNXPacket knxPacket, final String label) throws IOException {

        KNXPacket tunnelResponse = null;
        KNXPacket confirmKNXPacket = null;
        KNXPacket acknowledgeKNXPacket = null;

//        Device device = getDeviceService().getDevices().get("1.1.255");
        final Device device = retrieveDevice(knxPacket);

        switch (knxPacket.getCemiTunnelRequest().getTpci()) {

        case GROUP_VALUE_WRITE:

            // if APCI has the value zero, someone tries to read data from the group
            if (knxPacket.getCemiTunnelRequest().getApci() == 0) {

                LOG.info("GroupValue read: {}",
                        Utils.integerToKNXAddress(knxPacket.getCemiTunnelRequest().getDestKNXAddress(), "/"));

                //
                // @formatter:off
				//
				// To read a group value, six messages are exchanged back and forth between A and B:
				//
				// 3671	2168	112.844979	192.168.2.2	3671	192.168.2.3	3671	KNXnet/IP	63	TunnelReq #02:7 L_Data.req 0.0.0->0/3/4 GroupValueRead
				// 3671	2169	112.846078	192.168.2.3	3671	192.168.2.2	3671	KNXnet/IP	60	TunnelAck #02:7 OK
				// 3671	2228	112.920365	192.168.2.3	3671	192.168.2.2	3671	KNXnet/IP	63	TunnelReq #02:16 L_Data.con 1.1.10->0/3/4 GroupValueRead
				// 3671	2229	112.920874	192.168.2.2	3671	192.168.2.3	3671	KNXnet/IP	60	TunnelAck #02:16 OK
				// 3671	2238	112.978934	192.168.2.3	3671	192.168.2.2	3671	KNXnet/IP	63	TunnelReq #02:17 L_Data.ind 1.1.255->0/3/4 GroupValueResp $00
				// 3671	2239	112.979299	192.168.2.2	3671	192.168.2.3	3671	KNXnet/IP	60	TunnelAck #02:17 OK
				//
				// Message 1 - A sends a request to read the a group address
				// Message 2 - B acknowledges that message with OK
				// Message 3 - B Sends a confirmation to A
				// Message 4 - A acknowledges that confirmation with OK
				// Message 5 - B sends the value to A
				// Message 6 - A acknowledges the value with OK
				//
				// @formatter:on
                //

                //
                // Message 2
                //
                // Request: INCOMING message was: GroupValueRead. The response has to be an OK
                // response which just means that the request was received. The real answer
                // is send later in this method
                //
                // ANSWER an TunnelAcknowledge+OK
                //

                final KNXPacket ackKnxPacket = new KNXPacket(knxPacket);
                ackKnxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_RESPONSE);
                ackKnxPacket.setCemiTunnelRequest(null);
                knxPacket.getConnection().sendResponse(ackKnxPacket, datagramPacket.getSocketAddress());

                try {
                    Thread.sleep(100);
                } catch (final InterruptedException e) {
                    LOG.error(e.getMessage(), e);
                }

                //
                // Message 3
                //
                // SEND TunnelRequest+Confirm
                //

                final KNXPacket response = new KNXPacket(knxPacket);

//				response.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_RESPONSE);
                response.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_REQUEST);

                // confirm or indicate ??? I am not sure
//				response.getCemiTunnelRequest().setMessageCode(BaseController.INDICATION_PRIMITIVE);
                response.getCemiTunnelRequest().setMessageCode(BaseController.CONFIRM_PRIMITIVE);

                response.getCemiTunnelRequest().setAdditionalInfoLength(0);
                response.getCemiTunnelRequest().setLength(1);
//				response.getCemiTunnelRequest().setLength(0);
                // set GroupValueResponse and value
                // bitmask 01000001 (lower 6 bit are the value, upper two are the type, 01 is
                // response)
                // response.getCemiTunnelRequest().setApci(0x40 | ((byte) getDevice().getValue()
                // & 0xFF));
                response.getCemiTunnelRequest().setTpci(0x00);
                response.getCemiTunnelRequest().setApci(0x00);
//				response.getCemiTunnelRequest().setSourceKNXAddress(0x11FF);
//				response.getCemiTunnelRequest().setSourceKNXAddress(0x110B);
//				response.getCemiTunnelRequest().setDestKNXAddress(Utils.knxAddressToInteger("0/3/4"));
                // increment sequence
//				response.getConnectionHeader().setSequenceCounter(response.getConnectionHeader().getSequenceCounter());
                knxPacket.getConnection().sendResponse(response, datagramPacket.getSocketAddress());

                try {
                    Thread.sleep(100);
                } catch (final InterruptedException e) {
                    LOG.error(e.getMessage(), e);
                }

                //
                // Message 5
                //
                // SEND TunnelRequest+Indication GroupValueResp
                //
                // This is where the actual value is send as a payload
                //

                // TODO
                sendData(knxPacket, getDeviceService().getDevices().get("1.1.255"));

            } else {

                // TODO: write a data extractor that knows the datatype of the group address and
                // can correctly retrieve the data send inside the KNXPacket Tunneling Request

                // for a switch with a byte datatype, the value is encoded into the APCI byte
                // for some reason.
                // It is encoded into the lower six bit = 0x3F.
                final int value = ((byte) knxPacket.getCemiTunnelRequest().getApci().intValue()) & 0x3F;
                LOG.info("GROUP_VALUE_WRITE From External Client: Value: " + value);

                LOG.info("GroupValue write. Value = " + value);

                // TODO: set value into the device
//				getDevice().setValue(value);

                // send an acknowledge
                final KNXPacket ackKnxPacket = new KNXPacket(knxPacket);
                ackKnxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_RESPONSE);
                ackKnxPacket.setCemiTunnelRequest(null);

                knxPacket.getConnection().sendResponse(ackKnxPacket, datagramPacket.getSocketAddress());

                // send confirm
                final KNXPacket response = new KNXPacket(knxPacket);

//				response.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_RESPONSE);
                response.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_REQUEST);

                response.getCemiTunnelRequest().setMessageCode(BaseController.INDICATION_PRIMITIVE);
                response.getCemiTunnelRequest().setAdditionalInfoLength(0);
                response.getCemiTunnelRequest().setLength(1);
                // set GroupValueResponse and value
                // bitmask 01000001 (lower 6 bit are the value, upper two are the type, 01 is
                // response)

                // TODO: hardcoded magic number!
//				response.getCemiTunnelRequest().setSourceKNXAddress(0x11FF);
                response.getCemiTunnelRequest().setSourceKNXAddress(0x110B);

                // TODO: hardcoded magic number!
                response.getCemiTunnelRequest().setDestKNXAddress(Utils.knxAddressToInteger("0/3/4"));

//				response.getCemiTunnelRequest().setApci(0x41);
                response.getCemiTunnelRequest().setApci(0x80 | ((byte) (value & 0xFF)));

                // increment sequence
                response.getConnectionHeader().setSequenceCounter(response.getConnectionHeader().getSequenceCounter());

                knxPacket.getConnection().sendResponse(response, datagramPacket.getSocketAddress());

//				startThread("TunnelingController", knxPacket.getConnection());
            }

            break;

        case DEVICE_DESCRIPTION_READ_TCPI:
        case DEVICE_DESCRIPTION_READ_TCPI_EXT:
            // send acknowledge
            tunnelResponse = sendTunnelResponse(knxPacket, datagramSocket, datagramPacket);
            knxPacket.getConnection().sendResponse(tunnelResponse, datagramPacket.getSocketAddress());

            // all four packets req+OK, ind+OK belong to the same sequence counter value
//			sequenceCounter = knxPacket.getConnection().getSequenceCounter();

//            device = retrieveDevice(knxPacket);

            // send message acknowledge, send answer message, send confirm
            confirmKNXPacket = new KNXPacket(knxPacket);
//			confirmKNXPacket.getConnectionHeader().setSequenceCounter(sequenceCounter);
            confirmKNXPacket.getCemiTunnelRequest().setMessageCode(CONFIRM_PRIMITIVE);
            confirmKNXPacket.getCemiTunnelRequest().setSourceKNXAddress(device.getPhysicalAddress());
            confirmKNXPacket.getCemiTunnelRequest()
                    .setDestKNXAddress(knxPacket.getCemiTunnelRequest().getDestKNXAddress());
            confirmKNXPacket.getCemiTunnelRequest().setCtrl1(0x90);
            confirmKNXPacket.getCemiTunnelRequest().setLength(1);
            // <------- FIX for different application services!
            confirmKNXPacket.getCemiTunnelRequest().setApci(DEVICE_DESCRIPTION_READ_APCI);

            knxPacket.getConnection().sendResponse(confirmKNXPacket, datagramPacket.getSocketAddress());

//			// prepare packets that are sent when the communication partner sends a tunnel
//			// response
//			indicationKNXPacket = retrieveDeviceDescriptionReadAPCIIndicationPacket(knxPacket, sequenceCounter + 1);
//			acknowledgeKNXPacket = retrieveDeviceDescriptionReadAPCIAcknowledgePacket(knxPacket, sequenceCounter);
            break;

        case 0x01:
            // send acknowledge
            tunnelResponse = sendTunnelResponse(knxPacket, datagramSocket, datagramPacket);
            knxPacket.getConnection().sendResponse(tunnelResponse, datagramPacket.getSocketAddress());

//			sequenceCounter = knxPacket.getConnection().getSequenceCounter();

//            device = retrieveDevice(knxPacket);

            // send message acknowledge, send answer message
            confirmKNXPacket = new KNXPacket(knxPacket);
//			confirmKNXPacket.getConnectionHeader().setSequenceCounter(sequenceCounter);
            confirmKNXPacket.getCemiTunnelRequest().setMessageCode(CONFIRM_PRIMITIVE);
            confirmKNXPacket.getCemiTunnelRequest().setSourceKNXAddress(device.getPhysicalAddress());
            confirmKNXPacket.getCemiTunnelRequest()
                    .setDestKNXAddress(knxPacket.getCemiTunnelRequest().getSourceKNXAddress());
            confirmKNXPacket.getCemiTunnelRequest().setCtrl1(0x91);
            confirmKNXPacket.getCemiTunnelRequest().setLength(1);
            // <------- FIX for different application services!
            confirmKNXPacket.getCemiTunnelRequest().setApci(DEVICE_READ_APCI);
            knxPacket.getConnection().sendResponse(confirmKNXPacket, datagramPacket.getSocketAddress());

            indicationKNXPacket = retrieveDeviceReadAPCIIndicationPacket(knxPacket, device);
            break;

        case TUNNELING_DISCONNECT_REQUEST:
            LOG.info("Sending Tunnel DISCONNECT response for tunnel connection id {} ...", connection.getId());

            // ANSWER tunnel acknowledge
            acknowledgeKNXPacket = new KNXPacket(knxPacket);
            acknowledgeKNXPacket.setCemiPropReadRequest(null);
            acknowledgeKNXPacket.setCemiTunnelRequest(null);
            acknowledgeKNXPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_RESPONSE);

            // send response
            knxPacket.getConnection().sendResponse(acknowledgeKNXPacket, datagramPacket.getSocketAddress());

            indicationKNXPacket = new KNXPacket(knxPacket);
            indicationKNXPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_REQUEST);
            indicationKNXPacket.getCemiTunnelRequest().setMessageCode(BaseController.INDICATION_PRIMITIVE);

            // send back indication
            knxPacket.getConnection().sendData(indicationKNXPacket);
            break;

        // Example Wireshark: TunnelReq #01:0 L_Data.req 0.0.0->1.1.255 Connect
        case TUNNELING_CONNECTION_REQUEST:
            LOG.info("Sending Tunnel CONNECTION response for tunnel connection id {} ...", connection.getId());

            // ACK - tunnel acknowledge
            // Example Wireshark: TunnelAck #02:0 OK
            acknowledgeKNXPacket = new KNXPacket(knxPacket);
            acknowledgeKNXPacket.setCemiPropReadRequest(null);
            acknowledgeKNXPacket.setCemiTunnelRequest(null);
            acknowledgeKNXPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_RESPONSE);

            // send back acknowledge
            knxPacket.getConnection().sendResponse(acknowledgeKNXPacket, datagramPacket.getSocketAddress());

            // send confirmation
            // Example Wireshark: TunnelReq #02:0 L_Data.con 1.1.10->1.1.255 Connect
            confirmKNXPacket = new KNXPacket(knxPacket);
            confirmKNXPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_REQUEST);
            confirmKNXPacket.getCemiTunnelRequest().setMessageCode(BaseController.CONFIRM_PRIMITIVE);
            confirmKNXPacket.getCemiTunnelRequest().setSourceKNXAddress(Utils.knxAddressToInteger("1.1.10"));

            // send back confirmation
            knxPacket.getConnection().sendData(confirmKNXPacket);

//			// send response
//			// Example Wireshark: TunnelReq #02:0 L_Data.con 1.1.10->1.1.255 Connect
//			indicationKNXPacket = new KNXPacket(knxPacket);
//			indicationKNXPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_REQUEST);
//			indicationKNXPacket.getCemiTunnelRequest().setMessageCode(BaseController.INDICATION_PRIMITIVE);
//
//			// send back indication
//			knxPacket.getConnection().sendData(indicationKNXPacket);
            break;

        default:
            throw new RuntimeException("Unknown message TPCI=" + knxPacket.getCemiTunnelRequest().getTpci() + " APCI="
                    + knxPacket.getCemiTunnelRequest().getApci());
        }
    }

    private void sendData(final KNXPacket knxPacket, final Device device) throws IOException {

//        final String devicePhysicalAddress = "1.1.255";
//        final String devicePhysicalAddress = Utils.integerToKNXAddress(knxPacket.getCemiPropReadRequest(), ".");
        final String groupAddress = Utils.integerToKNXAddress(knxPacket.getCemiTunnelRequest().getDestKNXAddress(),
                "/");
        final KNXGroupAddress knxGroupAddress = device.getDeviceProperties().get(groupAddress);
        if (knxGroupAddress == null) {
            LOG.warn("GroupAddress is unknown: " + knxGroupAddress);
            return;
        }

        final KNXComObject knxComObject = device.getComObjects().get(groupAddress);

        final int dataPointId = knxComObject.getNumber();

        final int deviceIndex = 0;
        getDataSender().send(knxPacket.getConnection(), Utils.integerToKNXAddress(device.getPhysicalAddress(), "."),
                groupAddress, dataPointId, knxGroupAddress.getValue() == null ? 0 : knxGroupAddress.getValue(),
                deviceIndex);
    }

    @SuppressWarnings("unused")
    private void sendData2(final KNXPacket knxPacket, final Device device) throws IOException {

        final String integerToKNXAddress = Utils
                .integerToKNXAddress(knxPacket.getCemiTunnelRequest().getDestKNXAddress(), "/");
        final KNXGroupAddress knxGroupAddress = device.getDeviceProperties().get(integerToKNXAddress);
        if (knxGroupAddress == null) {
            LOG.warn("GroupAddress is unknown: " + knxGroupAddress);
            return;
        }

        final KNXConnectionHeader connectionHeader = new KNXConnectionHeader();

        final CemiTunnelRequest cemiTunnelRequest = new CemiTunnelRequest();
        cemiTunnelRequest.setMessageCode(BaseController.INDICATION_PRIMITIVE);
        cemiTunnelRequest.setAdditionalInfoLength(0);
        cemiTunnelRequest.setCtrl1(DataSender.PRIORITY_NORMAL);
        cemiTunnelRequest.setCtrl2(DataSender.HOP_COUNT_6);
        cemiTunnelRequest.setSourceKNXAddress(NetworkUtils.toNetworkOrder((short) device.getPhysicalAddress()));
//				cemiTunnelRequest.setDestKNXAddress(Utils.knxAddressToInteger("0/3/4"));
        cemiTunnelRequest.setDestKNXAddress(knxPacket.getCemiTunnelRequest().getDestKNXAddress());
        cemiTunnelRequest.setLength(1);

        // @formatter:off
		//
		// TCPI and APCI are encoded in two byte
		// The format is:
		// TT.. ..AA AAAA AAAA
		// ||     || |||| ||||
		//                   data bit 6
		//
		// @formatter:on

        cemiTunnelRequest.setTpci(0x00);

//				final String integerToKNXAddress = Utils
//						.integerToKNXAddress(knxPacket.getCemiTunnelRequest().getDestKNXAddress(), "/");
//				final KNXGroupAddress knxGroupAddress = getDevice().getDeviceProperties().get(integerToKNXAddress);
//				if (knxGroupAddress != null) {
        final String dataPointType = knxGroupAddress.getDataPointType();

//		final DataSerializer<Object> dataSerializer = dataSerializerMapByDataPointType.get(dataPointType);
//		if (dataSerializer == null) {
//			throw new RuntimeException("DataPointType: " + dataPointType + " not implemented yet!");
//		} else {
//			dataSerializer.serializeToBytes(data)
//		}

        if (StringUtils.equalsAnyIgnoreCase(dataPointType, "DPST-1-1")) {

            // default value is zero
            int value = 0;

            // if a specific value is given, use that specific value
            if (knxGroupAddress.getValue() != null) {
                value = (int) knxGroupAddress.getValue();
            }

            // response bit + value
            // TODO: answer with the correct data type
            cemiTunnelRequest.setApci(0x40 | ((byte) value & 0xFF));

        } else {
            throw new RuntimeException("dataPointType: " + dataPointType + " not implemented yet!");
        }
//				}

        final KNXPacket requestIndication = new KNXPacket();
        requestIndication.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_REQUEST);
        requestIndication.setConnectionHeader(connectionHeader);
        requestIndication.setCemiTunnelRequest(cemiTunnelRequest);

        knxPacket.getConnection().sendData(requestIndication);
    }

    private KNXPacket retrieveDeviceReadAPCIIndicationPacket(final KNXPacket knxPacket, final Device device) {

        final CemiTunnelRequest cemiTunnelRequest = new CemiTunnelRequest();
        // 0x29 == ind (the application layer sends response, the network layer converts
        // response to indication!)
        cemiTunnelRequest.setMessageCode(INDICATION_PRIMITIVE);
        cemiTunnelRequest.setAdditionalInfoLength(0);
        cemiTunnelRequest.setCtrl1(0xB0);
        cemiTunnelRequest.setCtrl2(0xE0);
        cemiTunnelRequest.setSourceKNXAddress(device.getPhysicalAddress());
        cemiTunnelRequest.setDestKNXAddress(0);
        cemiTunnelRequest.setTpci(0x01);
        cemiTunnelRequest.setApci(0x40);

        final KNXConnectionHeader connectionHeader = new KNXConnectionHeader();
        connectionHeader.setChannel(knxPacket.getConnectionHeader().getChannel());
        connectionHeader.setSequenceCounter(knxPacket.getConnectionHeader().getSequenceCounter() + 1);
        connectionHeader.setReserved(0x00);

        final KNXPacket indicationKNXPacket = new KNXPacket();
        indicationKNXPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_REQUEST);
        indicationKNXPacket.setConnectionHeader(connectionHeader);
        indicationKNXPacket.setCemiTunnelRequest(cemiTunnelRequest);

        return indicationKNXPacket;
    }

    /**
     * Learned from connecting ETS5 to the BAOS and sniffing the packets using
     * wireshark.
     *
     * @param knxPacket
     * @param sequenceCounter
     * @return
     */
    @SuppressWarnings("unused")
    private KNXPacket retrieveDeviceDescriptionReadAPCIAcknowledgePacket(final KNXPacket knxPacket,
            final int sequenceCounter, final Device device) {

        final CemiTunnelRequest cemiTunnelRequest = new CemiTunnelRequest();
        // 0x29 == ind (the application layer sends response, the network layer converts
        // response to indication!)
        cemiTunnelRequest.setMessageCode(REQUEST_PRIMITIVE);
        cemiTunnelRequest.setAdditionalInfoLength(0);
        cemiTunnelRequest.setCtrl1(PRIO_SYSTEM);
        cemiTunnelRequest.setCtrl2(HOPS);
        cemiTunnelRequest.setSourceKNXAddress(knxPacket.getCemiTunnelRequest().getDestKNXAddress());
        cemiTunnelRequest.setDestKNXAddress(device.getPhysicalAddress());
        cemiTunnelRequest.setTpci(0xc2);

        final KNXConnectionHeader connectionHeader = new KNXConnectionHeader();
        connectionHeader.setChannel(knxPacket.getConnectionHeader().getChannel());
        connectionHeader.setSequenceCounter(sequenceCounter + 1);
        connectionHeader.setReserved(0x00);

        // send the answer, that the server wanted
        final KNXPacket acknowledgeKNXPacket = new KNXPacket();
        acknowledgeKNXPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_REQUEST);
        acknowledgeKNXPacket.setConnectionHeader(connectionHeader);
        acknowledgeKNXPacket.setCemiTunnelRequest(cemiTunnelRequest);

        return acknowledgeKNXPacket;
    }

    /**
     * Create a packet that transfers the device's DEVICE_DESCRIPTOR property as a
     * payload.
     *
     * Learned from connecting ETS5 to the BAOS and sniffing the packets using
     * wireshark.
     *
     * @param knxPacket
     * @param sequenceCounter
     *
     * @return
     */
    @SuppressWarnings("unused")
    private KNXPacket retrieveDeviceDescriptionReadAPCIIndicationPacket(final KNXPacket knxPacket,
            final int sequenceCounter, final Device device) {

        // retrieve the device descriptor property and put it into the packet as payload
        final short deviceDescriptor = device.getProperties().get((short) PropertyId.PID_DEVICE_DESCRIPTOR.getValue());
        final byte[] payload = Utils.shortToByteArray(deviceDescriptor);

        final CemiTunnelRequest cemiTunnelRequest = new CemiTunnelRequest();
        // 0x29 == ind (the application layer sends response, the network layer converts
        // response to indication!)
        cemiTunnelRequest.setMessageCode(INDICATION_PRIMITIVE);
        cemiTunnelRequest.setAdditionalInfoLength(0);
        cemiTunnelRequest.setCtrl1(0x90);
        cemiTunnelRequest.setCtrl2(0x60);
        cemiTunnelRequest.setSourceKNXAddress(knxPacket.getCemiTunnelRequest().getDestKNXAddress());
        cemiTunnelRequest.setDestKNXAddress(device.getPhysicalAddress());
        cemiTunnelRequest.setTpci(0x43);
        cemiTunnelRequest.setApci(0x40);
        cemiTunnelRequest.setPayloadBytes(payload);

        // send the answer, that the server wanted
        final KNXPacket indicationKNXPacket = new KNXPacket();
        indicationKNXPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_REQUEST);
        indicationKNXPacket.setConnectionHeader(new KNXConnectionHeader());
        indicationKNXPacket.getConnectionHeader().setChannel(knxPacket.getConnectionHeader().getChannel());
        indicationKNXPacket.getConnectionHeader().setSequenceCounter(sequenceCounter + 1);
        indicationKNXPacket.getConnectionHeader().setReserved(0x00);
        indicationKNXPacket.setCemiTunnelRequest(cemiTunnelRequest);

        return indicationKNXPacket;
    }

    private KNXPacket sendTunnelResponse(final KNXPacket knxPacket, final DatagramSocket socket3671,
            final DatagramPacket datagramPacket) {

        final KNXPacket outKNXPacket = new KNXPacket();
        outKNXPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_RESPONSE);

        outKNXPacket.setConnectionHeader(new KNXConnectionHeader());
        outKNXPacket.getConnectionHeader().setChannel(knxPacket.getConnectionHeader().getChannel());
        outKNXPacket.getConnectionHeader().setSequenceCounter(knxPacket.getConnectionHeader().getSequenceCounter());
        // status OK
        outKNXPacket.getConnectionHeader().setReserved(0x00);

        return outKNXPacket;
    }

    @Override
    public boolean accepts(final DatagramPacket datagramPacket) {
        return false;
    }

    @Override
    public boolean accepts(final KNXPacket knxPacket) {
        switch (knxPacket.getHeader().getServiceIdentifier()) {
        case TUNNEL_REQUEST:
        case TUNNEL_RESPONSE:
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
