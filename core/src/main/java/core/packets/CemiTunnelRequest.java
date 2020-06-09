package core.packets;

import core.common.Utils;

/**
 * Tunneling Specification - 4.4.6 TUNNELLING_REQUEST
 */
public class CemiTunnelRequest {

	private int cemiTunnelRequestLength = 11;

	/** (1 Byte) */
	private int messageCode;

	/** (1 Byte) */
	private int additionalInfoLength;

	/** (1 Byte) */
	private int ctrl1;

	/** (1 Byte) */
	private int ctrl2;

	/** (2 Byte) */
	private int sourceKNXAddress;

	/** (2 Byte) */
	private int destKNXAddress;

	/** (1 Byte) */
	private int length;

	/** (2 Byte) */
	private int apci;

	/**
	 * ctor
	 */
	public CemiTunnelRequest() {
	}

	/**
	 * copy ctor
	 *
	 * @param cemiTunnelRequest the object to copy
	 */
	public CemiTunnelRequest(final CemiTunnelRequest cemiTunnelRequest) {
		messageCode = cemiTunnelRequest.messageCode;
		additionalInfoLength = cemiTunnelRequest.additionalInfoLength;
		ctrl1 = cemiTunnelRequest.ctrl1;
		ctrl2 = cemiTunnelRequest.ctrl2;
		sourceKNXAddress = cemiTunnelRequest.sourceKNXAddress;
		destKNXAddress = cemiTunnelRequest.destKNXAddress;
		length = cemiTunnelRequest.length;
		apci = cemiTunnelRequest.apci;
	}

	public void fromBytes(final byte[] source, final int startIndex) {

		// messageCode (1 byte)
		messageCode = (short) ((source[startIndex + 0]) & 0xFF);

		// additionalInfoLength (1 byte)
		additionalInfoLength = (short) ((source[startIndex + 1]) & 0xFF);

		// TODO: if additionalInfoLength is greater then 0, where is the additional data
		// stored???

		ctrl1 = ((source[startIndex + 2]) & 0xFF);
		ctrl2 = ((source[startIndex + 3]) & 0xFF);

		sourceKNXAddress = Utils.bytesToUnsignedShort(source[startIndex + 4], source[startIndex + 5], true);
		destKNXAddress = Utils.bytesToUnsignedShort(source[startIndex + 6], source[startIndex + 7], true);

		length = ((source[startIndex + 8]) & 0xFF);

		apci = Utils.bytesToUnsignedShort(source[startIndex + 9], source[startIndex + 10], true);
	}

	public byte[] getBytes() {

		int index = 0;

		final byte[] payload = new byte[cemiTunnelRequestLength];

		payload[index++] = (byte) (messageCode & 0xFF);

		payload[index++] = (byte) (additionalInfoLength & 0xFF);

		payload[index++] = (byte) (ctrl1 & 0xFF);

		payload[index++] = (byte) (ctrl2 & 0xFF);

		payload[index++] = (byte) (((sourceKNXAddress) >> 8) & 0xFF);
		payload[index++] = (byte) (sourceKNXAddress & 0xFF);

		payload[index++] = (byte) (((destKNXAddress) >> 8) & 0xFF);
		payload[index++] = (byte) (destKNXAddress & 0xFF);

		payload[index++] = (byte) (length & 0xFF);

		payload[index++] = (byte) (((apci) >> 8) & 0xFF);
		payload[index++] = (byte) (apci & 0xFF);

		return payload;
	}

	public int getCemiTunnelRequestLength() {
		return cemiTunnelRequestLength;
	}

	public void setCemiTunnelRequestLength(final int cemiTunnelRequestLength) {
		this.cemiTunnelRequestLength = cemiTunnelRequestLength;
	}

	public int getMessageCode() {
		return messageCode;
	}

	public void setMessageCode(final int messageCode) {
		this.messageCode = messageCode;
	}

	public int getAdditionalInfoLength() {
		return additionalInfoLength;
	}

	public void setAdditionalInfoLength(final int additionalInfoLength) {
		this.additionalInfoLength = additionalInfoLength;
	}

	public int getLength() {
		return length;
	}

	public void setLength(final int length) {
		this.length = length;
	}

	public int getCtrl1() {
		return ctrl1;
	}

	public void setCtrl1(final int ctrl1) {
		this.ctrl1 = ctrl1;
	}

	public int getCtrl2() {
		return ctrl2;
	}

	public void setCtrl2(final int ctrl2) {
		this.ctrl2 = ctrl2;
	}

	public int getSourceKNXAddress() {
		return sourceKNXAddress;
	}

	public void setSourceKNXAddress(final int sourceKNXAddress) {
		this.sourceKNXAddress = sourceKNXAddress;
	}

	public int getDestKNXAddress() {
		return destKNXAddress;
	}

	public void setDestKNXAddress(final int destKNXAddress) {
		this.destKNXAddress = destKNXAddress;
	}

	public int getApci() {
		return apci;
	}

	public void setApci(final int apci) {
		this.apci = apci;
	}

}
