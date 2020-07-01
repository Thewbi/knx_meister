package core.pipeline;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.pipeline.PipelineStep;
import common.utils.Utils;
import core.packets.KNXPacket;

public class InwardOutputPipelineStep implements PipelineStep<Object, Object> {

	private static final Logger LOG = LogManager.getLogger(InwardOutputPipelineStep.class);

	private String prefix;

	/**
	 * If a packet's service identifier is contained in this set (in lowercase) the
	 * packet is not output.
	 */
	private final Set<String> ignorePackets = new HashSet<>();

	@Override
	public Object execute(final Object dataAsObject) throws Exception {

		if (dataAsObject == null) {
			return null;
		}

		final Object[] data = (Object[]) dataAsObject;

		final KNXPacket knxPacket = (KNXPacket) data[1];

		// ignore packets
		final String serviceIdentifier = knxPacket.getHeader().getServiceIdentifier().name();
		if (ignorePackets.contains(serviceIdentifier.toLowerCase(Locale.getDefault()))) {
			return dataAsObject;
		}

		LOG.info(">>>>>>>>>>>>> {} {} {}", prefix, serviceIdentifier, Utils.retrieveCurrentTimeAsString());
		LOG.info("\n" + knxPacket.toString());

		return dataAsObject;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(final String prefix) {
		this.prefix = prefix;
	}

	public Set<String> getIgnorePackets() {
		return ignorePackets;
	}

}
