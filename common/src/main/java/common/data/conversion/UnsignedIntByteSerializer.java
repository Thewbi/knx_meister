package common.data.conversion;

import api.data.serializer.DataSerializer;

/**
 * Takes care of unsigned integers encoded in a single byte.
 */
public class UnsignedIntByteSerializer implements DataSerializer<Object> {

	@Override
	public short[] serialize(final Object data) {
		throw new RuntimeException("Not implemented yet!");
	}

	@Override
	public byte[] serializeToBytes(final Object data) {
		return new byte[] { (byte) (((int) data) & 0xFF) };
	}

	@Override
	public double deserialize(final short[] data) {
		throw new RuntimeException("Not implemented yet!");
	}

	@Override
	public double deserializeFromBytes(final byte[] data) {
		return data[0];
	}

}
