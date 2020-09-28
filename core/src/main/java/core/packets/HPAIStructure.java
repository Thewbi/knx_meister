package core.packets;

import java.net.InetAddress;
import java.net.UnknownHostException;

import common.utils.NetworkUtils;
import common.utils.Utils;

/**
 * Host Protocol Address Information (HPAI)
 */
public class HPAIStructure extends Structure {

    private static final int IPV4_UDP = 0x01;

    private byte[] ipAddress = new byte[4];

    private int port;

    public HPAIStructure() {
        setStructureType(StructureType.HPAI_CONTROL_ENDPOINT_UDP);
    }

    public HPAIStructure(final HPAIStructure other) {
        setLength(other.getLength());
        setStructureType(other.getStructureType());
        ipAddress = other.ipAddress.clone();
        port = other.port;
    }

    @Override
    public Structure clone() {
        return new HPAIStructure(this);
    }

    @Override
    public byte[] getPayloadBytes() {

        int index = 0;

        final byte[] payload = new byte[6];
        payload[index++] = ipAddress[0];
        payload[index++] = ipAddress[1];
        payload[index++] = ipAddress[2];
        payload[index++] = ipAddress[3];
        payload[index++] = (byte) (((port) >> 8) & 0xFF);
        payload[index++] = (byte) (port & 0xFF);

        return payload;
    }

    @Override
    public void fromBytes(final byte[] bytes, final int startIndex) {

        ipAddress[0] = bytes[startIndex];
        ipAddress[1] = bytes[startIndex + 1];
        ipAddress[2] = bytes[startIndex + 2];
        ipAddress[3] = bytes[startIndex + 3];
        port = Utils.bytesToUnsignedShort(bytes[startIndex + 4], bytes[startIndex + 5], true);
    }

    @Override
    public String toString() {

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.toString()).append("\n");
        stringBuilder.append("Host Protocol: ").append(getStructureType().name()).append("\n");
        stringBuilder.append("IP Address: ").append(NetworkUtils.printIPAddress(ipAddress)).append("\n");
        stringBuilder.append("Port Number: ").append(port & 0xFFFF);

        return stringBuilder.toString();
    }

    public byte[] getIpAddress() {
        return ipAddress;
    }

    public InetAddress getIpAddressAsObject() throws UnknownHostException {
        return InetAddress.getByAddress(ipAddress);
    }

    public void setIpAddress(final byte[] ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

}
