package core.packets;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import common.utils.Utils;

public class HPAIStructureTest {

	private static final Logger LOG = LogManager.getLogger(HPAIStructureTest.class);

	@Test
	public void testEncode() {

		final HPAIStructure hpaiStructure = new HPAIStructure();
		hpaiStructure.setIpAddress(new byte[] { 1, 2, 3, 4 });
		hpaiStructure.setPort((short) 1234);

		final byte[] bytes = hpaiStructure.getBytes();

		LOG.info(Utils.integerToStringNoPrefix(bytes));

		// 8 byte length
		assertEquals(0x08, bytes[0]);

		// type HPAI = 0x01
		assertEquals(0x01, bytes[1]);

		// IP 1.2.3.4
		assertEquals(0x01, bytes[2]);
		assertEquals(0x02, bytes[3]);
		assertEquals(0x03, bytes[4]);
		assertEquals(0x04, bytes[5]);

		// port 1234d = 0x04D2
		assertEquals(0x04, bytes[6]);
		assertEquals(0xD2, Utils.bytesToUnsignedShort((byte) 0x00, bytes[7], true));
		assertEquals(0x04D2, Utils.bytesToUnsignedShort(bytes[6], bytes[7], true));

	}

}
