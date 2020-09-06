package core.communication.controller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import api.configuration.ConfigurationManager;
import api.device.Device;
import api.device.DeviceService;
import common.packets.ServiceIdentifier;
import common.utils.Utils;
import core.communication.BaseDatagramPacketCallback;
import core.communication.Connection;
import core.communication.ConnectionManager;
import core.communication.thread.DataSenderRunnable;
import core.data.sending.DataSender;
import core.packets.CemiTunnelRequest;
import core.packets.ConnectionResponseDataBlock;
import core.packets.ConnectionStatus;
import core.packets.ConnectionType;
import core.packets.HPAIStructure;
import core.packets.KNXPacket;
import core.packets.StructureType;

public abstract class BaseController extends BaseDatagramPacketCallback {

    public static final short INDICATION_PRIMITIVE = 0x29;

    public static final short CONFIRM_PRIMITIVE = 0x2e;

    public static final short REQUEST_PRIMITIVE = 0x11;

    /** ??? what is the correct value???????? */
    public static final short RESPONSE_PRIMITIVE = 0xFF;

    private String localInetAddress;

    private final Map<String, HPAIStructure> deviceMap = new HashMap<>();

    private ConnectionManager connectionManager;

    private ConfigurationManager configurationManager;

//	private Device device;

    private DataSender dataSender;

    private DeviceService deviceService;

//	public BaseController(final String localInetAddress) throws SocketException, UnknownHostException {
//		this.localInetAddress = localInetAddress;
//	}

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
            hpaiStructure.setPort((short) ConfigurationManager.POINT_TO_POINT_CONTROL_PORT);
            knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, hpaiStructure);
        }

        final boolean addconnectionResponseDataBlock = true;
        if (addconnectionResponseDataBlock) {

            // CRD - Connection Response Data Block
            final ConnectionResponseDataBlock connectionResponseDataBlock = new ConnectionResponseDataBlock();
            connectionResponseDataBlock.setConnectionType(connectionType);
            if (connectionType != ConnectionType.DEVICE_MGMT_CONNECTION) {

//                // TODO
//                final Device device = deviceService.getDevices().get("");
//                connectionResponseDataBlock.setDeviceAddress(device.getHostPhysicalAddress());

                // in wireshark, weinzierl BAOS sends a additional physical address in response
                // to a
                // tunnel connection request. Additional physical addresses are listed in the
                // ETS 5 tool.
                // The Weinzierl does not send the address of a device (e.g. not 1.1.255)
                connectionResponseDataBlock.setDeviceAddress(Utils.knxAddressToIntegerDeviceAddressFirst("1.1.10"));
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

    protected DataSenderRunnable startThread(final String label, final Connection connection) {

        getLogger().info(label + " Starting Thread");

        final DataSenderRunnable dataSenderRunnable = new DataSenderRunnable();
//        dataSenderRunnable.setDeviceIndex(Main.DEVICE_INDEX);
        dataSenderRunnable.setLabel(label);
        dataSenderRunnable.setDataSender(dataSender);
        dataSenderRunnable.setConnection(connection);
        dataSenderRunnable.setDeviceService(deviceService);

        final Thread thread = new Thread(dataSenderRunnable);
        thread.start();

        return dataSenderRunnable;
    }

    protected Device retrieveDevice(final KNXPacket knxPacket) {

        final CemiTunnelRequest cemiTunnelRequest = knxPacket.getCemiTunnelRequest();

        if (cemiTunnelRequest == null) {
            return null;
        }

        final int destKNXAddress = cemiTunnelRequest.getDestKNXAddress();
        final String physicalAddress = Utils.integerToKNXAddress(destKNXAddress, ".");
        final Map<String, Device> devices = getDeviceService().getDevices();

        return devices.get(physicalAddress);
    }

    public void setConnectionManager(final ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

//    public Device getDevice() {
////		return device;
//        return null;
//    }
//
//	public void setDevice(final Device device) {
//		this.device = device;
//	}

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public Map<String, HPAIStructure> getDeviceMap() {
        return deviceMap;
    }

    public String getLocalInetAddress() {
        return localInetAddress;
    }

    public void setLocalInetAddress(final String localInetAddress) {
        this.localInetAddress = localInetAddress;
    }

    public DataSender getDataSender() {
        return dataSender;
    }

    public void setDataSender(final DataSender dataSender) {
        this.dataSender = dataSender;
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public void setConfigurationManager(final ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public void setDeviceService(final DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    public DeviceService getDeviceService() {
        return deviceService;
    }

}
