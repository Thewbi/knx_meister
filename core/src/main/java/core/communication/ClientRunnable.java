package core.communication;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.pipeline.Pipeline;
import common.utils.Utils;
import object_server.requests.BaseRequest;
import object_server.requests.BaseResponse;
import object_server.requests.RequestProcessor;

public class ClientRunnable implements Runnable {

	private static final Logger LOG = LogManager.getLogger(ClientRunnable.class);

	private static final int BUFFER_SIZE = 1024;

	private final Socket socket;

	private Pipeline<Object, Object> inputPipeline;

	private final List<RequestProcessor> requestProcessors = new ArrayList<>();

	public ClientRunnable(final Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {

		try {

			final byte[] buffer = new byte[BUFFER_SIZE];

			while (true) {

				// clear buffer
				Arrays.fill(buffer, (byte) 0);

				final int readBytes = socket.getInputStream().read(buffer, 0, BUFFER_SIZE);
				if (readBytes < 0) {

					// premature EOF
					break;
				}

				// put the buffer into the pipeline
				final BaseRequest baseRequest = (BaseRequest) inputPipeline.execute(buffer);

				LOG.info(baseRequest);

				BaseResponse baseResponse = null;

				for (final RequestProcessor requestProcessor : requestProcessors) {
					if (requestProcessor.accept(baseRequest)) {
						baseResponse = requestProcessor.process(baseRequest);
						break;
					}
				}

				final byte[] bytes = baseResponse.getBytes();

				final String integerToStringNoPrefix = Utils.integerToStringNoPrefix(bytes);

				LOG.info("Sending Response: " + integerToStringNoPrefix);

				final DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
				dataOutputStream.write(bytes);
				dataOutputStream.flush();
			}

		} catch (final IOException e) {
			LOG.error(e.getMessage(), e);
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public void setInputPipeline(final Pipeline<Object, Object> inputPipeline) {
		this.inputPipeline = inputPipeline;
	}

	public List<RequestProcessor> getRequestProcessors() {
		return requestProcessors;
	}

}
