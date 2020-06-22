package core.conversion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import core.common.Converter;
import core.common.Utils;
import core.packets.HPAIStructure;
import core.packets.KNXPacket;
import core.packets.ServiceIdentifier;
import core.packets.StructureType;

public class KNXPacketConverterTest {

	private static final Logger LOG = LogManager.getLogger(KNXPacketConverterTest.class);

	@Test
	public void testConvert() {

		final byte[] data = new byte[] { (byte) 0x06, (byte) 0x10, (byte) 0x02, (byte) 0x03, (byte) 0x00, (byte) 0x0E,
				(byte) 0x08, (byte) 0x01, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x04,
				(byte) 0xD2 };

		final Converter<byte[], KNXPacket> knxPacketConverter = new CoreKNXPacketConverter();

		final KNXPacket knxPacket = knxPacketConverter.convert(data);

		assertEquals(0x06, knxPacket.getHeader().getLength());
		assertEquals(0x10, knxPacket.getHeader().getProtocolVersion());
		assertEquals(ServiceIdentifier.DESCRIPTION_REQUEST, knxPacket.getHeader().getServiceIdentifier());
		assertEquals(0x0E, knxPacket.getHeader().getTotalLength());

		final HPAIStructure hpaiStructure = (HPAIStructure) knxPacket.getStructureMap()
				.get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);

		// 6 byte payload length (structure length has two more byte)
		assertEquals(0x06, hpaiStructure.getPayloadLength());

		// type HPAI = 0x01
		assertEquals(StructureType.HPAI_CONTROL_ENDPOINT_UDP, hpaiStructure.getStructureType());

		// IP 1.2.3.4
		assertEquals(0x01, hpaiStructure.getIpAddress()[0]);
		assertEquals(0x02, hpaiStructure.getIpAddress()[1]);
		assertEquals(0x03, hpaiStructure.getIpAddress()[2]);
		assertEquals(0x04, hpaiStructure.getIpAddress()[3]);

		// port 1234d = 0x04D2
		assertEquals(0x04D2, hpaiStructure.getPort());
	}

	@Test
	public void convertSearchResponse() {

//		final byte[] data = Utils.hexStringToByteArray(
//				"06100202004c0801c0a80201fde808020201030204013601200111020011030303030303e000170c010203040506746573745f6f626a65637400000000000000000000000000000000000000");

//		final byte[] data = Utils.hexStringToByteArray(
//				"0610020200540801c0a802030e57360102001101000000c50102d84ce000170c00246d01d80a4b4e582049502042414f5320373737000000000000000000000000000000080202010302040108fe00c50104f020");

		final byte[] data = Utils.hexStringToByteArray(
				"0610020200540801C0A80201FDE83601200111020011030303030303E000170C010203040506746573745F6F626A65637400000000000000000000000000000000000000080202010302040108FE00C50104F020");

		final Converter<byte[], KNXPacket> knxPacketConverter = new CoreKNXPacketConverter();

		final KNXPacket knxPacket = knxPacketConverter.convert(data);

		LOG.info(knxPacket);
	}
}
