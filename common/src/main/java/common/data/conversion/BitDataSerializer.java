package common.data.conversion;

import api.data.serializer.DataSerializer;

public class BitDataSerializer implements DataSerializer<Object> {

    @Override
    public short[] serialize(final Object data) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public byte[] serializeToBytes(final Object data) {

        int result = 0;

        if (data instanceof Double) {

            final Double doubleObject = (Double) data;
            result = doubleObject.intValue();

        } else if (data instanceof Integer) {

            final Integer integerObject = (Integer) data;
            result = integerObject.intValue();

        }

        return new byte[] { (byte) result };
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
