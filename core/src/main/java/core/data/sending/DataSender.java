package core.data.sending;

import core.communication.Connection;

public interface DataSender {

	void send(Connection connection);

	Object deserializeByFormat();

}
