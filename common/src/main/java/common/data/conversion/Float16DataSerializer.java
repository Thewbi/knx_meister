package common.data.conversion;

import api.data.serializer.DataSerializer;

/**
 * https://github.com/calimero-project/calimero-core/blob/master/src/tuwien/auto/calimero/dptxlator/DPTXlator2ByteFloat.java
 *
 * <pre>
 * Format: 2 octets: F16
 * octet nr 2 MSB 1 LSB
 * field names FloatValue
 * encoding M E E E E MMM MMMMMMMMM
 * Encoding
 * : FloatValue = (0,01*M)*2(E)
 * E = [0 … 15]
 * M = [-2 048 … 2 047], two’s complement notation
 * For all Datapoint Types 9.xxx, the encoded value 7FFFh shall always be used to denote invalid
 * data.
 * Range: [-671 088,64 … 670 760,96]
 * PDT: PDT_KNX_FLOAT
 * </pre>
 */
public class Float16DataSerializer implements DataSerializer<Object> {

	@Override
	public short[] serialize(final Object data) {

		final double value = (double) data;
		double v = value * 100.0f;
		int e = 0;
		for (; v < -2048.0f; v /= 2) {
			e++;
		}
		for (; v > 2047.0f; v /= 2) {
			e++;
		}
		final int m = (int) Math.round(v) & 0x7FF;
		short msb = (short) (e << 3 | m >> 8);
		if (value < 0.0) {
			msb |= 0x80;
		}

		return new short[] { msb, (short) (m & 0xFF) };
	}

	@Override
	public byte[] serializeToBytes(final Object data) {
		final short[] result = serialize(data);

		return new byte[] { (byte) result[0], (byte) result[1] };
	}

	@Override
	public double deserialize(final short[] data) {

		final int i = 0;

		// DPT bits high byte: MEEEEMMM, low byte: MMMMMMMM
		// left align all mantissa bits
		int v = ((data[i] & 0x80) << 24) | ((data[i] & 0x7) << 28) | (data[i + 1] << 20);

		// normalize
		v >>= 20;
		final int exp = (data[i] & 0x78) >> 3;

		return (1 << exp) * v * 0.01;
	}

	@Override
	public double deserializeFromBytes(final byte[] data) {

		final short[] dataAsShortArray = new short[] { (short) (data[0] & 0xFF), (short) (data[1] & 0xFF) };

		return deserialize(dataAsShortArray);
	}

}
