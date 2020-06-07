package main;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.api.pipeline.Pipeline;
import core.communication.ConnectionManager;
import core.communication.Controller;
import core.communication.DefaultConnectionManager;
import core.communication.MulticastListenerReaderThread;
import core.conversion.KNXPacketConverter;
import core.pipeline.DefaultPipeline;
import core.pipeline.InwardConverterPipelineStep;
import core.pipeline.InwardOutputPipelineStep;
import core.pipeline.IpFilterPipelineStep;
import core.pipeline.OutwardConverterPipelineStep;
import core.pipeline.OutwardOutputPipelineStep;

public class Main {

	private static final String POINT_TO_POINT_READER_IP_ADDRESS = "127.0.0.1";

	private static final Logger LOG = LogManager.getLogger("Main");

	public static void main(final String[] args) throws IOException {

		// https://github.com/apache/dubbo/issues/2423
		//
		// on a macbook, the JVM prioritizes IPv6 interfaces over
		// IPv4 interfaces. Force the JVM to use IPv4.
		System.setProperty("java.net.preferIPv4Stack", "true");

		final InetAddress inetAddress = InetAddress.getLocalHost();
		LOG.info("IP of my system is := " + inetAddress.getHostAddress());

		final OutwardOutputPipelineStep outwardOutputPipelineStep = new OutwardOutputPipelineStep();
		outwardOutputPipelineStep.setPrefix("MULTICAST");
		final OutwardConverterPipelineStep outwardConverterPipelineStep = new OutwardConverterPipelineStep();

		final Pipeline<Object, Object> outputPipeline = new DefaultPipeline();
//		outputPipeline.addStep(outwardOutputPipelineStep);
		outputPipeline.addStep(outwardConverterPipelineStep);

		final ConnectionManager connectionManager = new DefaultConnectionManager();
		connectionManager.setOutputPipeline(outputPipeline);

		final Controller controller = createKNXController();
		controller.setConnectionManager(connectionManager);

//		// reader for point to point connections
//		final PointToPointReaderThread readerThread = new PointToPointReaderThread(POINT_TO_POINT_READER_IP_ADDRESS,
//				Controller.POINT_TO_POINT_PORT);
//		readerThread.setDatagramPacketCallback(controller);
//		new Thread(readerThread).start();

		final KNXPacketConverter knxPacketConverter = new KNXPacketConverter();

		final InwardConverterPipelineStep inwardConverterPipelineStep = new InwardConverterPipelineStep();
		inwardConverterPipelineStep.setKnxPacketConverter(knxPacketConverter);
		final IpFilterPipelineStep ipFilterPipelineStep = new IpFilterPipelineStep();
		final InwardOutputPipelineStep inwardOutputPipelineStep = new InwardOutputPipelineStep();
		outwardOutputPipelineStep.setPrefix("MULTICAST");

		final Pipeline<Object, Object> inputPipeline = new DefaultPipeline();
		inputPipeline.addStep(inwardConverterPipelineStep);
		inputPipeline.addStep(ipFilterPipelineStep);
//		inputPipeline.addStep(inwardOutputPipelineStep);

		// reader for multicast messages
		final MulticastListenerReaderThread multicastListenerThread = new MulticastListenerReaderThread(
				Controller.POINT_TO_POINT_PORT);
		multicastListenerThread.setDatagramPacketCallback(controller);
		multicastListenerThread.setInputPipeline(inputPipeline);
		multicastListenerThread.setConnectionManager(connectionManager);
		new Thread(multicastListenerThread).start();

		// TODO: have a scheduled thread that repeatedly sends search requests
//		controller.sendSearchRequest();
	}

	@SuppressWarnings("unused")
	private static void pingGoogle() throws IOException {

		final String address = InetAddress.getByName("www.google.com").getHostAddress();
		final InetAddress inetAddress = InetAddress.getByName(address);

		if (inetAddress.isReachable(50000)) {
			System.out.println("Host is reachable");
			LOG.info("Host is reachable");
		} else {
			LOG.info("Host is not reachable");
		}
	}

	private static Controller createKNXController() throws IOException {

		final KNXPacketConverter knxPacketConverter = new KNXPacketConverter();

		final Controller controller = new Controller();
		controller.setKnxPacketConverter(knxPacketConverter);

		return controller;
	}
}
