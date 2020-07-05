package object_server.requests;

import api.project.KNXProject;
import common.packets.KNXConnectionHeader;
import common.packets.KNXHeader;
import common.packets.ServiceIdentifier;

public abstract class BaseRequest {

	public static final int GET_SERVER_ITEM_RESPONSE_CODE = 0x81;

	public static final int SET_SERVER_ITEM_RESPONSE_CODE = 0x82;

	public static final int GET_DATAPOINT_DESCRIPTION_RESPONSE_CODE = 0x83;

	protected static final int OBJECT_SERVER_PROTOCOL_VERSION = 0x20;

	private static final int MAIN_SERVICE_CODE = 0xF0;

	/**
	 * 16 = KNXHeader (6 byte) + Connection Header (4 Byte) +
	 * ObjectServer+MainService+SubService+StartItem+NumberOfItems (6 Byte)
	 */
	protected static final int REQUEST_PRELUDE_LENGTH = 16;

	protected final int mainService = MAIN_SERVICE_CODE;

	private int subService;

	protected KNXHeader knxHeader = new KNXHeader();

	protected KNXConnectionHeader knxConnectionHeader = new KNXConnectionHeader();

	private int start;

	private int maxAmount;

	private KNXProject knxProject;

	public abstract int getMessageLength();

	/**
	 * knxHeader and knxConnection has already been parsed. The implemented merely
	 * has to extract the payload from the byte array.
	 *
	 * @param bytes
	 */
	public abstract void fromBytes(byte[] bytes);

	private byte[] payload;

	public byte[] getBytes() {

		final byte[] payload = new byte[getMessageLength()];

		int index = 0;

		knxHeader.setTotalLength(getMessageLength());
		knxHeader.setProtocolVersion((byte) OBJECT_SERVER_PROTOCOL_VERSION);
		knxHeader.setServiceIdentifier(ServiceIdentifier.OBJECT_SERVER_REQUEST);

		knxHeader.writeBytesIntoBuffer(payload, index);
		index += knxHeader.getLength();

		knxConnectionHeader.writeBytesIntoBuffer(payload, index);
		index += knxConnectionHeader.getLength();

		payload[index++] = (byte) (mainService & 0xFF);
		payload[index++] = (byte) (getSubService() & 0xFF);

		payload[index++] = (byte) (((getStart()) >> 8) & 0xFF);
		payload[index++] = (byte) (getStart() & 0xFF);

		payload[index++] = (byte) (((getMaxAmount()) >> 8) & 0xFF);
		payload[index++] = (byte) (getMaxAmount() & 0xFF);

		return payload;
	}

	public KNXHeader getKnxHeader() {
		return knxHeader;
	}

	public void setKnxHeader(final KNXHeader knxHeader) {
		this.knxHeader = knxHeader;
	}

	public KNXConnectionHeader getKnxConnectionHeader() {
		return knxConnectionHeader;
	}

	public void setKnxConnectionHeader(final KNXConnectionHeader knxConnectionHeader) {
		this.knxConnectionHeader = knxConnectionHeader;
	}

	public byte[] getPayload() {
		return payload;
	}

	public void setPayload(final byte[] payload) {
		this.payload = payload;
	}

	public int getStart() {
		return start;
	}

	public void setStart(final int start) {
		this.start = start;
	}

	public int getMaxAmount() {
		return maxAmount;
	}

	public void setMaxAmount(final int maxAmount) {
		this.maxAmount = maxAmount;
	}

	public int getMainService() {
		return mainService;
	}

	public void setSubService(final int subService) {
		this.subService = subService;
	}

	public int getSubService() {
		return subService;
	}

	public KNXProject getKnxProject() {
		return knxProject;
	}

	public void setKnxProject(final KNXProject knxProject) {
		this.knxProject = knxProject;
	}

}
