package core.pipeline;

import java.net.DatagramPacket;
import java.net.SocketAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.pipeline.PipelineStep;
import common.utils.Utils;
import core.packets.KNXPacket;

/**
 * Input: KNXPacket and SocketAddress. <br />
 * <br />
 *
 * Output: A java.net.DatagramPacket that is addressed to the SocketAddress and
 * contains the input KNXPacket as a byte array in it's payload.
 */
public class OutwardConverterPipelineStep implements PipelineStep<Object, Object> {

    private static final Logger LOG = LogManager.getLogger(OutwardConverterPipelineStep.class);

    @Override
    public Object execute(final Object source) throws Exception {

        final Object[] objectArray = (Object[]) source;

        final KNXPacket knxPacket = (KNXPacket) objectArray[0];
        final SocketAddress socketAddress = (SocketAddress) objectArray[1];

        final byte[] bytes = knxPacket.getBytes();

        // DEBUG - output the bytes
        LOG.trace(Utils.integerToStringNoPrefix(bytes));

        return new DatagramPacket(bytes, bytes.length, socketAddress);
    }

}
