package main;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.api.device.Device;
import core.api.pipeline.Pipeline;
import core.common.KNXPacketConverter;
import core.common.NetworkUtils;
import core.communication.ConnectionManager;
import core.communication.DefaultConnectionManager;
import core.communication.MulticastListenerReaderThread;
import core.communication.controller.BaseController;
import core.communication.controller.CoreController;
import core.communication.controller.DeviceManagementController;
import core.communication.controller.ServerCoreController;
import core.communication.controller.TunnelingController;
import core.conversion.CoreKNXPacketConverter;
import core.conversion.DeviceManagementKNXPacketConverter;
import core.conversion.TunnelKNXPacketConverter;
import core.data.sending.DefaultDataSender;
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
import project.parsing.ProjectParser;
import project.parsing.domain.KNXProject;
import project.parsing.knx.KNXProjectParser;
import project.parsing.knx.KNXProjectParsingContext;
import project.parsing.knx.steps.DatapointTypeParsingStep;
import project.parsing.knx.steps.DeleteTempFolderParsingStep;
import project.parsing.knx.steps.ExtractArchiveParsingStep;
import project.parsing.knx.steps.GroupAddressParsingStep;
import project.parsing.knx.steps.OutputParsingStep;
import project.parsing.knx.steps.ReadProjectInstallationsParsingStep;
import project.parsing.knx.steps.ReadProjectParsingStep;

/**
 * Wireshark filters
 *
 * <pre>
 * On loopback
 * udp and ( (ip.src == 192.168.0.1/16) or (ip.dst == 192.168.0.0/16) )
 * udp and ( (ip.src == 192.168.0.1/16) or (ip.dst == 192.168.0.0/16) or (ip.dst == 224.0.23.12/32) )
 *
 * On WLAN: (Weinzierl without multicast traffic)
 * udp and ( (ip.src == 192.168.0.241/32) or (ip.dst == 192.168.0.241/32) )
 *
 * On WLAN nur KNX/IP multicast
 * udp and ( (ip.src == 224.0.23.12/32) or (ip.dst == 224.0.23.12/32) )
 *
 * On WLAN without discovery traffic
 * udp and ( (ip.src == 192.168.0.241/32) or (ip.dst == 192.168.0.241/32) ) and (not frame contains 06:10:02:02) and (not frame contains 06:10:02:01) and (not frame contains 06:10:02:0b)
 *
 * On WLAN: (Weinzierl including multicast traffic)
 * udp and ( (ip.src == 192.168.0.241/32) or (ip.dst == 192.168.0.241/32) or (ip.dst == 224.0.23.12/32) )
 *
 * udp and ( (ip.src == 192.168.0.234/32) or (ip.dst == 192.168.0.234/32) )
 * udp and ( (ip.src == 192.168.0.234/32) or (ip.dst == 192.168.0.234/32) or (ip.dst == 224.0.23.12/32) )
 *
 * Without discovery traffic:
 * udp and ( (ip.src == 192.168.0.1/16) or (ip.dst == 192.168.0.0/16) ) and (not frame contains 06:10:02:02) and (not frame contains 06:10:02:01) and (not frame contains 06:10:02:0b)
 *
 * Without discovery traffic and own computer name:
 * udp and ( (ip.src == 192.168.0.1/16) or (ip.dst == 192.168.0.0/16) ) and (not frame contains 06:10:02:02) and (not frame contains 06:10:02:01) and (not frame contains 06:10:02:0b) and (not frame contains "DE7487M")
 * </pre>
 */
public class Main {

	/** Host physical address in ETS5, 0x1A11 == 1.10.17 */
	// private static final int DEVICE_ADDRESS = 0x1A11;

	// 1.1.17
//	private static final int DEVICE_ADDRESS = 0x1111;

	// 1.1.101
	private static final int DEVICE_ADDRESS = 0x1165;

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
//		outwardPipeline.addStep(outwardOutputPipelineStep);
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

		final ExtractArchiveParsingStep extractArchiveParsingStep = new ExtractArchiveParsingStep();
		final ReadProjectParsingStep readProjectParsingStep = new ReadProjectParsingStep();
		final ReadProjectInstallationsParsingStep readProjectInstallationsParsingStep = new ReadProjectInstallationsParsingStep();
		final GroupAddressParsingStep groupAddressParsingStep = new GroupAddressParsingStep();
		final DatapointTypeParsingStep datapointTypeParsingStep = new DatapointTypeParsingStep();
		final DeleteTempFolderParsingStep deleteTempFolderParsingStep = new DeleteTempFolderParsingStep();
		final OutputParsingStep outputParsingStep = new OutputParsingStep();

		final ProjectParser<KNXProjectParsingContext> knxProjectParser = new KNXProjectParser();
		knxProjectParser.getParsingSteps().add(extractArchiveParsingStep);
		knxProjectParser.getParsingSteps().add(readProjectParsingStep);
		knxProjectParser.getParsingSteps().add(readProjectInstallationsParsingStep);
		knxProjectParser.getParsingSteps().add(groupAddressParsingStep);
		knxProjectParser.getParsingSteps().add(datapointTypeParsingStep);
		knxProjectParser.getParsingSteps().add(deleteTempFolderParsingStep);
		knxProjectParser.getParsingSteps().add(outputParsingStep);

		final File projectFile = new File("C:/dev/knx_simulator/K-NiX/ETS5/KNX IP BAOS 777.knxproj");
		final KNXProject knxProject = knxProjectParser.parse(projectFile);

		final DefaultDataSender dataSender = new DefaultDataSender();
		dataSender.setDevice(device);
		dataSender.setKnxProject(knxProject);

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
//		inwardPipeline.addStep(inwardOutputPipelineStep);

		final CoreController coreController = new CoreController(NetworkUtils.LOCAL_IP);
		coreController.setDevice(device);
		coreController.setConnectionManager(connectionManager);

		final ServerCoreController serverCoreController = new ServerCoreController(NetworkUtils.LOCAL_IP);
		serverCoreController.setDevice(device);
		serverCoreController.setConnectionManager(connectionManager);

		final DeviceManagementController deviceManagementController = new DeviceManagementController(
				NetworkUtils.LOCAL_IP);
		deviceManagementController.setDevice(device);
		deviceManagementController.setConnectionManager(connectionManager);

		final TunnelingController tunnelingController = new TunnelingController(NetworkUtils.LOCAL_IP);
		tunnelingController.setDevice(device);
		tunnelingController.setConnectionManager(connectionManager);
		tunnelingController.setDataSender(dataSender);

		// reader for multicast messages
		final MulticastListenerReaderThread multicastListenerThread = new MulticastListenerReaderThread(
				BaseController.KNX_PORT_DEFAULT);
		multicastListenerThread.getDatagramPacketCallbacks().add(coreController);
		multicastListenerThread.getDatagramPacketCallbacks().add(serverCoreController);
		multicastListenerThread.getDatagramPacketCallbacks().add(deviceManagementController);
		multicastListenerThread.getDatagramPacketCallbacks().add(tunnelingController);
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
