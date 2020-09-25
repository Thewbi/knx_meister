package core.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.configuration.ConfigurationManager;
import api.exception.CommunicationException;
import api.pipeline.Pipeline;
import common.utils.NetworkUtils;
import core.packets.KNXPacket;

public class MulticastListenerReaderThread implements Runnable, DatagramPacketCallback {

    private static final boolean BIND_TO_IP_AND_PORT = false;

    private static final Logger LOG = LogManager.getLogger(MulticastListenerReaderThread.class);

    private Pipeline<Object, Object> inputPipeline;

    private MulticastSocket multicastSocket;

    private ConfigurationManager configurationManager;

    private boolean running;

    /**
     * One of these callbacks receives the KNXPacket that went trough the input
     * pipeline.
     *
     * @param datagramPacketCallback
     */
    private final List<DatagramPacketCallback> datagramPacketCallbacks = new ArrayList<>();

//	/**
//	 * ctor
//	 *
//	 * @param bindPort
//	 */
//	public MulticastListenerReaderThread(final String localIP, final int bindPort) {
//		this.localIP = localIP;
//		this.bindPort = bindPort;
//	}

    @Override
    public void run() {

        LOG.info("Start MulticastListener thread!");

        running = true;

        try {
            runMultiCastListener(this);
        } catch (final IOException | CommunicationException e) {
            LOG.error(e.getMessage(), e);
        }

        LOG.info("Reader MulticastListener end!");
    }

    /**
     * 8.6.3.1 General listener for UDP multicast on the standard KNX device routing
     * multicast address 224.0.23.12:3671
     *
     * @param datagramPacketCallback
     * @throws IOException
     * @throws SocketException
     * @throws UnknownHostException
     * @throws CommunicationException
     */
    private void runMultiCastListener(final DatagramPacketCallback datagramPacketCallback)
            throws IOException, SocketException, UnknownHostException, CommunicationException {

        final String localIP = configurationManager.getPropertyAsString(ConfigurationManager.LOCAL_IP_CONFIG_KEY);
        final int port = configurationManager.getPropertyAsInt(ConfigurationManager.PORT_CONFIG_KEY);

        LOG.info("Binding " + getClass().getName()
                + (BIND_TO_IP_AND_PORT ? (" to IP and Port (" + localIP + ":" + port + ")")
                        : (" to port (" + port + ")")));

        if (BIND_TO_IP_AND_PORT) {

            // for networked environments
            final InetSocketAddress inetSocketAddress = new InetSocketAddress(localIP, port);
            multicastSocket = new MulticastSocket(inetSocketAddress);
            LOG.info("Multicast listener on " + localIP + ":" + port + " started.");

        } else {

            // local environment
            multicastSocket = new MulticastSocket(port);
            LOG.info("Multicast listener on " + NetworkUtils.KNX_MULTICAST_IP + ":" + port + " started.");

        }

        multicastSocket.setReuseAddress(true);

        final InetAddress inetAddress = InetAddress.getByName(NetworkUtils.KNX_MULTICAST_IP);
        multicastSocket.joinGroup(inetAddress);

        while (running) {

            final byte[] buf = new byte[1024];
            final DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);

            LOG.trace("MultiCastListener accepting ...");

            // blocking call
            multicastSocket.receive(datagramPacket);

            LOG.trace("MultiCastListener received packet ...");

            // use the pipeline to convert the input from the socket to a KNXPacket that the
            // system can use
            KNXPacket knxPacket = null;
            try {

                Object[] data = new Object[2];
                data[0] = multicastSocket;
                data[1] = datagramPacket;

                data = (Object[]) inputPipeline.execute(data);
                if (data == null) {
                    continue;
                }

                knxPacket = (KNXPacket) data[1];
                if (knxPacket == null) {
                    continue;
                }

            } catch (final Exception e) {
                LOG.error(e.getMessage(), e);
                throw new IOException(e);
            }

            // retrieve the connection
            if (knxPacket.getConnection() == null) {

                final int communicationChannelId = knxPacket.getCommunicationChannelId();
                LOG.warn("Connection with communicationChannelId = {} is not known! No response is sent!",
                        communicationChannelId);

            } else {

                datagramPacketCallback.knxPacket(knxPacket.getConnection(), multicastSocket, datagramPacket, knxPacket,
                        "Multicast");

            }
        }
    }

    public List<DatagramPacketCallback> getDatagramPacketCallbacks() {
        return datagramPacketCallbacks;
    }

    public void setInputPipeline(final Pipeline<Object, Object> inputPipeline) {
        this.inputPipeline = inputPipeline;
    }

    @Override
    public void datagramPacket(final Connection connection, final DatagramSocket socket,
            final DatagramPacket datagramPacket, final String label) throws UnknownHostException, IOException {

        if (CollectionUtils.isEmpty(datagramPacketCallbacks)) {
            throw new RuntimeException("No listeners registered! System is malconfigured!");
        }

        for (final DatagramPacketCallback datagramPacketCallback : datagramPacketCallbacks) {

            if (datagramPacketCallback.accepts(datagramPacket)) {
                datagramPacketCallback.datagramPacket(connection, socket, datagramPacket, label);
                return;
            }
        }

        throw new RuntimeException("No listener accepts the datagram packet" + datagramPacket);
    }

    @Override
    public void knxPacket(final Connection connection, final DatagramSocket socket, final DatagramPacket datagramPacket,
            final KNXPacket knxPacket, final String label)
            throws UnknownHostException, IOException, CommunicationException {

        boolean packetAcceptedAtLeastOnce = false;

        if (CollectionUtils.isEmpty(datagramPacketCallbacks)) {
            throw new RuntimeException("No listeners registered! System is malconfigured!");
        }

        // find a callback to handle this packet
        for (final DatagramPacketCallback datagramPacketCallback : datagramPacketCallbacks) {

            if (datagramPacketCallback.accepts(knxPacket)) {

                LOG.info("ServiceIdentifier: " + knxPacket.getHeader().getServiceIdentifier() + " is accepted by "
                        + datagramPacketCallback);

                packetAcceptedAtLeastOnce = true;
                datagramPacketCallback.knxPacket(connection, socket, datagramPacket, knxPacket, label);
            }
        }

        if (!packetAcceptedAtLeastOnce) {
            throw new RuntimeException("No listener accepts the KNX packet" + knxPacket);
        }
    }

    @Override
    public boolean accepts(final DatagramPacket datagramPacket) {
        return true;
    }

    @Override
    public boolean accepts(final KNXPacket knxPacket) {
        return true;
    }

    public MulticastSocket getMulticastSocket() {
        return multicastSocket;
    }

//	public String getLocalIP() {
//		return localIP;
//	}

    public void setConfigurationManager(final ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

}
