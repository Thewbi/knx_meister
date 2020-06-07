package core.pipeline;

import java.net.DatagramPacket;
import java.net.SocketAddress;

import core.api.pipeline.PipelineStep;
import core.packets.KNXPacket;

public class OutwardConverterPipelineStep implements PipelineStep<Object, Object> {

	@Override
	public Object execute(final Object source) throws Exception {

		final Object[] objectArray = (Object[]) source;

		final KNXPacket knxPacket = (KNXPacket) objectArray[0];
		final SocketAddress socketAddress = (SocketAddress) objectArray[1];

		final byte[] bytes = knxPacket.getBytes();

		final DatagramPacket outDatagramPacket = new DatagramPacket(bytes, bytes.length, socketAddress);

		return outDatagramPacket;
	}

}
