package core.packets;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import api.configuration.ConfigurationManager;
import api.device.DeviceStatus;
import common.packets.ServiceIdentifier;
import common.utils.Utils;

public class KNXPacketTest {

    private static final Logger LOG = LogManager.getLogger(KNXPacketTest.class);

    @Test
    public void testGetBytesDescriptionRequest() {

        final HPAIStructure hpaiStructure = new HPAIStructure();
        hpaiStructure.setIpAddress(new byte[] { 1, 2, 3, 4 });
        hpaiStructure.setPort((short) 1234);

        final KNXPacket knxPacket = new KNXPacket();
        knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.DESCRIPTION_REQUEST);
        knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, hpaiStructure);

        final byte[] bytes = knxPacket.getBytes();

        LOG.info(Utils.integerToStringNoPrefix(bytes));

//		06 10 02 03 00 0E 08 01 01 02 03 04 04 D2

        int index = 0;

        // 1 byte header length
        assertEquals(0x06, bytes[index++]);

        // protocol version in Binary Coded Decimal (1.0 = 10)
        assertEquals(0x10, bytes[index++]);

        // service identifier 0x0203
        assertEquals(0x02, bytes[index++]);
        assertEquals(0x03, bytes[index++]);

        // total KNX message length, 14d = 0x0e in this test
        assertEquals(0x00, bytes[index++]);
        assertEquals(0x0e, bytes[index++]);

        // 8 byte length
        assertEquals(0x08, bytes[index++]);

        // type HPAI = 0x01
        assertEquals(0x01, bytes[index++]);

        // IP 1.2.3.4
        assertEquals(0x01, bytes[index++]);
        assertEquals(0x02, bytes[index++]);
        assertEquals(0x03, bytes[index++]);
        assertEquals(0x04, bytes[index++]);

        // port 1234d = 0x04D2
        assertEquals(0x04D2, Utils.bytesToUnsignedShort(bytes[index++], bytes[index++], true));
    }

    @Test
    public void testGetBytesSearchResponse() {

        final KNXPacket knxPacket = new KNXPacket();

        // header
        knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.SEARCH_RESPONSE);

        // HPAI
        final HPAIStructure hpaiStructure = new HPAIStructure();
        hpaiStructure.setIpAddress(new byte[] { (byte) 192, (byte) 168, 2, 3 });
        hpaiStructure.setPort((short) 3671);
        knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, hpaiStructure);

        final DeviceInformationDIB deviceInformationDIB = retrieveDeviceInformationDIB();
        knxPacket.getDibMap().put(deviceInformationDIB.getType(), deviceInformationDIB);

        final SuppSvcFamiliesDIB suppSvcFamiliesDIB = retrieveServiceFamiliesDIB();
        knxPacket.getDibMap().put(suppSvcFamiliesDIB.getType(), suppSvcFamiliesDIB);

        final MfrDataDIB mfrDataDIB = retrieveMfrDataDIB();
        knxPacket.getDibMap().put(mfrDataDIB.getType(), mfrDataDIB);

        final byte[] bytes = knxPacket.getBytes();

        LOG.info(Utils.integerToStringNoPrefix(bytes));

//		06 10 02 02 00 54 08 01 C0 A8 02 01 FD E8 36 01 20 01 11 02 00 11 03 03 03 03 03 03 E0 00 17 0C 01 02 03 04 05 06 74 65 73 74 5F 6F 62 6A 65 63 74 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 08 02 02 01 03 02 04 01 08 FE 00 C5 01 04 F0 20
//		06 10 02 02 00 54 08 01 C0 BA 02 03 0E 57 36 01 02 00 11 01 00 00 00 C5 01 02 D8 4C E0 00 17 0C 00 24 6D 01 D8 0A 4B 4E 58 20 49 50 20 42 41 4F 53 20 37 37 37 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 08 02 02 01 03 02 04 01 08 FE 00 C5 01 04 F0 20
//      06 10 02 02 00 54 08 01 c0 a8 02 03 0e 57 36 01 02 00 11 01 00 00 00 c5 01 02 d8 4c e0 00 17 0c 00 24 6d 01 d8 0a 4b 4e 58 20 49 50 20 42 41 4f 53 20 37 37 37 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 08 02 02 01 03 02 04 01 08 fe 00 c5 01 04 f0 20

        int index = 0;

        // 1 byte header length
        assertEquals(0x06, bytes[index++]);

        // protocol version in Binary Coded Decimal (1.0 = 10)
        assertEquals(0x10, bytes[index++]);

        // service identifier 0x0203
        assertEquals(0x02, bytes[index++]);
        assertEquals(0x02, bytes[index++]);

        // total KNX message length
        assertEquals(0x00, bytes[index++]);
        assertEquals(84, bytes[index++]);

        // 8 byte length
        assertEquals(0x08, bytes[index++]);

        // type HPAI = 0x01
        assertEquals(0x01, bytes[index++]);

        // IP address
        assertEquals((byte) 192, bytes[index++]);
        assertEquals((byte) 168, bytes[index++]);
        assertEquals((byte) 2, bytes[index++]);
        assertEquals((byte) 3, bytes[index++]);

        // port 1234d = 0x04D2
        assertEquals((short) 3671, Utils.bytesToUnsignedShort(bytes[index++], bytes[index++], true));
    }

    /**
     * device info DescriptionInformationBlock (DIB)
     */
    private DeviceInformationDIB retrieveDeviceInformationDIB() {

        final DeviceInformationDIB deviceInformationDIB = new DeviceInformationDIB();
        deviceInformationDIB.setDeviceStatus(DeviceStatus.NORMAL_MODE);
        deviceInformationDIB.setIndividualAddress(0x1101);
        deviceInformationDIB.setMedium(KNXMedium.TP1);
        deviceInformationDIB.setProjectInstallationIdentifier(0);

        // serial number
        System.arraycopy(new byte[] { (byte) 0x00, (byte) 0xC5, (byte) 0x01, (byte) 0x02, (byte) 0xD8, (byte) 0x4C }, 0,
                deviceInformationDIB.getDeviceSerialNumber(), 0, deviceInformationDIB.getDeviceSerialNumber().length);

        // multicast address - 224.0.23.12
        System.arraycopy(new byte[] { (byte) 0xE0, (byte) 0x00, (byte) 0x17, (byte) 0x0C }, 0,
                deviceInformationDIB.getDeviceRoutingMulticastAddress(), 0,
                deviceInformationDIB.getDeviceRoutingMulticastAddress().length);

        // mac address
        System.arraycopy(new byte[] { (byte) 0x00, (byte) 0x24, (byte) 0x6D, (byte) 0x01, (byte) 0xD8, (byte) 0x0A }, 0,
                deviceInformationDIB.getDeviceMacAddress(), 0, deviceInformationDIB.getDeviceMacAddress().length);

        // friendly name
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

    @Test
    public void testConnectionRequest() throws UnknownHostException {

        final HPAIStructure controlHPAIStructure = new HPAIStructure();
        controlHPAIStructure.setIpAddress(InetAddress.getByName("192.168.2.2").getAddress());
        controlHPAIStructure.setPort((short) ConfigurationManager.POINT_TO_POINT_CONTROL_PORT);

        final HPAIStructure dataHPAIStructure = new HPAIStructure();
        dataHPAIStructure.setIpAddress(InetAddress.getByName("192.168.2.2").getAddress());
        dataHPAIStructure.setPort((short) ConfigurationManager.POINT_TO_POINT_DATA_PORT);

        final ConnectionRequestInformation connectionRequestInformation = new ConnectionRequestInformation();
        connectionRequestInformation.setStructureType(StructureType.TUNNELING_CONNECTION);
        connectionRequestInformation.setKnxLayer(KNXLayer.TUNNEL_LINKLAYER.getValue());

        // construct the packet
        final KNXPacket knxPacket = new KNXPacket();
        knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.CONNECT_REQUEST);
        knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, controlHPAIStructure);
        knxPacket.getStructureMap().put(StructureType.HPAI_DATA_ENDPOINT_UDP, dataHPAIStructure);
        knxPacket.getStructureMap().put(connectionRequestInformation.getStructureType(), connectionRequestInformation);

        final byte[] bytes = knxPacket.getBytes();
        LOG.info(Utils.integerToStringNoPrefix(bytes));
        LOG.info("test");
    }
}
