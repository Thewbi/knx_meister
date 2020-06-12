package core.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

public final class Utils {

	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

	private Utils() {
		// no instances of this class
	}

	/**
	 * Converts an integer to a string which is the value in hex format.
	 *
	 * @param value
	 * @return
	 */
	public static String integerToString(final int value) {
		return "0x" + String.format("%1$02X", value).toUpperCase(Locale.getDefault());
	}

	public static String integerToStringNoPrefix(final int value) {
		return String.format("%1$02X", value).toUpperCase(Locale.getDefault());
	}

	public static String integerToStringNoPrefix(final byte[] data) {
		if (data == null) {
			return StringUtils.EMPTY;
		}
		final StringBuffer stringBuffer = new StringBuffer();
		for (final byte tempByte : data) {
			stringBuffer.append(String.format("%1$02X", tempByte).toUpperCase(Locale.getDefault())).append(" ");
		}
		return stringBuffer.toString();
	}

	public static String integerToStringNoPrefix(final byte[] data, final int index, final int length) {
		if (data == null) {
			return StringUtils.EMPTY;
		}
		final StringBuffer stringBuffer = new StringBuffer();
		for (int i = index; i < index + length; i++) {
			stringBuffer.append(String.format("%1$02X", data[i]).toUpperCase(Locale.getDefault())).append(" ");
		}
		return stringBuffer.toString();
	}

	public static String integerToStringDecimalNoPrefix(final byte[] data) {
		if (data == null) {
			return StringUtils.EMPTY;
		}
		final StringBuffer stringBuffer = new StringBuffer();
		for (final byte tempByte : data) {
			final int tempByteUnsigned = tempByte & 0xFF;
			stringBuffer.append(String.format("%d", tempByteUnsigned).toUpperCase(Locale.getDefault())).append(" ");
		}
		return stringBuffer.toString();
	}

	public static int bytesToUnsignedShort(final byte byte1, final byte byte2, final boolean bigEndian) {
		if (bigEndian) {
			return (((byte1 & 0xFF) << 8) | (byte2 & 0xFF));
		}
		return (((byte2 & 0xFF) << 8) | (byte1 & 0xFF));
	}

	/**
	 * Example:
	 *
	 * <pre>
	 * final byte[] hexStringToByteArray = Utils.hexStringToByteArray(
	 * 		"360102001101000000c50102d84ce000170c00246d01d80a4b4e582049502042414f5320373737000000000000000000000000000000");
	 * </pre>
	 *
	 * @param s
	 * @return
	 */
	public static byte[] hexStringToByteArray(final String s) {
		final int len = s.length();
		final byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	public static String retrieveCurrentTimeAsString() {
		final LocalDateTime now = LocalDateTime.now();

		return dateTimeFormatter.format(now);
	}

	public static String integerToKNXAddress(final int individualAddress) {

		final int upperByte = ((individualAddress >> 8) & 0xFF);
		final int areaAddress = (upperByte & 0xF0) >> 4;
		final int lineAddress = upperByte & 0x0F;

		final int deviceAddress = ((individualAddress) & 0xFF);

		return areaAddress + "." + lineAddress + "." + deviceAddress;
	}

	public static int knxAddressToInteger(final String addressAsString) {

		final String[] split = StringUtils.split(addressAsString, "./");

		final int areaAddress = Integer.parseInt(split[0]);

		final int lineAddress = Integer.parseInt(split[1]);

		final int deviceAddress = Integer.parseInt(split[2]);

		return (areaAddress << 12) + (lineAddress << 8) + deviceAddress;
	}

	public static byte[] shortToByteArray(final short data) {

		int index = 0;
		final byte[] result = new byte[2];
		result[index++] = (byte) (data >> 8);
		result[index++] = (byte) (data & 0xFF);

		return result;
	}

}
