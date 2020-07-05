package object_server.service;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import object_server.api.service.ObjectServerConnectionService;

public class DefaultObjectServerConnectionService implements ObjectServerConnectionService {

	private Socket socket;

	private DataOutputStream dataOutputStream;

	@Override
	public void connect(final String ip, final int port) throws UnknownHostException, IOException {

		if (isConnected()) {
			disconnect();
		}
		socket = new Socket(ip, port);
		dataOutputStream = new DataOutputStream(socket.getOutputStream());
	}

	@Override
	public void connect(final String ip, final int port, final String localIp, final int localport)
			throws UnknownHostException, IOException {

		if (isConnected()) {
			disconnect();
		}

		final InetAddress localInetAddress = InetAddress.getByName(localIp);
		socket = new Socket(ip, port, localInetAddress, localport);
		dataOutputStream = new DataOutputStream(socket.getOutputStream());
	}

	@Override
	public boolean isConnected() {
		return socket != null && dataOutputStream != null;
	}

	@Override
	public boolean isNotConnected() {
		return socket == null || dataOutputStream == null;
	}

	@Override
	public void disconnect() throws IOException {

		// flush and close output stream
		try {
			dataOutputStream.flush();
			dataOutputStream.close();
		} catch (final Exception e) {
			// ignored
		} finally {
			dataOutputStream = null;
		}

		// close the socket
		try {
			socket.close();
		} catch (final Exception e) {
			// ignored
		} finally {
			socket = null;
		}
	}

	@Override
	public void send(final byte[] bytes) throws IOException {

		if (isNotConnected()) {
			throw new IllegalStateException("Not connected! Use connect() to establish a connection first!");
		}

		dataOutputStream.write(bytes);
		dataOutputStream.flush();
	}

	@Override
	public int read(final byte[] buffer) throws IOException {

		if (isNotConnected()) {
			throw new IllegalStateException("Not connected! Use connect() to establish a connection first!");
		}

		return socket.getInputStream().read(buffer);
	}

}
