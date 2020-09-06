package core.data.sending;

import core.communication.Connection;

public interface DataSender {

    static final int HOP_COUNT_6 = 0xE0;

    static final int PRIORITY_LOW = 0xBC;

    static final int PRIORITY_NORMAL = 0xB4;

    static final int PRIORITY_SYSTEM = 0xB0;

    void send(Connection connection, String physicalAddress, final String groupAddress, int dataPointId, Object value,
            int deviceIndex);

    Object deserializeByFormat();

}
