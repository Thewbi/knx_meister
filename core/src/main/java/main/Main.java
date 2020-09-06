package main;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.configuration.ConfigurationManager;
import api.data.serializer.DataSerializer;
import api.device.Device;
import api.device.DeviceService;
import api.device.DeviceStatus;
import api.exception.ProjectParsingException;
import api.pipeline.Pipeline;
import api.project.ProjectService;
import common.configuration.DefaultConfigurationManager;
import common.data.conversion.BitDataSerializer;
import common.data.conversion.DataConversion;
import common.data.conversion.Float16DataSerializer;
import common.data.conversion.UnsignedIntByteSerializer;
import common.packets.ServiceIdentifier;
import common.utils.NetworkUtils;
import core.common.KNXPacketConverter;
import core.communication.ConnectionManager;
import core.communication.DefaultConnectionManager;
import core.communication.MulticastListenerReaderThread;
import core.communication.ObjectServerReaderThread;
import core.communication.controller.CoreController;
import core.communication.controller.DeviceManagementController;
import core.communication.controller.ServerCoreController;
import core.communication.controller.TunnelingController;
import core.conversion.CoreKNXPacketConverter;
import core.conversion.DeviceManagementKNXPacketConverter;
import core.conversion.TunnelKNXPacketConverter;
import core.data.sending.DefaultDataSender;
import core.devices.DefaultDevice;
import core.devices.DefaultDeviceService;
import core.packets.KNXPacket;
import core.pipeline.DefaultPipeline;
import core.pipeline.InwardConnectionPipelineStep;
import core.pipeline.InwardConverterPipelineStep;
import core.pipeline.InwardOutputPipelineStep;
import core.pipeline.IpFilterPipelineStep;
import core.pipeline.OutwardConverterPipelineStep;
import core.pipeline.OutwardOutputPipelineStep;
import object_server.pipeline.ConverterPipelineStep;
import object_server.requests.DefaultRequestFactory;
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
import project.service.DefaultProjectService;

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

    // index 0 is Weinzierl
    public static final int DEVICE_INDEX = 0;
//	private static final int DEVICE_INDEX = 1;

    private static final boolean START_OBJECT_SERVER = true;
//	private static final boolean START_OBJECT_SERVER = false;

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

//        final File projectFile = new File("C:/Users/U5353/Documents/knxproj/KNX_IP_BAOS_777.knxproj");
//      final File projectFile = new File("C:/Users/U5353/Desktop/KNX_IP_BAOS_777.knxproj");
//      final File projectFile = new File("C:/Users/U5353/Desktop/KNX_IP_BAOS_777_version.knxproj");
//      final File projectFile = new File("C:/Users/U5353/Desktop/test.knxproj");
//      final File projectFile = new File("C:/Users/U5353/Desktop/KNXfirstSteps200212_5devices.knxproj");
//      final File projectFile = new File("C:/dev/knx_simulator/K-NiX/ETS5/KNX IP BAOS 777.knxproj");
//      final File projectFile = new File("C:/dev/knx_simulator/K-NiX/ETS5/KNXfirstSteps200212_5devices.knxproj");
//      final File projectFile = new File("C:/Users/U5353/Documents/knxproj/Messe_NatVent_newGA_2_PFS2.knxproj");

        final ConfigurationManager configurationManager = new DefaultConfigurationManager();
//		final ConfigurationManager configurationManager = null;
        configurationManager.setProperty(ConfigurationManager.PROJECT_FILE_KEY,
                "C:/Users/U5353/Documents/knxproj/KNX_IP_BAOS_777.knxproj");

        final String localIP = NetworkUtils.retrieveLocalIP();

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

        // 0x0A11 == 1.1.10
        device.setHostPhysicalAddress(0x0A11);
//		device.setHostPhysicalAddress(0xFF11);
//		device.setHostPhysicalAddress(0x11FF);

//		device.setPhysicalAddress(DEVICE_ADDRESS);
//		device.setPhysicalAddress(0x0A11);
        // 0xFF11 == 1.1.255, 0xFF11 == 15.15.17
//		device.setPhysicalAddress(0xFF11);
        device.setPhysicalAddress(0x11FF);

//		device.setDeviceStatus(DeviceStatus.PROGRAMMING_MODE);
        device.setDeviceStatus(DeviceStatus.NORMAL_MODE);

        final ProjectParser<KNXProjectParsingContext> knxProjectParser = retrieveProjectParser();

//        LOG.info("Parsing project file: \"" + projectFile.getAbsolutePath() + "\"");

//        final KNXProject knxProject = knxProjectParser.parse(projectFile);

        final DefaultProjectService projectService = new DefaultProjectService();
        projectService.setConfigurationManager(configurationManager);
        projectService.setProjectParser(knxProjectParser);
        projectService.parseProjectFile();

//        // copy data from KNXDeviceInstance into Device / DefaultDevice
//        final KNXDeviceInstance knxDeviceInstance = projectService.getProject().getDeviceInstances().get(DEVICE_INDEX);
//        knxDeviceInstance.getComObjects().values().stream().forEach(knxComObject -> {
//
//            // copy the group addresses of of comObjects into the device
//            final KNXGroupAddress knxGroupAddress = knxComObject.getKnxGroupAddress();
//            if (knxGroupAddress != null && StringUtils.isNotBlank(knxGroupAddress.getGroupAddress())) {
//
//                final String groupAddress = knxGroupAddress.getGroupAddress();
//                device.getDeviceProperties().put(groupAddress, knxGroupAddress);
//
//                // PUT_A and PUT_B put comObjects into knxDeviceInstance.
//                // Now copy from knxDeviceInstance into Device / DefaultDevice
//                LOG.info("PUT_C into device " + device.getPhysicalAddress() + " DataPointId:" + knxComObject.getNumber()
//                        + " " + knxComObject.getKnxGroupAddress() + " " + knxComObject.getHardwareName() + " "
//                        + knxComObject.getText());
//                device.getComObjects().put(groupAddress, knxComObject);
//                device.getComObjectsByDatapointType().put(knxComObject.getNumber(), knxComObject);
//            }
//
//        });

        final DeviceService deviceService = new DefaultDeviceService();
        deviceService.retrieveDevicesFromProject(projectService.getProject());

        final Map<String, DataSerializer<Object>> dataSerializerMap = new HashMap<>();
        dataSerializerMap.put(DataConversion.FLOAT16, new Float16DataSerializer());
        dataSerializerMap.put(DataConversion.BIT, new BitDataSerializer());
        dataSerializerMap.put(DataConversion.UNSIGNED_INTEGER_8, new UnsignedIntByteSerializer());

        final Map<String, DataSerializer<Object>> dataSerializerMapByDataPointType = new HashMap<>();
//		dataSerializerMapByDataPointType.put(DataConversion.FLOAT16, new Float16DataSerializer());
//		dataSerializerMapByDataPointType.put(DataConversion.BIT, new BitDataSerializer());
        dataSerializerMapByDataPointType.put("DPST-1-5", new UnsignedIntByteSerializer());

        final DefaultDataSender dataSender = new DefaultDataSender();
        dataSender.setDataSerializerMap(dataSerializerMap);
//        dataSender.setDevice(device);
        dataSender.setDeviceService(deviceService);
//        dataSender.setKnxProject(knxProject);
        dataSender.setProjectService(projectService);

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

        final CoreController coreController = new CoreController();
        coreController.setLocalInetAddress(localIP);
//		coreController.setDevice(device);
        coreController.setDeviceService(deviceService);
        coreController.setConnectionManager(connectionManager);

        final ServerCoreController serverCoreController = new ServerCoreController();
        serverCoreController.setLocalInetAddress(localIP);
//		serverCoreController.setDevice(device);
        serverCoreController.setDeviceService(deviceService);
        serverCoreController.setConnectionManager(connectionManager);
        serverCoreController.setDataSender(dataSender);

        final DeviceManagementController deviceManagementController = new DeviceManagementController();
//		deviceManagementController.setDevice(device);
        deviceManagementController.setDeviceService(deviceService);
        deviceManagementController.setConnectionManager(connectionManager);

        final TunnelingController tunnelingController = new TunnelingController();
        tunnelingController.setLocalInetAddress(localIP);
//		tunnelingController.setDevice(device);
        tunnelingController.setDeviceService(deviceService);
        tunnelingController.setConnectionManager(connectionManager);
        tunnelingController.setDataSender(dataSender);

        // reader for multicast messages
        final MulticastListenerReaderThread multicastListenerThread = new MulticastListenerReaderThread();
        multicastListenerThread.setConfigurationManager(configurationManager);
        multicastListenerThread.getDatagramPacketCallbacks().add(coreController);
        multicastListenerThread.getDatagramPacketCallbacks().add(serverCoreController);
        multicastListenerThread.getDatagramPacketCallbacks().add(deviceManagementController);
        multicastListenerThread.getDatagramPacketCallbacks().add(tunnelingController);
        multicastListenerThread.setInputPipeline(inwardPipeline);
//		multicastListenerThread.setConnectionManager(connectionManager);

        new Thread(multicastListenerThread).start();

        setupObjectServerInfrastructure(localIP, projectService, dataSerializerMap, configurationManager);

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
     * BAOS object server protocol server.
     *
     * Connection-Procedure to a BAOS Server (See
     * KNX_BAOS_Binary_Protocol_V2_0.pdf):
     * <ol>
     * <li />The application software that contains the BAOS client will send a
     * request to an IP.
     * <li />The BAOS client first perform KNX Discovery, that means it will send a
     * SearchRequest Package to the multicast IP 224.0.23.12.
     * <li />The BAOS server answers that request. It will send it's own IP and it's
     * supported protocols in the response. Two things are important. The IP of the
     * BAOS Server has to match the IP specified by the application program using
     * the BAOS client and second, the supported protocols have to contain the byte
     * sequence: 0x01 (= Record type) 0x04 (= Record length) 0xF0 (= ObjectServer
     * protocol) 0x20 (= ObjectServer version)
     * <li />The BOAS client checks the discovery response. If the BAOS Object
     * Server Protocol is supported AND the IP matches the IP specified by the
     * application, the BAOS client switches to talking to the IP using the Binary
     * Object Protocol.
     * <li />The BAOS server was bound the the IP beforehand and now talks BAOS to
     * the BAOS client.
     * </ol>
     *
     * @param knxProject
     * @param dataSerializerMap
     * @throws SocketException
     * @throws UnknownHostException
     */
    private static void setupObjectServerInfrastructure(final String localIP, final ProjectService projectService,
            final Map<String, DataSerializer<Object>> dataSerializerMap,
            final ConfigurationManager configurationManager) throws UnknownHostException, SocketException {

        final DefaultRequestFactory requestFactory = new DefaultRequestFactory();
//        requestFactory.setKnxProject(projectService.getProject());
        requestFactory.setProjectService(projectService);

        final ConverterPipelineStep objectServerConverterPipelineStep = new ConverterPipelineStep();
        objectServerConverterPipelineStep.setRequestFactory(requestFactory);

        final Pipeline<Object, Object> objectServerInwardPipeline = new DefaultPipeline();
        objectServerInwardPipeline.addStep(objectServerConverterPipelineStep);

        // start the ObjectServer protocol on port 12004
        final ObjectServerReaderThread objectServerReaderThread = new ObjectServerReaderThread();
        objectServerReaderThread.setConfigurationManager(configurationManager);
//        objectServerReaderThread.setKnxProject(knxProject);
        objectServerReaderThread.setProjectService(projectService);
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
