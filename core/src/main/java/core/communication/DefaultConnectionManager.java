package core.communication;

import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.pipeline.Pipeline;
import core.packets.ConnectionType;
import core.packets.KNXPacket;

public class DefaultConnectionManager implements ConnectionManager {

    private static final Logger LOG = LogManager.getLogger(DefaultConnectionManager.class);

    private final Map<Integer, Connection> connectionMap = new ConcurrentHashMap<>();

    private final AtomicInteger connectionIdAtomicInteger = new AtomicInteger();

    private Pipeline<Object, Object> outputPipeline;

    /**
     * ctor
     */
    public DefaultConnectionManager() {
        startDumpConnectionsThread(OUTPUT_PERIOD_IN_MILLIS);
        startPurgeConnectionsThread(PURGE_PERIOD_IN_MILLIS);
    }

    @SuppressWarnings("unlikely-arg-type")
    private void startPurgeConnectionsThread(final int periodInMillis) {

        // output connections
        final Runnable runnable = () -> {

            while (true) {

                try {
                    Thread.sleep(periodInMillis);
                } catch (final InterruptedException e) {
                    // ignored
                }

                if (MapUtils.isEmpty(connectionMap)) {
                    LOG.trace("No connections yet!");
                    continue;
                }

                final long currentTimeMillis = System.currentTimeMillis();

                final List<Connection> removeList = new ArrayList<>();

                // @formatter:off

                connectionMap.values()
                    .stream()
                    .filter(c -> {
                        final long idle = currentTimeMillis - c.getTimestampLastUsed();
                        return idle > CONNECTION_TIMEOUT;
                    })
                    .forEach(c -> removeList.add(c));

                removeList.stream().forEach(c -> {
                    LOG.info("Removing connection '{}' because is not used for {} ms.", c, CONNECTION_TIMEOUT);
                    connectionMap.remove(c.getId());
                });

                // @formatter:on
            }

        };
        final Thread thread = new Thread(runnable);
        thread.start();
    }

    private void startDumpConnectionsThread(final int periodInMillis) {

        // output connections
        final Runnable runnable = () -> {

            while (true) {

                try {
                    Thread.sleep(periodInMillis);
                } catch (final InterruptedException e) {
                    // ignored
                }

                if (MapUtils.isEmpty(connectionMap)) {
                    LOG.trace("No connections yet!");
                    continue;
                }

                LOG.info("\n");
                LOG.info("-------------------------------- CONNECTIONS --------------------------------");

                connectionMap.values().stream().forEach(c -> LOG.info(c.toString()));

                LOG.info("-----------------------------------------------------------------------------");
            }

        };
        final Thread thread = new Thread(runnable);
        thread.start();
    }

    /**
     * Connection via the knxPacket.getCommunicationChannelId() property.
     */
    @Override
    public Connection retrieveConnection(final KNXPacket knxPacket, final DatagramSocket datagramSocket) {

        final int communicationChannelId = knxPacket.getCommunicationChannelId();

        // if the packet has a specific communicationChannelId, try to find that
        // connection
        if (communicationChannelId > 0) {
            return connectionMap.get(communicationChannelId);
        }

        // if the packet has no communicationChannelId, return the basic 0 connection,
        // if that connection exists already
        if (connectionMap.containsKey(0)) {
            return connectionMap.get(0);
        }

        // create a new connection and return it. Because connectionIdAtomicInteger
        // starts with 0, this will create the basic 0 connection
        return createNewConnection(datagramSocket, connectionIdAtomicInteger.getAndIncrement(), ConnectionType.UNKNOWN);
    }

    @Override
    public Connection retrieveConnection(final int communicationChannelId) {
        return connectionMap.get(communicationChannelId);
    }

    @Override
    public Optional<Connection> getLiveTunnelingConnection() {

        if (MapUtils.isEmpty(connectionMap)) {
            return null;
        }

        // @formatter:off

        final Optional<Connection> mostRecentlyUsedOptional = connectionMap.values()
                .stream()
                .filter(c -> c.getConnectionType() == ConnectionType.TUNNEL_CONNECTION)
                .max(Comparator.comparing(Connection::getTimestampLastUsed));

        // @formatter:on

        return mostRecentlyUsedOptional;
    }

    @Override
    public Connection createNewConnection(final DatagramSocket datagramSocket, final ConnectionType connectionType) {
        return createNewConnection(datagramSocket, connectionIdAtomicInteger.getAndIncrement(), connectionType);
    }

    @Override
    public Connection createNewConnection(final DatagramSocket datagramSocket, final int id,
            final ConnectionType connectionType) {

        // when the application is closed and restarted, the connection map is empty and
        // the connectionIdAtomicInteger is initialized to 0. Meanwhile the
        // communication partner might still keep the connections alive. That means a
        // restarted application might be confronted with a connection id larger then
        // the connectionidAtomicInteger value.
        //
        // In this case, increment the connectionidAtomicInteger until it has caught up.
        while (connectionIdAtomicInteger.get() <= id) {
            connectionIdAtomicInteger.getAndIncrement();
        }

        LOG.info("Creating new connection " + connectionType + " with id {}", id);

        final DefaultConnection connection = new DefaultConnection();
        connection.setId(id);
        connection.setDatagramSocket(datagramSocket);
        connection.setConnectionType(connectionType);
        connection.setOutputPipeline(outputPipeline);

        connectionMap.put(id, connection);

        LOG.info(connection.toString());

        return connection;
    }

    @Override
    public void closeConnection(final int id) {

        LOG.info("Removing connection with id {}", id);

        if (!connectionMap.containsKey(id)) {
            return;
        }
        connectionMap.remove(id);
    }

    @Override
    public void setOutputPipeline(final Pipeline<Object, Object> outputPipeline) {
        this.outputPipeline = outputPipeline;
    }

}
