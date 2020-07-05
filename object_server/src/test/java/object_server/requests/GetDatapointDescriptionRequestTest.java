package object_server.requests;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Test;

import common.utils.Utils;

public class GetDatapointDescriptionRequestTest {

	private static final int BUFFER_SIZE = 1024;

	@Test
	public void testSendRequest() throws UnknownHostException, IOException {

		int startDataPoint = 0;
		int maxNumberOfDataPoints = 0;

		startDataPoint = 0x56;
		maxNumberOfDataPoints = 1;
		sendRequest(startDataPoint, maxNumberOfDataPoints);

//		startDataPoint = 232;
//		maxNumberOfDataPoints = 1;
//		sendRequest(startDataPoint, maxNumberOfDataPoints);

//		startDataPoint = 1;
//		maxNumberOfDataPoints = 255;
//		sendRequest(startDataPoint, maxNumberOfDataPoints);
	}

	@Test
	public void testSeveralDatapoints() throws UnknownHostException, IOException {

		final int startDataPoint = 0;
		final int maxNumberOfDataPoints = 0;

		// the data points are starting from 1
		for (int i = 1; i < 99; i++) {
			sendRequest(i, maxNumberOfDataPoints);
			try {
				Thread.sleep(1000);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendRequest(final int startDataPoint, final int maxNumberOfDataPoints)
			throws UnknownHostException, IOException {

		System.out.println(
				"GetDatapointDescriptionRequest start: " + startDataPoint + " maxNumber: " + maxNumberOfDataPoints);

		final GetDatapointDescriptionRequest getDatapointDescriptionRequest = new GetDatapointDescriptionRequest();
		getDatapointDescriptionRequest.setStart(startDataPoint);
		getDatapointDescriptionRequest.setMaxAmount(maxNumberOfDataPoints);

		final String ip = "192.168.0.241";
		final int port = 12004;

		final Socket socket = new Socket(ip, port);

		final DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
		dataOutputStream.write(getDatapointDescriptionRequest.getBytes());
		dataOutputStream.flush();

		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		final byte buffer[] = new byte[BUFFER_SIZE];

		final int bytesRead = socket.getInputStream().read(buffer);

		System.out.println(startDataPoint + ") " + Utils.integerToStringNoPrefix(buffer, 0, bytesRead));
	}

}
