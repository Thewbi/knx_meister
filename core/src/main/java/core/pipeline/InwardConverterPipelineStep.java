package core.pipeline;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.pipeline.PipelineStep;
import common.packets.KNXHeader;
import core.common.KNXPacketConverter;
import core.packets.KNXPacket;

public class InwardConverterPipelineStep implements PipelineStep<Object, Object> {

	private static final Logger LOG = LogManager.getLogger(InwardConverterPipelineStep.class);

	private final List<KNXPacketConverter<byte[], KNXPacket>> converters = new ArrayList<>();

	private final KNXHeader header = new KNXHeader();

	@Override
	public Object execute(final Object dataAsObject) {

		if (dataAsObject == null) {
			return null;
		}

		final Object[] data = (Object[]) dataAsObject;
		final DatagramPacket datagramPacket = (DatagramPacket) data[1];

		// retrieve KNX header
		header.fromBytes(datagramPacket.getData(), 0);

		for (final KNXPacketConverter<byte[], KNXPacket> converter : converters) {

			LOG.trace(header.getServiceIdentifier().name());

			if (converter.accept(header)) {
				data[1] = converter.convert(datagramPacket.getData());
				return data;
			}
		}

		LOG.warn("No converter accepts {}", header.getServiceIdentifier().name());

		return null;
	}

	public List<KNXPacketConverter<byte[], KNXPacket>> getConverters() {
		return converters;
	}

}
