package knx.server;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

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
import core.data.sending.DataSender;
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
	public Device getDevice() {

		final Device device = new DefaultDevice();
		device.setHostPhysicalAddress(DEVICE_ADDRESS);
		device.setPhysicalAddress(DEVICE_ADDRESS);
		device.setDeviceStatus(DeviceStatus.PROGRAMMING_MODE);

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
	KNXProject getKnxProject(final ProjectParser<KNXProjectParsingContext> projectParser) throws IOException {

		final File projectFile = new File("C:/dev/knx_simulator/K-NiX/ETS5/KNX IP BAOS 777.knxproj");
		final KNXProject knxProject = projectParser.parse(projectFile);

		return knxProject;
	}

	@Bean
	public DefaultDataSender getDefaultDataSender(final KNXProject knxProject, final Device device) throws IOException {

		final DefaultDataSender dataSender = new DefaultDataSender();
		dataSender.setDevice(device);
		dataSender.setKnxProject(knxProject);

		return dataSender;
	}

	@Bean
	public CoreController getCoreController(final ConnectionManager connectionManager, final Device device)
			throws SocketException, UnknownHostException {

		final CoreController coreController = new CoreController(NetworkUtils.retrieveLocalIP());
		coreController.setDevice(device);
		coreController.setConnectionManager(connectionManager);

		return coreController;
	}

	@Bean
	public ServerCoreController getServerCoreController(final ConnectionManager connectionManager, final Device device)
			throws SocketException, UnknownHostException {
		final ServerCoreController serverCoreController = new ServerCoreController(NetworkUtils.retrieveLocalIP());
		serverCoreController.setDevice(device);
		serverCoreController.setConnectionManager(connectionManager);

		return serverCoreController;
	}

	@Bean
	public DeviceManagementController getDeviceManagementController(final ConnectionManager connectionManager,
			final Device device) throws SocketException, UnknownHostException {
		final DeviceManagementController deviceManagementController = new DeviceManagementController(
				NetworkUtils.retrieveLocalIP());
		deviceManagementController.setDevice(device);
		deviceManagementController.setConnectionManager(connectionManager);

		return deviceManagementController;
	}

	@Bean
	public TunnelingController getTunnelingController(final ConnectionManager connectionManager, final Device device,
			final DataSender dataSender) throws SocketException, UnknownHostException {

		final TunnelingController tunnelingController = new TunnelingController(NetworkUtils.retrieveLocalIP());
		tunnelingController.setDevice(device);
		tunnelingController.setConnectionManager(connectionManager);
		tunnelingController.setDataSender(dataSender);

		return tunnelingController;
	}

	@Bean
	public MulticastListenerReaderThread getMulticastListenerReaderThread(final CoreController coreController,
			final ServerCoreController serverCoreController,
			final DeviceManagementController deviceManagementController, final TunnelingController tunnelingController,
			final Pipeline<Object, Object> inwardPipeline) {

		// reader for multicast messages
		final MulticastListenerReaderThread multicastListenerThread = new MulticastListenerReaderThread(
				BaseController.KNX_PORT_DEFAULT);
		multicastListenerThread.getDatagramPacketCallbacks().add(coreController);
		multicastListenerThread.getDatagramPacketCallbacks().add(serverCoreController);
		multicastListenerThread.getDatagramPacketCallbacks().add(deviceManagementController);
		multicastListenerThread.getDatagramPacketCallbacks().add(tunnelingController);
		multicastListenerThread.setInputPipeline(inwardPipeline);

		new Thread(multicastListenerThread).start();

		return multicastListenerThread;
	}

}
