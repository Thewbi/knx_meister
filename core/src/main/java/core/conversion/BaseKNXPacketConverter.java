package core.conversion;

import org.apache.logging.log4j.Logger;

import core.common.KNXPacketConverter;
import core.packets.HPAIStructure;
import core.packets.KNXPacket;

public abstract class BaseKNXPacketConverter implements KNXPacketConverter<byte[], KNXPacket> {

	protected abstract Logger getLogger();

	@Override
	public KNXPacket convert(final byte[] source) {
		final KNXPacket result = new KNXPacket();
		convert(source, result);

		return result;
	}

	protected String retrieveIPFromHPAI(final HPAIStructure structure) {
		String ip = "unknown";
		if (structure != null && structure instanceof HPAIStructure) {
			final HPAIStructure hpaiStructure = structure;
			final byte[] ipAddress = hpaiStructure.getIpAddress();
			ip = (ipAddress[0] & 0xFF) + "." + (ipAddress[1] & 0xFF) + "." + (ipAddress[2] & 0xFF) + "."
					+ (ipAddress[3] & 0xFF);
		}
		return ip;
	}

}
