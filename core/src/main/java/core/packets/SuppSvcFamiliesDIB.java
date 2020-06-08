package core.packets;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.common.Utils;

/**
 * 7.5.4.3 Supported service families DIB
 */
public class SuppSvcFamiliesDIB extends DescriptionInformationBlock {

	private static final Logger LOG = LogManager.getLogger(SuppSvcFamiliesDIB.class);

	private final List<ProtocolDescriptor> protocolDescriptors = new ArrayList<>();

	public SuppSvcFamiliesDIB() {

	}

	public SuppSvcFamiliesDIB(final SuppSvcFamiliesDIB other) {
		setLength(other.getLength());

		if (CollectionUtils.isNotEmpty(other.getProtocolDescriptors())) {
			for (final ProtocolDescriptor protocolDescriptor : other.getProtocolDescriptors()) {
				protocolDescriptors.add(protocolDescriptor.clone());
			}
		}
	}

	@Override
	public SuppSvcFamiliesDIB clone() {
		return new SuppSvcFamiliesDIB(this);
	}

	@Override
	public DescriptionInformationBlockType getType() {
		return DescriptionInformationBlockType.SUPP_SVC_FAMILIES;
	}

	@Override
	public void fromBytes(final byte[] source, final int index) {

		// find the rest length by subtracting the length (1 byte) and type (1 byte)
		// field
		int tempLength = getLength() - 2;

		int tempIndex = index;

		while (tempLength > 0) {

			final int serviceFamilyCode = source[tempIndex++] & 0xFF;
			LOG.trace("ServiceFamily: " + ServiceFamily.fromInt(serviceFamilyCode).name());

			LOG.trace("ServiceFamily Version: " + Utils.bytesToUnsignedShort((byte) 0x00, source[tempIndex++], true));

			final int version = source[tempIndex++] & 0xFF;
			LOG.trace("ServiceFamily Version: " + Utils.integerToString(version));
			tempLength -= 2;

			final ProtocolDescriptor protocoDescriptor = new ProtocolDescriptor();
			protocolDescriptors.add(protocoDescriptor);
			protocoDescriptor.setProtocol(serviceFamilyCode);
			protocoDescriptor.setVersion(version);
		}
	}

	@Override
	public byte[] getBytes() {

		final int length = 2 + protocolDescriptors.size() * 2;
		final byte[] bytes = new byte[length];

		int index = 0;
		bytes[index++] = (byte) length;
		bytes[index++] = (byte) getType().getValue();

		for (final ProtocolDescriptor protocolDescriptor : protocolDescriptors) {
			bytes[index++] = (byte) protocolDescriptor.getProtocol();
			bytes[index++] = (byte) protocolDescriptor.getVersion();
		}

		return bytes;
	}

	@Override
	public String toString() {

		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Structure Length: ").append(getLength()).append("\n");
		stringBuilder.append("Description Type: ").append(getType().name()).append(" ")
				.append(Utils.integerToString(getType().getValue())).append("\n");

		int index = 0;
		if (CollectionUtils.isNotEmpty(protocolDescriptors)) {
			for (final ProtocolDescriptor protocolDescriptor : protocolDescriptors) {
				if (index != 0) {
					stringBuilder.append("\n");
				}
				stringBuilder.append(protocolDescriptor.toString());
				index++;
			}
		}

		return stringBuilder.toString();
	}

	public List<ProtocolDescriptor> getProtocolDescriptors() {
		return protocolDescriptors;
	}

}
