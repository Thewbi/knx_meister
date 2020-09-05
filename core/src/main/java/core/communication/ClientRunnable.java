package core.communication;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.exception.ObjectServerException;
import api.pipeline.Pipeline;
import common.packets.ServiceIdentifier;
import common.utils.Utils;
import object_server.requests.BaseRequest;
import object_server.requests.BaseResponse;
import object_server.requests.DatapointValueIndicationRequest;
import object_server.requests.RequestFactory;
import object_server.requests.processors.RequestProcessor;

/**
 * Object server protocol server handler for incoming messages.
 */
public class ClientRunnable implements Runnable {

	private static final Logger LOG = LogManager.getLogger(ClientRunnable.class);

	private static final int BUFFER_SIZE = 1024;

	private final Socket socket;

	private Pipeline<Object, Object> inputPipeline;

	// TODO: provide outputPipeline

	// move this into the outputPipeline
	private final RequestFactory requestFactory = new RequestFactory();

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

				// DEBUG
				String integerToStringNoPrefix = Utils.integerToStringNoPrefix(buffer);
				LOG.trace("Received: " + integerToStringNoPrefix);

				final int sequenceCounter = baseRequest.getKnxConnectionHeader().getSequenceCounter();

				BaseResponse baseResponse = null;

				boolean requestProcessorFound = false;
				RequestProcessor usedRequestProcessor = null;
				for (final RequestProcessor requestProcessor : requestProcessors) {

					if (requestProcessor.accept(baseRequest)) {

						LOG.trace("Using request processor for [" + sequenceCounter + "]: " + requestProcessor);

						usedRequestProcessor = requestProcessor;

						// convert request into response
						baseResponse = requestProcessor.process(baseRequest);

						requestProcessorFound = true;
						break;
					}
				}

				if (!requestProcessorFound) {
					throw new ObjectServerException(
							"No processor found for request \"" + baseRequest.getClass().getName() + "\"");
				}

				final byte[] bytes = baseResponse.getBytes();

				// DEBUG
				integerToStringNoPrefix = Utils.integerToStringNoPrefix(bytes);
				LOG.trace("Sending Response for [" + sequenceCounter + "] from "
						+ usedRequestProcessor.getClass().getName() + ": " + integerToStringNoPrefix);

				// send data
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

	public void sendWindowPositionRequest() {

		try {

			final DatapointValueIndicationRequest request = new DatapointValueIndicationRequest();

			// header
			request.getKnxHeader().setLength((byte) 0x06);
			request.getKnxHeader().setProtocolVersion((byte) 0x20);
			request.getKnxHeader().setServiceIdentifier(ServiceIdentifier.OBJECT_SERVER_REQUEST);
			// has to be computed automatically based on real payload
			request.getKnxHeader().setTotalLength(0);

			// connection header
			request.getKnxConnectionHeader().setLength((byte) 0x04);
			request.getKnxConnectionHeader().setChannel((byte) 0x00);
			request.getKnxConnectionHeader().setSequenceCounter((byte) 0x00);
			request.getKnxConnectionHeader().setReserved((byte) 0x00);

			// main service is already set and cannot be altered
			// set the subservice only
			request.setSubService(DatapointValueIndicationRequest.DATAPOINTVALUE_INDICATION_REQUEST_CODE);
			request.setStart(0x55);
			request.setMaxAmount(0x01);

			// 00 55 (Datapoint ID)
			// 1a (Datapoint state)
			// 01 (Datapoint length)
			// 40 (Datapoint value) 0x40 = 64d <==> 64/244 ~ 25%

//			request.setPayload(new byte[] { 0x00, (byte) 0x55, 0x1a, 0x01, 0x40 });
			request.setPayload(new byte[] { 0x00, (byte) 0x85, 0x1a, 0x01, 0x40 });

			final byte[] payload = request.getBytes();

			// DEBUG
			final String integerToStringNoPrefix = Utils.integerToStringNoPrefix(payload);
			LOG.trace("Sending DatapointValueIndicationRequest: " + integerToStringNoPrefix);

			final DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
			dataOutputStream.write(payload);
			dataOutputStream.flush();

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
