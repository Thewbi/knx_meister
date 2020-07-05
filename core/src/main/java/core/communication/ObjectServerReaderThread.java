package core.communication;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.pipeline.Pipeline;
import api.project.KNXProject;
import common.data.conversion.BitDataSerializer;
import common.data.conversion.DataConversion;
import common.data.conversion.Float16DataSerializer;
import common.utils.NetworkUtils;
import object_server.requests.processors.GetDatapointDescriptionRequestProcessor;
import object_server.requests.processors.GetDatapointValueRequestProcessor;
import object_server.requests.processors.GetServerItemRequestProcessor;
import object_server.requests.processors.SetDatapointValueRequestProcessor;

public class ObjectServerReaderThread implements Runnable {

	private static final Logger LOG = LogManager.getLogger(ObjectServerReaderThread.class);

	private final boolean running = true;

	private final int bindPort;

	private Pipeline<Object, Object> inputPipeline;

	private KNXProject knxProject;

	public ObjectServerReaderThread(final int bindPort) {
		this.bindPort = bindPort;
	}

	@Override
	public void run() {

		ServerSocket serverSocket = null;

		try {

			final InetSocketAddress inetSocketAddress = new InetSocketAddress(NetworkUtils.retrieveLocalIP(), bindPort);

			serverSocket = new ServerSocket();
			serverSocket.bind(inetSocketAddress);

			while (running) {

				// blocking call
				final Socket clientSocket = serverSocket.accept();

				final ClientRunnable clientRunnable = new ClientRunnable(clientSocket);
				clientRunnable.setInputPipeline(inputPipeline);

				final GetServerItemRequestProcessor getServerItemRequestProcessor = new GetServerItemRequestProcessor();
				getServerItemRequestProcessor.setKnxProject(knxProject);
				clientRunnable.getRequestProcessors().add(getServerItemRequestProcessor);

				final GetDatapointDescriptionRequestProcessor getDatapointDescriptionRequestProcessor = new GetDatapointDescriptionRequestProcessor();
				getDatapointDescriptionRequestProcessor.setKnxProject(knxProject);
				clientRunnable.getRequestProcessors().add(getDatapointDescriptionRequestProcessor);

				final GetDatapointValueRequestProcessor getDatapointValueRequestProcessor = new GetDatapointValueRequestProcessor();
				getDatapointValueRequestProcessor.setKnxProject(knxProject);
				clientRunnable.getRequestProcessors().add(getDatapointValueRequestProcessor);

				final SetDatapointValueRequestProcessor setDatapointValueRequestProcessor = new SetDatapointValueRequestProcessor();
				setDatapointValueRequestProcessor.getDataSerializerMap().put(DataConversion.FLOAT16,
						new Float16DataSerializer());
				setDatapointValueRequestProcessor.getDataSerializerMap().put(DataConversion.BIT,
						new BitDataSerializer());
				setDatapointValueRequestProcessor.setKnxProject(knxProject);
				clientRunnable.getRequestProcessors().add(setDatapointValueRequestProcessor);

				final Thread clientThread = new Thread(clientRunnable);
				clientThread.start();
			}

		} catch (final UnknownHostException e) {
			LOG.error(e.getMessage(), e);
		} catch (final SocketException e) {
			LOG.error(e.getMessage(), e);
		} catch (final IOException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (final IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
	}

	public void setInputPipeline(final Pipeline<Object, Object> inputPipeline) {
		this.inputPipeline = inputPipeline;
	}

	public void setKnxProject(final KNXProject knxProject) {
		this.knxProject = knxProject;
	}

}
