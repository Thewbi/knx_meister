package core.packets;

import core.common.Utils;

/**
 * Tunneling Specification - 4.4.6 TUNNELLING_REQUEST
 */
public class CemiTunnelRequest {

	private int cemiTunnelRequestLength = 10;

	/** (1 Byte) */
	private short messageCode;

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

	/** (1 Byte) */
	private int tpci;

	/** (1 Byte) */
	private Integer apci;

	private byte[] payloadBytes;

	private int payloadLength;

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
		tpci = cemiTunnelRequest.tpci;
		apci = cemiTunnelRequest.apci;
		if (cemiTunnelRequest.getPayloadBytes() != null) {
			payloadBytes = cemiTunnelRequest.getPayloadBytes().clone();
		}
		payloadLength = cemiTunnelRequest.payloadLength;
	}

	public void fromBytes(final byte[] source, final int startIndex) {

		cemiTunnelRequestLength = 0;

		// messageCode (1 byte)
		messageCode = (short) ((source[startIndex + 0]) & 0xFF);
		cemiTunnelRequestLength++;

		// additionalInfoLength (1 byte)
		additionalInfoLength = (short) ((source[startIndex + 1]) & 0xFF);
		cemiTunnelRequestLength++;

		// TODO: if additionalInfoLength is greater then 0, where is the additional data
		// stored???

		ctrl1 = ((source[startIndex + 2]) & 0xFF);
		cemiTunnelRequestLength++;

		ctrl2 = ((source[startIndex + 3]) & 0xFF);
		cemiTunnelRequestLength++;

		sourceKNXAddress = Utils.bytesToUnsignedShort(source[startIndex + 4], source[startIndex + 5], true);
		cemiTunnelRequestLength += 2;

		destKNXAddress = Utils.bytesToUnsignedShort(source[startIndex + 6], source[startIndex + 7], true);
		cemiTunnelRequestLength += 2;

		length = ((source[startIndex + 8]) & 0xFF);
		cemiTunnelRequestLength++;

		tpci = ((source[startIndex + 9]) & 0xFF);
		cemiTunnelRequestLength++;

		if (length > 0) {

			apci = ((source[startIndex + 10]) & 0xFF);
			cemiTunnelRequestLength++;

			// How to detect payload bytes?????????????
			// maybe via header total length?
			payloadLength = length - 1;
			if (payloadLength > 0) {
				payloadBytes = new byte[payloadLength];

				System.arraycopy(source, startIndex + 11, payloadBytes, 0, payloadLength);

				cemiTunnelRequestLength += payloadLength;
			}
		}
	}

	public byte[] getBytes() {

		int index = 0;

		cemiTunnelRequestLength = 10;
		length = 0;

		if (apci != null) {
			cemiTunnelRequestLength++;
			length++;
		}

		payloadLength = 0;
		if (payloadBytes != null) {
			payloadLength = payloadBytes.length;
			cemiTunnelRequestLength += payloadLength;
			length += payloadLength;

		}
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

		payload[index++] = (byte) (tpci & 0xFF);

		if (apci != null) {

			payload[index++] = (byte) (apci.intValue() & 0xFF);

			if (payloadBytes != null) {
				System.arraycopy(payloadBytes, 0, payload, index, payloadLength);
			}
		}

		return payload;
	}

	public int getCemiTunnelRequestLength() {
		return cemiTunnelRequestLength;
	}

	public void setCemiTunnelRequestLength(final int cemiTunnelRequestLength) {
		this.cemiTunnelRequestLength = cemiTunnelRequestLength;
	}

	public short getMessageCode() {
		return messageCode;
	}

	public void setMessageCode(final short messageCode) {
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

	public Integer getApci() {
		return apci;
	}

	public void setApci(final int apci) {
		this.apci = apci;
	}

	public byte[] getPayloadBytes() {
		return payloadBytes;
	}

	public void setPayloadBytes(final byte[] payloadBytes) {
		this.payloadBytes = payloadBytes;
	}

	public int getTpci() {
		return tpci;
	}

	public void setTpci(final int tpci) {
		this.tpci = tpci;
	}

}
