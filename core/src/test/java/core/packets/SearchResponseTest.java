package core.packets;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import core.common.Converter;
import core.conversion.CoreKNXPacketConverter;

public class SearchResponseTest {

	@Test
	public void testSearchResponse() {

		final byte[] source = new byte[] { 0x06, 0x10, 0x02, 0x02, 0x00, 0x54, 0x08, 0x01, (byte) 0xc0, (byte) 0xa8,
				0x02, 0x03, (byte) 0x0e, 0x57, 0x36, 0x01, 0x02, 0x00, 0x11, 0x01, 0x00, 0x00, 0x00, (byte) 0xc5, 0x01,
				0x02, (byte) 0xd8, (byte) 0x4c, (byte) 0xe0, 0x00, 0x17, (byte) 0x0c, 0x00, 0x24, 0x6d, 0x01,
				(byte) 0xd8, (byte) 0x0a, (byte) 0x4b, 0x4e, 0x58, 0x20, 0x49, 0x50, 0x20, 0x42, 0x41, 0x4f, 0x53, 0x20,
				0x37, 0x37, 0x37, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x08, 0x02, 0x02, 0x01, 0x03, 0x02, 0x04, 0x01, 0x08, (byte) 0xfe, 0x00, (byte) 0xc5, 0x01, 0x04,
				(byte) 0xf0, 0x20 };

		final Converter<byte[], KNXPacket> knxPacketConverter = new CoreKNXPacketConverter();

		final KNXPacket knxPacket = knxPacketConverter.convert(source);

		System.out.println(knxPacket);

		//
		// Header
		//

		// 1 byte header length
		assertEquals(0x06, knxPacket.getHeader().getLength());

		// protocol version in Binary Coded Decimal (1.0 = 10)
		assertEquals(0x10, knxPacket.getHeader().getProtocolVersion());

		// service identifier 0x0203
		assertEquals(ServiceIdentifier.SEARCH_RESPONSE, knxPacket.getHeader().getServiceIdentifier());

		// total KNX message length, 14d = 0x0e in this test
		assertEquals(source.length, knxPacket.getHeader().getTotalLength());

		//
		// HPAI
		//

		final HPAIStructure hpaiStructure = (HPAIStructure) knxPacket.getStructureMap()
				.get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);
		assertEquals(0x08, hpaiStructure.getLength());
		assertEquals(StructureType.HPAI_CONTROL_ENDPOINT_UDP, hpaiStructure.getStructureType());
		assertEquals(192, hpaiStructure.getIpAddress()[0] & 0xFF);
		assertEquals(168, hpaiStructure.getIpAddress()[1] & 0xFF);
		assertEquals(2, hpaiStructure.getIpAddress()[2] & 0xFF);
		assertEquals(3, hpaiStructure.getIpAddress()[3] & 0xFF);
		assertEquals(3671, hpaiStructure.getPort());

	}

}
