package core.data.sending;

import core.communication.Connection;

public interface DataSender {

	public static final String BIT = "Bit";

	public static final String FLOAT16 = "Float16";

	public void send(Connection connection);

}
