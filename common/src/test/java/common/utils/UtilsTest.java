package common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class UtilsTest {

	@Test
	public void testKnxAddressToInteger() {

		int knxAddressToInteger = Utils.knxAddressToInteger("0.0.0");
		assertEquals(0x0000, knxAddressToInteger);
		knxAddressToInteger = Utils.knxAddressToInteger("0/0/0");
		assertEquals(0x0000, knxAddressToInteger);

		knxAddressToInteger = Utils.knxAddressToInteger("1.0.0");
		assertEquals(0x1000, knxAddressToInteger);
		knxAddressToInteger = Utils.knxAddressToInteger("1/0/0");
		assertEquals(0x1000, knxAddressToInteger);

		knxAddressToInteger = Utils.knxAddressToInteger("0.1.0");
		assertEquals(0x0100, knxAddressToInteger);
		knxAddressToInteger = Utils.knxAddressToInteger("0/1/0");
		assertEquals(0x0100, knxAddressToInteger);

		knxAddressToInteger = Utils.knxAddressToInteger("0.0.1");
		assertEquals(0x0001, knxAddressToInteger);
		knxAddressToInteger = Utils.knxAddressToInteger("0/0/1");
		assertEquals(0x0001, knxAddressToInteger);

		knxAddressToInteger = Utils.knxAddressToInteger("1.2.3");
		assertEquals(0x1203, knxAddressToInteger);
		knxAddressToInteger = Utils.knxAddressToInteger("1/2/3");
		assertEquals(0x1203, knxAddressToInteger);

		knxAddressToInteger = Utils.knxAddressToInteger("1.2.52");
		assertEquals(0x1234, knxAddressToInteger);
		knxAddressToInteger = Utils.knxAddressToInteger("1/2/52");
		assertEquals(0x1234, knxAddressToInteger);
	}

}
