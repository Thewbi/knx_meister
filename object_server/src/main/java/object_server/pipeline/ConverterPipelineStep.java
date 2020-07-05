package object_server.pipeline;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.factory.Factory;
import api.pipeline.PipelineStep;
import common.packets.KNXConnectionHeader;
import common.packets.KNXHeader;
import common.packets.ServiceIdentifier;
import common.utils.Utils;
import object_server.requests.BaseRequest;

public class ConverterPipelineStep implements PipelineStep<Object, Object> {

	private static final Logger LOG = LogManager.getLogger(ConverterPipelineStep.class);

	private Factory<BaseRequest> requestFactory;

	@Override
	public Object execute(final Object source) throws Exception {

		final byte[] bytes = (byte[]) source;

		LOG.trace("Received Bytes: " + Utils.integerToStringNoPrefix(bytes));

		int offset = 0;

		final KNXHeader knxHeader = new KNXHeader();
		knxHeader.fromBytes(bytes, offset);

		if (knxHeader.getServiceIdentifier() != ServiceIdentifier.OBJECT_SERVER_REQUEST) {
			throw new IllegalArgumentException(
					"The input data is not a valid ObjectServer message! Service Identifier is not OBJECT_SERVER_REQUEST!");
		}
		if (knxHeader.getProtocolVersion() != 0x20) {
			throw new IllegalArgumentException(
					"The input data is not a valid ObjectServer message! Version is not 0x20!");
		}

		offset += knxHeader.getLength();

		final KNXConnectionHeader knxConnectionHeader = new KNXConnectionHeader();
		knxConnectionHeader.fromBytes(bytes, offset);

		offset += 4;

		final boolean bigEndian = true;
		final int mainService = Utils.bytesToUnsignedShort((byte) 0, bytes[offset++], bigEndian);

		if (mainService != 0xF0) {
			throw new IllegalArgumentException("The input data is not a valid ObjectServer message!");
		}

		final int subService = Utils.bytesToUnsignedShort((byte) 0, bytes[offset++], bigEndian);

		return requestFactory.create(subService, bytes, knxHeader, knxConnectionHeader);
	}

	public void setRequestFactory(final Factory<BaseRequest> requestFactory) {
		this.requestFactory = requestFactory;
	}

}
