package main;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Locale;

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
import core.conversion.TunnelKNXPacketConverter;
import core.devices.DefaultDevice;
import core.packets.DeviceStatus;
import core.packets.KNXPacket;
import core.packets.ServiceIdentifier;
import core.pipeline.DefaultPipeline;
import core.pipeline.InwardConnectionPipelineStep;
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
 * udp and ( (ip.src == 192.168.0.1/16) or ( ip.dst == 192.168.0.0/16) or ( ip.dst == 224.0.23.12/32) )
 *
 * On WLAN: (Weinzierl without multicast traffic)
 * udp and ( (ip.src == 192.168.0.241/32) or ( ip.dst == 192.168.0.241/32) )
 *
 * On WLAN: (Weinzierl including multicast traffic)
 * udp and ( (ip.src == 192.168.0.241/32) or ( ip.dst == 192.168.0.241/32) or ( ip.dst == 224.0.23.12/32) )
 * </pre>
 *
 */
public class Main {

	/** Host physical address in ETS5, 0x1A11 == 1.10.17 */
	// private static final int DEVICE_ADDRESS = 0x1A11;
	private static final int DEVICE_ADDRESS = 0x1111;

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
		outwardOutputPipelineStep.getIgnorePackets()
				.add(ServiceIdentifier.SEARCH_REQUEST.name().toLowerCase(Locale.getDefault()));
		outwardOutputPipelineStep.getIgnorePackets()
				.add(ServiceIdentifier.SEARCH_REQUEST_EXT.name().toLowerCase(Locale.getDefault()));
		outwardOutputPipelineStep.getIgnorePackets()
				.add(ServiceIdentifier.SEARCH_RESPONSE.name().toLowerCase(Locale.getDefault()));
		outwardOutputPipelineStep.getIgnorePackets()
				.add(ServiceIdentifier.SEARCH_RESPONSE_EXT.name().toLowerCase(Locale.getDefault()));

		final OutwardConverterPipelineStep outwardConverterPipelineStep = new OutwardConverterPipelineStep();

		final Pipeline<Object, Object> outwardPipeline = new DefaultPipeline();
		outwardPipeline.addStep(outwardOutputPipelineStep);
		outwardPipeline.addStep(outwardConverterPipelineStep);

		final ConnectionManager connectionManager = new DefaultConnectionManager();
		connectionManager.setOutputPipeline(outwardPipeline);

		final KNXPacketConverter<byte[], KNXPacket> coreKNXPacketConverter = new CoreKNXPacketConverter();
		final KNXPacketConverter<byte[], KNXPacket> deviceManagementKNXPacketConverter = new DeviceManagementKNXPacketConverter();
		final KNXPacketConverter<byte[], KNXPacket> tunnelKNXPacketConverter = new TunnelKNXPacketConverter();

		final Device device = new DefaultDevice();
		device.setHostPhysicalAddress(DEVICE_ADDRESS);
		device.setPhysicalAddress(DEVICE_ADDRESS);
		device.setDeviceStatus(DeviceStatus.PROGRAMMING_MODE);

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
		inwardConverterPipelineStep.getConverters().add(tunnelKNXPacketConverter);

		final IpFilterPipelineStep inwardIpFilterPipelineStep = new IpFilterPipelineStep();

		final InwardConnectionPipelineStep inwardConnectionPipelineStep = new InwardConnectionPipelineStep();
		inwardConnectionPipelineStep.setConnectionManager(connectionManager);

		final InwardOutputPipelineStep inwardOutputPipelineStep = new InwardOutputPipelineStep();
		inwardOutputPipelineStep.setPrefix("MULTICAST");
		inwardOutputPipelineStep.getIgnorePackets()
				.add(ServiceIdentifier.SEARCH_REQUEST.name().toLowerCase(Locale.getDefault()));
		inwardOutputPipelineStep.getIgnorePackets()
				.add(ServiceIdentifier.SEARCH_REQUEST_EXT.name().toLowerCase(Locale.getDefault()));
		inwardOutputPipelineStep.getIgnorePackets()
				.add(ServiceIdentifier.SEARCH_RESPONSE.name().toLowerCase(Locale.getDefault()));
		inwardOutputPipelineStep.getIgnorePackets()
				.add(ServiceIdentifier.SEARCH_RESPONSE_EXT.name().toLowerCase(Locale.getDefault()));

		final Pipeline<Object, Object> inwardPipeline = new DefaultPipeline();
		inwardPipeline.addStep(inwardConverterPipelineStep);
		inwardPipeline.addStep(inwardIpFilterPipelineStep);
		inwardPipeline.addStep(inwardConnectionPipelineStep);
		inwardPipeline.addStep(inwardOutputPipelineStep);

		// reader for multicast messages
		final MulticastListenerReaderThread multicastListenerThread = new MulticastListenerReaderThread(
				Controller.POINT_TO_POINT_PORT);
		multicastListenerThread.setDatagramPacketCallback(controller);
		multicastListenerThread.setInputPipeline(inwardPipeline);
//		multicastListenerThread.setConnectionManager(connectionManager);
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
