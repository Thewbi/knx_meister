package object_server.api.service;

import java.io.IOException;
import java.net.UnknownHostException;

public interface ObjectServerConnectionService {

	void connect(String ip, int port) throws UnknownHostException, IOException;

	void connect(String ip, int port, String localIp, int localport) throws UnknownHostException, IOException;

	boolean isConnected();

	boolean isNotConnected();

	void disconnect() throws IOException;

	void send(byte[] bytes) throws IOException;

	int read(byte[] buffer) throws IOException;

}
