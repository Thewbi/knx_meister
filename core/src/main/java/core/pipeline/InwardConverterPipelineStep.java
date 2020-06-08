package core.pipeline;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.api.pipeline.PipelineStep;
import core.common.KNXPacketConverter;
import core.packets.Header;
import core.packets.KNXPacket;

public class InwardConverterPipelineStep implements PipelineStep<Object, Object> {

	private static final Logger LOG = LogManager.getLogger(InwardConverterPipelineStep.class);

	private final List<KNXPacketConverter<byte[], KNXPacket>> converters = new ArrayList<>();

	private final Header header = new Header();

	@Override
	public Object execute(final Object datagramPacketAsObject) {

		if (datagramPacketAsObject == null) {
			return null;
		}

		final DatagramPacket datagramPacket = (DatagramPacket) datagramPacketAsObject;

		// retrieve KNX header
		header.fromBytes(datagramPacket.getData(), 0);

		for (final KNXPacketConverter<byte[], KNXPacket> converter : converters) {

			LOG.info(header.getServiceIdentifier().name());

			if (converter.accept(header)) {
				return converter.convert(datagramPacket.getData());
			}
		}

		return null;
	}

	public List<KNXPacketConverter<byte[], KNXPacket>> getConverters() {
		return converters;
	}

}
