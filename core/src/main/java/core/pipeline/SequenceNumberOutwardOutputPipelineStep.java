package core.pipeline;

import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import common.utils.Utils;
import core.communication.Connection;
import core.packets.KNXPacket;

public class SequenceNumberOutwardOutputPipelineStep extends OutwardOutputPipelineStep {

    private static final Logger LOG = LogManager.getLogger(SequenceNumberOutwardOutputPipelineStep.class);

    @Override
    public Object execute(final Object source) throws Exception {

        if (source == null) {
            return null;
        }

        final Object[] objectArray = (Object[]) source;
        final KNXPacket knxPacket = (KNXPacket) objectArray[0];

        // ignore packets
        final String serviceIdentifier = knxPacket.getHeader().getServiceIdentifier().name();
        if (getIgnorePackets().contains(serviceIdentifier.toLowerCase(Locale.getDefault()))) {
            return source;
        }

        final String currentTimeAsString = Utils.retrieveCurrentTimeAsString();

        final Connection connection = knxPacket.getConnection();
        LOG.info(currentTimeAsString + " CONNECTION: " + connection);

        if (knxPacket.getConnectionHeader() != null) {
            final int sequenceCounter = knxPacket.getConnectionHeader().getSequenceCounter();
            LOG.info(currentTimeAsString + " PACKET SEQUENCE COUNTER: " + sequenceCounter);
        }

        return source;
    }

}
