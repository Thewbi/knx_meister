package core.pipeline;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.pipeline.PipelineStep;
import common.utils.Utils;
import core.packets.KNXPacket;

/**
 * Input: KNXPacket<br />
 * <br />
 * Output: The same unchanged KNXPacket<br />
 * <br />
 * Side Effect: prints the packet to the console.
 */
public class OutwardOutputPipelineStep implements PipelineStep<Object, Object> {

	private static final Logger LOG = LogManager.getLogger(OutwardOutputPipelineStep.class);

	private String prefix;

	/**
	 * If a packet's service identifier is contained in this set (in lowercase) the
	 * packet is not output.
	 */
	private final Set<String> ignorePackets = new HashSet<>();

	@Override
	public Object execute(final Object source) throws Exception {

		if (source == null) {
			return null;
		}

		final Object[] objectArray = (Object[]) source;
		final KNXPacket knxPacket = (KNXPacket) objectArray[0];

		// ignore packets
		final String serviceIdentifier = knxPacket.getHeader().getServiceIdentifier().name();
		if (ignorePackets.contains(serviceIdentifier.toLowerCase(Locale.getDefault()))) {
			return source;
		}

		LOG.info("<<<<<<<<<<<<< {} {} {}", prefix, serviceIdentifier, Utils.retrieveCurrentTimeAsString());
		LOG.info("\n" + knxPacket.toString());

		return source;
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
