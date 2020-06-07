package core.pipeline;

import java.net.DatagramPacket;

import core.api.pipeline.PipelineStep;
import core.common.Converter;
import core.packets.KNXPacket;

public class InwardConverterPipelineStep implements PipelineStep<Object, Object> {

	private Converter<byte[], KNXPacket> knxPacketConverter;

	@Override
	public Object execute(final Object datagramPacket) {
		if (datagramPacket == null) {
			return null;
		}
		return knxPacketConverter.convert(((DatagramPacket) datagramPacket).getData());
	}

	public void setKnxPacketConverter(final Converter<byte[], KNXPacket> knxPacketConverter) {
		this.knxPacketConverter = knxPacketConverter;
	}

}
