package core.pipeline;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.api.pipeline.PipelineStep;
import core.common.Utils;
import core.packets.KNXPacket;

public class OutwardOutputPipelineStep implements PipelineStep<Object, Object> {

	private static final Logger LOG = LogManager.getLogger(OutwardOutputPipelineStep.class);

	private String prefix;

	@Override
	public Object execute(final Object source) throws Exception {

		if (source == null) {
			return null;
		}

		final Object[] objectArray = (Object[]) source;

		final KNXPacket knxPacket = (KNXPacket) objectArray[0];
		LOG.info("<<<<<<<<<<<<< {} {} {}", prefix, knxPacket.getHeader().getServiceIdentifier().name(),
				Utils.retrieveCurrentTimeAsString());
		LOG.info("\n" + knxPacket.toString());

		return source;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(final String prefix) {
		this.prefix = prefix;
	}

}
