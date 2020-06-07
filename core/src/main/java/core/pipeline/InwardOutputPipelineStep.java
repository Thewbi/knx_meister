package core.pipeline;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.api.pipeline.PipelineStep;
import core.common.Utils;
import core.packets.KNXPacket;

public class InwardOutputPipelineStep implements PipelineStep<Object, Object> {

	private static final Logger LOG = LogManager.getLogger(InwardOutputPipelineStep.class);

	private String prefix;

	@Override
	public Object execute(final Object source) throws Exception {

		if (source == null) {
			return null;
		}

		final KNXPacket knxPacket = (KNXPacket) source;

		LOG.info(">>>>>>>>>>>>> {} {} {}", prefix, knxPacket.getHeader().getServiceIdentifier().name(),
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
