package main;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.api.device.Device;
import core.api.pipeline.Pipeline;
import core.common.KNXPacketConverter;
import core.communication.ConnectionManager;
import core.communication.Controller;
import core.communication.DefaultConnectionManager;
import core.communication.MulticastListenerReaderThread;
import core.conversion.CoreKNXPacketConverter;
import core.conversion.DeviceManagementKNXPacketConverter;
import core.devices.DefaultDevice;
import core.packets.KNXPacket;
import core.pipeline.DefaultPipeline;
import core.pipeline.InwardConverterPipelineStep;
import core.pipeline.InwardOutputPipelineStep;
import core.pipeline.IpFilterPipelineStep;
import core.pipeline.OutwardConverterPipelineStep;
import core.pipeline.OutwardOutputPipelineStep;

/**
 * Wireshark filters
 *
 * <pre>
 * On loopback
 * udp and ( (ip.src == 192.168.0.1/16) or ( ip.dst == 192.168.0.0/16) )
 *
 * On WLAN:
 * udp and ( (ip.src == 192.168.0.241/32) or ( ip.dst == 192.168.0.241/32) )
 * </pre>
 *
 */
public class Main {

	private static final Logger LOG = LogManager.getLogger(Main.class);

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
		outputPipeline.addStep(outwardOutputPipelineStep);
		outputPipeline.addStep(outwardConverterPipelineStep);

		final ConnectionManager connectionManager = new DefaultConnectionManager();
		connectionManager.setOutputPipeline(outputPipeline);

		final KNXPacketConverter<byte[], KNXPacket> coreKNXPacketConverter = new CoreKNXPacketConverter();
		final KNXPacketConverter<byte[], KNXPacket> deviceManagementKNXPacketConverter = new DeviceManagementKNXPacketConverter();

		final Device device = new DefaultDevice();

		final Controller controller = new Controller();
		controller.setDevice(device);
		controller.setConnectionManager(connectionManager);

//		// reader for point to point connections
//		final PointToPointReaderThread readerThread = new PointToPointReaderThread(POINT_TO_POINT_READER_IP_ADDRESS,
//				Controller.POINT_TO_POINT_PORT);
//		readerThread.setDatagramPacketCallback(controller);
//		new Thread(readerThread).start();

		final InwardConverterPipelineStep inwardConverterPipelineStep = new InwardConverterPipelineStep();
		inwardConverterPipelineStep.getConverters().add(coreKNXPacketConverter);
		inwardConverterPipelineStep.getConverters().add(deviceManagementKNXPacketConverter);

		final IpFilterPipelineStep ipFilterPipelineStep = new IpFilterPipelineStep();

		final InwardOutputPipelineStep inwardOutputPipelineStep = new InwardOutputPipelineStep();
		inwardOutputPipelineStep.setPrefix("MULTICAST");

		final Pipeline<Object, Object> inputPipeline = new DefaultPipeline();
		inputPipeline.addStep(inwardConverterPipelineStep);
		inputPipeline.addStep(ipFilterPipelineStep);
		inputPipeline.addStep(inwardOutputPipelineStep);

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

}
