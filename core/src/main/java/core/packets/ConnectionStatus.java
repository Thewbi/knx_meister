package core.packets;

/**
 * 7.8.2 CONNECT_RESPONSE - Table 4
 */
public enum ConnectionStatus {

    /** The connection is established successfully. */
    E_NO_ERROR(0x00),

    /** 1 The requested host protocol is not supported by the KNXnet/IP device. */
    E_HOST_PROTOCOL_TYPE(0x01),

    /** The requested protocol version is not supported by the KNXnet/IP device. */
    E_VERSION_NOT_SUPPORTED(0x02),

    /** The received sequence number is out of order. */
    E_SEQUENCE_NUMBER(0x04),

    /**
     * The KNXnet/IP Server device cannot find an active data connection with the
     * specified ID.
     */
    E_CONNECTION_ID(0x21),

    /**
     * The KNXnet/IP Server device detects an error concerning the data connection
     * with the specified ID.
     */
    E_DATA_CONNECTION(0x26),

    /**
     * The KNXnet/IP Server device detects an error concerning the KNX connection
     * with the specified ID.
     */
    E_KNX_CONNECTION(0x27),

    /**
     * The requested connection type is not supported by the KNXnet/IP Server
     * device.
     */
    E_CONNECTION_TYPE(0x22),

    /**
     * One or more requested connection options are not supported by the KNXnet/IP
     * Server device.
     */
    E_CONNECTION_OPTION(0x23),

    /**
     * The KNXnet/IP Server device cannot accept the new data connection because its
     * maximum amount of concurrent connections is already occupied.
     */
    E_NO_MORE_CONNECTIONS(0x24),

    /** No Value */
    UNSET(0x99);

    private final int id;

    ConnectionStatus(final int id) {
        this.id = id;
    }

    public static ConnectionStatus fromInt(final int id) {

        switch (id) {
        case 0x00:
            return E_NO_ERROR;

        case 0x22:
            return E_CONNECTION_TYPE;

        case 0x23:
            return E_CONNECTION_OPTION;

        case 0x24:
            return E_NO_MORE_CONNECTIONS;

        default:
            throw new RuntimeException("Unkown id " + id);
        }
    }

    public int getValue() {
        return id;
    }

}
