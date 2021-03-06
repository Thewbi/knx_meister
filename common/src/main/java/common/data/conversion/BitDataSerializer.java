package common.data.conversion;

import api.data.serializer.DataSerializer;

public class BitDataSerializer implements DataSerializer<Object> {

	@Override
	public short[] serialize(final Object data) {
		throw new RuntimeException("Not implemented!");
	}

	@Override
	public byte[] serializeToBytes(final Object data) {

		final int value = (int) data;

		return new byte[] { (byte) value };
	}

	@Override
	public double deserialize(final short[] data) {
		throw new RuntimeException("Not implemented!");
	}

	@Override
	public double deserializeFromBytes(final byte[] data) {
		return data[0];
	}

}
