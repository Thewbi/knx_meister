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
import common.utils.NetworkUtils;
import object_server.requests.GetServerItemRequestProcessor;

public class ObjectServerReaderThread implements Runnable {

	private static final Logger LOG = LogManager.getLogger(ObjectServerReaderThread.class);

	private final boolean running = true;

	private final int bindPort;

	private Pipeline<Object, Object> inputPipeline;

	public ObjectServerReaderThread(final int bindPort) {
		this.bindPort = bindPort;
	}

	@Override
	public void run() {

		try {

			final InetSocketAddress inetSocketAddress = new InetSocketAddress(NetworkUtils.retrieveLocalIP(), bindPort);

			final ServerSocket serverSocket = new ServerSocket();
			serverSocket.bind(inetSocketAddress);

			while (running) {

				// blocking call
				final Socket clientSocket = serverSocket.accept();

				final ClientRunnable clientRunnable = new ClientRunnable(clientSocket);
				clientRunnable.setInputPipeline(inputPipeline);
				clientRunnable.getRequestProcessors().add(new GetServerItemRequestProcessor());

				final Thread clientThread = new Thread(clientRunnable);
				clientThread.start();
			}

		} catch (final UnknownHostException e) {
			LOG.error(e.getMessage(), e);
		} catch (final SocketException e) {
			LOG.error(e.getMessage(), e);
		} catch (final IOException e) {
			LOG.error(e.getMessage(), e);
		}

	}

	public void setInputPipeline(final Pipeline<Object, Object> inputPipeline) {
		this.inputPipeline = inputPipeline;
	}

}
