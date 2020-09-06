package core.data.sending;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.data.serializer.DataSerializer;
import api.device.Device;
import api.device.DeviceService;
import api.project.KNXComObject;
import api.project.KNXDatapointSubtype;
import api.project.KNXGroupAddress;
import api.project.ProjectService;
import common.packets.KNXConnectionHeader;
import common.packets.ServiceIdentifier;
import common.utils.KNXProjectUtils;
import common.utils.NetworkUtils;
import common.utils.Utils;
import core.communication.Connection;
import core.communication.controller.BaseController;
import core.packets.CemiTunnelRequest;
import core.packets.KNXPacket;

public class DefaultDataSender implements DataSender {

    private static final Logger LOG = LogManager.getLogger(DefaultDataSender.class);

//    private Device device;

    private DeviceService deviceService;

    private Connection connection;

//	private KNXProject knxProject;

    private ProjectService projectService;

    private Map<String, DataSerializer<Object>> dataSerializerMap = new HashMap<>();

    @Override
    public void send(final Device device, final Connection connection, final String physicalAddress,
            final String groupAddress, final int dataPointId, final Object value) {

        LOG.info("[DefaultDataSender] send() ...");

//		final String comObjectId = "O-145_R-1849";
//		final double value = 100.0d;
//		sendViaComObject(connection, comObjectId, value);

//		final KNXGroupAddress knxGroupAddress = new KNXGroupAddress();
////		knxGroupAddress.setGroupAddress(currentValue == 0 ? "0/3/4" : "0/3/3");
////		knxGroupAddress.setGroupAddress("0/3/4");
////		knxGroupAddress.setGroupAddress("7/7/2");
////		knxGroupAddress.setGroupAddress("0/4/1");
//		knxGroupAddress.setGroupAddress("0/1/1");

//		final String integerToKNXAddress = Utils
//				.integerToKNXAddress(knxPacket.getCemiTunnelRequest().getDestKNXAddress(), "/");

//		final String physicalAddress = "0/1/1";
//		final String physicalAddress = "0/0/6";
//		final String physicalAddress = "0/0/9";

        // TODO
        LOG.info("Physical Address: '{}', groupAddress: '{}'", physicalAddress, groupAddress);
//        final Device device = deviceService.getDevices().get(physicalAddress);
        final KNXGroupAddress knxGroupAddress = device.getDeviceProperties().get(groupAddress);
        if (knxGroupAddress == null) {
            LOG.warn(
                    "Tried to find GroupAddress for PhysicalAddressGroupAddress '{}' but was unknown: '{}'. Cannot send data!",
                    physicalAddress, knxGroupAddress);
            return;
        }

//		final String dataPointType = knxGroupAddress.getDataPointType();
//		final KNXDatapointSubtype knxDatapointSubtype = knxProject.getDatapointSubtypeMap().get(dataPointType);
//		final DataSerializer<Object> dataSerializer = dataSerializerMap.get(knxDatapointSubtype.getFormat());

//		sendBit(connection, knxGroupAddress, device.getValue());

//		// ETS-File: C:\Users\U5353\Desktop\KNX_IP_BAOS_777.knxproj
//		//
//		// 0/1/1 - Temperatur ist
//		// Datatype: 9.xxx 1 2-Byte Gleitkommawert, Temperatur (°C) Float16
//		final KNXGroupAddress knxGroupAddress = new KNXGroupAddress();
//		knxGroupAddress.setGroupAddress("0/1/1");
//		final String comObjectId = "O-145_R-1849";

//		LOG.info("KNXGroupAddress.id = " + knxGroupAddress.getId());

//		final int dataPointId = 325;
//		final int dataPointId = 17;
//		double value = 100.0d;

//		if (knxGroupAddress.getValue() == null) {
//			knxGroupAddress.setValue(value);
//		} else {
//			value = (double) knxGroupAddress.getValue();
//		}

        sendViaComObject(device, connection, dataPointId, value);

//		sendViaFormat(connection, DataSender.BIT, knxGroupAddress, 0);
//		sendBit(connection, knxGroupAddress, currentValue);

        // toggle
//		currentValue = 1 - currentValue;
//		device.setValue(1 - device.getValue());

        // increment value
//		knxGroupAddress.setValue(value + 1.0d);
    }

    @SuppressWarnings("unused")
    private void sendViaComObject(final Device device, final Connection connection, final int datapointId,
            final Object value) {

        // TODO
//        final Device device = deviceService.getDevices().get("");
        final KNXComObject knxComObject = device.getComObjectsByDatapointId().get(datapointId);
        final KNXGroupAddress knxGroupAddress = knxComObject.getKnxGroupAddress();
        final String dataPointType = knxGroupAddress.getDataPointType();
        final KNXDatapointSubtype knxDatapointSubtype = KNXProjectUtils
                .retrieveDataPointSubType(projectService.getProject(), device, datapointId);

////		final int deviceIndex = 0;
//		final KNXGroupAddress knxGroupAddress = KNXProjectUtils.retrieveGroupAddress(knxProject, deviceIndex,
//				datapointId);
//		final KNXDatapointSubtype knxDatapointSubtype = KNXProjectUtils.retrieveDataPointSubType(knxProject,
//				deviceIndex, datapointId);

        // from the datapoint subtype, retrieve the datapoint type
        final String format = knxDatapointSubtype.getFormat();

        sendViaFormat(connection, device, format, knxGroupAddress, value);
    }

    private void sendViaFormat(final Connection connection, final Device device, final String format,
            final KNXGroupAddress knxGroupAddress, final Object value) {

        // retrieve the data serializer that can convert the data into the datapoint
        // type's format
        final DataSerializer<Object> dataSerializer = dataSerializerMap.get(format);
        if (dataSerializer == null) {
            throw new RuntimeException("No serializer for format \"" + format + "\" registered!");
        } else {
            LOG.trace("Using serializer: '{}' for format '{}'", dataSerializer.getClass(), format);
        }
        final byte[] payload = dataSerializer.serializeToBytes(value);

        LOG.info("[DefaultDataSender] sending value '{}' encoded as '{}' to address: '{}'", value,
                Utils.byteArrayToStringNoPrefix(payload), knxGroupAddress.getGroupAddress());

        final KNXConnectionHeader connectionHeader = new KNXConnectionHeader();

        // TODO
//        final Device device = deviceService.getDevices().get("");

        final CemiTunnelRequest cemiTunnelRequest = new CemiTunnelRequest();
        cemiTunnelRequest.setMessageCode(BaseController.INDICATION_PRIMITIVE);
        cemiTunnelRequest.setAdditionalInfoLength(0);
        cemiTunnelRequest.setCtrl1(PRIORITY_LOW);
        cemiTunnelRequest.setCtrl2(HOP_COUNT_6);
        cemiTunnelRequest.setSourceKNXAddress(device.getPhysicalAddress());
//		cemiTunnelRequest.setSourceKNXAddress(getDevice().getHostPhysicalAddress());
        cemiTunnelRequest.setDestKNXAddress(Utils.knxAddressToInteger(knxGroupAddress.getGroupAddress()));
//		cemiTunnelRequest.setLength(1 + payload.length);
        cemiTunnelRequest.setLength(0);
        cemiTunnelRequest.setTpci(0x00);
        cemiTunnelRequest.setApci(0x80);

        // TODO: the one bit datatypes are encoded directly into APCI !!!!!!!!!!!!!!!!
        // Also the one byte datatypes are encoded directly into APCI !!!!!!!!!!!!!!!!
        if (format.equalsIgnoreCase("UnsignedInteger8")) {

            cemiTunnelRequest.setLength(1);

//			// default value is zero
//			int temp = 0;
//
//			// if a specific value is given, use that specific value
//			if (knxGroupAddress.getValue() != null) {
//				temp = (int) knxGroupAddress.getValue();
//			}

            // response bit + value
            // TODO: answer with the correct data type
//			cemiTunnelRequest.setApci(0x40 | ((byte) temp & 0xFF));

            cemiTunnelRequest.setApci(0x40 | (payload[0] & 0xFF));

        } else {

            if (ArrayUtils.isNotEmpty(payload)) {
                cemiTunnelRequest.setLength(2 + payload.length);
                cemiTunnelRequest.setPayloadBytes(payload);
            }

        }

//		final String dataPointType = knxGroupAddress.getDataPointType();
//		if (StringUtils.equalsAnyIgnoreCase(dataPointType, "DPST-1-1")) {
//
//			// default value is zero
//			int temp = 0;
//
//			// if a specific value is given, use that specific value
//			if (knxGroupAddress.getValue() != null) {
//				temp = (int) knxGroupAddress.getValue();
//			}
//
//			// response bit + value
//			// TODO: answer with the correct data type
//			cemiTunnelRequest.setApci(0x40 | ((byte) temp & 0xFF));
//
//		} else {
//			throw new RuntimeException("dataPointType: " + dataPointType + " not implemented yet!");
//		}

        final KNXPacket knxPacket = new KNXPacket();
        knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_REQUEST);
        knxPacket.setConnectionHeader(connectionHeader);
        knxPacket.setCemiTunnelRequest(cemiTunnelRequest);

        try {
            connection.sendData(knxPacket);
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unused")
    private void sendBit(final Connection connection, final KNXGroupAddress knxGroupAddress, final int value) {

        LOG.info("Sending BIT Value: " + value);

        final KNXConnectionHeader connectionHeader = new KNXConnectionHeader();

        // TODO
        final Device device = deviceService.getDevices().get("");

        final CemiTunnelRequest cemiTunnelRequest = new CemiTunnelRequest();
//		cemiTunnelRequest.setMessageCode(BaseController.CONFIRM_PRIMITIVE);
        cemiTunnelRequest.setMessageCode(BaseController.INDICATION_PRIMITIVE);
//		cemiTunnelRequest.setMessageCode(BaseController.REQUEST_PRIMITIVE);
        cemiTunnelRequest.setAdditionalInfoLength(0);
        cemiTunnelRequest.setCtrl1(0xBC);
        cemiTunnelRequest.setCtrl2(0xE0);
        cemiTunnelRequest.setSourceKNXAddress(NetworkUtils.toNetworkOrder((short) device.getPhysicalAddress()));
//		cemiTunnelRequest.setSourceKNXAddress(0x110B);
//		cemiTunnelRequest.setSourceKNXAddress(0x11FF);
//		cemiTunnelRequest.setSourceKNXAddress(0x0000);
        cemiTunnelRequest.setDestKNXAddress(Utils.knxAddressToInteger(knxGroupAddress.getGroupAddress()));
        cemiTunnelRequest.setLength(1);
        cemiTunnelRequest.setTpci(0x00);
        cemiTunnelRequest.setApci(value == 0 ? 0x80 : 0x81);
//		cemiTunnelRequest.setPayloadBytes(dataSerializer.serializeToBytes(value));

        final KNXPacket knxPacket = new KNXPacket();
        knxPacket.getHeader().setServiceIdentifier(ServiceIdentifier.TUNNEL_REQUEST);
        knxPacket.setConnectionHeader(connectionHeader);
        knxPacket.setCemiTunnelRequest(cemiTunnelRequest);

        try {
            connection.sendData(knxPacket);
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public Object deserializeByFormat() {
        return null;
    }

//    public Device getDevice() {
//        return device;
//    }
//
//    public void setDevice(final Device device) {
//        this.device = device;
//    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(final Connection connection) {
        this.connection = connection;
    }

//    public KNXProject getKnxProject() {
//        return knxProject;
//    }
//
//    public void setKnxProject(final KNXProject knxProject) {
//        this.knxProject = knxProject;
//    }

    public Map<String, DataSerializer<Object>> getDataSerializerMap() {
        return dataSerializerMap;
    }

    public void setDataSerializerMap(final Map<String, DataSerializer<Object>> dataSerializerMap) {
        this.dataSerializerMap = dataSerializerMap;
    }

    public void setProjectService(final ProjectService projectService) {
        this.projectService = projectService;
    }

    public void setDeviceService(final DeviceService deviceService) {
        this.deviceService = deviceService;
    }

}
