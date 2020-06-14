package core.data.sending;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.api.device.Device;
import core.common.Utils;
import core.communication.Connection;
import core.data.serializer.DataSerializer;
import core.data.serializer.Float16DataSerializer;
import core.packets.CemiTunnelRequest;
import core.packets.ConnectionHeader;
import core.packets.KNXPacket;
import core.packets.ServiceIdentifier;
import project.parsing.domain.KNXComObject;
import project.parsing.domain.KNXDatapointSubtype;
import project.parsing.domain.KNXDeviceInstance;
import project.parsing.domain.KNXGroupAddress;
import project.parsing.domain.KNXProject;

public class DefaultDataSender implements DataSender {

	private static final Logger LOG = LogManager.getLogger(DefaultDataSender.class);

	private Device device;

	private Connection connection;

	private KNXProject knxProject;

	private final Map<String, DataSerializer<Object>> dataSerializerMap = new HashMap<>();

	/**
	 * ctor
	 */
	public DefaultDataSender() {
		dataSerializerMap.put("Float16", new Float16DataSerializer());
	}

	@Override
	public void send(final Connection connection) {

		// how to identify the correct device if there are several devices in the list?
		final KNXDeviceInstance knxDeviceInstance = knxProject.getDeviceInstances().get(0);

		// pick one of the communication objects by its name/id
		final KNXComObject knxComObject = knxDeviceInstance.getComObjects().get(145);

		// retrieve the group address to send the data to
		final KNXGroupAddress knxGroupAddress = knxComObject.getKnxGroupAddress();

		// the group address also stores the datatype. The data has to be send in this
		// specific datatype so the receiver can decode it correctly
		final String dataPointType = knxGroupAddress.getDataPointType();

		// retrieve the datapoint subtype because the datapoint subtype stores datapoint
		// type
		final KNXDatapointSubtype knxDatapointSubtype = knxProject.getDatapointSubtypeMap().get(dataPointType);

		// from the datapoint subtype, retrieve the datapoint type
//		final KNXDatapointType knxDatapointType = knxDatapointSubtype.getKnxDatapointType();
		final String format = knxDatapointSubtype.getFormat();

		// retrieve the data serializer that can convert the data into the datapoint
		// type's format
		final DataSerializer<Object> dataSerializer = dataSerializerMap.get(format);
		if (dataSerializer == null) {
			throw new RuntimeException("No serializer for format \"" + format + "\" registered!");
		}

		final ConnectionHeader connectionHeader = new ConnectionHeader();

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
//		cemiTunnelRequest.setPayloadBytes(new byte[] { 0x05, 0x02 });
		cemiTunnelRequest.setPayloadBytes(dataSerializer.serializeToBytes(100.0d));

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

}
