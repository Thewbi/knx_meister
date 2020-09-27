package knx.server.configuration;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import api.configuration.ConfigurationManager;
import api.conversion.Converter;
import api.data.serializer.DataSerializer;
import api.datagenerator.DataGenerator;
import api.datagenerator.DefaultDataGeneratorFactory;
import api.device.Device;
import api.device.DeviceService;
import api.device.dto.DeviceDto;
import api.factory.Factory;
import api.factory.exception.FactoryException;
import api.pipeline.Pipeline;
import api.pipeline.PipelineStep;
import api.project.KNXComObject;
import api.project.ProjectService;
import api.project.dto.KNXComObjectDto;
import common.configuration.DefaultConfigurationManager;
import common.data.conversion.BitDataSerializer;
import common.data.conversion.DataConversion;
import common.data.conversion.Float16DataSerializer;
import common.data.conversion.UnsignedIntByteSerializer;
import common.packets.ServiceIdentifier;
import common.project.conversion.KNXComObjectKNXComObjectDtoConverter;
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
import core.devices.DefaultDeviceService;
import core.devices.conversion.DefaultDeviceDeviceDtoConverter;
import core.packets.KNXPacket;
import core.pipeline.DefaultPipeline;
import core.pipeline.InwardConnectionPipelineStep;
import core.pipeline.InwardConverterPipelineStep;
import core.pipeline.InwardOutputPipelineStep;
import core.pipeline.IpFilterPipelineStep;
import core.pipeline.OutwardConverterPipelineStep;
import core.pipeline.OutwardOutputPipelineStep;
import core.pipeline.SequenceNumberOutwardOutputPipelineStep;
import object_server.pipeline.ConverterPipelineStep;
import object_server.requests.BaseRequest;
import object_server.requests.DefaultRequestFactory;
import project.parsing.ProjectParser;
import project.parsing.factory.ProjectParserFactory;
import project.parsing.knx.KNXProjectParsingContext;
import project.service.DefaultProjectService;

@Component
public class Configuration {

    @SuppressWarnings("unused")
    private static final Logger LOG = LogManager.getLogger(Configuration.class);

    @Value("${knx.projectfile}")
    private String projectfile;

    @Value("${knx.ip}")
    private String ip;

    @Bean
    public ProjectService getProjectService(final ConfigurationManager configurationManager,
            final ProjectParser<KNXProjectParsingContext> projectParser) {

        final DefaultProjectService defaultProjectService = new DefaultProjectService();
        defaultProjectService.setConfigurationManager(configurationManager);
        defaultProjectService.setProjectParser(projectParser);

        return defaultProjectService;
    }

    @Bean
    public DeviceService getDeviceService() {
        final DefaultDeviceService deviceService = new DefaultDeviceService();
        return deviceService;
    }

    @Bean
    @Qualifier("defaultOutwardOutput")
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
    @Qualifier("sequenceOutwardOutput")
    public SequenceNumberOutwardOutputPipelineStep getSequenceNumberOutwardOutputPipelineStep() {

        final SequenceNumberOutwardOutputPipelineStep outwardOutputPipelineStep = new SequenceNumberOutwardOutputPipelineStep();
//        outwardOutputPipelineStep.setPrefix("MULTICAST");

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

    /**
     * Input: KNXPacket, output: java.net.DatagramPacket for sending over ethernet.
     *
     * @return
     */
    @Bean
    @Qualifier("knxToDatagramPacketOoutputConverter")
    public OutwardConverterPipelineStep getOutwardConverterPipelineStep() {
        final OutwardConverterPipelineStep outwardConverterPipelineStep = new OutwardConverterPipelineStep();
        return outwardConverterPipelineStep;
    }

    @Bean("outwardPipeline")
    @Qualifier("outwardPipeline")
    public Pipeline<Object, Object> getOutwardPipeline(
            @Qualifier("sequenceOutwardOutput") final PipelineStep<Object, Object> outwardOutputPipelineStep,
            @Qualifier("knxToDatagramPacketOoutputConverter") final OutwardConverterPipelineStep outwardConverterPipelineStep) {

        final Pipeline<Object, Object> outwardPipeline = new DefaultPipeline();
//        outwardPipeline.addStep(outwardOutputPipelineStep);
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
    public ProjectParser<KNXProjectParsingContext> getProjectParser() throws FactoryException {
        final ProjectParserFactory projectParserFactory = new ProjectParserFactory();
        return projectParserFactory.create();
    }

    @Bean
    public DefaultDataSender getDefaultDataSender(final ProjectService projectService,
            final DeviceService deviceService) throws IOException {

        final Map<String, DataSerializer<Object>> dataSerializerMap = new HashMap<>();
        dataSerializerMap.put(DataConversion.FLOAT16, new Float16DataSerializer());
        dataSerializerMap.put(DataConversion.BIT, new BitDataSerializer());
        dataSerializerMap.put(DataConversion.UNSIGNED_INTEGER_8, new UnsignedIntByteSerializer());

        final DefaultDataSender dataSender = new DefaultDataSender();
        dataSender.setDeviceService(deviceService);
        dataSender.setProjectService(projectService);
        dataSender.setDataSerializerMap(dataSerializerMap);

        return dataSender;
    }

    @Bean
    public CoreController getCoreController(final ConfigurationManager configurationManager,
            final ConnectionManager connectionManager, final DeviceService deviceService)
            throws SocketException, UnknownHostException {

        final CoreController coreController = new CoreController();
        coreController.setConfigurationManager(configurationManager);
        coreController.setDeviceService(deviceService);
        coreController.setConnectionManager(connectionManager);

        return coreController;
    }

    @Bean
    public ServerCoreController getServerCoreController(final ConfigurationManager configurationManager,
            final ConnectionManager connectionManager, final DeviceService deviceService)
            throws SocketException, UnknownHostException {

        final ServerCoreController serverCoreController = new ServerCoreController();
        serverCoreController.setConfigurationManager(configurationManager);
        serverCoreController.setDeviceService(deviceService);
        serverCoreController.setConnectionManager(connectionManager);

        return serverCoreController;
    }

    @Bean
    public DeviceManagementController getDeviceManagementController(final ConfigurationManager configurationManager,
            final ConnectionManager connectionManager, final DeviceService deviceService)
            throws SocketException, UnknownHostException {

        final DeviceManagementController deviceManagementController = new DeviceManagementController();
        deviceManagementController.setConfigurationManager(configurationManager);
        deviceManagementController.setLocalInetAddress(
                configurationManager.getPropertyAsString(ConfigurationManager.LOCAL_IP_CONFIG_KEY));
        deviceManagementController.setDeviceService(deviceService);
        deviceManagementController.setConnectionManager(connectionManager);

        return deviceManagementController;
    }

    @Bean
    public TunnelingController getTunnelingController(final ConfigurationManager configurationManager,
            final ConnectionManager connectionManager, final DeviceService deviceService, final DataSender dataSender)
            throws SocketException, UnknownHostException {

        final TunnelingController tunnelingController = new TunnelingController();
        tunnelingController.setConfigurationManager(configurationManager);
        tunnelingController.setLocalInetAddress(
                configurationManager.getPropertyAsString(ConfigurationManager.LOCAL_IP_CONFIG_KEY));
        tunnelingController.setDeviceService(deviceService);
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
    public Factory<BaseRequest> getRequestFactory(final ProjectService projectService) {

        final DefaultRequestFactory requestFactory = new DefaultRequestFactory();
        requestFactory.setProjectService(projectService);

        return requestFactory;
    }

    @Bean("objectServerInputPipeline")
    @Qualifier("objectServerInputPipeline")
    public Pipeline<Object, Object> getObjectServerInwardPipeline(final Factory<BaseRequest> requestFactory) {

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
    public ObjectServerReaderThread getObjectServerReaderThread(final ProjectService projectService,
            final Map<String, DataSerializer<Object>> dataSerializerMap,
            final Pipeline<Object, Object> objectServerInputPipeline, final ConfigurationManager configurationManager) {

        final ObjectServerReaderThread objectServerReaderThread = new ObjectServerReaderThread();
        objectServerReaderThread.setConfigurationManager(configurationManager);
        objectServerReaderThread.setProjectService(projectService);
        objectServerReaderThread.setDataSerializerMap(dataSerializerMap);
        objectServerReaderThread.setInputPipeline(objectServerInputPipeline);

        return objectServerReaderThread;
    }

    @Bean
    public ConfigurationManager getDefaultConfigurationManager() {

        final DefaultConfigurationManager defaultConfigurationManager = new DefaultConfigurationManager();
        defaultConfigurationManager.setProperty(ConfigurationManager.LOCAL_IP_CONFIG_KEY, ip);
        defaultConfigurationManager.setProperty(ConfigurationManager.PROJECT_FILE_KEY, projectfile);

        return defaultConfigurationManager;
    }

    @Bean
    public Converter<Device, DeviceDto> getDefaultDeviceDeviceDtoConverter() {
        final DefaultDeviceDeviceDtoConverter defaultDeviceDeviceDtoConverter = new DefaultDeviceDeviceDtoConverter();
        return defaultDeviceDeviceDtoConverter;
    }

    @Bean
    public Converter<KNXComObject, KNXComObjectDto> getKNXComObjectKNXComObjectDtoConverter() {
        final KNXComObjectKNXComObjectDtoConverter knxComObjectKNXComObjectDtoConverter = new KNXComObjectKNXComObjectDtoConverter();
        return knxComObjectKNXComObjectDtoConverter;
    }

    @Bean
    public Factory<DataGenerator> getDefaultDataGeneratorFactory() {
        return new DefaultDataGeneratorFactory();
    }

}
