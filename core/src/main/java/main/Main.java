package main;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.data.serializer.DataSerializer;
import api.device.Device;
import api.device.DeviceStatus;
import api.exception.ProjectParsingException;
import api.pipeline.Pipeline;
import api.project.KNXDeviceInstance;
import api.project.KNXGroupAddress;
import api.project.KNXProject;
import common.data.conversion.BitDataSerializer;
import common.data.conversion.DataConversion;
import common.data.conversion.Float16DataSerializer;
import common.packets.ServiceIdentifier;
import common.utils.NetworkUtils;
import core.common.KNXPacketConverter;
import core.communication.ConnectionManager;
import core.communication.DefaultConnectionManager;
import core.communication.MulticastListenerReaderThread;
import core.communication.ObjectServerReaderThread;
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
import core.packets.KNXPacket;
import core.pipeline.DefaultPipeline;
import core.pipeline.InwardConnectionPipelineStep;
import core.pipeline.InwardConverterPipelineStep;
import core.pipeline.InwardOutputPipelineStep;
import core.pipeline.IpFilterPipelineStep;
import core.pipeline.OutwardConverterPipelineStep;
import core.pipeline.OutwardOutputPipelineStep;
import object_server.pipeline.ConverterPipelineStep;
import object_server.requests.RequestFactory;
import project.parsing.ProjectParser;
import project.parsing.knx.KNXProjectParser;
import project.parsing.knx.KNXProjectParsingContext;
import project.parsing.knx.steps.ApplicationProgramParsingStep;
import project.parsing.knx.steps.DatapointTypeParsingStep;
import project.parsing.knx.steps.DeleteTempFolderParsingStep;
import project.parsing.knx.steps.ExtractArchiveParsingStep;
import project.parsing.knx.steps.GroupAddressParsingStep;
import project.parsing.knx.steps.HardwareParsingStep;
import project.parsing.knx.steps.ManufacturerParsingStep;
import project.parsing.knx.steps.OutputParsingStep;
import project.parsing.knx.steps.ReadProjectInstallationsParsingStep;
import project.parsing.knx.steps.ReadProjectParsingStep;

/**
 * TODO:
 * <ol>
 * <li />Initiale group reads werden nicht korrekt beantwortet glaub ich.
 * <li />Weiter an Device und enthaltene Properties arbeiten.
 * <li />Vereinheitlichen von Datenserien senden.
 * </ol>
 *
 * Wireshark filters
 *
 * <pre>
 * On loopback
 * udp and ( (ip.src == 192.168.0.0/16) or (ip.dst == 192.168.0.0/16) )
 * udp and ( (ip.src == 192.168.0.24/32) or (ip.dst == 192.168.0.24/32) )
 * udp and ( (ip.src == 192.168.0.0/16) or (ip.dst == 192.168.0.0/16) or (ip.dst == 224.0.23.12/32) )
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
 * udp and ( (ip.src == 192.168.0.0/16) or (ip.dst == 192.168.0.0/16) ) and (not frame contains 06:10:02:02) and (not frame contains 06:10:02:01) and (not frame contains 06:10:02:0b)
 *
 * Without discovery traffic and own computer name:
 * udp and ( (ip.src == 192.168.0.0/16) or (ip.dst == 192.168.0.0/16) ) and (not frame contains 06:10:02:02) and (not frame contains 06:10:02:01) and (not frame contains 06:10:02:0b) and (not frame contains "DE7487M")
 *
 * (ip.src == 127.0.0.1/32) and (ip.dst == 192.168.0.108/32)
 * (ip.src == 192.168.0.108/32) and (ip.dst == 192.168.0.108/32)
 * udp and ((ip.src == 192.168.0.108/32) or (ip.dst == 192.168.0.108/32))
 *
 * (ip.src == 192.168.2.3/32) or (ip.dst == 192.168.2.3/32)
 *
 * (ip.src == 192.168.0.241/32) or (ip.dst == 192.168.0.241/32)
 *
 * (ip.src == 192.168.2.2/32) or (ip.dst == 192.168.2.2/32)
 *
 * </pre>
 */
public class Main {

	private static final boolean START_OBJECT_SERVER = true;

	/** Host physical address in ETS5, 0x1A11 == 1.10.17 */
	// private static final int DEVICE_ADDRESS = 0x1A11;

	// 1.1.17
//	private static final int DEVICE_ADDRESS = 0x1111;

	// 1.1.101
//	private static final int DEVICE_ADDRESS = 0x1165;

	// 1.1.1
//	private static final int DEVICE_ADDRESS = 0x0111;

	// 1.1.255
//	private static final int DEVICE_ADDRESS = 0xFF11;
	private static final int DEVICE_ADDRESS = 0x11FF;

	// 1.1.11
//	private static final int DEVICE_ADDRESS = 0x0B11;

	private static final Logger LOG = LogManager.getLogger(Main.class);

	public static void main(final String[] args) throws IOException, ProjectParsingException {

//		try (Socket socket = new Socket("192.168.0.234", 12004)) {
//
//			final InputStream input = socket.getInputStream();
//			final BufferedReader reader = new BufferedReader(new InputStreamReader(input));
//
//			final String time = reader.readLine();
//
//			System.out.println(time);
//
//		} catch (final UnknownHostException ex) {
//
//			System.out.println("Server not found: " + ex.getMessage());
//
//		} catch (final IOException ex) {
//
//			System.out.println("I/O error: " + ex.getMessage());
//		}

		// https://github.com/apache/dubbo/issues/2423
		//
		// on a macbook, the JVM prioritizes IPv6 interfaces over
		// IPv4 interfaces. Force the JVM to use IPv4.
		System.setProperty("java.net.preferIPv4Stack", "true");

//		final InetAddress inetAddress = InetAddress.getLocalHost();
//		LOG.info("IP of my system is := " + inetAddress.getHostAddress());

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
		device.setHostPhysicalAddress(0x0A11); // 1.1.10
//		device.setHostPhysicalAddress(0xFF11);
//		device.setHostPhysicalAddress(0x11FF);
//		device.setPhysicalAddress(DEVICE_ADDRESS);
//		device.setPhysicalAddress(0x0A11);
		device.setPhysicalAddress(0xFF11); // 1.1.255
//		device.setPhysicalAddress(0x11FF);
//		device.setDeviceStatus(DeviceStatus.PROGRAMMING_MODE);
		device.setDeviceStatus(DeviceStatus.NORMAL_MODE);

		final ProjectParser<KNXProjectParsingContext> knxProjectParser = retrieveProjectParser();

		final File projectFile = new File("C:/Users/U5353/Desktop/KNX_IP_BAOS_777.knxproj");
//		final File projectFile = new File("C:/dev/knx_simulator/K-NiX/ETS5/KNX IP BAOS 777.knxproj");
//		final File projectFile = new File("C:/dev/knx_simulator/K-NiX/ETS5/KNXfirstSteps200212_5devices.knxproj");

		LOG.info("Parsing project file: \"" + projectFile.getAbsolutePath() + "\"");

		final KNXProject knxProject = knxProjectParser.parse(projectFile);

		// index 0 is Weinzierl
		final KNXDeviceInstance knxDeviceInstance = knxProject.getDeviceInstances().get(0);
		knxDeviceInstance.getComObjects().values().stream().forEach(comObject -> {

			final KNXGroupAddress knxGroupAddress = comObject.getKnxGroupAddress();
			if (knxGroupAddress != null && StringUtils.isNotBlank(knxGroupAddress.getGroupAddress())) {

				final String groupAddress = knxGroupAddress.getGroupAddress();
				device.getDeviceProperties().put(groupAddress, knxGroupAddress);
			}
		});

		final Map<String, DataSerializer<Object>> dataSerializerMap = new HashMap<>();
		dataSerializerMap.put(DataConversion.FLOAT16, new Float16DataSerializer());
		dataSerializerMap.put(DataConversion.BIT, new BitDataSerializer());

		final DefaultDataSender dataSender = new DefaultDataSender();
		dataSender.setDataSerializerMap(dataSerializerMap);
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

		final CoreController coreController = new CoreController(NetworkUtils.retrieveLocalIP());
		coreController.setDevice(device);
		coreController.setConnectionManager(connectionManager);

		final ServerCoreController serverCoreController = new ServerCoreController(NetworkUtils.retrieveLocalIP());
		serverCoreController.setDevice(device);
		serverCoreController.setConnectionManager(connectionManager);
		serverCoreController.setDataSender(dataSender);

		final DeviceManagementController deviceManagementController = new DeviceManagementController(
				NetworkUtils.retrieveLocalIP());
		deviceManagementController.setDevice(device);
		deviceManagementController.setConnectionManager(connectionManager);

		final TunnelingController tunnelingController = new TunnelingController(NetworkUtils.retrieveLocalIP());
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

//		new Thread(multicastListenerThread).start();

		setupObjectServerInfrastructure(knxProject, dataSerializerMap);

//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				try {
//					serverCoreController.sendSearchRequest();
//				} catch (final IOException e) {
//					LOG.error(e.getMessage(), e);
//				}
//
//				try {
//					Thread.sleep(10000);
//				} catch (final InterruptedException e) {
//					LOG.error(e.getMessage(), e);
//				}
//			}
//
//		}).start();

		// the Bosch IoT Gateway does not even send an response this request
//		serverCoreController.sendTunnelConnectionRequest(multicastListenerThread.getMulticastSocket());
	}

	/**
	 * BASO object server protocol server
	 *
	 * @param knxProject
	 * @param dataSerializerMap
	 */
	private static void setupObjectServerInfrastructure(final KNXProject knxProject,
			final Map<String, DataSerializer<Object>> dataSerializerMap) {

		final RequestFactory requestFactory = new RequestFactory();
		requestFactory.setKnxProject(knxProject);

		final ConverterPipelineStep objectServerConverterPipelineStep = new ConverterPipelineStep();
		objectServerConverterPipelineStep.setRequestFactory(requestFactory);

		final Pipeline<Object, Object> objectServerInwardPipeline = new DefaultPipeline();
		objectServerInwardPipeline.addStep(objectServerConverterPipelineStep);

		// start the ObjectServer protocol on port 12004
//		final String ip = "127.0.0.1";
//		final String ip = "192.168.0.108";
		final String ip = "192.168.2.1";

		final ObjectServerReaderThread objectServerReaderThread = new ObjectServerReaderThread(ip,
				NetworkUtils.OBJECT_SERVER_PROTOCOL_PORT);
		objectServerReaderThread.setKnxProject(knxProject);
		objectServerReaderThread.setDataSerializerMap(dataSerializerMap);
		objectServerReaderThread.setInputPipeline(objectServerInwardPipeline);

		if (START_OBJECT_SERVER) {
			new Thread(objectServerReaderThread).start();
		}
	}

	private static ProjectParser<KNXProjectParsingContext> retrieveProjectParser() {

		final ExtractArchiveParsingStep extractArchiveParsingStep = new ExtractArchiveParsingStep();
		final ReadProjectParsingStep readProjectParsingStep = new ReadProjectParsingStep();
		final ManufacturerParsingStep manufacturerParsingStep = new ManufacturerParsingStep();
		final ReadProjectInstallationsParsingStep readProjectInstallationsParsingStep = new ReadProjectInstallationsParsingStep();
		final HardwareParsingStep hardwareParsingStep = new HardwareParsingStep();
		final ApplicationProgramParsingStep applicationProgramParsingStep = new ApplicationProgramParsingStep();
		final GroupAddressParsingStep groupAddressParsingStep = new GroupAddressParsingStep();
		final DatapointTypeParsingStep datapointTypeParsingStep = new DatapointTypeParsingStep();
		final DeleteTempFolderParsingStep deleteTempFolderParsingStep = new DeleteTempFolderParsingStep();
		final OutputParsingStep outputParsingStep = new OutputParsingStep();

		final ProjectParser<KNXProjectParsingContext> knxProjectParser = new KNXProjectParser();
		knxProjectParser.getParsingSteps().add(extractArchiveParsingStep);
		knxProjectParser.getParsingSteps().add(readProjectParsingStep);
		knxProjectParser.getParsingSteps().add(manufacturerParsingStep);
		knxProjectParser.getParsingSteps().add(readProjectInstallationsParsingStep);
		knxProjectParser.getParsingSteps().add(hardwareParsingStep);
		knxProjectParser.getParsingSteps().add(applicationProgramParsingStep);
		knxProjectParser.getParsingSteps().add(groupAddressParsingStep);
		knxProjectParser.getParsingSteps().add(datapointTypeParsingStep);
		knxProjectParser.getParsingSteps().add(deleteTempFolderParsingStep);
		knxProjectParser.getParsingSteps().add(outputParsingStep);

		return knxProjectParser;
	}

	@SuppressWarnings("unused")
	private static void pingGoogle() throws IOException {

		final String address = InetAddress.getByName("www.google.com").getHostAddress();
		final InetAddress inetAddress = InetAddress.getByName(address);

		if (inetAddress.isReachable(50000)) {
			LOG.info("Host is reachable");
		} else {
			LOG.info("Host is not reachable");
		}
	}

}
