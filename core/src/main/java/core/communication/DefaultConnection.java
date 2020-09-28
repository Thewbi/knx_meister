package core.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.exception.CommunicationException;
import api.exception.SequenceCounterException;
import api.pipeline.Pipeline;
import common.utils.Utils;
import core.packets.ConnectionType;
import core.packets.HPAIStructure;
import core.packets.KNXPacket;

public class DefaultConnection implements Connection {

    private static final Logger LOG = LogManager.getLogger(DefaultConnection.class);

    private long timestampLastUsed = System.currentTimeMillis();

    private int id;

    /**
     * The sequenceCounter is not a number used to order UDP packets received out of
     * order. The sequenceCounter correlates several UDP packets to a unit of work.
     * <br />
     * <br />
     * For example the Tunneling DEVICE_DESCRIPTION_READ_APCI unit of work consists
     * of four packets all belonging to the same sequenceCounter value req+OK,
     * ind+OK.
     */
    private int sendSequenceCounter = -1;

    private int receiveSequenceCounter = -1;

    private DatagramSocket datagramSocket;

    private ConnectionType connectionType;

    private Pipeline<Object, Object> outputPipeline;

    private HPAIStructure controlEndpoint;

    private HPAIStructure dataEndpoint;

    private void prepare() throws IOException {

//        if (getConnectionType() != ConnectionType.TUNNEL_CONNECTION) {
//            throw new IOException("This is not a tunnel connection! Cannot send data");
//        }

        // touch the connection because it is alive and should not be purged
        timestampLastUsed = System.currentTimeMillis();
    }

    @Override
    public void sendResponse(final DatagramPacket datagramPacket) throws IOException {
        timestampLastUsed = System.currentTimeMillis();
        datagramSocket.send(datagramPacket);
    }

    @Override
    public void sendResponse(final KNXPacket knxPacket) throws IOException, SequenceCounterException {

        prepare();

//        // increment the connections sequence counter and set the incremented value into
//        // the knxPacket
//        sendSequenceCounter++;
//        knxPacket.getConnectionHeader().setSequenceCounter(sendSequenceCounter);
//        knxPacket.getConnectionHeader().setChannel((short) id);

        final InetSocketAddress inetSocketAddress = new InetSocketAddress(dataEndpoint.getIpAddressAsObject(),
                dataEndpoint.getPort());

//        LOG.info(">>>>>>>> " + sequenceCounter + " >>>>>>> " + this);

        sendResponse(knxPacket, inetSocketAddress);
    }

    /**
     * Convert a domain specific KNX Payload into a java.net.DatagramPacket and send
     * it out to the connected communication partner.
     *
     * <ol>
     * <li/>Construct an object array containing the KNXPacket which is then put
     * into the outward pipeline.
     * <li/>The outward pipeline converts the payload into a byte buffer
     * <li/>Send the byte buffer as payload via a java.net.DatagramPacket
     * </ol>
     *
     * @throws SequenceCounterException
     */
    @Override
    public void sendResponse(final KNXPacket knxPacket, final SocketAddress socketAddress)
            throws IOException, SequenceCounterException {

        prepare();

        // increment the connections sequence counter and set the incremented value into
        // the knxPacket
        sendSequenceCounter++;
        if (knxPacket.getConnectionHeader() != null) {
            knxPacket.getConnectionHeader().setSequenceCounter(sendSequenceCounter);
            knxPacket.getConnectionHeader().setChannel((short) id);
        }

        DatagramPacket datagramPacket;
        try {
            // domain specific payload data
            final Object[] objectArray = new Object[2];
            objectArray[0] = knxPacket;
            objectArray[1] = socketAddress;

            // pipeline serializes the payload and puts it into a java.net.DatagramPacket
            // ready for sending
            datagramPacket = (DatagramPacket) outputPipeline.execute(objectArray);
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw new IOException(e);
        }

        LOG.trace("Connection {} is sending packet over socketAddress {}", id, socketAddress);

        // send the datagramPacket over the socket
        datagramSocket.send(datagramPacket);
    }

    @Override
    public void sendAcknowledge(final KNXPacket knxPacket, final SocketAddress socketAddress) throws IOException {

        prepare();

        // an acknowledge uses the sequence number of the incoming message
        // So do not change the knxPacket
//        sendSequenceCounter++;
//        knxPacket.getConnectionHeader().setSequenceCounter(sendSequenceCounter);
//        knxPacket.getConnectionHeader().setChannel((short) id);

        DatagramPacket datagramPacket;
        try {
            // domain specific payload data
            final Object[] objectArray = new Object[2];
            objectArray[0] = knxPacket;
            objectArray[1] = socketAddress;

            // pipeline serializes the payload and puts it into a java.net.DatagramPacket
            // ready for sending
            datagramPacket = (DatagramPacket) outputPipeline.execute(objectArray);
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw new IOException(e);
        }

        LOG.trace("Connection {} is sending packet over socketAddress {}", id, socketAddress);

        // send the datagramPacket over the socket
        datagramSocket.send(datagramPacket);
    }

    @Override
    public void sendRequest(final KNXPacket knxPacket) throws IOException, CommunicationException {

        prepare();

        if (knxPacket.getConnectionHeader() != null) {
            // increment the connections sequence counter and set the incremented value into
            // the knxPacket
            sendSequenceCounter++;
            knxPacket.getConnectionHeader().setSequenceCounter(sendSequenceCounter);
            knxPacket.getConnectionHeader().setChannel((short) id);
        }

//        if (dataEndpoint == null) {
//            LOG.warn("DataEndpoint is null!");
//            return;
//        }
//
//        final InetSocketAddress destinationInetSocketAddress = new InetSocketAddress(
//                dataEndpoint.getIpAddressAsObject(), dataEndpoint.getPort());

//        final InetAddress inetAddress = datagramSocket.getInetAddress();
//        final int port = datagramSocket.getPort();
//        LOG.info("Address: '{}' Port: '{}'", inetAddress, port);

//        final InetAddress inetAddress = controlEndpoint.getIpAddressAsObject();
//        final int port = controlEndpoint.getPort();
//        LOG.info("Address: '{}' Port: '{}'", inetAddress, port);

//        final InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, port);
//        DatagramPacket datagramPacket;
//        try {
//            final Object[] objectArray = new Object[2];
//            objectArray[0] = knxPacket;
//            objectArray[1] = inetSocketAddress;
//
//            datagramPacket = (DatagramPacket) outputPipeline.execute(objectArray);
//        } catch (final Exception e) {
//            LOG.error(e.getMessage(), e);
//            throw new IOException(e);
//        }

//        LOG.trace("Connection {} is sending packet over socketAddress {}", id, inetSocketAddress);

        final InetSocketAddress destinationInetSocketAddress = new InetSocketAddress(
                InetAddress.getByName("192.168.2.2"), 3671);

        final byte[] bytes = knxPacket.getBytes();
        final DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, destinationInetSocketAddress);

        datagramSocket.send(datagramPacket);
    }

    @Override
    public void sendResponse(final DatagramPacket datagramPacket, final InetAddress inetAddress, final int port)
            throws IOException {
        prepare();
        datagramSocket.send(datagramPacket);
    }

    /**
     * Sends Data to the data HPAI endpoint of the communication partner. Increments
     * the own sendSequenceCounter (!= receiveSequenceCounter) and uses that
     * sequence counter as the sequence counter for the packet.
     *
     * @throws CommunicationException
     */
    @Override
    public void sendData(final KNXPacket knxPacket) throws IOException, CommunicationException {

        prepare();

        // increment the connections sequence counter and set the incremented value into
        // the knxPacket
        sendSequenceCounter++;
        knxPacket.getConnectionHeader().setSequenceCounter(sendSequenceCounter);
        knxPacket.getConnectionHeader().setChannel((short) id);

        if (dataEndpoint == null) {
            LOG.warn("DataEndpoint is null!");
            return;
        }

        final InetSocketAddress destinationInetSocketAddress = new InetSocketAddress(
                dataEndpoint.getIpAddressAsObject(), dataEndpoint.getPort());

//        // increment the connections sequence counter and set the incremented value into
//        // the knxPacket
//        sendSequenceCounter++;
//        knxPacket.getConnectionHeader().setSequenceCounter(sendSequenceCounter);
//        knxPacket.getConnectionHeader().setChannel((short) id);

        // use the pipeline to retrieve a DatagramPacket from a KNX packet
        final DatagramPacket datagramPacket = createDatagramPacket(knxPacket, destinationInetSocketAddress);

        LOG.trace("Connection {} is sending packet to socketAddress {}", id, destinationInetSocketAddress);
        LOG.trace("SendSequenceCounter: " + sendSequenceCounter + ") Sending Data to "
                + destinationInetSocketAddress.getHostString() + ":" + destinationInetSocketAddress.getPort());

        datagramSocket.send(datagramPacket);
    }

    private DatagramPacket createDatagramPacket(final KNXPacket knxPacket, final InetSocketAddress inetSocketAddress)
            throws IOException {

        DatagramPacket datagramPacket;
        try {
            // parameter 0 is the KNX packet to convert into a datagram packet
            // parameter 1 is the destination IP address to send the datagram packet to
            final Object[] objectArray = new Object[2];
            objectArray[0] = knxPacket;
            objectArray[1] = inetSocketAddress;

            datagramPacket = (DatagramPacket) outputPipeline.execute(objectArray);
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw new IOException(e);
        }

        return datagramPacket;
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
    public int getSendSequenceCounter() {
        return sendSequenceCounter;
    }

    @Override
    public void setSendSequenceCounter(final int sendSequenceCounter) {
        this.sendSequenceCounter = sendSequenceCounter;
    }

    @Override
    public int getReceiveSequenceCounter() {
        return receiveSequenceCounter;
    }

    @Override
    public void setReceiveSequenceCounter(final int receiveSequenceCounter) {
        LOG.trace("setReceiveSequenceCounter() newValue:" + receiveSequenceCounter);
        this.receiveSequenceCounter = receiveSequenceCounter;
    }

    @Override
    public String toString() {

        final long idleInSeconds = (System.currentTimeMillis() - timestampLastUsed) / 1000;

        return "DefaultConnection [id: #" + id + " #" + Utils.integerToString(id) + ", sendSequenceCounter="
                + sendSequenceCounter + ", receiveSequenceCounter=" + receiveSequenceCounter + ", connectionType="
                + connectionType + " Idle for: " + idleInSeconds + " seconds ]";
    }

    @Override
    public long getTimestampLastUsed() {
        return timestampLastUsed;
    }

    @Override
    public void setTimestampLastUsed(final long timestampLastUsed) {
        this.timestampLastUsed = timestampLastUsed;
    }

}
