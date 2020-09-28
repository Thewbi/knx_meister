package api.connection;

public class ConnectionDto {

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

    private String connectionType;

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public int getSendSequenceCounter() {
        return sendSequenceCounter;
    }

    public void setSendSequenceCounter(final int sendSequenceCounter) {
        this.sendSequenceCounter = sendSequenceCounter;
    }

    public int getReceiveSequenceCounter() {
        return receiveSequenceCounter;
    }

    public void setReceiveSequenceCounter(final int receiveSequenceCounter) {
        this.receiveSequenceCounter = receiveSequenceCounter;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(final String connectionType) {
        this.connectionType = connectionType;
    }

}
