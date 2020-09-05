package knx.server.configuration;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import api.configuration.ConfigurationManager;
import api.data.serializer.DataSerializer;
import api.device.Device;
import api.device.DeviceStatus;
import api.exception.ProjectParsingException;
import api.pipeline.Pipeline;
import api.project.KNXDeviceInstance;
import api.project.KNXGroupAddress;
import api.project.KNXProject;
import common.configuration.DefaultConfigurationManager;
import common.data.conversion.BitDataSerializer;
import common.data.conversion.DataConversion;
import common.data.conversion.Float16DataSerializer;
import common.packets.ServiceIdentifier;
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
import core.data.sending.DataSender;
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

@Component
public class Configuration {

	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger(Configuration.class);

	// 1.1.101
	private static final int DEVICE_ADDRESS = 0x1165;

	@Value("${knx.projectfile}")
	private String projectfile;

	@Value("${ip}")
	private String ip;

	@Bean
	public KNXProject getKnxProject(final ProjectParser<KNXProjectParsingContext> projectParser)
			throws IOException, ProjectParsingException {

		final File projectFile = new File(projectfile);

		LOG.info("Parsing file: '{}'", projectFile.getAbsoluteFile());
		final KNXProject knxProject = projectParser.parse(projectFile);

		return knxProject;
	}

	@Bean
	public OutwardOutputPipelineStep getOutwardOutputPipelineStep() {

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

		return outwardOutputPipelineStep;
	}

	@Bean
	public OutwardConverterPipelineStep getOutwardConverterPipelineStep() {

		final OutwardConverterPipelineStep outwardConverterPipelineStep = new OutwardConverterPipelineStep();
		return outwardConverterPipelineStep;
	}

	@Bean("outwardPipeline")
	@Qualifier("outwardPipeline")
	public Pipeline<Object, Object> getOutwardPipeline(final OutwardOutputPipelineStep outwardOutputPipelineStep,
			final OutwardConverterPipelineStep outwardConverterPipelineStep) {

		final Pipeline<Object, Object> outwardPipeline = new DefaultPipeline();
//		outwardPipeline.addStep(outwardOutputPipelineStep);
		outwardPipeline.addStep(outwardConverterPipelineStep);

		return outwardPipeline;
	}

	@Bean("coreKNXPacketConverter")
	@Qualifier("coreKNXPacketConverter")
	public KNXPacketConverter<byte[], KNXPacket> getCoreKNXPacketConverter() {
		final KNXPacketConverter<byte[], KNXPacket> coreKNXPacketConverter = new CoreKNXPacketConverter();
		return coreKNXPacketConverter;
	}

	@Bean("deviceManagementKNXPacketConverter")
	@Qualifier("deviceManagementKNXPacketConverter")
	public KNXPacketConverter<byte[], KNXPacket> getDeviceManagementKNXPacketConverter() {
		final KNXPacketConverter<byte[], KNXPacket> deviceManagementKNXPacketConverter = new DeviceManagementKNXPacketConverter();
		return deviceManagementKNXPacketConverter;
	}

	@Bean("tunnelKNXPacketConverter")
	@Qualifier("tunnelKNXPacketConverter")
	public KNXPacketConverter<byte[], KNXPacket> getTunnelKNXPacketConverter() {
		final KNXPacketConverter<byte[], KNXPacket> tunnelKNXPacketConverter = new TunnelKNXPacketConverter();
		return tunnelKNXPacketConverter;
	}

	@Bean
	public InwardConverterPipelineStep getInwardConverterPipelineStep(
			@Qualifier("coreKNXPacketConverter") final KNXPacketConverter<byte[], KNXPacket> coreKNXPacketConverter,
			@Qualifier("deviceManagementKNXPacketConverter") final KNXPacketConverter<byte[], KNXPacket> deviceManagementKNXPacketConverter,
			@Qualifier("tunnelKNXPacketConverter") final KNXPacketConverter<byte[], KNXPacket> tunnelKNXPacketConverter) {

		final InwardConverterPipelineStep inwardConverterPipelineStep = new InwardConverterPipelineStep();
		inwardConverterPipelineStep.getConverters().add(coreKNXPacketConverter);
		inwardConverterPipelineStep.getConverters().add(deviceManagementKNXPacketConverter);
		inwardConverterPipelineStep.getConverters().add(tunnelKNXPacketConverter);

		return inwardConverterPipelineStep;
	}

	@Bean
	public IpFilterPipelineStep getInwardIpFilterPipelineStep() {
		final IpFilterPipelineStep inwardIpFilterPipelineStep = new IpFilterPipelineStep();
		return inwardIpFilterPipelineStep;
	}

	@Bean
	public InwardConnectionPipelineStep getInwardConnectionPipelineStep(final ConnectionManager connectionManager) {
		final InwardConnectionPipelineStep inwardConnectionPipelineStep = new InwardConnectionPipelineStep();
		inwardConnectionPipelineStep.setConnectionManager(connectionManager);

		return inwardConnectionPipelineStep;
	}

	@Bean
	public InwardOutputPipelineStep getInwardOutputPipelineStep() {

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

		return inwardOutputPipelineStep;
	}

	@Bean("inwardPipeline")
	@Qualifier("inwardPipeline")
	public Pipeline<Object, Object> getInwardPipeline(final InwardConverterPipelineStep inwardConverterPipelineStep,
			final IpFilterPipelineStep inwardIpFilterPipelineStep,
			final InwardConnectionPipelineStep inwardConnectionPipelineStep,
			final InwardOutputPipelineStep inwardOutputPipelineStep) {

		final Pipeline<Object, Object> inwardPipeline = new DefaultPipeline();
		inwardPipeline.addStep(inwardConverterPipelineStep);
		inwardPipeline.addStep(inwardIpFilterPipelineStep);
		inwardPipeline.addStep(inwardConnectionPipelineStep);
		// inwardPipeline.addStep(inwardOutputPipelineStep);

		return inwardPipeline;
	}

	@Bean
	public ConnectionManager get(@Qualifier("outwardPipeline") final Pipeline<Object, Object> outwardPipeline) {

		final ConnectionManager connectionManager = new DefaultConnectionManager();
		connectionManager.setOutputPipeline(outwardPipeline);

		return connectionManager;
	}

	@Bean
	public Device getDevice(final KNXProject knxProject) {

		final Device device = new DefaultDevice();
		device.setHostPhysicalAddress(DEVICE_ADDRESS);
		device.setPhysicalAddress(DEVICE_ADDRESS);
//		device.setDeviceStatus(DeviceStatus.PROGRAMMING_MODE);
		device.setDeviceStatus(DeviceStatus.NORMAL_MODE);

		final KNXDeviceInstance knxDeviceInstance = knxProject.getDeviceInstances().get(0);
		knxDeviceInstance.getComObjects().values().stream().forEach(comObject -> {

			final KNXGroupAddress knxGroupAddress = comObject.getKnxGroupAddress();
			if (knxGroupAddress != null && StringUtils.isNotBlank(knxGroupAddress.getGroupAddress())) {

				final String groupAddress = knxGroupAddress.getGroupAddress();
				device.getDeviceProperties().put(groupAddress, knxGroupAddress);
			}
		});

		return device;
	}

	@Bean
	public ProjectParser<KNXProjectParsingContext> getProjectParser() {

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

	@Bean
	public DefaultDataSender getDefaultDataSender(final KNXProject knxProject, final Device device) throws IOException {

		final DefaultDataSender dataSender = new DefaultDataSender();
		dataSender.setDevice(device);
		dataSender.setKnxProject(knxProject);

		return dataSender;
	}

	@Bean
	public CoreController getCoreController(final ConfigurationManager configurationManager,
			final ConnectionManager connectionManager, final Device device)
			throws SocketException, UnknownHostException {

//		final CoreController coreController = new CoreController(NetworkUtils.retrieveLocalIP());
		final CoreController coreController = new CoreController();
		coreController.setConfigurationManager(configurationManager);
		coreController.setDevice(device);
		coreController.setConnectionManager(connectionManager);

		return coreController;
	}

	@Bean
	public ServerCoreController getServerCoreController(final ConfigurationManager configurationManager,
			final ConnectionManager connectionManager, final Device device)
			throws SocketException, UnknownHostException {

//		final ServerCoreController serverCoreController = new ServerCoreController(NetworkUtils.retrieveLocalIP());
		final ServerCoreController serverCoreController = new ServerCoreController();
		serverCoreController.setConfigurationManager(configurationManager);
		serverCoreController.setDevice(device);
		serverCoreController.setConnectionManager(connectionManager);

		return serverCoreController;
	}

	@Bean
	public DeviceManagementController getDeviceManagementController(final ConfigurationManager configurationManager,
			final ConnectionManager connectionManager, final Device device)
			throws SocketException, UnknownHostException {

//		final DeviceManagementController deviceManagementController = new DeviceManagementController(
//				NetworkUtils.retrieveLocalIP());
		final DeviceManagementController deviceManagementController = new DeviceManagementController();
		deviceManagementController.setConfigurationManager(configurationManager);
		deviceManagementController.setLocalInetAddress(
				configurationManager.getPropertyAsString(ConfigurationManager.LOCAL_IP_CONFIG_KEY));
		deviceManagementController.setDevice(device);
		deviceManagementController.setConnectionManager(connectionManager);

		return deviceManagementController;
	}

	@Bean
	public TunnelingController getTunnelingController(final ConfigurationManager configurationManager,
			final ConnectionManager connectionManager, final Device device, final DataSender dataSender)
			throws SocketException, UnknownHostException {

//		final TunnelingController tunnelingController = new TunnelingController(NetworkUtils.retrieveLocalIP());
		final TunnelingController tunnelingController = new TunnelingController();
		tunnelingController.setConfigurationManager(configurationManager);
		tunnelingController.setLocalInetAddress(
				configurationManager.getPropertyAsString(ConfigurationManager.LOCAL_IP_CONFIG_KEY));
		tunnelingController.setDevice(device);
		tunnelingController.setConnectionManager(connectionManager);
		tunnelingController.setDataSender(dataSender);

		return tunnelingController;
	}

	@Bean
	public MulticastListenerReaderThread getMulticastListenerReaderThread(final CoreController coreController,
			final ServerCoreController serverCoreController,
			final DeviceManagementController deviceManagementController, final TunnelingController tunnelingController,
			final Pipeline<Object, Object> inwardPipeline, final ConfigurationManager configurationManager) {

		// reader for multicast messages
		final MulticastListenerReaderThread multicastListenerThread = new MulticastListenerReaderThread();
		multicastListenerThread.setConfigurationManager(configurationManager);
		multicastListenerThread.getDatagramPacketCallbacks().add(coreController);
		multicastListenerThread.getDatagramPacketCallbacks().add(serverCoreController);
		multicastListenerThread.getDatagramPacketCallbacks().add(deviceManagementController);
		multicastListenerThread.getDatagramPacketCallbacks().add(tunnelingController);
		multicastListenerThread.setInputPipeline(inwardPipeline);

		return multicastListenerThread;
	}

	@Bean
	public RequestFactory getRequestFactory(final KNXProject knxProject) {

		final RequestFactory requestFactory = new RequestFactory();
		requestFactory.setKnxProject(knxProject);

		return requestFactory;
	}

	@Bean("objectServerInputPipeline")
	@Qualifier("objectServerInputPipeline")
	public Pipeline<Object, Object> getObjectServerInwardPipeline(final RequestFactory requestFactory) {

		final ConverterPipelineStep objectServerConverterPipelineStep = new ConverterPipelineStep();
		objectServerConverterPipelineStep.setRequestFactory(requestFactory);

		final Pipeline<Object, Object> objectServerInwardPipeline = new DefaultPipeline();
		objectServerInwardPipeline.addStep(objectServerConverterPipelineStep);

		return objectServerInwardPipeline;
	}

	@Bean
	public Map<String, DataSerializer<Object>> getDataSerializerMap() {

		final Map<String, DataSerializer<Object>> dataSerializerMap = new HashMap<>();
		dataSerializerMap.put(DataConversion.FLOAT16, new Float16DataSerializer());
		dataSerializerMap.put(DataConversion.BIT, new BitDataSerializer());

		return dataSerializerMap;
	}

	@Bean
	public ObjectServerReaderThread getObjectServerReaderThread(final KNXProject knxProject,
			final Map<String, DataSerializer<Object>> dataSerializerMap,
			final Pipeline<Object, Object> objectServerInputPipeline, final ConfigurationManager configurationManager) {

		final ObjectServerReaderThread objectServerReaderThread = new ObjectServerReaderThread();
		objectServerReaderThread.setConfigurationManager(configurationManager);
		objectServerReaderThread.setKnxProject(knxProject);
		objectServerReaderThread.setDataSerializerMap(dataSerializerMap);
		objectServerReaderThread.setInputPipeline(objectServerInputPipeline);

		return objectServerReaderThread;
	}

	@Bean
	public ConfigurationManager getDefaultConfigurationManager() {

		final DefaultConfigurationManager defaultConfigurationManager = new DefaultConfigurationManager();
		defaultConfigurationManager.setProperty(ConfigurationManager.LOCAL_IP_CONFIG_KEY, ip);

		return defaultConfigurationManager;
	}

}
