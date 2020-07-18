package core.communication;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.data.serializer.DataSerializer;
import api.pipeline.Pipeline;
import api.project.KNXProject;
import common.utils.NetworkUtils;
import object_server.conversion.ComObjectValueConverter;
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

	private Map<String, DataSerializer<Object>> dataSerializerMap;

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

				final ComObjectValueConverter comObjectValueConverter = new ComObjectValueConverter();
				comObjectValueConverter.setKnxProject(knxProject);
				comObjectValueConverter.setDataSerializerMap(dataSerializerMap);

				final GetDatapointValueRequestProcessor getDatapointValueRequestProcessor = new GetDatapointValueRequestProcessor();
				getDatapointValueRequestProcessor.setKnxProject(knxProject);
				getDatapointValueRequestProcessor.setObjectServerValueConverter(comObjectValueConverter);
				clientRunnable.getRequestProcessors().add(getDatapointValueRequestProcessor);

				final SetDatapointValueRequestProcessor setDatapointValueRequestProcessor = new SetDatapointValueRequestProcessor();
				setDatapointValueRequestProcessor.setKnxProject(knxProject);
				setDatapointValueRequestProcessor.setDataSerializerMap(dataSerializerMap);
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

	public Map<String, DataSerializer<Object>> getDataSerializerMap() {
		return dataSerializerMap;
	}

	public void setDataSerializerMap(final Map<String, DataSerializer<Object>> dataSerializerMap) {
		this.dataSerializerMap = dataSerializerMap;
	}

}
