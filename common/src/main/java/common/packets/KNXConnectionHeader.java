package common.packets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.exception.SequenceCounterException;

public class KNXConnectionHeader {

    @SuppressWarnings("unused")
    private static final Logger LOG = LogManager.getLogger(KNXConnectionHeader.class);

    private short length = 4;

    private short channel;

    private int sequenceCounter = -1;

    private int reserved;

    public KNXConnectionHeader() {
    }

    public KNXConnectionHeader(final KNXConnectionHeader connectionHeader) {
        length = connectionHeader.length;
        channel = connectionHeader.channel;
        sequenceCounter = connectionHeader.sequenceCounter;
        reserved = connectionHeader.reserved;
    }

    public byte[] getBytes() {

        final byte[] bytes = new byte[4];
        bytes[0] = (byte) length;
        bytes[1] = (byte) channel;
        bytes[2] = (byte) sequenceCounter;
        bytes[3] = (byte) reserved;

        return bytes;
    }

    public void writeBytesIntoBuffer(final byte[] buffer, final int offset) {

        int index = 0;

        buffer[offset + index] = (byte) length;
        index++;

        buffer[offset + index] = (byte) channel;
        index++;

        buffer[offset + index] = (byte) sequenceCounter;
        index++;

        buffer[offset + index] = (byte) reserved;
        index++;
    }

    public void fromBytes(final byte[] source, final int startIndex) {

        // length
        length = (short) ((source[startIndex]) & 0xFF);

        // channel id
        channel = (short) ((source[startIndex + 1]) & 0xFF);

        // sequenceCounter
        sequenceCounter = (source[startIndex + 2]) & 0xFF;

        // reserved
        reserved = (source[startIndex + 3]) & 0xFF;
    }

    public short getLength() {
        return length;
    }

    public void setLength(final short length) {
        this.length = length;
    }

    public short getChannel() {
        return channel;
    }

    public void setChannel(final short channel) {
        this.channel = channel;
    }

    public int getSequenceCounter() {
        return sequenceCounter;
    }

    public void setSequenceCounter(final int sequenceCounter) throws SequenceCounterException {
//        final String msg = String.format("setSequenceCounter() channel:%d oldValue:%d newValue:%d", channel,
//                this.sequenceCounter, sequenceCounter);
//        LOG.info(msg);
//        if (sequenceCounter <= this.sequenceCounter) {
//            throw new SequenceCounterException("SequenceCounter has to increase but was set to a lower value! " + msg);
//        }
        this.sequenceCounter = sequenceCounter;
    }

    public int getReserved() {
        return reserved;
    }

    public void setReserved(final int reserved) {
        this.reserved = reserved;
    }

    @Override
    public String toString() {
        return "KNXConnectionHeader [channel=" + channel + ", sequenceCounter=" + sequenceCounter + ", reserved="
                + reserved + "]";
    }

}
