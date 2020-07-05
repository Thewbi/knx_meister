package object_server.requests;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import common.utils.Utils;

public class SetDatapointValueRequest extends BaseRequest {

	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger(SetDatapointValueRequest.class);

	public static final int SET_DATAPOINT_VALUE_REQUEST_CODE = 0x06;

	public static final int SET_DATAPOINT_VALUE_RESPONSE_CODE = 0x86;

	private int length;

	private final int valueAmount = 1;

	private final List<SetDatapointValueRequestEntry> entries = new ArrayList<>();

	public SetDatapointValueRequest() {
		setSubService(SET_DATAPOINT_VALUE_REQUEST_CODE);
	}

	@Override
	public int getMessageLength() {
		return REQUEST_PRELUDE_LENGTH + (4 + length) * valueAmount;
	}

	@Override
	public void fromBytes(final byte[] bytes) {

		final int preludeLength = 12;

		// start, length
		final boolean bigEndian = true;
		setStart(Utils.bytesToUnsignedShort(bytes[preludeLength + 0], bytes[preludeLength + 1], bigEndian));
		setMaxAmount(Utils.bytesToUnsignedShort(bytes[preludeLength + 2], bytes[preludeLength + 3], bigEndian));

		int index = preludeLength + 4;

		for (int i = 0; i < getMaxAmount(); i++) {

			final SetDatapointValueRequestEntry entry = new SetDatapointValueRequestEntry();

			// datapoint id
			entry.setDatapointId(Utils.bytesToUnsignedShort(bytes[index++], bytes[index++], bigEndian));

			// command
			entry.setCommand(bytes[index++]);

			// length
			length = bytes[index++];
			entry.setLength(length);

			// value
			final byte[] valueAsBytes = new byte[length];
			System.arraycopy(bytes, index, valueAsBytes, 0, length);
			entry.setValueAsBytes(valueAsBytes);

			entries.add(entry);
		}
	}

	public List<SetDatapointValueRequestEntry> getEntries() {
		return entries;
	}

}
