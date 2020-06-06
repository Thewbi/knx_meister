package core.conversion;

import core.common.Converter;
import core.common.Utils;
import core.packets.DescriptionInformationBlock;
import core.packets.DescriptionInformationBlockType;
import core.packets.DeviceInformationDIB;
import core.packets.MfrDataDIB;
import core.packets.SuppSvcFamiliesDIB;

public class ByteArrayToDIBConverter implements Converter<byte[], DescriptionInformationBlock> {

	@Override
	public void convert(final byte[] source, final DescriptionInformationBlock target) {
		throw new RuntimeException("Not implemented exception!");
	}

	@Override
	public DescriptionInformationBlock convert(final byte[] source) {
		throw new RuntimeException("Not implemented exception!");
	}

	public DescriptionInformationBlock convert(final byte[] source, final int startIndex) {

		int index = startIndex;

		// length
		int length = source[index];
		index++;
		if (length == 0xFF) {
			length = Utils.bytesToUnsignedShort(source[index++], source[index++], true);
		}

		// type
		final int type = Utils.bytesToUnsignedShort((byte) 0x00, source[index++], true);

		DescriptionInformationBlock descriptionInformationBlock = null;

		switch (DescriptionInformationBlockType.fromInt(type)) {

		case DEVICE_INFO:
			descriptionInformationBlock = new DeviceInformationDIB();
			break;

		case SUPP_SVC_FAMILIES:
			descriptionInformationBlock = new SuppSvcFamiliesDIB();
			break;

		case MFR_DATA:
			descriptionInformationBlock = new MfrDataDIB();
			break;

		default:
			throw new RuntimeException("Unkown type: " + source[index - 1]);
		}
		descriptionInformationBlock.setLength(length);
		descriptionInformationBlock.fromBytes(source, index);

		return descriptionInformationBlock;
	}

}
