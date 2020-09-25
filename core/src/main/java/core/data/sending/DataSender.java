package core.data.sending;

import api.device.Device;
import api.exception.CommunicationException;
import core.communication.Connection;

public interface DataSender {

    static final int HOP_COUNT_6 = 0xE0;

    static final int PRIORITY_LOW = 0xBC;

    static final int PRIORITY_NORMAL = 0xB4;

    static final int PRIORITY_SYSTEM = 0xB0;

    void send(Device device, Connection connection, String physicalAddress, final String groupAddress, int dataPointId,
            Object value) throws CommunicationException;

    Object deserializeByFormat();

}
