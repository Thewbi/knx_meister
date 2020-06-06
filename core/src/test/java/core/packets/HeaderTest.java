package core.packets;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import core.common.Utils;

public class HeaderTest {

	@Test
	public void testGetBytes() {

		Header header = new Header();
		header.setLength((byte) 0x06);
		header.setProtocolVersion((byte) 0x10);
		header.setServiceIdentifier(ServiceIdentifier.DESCRIPTION_REQUEST);
		header.setTotalLength((int) 14);

		byte[] bytes = header.getBytes();

		System.out.println(Utils.integerToStringNoPrefix(bytes));
		
		// 1 byte header length
		assertEquals(0x06, bytes[0]);
		
		// protocol version in Binary Coded Decimal (1.0 = 10)
		assertEquals(0x10, bytes[1]);
		
		// service identifier 0x0203
		assertEquals(0x02, bytes[2]);
		assertEquals(0x03, bytes[3]);
		
		// total KNX message length, 14d = 0x0e in this test
		assertEquals(0x00, bytes[4]);
		assertEquals(0x0e, bytes[5]);
	}
}
