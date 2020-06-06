package core.packets;

import core.common.Utils;

/**
 * Manufacturer (Mfr) Data
 *
 * 7.5.4.7 Manufacturer data DIB
 */
public class MfrDataDIB extends DescriptionInformationBlock {

	private int manufacturerId;

	private byte[] optionalData;

	@Override
	public DescriptionInformationBlockType getType() {
		return DescriptionInformationBlockType.MFR_DATA;
	}

	@Override
	public void fromBytes(final byte[] source, final int index) {

		int tempIndex = index;

//		// length
//		final int length = source[tempIndex++] & 0xFF;
//		setLength(length);
//
//		// type
//		final int mfrType = source[tempIndex++];
//		if (mfrType != DescriptionInformationBlockType.MFR_DATA.getValue()) {
//			throw new RuntimeException("Invalid type!");
//		}

		// manufacturer id (Weinzierl = 197d)
		manufacturerId = Utils.bytesToUnsignedShort(source[tempIndex++], source[tempIndex++], true);

		// todo optional data
		optionalData = new byte[4];
	}

	@Override
	public byte[] getBytes() {

		final byte[] bytes = new byte[8];

		setLength(8);

		bytes[0] = (byte) getLength();
		bytes[1] = (byte) DescriptionInformationBlockType.MFR_DATA.getValue();

		bytes[2] = (byte) ((manufacturerId >> 8) & 0xFF);
		bytes[3] = (byte) (manufacturerId & 0xFF);

		bytes[4] = (byte) 0x01;
		bytes[5] = (byte) 0x04;
		bytes[6] = (byte) 0xF0;
		bytes[7] = (byte) 0x20;

		return bytes;
	}

	@Override
	public String toString() {

		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Structure Length: ").append(getLength()).append("\n");
		stringBuilder.append("Description Type: ").append(getType().name()).append(" ")
				.append(Utils.integerToString(getType().getValue())).append("\n");
		stringBuilder.append("KNX Manufacturer Code: ").append(Utils.integerToString(manufacturerId));

		return stringBuilder.toString();
	}

	public int getManufacturerId() {
		return manufacturerId;
	}

	public void setManufacturerId(final int manufacturerId) {
		this.manufacturerId = manufacturerId;
	}

}
