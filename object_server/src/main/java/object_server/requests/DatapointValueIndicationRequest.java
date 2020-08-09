package object_server.requests;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import common.packets.ServiceIdentifier;
import common.utils.Utils;

public class DatapointValueIndicationRequest extends BaseRequest {

	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger(DatapointValueIndicationRequest.class);

	public static final int DATAPOINTVALUE_INDICATION_REQUEST_CODE = 0xC1;

	public DatapointValueIndicationRequest() {
		setSubService(DATAPOINTVALUE_INDICATION_REQUEST_CODE);
	}

	@Override
	public int getMessageLength() {
		return REQUEST_PRELUDE_LENGTH + getPayload().length;
	}

	@Override
	public void fromBytes(final byte[] bytes) {

		LOG.info("fromBytes()");

		int preludeLength = 12;

		// start, length
		final boolean bigEndian = true;
		setStart(Utils.bytesToUnsignedShort(bytes[preludeLength + 0], bytes[preludeLength + 1], bigEndian));
		setMaxAmount(Utils.bytesToUnsignedShort(bytes[preludeLength + 2], bytes[preludeLength + 3], bigEndian));

		preludeLength += 4;

		throw new RuntimeException("Not implemented exception!");
	}

	@Override
	public byte[] getBytes() {

		final byte[] data = super.getBytes();

		int index = 0;

		knxHeader.setTotalLength(getMessageLength());
		knxHeader.setProtocolVersion((byte) OBJECT_SERVER_PROTOCOL_VERSION);
		knxHeader.setServiceIdentifier(ServiceIdentifier.OBJECT_SERVER_REQUEST);

		knxHeader.writeBytesIntoBuffer(data, index);
		index += knxHeader.getLength();

		knxConnectionHeader.writeBytesIntoBuffer(data, index);
		index += knxConnectionHeader.getLength();

		data[index++] = (byte) (mainService & 0xFF);
		data[index++] = (byte) (getSubService() & 0xFF);

		data[index++] = (byte) (((getStart()) >> 8) & 0xFF);
		data[index++] = (byte) (getStart() & 0xFF);

		data[index++] = (byte) (((getMaxAmount()) >> 8) & 0xFF);
		data[index++] = (byte) (getMaxAmount() & 0xFF);

		System.arraycopy(getPayload(), 0, data, REQUEST_PRELUDE_LENGTH, getPayload().length);

		return data;
	}

}
