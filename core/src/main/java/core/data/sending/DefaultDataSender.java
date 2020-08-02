package core.data.sending;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.data.serializer.DataSerializer;
import api.device.Device;
import api.project.KNXDatapointSubtype;
import api.project.KNXGroupAddress;
import api.project.KNXProject;
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

	private Device device;

	private Connection connection;

	private KNXProject knxProject;

	private Map<String, DataSerializer<Object>> dataSerializerMap = new HashMap<>();

	@Override
	public void send(final Connection connection) {

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
		final KNXGroupAddress knxGroupAddress = getDevice().getDeviceProperties().get("0/1/1");
		if (knxGroupAddress == null) {
			LOG.warn("GroupAddress is unknown: " + knxGroupAddress);
			return;
		}

		final String dataPointType = knxGroupAddress.getDataPointType();
		final KNXDatapointSubtype knxDatapointSubtype = knxProject.getDatapointSubtypeMap().get(dataPointType);
		final DataSerializer<Object> dataSerializer = dataSerializerMap.get(knxDatapointSubtype.getFormat());

//		sendBit(connection, knxGroupAddress, device.getValue());

//		// ETS-File: C:\Users\U5353\Desktop\KNX_IP_BAOS_777.knxproj
//		//
//		// 0/1/1 - Temperatur ist
//		// Datatype: 9.xxx 1 2-Byte Gleitkommawert, Temperatur (°C) Float16
//		final KNXGroupAddress knxGroupAddress = new KNXGroupAddress();
//		knxGroupAddress.setGroupAddress("0/1/1");
//		final String comObjectId = "O-145_R-1849";
		final int dataPointId = 325;
		double value = 100.0d;

		if (knxGroupAddress.getValue() == null) {
			knxGroupAddress.setValue(value);
		} else {
			value = (double) knxGroupAddress.getValue();
		}

		sendViaComObject(connection, dataPointId, value);

//		sendViaFormat(connection, DataSender.BIT, knxGroupAddress, 0);
//		sendBit(connection, knxGroupAddress, currentValue);

		// toggle
//		currentValue = 1 - currentValue;
//		device.setValue(1 - device.getValue());

		knxGroupAddress.setValue(value + 1.0d);
	}

	@SuppressWarnings("unused")
	private void sendViaComObject(final Connection connection, final int datapointId, final double value) {

		final KNXGroupAddress knxGroupAddress = KNXProjectUtils.retrieveGroupAddress(knxProject, datapointId);
		final KNXDatapointSubtype knxDatapointSubtype = KNXProjectUtils.retrieveDataPointSubType(knxProject,
				datapointId);

		// from the datapoint subtype, retrieve the datapoint type
		final String format = knxDatapointSubtype.getFormat();

		sendViaFormat(connection, format, knxGroupAddress, value);
	}

	private void sendViaFormat(final Connection connection, final String format, final KNXGroupAddress knxGroupAddress,
			final double value) {

		// retrieve the data serializer that can convert the data into the datapoint
		// type's format
		final DataSerializer<Object> dataSerializer = dataSerializerMap.get(format);
		if (dataSerializer == null) {
			throw new RuntimeException("No serializer for format \"" + format + "\" registered!");
		}
		final byte[] payload = dataSerializer.serializeToBytes(value);

		LOG.info("[DefaultDataSender] sending: {}", Utils.byteArrayToStringNoPrefix(payload));

		final KNXConnectionHeader connectionHeader = new KNXConnectionHeader();

		final CemiTunnelRequest cemiTunnelRequest = new CemiTunnelRequest();
		cemiTunnelRequest.setMessageCode((short) 0x29);
		cemiTunnelRequest.setAdditionalInfoLength(0);
		cemiTunnelRequest.setCtrl1(0xB0);
		cemiTunnelRequest.setCtrl2(0xE0);
		cemiTunnelRequest.setSourceKNXAddress(getDevice().getPhysicalAddress());
		cemiTunnelRequest.setDestKNXAddress(Utils.knxAddressToInteger(knxGroupAddress.getGroupAddress()));
		cemiTunnelRequest.setLength(3);
		cemiTunnelRequest.setTpci(0x00);
		cemiTunnelRequest.setApci(0x80);

		// TODO: the one bit datatypes are encoded directly into APCI !!!!!!!!!!!!!!!!
		cemiTunnelRequest.setPayloadBytes(payload);

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

		final CemiTunnelRequest cemiTunnelRequest = new CemiTunnelRequest();
//		cemiTunnelRequest.setMessageCode(BaseController.CONFIRM_PRIMITIVE);
		cemiTunnelRequest.setMessageCode(BaseController.INDICATION_PRIMITIVE);
//		cemiTunnelRequest.setMessageCode(BaseController.REQUEST_PRIMITIVE);
		cemiTunnelRequest.setAdditionalInfoLength(0);
		cemiTunnelRequest.setCtrl1(0xBC);
		cemiTunnelRequest.setCtrl2(0xE0);
		cemiTunnelRequest.setSourceKNXAddress(NetworkUtils.toNetworkOrder((short) getDevice().getPhysicalAddress()));
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

	public Device getDevice() {
		return device;
	}

	public void setDevice(final Device device) {
		this.device = device;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(final Connection connection) {
		this.connection = connection;
	}

	public KNXProject getKnxProject() {
		return knxProject;
	}

	public void setKnxProject(final KNXProject knxProject) {
		this.knxProject = knxProject;
	}

	public Map<String, DataSerializer<Object>> getDataSerializerMap() {
		return dataSerializerMap;
	}

	public void setDataSerializerMap(final Map<String, DataSerializer<Object>> dataSerializerMap) {
		this.dataSerializerMap = dataSerializerMap;
	}

}
