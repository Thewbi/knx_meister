package core.conversion;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.common.Converter;
import core.common.Utils;
import core.packets.ConnectionRequestInformation;
import core.packets.DescriptionInformationBlock;
import core.packets.HPAIStructure;
import core.packets.Header;
import core.packets.KNXPacket;
import core.packets.StructureType;

public class KNXPacketConverter implements Converter<byte[], KNXPacket> {

	private static final Logger LOG = LogManager.getLogger("KNXPacketConverter");

	private final ByteArrayToStructureConverter byteArrayToStructureConverter = new ByteArrayToStructureConverter();

	private final ByteArrayToDIBConverter byteArrayToDIBConverter = new ByteArrayToDIBConverter();

	@Override
	public void convert(final byte[] source, final KNXPacket knxPacket) {

		int index = 0;

		final Header header = knxPacket.getHeader();

		// parse the header
//		System.out.println("Parsing header from: " + Utils.integerToStringNoPrefix(source, index, 6));
		header.fromBytes(source, index);
		index += knxPacket.getHeader().getLength();

		DescriptionInformationBlock descriptionInformationBlock = null;

//		// HPAI structure - Control Endpoint
		HPAIStructure structure = null;
//		final HPAIStructure structure = (HPAIStructure) byteArrayToStructureConverter.convert(source, index);
//		knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, structure);
//		index += structure.getLength();

//		String ip = retrieveIPFromHPAI(structure);
//		LOG.info("<<<<<<<<<< " + knxPacket.getHeader().getServiceIdentifier().toString() + " from " + ip);

		switch (header.getServiceIdentifier()) {

		case SEARCH_REQUEST_EXT:
		case SEARCH_RESPONSE_EXT:
//			System.out.println("KNXPacketConverter ignoring: " + header.getServiceIdentifier());
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
//			System.out.println("Parsing HPAI structure from: "
//					+ Utils.integerToStringNoPrefix(source, index, structure.getLength()));
			index += structure.getLength();

			// device info DescriptionInformationBlock (DIB)
			descriptionInformationBlock = byteArrayToDIBConverter.convert(source, index);
			System.out.println("Parsing device info DIB from : "
					+ Utils.integerToStringNoPrefix(source, index, descriptionInformationBlock.getLength()));
			index += descriptionInformationBlock.getLength();
			knxPacket.getDibMap().put(descriptionInformationBlock.getType(), descriptionInformationBlock);

			// supported service families - SuppSvcFamilies DescriptionInformationBlock
			// (DIB)
			descriptionInformationBlock = byteArrayToDIBConverter.convert(source, index);
//			System.out.println("Parsing SuppSvcFamilies DIB from index : " + index + " => "
//					+ Utils.integerToStringNoPrefix(source, index, descriptionInformationBlock.getLength()));
			index += descriptionInformationBlock.getLength();
			knxPacket.getDibMap().put(descriptionInformationBlock.getType(), descriptionInformationBlock);

			// MfrData DescriptionInformationBlock (DIB)
//			System.out.println("Parsing MfrData DIB from index: " + index + " => "
//					+ Utils.integerToStringNoPrefix(source, index, 8));
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
//			// HPAI structure - Control Endpoint
//			structure = (HPAIStructure) byteArrayToStructureConverter.convert(source, index);
//			knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, structure);
//			index += structure.getLength();

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
			final HPAIStructure dataEndpointHPAIStructure = (HPAIStructure) byteArrayToStructureConverter
					.convert(source, index);
			knxPacket.getStructureMap().put(StructureType.HPAI_DATA_ENDPOINT_UDP, dataEndpointHPAIStructure);
			index += dataEndpointHPAIStructure.getLength();

			// Connection Request Information (CRI)
			final ConnectionRequestInformation connectionRequestInformation = (ConnectionRequestInformation) byteArrayToStructureConverter
					.convert(source, index);
			knxPacket.getStructureMap().put(connectionRequestInformation.getStructureType(),
					connectionRequestInformation);
			index += connectionRequestInformation.getLength();

			LOG.trace("Conn Type: " + connectionRequestInformation.getStructureType().name());
			LOG.trace("Conn Layer: " + connectionRequestInformation.getKnxLayer());
			break;

		case CONNECT_RESPONSE:
			break;

		case CONNECTIONSTATE_REQUEST:
			knxPacket.setCommunicationChannelId(source[index++]);

			LOG.info("Connecting to channel: " + knxPacket.getCommunicationChannelId());

			final int reserved = source[index++];

			// HPAI structure - Control Endpoint
			structure = (HPAIStructure) byteArrayToStructureConverter.convert(source, index);
			knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, structure);
			index += structure.getLength();
			break;

		case DISCONNECT_REQUEST:
			knxPacket.setCommunicationChannelId(source[index++]);

			LOG.info("Disconnecting from channel: " + knxPacket.getCommunicationChannelId());

			final int reserved2 = source[index++];

			// HPAI structure - Control Endpoint
			structure = (HPAIStructure) byteArrayToStructureConverter.convert(source, index);
			knxPacket.getStructureMap().put(StructureType.HPAI_CONTROL_ENDPOINT_UDP, structure);
			index += structure.getLength();
			break;

		case TUNNEL_REQUEST:
			throw new RuntimeException("test");

		default:
			throw new RuntimeException("Unknown type: " + header.getServiceIdentifier());
		}
	}

	private String retrieveIPFromHPAI(final HPAIStructure structure) {
		String ip = "unknown";
		if (structure != null && structure instanceof HPAIStructure) {
			final HPAIStructure hpaiStructure = structure;
			final byte[] ipAddress = hpaiStructure.getIpAddress();
			ip = (ipAddress[0] & 0xFF) + "." + (ipAddress[1] & 0xFF) + "." + (ipAddress[2] & 0xFF) + "."
					+ (ipAddress[3] & 0xFF);
		}
		return ip;
	}

	@Override
	public KNXPacket convert(final byte[] source) {
		final KNXPacket result = new KNXPacket();
		convert(source, result);

		return result;
	}

}
