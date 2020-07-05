package object_server.requests;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Test;

import common.utils.Utils;

public class GetServerItemRequestTest {

//	private static final String IP = "192.168.0.241";

	private static final String IP = "172.17.200.64";

	private static final int PORT = 12004;

	private static final int BUFFER_SIZE = 200;

	@Test
	public void testSendRequest() throws UnknownHostException, IOException {

		final GetServerItemRequest getServerItemRequest = new GetServerItemRequest();
		getServerItemRequest.setStart(0);
		getServerItemRequest.setMaxAmount(99);

		final Socket socket = new Socket(IP, PORT);
//		final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		final DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
		dataOutputStream.write(getServerItemRequest.getBytes());
		dataOutputStream.flush();

		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		final byte buffer[] = new byte[1024];

		final int bytesRead = socket.getInputStream().read(buffer);
//		for (int s; (s = socket.getInputStream().read(buffer)) != -1;) {
//			byteArrayOutputStream.write(buffer, 0, s);
//		}
//		final byte result[] = byteArrayOutputStream.toByteArray();

		System.out.println(Utils.integerToStringNoPrefix(buffer, 0, bytesRead));

//		final byte[] buffer = new char[BUFFER_SIZE];
//
//		final int bytesRead = readIntoBuffer(bufferedReader, buffer, buffer.length);
//		if (bytesRead > 0) {
//			System.out.println(bytesRead);
//			Utils.integerToStringNoPrefix(buffer);
//		}
	}

//	/**
//	 * Reads from the socket into a buffer. Blocking call. Blocks until data could
//	 * be read
//	 *
//	 * @param bufferedReader the read to read from
//	 * @param buffer         the buffer to read into
//	 * @param bufferSize     the buffer size
//	 * @return the amount of bytes read
//	 *
//	 * @throws IOException
//	 */
//	private int readIntoBuffer(final BufferedReader bufferedReader, final byte[] buffer, final int bufferSize)
//			throws IOException {
//		return bufferedReader.read(buffer, 0, BUFFER_SIZE);
//	}

	private String readString(final java.net.Socket socket) throws IOException {

		final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		final char[] buffer = new char[BUFFER_SIZE];

		// blocking call
		final int charactersRead = bufferedReader.read(buffer, 0, BUFFER_SIZE);
		final String nachricht = new String(buffer, 0, charactersRead);

		return nachricht;
	}

}
