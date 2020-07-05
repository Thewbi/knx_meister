package common.data.conversion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class Float16DataSerializerTest {

	@Test
	public void testA_ConvertFloatToKNX2OctetFloat() {

		final Float16DataSerializer float16DataSerializer = new Float16DataSerializer();
		final short[] serialize = float16DataSerializer.serialize(16.0d);

		assertEquals(6, serialize[0]);
		assertEquals(64, serialize[1]);

		final double deserialize = float16DataSerializer.deserialize(serialize);

		assertEquals(16, deserialize);
	}

	@Test
	public void testB_ConvertFloatToKNX2OctetFloat() {

		final Float16DataSerializer float16DataSerializer = new Float16DataSerializer();
		final byte[] serialize = float16DataSerializer.serializeToBytes(28.2d);

		assertEquals(0x0D, (short) (serialize[0] & 0xFF));
		assertEquals(0x82, (short) (serialize[1] & 0xFF));

		final double deserialize = float16DataSerializer.deserializeFromBytes(serialize);

		assertEquals(28.2f, deserialize, 0.000001);
	}

}
