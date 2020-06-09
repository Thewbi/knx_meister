package core.pipeline;

import java.net.InetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.api.pipeline.PipelineStep;
import core.packets.HPAIStructure;
import core.packets.KNXPacket;
import core.packets.StructureType;

public class IpFilterPipelineStep implements PipelineStep<Object, Object> {

	private static final String BLACKLISTED_IP = "192.168.56.1";

	private static final Logger LOG = LogManager.getLogger(IpFilterPipelineStep.class);

	@Override
	public Object execute(final Object dataAsObject) throws Exception {

		if (dataAsObject == null) {
			return null;
		}

		final Object[] data = (Object[]) dataAsObject;

		final KNXPacket knxPacket = (KNXPacket) data[1];
		final HPAIStructure hpaiStructure = (HPAIStructure) knxPacket.getStructureMap()
				.get(StructureType.HPAI_CONTROL_ENDPOINT_UDP);
		if (hpaiStructure != null && hpaiStructure.getIpAddressAsObject() != null
				&& hpaiStructure.getIpAddressAsObject().equals(InetAddress.getByName(BLACKLISTED_IP))) {
			LOG.warn("Ignoring packet from IP {}", BLACKLISTED_IP);
			return null;
		}

		return dataAsObject;
	}

}
