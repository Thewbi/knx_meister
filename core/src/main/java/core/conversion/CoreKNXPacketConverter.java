package core.conversion;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import common.packets.KNXHeader;
import common.utils.Utils;
import core.packets.ConnectionRequestInformation;
import core.packets.ConnectionResponseDataBlock;
import core.packets.ConnectionStatus;
import core.packets.DescriptionInformationBlock;
import core.packets.HPAIStructure;
import core.packets.KNXPacket;
import core.packets.StructureType;

public class CoreKNXPacketConverter extends BaseKNXPacketConverter {

	private static final Logger LOG = LogManager.getLogger(CoreKNXPacketConverter.class);

	private final ByteArrayToStructureConverter byteArrayToStructureConverter = new ByteArrayToStructureConverter();

	private final ByteArrayToDIBConverter byteArrayToDIBConverter = new ByteArrayToDIBConverter();

	private final boolean acceptAll = false;

	@Override
	public void convert(final byte[] source, final KNXPacket knxPacket) {

		int index = 0;

		// header
		final KNXHeader header = knxPacket.getHeader();
		header.fromBytes(source, index);
		index += header.getLength();

		// validate, early out
		if (!accept(header)) {
			return;
		}

		DescriptionInformationBlock descriptionInformationBlock = null;
		ConnectionRequestInformation connectionRequestInformation = null;
		HPAIStructure dataEndpointHPAIStructure = null;

		// HPAI structure - Control Endpoint
		HPAIStructure structure = null;

		switch (header.getServiceIdentifier()) {

		case SEARCH_REQUEST_EXT:
		case SEARCH_RESPONSE_EXT:
			LOG.trace("KNXPacketConverter ignoring: " + header.getServiceIdentifier());
			LOG.trace(">>>>>>>>>> IGNORING " + knxPacket.getHeader().getServiceIdentifier().toString());
			break;

		case SEARCH_REQUEST:
			// HPAI structure - Control Endpoint
			structure = (HPAIStructure) byteArrayToStructureConverter.convert(source, index);
			knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, structure);
			index += structure.getLength();
			break;

		case SEARCH_RESPONSE:
			// HPAI structure - Control Endpoint
			structure = (HPAIStructure) byteArrayToStructureConverter.convert(source, index);
			knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, structure);
			index += structure.getLength();

			// device info DescriptionInformationBlock (DIB)
			descriptionInformationBlock = byteArrayToDIBConverter.convert(source, index);
			LOG.trace("Parsing device info DIB from : "
					+ Utils.integerToStringNoPrefix(source, index, descriptionInformationBlock.getLength()));
			index += descriptionInformationBlock.getLength();
			knxPacket.getDibMap().put(descriptionInformationBlock.getType(), descriptionInformationBlock);

			// supported service families - SuppSvcFamilies DescriptionInformationBlock
			// (DIB)
			descriptionInformationBlock = byteArrayToDIBConverter.convert(source, index);
			index += descriptionInformationBlock.getLength();
			knxPacket.getDibMap().put(descriptionInformationBlock.getType(), descriptionInformationBlock);

			// MfrData DescriptionInformationBlock (DIB)
			descriptionInformationBlock = byteArrayToDIBConverter.convert(source, index);

			index += descriptionInformationBlock.getLength();
			knxPacket.getDibMap().put(descriptionInformationBlock.getType(), descriptionInformationBlock);

			break;

		case DESCRIPTION_REQUEST:
			// HPAI structure - Control Endpoint
			structure = (HPAIStructure) byteArrayToStructureConverter.convert(source, index);
			knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, structure);
			index += structure.getLength();
			break;

		case DESCRIPTION_RESPONSE:
			// device info DescriptionInformationBlock (DIB)
			descriptionInformationBlock = byteArrayToDIBConverter.convert(source, index);
			index += descriptionInformationBlock.getLength();
			knxPacket.getDibMap().put(descriptionInformationBlock.getType(), descriptionInformationBlock);

			// supported service families - SuppSvcFamilies DescriptionInformationBlock
			// (DIB)
			descriptionInformationBlock = byteArrayToDIBConverter.convert(source, index);
			index += descriptionInformationBlock.getLength();
			knxPacket.getDibMap().put(descriptionInformationBlock.getType(), descriptionInformationBlock);

			// more optional stuff

			break;

		case CONNECT_REQUEST:
			// HPAI structure - Control Endpoint
			structure = (HPAIStructure) byteArrayToStructureConverter.convert(source, index);
			knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, structure);
			index += structure.getLength();

			// HPAI structure - Data Endpoint (First HPAI is the control endpoint)
			dataEndpointHPAIStructure = (HPAIStructure) byteArrayToStructureConverter.convert(source, index);
			knxPacket.getStructureMap().put(StructureType.HPAI_DATA_ENDPOINT_UDP, dataEndpointHPAIStructure);
			index += dataEndpointHPAIStructure.getLength();

			// Connection Request Information (CRI)
			connectionRequestInformation = (ConnectionRequestInformation) byteArrayToStructureConverter.convert(source,
					index);
			knxPacket.getStructureMap().put(connectionRequestInformation.getStructureType(),
					connectionRequestInformation);
			index += connectionRequestInformation.getLength();

			LOG.trace("Conn Type: " + connectionRequestInformation.getStructureType().name());
			LOG.trace("Conn Layer: " + connectionRequestInformation.getKnxLayer());
			break;

		case CONNECT_RESPONSE:

			final int communicationChannelId = source[index];
			LOG.info("communicationChannelId: " + communicationChannelId);
			knxPacket.setCommunicationChannelId(communicationChannelId);
			index++;

			final ConnectionStatus connectionStatus = ConnectionStatus.fromInt(source[index]);
			LOG.info("connectionStatus: " + connectionStatus);
			knxPacket.setConnectionStatus(connectionStatus);
			index++;

			// HPAI structure - Data Endpoint (First HPAI is the control endpoint)
			dataEndpointHPAIStructure = (HPAIStructure) byteArrayToStructureConverter.convert(source, index);
			knxPacket.getStructureMap().put(StructureType.HPAI_DATA_ENDPOINT_UDP, dataEndpointHPAIStructure);
			index += dataEndpointHPAIStructure.getLength();

			final ConnectionResponseDataBlock connectionResponseDataBlock = new ConnectionResponseDataBlock();
			connectionResponseDataBlock.fromBytes(source, index);
			index += connectionResponseDataBlock.getLength();

			break;

		case CONNECTIONSTATE_REQUEST:

			// communication channel
			knxPacket.setCommunicationChannelId(source[index++]);

			// skip reserved byte
			index++;

			// HPAI structure - Control Endpoint
			structure = (HPAIStructure) byteArrayToStructureConverter.convert(source, index);
			knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, structure);
			index += structure.getLength();
			break;

		case DISCONNECT_REQUEST:
			// communication channel
			knxPacket.setCommunicationChannelId(source[index++]);

			// skip reserved byte
			index++;

			// HPAI structure - Control Endpoint
			structure = (HPAIStructure) byteArrayToStructureConverter.convert(source, index);
			knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, structure);
			index += structure.getLength();
			break;

		default:
			throw new RuntimeException("Unknown type: " + header.getServiceIdentifier());
		}
	}

	@Override
	public boolean accept(final KNXHeader header) {

		if (acceptAll) {
			return true;
		}

		switch (header.getServiceIdentifier()) {
		case SEARCH_REQUEST_EXT:
		case SEARCH_RESPONSE_EXT:
		case SEARCH_REQUEST:
		case SEARCH_RESPONSE:
		case DESCRIPTION_REQUEST:
		case DESCRIPTION_RESPONSE:
		case CONNECT_REQUEST:
		case CONNECT_RESPONSE:
		case CONNECTIONSTATE_REQUEST:
		case DISCONNECT_REQUEST:
			return true;

		default:
			return false;
		}
	}

	@Override
	protected Logger getLogger() {
		return LOG;
	}

}
