package core.conversion;

import common.utils.Utils;
import core.common.Converter;
import core.packets.ConnectionRequestInformation;
import core.packets.HPAIStructure;
import core.packets.Structure;
import core.packets.StructureType;

public class ByteArrayToStructureConverter implements Converter<byte[], Structure> {

	@Override
	public void convert(final byte[] source, final Structure target) {
		throw new RuntimeException("Not implemented exception!");
	}

	@Override
	public Structure convert(final byte[] source) {
		throw new RuntimeException("Not implemented exception!");
	}

	public Structure convert(final byte[] source, final int startIndex) {

		int index = startIndex;

		// length
		int length = source[index];
		index++;
		if (length == 0xFF) {
			length = Utils.bytesToUnsignedShort(source[index++], source[index++], true);
		}

		Structure structure = null;

		// type
		final StructureType structureType = StructureType.fromInt(source[index++]);
		switch (structureType) {
		case HPAI_CONTROL_ENDPOINT_UDP:
			structure = new HPAIStructure();
			break;

		case TUNNELING_CONNECTION:
			structure = new ConnectionRequestInformation();
			break;

		case DEVICE_MGMT_CONNECTION:
			structure = new ConnectionRequestInformation();
			break;

		default:
			throw new RuntimeException("Unkown type: " + source[index - 1]);
		}

		structure.setLength(length);
		structure.setStructureType(structureType);
		structure.fromBytes(source, index);

		return structure;
	}

}
